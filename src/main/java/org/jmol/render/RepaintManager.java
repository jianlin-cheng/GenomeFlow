/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-09-08 21:39:22 -0500 (Sat, 08 Sep 2012) $
 * $Revision: 17541 $
 *
 * Copyright (C) 2003-2005  The Jmol Development Team
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
 *  Lesser General License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jmol.render;

import javax.vecmath.Point3f;

import org.jmol.api.JmolRendererInterface;
import org.jmol.api.JmolRepaintInterface;
import org.jmol.g3d.Graphics3D;
import org.jmol.modelset.ModelSet;
import org.jmol.shape.Shape;
import org.jmol.util.Colix;
import org.jmol.util.GData;
import org.jmol.util.Logger;
import org.jmol.util.Rectangle;
import org.jmol.viewer.JmolConstants;
import org.jmol.viewer.ShapeManager;
import org.jmol.viewer.Viewer;

public class RepaintManager implements JmolRepaintInterface {

  private Viewer viewer;
  private ShapeManager shapeManager;
  private ShapeRenderer[] renderers;

  public RepaintManager() {
    // required for reflection
  }
  
  @Override
public void set(Viewer viewer, ShapeManager shapeManager) {
    this.viewer = viewer;
    this.shapeManager = shapeManager;
  }

  /////////// thread management ///////////
  
  private int holdRepaint = 0;
  private boolean repaintPending;
  
  
  @Override
public boolean isRepaintPending() {
    return repaintPending;
  }
  
  
  @Override
public void pushHoldRepaint() {
    ++holdRepaint;
    //System.out.println("repaintManager pushHoldRepaint holdRepaint=" + holdRepaint + " thread=" + Thread.currentThread().getName());
  }

  
  @Override
public void popHoldRepaint(boolean andRepaint) {
    --holdRepaint;
    //System.out.println("repaintManager popHoldRepaint holdRepaint=" + holdRepaint + " thread=" + Thread.currentThread().getName());
    if (holdRepaint <= 0) {
      holdRepaint = 0;
      if (andRepaint) {
        repaintPending = true;
        //System.out.println("RM popholdrepaint TRUE " + (test++));
        viewer.repaint();
      }
    }
  }

  
  @Override
public boolean refresh() {
    if (repaintPending)
      return false;
    repaintPending = true;
    if (holdRepaint == 0) {
      //System.out.println("RM refresh() " + (test++));
      viewer.repaint();
    }
    return true;
  }

  
  @Override
synchronized public void repaintDone() {
    repaintPending = false;
    //System.out.println("repaintManager repaintDone thread=" + Thread.currentThread().getName());
    notify(); // to cancel any wait in requestRepaintAndWait()
  }

  
  @Override
synchronized public void requestRepaintAndWait() {
    //System.out.println("RM requestRepaintAndWait() " + (test++));
    viewer.repaint();
    try {
      //System.out.println("repaintManager requestRepaintAndWait I am waiting for a repaint: thread=" + Thread.currentThread().getName());
      wait(viewer.getRepaintWait());  // more than a second probably means we are locked up here
      if (repaintPending) {
        Logger.error("repaintManager requestRepaintAndWait timeout");
        repaintDone();
      }
    } catch (InterruptedException e) {
      //System.out.println("repaintManager requestRepaintAndWait interrupted thread=" + Thread.currentThread().getName());
    }
    //System.out.println("repaintManager requestRepaintAndWait I am no longer waiting for a repaint: thread=" + Thread.currentThread().getName());
  }

  /////////// renderer management ///////////
  
  
  @Override
public void clear(int iShape) {
    if (renderers ==  null)
      return;
    if (iShape >= 0)
      renderers[iShape] = null;
    else
      for (int i = 0; i < JmolConstants.SHAPE_MAX; ++i)
        renderers[i] = null;
  }

  private ShapeRenderer getRenderer(int shapeID, Graphics3D g3d) {
    if (renderers[shapeID] != null)
      return renderers[shapeID];
    String className = JmolConstants.getShapeClassName(shapeID, true) + "Renderer";
    try {
      Class<?> shapeClass = Class.forName(className);
      ShapeRenderer renderer = (ShapeRenderer) shapeClass.newInstance();
      renderer.setViewerG3dShapeID(viewer, g3d, shapeID);
      return renderers[shapeID] = renderer;
    } catch (Exception e) {
      Logger.error("Could not instantiate renderer:" + className, e);
      return null;
    }
  }

