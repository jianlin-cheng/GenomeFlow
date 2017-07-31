/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-09-11 19:29:26 -0500 (Tue, 11 Sep 2012) $
 * $Revision: 17556 $
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

package org.jmol.shape;

import java.util.BitSet;
import java.util.Hashtable;
import java.util.Map;

import javax.vecmath.Point3i;

import org.jmol.util.ArrayUtil;
import org.jmol.util.Colix;
import org.jmol.util.Escape;
import org.jmol.util.JmolFont;

public class Hover extends TextShape {

  private final static String FONTFACE = "SansSerif";
  private final static String FONTSTYLE = "Plain";
  private final static int FONTSIZE = 12;

  public Text hoverText;
  public int atomIndex = -1;
  public Point3i xy;
  public String text;
  public String labelFormat = "%U";
  public String[] atomFormats;
  public String specialLabel;

  @Override
  public void initShape() {
    super.initShape();
    isHover = true;
    JmolFont font3d = gdata.getFont3D(FONTFACE, FONTSTYLE, FONTSIZE);
    short bgcolix = Colix.getColix("#FFFFC3"); // 255, 255, 195
    short colix = Colix.BLACK;
    currentObject = hoverText = new Text(gdata, font3d, null, colix, bgcolix, 0, 0,
        1, Integer.MIN_VALUE, Object2d.ALIGN_LEFT, 0);
    hoverText.setAdjustForWindow(true);
  }

  @Override
  public void setProperty(String propertyName, Object value, BitSet bsSelected) {

    //if (Logger.debugging) {
      //Logger.debug("Hover.setProperty(" + propertyName + "," + value + ")");
    //}

    if ("target" == propertyName) {
      if (value == null)
        atomIndex = -1;
      else {
        atomIndex = ((Integer) value).intValue();
      }
      return;
    }

    if ("text" == propertyName) {
      text = (String) value;
      if (text != null && text.length() == 0)
        text = null;
      return;
    }

    if ("specialLabel" == propertyName) {
      specialLabel = (String) value;
      return;
    }

    if ("atomLabel" == propertyName) {
      String text = (String) value;
      if (text != null && text.length() == 0)
        text = null;
      int count = viewer.getAtomCount();
      if (atomFormats == null || atomFormats.length < count)
        atomFormats = new String[count];
      for (int i = bsSelected.nextSetBit(0); i >= 0; i = bsSelected.nextSetBit(i + 1))
        atomFormats[i] = text;
      return;
    }

    if ("xy" == propertyName) {
      xy = (Point3i) value;
      return;
    }

    if ("label" == propertyName) {
      labelFormat = (String) value;
      if (labelFormat != null && labelFormat.length() == 0)
        labelFormat = null;
      return;
    }

    if (propertyName == "deleteModelAtoms") {
      if (atomFormats != null) {
        int firstAtomDeleted = ((int[])((Object[])value)[2])[1];
        int nAtomsDeleted = ((int[])((Object[])value)[2])[2];
        atomFormats = (String[]) ArrayUtil.deleteElements(atomFormats, firstAtomDeleted, nAtomsDeleted);
      }
      atomIndex = -1;
      return;
    }
    
    super.setProperty(propertyName, value, null);

  }

  @Override
  public String getShapeState() {
    Map<String, BitSet> temp = new Hashtable<String, BitSet>();
    if (atomFormats != null)
      for (int i = viewer.getAtomCount(); --i >= 0;)
        if (atomFormats[i] != null)
          setStateInfo(temp, i, "set hoverLabel " + Escape.escapeStr(atomFormats[i]));
    return "\n  hover " + Escape.escapeStr((labelFormat == null ? "" : labelFormat)) 
    + ";\n" + getShapeCommands(temp, null);
  }
}
