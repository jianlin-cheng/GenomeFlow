/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2009-06-25 02:42:30 -0500 (Thu, 25 Jun 2009) $
 * $Revision: 11113 $
 *
 * Copyright (C) 2004-2005  The Jmol Development Team
 *
 * Contact: jmol-developers@lists.sf.net, www.jmol.org
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
package org.jmol.console;


import java.awt.Container;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.jmol.api.JmolCallbackListener;
import org.jmol.api.JmolScriptEditorInterface;
import org.jmol.api.JmolViewer;
import org.jmol.constant.EnumCallback;
import org.jmol.i18n.GT;
import org.jmol.script.ScriptCompiler;
import org.jmol.script.Token;
import org.jmol.util.ArrayUtil;
import org.jmol.util.TextFormat;
import org.jmol.viewer.FileManager;
import org.jmol.viewer.Viewer;

public abstract class JmolConsole implements JmolCallbackListener, ActionListener, WindowListener {

  public JmolViewer viewer;
  protected JFrame viewerFrame;
  protected Container externalContainer;

  protected JButton editButton, runButton, historyButton, stateButton;
  protected Map<String, String> labels;
  
  abstract protected void setupLabels();
  
  protected Map<String, AbstractButton> menuMap = new Hashtable<String, AbstractButton>();
  
  public void dispose() {
    if (externalContainer instanceof Window)
      ((Window) externalContainer).dispose();
    else
      externalContainer.setVisible(false);
  }

  protected Container getPane() {
    return (externalContainer instanceof JFrame ? ((JFrame) externalContainer)
        .getContentPane() : externalContainer);
  }
  protected String getLabel(String key) {
    if (labels == null) {
      labels = new Hashtable<String, String>();
      labels.put("title", GT._("Gmol Script Console") + " " + Viewer.getJmolVersion());
      setupLabels();
    }
    return labels.get(key);
  }

  protected void setTitle() {
    if (externalContainer instanceof JFrame)
      ((JFrame) this.externalContainer).setTitle(getLabel("title"));
    else if (externalContainer instanceof JDialog)
      ((JDialog) externalContainer).setTitle(getLabel("title"));
  }

  public void setVisible(boolean isVisible) {
    externalContainer.setVisible(isVisible);
  }

  protected ScriptEditor scriptEditor;
  
  void setScriptEditor(ScriptEditor se) {
    scriptEditor = se;
  }
  
  public JmolScriptEditorInterface getScriptEditor() {
    return (scriptEditor == null ? 
        (scriptEditor = new ScriptEditor(viewer, viewerFrame, this)) : scriptEditor);
  }
  
  //public void finalize() {
  //  System.out.println("Console " + this + " finalize");
  //}
  
  abstract protected void clearContent(String text);
  abstract protected void execute(String strCommand);
  abstract public String getText();
  
  public int nTab = 0;
  private String incompleteCmd;
  
  public String completeCommand(String thisCmd) {
    if (thisCmd.length() == 0)
      return null;
    String strCommand = (nTab <= 0 || incompleteCmd == null ? thisCmd
        : incompleteCmd);
    incompleteCmd = strCommand;
    String[] splitCmd = ScriptCompiler.splitCommandLine(thisCmd);
    if (splitCmd == null)
      return null;
    boolean asCommand = splitCmd[2] == null;
    String notThis = splitCmd[asCommand ? 1 : 2];
    String s = splitCmd[1];
    if (notThis.length() == 0)
      return null;
    splitCmd = ScriptCompiler.splitCommandLine(strCommand);
    String cmd = null;
    if (!asCommand && (notThis.charAt(0) == '"' || notThis.charAt(0) == '\'')) {
      char q = notThis.charAt(0);
      notThis = TextFormat.trim(notThis, "\"\'");
      String stub = TextFormat.trim(splitCmd[2], "\"\'");
      cmd = nextFileName(stub, nTab);
      if (cmd != null)
        cmd = splitCmd[0] + splitCmd[1] + q + cmd + q;
    } else {
      Map<String, Token> map = null;
      if (!asCommand) {
        //System.out.println(" tsting " + splitCmd[0] + "///" + splitCmd[1] + "///" + splitCmd[2]);
        notThis = s;
        if (splitCmd[2].startsWith("$") 
            || s.equalsIgnoreCase("isosurface ")
            || s.equalsIgnoreCase("contact ")
            || s.equalsIgnoreCase("draw ")
         ) {
          map = new Hashtable<String, Token>();
          viewer.getObjectMap(map, splitCmd[2].startsWith("$"));
        }
      }
      cmd = Token.completeCommand(map, s.equalsIgnoreCase("set "), asCommand, asCommand ? splitCmd[1]
          : splitCmd[2], nTab);
      cmd = splitCmd[0]
          + (cmd == null ? notThis : asCommand ? cmd : splitCmd[1] + cmd);
    }
    return (cmd == null || cmd.equals(strCommand) ? null : cmd);
  }

  private String nextFileName(String stub, int nTab) {
    String sname = FileManager.getLocalPathForWritingFile(viewer, stub);
    String root = sname.substring(0, sname.lastIndexOf("/") + 1);
    if (sname.startsWith("file:/"))
      sname = sname.substring(6);
    if (sname.indexOf("/") >= 0) {
      if (root.equals(sname)) {
        stub = "";
      } else {
        File dir = new File(sname);
        sname = dir.getParent();
        stub = dir.getName();
      }
    }
    FileChecker fileChecker = new FileChecker(stub);
    try {
      (new File(sname)).list(fileChecker);
      return root + fileChecker.getFile(nTab);
    } catch (Exception e) {
      //
    }
    return null;
  }

