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

import org.jmol.shape.Bbcage;
import org.jmol.util.BoxInfo;
import org.jmol.viewer.StateManager;

public class BbcageRenderer extends CageRenderer {

  @Override
  protected void setEdges() {
    tickEdges = BoxInfo.bbcageTickEdges; 
  }
  
  @Override
  protected void render() {
    Bbcage bbox = (Bbcage) shape;
    if (!bbox.isVisible 
        || !isExport && !g3d.checkTranslucent(false)
        || viewer.isJmolDataFrame())
      return;
    colix = viewer.getObjectColix(StateManager.OBJ_BOUNDBOX);
    render(bbox.mad, modelSet.getBboxVertices(), null, 0, 0xFF, 0xFF, 1);
  }

}
