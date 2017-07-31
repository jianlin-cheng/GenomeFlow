/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2007-08-05 16:26:58 -0500 (Sun, 05 Aug 2007) $
 * $Revision: 8032 $
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

import javax.vecmath.Point3f;

import org.jmol.shape.Bbcage;
import org.jmol.shape.FontLineShape;
import org.jmol.util.BoxInfo;
import org.jmol.util.Point3fi;

abstract class CageRenderer extends FontLineShapeRenderer {

  // Bbcage and Uccage

  protected final Point3f[] screens = new Point3f[8];
  {
    for (int i = 8; --i >= 0; )
      screens[i] = new Point3f();
  }

  protected char[] tickEdges;
  
  abstract protected void setEdges();
  
  protected boolean isSlab;
  protected boolean isPolymer;
  
  @Override
  protected void initRenderer() {
    setEdges();
  }
  
  private Point3f pt = new Point3f();
  protected void render(int mad, Point3f[] vertices, Point3f[] axisPoints,
                        int firstLine, int allowedEdges0, int allowedEdges1,
                        float scale) {
    //clearBox();
    g3d.setColix(colix);
    FontLineShape fls = (FontLineShape) shape;
    imageFontScaling = viewer.getImageFontScaling();
    font3d = g3d.getFont3DScaled(fls.font3d, imageFontScaling);

    float zSum = 0;
    for (int i = 8; --i >= 0;) {
      pt.set(vertices[i]);
      if (scale != 1) {
        pt.sub(vertices[0]);
        pt.scaleAdd(scale, pt, vertices[0]);
      }
      viewer.transformPointNoClip(pt, screens[i]);
      zSum += screens[i].z;
    }
    
    int diameter = getDiameter((int) (zSum / 8), mad);
    int axisPt = 2;
    char edge = 0;
    allowedEdges0 &= (isPolymer ? 0x1 : isSlab ? 0x55 : 0xFF);
    allowedEdges1 &= (isPolymer ? 0x10 : isSlab ? 0x55 : 0xFF);
    for (int i = firstLine * 2; i < 24; i += 2) {
      int edge0 = BoxInfo.edges[i];
      int edge1 = BoxInfo.edges[i + 1];
      if (axisPoints != null && edge0 == 0)
        viewer.transformPointNoClip(axisPoints[axisPt--], screens[0]);
      if ((allowedEdges0 & (1 << edge0)) == 0 
        || (allowedEdges1 & (1 << edge1)) == 0)
        continue;
      boolean drawTicks = (fls.tickInfos != null && (edge = tickEdges[i >> 1]) != 0);
      if (drawTicks) {
        if (atomA == null) {
          atomA = new Point3fi();
          atomB = new Point3fi();
        }
        atomA.set(vertices[edge0]);
        atomB.set(vertices[edge1]);
        float start = 0;
        if (shape instanceof Bbcage)
          switch (edge) {
          case 'x':
            start = atomA.x;
            break;
          case 'y':
            start = atomA.y;
            break;
          case 'z':
            start = atomA.z;
            break;
          }
        tickInfo = fls.tickInfos["xyz".indexOf(edge) + 1];
        if (tickInfo == null)
          tickInfo = fls.tickInfos[0];
        if (tickInfo == null)
          drawTicks = false;
        else
          tickInfo.first = start;
      }
      renderLine(screens[edge0], screens[edge1], diameter, pt0, pt1,
          drawTicks);
    }
  }
}

