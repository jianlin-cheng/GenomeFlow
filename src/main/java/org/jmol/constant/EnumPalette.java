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
 * Enum for animation mode.
 */
public enum EnumPalette {

  UNKNOWN(null, (byte) 0xFF),
  NONE("none",StaticConstants.PALETTE_NONE),
  CPK("cpk",StaticConstants.PALETTE_CPK),
  PARTIAL_CHARGE("partialcharge",StaticConstants.PALETTE_PARTIAL_CHARGE),
  FORMAL_CHARGE("formalcharge",StaticConstants.PALETTE_FORMAL_CHARGE),
  TEMP("temperature",StaticConstants.PALETTE_TEMP),
  FIXEDTEMP("fixedtemperature",StaticConstants.PALETTE_FIXEDTEMP),
  SURFACE("surfacedistance",StaticConstants.PALETTE_SURFACE),
  ATOM("atomsequence",StaticConstants.PALETTE_ATOM),    //added lxq35
  STRUCTURE("structure",StaticConstants.PALETTE_STRUCTURE),
  AMINO("amino",StaticConstants.PALETTE_AMINO),
  SHAPELY("shapely",StaticConstants.PALETTE_SHAPELY),
  CHAIN("chain",StaticConstants.PALETTE_CHAIN),
  GROUP("group",StaticConstants.PALETTE_GROUP),
  MONOMER("monomer",StaticConstants.PALETTE_MONOMER),
  MOLECULE("molecule",StaticConstants.PALETTE_MOLECULE),
  ALTLOC("altloc",StaticConstants.PALETTE_ALTLOC),
  INSERTION("insertion",StaticConstants.PALETTE_INSERTION),
  JMOL("jmol",StaticConstants.PALETTE_JMOL),
  RASMOL("rasmol",StaticConstants.PALETTE_RASMOL),
  TYPE("type",StaticConstants.PALETTE_TYPE),
  ENERGY("energy",StaticConstants.PALETTE_ENERGY),
  PROPERTY("property",StaticConstants.PALETTE_PROPERTY),
  VARIABLE("variable",StaticConstants.PALETTE_VARIABLE),
  STRAIGHTNESS("straightness",StaticConstants.PALETTE_STRAIGHTNESS),
  POLYMER("polymer",StaticConstants.PALETTE_POLYMER);
  

  private String name;
  public byte id;

  private EnumPalette(String name, int id) {
    this.name = name;
    this.id = (byte) id;
  }
  
  public static byte pidOf(Object value) {
    return (value instanceof EnumPalette ? 
        ((EnumPalette) value).id 
        : value instanceof Byte ? ((Byte) value).byteValue() 
            : UNKNOWN.id);
  }

  public static boolean isPaletteVariable(byte pid) {
    return ((pid & StaticConstants.PALETTE_VOLATILE) != 0);
  }

  public static EnumPalette getPalette(String paletteName) {
    if (paletteName.indexOf('_') < 0)
      for (EnumPalette item : values())
        if (paletteName.equalsIgnoreCase(item.name))
          return item;
    return (paletteName.indexOf("property_") == 0 ? PROPERTY : UNKNOWN);
  }

  public final static byte getPaletteID(String paletteName) {
    if (paletteName.indexOf('_') < 0)
      for (EnumPalette item : values())
        if (paletteName.equalsIgnoreCase(item.name))
          return item.id;
    return (paletteName.indexOf("property_") == 0 ? PROPERTY.id : UNKNOWN.id);
  }

  public final static String getPaletteName(byte pid) {
    for (EnumPalette item : values())
      if (item.id == pid)
        return item.name;
    return null;
  }

