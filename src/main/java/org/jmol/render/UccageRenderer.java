/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-09-03 19:02:08 -0500 (Mon, 03 Sep 2012) $
 * $Revision: 17502 $
 *
 * Copyright (C) 2002-2006  Miguel, Jmol Development, www.jmol.org
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
package org.jmol.render;

import java.text.NumberFormat;

import javax.vecmath.Point3f;

import org.jmol.api.SymmetryInterface;
import org.jmol.shape.Uccage;
import org.jmol.util.BoxInfo;
import org.jmol.util.SimpleUnitCell;
import org.jmol.util.TextFormat;
import org.jmol.viewer.StateManager;

public class UccageRenderer extends CageRenderer {

  NumberFormat nf;
  byte fid;
  boolean doLocalize;
  
  @Override
  protected void setEdges() {
    tickEdges = BoxInfo.uccageTickEdges;    
  }

  private final Point3f[] verticesT = new Point3f[8];  
  {
    for (int i = 8; --i >= 0; ) {
      verticesT[i] = new Point3f();
    }
  }

  @Override
  protected void initRenderer() {
    super.initRenderer();
    draw000 = false;
  }
  
  @Override
  protected void render() {
    imageFontScaling = viewer.getImageFontScaling();
    font3d = g3d.getFont3DScaled(((Uccage)shape).font3d, imageFontScaling);
    int mad = viewer.getObjectMad(StateManager.OBJ_UNITCELL);
    colix = viewer.getObjectColix(StateManager.OBJ_UNITCELL);
    if (mad == 0 || !g3d.setColix(colix) || viewer.isJmolDataFrame()
        || viewer.isNavigating() && viewer.getNavigationPeriodic())
      return;
    doLocalize = viewer.getUseNumberLocalization();
    render1(mad);
  }

  private Point3f fset0 = new Point3f(555,555,1);
  private Point3f cell0 = new Point3f();
  private Point3f cell1 = new Point3f();
  private Point3f offset = new Point3f();
  private Point3f offsetT = new Point3f();
  
  void render1(int mad) {
    SymmetryInterface unitcell = viewer.getCurrentUnitCell();
    if (unitcell == null)
      return;
    isPolymer = unitcell.isPolymer();
    isSlab = unitcell.isSlab();
    Point3f[] vertices = unitcell.getUnitCellVertices();
    offset.set(unitcell.getCartesianOffset());
    Point3f fset = unitcell.getUnitCellMultiplier();
    boolean haveMultiple = (fset != null);
    if (!haveMultiple) 
      fset = fset0;

    SimpleUnitCell.ijkToPoint3f((int) fset.x, cell0, 0);
    SimpleUnitCell.ijkToPoint3f((int) fset.y, cell1, 1);
    int firstLine, allow0, allow1;
    if (fset.z < 0) {
      cell0.scale (-1/fset.z);
      cell1.scale (-1/fset.z);
    }
    Point3f[] axisPoints = viewer.getAxisPoints();
    boolean drawAllLines = (viewer.getObjectMad(StateManager.OBJ_AXIS1) == 0
        || viewer.getAxesScale() < 2 || axisPoints == null);
    Point3f[] aPoints = axisPoints;
    for (int x = (int) cell0.x; x < cell1.x; x++) {
      for (int y = (int) cell0.y; y < cell1.y; y++) {
        for (int z = (int) cell0.z; z < cell1.z; z++) {
          if (haveMultiple) {
            offsetT.set(x, y, z);
            offsetT.scale(Math.abs(fset.z));
            unitcell.toCartesian(offsetT, true);
            offsetT.add(offset);
            aPoints = (x == 0 && y == 0 && z == 0 ? axisPoints : null);
            firstLine = (drawAllLines || aPoints == null ? 0 : 3);
            allow0 = 0xFF;
            allow1 = 0xFF;            
          } else {
            offsetT.set(offset);
            firstLine = (drawAllLines ? 0 : 3);
            allow0 = 0xFF;
            allow1 = 0xFF;
          }
          for (int i = 8; --i >= 0;)
            verticesT[i].add(vertices[i], offsetT);
          render(mad, verticesT, aPoints, firstLine, allow0, allow1, Math.abs(fset.z));
        }
      }
    }

    if (viewer.getDisplayCellParameters() && !viewer.isPreviewOnly()
        && !unitcell.isPeriodic())
      renderInfo(unitcell);
  }
  
  private String nfformat(float x) {
    return (doLocalize && nf != null ? nf.format(x) : TextFormat.formatDecimal(x, 3));
  }

  private void renderInfo(SymmetryInterface symmetry) {
    if (isExport
        || !g3d.setColix(viewer.getColixBackgroundContrast()))
      return;
    if (nf == null) {
      nf = NumberFormat.getInstance();
    }

    fid = g3d.getFontFid("Monospaced", 14 * imageFontScaling);

    if (nf != null) {
      nf.setMaximumFractionDigits(3);
      nf.setMinimumFractionDigits(3);
    }
    g3d.setFont(fid);

    int lineheight = (int) (15 * imageFontScaling);
    int x = (int) (5 * imageFontScaling);
    int y = lineheight;

    String spaceGroup = symmetry.getSpaceGroupName();
    if (isPolymer)
      spaceGroup = "polymer";
    else if (isSlab)
      spaceGroup = "slab";
    if (spaceGroup != null & !spaceGroup.equals("-- [--]")) {
      y += lineheight;
      g3d.drawStringNoSlab(spaceGroup, null, x, y, 0);
    }
    y += lineheight;
    g3d.drawStringNoSlab("a="
        + nfformat(symmetry.getUnitCellInfo(SimpleUnitCell.INFO_A)) + "\u00C5",
        null, x, y, 0);
    if (!isPolymer) {
      y += lineheight;
      g3d.drawStringNoSlab(
          "b=" + nfformat(symmetry.getUnitCellInfo(SimpleUnitCell.INFO_B))
              + "\u00C5", null, x, y, 0);
    }
    if (!isPolymer && !isSlab) {
      y += lineheight;
      g3d.drawStringNoSlab(
          "c=" + nfformat(symmetry.getUnitCellInfo(SimpleUnitCell.INFO_C))
              + "\u00C5", null, x, y, 0);
    }
    if (nf != null)
      nf.setMaximumFractionDigits(1);
    if (!isPolymer) {
      if (!isSlab) {
        y += lineheight;
        g3d.drawStringNoSlab("\u03B1="
            + nfformat(symmetry.getUnitCellInfo(SimpleUnitCell.INFO_ALPHA))
            + "\u00B0", null, x, y, 0);
        y += lineheight;
        g3d.drawStringNoSlab("\u03B2="
            + nfformat(symmetry.getUnitCellInfo(SimpleUnitCell.INFO_BETA))
            + "\u00B0", null, x, y, 0);
      }
      y += lineheight;
      g3d.drawStringNoSlab("\u03B3="
          + nfformat(symmetry.getUnitCellInfo(SimpleUnitCell.INFO_GAMMA))
          + "\u00B0", null, x, y, 0);
    }
  }

}

