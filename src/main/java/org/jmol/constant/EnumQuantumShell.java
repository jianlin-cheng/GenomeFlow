/* $RCSfile$
 * $Author$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 2011  The Jmol Development Team
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
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 *  02110-1301, USA.
 */

package org.jmol.constant;


/**
 * Enum for quantum shells.
 */
public enum EnumQuantumShell {

  S("S","S",0,0),
  P("P","X",1,1),
  SP("SP","SP",2,2),
  D_SPHERICAL("5D","5D",3,3),
  D_CARTESIAN("D","XX",4,3),
  F_SPHERICAL("7F","7F",5,5),
  F_CARTESIAN("F","XXX",6,5),
  G_SPHERICAL("9G","9G",7,7),
  G_CARTESIAN("G","XXXX",8,7),
  H_SPHERICAL("10H","10H",9,9),
  H_CARTESIAN("H","XXXXX",10,9);

  final public static String SUPPORTED_BASIS_FUNCTIONS = "SPLDF";
  
  public final String tag;
  private final String tag2;
  public final int id;
  private final int idSpherical;
  
  private EnumQuantumShell(String tag, String tag2, int id, int idSpherical) {
    this.tag = tag;
    this.tag2 = tag2;
    this.id = id;
    this.idSpherical = idSpherical;
  }
  
  public static int[][] getNewDfCoefMap() {
    return new int[][] { 
        new int[1], //S 
        new int[3], //P
        new int[4], //SP
        new int[5], //D5
        new int[6], //D6
        new int[7], //F7 
        new int[10] //F10
    };
  }

  final public static int getQuantumShellTagID(String tag) {
    if (tag.equals("L"))
      return SP.id;
    EnumQuantumShell item = getQuantumShell(tag);
    return (item == null ? -1 : item.id);
  }

  private static EnumQuantumShell getQuantumShell(String tag) {
    for (EnumQuantumShell item : values())
      if (item.tag.equals(tag) || item.tag2.equals(tag))
        return item;
    return null;
  }

  final public static int getQuantumShellTagIDSpherical(String tag) {
    if (tag.equals("L"))
      return SP.idSpherical;
    EnumQuantumShell item = getQuantumShell(tag);
    return (item == null ? -1 : item.idSpherical);
  }

  public static EnumQuantumShell getItem(int id) {
    // Sorry -- The only way to do this efficiently, since we have
    // encoded the id into shells[] base on its integer value
    switch(id) {
    case 0:
      return S;
    case 1:
      return P;
    case 2:
      return SP;
    case 3:
      return D_SPHERICAL;
    case 4:
      return D_CARTESIAN;
    case 5:
      return F_SPHERICAL;
    case 6:
      return F_CARTESIAN;
    case 7:
      return G_SPHERICAL;
    case 8:
      return G_CARTESIAN;
    case 9:
      return H_SPHERICAL;
    case 10:
      return H_CARTESIAN;
    }
    return null;
  }

  final public static String getQuantumShellTag(int id) {
    for (EnumQuantumShell item : values())
      if (item.id == id)
        return item.tag;
    return "" + id;
  }

  final public static String getMOString(float[] lc) {
    StringBuffer sb = new StringBuffer();
    if (lc.length == 2)
      return "" + (int)(lc[0] < 0 ? -lc[1] : lc[1]);
    sb.append('[');
    for (int i = 0; i < lc.length; i += 2) {
      if (i > 0)
        sb.append(", ");
      sb.append(lc[i]).append(" ").append((int) lc[i + 1]);
    }
    sb.append(']');
    return sb.toString();
  }
}
