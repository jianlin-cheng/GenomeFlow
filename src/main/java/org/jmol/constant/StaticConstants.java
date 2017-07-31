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

public class StaticConstants {

  // necessary for rapid switch((byte)pid) in ColorManager
  // because for some odd reason final static xxx = NONE.id 
  // is not a true final static variable -- it is not 
  // assigned at compile time, I guess, so it can't be used
  // in a switch command. I think this is a Java bug. 

  public final static byte PALETTE_VOLATILE = 0x40;
  public final static byte PALETTE_NONE = 0;
  public final static byte PALETTE_CPK = 1;
  public final static byte PALETTE_PARTIAL_CHARGE = 2;
  public final static byte PALETTE_FORMAL_CHARGE = 3;
  public final static byte PALETTE_TEMP = 4 | PALETTE_VOLATILE;
  public final static byte PALETTE_FIXEDTEMP = 5;
  public final static byte PALETTE_SURFACE = 6 | PALETTE_VOLATILE;
  public final static byte PALETTE_ATOM = 24 | PALETTE_VOLATILE;
  
  public final static byte PALETTE_STRUCTURE = 7;
  public final static byte PALETTE_AMINO = 8;
  public final static byte PALETTE_SHAPELY = 9;
  public final static byte PALETTE_CHAIN = 10;
  public final static byte PALETTE_GROUP = 11 | PALETTE_VOLATILE;
  public final static byte PALETTE_MONOMER = 12 | PALETTE_VOLATILE;
  public final static byte PALETTE_MOLECULE = 13 | PALETTE_VOLATILE;
  public final static byte PALETTE_ALTLOC = 14;
  public final static byte PALETTE_INSERTION = 15;
  public final static byte PALETTE_JMOL = 16;
  public final static byte PALETTE_RASMOL = 17;
  public final static byte PALETTE_TYPE = 18;
  public final static byte PALETTE_ENERGY = 19;
  public final static byte PALETTE_PROPERTY = 20 | PALETTE_VOLATILE;
  public final static byte PALETTE_VARIABLE = 21 | PALETTE_VOLATILE;
  public final static byte PALETTE_STRAIGHTNESS = 22 | PALETTE_VOLATILE;
  public final static byte PALETTE_POLYMER = 23 | PALETTE_VOLATILE;
}
