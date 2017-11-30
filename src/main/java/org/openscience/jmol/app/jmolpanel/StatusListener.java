/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2009-06-26 23:35:44 -0500 (Fri, 26 Jun 2009) $
 * $Revision: 11131 $
 *
 * Copyright (C) 2000-2005  The Jmol Development Team
 *
 * Contact: jmol-developers@lists.sf.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.jmol.app.jmolpanel;

import java.awt.Component;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;
import java.util.Properties;

import jspecview.application.MainFrame;

import org.jmol.api.JSVInterface;
import org.jmol.api.JmolAppConsoleInterface;
import org.jmol.api.JmolCallbackListener;
import org.jmol.api.JmolStatusListener;
import org.jmol.api.JmolSyncInterface;
import org.jmol.api.JmolViewer;
import org.jmol.constant.EnumCallback;
import org.jmol.export.dialog.Dialog;
import org.jmol.util.Escape;
import org.jmol.util.Logger;
import org.openscience.jmol.app.webexport.WebExport;

class StatusListener implements JmolStatusListener, JmolSyncInterface, JSVInterface {

  /*
   * starting with Jmol 11.7.27, JmolStatusListener extends JmolCallbackListener
   * 
   * providing a simpler interface if all that is wanted is callback
   * functionality.
   * 
   * Only three methods are involved:
   * 
   * boolean notifyEnabled(int type) -- lets the statusManager know if there is
   * an implementation of a given callback type
   * 
   * void notifyCallback(int type, Object[] data) -- callback action; data
   * varies with callback type -- see org.jmol.viewer.StatusManager for details
   * 
   * void setCallbackFunction(String callbackType, String callbackFunction) --
   * called by statusManager in response to the "set callback" script command --
   * also used by the Jmol application to change menus and languages -- can
   * remain unimplemented if no such user action is intended
   */

  private JmolPanel jmol;
  private DisplayPanel display;

  private JmolViewer viewer;
  private MainFrame jSpecViewFrame;
  void setViewer(JmolViewer viewer) {
    this.viewer = viewer;
  }
  
  StatusListener(JmolPanel jmol, DisplayPanel display) {
    // just required for Jmol application's particular callbacks
    this.jmol = jmol;
    this.display = display;  
  }
  
  // / JmolCallbackListener interface ///
  @Override
public boolean notifyEnabled(EnumCallback type) {
    switch (type) {
    case ANIMFRAME:
    case ECHO:
    case LOADSTRUCT:
    case MEASURE:
    case MESSAGE:
    case PICK:
    case SCRIPT:
    case SYNC:
      return true;
    case EVAL:
    case ATOMMOVED:
    case CLICK:
    case ERROR:
    case HOVER:
    case MINIMIZATION:
    case RESIZE:
    case APPLETREADY:
      // applet only (but you could change this for your listener)
      break;
    }
    return false;
  }

  @Override
@SuppressWarnings("incomplete-switch")
  public void notifyCallback(EnumCallback type, Object[] data) {
    String strInfo = (data == null || data[1] == null ? null : data[1]
        .toString());
    switch (type) {
    case LOADSTRUCT:
      notifyFileLoaded(strInfo, (String) data[2], (String) data[3],
          (String) data[4]);
      return;
    case ANIMFRAME:
      int[] iData = (int[]) data[1];
      int modelIndex = iData[0];
      if (modelIndex <= -2)
        modelIndex = -2 - modelIndex; // animation is running
      //int file = iData[1];
      //int model = iData[2];
      if (display.haveDisplay) {
        String menuName = (String) data[2];
        display.status.setStatus(1, menuName);
        if (jmol.frame != null)
          jmol.frame.setTitle("GenomeFlow"); //modified lxq35 menuName->"Gmol"
      }
      return;
    case SCRIPT:
      int msWalltime = ((Integer) data[3]).intValue();
      if (msWalltime == 0) {
        if (data[2] != null && display.haveDisplay)
          display.status.setStatus(1, (String) data[2]);
      }
      return;
    case ECHO:
      break;
    case MEASURE:
      String mystatus = (String) data[3];
      if (mystatus.indexOf("Sequence") < 0) {
        if (mystatus.indexOf("Pending") < 0 && display.haveDisplay)
          display.measurementTable.updateTables();
        if (mystatus.indexOf("Picked") >= 0) // picking mode
          notifyAtomPicked(strInfo);
        else if (mystatus.indexOf("Completed") < 0)
          return;
      }
      break;
    case MESSAGE:
      break;
    //    case CLICK:
    // x, y, action, int[] {action}
    // the fourth parameter allows an application to change the action
    //      if (display.haveDisplay)
    //        display.status
    //          .setStatus(1, "(" + data[1] + "," + data[2] + ")");
    //      break;
    case PICK:
      notifyAtomPicked(strInfo);
      break;
    case SYNC:
      if (strInfo != null && strInfo.toLowerCase().startsWith("jspecview")) {
        setJSpecView(strInfo.substring(9).trim());
        return;
      }
      jmol.sendNioMessage(((Integer) data[3]).intValue(), strInfo);
      return;
    case ERROR:
    case HOVER:
    case MINIMIZATION:
    case RESIZE:
      // applet only (but you could change this for your listener)
      return;
    }
    // cases that fail to return are sent to the console for processing
    if (jmol.service != null)
      jmol.service.scriptCallback(strInfo);
    JmolCallbackListener appConsole = (JmolCallbackListener) viewer
        .getProperty("DATA_API", "getAppConsole", null);
    if (appConsole != null)
      appConsole.notifyCallback(type, data);
  }