  /////////// actual rendering ///////////
  
  private boolean logTime;
  
  
  @Override
public void render(GData gdata, ModelSet modelSet, boolean isFirstPass, int[] minMax) {
    logTime = false;//viewer.getTestFlag(2);
    if (logTime)
      Logger.startTimer();
    try {
      Graphics3D g3d = (Graphics3D) gdata;
      g3d.renderBackground(null);
      if (isFirstPass)  {
        if (minMax != null)
          renderCrossHairs(g3d, minMax);
        renderSelectionRubberBand(g3d);
      }
      if (renderers == null)
        renderers = new ShapeRenderer[JmolConstants.SHAPE_MAX];

      for (int i = 0; i < JmolConstants.SHAPE_MAX && g3d.currentlyRendering(); ++i) {
        Shape shape = shapeManager.getShape(i);
        if (shape == null)
          continue;
        getRenderer(i, g3d).render(g3d, modelSet,
        		shape);
        if (logTime)
          Logger.checkTimer("render time " + JmolConstants.getShapeClassName(i, false));
      }
    } catch (Exception e) {
      e.printStackTrace();
      Logger.error("rendering error? ");
    }
  }

  
  @Override
public String renderExport(String type, GData gdata, ModelSet modelSet,
                      String fileName) {

    JmolRendererInterface g3dExport = null;
    Object output = null;
    boolean isOK;
    viewer.finalizeTransformParameters();
    try {
      shapeManager.transformAtoms(null, null);
      output = (fileName == null ? new StringBuffer() : fileName);
      Class<?> export3Dclass = Class.forName("org.jmol.export.Export3D");
      g3dExport = (JmolRendererInterface) export3Dclass.newInstance();
      isOK = viewer.initializeExporter(g3dExport, type, output);
    } catch (Exception e) {
      isOK = false;
    }
    if (!isOK) {
      Logger.error("Cannot export " + type);
      return null;
    }
    g3dExport.renderBackground(g3dExport);
    for (int i = 0; i < JmolConstants.SHAPE_MAX; ++i) {
      Shape shape = shapeManager.getShape(i);
      if (shape != null)
        getRenderer(i, (Graphics3D) gdata).render(g3dExport, modelSet, shape);
    }
    return g3dExport.finalizeOutput();
  }

  /////////// special rendering ///////////
  
  private void renderCrossHairs(Graphics3D g3d, int[] minMax) {
    // this is the square and crosshairs for the navigator
    Point3f navOffset = new Point3f(viewer.getNavigationOffset());
    boolean antialiased = g3d.isAntialiased();
    float navDepth = viewer.getNavigationDepthPercent();
    g3d.setColix(navDepth < 0 ? Colix.RED
        : navDepth > 100 ? Colix.GREEN : Colix.GOLD);
    int x = Math.max(Math.min(viewer.getScreenWidth(), (int) navOffset.x), 0);
    int y = Math.max(Math.min(viewer.getScreenHeight(), (int) navOffset.y), 0);
    int z = (int) navOffset.z + 1;
    // TODO: fix for antialiasDisplay
    int off = (antialiased ? 8 : 4);
    int h = (antialiased ? 20 : 10);
    int w = (antialiased ? 2 : 1);
    g3d.drawRect(x - off, y, z, 0, h, w);
    g3d.drawRect(x, y - off, z, 0, w, h);
    g3d.drawRect(x - off, y - off, z, 0, h, h);
    off = h;
    h = h >> 1;
    g3d.setColix(minMax[1] < navOffset.x ? Colix.YELLOW
            : Colix.GREEN);
    g3d.drawRect(x - off, y, z, 0, h, w);
    g3d.setColix(minMax[0] > navOffset.x ? Colix.YELLOW
            : Colix.GREEN);
    g3d.drawRect(x + h, y, z, 0, h, w);
    g3d.setColix(minMax[3] < navOffset.y ? Colix.YELLOW
            : Colix.GREEN);
    g3d.drawRect(x, y - off, z, 0, w, h);
    g3d.setColix(minMax[2] > navOffset.y ? Colix.YELLOW
            : Colix.GREEN);
    g3d.drawRect(x, y + h, z, 0, w, h);
  }

  private void renderSelectionRubberBand(Graphics3D g3d) {
    Rectangle band = viewer.getRubberBandSelection();
    if (band != null && g3d.setColix(viewer.getColixRubberband()))
      g3d.drawRect(band.x, band.y, 0, 0, band.width, band.height);
  }

}
