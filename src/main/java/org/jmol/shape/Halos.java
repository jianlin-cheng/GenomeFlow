/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2006-08-22 03:13:40 -0500 (Tue, 22 Aug 2006) $
 * $Revision: 5412 $

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
 *  Lesser General License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jmol.shape;

import java.util.BitSet;

import org.jmol.util.BitSetUtil;
import org.jmol.util.Colix;
import org.jmol.util.Escape;
import org.jmol.util.Logger;

public class Halos extends AtomShape {

  public short colixSelection = Colix.USE_PALETTE;

  public BitSet bsHighlight;
  public short colixHighlight = Colix.RED;
  
  void initState() {
    Logger.debug("init halos");
    translucentAllowed = false;
  }
  
 @Override
public void setProperty(String propertyName, Object value, BitSet bs) {
    if ("translucency" == propertyName)
      return;
    if ("argbSelection" == propertyName) {
      colixSelection = Colix.getColix(((Integer)value).intValue());
      return;
    }
    if ("argbHighlight" == propertyName) {
      colixHighlight = Colix.getColix(((Integer)value).intValue());
      return;
    }
    if ("highlight" == propertyName) {
      bsHighlight = (BitSet) value;
      return;
    }

    if (propertyName == "deleteModelAtoms") {
      BitSetUtil.deleteBits(bsHighlight, bs);
      // pass through to AtomShape
    }
    
    super.setProperty(propertyName, value, bs);
  }

 @Override
public void setVisibilityFlags(BitSet bs) {
    BitSet bsSelected = (viewer.getSelectionHaloEnabled(false) ? viewer
        .getSelectionSet(false) : null);
    for (int i = atomCount; --i >= 0;) {
      boolean isVisible = bsSelected != null && bsSelected.get(i)
          || (mads != null && mads[i] != 0);
      atoms[i].setShapeVisibility(myVisibilityFlag, isVisible);
    }
  }
  
 @Override
public String getShapeState() {
    String state = super.getShapeState()
        + (colixSelection == Colix.USE_PALETTE ? ""
            : colixSelection == Colix.INHERIT_ALL ? "  color SelectionHalos NONE;\n"
                : getColorCommand("selectionHalos", colixSelection) + ";\n");
    if (bsHighlight != null)
      state += "  set highlight " + Escape.escape(bsHighlight) + "; "
          + getColorCommand("highlight", colixHighlight) + ";\n";
    return state;
  }
 
}