  protected class FileChecker implements FilenameFilter {
    private String stub;
    private List<String> v = new ArrayList<String>();
    
    protected FileChecker(String stub) {
      this.stub = stub.toLowerCase();
    }

    @Override
	public boolean accept(File dir, String name) {
      if (!name.toLowerCase().startsWith(stub))
        return false;
      v.add(name); 
      return true;
    }
    
    protected String getFile(int n) {
      return ArrayUtil.sortedItem(v, n);
    }
  }
  
  protected void setEnabled(JButton button, boolean TF) {
    if (button != null)
      button.setEnabled(TF);
  }

  protected JButton setButton(String s) {
    JButton b = new JButton(getLabel(s));
    b.addActionListener(this);
    menuMap.put(s, b);
    return b;
  }

  protected String defaultMessage;
  protected JLabel label1;
  

  protected void updateLabels() {
    boolean doTranslate = GT.getDoTranslate();
    labels = null;
    GT.setDoTranslate(true);
    defaultMessage = getLabel("default");
    KeyJMenuItem.setAbstractButtonLabels(menuMap, labels);
    setTitle();
    if (label1 != null)
      label1.setText(getLabel("label1"));
    GT.setDoTranslate(doTranslate);
  }

  @Override
public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    if (source == runButton) {
      execute(null);
    } else if (source == editButton) {
      viewer.getProperty("DATA_API","scriptEditor", null);
    } else if (source == historyButton) {
      clearContent(viewer.getSetHistory(Integer.MAX_VALUE));
    } else if (source == stateButton) {
      clearContent(viewer.getStateInfo());
      // problem here is that in some browsers, you cannot clip from
      // the editor.
      //viewer.getProperty("DATA_API","scriptEditor", new String[] { "current state" , viewer.getStateInfo() });
    }
  }

  ////////////////////////////////////////////////////////////////
  // window listener stuff to close when the window closes
  ////////////////////////////////////////////////////////////////

  protected void addWindowListener() {
    Window w = getWindow(externalContainer);
    if (w != null)
      w.addWindowListener(this);
  }


  /**
   * @param we 
   * 
   */
  @Override
public void windowActivated(WindowEvent we) {
  }

  @Override
public void windowClosed(WindowEvent we) {
    destroyConsole();
  }

  @Override
public void windowClosing(WindowEvent we) {
    destroyConsole();
  }

  private void destroyConsole() {
    // if the viewer is an applet, when we close the console
    // we 
    if (viewer.isApplet())
      viewer.getProperty("DATA_API", "getAppConsole", Boolean.FALSE);
  }

  /**
   * @param we 
   * 
   */
  @Override
public void windowDeactivated(WindowEvent we) {
  }

  /**
   * @param we 
   * 
   */
  @Override
public void windowDeiconified(WindowEvent we) {
  }

  /**
   * @param we 
   * 
   */
  @Override
public void windowIconified(WindowEvent we) {
  }

  /**
   * @param we 
   * 
   */
  @Override
public void windowOpened(WindowEvent we) {
  }

  ///////////// JmolCallbackListener interface

  // Allowing for just the callbacks needed to provide status feedback to the console.
  // For applications that embed Jmol, see the example application Integration.java.

  @Override
public boolean notifyEnabled(EnumCallback type) {
    // See org.jmol.viewer.JmolConstants.java for a complete list
    switch (type) {
    case ECHO:
    case MEASURE:
    case MESSAGE:
    case PICK:
      return true;
    case ANIMFRAME:
    case APPLETREADY:
    case ATOMMOVED:
    case CLICK:
    case ERROR:
    case EVAL:
    case HOVER:
    case LOADSTRUCT:
    case MINIMIZATION:
    case RESIZE:
    case SCRIPT:
    case SYNC:
      break;
    }
    return false;
  }

  abstract public void sendConsoleMessage(String info);
  abstract public void sendConsoleEcho(String info);
  
  @Override
@SuppressWarnings("incomplete-switch")
  public void notifyCallback(EnumCallback type, Object[] data) {
    String strInfo = (data == null || data[1] == null ? null : data[1]
        .toString());
    switch (type) {
    case ECHO:
      sendConsoleEcho(strInfo);
      break;
    case MEASURE:
      String mystatus = (String) data[3];
      if (mystatus.indexOf("Picked") >= 0 || mystatus.indexOf("Sequence") >= 0) // picking mode
        sendConsoleMessage(strInfo);
      else if (mystatus.indexOf("Completed") >= 0)
        sendConsoleEcho(strInfo.substring(strInfo.lastIndexOf(",") + 2, strInfo
            .length() - 1));
      break;
    case MESSAGE:
      sendConsoleMessage(data == null ? null : strInfo);
      break;
    case PICK:
      sendConsoleMessage(strInfo);
      break;
    }
  }

  @Override
public void setCallbackFunction(String callbackType, String callbackFunction) {
    // application-dependent option
  }

  /**
   * @param p 
   * @return The hosting frame or JDialog.
   */
  static public Window getWindow(Container p) {
    while (p != null) {
      if (p instanceof Frame)
        return (Frame) p;
      else if (p instanceof JDialog)
        return (JDialog) p;
      else if (p instanceof JmolFrame)
        return ((JmolFrame) p).getFrame();
      p = p.getParent();
    }
    return null;
  }

  
}
