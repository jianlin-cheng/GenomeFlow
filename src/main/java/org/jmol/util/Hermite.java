/* $RCSfile$
 * $Author: egonw $
 * $Date: 2005-11-10 09:52:44f -0600 (Thu, 10 Nov 2005) $
 * $Revision: 4255 $
 *
 * Copyright (C) 2003-2005  Miguel, Jmol Development, www.jmol.org
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

package org.jmol.util;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;


public class Hermite {

  public static void getHermiteList(int tension, Tuple3f p0, Tuple3f p1,
                                    Tuple3f p2, Tuple3f p3, Tuple3f p4,
                                    Tuple3f[] list, int index0, int n) {
    //always deliver ONE MORE than one might expect, to provide a normal
    int nPoints = n + 1;
    float fnPoints = n - 1;
    float x1 = p1.x, y1 = p1.y, z1 = p1.z;
    float x2 = p2.x, y2 = p2.y, z2 = p2.z;
    float xT1 = ((x2 - p0.x) * tension) / 8;
    float yT1 = ((y2 - p0.y) * tension) / 8;
    float zT1 = ((z2 - p0.z) * tension) / 8;
    float xT2 = ((p3.x - x1) * tension) / 8;
    float yT2 = ((p3.y - y1) * tension) / 8;
    float zT2 = ((p3.z - z1) * tension) / 8;
    float xT3 = ((p4.x - x2) * tension) / 8;
    float yT3 = ((p4.y - y2) * tension) / 8;
    float zT3 = ((p4.z - z2) * tension) / 8;
    list[index0] = p1;
    for (int i = 0; i < nPoints; i++) {
      double s = i / fnPoints;
      if (i == nPoints - 1) {
        x1 = x2;
        y1 = y2;
        z1 = z2;
        x2 = p3.x;
        y2 = p3.y;
        z2 = p3.z;
        xT1 = xT2;
        yT1 = yT2;
        zT1 = zT2;
        xT2 = xT3;
        yT2 = yT3;
        zT2 = zT3;
        s -= 1;
      }
      double s2 = s * s;
      double s3 = s2 * s;
      double h1 = 2 * s3 - 3 * s2 + 1;
      double h2 = -2 * s3 + 3 * s2;
      double h3 = s3 - 2 * s2 + s;
      double h4 = s3 - s2;
      float x = (float) (h1 * x1 + h2 * x2 + h3 * xT1 + h4 * xT2);
      float y = (float) (h1 * y1 + h2 * y2 + h3 * yT1 + h4 * yT2);
      float z = (float) (h1 * z1 + h2 * z2 + h3 * zT1 + h4 * zT2);
      if (list instanceof Point3f[])
        list[index0 + i] = new Point3f(x, y, z);
      else
        list[index0 + i] = new Vector3f(x, y, z);
    }
  }

}