  @Override
public void setCallbackFunction(String callbackType, String callbackFunction) {
    if (callbackType.equals("modelkit")) {
      if (callbackFunction.equals("ON"))
        display.buttonModelkit.setSelected(true);
      else
        display.buttonRotate.setSelected(true);
      return;
    }
    //if (callbackType.equalsIgnoreCase("menu")) {
      //jmol.setupNewFrame(vi/ewer);
      //return;
    //}
    if (callbackType.equalsIgnoreCase("language")) {
      JmolResourceHandler.clear();
      Dialog.setupUIManager();
      if (jmol.webExport != null) {
        WebExport.saveHistory();
        WebExport.dispose();
        jmol.createWebExport();
      }
      AppConsole appConsole = (AppConsole) viewer.getProperty("DATA_API",
          "getAppConsole", null);
      if (appConsole != null)
        appConsole.sendConsoleEcho(null);
      display.jmolPanel.updateLabels();
      return;
    }
  }

  // / end of JmolCallbackListener interface ///

  @Override
public String eval(String strEval) {
   String msg = "# this funcationality is implemented only for the applet.\n" + strEval;
   sendConsoleMessage(msg);
    return msg;
  }

  /**
   * 
   * @param fileName
   * @param type
   * @param text_or_bytes
   * @param quality
   * @return null ("you do it" or canceled) or a message starting with OK or an
   *         error message
   */
  @Override
public String createImage(String fileName, String type, Object text_or_bytes,
                            int quality) {
    return null;
  }

  private void notifyAtomPicked(String info) {
    if (display.haveDisplay)
      display.status.setStatus(1, info);
  }

  private void notifyFileLoaded(String fullPathName, String fileName,
                                String modelName, String errorMsg) {
    if (errorMsg != null) {
      return;
    }
    if (!display.haveDisplay)
      return;

    // this code presumes only ptLoad = -1 (error), 0 (zap), or 3 (completed)
    String title = "Jmol";
    if (modelName != null && fileName != null)
      title = fileName + " - " + modelName;
    else if (fileName != null)
      title = fileName;
    else if (modelName != null)
      title = modelName;
    jmol.notifyFileOpen(fullPathName, title);    
    if (jSpecViewFrame != null) {
      if (fullPathName == null) {
        jSpecViewFrame.syncScript("close ALL");
      } else if (fullPathName.endsWith(".jdx"))
        jSpecViewFrame.syncScript("load CHECK " + Escape.escapeStr(fullPathName));
    }
  }

  private void sendConsoleMessage(String strStatus) {
    JmolAppConsoleInterface appConsole = (JmolAppConsoleInterface) viewer
        .getProperty("DATA_API", "getAppConsole", null);
    if (appConsole != null)
      appConsole.sendConsoleMessage(strStatus);
  }

  @Override
public void showUrl(String url) {
    try {
      Class<?> c = Class.forName("java.awt.Desktop");
      Method getDesktop = c.getMethod("getDesktop", new Class[] {});
      Object deskTop = getDesktop.invoke(null, new Object[] {});
      Method browse = c.getMethod("browse", new Class[] { URI.class });
      Object arguments[] = { new URI(url) };
      browse.invoke(deskTop, arguments);
    } catch (Exception e) {
      Logger.error(e.getMessage());
      JmolAppConsoleInterface appConsole = (JmolAppConsoleInterface) viewer
          .getProperty("DATA_API", "getAppConsole", null);
      if (appConsole != null) {
        appConsole
            .sendConsoleMessage("Java 6 Desktop.browse() capability unavailable. Could not open "
                + url);
      } else {
        Logger
            .error("Java 6 Desktop.browse() capability unavailable. Could not open "
                + url);
      }
    }
  }

