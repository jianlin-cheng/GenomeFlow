/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2007-10-07 20:10:15 -0500 (Sun, 07 Oct 2007) $
 * $Revision: 8384 $
 *
 * Copyright (C) 2003-2006  Miguel, Jmol Development, www.jmol.org
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
package org.jmol.export;

import java.awt.Image;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;

import org.jmol.api.JmolRendererInterface;
import org.jmol.g3d.Graphics3D;
import org.jmol.g3d.HermiteRenderer;
import org.jmol.modelset.Atom;
import org.jmol.util.GData;
import org.jmol.util.JmolFont;
import org.jmol.util.MeshSurface;
import org.jmol.viewer.Viewer;

/**
 * Provides high-level graphics primitives for 3D graphics export.
 * 
 * @author hansonr, hansonr@stolaf.edu
 * 
 */

final public class Export3D implements JmolRendererInterface {

  private ___Exporter exporter;
  private double privateKey;

  private Graphics3D g3d;
  private short colix;
  private HermiteRenderer hermite3d;
  private int width;
  private int height;
  private int slab;
  
  String exportName;

  public Export3D() {
    hermite3d = new HermiteRenderer(this);

  }

  @Override
public int getExportType() {
    return exporter.exportType;
  }

  @Override
public String getExportName() {
    return exportName;
  }

  @Override
public boolean initializeExporter(String type, Viewer viewer, double privateKey, GData gdata,
                                    Object output) {
    exportName = type;
    try {
      String name = "org.jmol.export._"
          + type + "Exporter";
      Class<?> exporterClass = Class.forName(name);
      // Class exporterClass =
      // Class.forName("org.jmol.export.NewPovrayExporter");
      exporter = (___Exporter) exporterClass.newInstance();
    } catch (Exception e) {
      return false;
    }
    g3d = (Graphics3D) gdata;
    exporter.setRenderer(this);
    g3d.setNewWindowParametersForExport();
    slab = g3d.getSlab();
    width = g3d.getRenderWidth();
    height = g3d.getRenderHeight();
    this.privateKey = privateKey;
    return exporter.initializeOutput(viewer, privateKey, g3d, output);
  }

  @Override
public String finalizeOutput() {
    return exporter.finalizeOutput();
  }

  @Override
public void setSlab(int slabValue) {
    slab = slabValue;
    g3d.setSlab(slabValue);
  }

  @Override
public void setDepth(int depthValue) {
    // no equivalent in exporters?
    g3d.setDepth(depthValue);
  }

  @Override
public void renderBackground(JmolRendererInterface me) {
    if (exporter.exportType == Graphics3D.EXPORT_RAYTRACER)
      g3d.renderBackground(me);
  }

  @Override
public void drawAtom(Atom atom) {
    exporter.drawAtom(atom);
  }

  /**
   * draws a screened circle ... every other dot is turned on
   * @param colixRing 
   * @param colixFill
   * @param diameter
   * @param x
   *          center x
   * @param y
   *          center y
   * @param z
   *          center z
   */

  @Override
public void drawFilledCircle(short colixRing, short colixFill, int diameter, int x, int y,
                                 int z) {
    // halos, draw
    if (isClippedZ(z))
      return;
    exporter.drawFilledCircle(colixRing, colixFill, diameter, x, y, z);
  }

  /**
   * draws a simple circle (draw circle)
   * 
   * @param colix
   *          the color index
   * @param diameter
   *          the pixel diameter
   * @param x
   *          center x
   * @param y
   *          center y
   * @param z
   *          center z
   * @param doFill
   *          (not implemented in exporters)
   */

  public void drawCircle(short colix, int diameter, int x, int y, int z,
                         boolean doFill) {
    // halos, draw
    if (isClippedZ(z))
      return;
    exporter.drawCircle(x, y, z, diameter, colix, doFill);
  }

