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

public enum EnumCallback {

  ANIMFRAME,
  APPLETREADY,
  ATOMMOVED,
  CLICK,
  ECHO,
  ERROR,
  EVAL,
  HOVER,
  LOADSTRUCT,
  MEASURE,
  MESSAGE,
  MINIMIZATION,
  PICK,
  RESIZE,
  SCRIPT,
  SYNC;

  public static EnumCallback getCallback(String name) {
    
    name = name.toUpperCase();
    name = name.substring(0, Math.max(name.indexOf("CALLBACK"), 0));
    for (EnumCallback item : values())
      if (item.name().equalsIgnoreCase(name))
        return item;
    return null;
  }

  private static String nameList;

  public static synchronized String getNameList() {
    if (nameList == null) {
      StringBuffer names = new StringBuffer();
      for (EnumCallback item : values())
        names.append(item.name().toLowerCase()).append("Callback;");
      nameList = names.toString();
    }
    return nameList;
  }
}
