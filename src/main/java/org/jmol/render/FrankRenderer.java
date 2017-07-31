/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-09-03 19:02:08 -0500 (Mon, 03 Sep 2012) $
 * $Revision: 17502 $
 *
 * Copyright (C) 2002-2005  The Jmol Development Team
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
import org.jmol.shape.Frank;
import org.jmol.util.Colix;

public class FrankRenderer extends ShapeRenderer {

  //we render Frank last just for the touch that if there are translucent
  //objects, then it becomes translucent. Just for fun.
  
  // no Frank export
    
  @Override
  protected void render() {
    Frank frank = (Frank) shape;
    boolean allowKeys = viewer.getBooleanProperty("allowKeyStrokes");
    boolean modelKitMode = viewer.isModelKitMode();
    colix = (modelKitMode ? Colix.MAGENTA 
        : viewer.isSignedApplet() ? (allowKeys ? Colix.ORANGE : Colix.RED) : allowKeys ? Colix.BLUE : Colix.GRAY);
    if (isExport || !viewer.getShowFrank()
        || !g3d.setColix(Colix.getColixTranslucent(colix,
            g3d.haveTranslucentObjects(), 0.5f)))
      return;
    float imageFontScaling = viewer.getImageFontScaling();
    frank.getFont(imageFontScaling);
    int dx = (int) (frank.frankWidth + Frank.frankMargin * imageFontScaling);
    int dy = frank.frankDescent;
    g3d.drawStringNoSlab(frank.frankString, frank.font3d,
        g3d.getRenderWidth() - dx, g3d.getRenderHeight() - dy, 0);
  //  g3d.drawStringNoSlab("ForScales", frank.font3d,
  //          g3d.getRenderWidth() - 5*dx, g3d.getRenderHeight() - 10*dy, 0);//here to initial the scales information display -hcf
    if (modelKitMode) {
     //g3d.setColix(GData.GRAY);
      g3d.fillRect(0, 0, 0, 0, dy * 2, dx * 3 / 2);      
    }
  }
}