  private Point3f ptA = new Point3f();
  private Point3f ptB = new Point3f();
  private Point3f ptC = new Point3f();
  private Point3f ptD = new Point3f();
  /*
   * private Point3f ptE = new Point3f(); private Point3f ptF = new Point3f();
   * private Point3f ptG = new Point3f(); private Point3f ptH = new Point3f();
   */
  private Point3i ptAi = new Point3i();
  private Point3i ptBi = new Point3i();

  /**
   * fills a solid sphere
   * 
   * @param diameter
   *          pixel count
   * @param x
   *          center x
   * @param y
   *          center y
   * @param z
   *          center z
   */
  @Override
public void fillSphere(int diameter, int x, int y, int z) {
    ptA.set(x, y, z);
    fillSphere(diameter, ptA);
  }

  /**
   * fills a solid sphere
   * 
   * @param diameter
   *          pixel count
   * @param center
   *          javax.vecmath.Point3i defining the center
   */

  @Override
public void fillSphere(int diameter, Point3i center) {
    ptA.set(center.x, center.y, center.z);
    fillSphere(diameter, ptA);
  }

  /**
   * fills a solid sphere
   * 
   * @param diameter
   *          pixel count
   * @param center
   *          a javax.vecmath.Point3f ... floats are casted to ints
   */
  @Override
public void fillSphere(int diameter, Point3f center) {
    if (diameter == 0)
      return;
    exporter.fillSphere(colix, diameter, center);
  }

  /**
   * draws a rectangle
   * 
   * @param x
   *          upper left x
   * @param y
   *          upper left y
   * @param z
   *          upper left z
   * @param zSlab
   *          z for slab check (for set labelsFront)
   * @param rWidth
   *          pixel count
   * @param rHeight
   *          pixel count
   */
  @Override
public void drawRect(int x, int y, int z, int zSlab, int rWidth, int rHeight) {
    // labels (and rubberband, not implemented) and navigation cursor
    if (zSlab != 0 && isClippedZ(zSlab))
      return;
    int w = rWidth - 1;
    int h = rHeight - 1;
    int xRight = x + w;
    int yBottom = y + h;
    if (y >= 0 && y < height)
      drawHLine(x, y, z, w);
    if (yBottom >= 0 && yBottom < height)
      drawHLine(x, yBottom, z, w);
    if (x >= 0 && x < width)
      drawVLine(x, y, z, h);
    if (xRight >= 0 && xRight < width)
      drawVLine(xRight, y, z, h);
  }

  private void drawHLine(int x, int y, int z, int w) {
    // hover, labels only
    int argbCurrent = g3d.getColorArgbOrGray(colix);
    if (w < 0) {
      x += w;
      w = -w;
    }
    for (int i = 0; i <= w; i++) {
      exporter.drawTextPixel(argbCurrent, x + i, y, z);
    }
  }

  private void drawVLine(int x, int y, int z, int h) {
    // hover, labels only
    int argbCurrent = g3d.getColorArgbOrGray(colix);
    if (h < 0) {
      y += h;
      h = -h;
    }
    for (int i = 0; i <= h; i++) {
      exporter.drawTextPixel(argbCurrent, x, y + i, z);
    }
  }

  /**
   * fills background rectangle for label
   *<p>
   * 
   * @param x
   *          upper left x
   * @param y
   *          upper left y
   * @param z
   *          upper left z
   * @param zSlab
   *          z value for slabbing
   * @param widthFill
   *          pixel count
   * @param heightFill
   *          pixel count
   */
  @Override
public void fillRect(int x, int y, int z, int zSlab, int widthFill,
                       int heightFill) {
    // hover and labels only -- slab at atom or front -- simple Z/window clip
    if (isClippedZ(zSlab))
      return;
    ptA.set(x, y, z);
    ptB.set(x + widthFill, y, z);
    ptC.set(x + widthFill, y + heightFill, z);
    ptD.set(x, y + heightFill, z);
    fillQuadrilateral(ptA, ptB, ptC, ptD);
  }