  public final static int[] argbsCpkRasmol = { 0x00FF1493 + (0 << 24), // Xx 0
      0x00FFFFFF + (1 << 24), // H  1
      0x00FFC0CB + (2 << 24), // He 2
      0x00B22222 + (3 << 24), // Li 3
      0x0000FF00 + (5 << 24), // B  5
      0x00C8C8C8 + (6 << 24), // C  6
      0x008F8FFF + (7 << 24), // N  7
      0x00F00000 + (8 << 24), // O  8
      0x00DAA520 + (9 << 24), // F  9
      0x000000FF + (11 << 24), // Na 11
      0x00228B22 + (12 << 24), // Mg 12
      0x00808090 + (13 << 24), // Al 13
      0x00DAA520 + (14 << 24), // Si 14
      0x00FFA500 + (15 << 24), // P  15
      0x00FFC832 + (16 << 24), // S  16
      0x0000FF00 + (17 << 24), // Cl 17
      0x00808090 + (20 << 24), // Ca 20
      0x00808090 + (22 << 24), // Ti 22
      0x00808090 + (24 << 24), // Cr 24
      0x00808090 + (25 << 24), // Mn 25
      0x00FFA500 + (26 << 24), // Fe 26
      0x00A52A2A + (28 << 24), // Ni 28
      0x00A52A2A + (29 << 24), // Cu 29
      0x00A52A2A + (30 << 24), // Zn 30
      0x00A52A2A + (35 << 24), // Br 35
      0x00808090 + (47 << 24), // Ag 47
      0x00A020F0 + (53 << 24), // I  53
      0x00FFA500 + (56 << 24), // Ba 56
      0x00DAA520 + (79 << 24), // Au 79
  };
  /**
   * Default table of CPK atom colors. ghemical colors with a few 
   * modifications
   */
  public final static int[] argbsCpk = { 0xFFFF1493, // Xx 0
      0xFFFFFFFF, // H  1
      0xFFD9FFFF, // He 2
      0xFFCC80FF, // Li 3
      0xFFC2FF00, // Be 4
      0xFFFFB5B5, // B  5
      0xFF909090, // C  6 - changed from ghemical
      0xFF3050F8, // N  7 - changed from ghemical
      0xFFFF0D0D, // O  8
      0xFF90E050, // F  9 - changed from ghemical
      0xFFB3E3F5, // Ne 10
      0xFFAB5CF2, // Na 11
      0xFF8AFF00, // Mg 12
      0xFFBFA6A6, // Al 13
      0xFFF0C8A0, // Si 14 - changed from ghemical
      0xFFFF8000, // P  15
      0xFFFFFF30, // S  16
      0xFF1FF01F, // Cl 17
      0xFF80D1E3, // Ar 18
      0xFF8F40D4, // K  19
      0xFF3DFF00, // Ca 20
      0xFFE6E6E6, // Sc 21
      0xFFBFC2C7, // Ti 22
      0xFFA6A6AB, // V  23
      0xFF8A99C7, // Cr 24
      0xFF9C7AC7, // Mn 25
      0xFFE06633, // Fe 26 - changed from ghemical
      0xFFF090A0, // Co 27 - changed from ghemical
      0xFF50D050, // Ni 28 - changed from ghemical
      0xFFC88033, // Cu 29 - changed from ghemical
      0xFF7D80B0, // Zn 30
      0xFFC28F8F, // Ga 31
      0xFF668F8F, // Ge 32
      0xFFBD80E3, // As 33
      0xFFFFA100, // Se 34
      0xFFA62929, // Br 35
      0xFF5CB8D1, // Kr 36
      0xFF702EB0, // Rb 37
      0xFF00FF00, // Sr 38
      0xFF94FFFF, // Y  39
      0xFF94E0E0, // Zr 40
      0xFF73C2C9, // Nb 41
      0xFF54B5B5, // Mo 42
      0xFF3B9E9E, // Tc 43
      0xFF248F8F, // Ru 44
      0xFF0A7D8C, // Rh 45
      0xFF006985, // Pd 46
      0xFFC0C0C0, // Ag 47 - changed from ghemical
      0xFFFFD98F, // Cd 48
      0xFFA67573, // In 49
      0xFF668080, // Sn 50
      0xFF9E63B5, // Sb 51
      0xFFD47A00, // Te 52
      0xFF940094, // I  53
      0xFF429EB0, // Xe 54
      0xFF57178F, // Cs 55
      0xFF00C900, // Ba 56
      0xFF70D4FF, // La 57
      0xFFFFFFC7, // Ce 58
      0xFFD9FFC7, // Pr 59
      0xFFC7FFC7, // Nd 60
      0xFFA3FFC7, // Pm 61
      0xFF8FFFC7, // Sm 62
      0xFF61FFC7, // Eu 63
      0xFF45FFC7, // Gd 64
      0xFF30FFC7, // Tb 65
      0xFF1FFFC7, // Dy 66
      0xFF00FF9C, // Ho 67
      0xFF00E675, // Er 68
      0xFF00D452, // Tm 69
      0xFF00BF38, // Yb 70
      0xFF00AB24, // Lu 71
      0xFF4DC2FF, // Hf 72
      0xFF4DA6FF, // Ta 73
      0xFF2194D6, // W  74
      0xFF267DAB, // Re 75
      0xFF266696, // Os 76
      0xFF175487, // Ir 77
      0xFFD0D0E0, // Pt 78 - changed from ghemical
      0xFFFFD123, // Au 79 - changed from ghemical
      0xFFB8B8D0, // Hg 80 - changed from ghemical
      0xFFA6544D, // Tl 81
      0xFF575961, // Pb 82
      0xFF9E4FB5, // Bi 83
      0xFFAB5C00, // Po 84
      0xFF754F45, // At 85
      0xFF428296, // Rn 86
      0xFF420066, // Fr 87
      0xFF007D00, // Ra 88
      0xFF70ABFA, // Ac 89
      0xFF00BAFF, // Th 90
      0xFF00A1FF, // Pa 91
      0xFF008FFF, // U  92
      0xFF0080FF, // Np 93
      0xFF006BFF, // Pu 94
      0xFF545CF2, // Am 95
      0xFF785CE3, // Cm 96
      0xFF8A4FE3, // Bk 97
      0xFFA136D4, // Cf 98
      0xFFB31FD4, // Es 99
      0xFFB31FBA, // Fm 100
      0xFFB30DA6, // Md 101
      0xFFBD0D87, // No 102
      0xFFC70066, // Lr 103
      0xFFCC0059, // Rf 104
      0xFFD1004F, // Db 105
      0xFFD90045, // Sg 106
      0xFFE00038, // Bh 107
      0xFFE6002E, // Hs 108
      0xFFEB0026, // Mt 109
  };

}