  /**
   * this is just a test method for isosurface FUNCTIONXY
   * 
   * @param functionName
   * @param nX
   * @param nY
   * @return f(x,y) as a 2D array
   * 
   */
  @Override
public float[][] functionXY(String functionName, int nX, int nY) {
    nX = Math.abs(nX);
    nY = Math.abs(nY);
    float[][] f = new float[nX][nY];
    // boolean isSecond = (functionName.indexOf("2") >= 0);
    for (int i = nX; --i >= 0;)
      for (int j = nY; --j >= 0;) {
        float x = i / 5f; // / 15f - 1;
        float y = j / 5f; // / 15f - 1;
        f[i][j] = /* (float) Math.sqrt */(x * x + y);
        if (Float.isNaN(f[i][j]))
          f[i][j] = -(float) Math.sqrt(-x * x - y);
        // f[i][j] = (isSecond ? (float) ((i + j - nX) / (2f)) : (float) Math
        // .sqrt(Math.abs(i * i + j * j)) / 2f);
        // if (i < 10 && j < 10)
        //System.out.println(" functionXY " + i + " " + j + " " + f[i][j]);
      }

    return f; // for user-defined isosurface functions (testing only -- bob
              // hanson)
  }

  @Override
public float[][][] functionXYZ(String functionName, int nX, int nY, int nZ) {
    nX = Math.abs(nX);
    nY = Math.abs(nY);
    nZ = Math.abs(nZ);
    float[][][] f = new float[nX][nY][nZ];
    for (int i = nX; --i >= 0;)
      for (int j = nY; --j >= 0;)
        for (int k = nZ; --k >= 0;) {
          float x = i / ((nX - 1) / 2f) - 1;
          float y = j / ((nY - 1) / 2f) - 1;
          float z = k / ((nZ - 1) / 2f) - 1;
          f[i][j][k] = x * x + y * y - z * z;//(float) x * x + y - z * z;
          // if (i == 22 || i == 23)
          // System.out.println(" functionXYZ " + i + " " + j + " " + k + " " +
          // f[i][j][k]);
        }
    return f; // for user-defined isosurface functions (testing only -- bob
              // hanson)
  }

  @Override
public Map<String, Object> getRegistryInfo() {
    return null;
  }

  @Override
public void resizeInnerPanel(String data) {
    jmol.resizeInnerPanel(data);
  }

  public void setJSpecView(String peaks) {
    if (!display.isRotateMode())
      return;
    if (peaks.startsWith(":"))
      peaks = peaks.substring(1);
    if (jSpecViewFrame == null) {
      jSpecViewFrame = new MainFrame((Component) viewer.getDisplay(), this);
      jSpecViewFrame.setSize(800, 500);
      jSpecViewFrame.setLocation(400, 400);
      jSpecViewFrame.register("Jmol", this);
      if (peaks.length() == 0) {
        String s = "" + viewer.getParameter("_modelfile");
        if (s.indexOf("/") >= 0)
          peaks = "hidden false; load " + Escape.escapeStr(s);
      }
    }
    if (!jSpecViewFrame.isVisible() && !peaks.toLowerCase().startsWith("hidden")) {
      jSpecViewFrame.awaken(true);
      display.setViewer(viewer);
    }
    if (peaks.length() == 0)
      peaks = "HIDDEN false";
    jSpecViewFrame.syncScript(peaks);
  }

  @Override
public void register(String id, JmolSyncInterface jsi) {
    // this would be a call from JSpecView requesting that Jmol 
    // register the JSpecView applet in the JmolAppletRegistry. 
  }

  @Override
public void syncScript(String script) {
    // called from JSpecView to send "Select: <Peaks...." script
    jmol.syncScript(script);    
  }

  
  // -- JSVInterface -- 
  
  private static String propertiesFileName = "jspecview.properties";

  @Override
public void setProperties(Properties properties) {
    try {
      FileInputStream fileIn = new FileInputStream(propertiesFileName);
      properties.load(fileIn);
    } catch (Exception e) {
    }
  }

  @Override
public void saveProperties(Properties properties) {
    // Write out current properties
    try {
      FileOutputStream fileOut = new FileOutputStream(propertiesFileName);
      properties.store(fileOut, "JSpecView Application Properties");
    } catch (Exception e) {
    }
  }

  /**
   * @param withDialog  
   * @param frame 
   */
  @Override
public void exitJSpecView(boolean withDialog, Object frame) {
    // no exit from Jmol
  }

  /**
   * no queuing here -- called by MainFrame
   * 
   * @param script 
   */
  @Override
public void runScript(String script) {
    jSpecViewFrame.runScriptNow(script);
    
  }

  /**
   * @param msg
   */
  @Override
public void syncToJmol(String msg) {
    // not utilized in Jmol application -- jmolSyncInterface used instead
  }

  @Override
public Map<String, Object> getProperty(String type) {
    if (type.toLowerCase().startsWith("jspecview")) {
      type = type.substring(9);
      if (type.startsWith(":"))
          type = type.substring(1);
      return (jSpecViewFrame == null ? null : jSpecViewFrame.getProperty(type));
    }
    return null;
  }
  
}