  /**
   * draws the specified string in the current font. no line wrapping -- axis,
   * labels, measures
   * 
   * @param str
   *          the String
   * @param font3d
   *          the Font3D
   * @param xBaseline
   *          baseline x
   * @param yBaseline
   *          baseline y
   * @param z
   *          baseline z
   * @param zSlab
   *          z for slab calculation
   */

  @Override
public void drawString(String str, JmolFont font3d, int xBaseline,
                         int yBaseline, int z, int zSlab) {
    // axis, labels, measures
    if (str == null)
      return;
    if (isClippedZ(zSlab))
      return;
    drawStringNoSlab(str, font3d, xBaseline, yBaseline, z);
  }

  /**
   * draws the specified string in the current font. no line wrapping -- echo,
   * frank, hover, molecularOrbital, uccage
   * 
   * @param str
   *          the String
   * @param font3d
   *          the Font3D
   * @param xBaseline
   *          baseline x
   * @param yBaseline
   *          baseline y
   * @param z
   *          baseline z
   */

  @Override
public void drawStringNoSlab(String str, JmolFont font3d, int xBaseline,
                               int yBaseline, int z) {
    // echo, frank, hover, molecularOrbital, uccage
    if (str == null)
      return;
    z = Math.max(slab, z);
    if (font3d == null)
      font3d = g3d.getFont3DCurrent();
    else
      g3d.setFont(font3d);
    exporter.plotText(xBaseline, yBaseline, z, colix, str, font3d);
  }

  @Override
public void drawImage(Object objImage, int x, int y, int z, int zSlab,
                        short bgcolix, int width, int height) {
    if (objImage == null || width == 0 || height == 0)
      return;
    if (isClippedZ(zSlab))
      return;
    z = Math.max(slab, z);
    exporter.plotImage(x, y, z, (Image) objImage, bgcolix, width, height);
  }

  // mostly public drawing methods -- add "public" if you need to

  /*
   * *************************************************************** points
   * **************************************************************
   */

  @Override
public void drawPixel(int x, int y, int z) {
    // measures - render angle
    plotPixelClipped(x, y, z);
  }

  void plotPixelClipped(int x, int y, int z) {
    // circle3D, drawPixel, plotPixelClipped(point3)
    if (isClipped(x, y, z))
      return;
    exporter.drawPixel(colix, x, y, z, 1);
  }

  @Override
public void plotPixelClippedNoSlab(int argb, int x, int y, int z) {
    // from Text3D
    z = Math.max(slab, z);
    exporter.drawTextPixel(argb, x, y, z);
  }

  @Override
public void plotPixelClipped(Point3i screen) {
    if (isClipped(screen.x, screen.y, screen.z))
      return;
    // circle3D, drawPixel, plotPixelClipped(point3)
    exporter.drawPixel(colix, screen.x, screen.y, screen.z, 1);
  }

  @Override
public void drawPoints(int count, int[] coordinates, int scale) {
    for (int i = count * 3; i > 0;) {
      int z = coordinates[--i];
      int y = coordinates[--i];
      int x = coordinates[--i];
      if (isClipped(x, y, z))
        continue;
      exporter.drawPixel(colix, x, y, z, scale);
    }
  }

  /*
   * *************************************************************** lines and
   * cylinders **************************************************************
   */

  @Override
public void drawDashedLine(int run, int rise, Point3i pointA, Point3i pointB) {
    // axes and such -- ignored dashed for exporters
    drawLine(pointA, pointB); 
    // ptA.set(pointA.x, pointA.y, pointA.z);
    // ptB.set(pointB.x, pointB.y, pointB.z);
    // exporter.drawDashedLine(colix, run, rise, ptA, ptB);
  }

  @Override
public void drawDottedLine(Point3i pointA, Point3i pointB) {
    // TODO
    // axes, bbcage only
    drawLine(pointA, pointB); // Temporary only
    // ptA.set(pointA.x, pointA.y, pointA.z);
    // ptB.set(pointB.x, pointB.y, pointB.z);
    // exporter.drawDashedLine(colix, 2, 1, ptA, ptB);
  }

  @Override
public void drawLine(int x1, int y1, int z1, int x2, int y2, int z2) {
    // stars
    ptAi.set(x1, y1, z1);
    ptBi.set(x2, y2, z2);
    drawLine(ptAi, ptBi);
  }

  @Override
public void drawLine(short colixA, short colixB, int xA, int yA, int zA,
                       int xB, int yB, int zB) {
    // line bonds, line backbone, drawTriangle
    fillCylinder(colixA, colixB, GData.ENDCAPS_FLAT, exporter.lineWidthMad, xA, yA, zA,
        xB, yB, zB);
  }

  @Override
public void drawLine(Point3i pointA, Point3i pointB) {
    // draw quadrilateral and hermite, stars
    ptA.set(pointA.x, pointA.y, pointA.z);
    ptB.set(pointB.x, pointB.y, pointB.z);
    exporter.fillCylinderScreenMad(colix, GData.ENDCAPS_FLAT, exporter.lineWidthMad, ptA, ptB);
  }

  @Override
public void drawBond(Atom atomA, Atom atomB, short colixA, short colixB,
                       byte endcaps, short mad) {
    // from SticksRenderer to allow for a direct
    // writing of single bonds -- just for efficiency here 
    // bondOrder == -1 indicates we have cartesian coordinates
    if (mad == 1)
      mad = exporter.lineWidthMad;
    exporter.drawCylinder(atomA, atomB, colixA, colixB, endcaps, mad, -1);
  }

  @Override
public void fillCylinder(short colixA, short colixB, byte endcaps,
                                 int mad, int xA, int yA, int zA, int xB,
                                 int yB, int zB) {
    /*
     * from drawLine, Sticks, fillCylinder, backbone
     * 
     */
    ptA.set(xA, yA, zA);
    ptB.set(xB, yB, zB);
    // bond order 1 here indicates that we have screen coordinates
    exporter.drawCylinder(ptA, ptB, colixA, colixB, endcaps, mad, 1);
  }

  @Override
public void fillCylinderScreen(byte endcaps, int screenDiameter, int xA, int yA, int zA,
                           int xB, int yB, int zB) {
    // vectors, polyhedra
    ptA.set(xA, yA, zA);
    ptB.set(xB, yB, zB);
    exporter.fillCylinderScreen(colix, endcaps, screenDiameter, ptA, ptB);
  }

  @Override
public void fillCylinderScreen(byte endcaps, int diameter, Point3i pointA,
                           Point3i pointB) {
    if (diameter <= 0)
      return;
    ptA.set(pointA.x, pointA.y, pointA.z);
    ptB.set(pointB.x, pointB.y, pointB.z);
    exporter.fillCylinderScreen(colix, endcaps, diameter, ptA, ptB);
  }

  @Override
public void fillCylinder(byte endcaps, int diameter, Point3i pointA,
                           Point3i pointB) {
    if (diameter <= 0)
      return;
    ptA.set(pointA.x, pointA.y, pointA.z);
    ptB.set(pointB.x, pointB.y, pointB.z);
    exporter.fillCylinderScreenMad(colix, endcaps, diameter, ptA, ptB);
  }

  @Override
public void fillCylinderBits(byte endcaps, int diameter, Point3f pointA,
                               Point3f pointB) {
    if (diameter <= 0)
      return;
    exporter.fillCylinderScreenMad(colix, endcaps, diameter, pointA,
        pointB);
  }

  @Override
public void fillConeScreen(byte endcap, int screenDiameter, Point3i pointBase,
                       Point3i screenTip, boolean isBarb) {
    // dipole, vector, draw arrow/vector
    ptA.set(pointBase.x, pointBase.y, pointBase.z);
    ptB.set(screenTip.x, screenTip.y, screenTip.z);
    exporter.fillConeScreen(colix, endcap, screenDiameter, ptA, ptB, isBarb);
  }

  @Override
public void fillConeSceen(byte endcap, int screenDiameter, Point3f pointBase,
                       Point3f screenTip) {
    // cartoons, rockets
    exporter.fillConeScreen(colix, endcap, screenDiameter, pointBase, screenTip, false);
  }

  @Override
public void drawHermite(int tension, Point3i s0, Point3i s1, Point3i s2,
                          Point3i s3) {
    // strands
    hermite3d.renderHermiteRope(false, tension, 0, 0, 0, s0, s1, s2, s3);
  }

  @Override
public void fillHermite(int tension, int diameterBeg, int diameterMid,
                          int diameterEnd, Point3i s0, Point3i s1, Point3i s2,
                          Point3i s3) {
    hermite3d.renderHermiteRope(true, tension, diameterBeg, diameterMid,
        diameterEnd, s0, s1, s2, s3);
  }

  @Override
public void drawHermite(boolean fill, boolean border, int tension,
                          Point3i s0, Point3i s1, Point3i s2, Point3i s3,
                          Point3i s4, Point3i s5, Point3i s6, Point3i s7,
                          int aspectRatio) {
    hermite3d.renderHermiteRibbon(fill, border, tension, s0, s1, s2, s3, s4,
        s5, s6, s7, aspectRatio);
  }

  /*
   * *************************************************************** triangles
   * **************************************************************
   */

  @Override
public void drawTriangle(Point3i screenA, short colixA, Point3i screenB,
                           short colixB, Point3i screenC, short colixC,
                           int check) {
    // primary method for mapped Mesh
    if ((check & 1) == 1)
      drawLine(colixA, colixB, screenA.x, screenA.y, screenA.z, screenB.x,
          screenB.y, screenB.z);
    if ((check & 2) == 2)
      drawLine(colixB, colixC, screenB.x, screenB.y, screenB.z, screenC.x,
          screenC.y, screenC.z);
    if ((check & 4) == 4)
      drawLine(colixA, colixC, screenA.x, screenA.y, screenA.z, screenC.x,
          screenC.y, screenC.z);
  }

  @Override
public void drawTriangle(Point3i screenA, Point3i screenB, Point3i screenC,
                           int check) {
    // primary method for mapped Mesh
    if ((check & 1) == 1)
      drawLine(colix, colix, screenA.x, screenA.y, screenA.z, screenB.x,
          screenB.y, screenB.z);
    if ((check & 2) == 2)
      drawLine(colix, colix, screenB.x, screenB.y, screenB.z, screenC.x,
          screenC.y, screenC.z);
    if ((check & 4) == 4)
      drawLine(colix, colix, screenA.x, screenA.y, screenA.z, screenC.x,
          screenC.y, screenC.z);
  }

  /*
   * public void drawfillTriangle(int xA, int yA, int zA, int xB, int yB, int
   * zB, int xC, int yC, int zC) { ptA.set(xA, yA, zA); ptB.set(xB, yB, zB);
   * ptC.set(xC, yC, zC); fillTriangle(ptA, ptB, ptC); }
   */

  @Override
public void fillTriangle(Point3i pointA, short colixA, short normixA,
                           Point3i pointB, short colixB, short normixB,
                           Point3i pointC, short colixC, short normixC) {
    // mesh, isosurface
    if (colixA != colixB || colixB != colixC) {
      // shouldn't be here, because that uses renderIsosurface
      return;
    }
    ptA.set(pointA.x, pointA.y, pointA.z);
    ptB.set(pointB.x, pointB.y, pointB.z);
    ptC.set(pointC.x, pointC.y, pointC.z);
    exporter.fillTriangle(colixA, ptA, ptB, ptC, false);
  }

  @Override
public void fillTriangleTwoSided(short normix, int xpointA, int ypointA, int zpointA,
                           int xpointB, int ypointB, int zpointB, int xpointC,
                           int ypointC, int zpointC) {
    // polyhedra
    ptA.set(xpointA, ypointA, zpointA);
    ptB.set(xpointB, ypointB, zpointB);
    ptC.set(xpointC, ypointC, zpointC);
    exporter.fillTriangle(colix, ptA, ptB, ptC, true);
  }

  @Override
public void fillTriangle(Point3f pointA, Point3f pointB, Point3f pointC) {
    // rockets
    exporter.fillTriangle(colix, pointA, pointB, pointC, false);
  }

  @Override
public void fillTriangle(Point3i pointA, Point3i pointB, Point3i pointC) {
    // cartoon only, for nucleic acid bases

    ptA.set(pointA.x, pointA.y, pointA.z);
    ptB.set(pointB.x, pointB.y, pointB.z);
    ptC.set(pointC.x, pointC.y, pointC.z);
    exporter.fillTriangle(colix, ptA, ptB, ptC, true);
  }

  @Override
public void fillTriangle(Point3i pointA, short colixA, short normixA,
                           Point3i pointB, short colixB, short normixB,
                           Point3i pointC, short colixC, short normixC,
                           float factor) {
    fillTriangle(pointA, colixA, normixA, pointB, colixB, normixB, pointC,
        colixC, normixC);
  }

  /*
   * ***************************************************************
   * quadrilaterals
   * **************************************************************
   */

  @Override
public void drawQuadrilateral(short colix, Point3i pointA, Point3i pointB,
                                Point3i pointC, Point3i screenD) {
    // mesh only -- translucency has been checked
    setColix(colix);
    drawLine(pointA, pointB);
    drawLine(pointB, pointC);
    drawLine(pointC, screenD);
    drawLine(screenD, pointA);
  }

  @Override
public void fillQuadrilateral(Point3f pointA, Point3f pointB, Point3f pointC,
                                Point3f pointD) {
    // hermite, rockets, cartoons
    exporter.fillTriangle(colix, pointA, pointB, pointC, false);
    exporter.fillTriangle(colix, pointA, pointC, pointD, false);
  }

  @Override
public void fillQuadrilateral(Point3i pointA, short colixA, short normixA,
                                Point3i pointB, short colixB, short normixB,
                                Point3i pointC, short colixC, short normixC,
                                Point3i screenD, short colixD, short normixD) {
    // mesh
    fillTriangle(pointA, colixA, normixA, pointB, colixB, normixB, pointC,
        colixC, normixC);
    fillTriangle(pointA, colixA, normixA, pointC, colixC, normixC, screenD,
        colixD, normixD);
  }

  @Override
public void drawSurface(MeshSurface meshSurface, short colix) {
    exporter.drawSurface(meshSurface, colix);
  }

  @Override
public short[] getBgColixes(short[] bgcolixes) {
    // 3D exporters cannot do background labels
    return exporter.exportType == Graphics3D.EXPORT_CARTESIAN ? null : bgcolixes;
  }

  @Override
public void fillEllipsoid(Point3f center, Point3f[] points, int x, int y,
                            int z, int diameter, Matrix3f mToEllipsoidal,
                            double[] coef, Matrix4f mDeriv, int selectedOctant,
                            Point3i[] octantPoints) {
    exporter.fillEllipsoid(center, points, colix, x, y, z, diameter,
        mToEllipsoidal, coef, mDeriv, octantPoints);
  }

  @Override
public boolean drawEllipse(Point3f ptAtom, Point3f ptX, Point3f ptY,
                           boolean fillArc, boolean wireframeOnly) {
    return exporter.drawEllipse(ptAtom, ptX, ptY, colix, fillArc); 
  }


  /*
   * *************************************************************** g3d-relayed
   * info specifically needed for the renderers
   * **************************************************************
   */

  @Override
public GData getGData() {
    return g3d;
  }

  /**
   * is full scene / oversampling antialiasing in effect
   * 
   * @return the answer
   */
  @Override
public boolean isAntialiased() {
    return false;
  }

  @Override
public boolean checkTranslucent(boolean isAlphaTranslucent) {
    return true;
  }

  @Override
public boolean haveTranslucentObjects() {
    return true;
  }

  @Override
public void setColor(int color) {
    g3d.setColor(color);
  }

  /**
   * gets g3d width
   * 
   * @return width pixel count;
   */
  @Override
public int getRenderWidth() {
    return g3d.getRenderWidth();
  }

  /**
   * gets g3d height
   * 
   * @return height pixel count
   */
  @Override
public int getRenderHeight() {
    return g3d.getRenderHeight();
  }

  @Override
public boolean isPass2() {
    return g3d.isPass2();
  }

  /**
   * gets g3d slab
   * 
   * @return slab
   */
  @Override
public int getSlab() {
    return g3d.getSlab();
  }

  /**
   * gets g3d depth
   * 
   * @return depth
   */
  @Override
public int getDepth() {
    return g3d.getDepth();
  }

  /**
   * sets current color from colix color index
   * 
   * @param colix
   *          the color index
   * @return true or false if this is the right pass
   */
  @Override
public boolean setColix(short colix) {
    this.colix = colix;
    g3d.setColix(colix);
    return true;
  }

  @Override
public void setFont(byte fid) {
    g3d.setFont(fid);
  }

  @Override
public JmolFont getFont3DCurrent() {
    return g3d.getFont3DCurrent();
  }

  @Override
public boolean isInDisplayRange(int x, int y) {
    if (exporter.exportType == Graphics3D.EXPORT_CARTESIAN)
      return true;
    return g3d.isInDisplayRange(x, y);
  }

  @Override
public boolean isClippedZ(int z) {
    return g3d.isClippedZ(z);
  }

  public int clipCode(int x, int y, int z) {
    return (exporter.exportType == Graphics3D.EXPORT_CARTESIAN ? g3d.clipCode(z) : g3d.clipCode(x, y, z));
  }

  @Override
public boolean isClippedXY(int diameter, int x, int y) {
    if (exporter.exportType == Graphics3D.EXPORT_CARTESIAN)
      return false;
    return g3d.isClippedXY(diameter, x, y);
  }

  public boolean isClipped(int x, int y, int z) {
    return (g3d.isClippedZ(z) || isClipped(x, y));
  }

  protected boolean isClipped(int x, int y) {
    if (exporter.exportType == Graphics3D.EXPORT_CARTESIAN)
      return false;
    return g3d.isClipped(x, y);
  }

  @Override
public int getColorArgbOrGray(short colix) {
    return g3d.getColorArgbOrGray(colix);
  }

  @Override
public void setNoisySurfaceShade(Point3i pointA, Point3i pointB,
                                   Point3i pointC) {
    g3d.setNoisySurfaceShade(pointA, pointB, pointC);
  }

  @Override
public byte getFontFid(String fontFace, float fontSize) {
    return g3d.getFontFid(fontFace, fontSize);
  }

  @Override
public boolean isDirectedTowardsCamera(short normix) {
    // polyhedra
    return g3d.isDirectedTowardsCamera(normix);
  }

  @Override
public Vector3f[] getTransformedVertexVectors() {
    return g3d.getTransformedVertexVectors();
  }

  @Override
public JmolFont getFont3DScaled(JmolFont font, float scale) {
    return g3d.getFont3DScaled(font, scale);
  }

  @Override
public byte getFontFid(float fontSize) {
    return g3d.getFontFid(fontSize);
  }

  @Override
public void setTranslucentCoverOnly(boolean TF) {
    // ignore
  }

  public double getPrivateKey() {
    return privateKey;
  }

  @Override
public void volumeRender(boolean TF) {
    // not implemented
  }

  @Override
public void volumeRender(int diam, int x, int y, int z) {
    fillSphere(diam, x, y, z);
    
  }

}
