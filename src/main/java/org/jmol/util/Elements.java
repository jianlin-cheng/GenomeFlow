/* $RCSfile$
 * $Author: egonw $
 * $Date: 2005-11-10 10:52:44 -0500 (Thu, 10 Nov 2005) $
 * $Revision: 4255 $
 *
 * Copyright (C) 2006  Miguel, Jmol Development, www.jmol.org
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

import java.util.BitSet;
import java.util.Hashtable;
import java.util.Map;

import org.jmol.constant.EnumVdw;



public class Elements {

  /**
   * The default elementSymbols. Presumably the only entry which may cause
   * confusion is element 0, whose symbol we have defined as "Xx". 
   */
  public final static String[] elementSymbols = {
    "Xx", // 0
    "H",  // 1
    "He", // 2
    "Li", // 3
    "Be", // 4
    "B",  // 5
    "C",  // 6
    "N",  // 7
    "O",  // 8
    "F",  // 9
    "Ne", // 10
    "Na", // 11
    "Mg", // 12
    "Al", // 13
    "Si", // 14
    "P",  // 15
    "S",  // 16
    "Cl", // 17
    "Ar", // 18
    "K",  // 19
    "Ca", // 20
    "Sc", // 21
    "Ti", // 22
    "V",  // 23
    "Cr", // 24
    "Mn", // 25
    "Fe", // 26
    "Co", // 27
    "Ni", // 28
    "Cu", // 29
    "Zn", // 30
    "Ga", // 31
    "Ge", // 32
    "As", // 33
    "Se", // 34
    "Br", // 35
    "Kr", // 36
    "Rb", // 37
    "Sr", // 38
    "Y",  // 39
    "Zr", // 40
    "Nb", // 41
    "Mo", // 42
    "Tc", // 43
    "Ru", // 44
    "Rh", // 45
    "Pd", // 46
    "Ag", // 47
    "Cd", // 48
    "In", // 49
    "Sn", // 50
    "Sb", // 51
    "Te", // 52
    "I",  // 53
    "Xe", // 54
    "Cs", // 55
    "Ba", // 56
    "La", // 57
    "Ce", // 58
    "Pr", // 59
    "Nd", // 60
    "Pm", // 61
    "Sm", // 62
    "Eu", // 63
    "Gd", // 64
    "Tb", // 65
    "Dy", // 66
    "Ho", // 67
    "Er", // 68
    "Tm", // 69
    "Yb", // 70
    "Lu", // 71
    "Hf", // 72
    "Ta", // 73
    "W",  // 74
    "Re", // 75
    "Os", // 76
    "Ir", // 77
    "Pt", // 78
    "Au", // 79
    "Hg", // 80
    "Tl", // 81
    "Pb", // 82
    "Bi", // 83
    "Po", // 84
    "At", // 85
    "Rn", // 86
    "Fr", // 87
    "Ra", // 88
    "Ac", // 89
    "Th", // 90
    "Pa", // 91
    "U",  // 92
    "Np", // 93
    "Pu", // 94
    "Am", // 95
    "Cm", // 96
    "Bk", // 97
    "Cf", // 98
    "Es", // 99
    "Fm", // 100
    "Md", // 101
    "No", // 102
    "Lr", // 103
    "Rf", // 104
    "Db", // 105
    "Sg", // 106
    "Bh", // 107
    "Hs", // 108
    "Mt", // 109
    /*
    "Ds", // 110
    "Uu",// 111
    "Ub",// 112
    "Ut",// 113
    "Uq",// 114
    "Up",// 115
    "Uh",// 116
    "Us",// 117
    "Uuo",// 118
    */
  };

   public final static float[] atomicMass = {
    0, 
    /* 1 H */ 1.008f, 4.003f, 
    /* 2 Li */ 6.941f, 9.012f,      10.81f, 12.011f, 14.007f, 15.999f, 18.998f, 20.18f, 
    /* 3 Na */ 22.99f, 24.305f,     26.981f, 28.086f, 30.974f, 32.07f, 35.453f, 39.948f, 
    /* 4 K */ 39.1f, 40.08f, 44.956f, 47.88f, 50.941f, 52f, 54.938f, 55.847f, 58.93f, 58.69f, 63.55f, 65.39f, 69.72f, 72.61f, 74.92f, 78.96f, 79.9f, 83.8f, 
    /* 5 Rb */ 85.47f, 87.62f, 88.91f, 91.22f, 92.91f, 95.94f, 98.91f, 101.07f, 102.91f, 106.42f, 107.87f, 112.41f, 114.82f, 118.71f, 121.75f, 127.6f, 126.91f, 131.29f, 
    /* 6 Cs, Ba, actinides */ 132.91f, 137.33f,      138.91f, 140.12f, 140.91f, 144.24f, 144.9f, 150.36f, 151.96f, 157.25f, 158.93f, 162.5f, 164.93f, 167.26f, 168.93f, 173.04f, 174.97f, 
    /* 6 Hf */ 178.49f, 180.95f, 183.85f, 186.21f, 190.2f, 192.22f, 195.08f, 196.97f, 200.59f, 204.38f, 207.2f, 208.98f, 210f, 210f, 222f, 
    /* 7 Fr, Ra, lanthanides */ 223f, 226.03f, 227.03f, 232.04f, 231.04f, 238.03f, 237.05f, 239.1f, 243.1f, 247.1f, 247.1f, 252.1f, 252.1f, 257.1f, 256.1f, 259.1f, 260.1f, 
    /* 7 Rf - Mt */ 261f, 262f, 263f, 262f, 265f, 268f
    };
  
   public static float getAtomicMass(int i) {
     return (i < 1 || i >= atomicMass.length ? 0 : atomicMass[i]);
   }
   

  /**
   * one larger than the last elementNumber, same as elementSymbols.length
   */
  public final static int elementNumberMax = elementSymbols.length;
  /**
   * @param elementSymbol First char must be upper case, second char accepts upper or lower case
   * @param isSilent TODO
   * @return elementNumber = atomicNumber + IsotopeNumber*128
   */
  public final static short elementNumberFromSymbol(String elementSymbol, boolean isSilent) {
    if (htElementMap == null) {
      Map<String, Integer> map = new Hashtable<String, Integer>();
      for (int elementNumber = elementNumberMax; --elementNumber >= 0;) {
        String symbol = elementSymbols[elementNumber];
        Integer boxed = Integer.valueOf(elementNumber);
        map.put(symbol, boxed);
        if (symbol.length() == 2)
          map.put(symbol.toUpperCase(), boxed);
      }
      for (int i = altElementMax; --i >= firstIsotope;) {
        String symbol = altElementSymbols[i];
        Integer boxed = Integer.valueOf(altElementNumbers[i]);
        map.put(symbol, boxed);
        if (symbol.length() == 2)
          map.put(symbol.toUpperCase(), boxed);
      }
      htElementMap = map;
    }
    if (elementSymbol == null)
      return 0;
    Integer boxedAtomicNumber = htElementMap.get(elementSymbol);
    if (boxedAtomicNumber != null)
      return (short) boxedAtomicNumber.intValue();
    if (!isSilent)
      Logger.error("'" + elementSymbol + "' is not a recognized symbol");
    return 0;
  }
  public static Map<String, Integer> htElementMap;
  /**
   * @param elementNumber may be atomicNumber + isotopeNumber*128
   * @return elementSymbol
   */
  public final static String elementSymbolFromNumber(int elementNumber) {
    //Isotopes as atomicNumber + IsotopeNumber * 128
    if (elementNumber >= elementNumberMax) {
      for (int j = altElementMax; --j >= 0;)
        if (elementNumber == altElementNumbers[j])
          return altElementSymbols[j];
      elementNumber %= 128;
    }
    if (elementNumber < 0 || elementNumber >= elementNumberMax)
      elementNumber = 0;
    return elementSymbols[elementNumber];
  }
  public final static String elementNames[] = {
    "unknown",       //  0
    "hydrogen",      //  1
    "helium",        //  2
    "lithium",       //  3
    "beryllium",     //  4
    "boron",         //  5
    "carbon",        //  6
    "nitrogen",      //  7
    "oxygen",        //  8
    "fluorine",      //  9
    "neon",          // 10
    "sodium",        // 11
    "magnesium",     // 12
    "aluminum",      // 13 aluminium
    "silicon",       // 14
    "phosphorus",    // 15
    "sulfur",        // 16 sulphur
    "chlorine",      // 17
    "argon",         // 18
    "potassium",     // 19
    "calcium",       // 20
    "scandium",      // 21
    "titanium",      // 22
    "vanadium",      // 23
    "chromium",      // 24
    "manganese",     // 25
    "iron",          // 26
    "cobalt",        // 27
    "nickel",        // 28
    "copper",        // 29
    "zinc",          // 30
    "gallium",       // 31
    "germanium",     // 32
    "arsenic",       // 33
    "selenium",      // 34
    "bromine",       // 35
    "krypton",       // 36
    "rubidium",      // 37
    "strontium",     // 38
    "yttrium",       // 39
    "zirconium",     // 40
    "niobium",       // 41
    "molybdenum",    // 42
    "technetium",    // 43
    "ruthenium",     // 44
    "rhodium",       // 45
    "palladium",     // 46
    "silver",        // 47
    "cadmium",       // 48
    "indium",        // 49
    "tin",           // 50
    "antimony",      // 51
    "tellurium",     // 52
    "iodine",        // 53
    "xenon",         // 54
    "cesium",        // 55  caesium
    "barium",        // 56
    "lanthanum",     // 57
    "cerium",        // 58
    "praseodymium",  // 59
    "neodymium",     // 60
    "promethium",    // 61
    "samarium",      // 62
    "europium",      // 63
    "gadolinium",    // 64
    "terbium",       // 66
    "dysprosium",    // 66
    "holmium",       // 67
    "erbium",        // 68
    "thulium",       // 69
    "ytterbium",     // 70
    "lutetium",      // 71
    "hafnium",       // 72
    "tantalum",      // 73
    "tungsten",      // 74
    "rhenium",       // 75
    "osmium",        // 76
    "iridium",       // 77
    "platinum",      // 78
    "gold",          // 79
    "mercury",       // 80
    "thallium",      // 81
    "lead",          // 82
    "bismuth",       // 83
    "polonium",      // 84
    "astatine",      // 85
    "radon",         // 86
    "francium",      // 87
    "radium",        // 88
    "actinium",      // 89
    "thorium",       // 90
    "protactinium",  // 91
    "uranium",       // 92
    "neptunium",     // 93
    "plutonium",     // 94
    "americium",     // 95
    "curium",        // 96
    "berkelium",     // 97
    "californium",   // 98
    "einsteinium",   // 99
    "fermium",       // 100
    "mendelevium",   // 101
    "nobelium",      // 102
    "lawrencium",    // 103
    "rutherfordium", // 104
    "dubnium",       // 105
    "seaborgium",    // 106
    "bohrium",       // 107
    "hassium",       // 108
    "meitnerium"     // 109
  };
  
  /**
   * @param elementNumber may be atomicNumber + isotopeNumber*128
   * @return elementName
   */
  public final static String elementNameFromNumber(int elementNumber) {
    //Isotopes as atomicNumber + IsotopeNumber * 128
    if (elementNumber >= elementNumberMax) {
      for (int j = altElementMax; --j >= 0;)
        if (elementNumber == altElementNumbers[j])
          return altElementNames[j];
      elementNumber %= 128;
    }
    if (elementNumber < 0 || elementNumber >= elementNumberMax)
      elementNumber = 0;
    return elementNames[elementNumber];
  }
  
  /**
   * @param i index into altElementNames
   * @return elementName
   */
  public final static String altElementNameFromIndex(int i) {
    return altElementNames[i];
  }
  
  /**
   * @param i index into altElementNumbers
   * @return elementNumber (may be atomicNumber + isotopeNumber*128)
   */
  public final static short altElementNumberFromIndex(int i) {
    return altElementNumbers[i];
  }
  
  /** 
   * @param i index into altElementSymbols
   * @return elementSymbol
   */
  public final static String altElementSymbolFromIndex(int i) {
    return altElementSymbols[i];
  }
  
  /**
   * @param i index into altElementSymbols
   * @return 2H
   */
  public final static String altIsotopeSymbolFromIndex(int i) {
    int code = altElementNumbers[i]; 
    return (code >> 7) + elementSymbolFromNumber(code & 127);
  }
  
  /**
   * @param i index into altElementSymbols
   * @return H2
   */
  public final static String altIsotopeSymbolFromIndex2(int i) {
    int code = altElementNumbers[i]; 
    return  elementSymbolFromNumber(code & 127) + (code >> 7);
  }
  
  public final static short getElementNumber(short atomicAndIsotopeNumber) {
    return (short) (atomicAndIsotopeNumber % 128);
  }

  public final static short getIsotopeNumber(short atomicAndIsotopeNumber) {
    return (short) (atomicAndIsotopeNumber >> 7);
  }

  public final static short getAtomicAndIsotopeNumber(int n, int mass) {
    return (short) ((n < 0 ? 0 : n) + (mass <= 0 ? 0 : mass << 7));
  }
  
  /**
   * @param atomicAndIsotopeNumber (may be atomicNumber + isotopeNumber*128)
   * @return  index into altElementNumbers
   */
  public final static int altElementIndexFromNumber(int atomicAndIsotopeNumber) {
    for (int i = 0; i < altElementMax; i++)
      if (altElementNumbers[i] == atomicAndIsotopeNumber)
        return i;
    return 0;
  }
  
  private static int[] naturalIsotopeMasses = {
    1, 1, // H
    6, 12,// C
    7, 14,// N
    8, 16,// O   -- can add any number more if desired
  };

  public static int getNaturalIsotope(int elementNumber) {
    for (int i = 0; i < naturalIsotopeMasses.length; i += 2)
      if (naturalIsotopeMasses[i] == elementNumber)
        return naturalIsotopeMasses[++i];
    return 0;
  }


  // add as we go
  private final static String naturalIsotopes = "1H,12C,14N,";
  
  public final static boolean isNaturalIsotope(String isotopeSymbol) {
    return (naturalIsotopes.indexOf(isotopeSymbol + ",") >= 0);      
  }
  
  /**
   * first entry of an actual isotope int the altElementSymbols, altElementNames, altElementNumbers arrays
   */
  public final static int firstIsotope = 4;

  private final static short[] altElementNumbers = {
    0,
    13,
    16,
    55,
    (2 << 7) + 1, // D = 2*128 + 1 <-- firstIsotope
    (3 << 7) + 1, // T = 3*128 + 1
    (11 << 7) + 6, // 11C
    (13 << 7) + 6, // 13C
    (14 << 7) + 6, // 14C
    (15 << 7) + 7, // 15N
  };
  
  /**
   * length of the altElementSymbols, altElementNames, altElementNumbers arrays
   */
  public final static int altElementMax = altElementNumbers.length;

  private final static String[] altElementSymbols = {
    "Xx",
    "Al",
    "S",
    "Cs",
    "D",
    "T",
    "11C",
    "13C",
    "14C",
    "15N",
  };
  
  private final static String[] altElementNames = {
    "dummy",
    "aluminium",
    "sulphur",
    "caesium",
    "deuterium",
    "tritium",
    "",
    "",
    "",
    "",
  };

  public final static String VdwPROBE = "#VDW radii for PROBE;{_H}.vdw = 1.0;" +
  "{_H and connected(_C) and not connected(within(smiles,'[a]'))}.vdw = 1.17;" +
  "{_C}.vdw = 1.75;{_C and connected(3) and connected(_O)}.vdw = 1.65;" +
  "{_N}.vdw = 1.55;" +
  "{_O}.vdw = 1.4;" +
  "{_P}.vdw = 1.8;" +
  "{_S}.vdw = 1.8;" +
  "message VDW radii for H, C, N, O, P, and S set according to " +
  "Word, et al., J. Mol. Biol. (1999) 285, 1711-1733"; //MolProbity

  /**
   * Default table of van der Waals Radii.
   * values are stored as MAR -- Milli Angstrom Radius
   * Used for spacefill rendering of atoms.
   * Values taken from OpenBabel.
   * 
   * Note that AUTO_JMOL, AUTO_BABEL, and AUTO_RASMOL are 4, 5, and 6, respectively,
   * so their mod will be JMOL, BABEL, and RASMOL. AUTO is 8, so will default to Jmol
   * 
   * @see <a href="http://openbabel.sourceforge.net">openbabel.sourceforge.net</a>
   * @see <a href="http://jmol.svn.sourceforge.net/viewvc/jmol/trunk/Jmol/src/org/jmol/_documents/vdw_comparison.xls">vdw_comparison.xls</a>
   */
  public final static short[] vanderwaalsMars = {
    // Jmol -- accounts for missing H atoms on PDB files -- source unknown
         // openBabel -- standard values http://openbabel.svn.sourceforge.net/viewvc/openbabel/openbabel/trunk/data/element.txt?revision=3469&view=markup
              // openRasmol -- VERY tight values http://www.openrasmol.org/software/RasMol_2.7.3/src/abstree.h 
                   // filler/reserved
    //Jmol-11.2,
         //OpenBabel-2.2.1,
              //OpenRasmol-2.7.2.1.1,
                   //OpenBabel-2.1.1
    1000,1000,1000,1000, // XX 0
    1200,1100,1100,1200, // H 1
    1400,1400,2200,1400, // He 2
    1820,1810,1220,2200, // Li 3
    1700,1530,628,1900, // Be 4
    2080,1920,1548,1800, // B 5
    1950,1700,1548,1700, // C 6
    1850,1550,1400,1600, // N 7
    1700,1520,1348,1550, // O 8
    1730,1470,1300,1500, // F 9
    1540,1540,2020,1540, // Ne 10
    2270,2270,2200,2400, // Na 11
    1730,1730,1500,2200, // Mg 12
    2050,1840,1500,2100, // Al 13
    2100,2100,2200,2100, // Si 14
    2080,1800,1880,1950, // P 15
    2000,1800,1808,1800, // S 16
    1970,1750,1748,1800, // Cl 17
    1880,1880,2768,1880, // Ar 18
    2750,2750,2388,2800, // K 19
    1973,2310,1948,2400, // Ca 20
    1700,2300,1320,2300, // Sc 21
    1700,2150,1948,2150, // Ti 22
    1700,2050,1060,2050, // V 23
    1700,2050,1128,2050, // Cr 24
    1700,2050,1188,2050, // Mn 25
    1700,2050,1948,2050, // Fe 26
    1700,2000,1128,2000, // Co 27
    1630,2000,1240,2000, // Ni 28
    1400,2000,1148,2000, // Cu 29
    1390,2100,1148,2100, // Zn 30
    1870,1870,1548,2100, // Ga 31
    1700,2110,3996,2100, // Ge 32
    1850,1850,828,2050, // As 33
    1900,1900,900,1900, // Se 34
    2100,1830,1748,1900, // Br 35
    2020,2020,1900,2020, // Kr 36
    1700,3030,2648,2900, // Rb 37
    1700,2490,2020,2550, // Sr 38
    1700,2400,1608,2400, // Y 39
    1700,2300,1420,2300, // Zr 40
    1700,2150,1328,2150, // Nb 41
    1700,2100,1748,2100, // Mo 42
    1700,2050,1800,2050, // Tc 43
    1700,2050,1200,2050, // Ru 44
    1700,2000,1220,2000, // Rh 45
    1630,2050,1440,2050, // Pd 46
    1720,2100,1548,2100, // Ag 47
    1580,2200,1748,2200, // Cd 48
    1930,2200,1448,2200, // In 49
    2170,1930,1668,2250, // Sn 50
    2200,2170,1120,2200, // Sb 51
    2060,2060,1260,2100, // Te 52
    2150,1980,1748,2100, // I 53
    2160,2160,2100,2160, // Xe 54
    1700,3430,3008,3000, // Cs 55
    1700,2680,2408,2700, // Ba 56
    1700,2500,1828,2500, // La 57
    1700,2480,1860,2480, // Ce 58
    1700,2470,1620,2470, // Pr 59
    1700,2450,1788,2450, // Nd 60
    1700,2430,1760,2430, // Pm 61
    1700,2420,1740,2420, // Sm 62
    1700,2400,1960,2400, // Eu 63
    1700,2380,1688,2380, // Gd 64
    1700,2370,1660,2370, // Tb 65
    1700,2350,1628,2350, // Dy 66
    1700,2330,1608,2330, // Ho 67
    1700,2320,1588,2320, // Er 68
    1700,2300,1568,2300, // Tm 69
    1700,2280,1540,2280, // Yb 70
    1700,2270,1528,2270, // Lu 71
    1700,2250,1400,2250, // Hf 72
    1700,2200,1220,2200, // Ta 73
    1700,2100,1260,2100, // W 74
    1700,2050,1300,2050, // Re 75
    1700,2000,1580,2000, // Os 76
    1700,2000,1220,2000, // Ir 77
    1720,2050,1548,2050, // Pt 78
    1660,2100,1448,2100, // Au 79
    1550,2050,1980,2050, // Hg 80
    1960,1960,1708,2200, // Tl 81
    2020,2020,2160,2300, // Pb 82
    1700,2070,1728,2300, // Bi 83
    1700,1970,1208,2000, // Po 84
    1700,2020,1120,2000, // At 85
    1700,2200,2300,2000, // Rn 86
    1700,3480,3240,2000, // Fr 87
    1700,2830,2568,2000, // Ra 88
    1700,2000,2120,2000, // Ac 89
    1700,2400,1840,2400, // Th 90
    1700,2000,1600,2000, // Pa 91
    1860,2300,1748,2300, // U 92
    1700,2000,1708,2000, // Np 93
    1700,2000,1668,2000, // Pu 94
    1700,2000,1660,2000, // Am 95
    1700,2000,1648,2000, // Cm 96
    1700,2000,1640,2000, // Bk 97
    1700,2000,1628,2000, // Cf 98
    1700,2000,1620,2000, // Es 99
    1700,2000,1608,2000, // Fm 100
    1700,2000,1600,2000, // Md 101
    1700,2000,1588,2000, // No 102
    1700,2000,1580,2000, // Lr 103
    1700,2000,1600,2000, // Rf 104
    1700,2000,1600,2000, // Db 105
    1700,2000,1600,2000, // Sg 106
    1700,2000,1600,2000, // Bh 107
    1700,2000,1600,2000, // Hs 108
    1700,2000,1600,2000, // Mt 109
  };

  /**
   * Default table of covalent Radii
   * stored as a short mar ... Milli Angstrom Radius
   * Values taken from OpenBabel.
   * @see <a href="http://openbabel.sourceforge.net">openbabel.sourceforge.net</a>
   */
  public final static short[] covalentMars = {
    0,    //   0  Xx does not bond
    230,  //   1  H
    930,  //   2  He
    680,  //   3  Li
    350,  //   4  Be
    830,  //   5  B
    680,  //   6  C
    680,  //   7  N
    680,  //   8  O
    640,  //   9  F
    1120, //  10  Ne
    970,  //  11  Na
    1100, //  12  Mg
    1350, //  13  Al
    1200, //  14  Si
    750,  //  15  P
    1020, //  16  S
    990,  //  17  Cl
    1570, //  18  Ar
    1330, //  19  K
    990,  //  20  Ca
    1440, //  21  Sc
    1470, //  22  Ti
    1330, //  23  V
    1350, //  24  Cr
    1350, //  25  Mn
    1340, //  26  Fe
    1330, //  27  Co
    1500, //  28  Ni
    1520, //  29  Cu
    1450, //  30  Zn
    1220, //  31  Ga
    1170, //  32  Ge
    1210, //  33  As
    1220, //  34  Se
    1210, //  35  Br
    1910, //  36  Kr
    1470, //  37  Rb
    1120, //  38  Sr
    1780, //  39  Y
    1560, //  40  Zr
    1480, //  41  Nb
    1470, //  42  Mo
    1350, //  43  Tc
    1400, //  44  Ru
    1450, //  45  Rh
    1500, //  46  Pd
    1590, //  47  Ag
    1690, //  48  Cd
    1630, //  49  In
    1460, //  50  Sn
    1460, //  51  Sb
    1470, //  52  Te
    1400, //  53  I
    1980, //  54  Xe
    1670, //  55  Cs
    1340, //  56  Ba
    1870, //  57  La
    1830, //  58  Ce
    1820, //  59  Pr
    1810, //  60  Nd
    1800, //  61  Pm
    1800, //  62  Sm
    1990, //  63  Eu
    1790, //  64  Gd
    1760, //  65  Tb
    1750, //  66  Dy
    1740, //  67  Ho
    1730, //  68  Er
    1720, //  69  Tm
    1940, //  70  Yb
    1720, //  71  Lu
    1570, //  72  Hf
    1430, //  73  Ta
    1370, //  74  W
    1350, //  75  Re
    1370, //  76  Os
    1320, //  77  Ir
    1500, //  78  Pt
    1500, //  79  Au
    1700, //  80  Hg
    1550, //  81  Tl
    1540, //  82  Pb
    1540, //  83  Bi
    1680, //  84  Po
    1700, //  85  At
    2400, //  86  Rn
    2000, //  87  Fr
    1900, //  88  Ra
    1880, //  89  Ac
    1790, //  90  Th
    1610, //  91  Pa
    1580, //  92  U
    1550, //  93  Np
    1530, //  94  Pu
    1510, //  95  Am
    1500, //  96  Cm
    1500, //  97  Bk
    1500, //  98  Cf
    1500, //  99  Es
    1500, // 100  Fm
    1500, // 101  Md
    1500, // 102  No
    1500, // 103  Lr
    1600, // 104  Rf
    1600, // 105  Db
    1600, // 106  Sg
    1600, // 107  Bh
    1600, // 108  Hs
    1600, // 109  Mt
  };

  /****************************************************************
   * ionic radii are looked up using an array of shorts (16 bits each) 
   * that contains the atomic number, the charge, and the radius in two
   * consecutive values, encoded as follows:
   * 
   *   (atomicNumber << 4) + (charge + 4), radiusAngstroms*1000
   * 
   * That is, (atomicNumber * 16 + charge + 4), milliAngstromRadius
   * 
   * This allows for charges from -4 to 11, but we only really have -4 to 7.
   *
   * This data is from
   *  Handbook of Chemistry and Physics. 48th Ed, 1967-8, p. F143
   *  (scanned for Jmol by Phillip Barak, Jan 2004)
   *  
   * Reorganized from two separate arrays 9/2006 by Bob Hanson, who thought
   * it was just too hard to look these up and, if necessary, add or modify.
   * At the same time, the table was split into cations and anions for easier
   * retrieval.
   * 
   * O- and N+ removed 9/2008 - BH. The problem is that
   * the formal charge is used to determine bonding radius. 
   * But these formal charges are different than the charges used in 
   * compilation of HCP data (which is crystal ionic radii). 
   * Specifically, because O- and N+ are very common in organic 
   * compounds, I have removed their radii from the table FOR OUR PURPOSES HERE.
   * 
   * I suppose there are some ionic compounds that have O- and N+ as 
   * isolated ions, but what they would be I have no clue. Better to 
   * be safe and go with somewhat more reasonable values.
   * 
   *  Argh. Changed for Jmol 11.6.RC15
   * 
   *  
   ****************************************************************/
  
  public final static int FORMAL_CHARGE_MIN = -4;
  public final static int FORMAL_CHARGE_MAX = 7;

  private final static short[] cationLookupTable = {
    (3 << 4) + (1 + 4),   680,  // "Li+1"
    (4 << 4) + (1 + 4),   440,  // "Be+1"
    (4 << 4) + (2 + 4),   350,  // "Be+2"
    (5 << 4) + (1 + 4),   350,  // "B+1"
    (5 << 4) + (3 + 4),   230,  // "B+3"
    (6 << 4) + (4 + 4),   160,  // "C+4"
    (7 << 4) + (1 + 4),   680,  // "N+1" // covalent radius --  250 way too small for organic charges
    (7 << 4) + (3 + 4),   160,  // "N+3"
    (7 << 4) + (5 + 4),   130,  // "N+5"
    (8 << 4) + (1 + 4),   220,  // "O+1"
    (8 << 4) + (6 + 4),   90,   // "O+6"
    (9 << 4) + (7 + 4),   80,   // "F+7"
    (10 << 4) + (1 + 4),  1120, // "Ne+1"
    (11 << 4) + (1 + 4),  970,  // "Na+1"
    (12 << 4) + (1 + 4),  820,  // "Mg+1"
    (12 << 4) + (2 + 4),  660,  // "Mg+2"
    (13 << 4) + (3 + 4),  510,  // "Al+3"
    (14 << 4) + (1 + 4),  650,  // "Si+1"
    (14 << 4) + (4 + 4),  420,  // "Si+4"
    (15 << 4) + (3 + 4),  440,  // "P+3"
    (15 << 4) + (5 + 4),  350,  // "P+5"
    (16 << 4) + (2 + 4),  2190, // "S+2"
    (16 << 4) + (4 + 4),  370,  // "S+4"
    (16 << 4) + (6 + 4),  300,  // "S+6"
    (17 << 4) + (5 + 4),  340,  // "Cl+5"
    (17 << 4) + (7 + 4),  270,  // "Cl+7"
    (18 << 4) + (1 + 4),  1540, // "Ar+1"
    (19 << 4) + (1 + 4),  1330, // "K+1"
    (20 << 4) + (1 + 4),  1180, // "Ca+1"
    (20 << 4) + (2 + 4),  990,  // "Ca+2"
    (21 << 4) + (3 + 4),  732,  // "Sc+3"
    (22 << 4) + (1 + 4),  960,  // "Ti+1"
    (22 << 4) + (2 + 4),  940,  // "Ti+2"
    (22 << 4) + (3 + 4),  760,  // "Ti+3"
    (22 << 4) + (4 + 4),  680,  // "Ti+4"
    (23 << 4) + (2 + 4),  880,  // "V+2"
    (23 << 4) + (3 + 4),  740,  // "V+3"
    (23 << 4) + (4 + 4),  630,  // "V+4"
    (23 << 4) + (5 + 4),  590,  // "V+5"
    (24 << 4) + (1 + 4),  810,  // "Cr+1"
    (24 << 4) + (2 + 4),  890,  // "Cr+2"
    (24 << 4) + (3 + 4),  630,  // "Cr+3"
    (24 << 4) + (6 + 4),  520,  // "Cr+6"
    (25 << 4) + (2 + 4),  800,  // "Mn+2"
    (25 << 4) + (3 + 4),  660,  // "Mn+3"
    (25 << 4) + (4 + 4),  600,  // "Mn+4"
    (25 << 4) + (7 + 4),  460,  // "Mn+7"
    (26 << 4) + (2 + 4),  740,  // "Fe+2"
    (26 << 4) + (3 + 4),  640,  // "Fe+3"
    (27 << 4) + (2 + 4),  720,  // "Co+2"
    (27 << 4) + (3 + 4),  630,  // "Co+3"
    (28 << 4) + (2 + 4),  690,  // "Ni+2"
    (29 << 4) + (1 + 4),  960,  // "Cu+1"
    (29 << 4) + (2 + 4),  720,  // "Cu+2"
    (30 << 4) + (1 + 4),  880,  // "Zn+1"
    (30 << 4) + (2 + 4),  740,  // "Zn+2"
    (31 << 4) + (1 + 4),  810,  // "Ga+1"
    (31 << 4) + (3 + 4),  620,  // "Ga+3"
    (32 << 4) + (2 + 4),  730,  // "Ge+2"
    (32 << 4) + (4 + 4),  530,  // "Ge+4"
    (33 << 4) + (3 + 4),  580,  // "As+3"
    (33 << 4) + (5 + 4),  460,  // "As+5"
    (34 << 4) + (1 + 4),  660,  // "Se+1"
    (34 << 4) + (4 + 4),  500,  // "Se+4"
    (34 << 4) + (6 + 4),  420,  // "Se+6"
    (35 << 4) + (5 + 4),  470,  // "Br+5"
    (35 << 4) + (7 + 4),  390,  // "Br+7"
    (37 << 4) + (1 + 4),  1470, // "Rb+1"
    (38 << 4) + (2 + 4),  1120, // "Sr+2"
    (39 << 4) + (3 + 4),  893,  // "Y+3"
    (40 << 4) + (1 + 4),  1090, // "Zr+1"
    (40 << 4) + (4 + 4),  790,  // "Zr+4"
    (41 << 4) + (1 + 4),  1000, // "Nb+1"
    (41 << 4) + (4 + 4),  740,  // "Nb+4"
    (41 << 4) + (5 + 4),  690,  // "Nb+5"
    (42 << 4) + (1 + 4),  930,  // "Mo+1"
    (42 << 4) + (4 + 4),  700,  // "Mo+4"
    (42 << 4) + (6 + 4),  620,  // "Mo+6"
    (43 << 4) + (7 + 4),  979,  // "Tc+7"
    (44 << 4) + (4 + 4),  670,  // "Ru+4"
    (45 << 4) + (3 + 4),  680,  // "Rh+3"
    (46 << 4) + (2 + 4),  800,  // "Pd+2"
    (46 << 4) + (4 + 4),  650,  // "Pd+4"
    (47 << 4) + (1 + 4),  1260, // "Ag+1"
    (47 << 4) + (2 + 4),  890,  // "Ag+2"
    (48 << 4) + (1 + 4),  1140, // "Cd+1"
    (48 << 4) + (2 + 4),  970,  // "Cd+2"
    (49 << 4) + (3 + 4),  810,  // "In+3"
    (50 << 4) + (2 + 4),  930,  // "Sn+2"
    (50 << 4) + (4 + 4),  710,  // "Sn+4"
    (51 << 4) + (3 + 4),  760,  // "Sb+3"
    (51 << 4) + (5 + 4),  620,  // "Sb+5"
    (52 << 4) + (1 + 4),  820,  // "Te+1"
    (52 << 4) + (4 + 4),  700,  // "Te+4"
    (52 << 4) + (6 + 4),  560,  // "Te+6"
    (53 << 4) + (5 + 4),  620,  // "I+5"
    (53 << 4) + (7 + 4),  500,  // "I+7"
    (55 << 4) + (1 + 4),  1670, // "Cs+1"
    (56 << 4) + (1 + 4),  1530, // "Ba+1"
    (56 << 4) + (2 + 4),  1340, // "Ba+2"
    (57 << 4) + (1 + 4),  1390, // "La+1"
    (57 << 4) + (3 + 4),  1016, // "La+3"
    (58 << 4) + (1 + 4),  1270, // "Ce+1"
    (58 << 4) + (3 + 4),  1034, // "Ce+3"
    (58 << 4) + (4 + 4),  920,  // "Ce+4"
    (59 << 4) + (3 + 4),  1013, // "Pr+3"
    (59 << 4) + (4 + 4),  900,  // "Pr+4"
    (60 << 4) + (3 + 4),  995,  // "Nd+3"
    (61 << 4) + (3 + 4),  979,  // "Pm+3"
    (62 << 4) + (3 + 4),  964,  // "Sm+3"
    (63 << 4) + (2 + 4),  1090, // "Eu+2"
    (63 << 4) + (3 + 4),  950,  // "Eu+3"
    (64 << 4) + (3 + 4),  938,  // "Gd+3"
    (65 << 4) + (3 + 4),  923,  // "Tb+3"
    (65 << 4) + (4 + 4),  840,  // "Tb+4"
    (66 << 4) + (3 + 4),  908,  // "Dy+3"
    (67 << 4) + (3 + 4),  894,  // "Ho+3"
    (68 << 4) + (3 + 4),  881,  // "Er+3"
    (69 << 4) + (3 + 4),  870,  // "Tm+3"
    (70 << 4) + (2 + 4),  930,  // "Yb+2"
    (70 << 4) + (3 + 4),  858,  // "Yb+3"
    (71 << 4) + (3 + 4),  850,  // "Lu+3"
    (72 << 4) + (4 + 4),  780,  // "Hf+4"
    (73 << 4) + (5 + 4),  680,  // "Ta+5"
    (74 << 4) + (4 + 4),  700,  // "W+4"
    (74 << 4) + (6 + 4),  620,  // "W+6"
    (75 << 4) + (4 + 4),  720,  // "Re+4"
    (75 << 4) + (7 + 4),  560,  // "Re+7"
    (76 << 4) + (4 + 4),  880,  // "Os+4"
    (76 << 4) + (6 + 4),  690,  // "Os+6"
    (77 << 4) + (4 + 4),  680,  // "Ir+4"
    (78 << 4) + (2 + 4),  800,  // "Pt+2"
    (78 << 4) + (4 + 4),  650,  // "Pt+4"
    (79 << 4) + (1 + 4),  1370, // "Au+1"
    (79 << 4) + (3 + 4),  850,  // "Au+3"
    (80 << 4) + (1 + 4),  1270, // "Hg+1"
    (80 << 4) + (2 + 4),  1100, // "Hg+2"
    (81 << 4) + (1 + 4),  1470, // "Tl+1"
    (81 << 4) + (3 + 4),  950,  // "Tl+3"
    (82 << 4) + (2 + 4),  1200, // "Pb+2"
    (82 << 4) + (4 + 4),  840,  // "Pb+4"
    (83 << 4) + (1 + 4),  980,  // "Bi+1"
    (83 << 4) + (3 + 4),  960,  // "Bi+3"
    (83 << 4) + (5 + 4),  740,  // "Bi+5"
    (84 << 4) + (6 + 4),  670,  // "Po+6"
    (85 << 4) + (7 + 4),  620,  // "At+7"
    (87 << 4) + (1 + 4),  1800, // "Fr+1"
    (88 << 4) + (2 + 4),  1430, // "Ra+2"
    (89 << 4) + (3 + 4),  1180, // "Ac+3"
    (90 << 4) + (4 + 4),  1020, // "Th+4"
    (91 << 4) + (3 + 4),  1130, // "Pa+3"
    (91 << 4) + (4 + 4),  980,  // "Pa+4"
    (91 << 4) + (5 + 4),  890,  // "Pa+5"
    (92 << 4) + (4 + 4),  970,  // "U+4"
    (92 << 4) + (6 + 4),  800,  // "U+6"
    (93 << 4) + (3 + 4),  1100, // "Np+3"
    (93 << 4) + (4 + 4),  950,  // "Np+4"
    (93 << 4) + (7 + 4),  710,  // "Np+7"
    (94 << 4) + (3 + 4),  1080, // "Pu+3"
    (94 << 4) + (4 + 4),  930,  // "Pu+4"
    (95 << 4) + (3 + 4),  1070, // "Am+3"
    (95 << 4) + (4 + 4),  920,  // "Am+4"
  };

  private final static short[] anionLookupTable = {
    (1 << 4) + (-1 + 4),  1540, // "H-1"
    (6 << 4) + (-4 + 4),  2600, // "C-4"
    (7 << 4) + (-3 + 4),  1710, // "N-3"
    (8 << 4) + (-2 + 4),  1360, // "O-2" *Shannon (1976)
    (8 << 4) + (-1 + 4),   680, // "O-1" *necessary for CO2-, NO2, etc.  
    (9 << 4) + (-1 + 4),  1330, // "F-1"
  //(14 << 4) + (-4 + 4), 2710, // "Si-4" *not in 77th
  //(14 << 4) + (-1 + 4), 3840, // "Si-1" *not in 77th 
    (15 << 4) + (-3 + 4), 2120, // "P-3"
    (16 << 4) + (-2 + 4), 1840, // "S-2"
    (17 << 4) + (-1 + 4), 1810, // "Cl-1"
    (32 << 4) + (-4 + 4), 2720, // "Ge-4"
    (33 << 4) + (-3 + 4), 2220, // "As-3"
    (34 << 4) + (-2 + 4), 1980, // "Se-2"  *Shannon (1976)
  //(34 << 4) + (-1 + 4), 2320, // "Se-1" *not in 77th
    (35 << 4) + (-1 + 4), 1960, // "Br-1"
    (50 << 4) + (-4 + 4), 2940, // "Sn-4"
    (50 << 4) + (-1 + 4), 3700, // "Sn-1"
    (51 << 4) + (-3 + 4), 2450, // "Sb-3"
    (52 << 4) + (-2 + 4), 2110, // "Te-2"
    (52 << 4) + (-1 + 4), 2500, // "Te-1"
    (53 << 4) + (-1 + 4), 2200, // "I-1"
  };

  private final static BitSet bsCations = new BitSet();
  private final static BitSet bsAnions = new BitSet();

  static {
    for (int i = 0; i < anionLookupTable.length; i+=2)
      bsAnions.set(anionLookupTable[i]>>4);
    for (int i = 0; i < cationLookupTable.length; i+=2)
      bsCations.set(cationLookupTable[i]>>4);
  }

  public static float getBondingRadiusFloat(short atomicNumberAndIsotope, int charge) {
    int atomicNumber = getElementNumber(atomicNumberAndIsotope);
    if (charge > 0 && bsCations.get(atomicNumber))
      return getBondingRadiusFloat(atomicNumber, charge, cationLookupTable);
    if (charge < 0 && bsAnions.get(atomicNumber))
      return getBondingRadiusFloat(atomicNumber, charge, anionLookupTable);
    return covalentMars[atomicNumber] / 1000f;
  }

  public static float getBondingRadiusFloat(int atomicNumber, int charge, short[] table) {
    // when found, return the corresponding value in ionicMars
    // if atom is not found, just return covalent radius
    // if atom is found, but charge is not found, return next lower charge
    short ionic = (short) ((atomicNumber << 4) + (charge + 4)); 
    int iVal = 0, iMid = 0, iMin = 0, iMax = table.length / 2;
    while (iMin != iMax) {
      iMid = (iMin + iMax) / 2;
      iVal = table[iMid<<1];
      if (iVal > ionic)
        iMax = iMid;
      else if (iVal < ionic)
        iMin = iMid + 1;
      else
        return table[(iMid << 1) + 1] / 1000f;
    }
    // find closest with same element and charge <= this charge
    if (iVal > ionic) 
      iMid--; // overshot
    iVal = table[iMid << 1];
    if (atomicNumber != (iVal >> 4)) 
      iMid++; // must be same element and not a negative charge;
    return table[(iMid << 1) + 1] / 1000f;
  }

  public static int getVanderwaalsMar(int i, EnumVdw type) {
    return vanderwaalsMars[(i << 2) + (type.pt % 4)];
  }

  public static float getHydrophobicity(int i) {
    return (i < 1 || i >= Elements.hydrophobicities.length ? 0 : Elements.hydrophobicities[i]);
  }


  private final static float[] hydrophobicities = {
                0f,
      /* Ala*/  0.62f,
      /* Arg*/ -2.53f,
      /* Asn*/ -0.78f,
      /* Asp*/ -0.90f,
      /* Cys*/  0.29f,
      /* Gln*/ -0.85f,
      /* Glu*/ -0.74f,
      /* Gly*/  0.48f,
      /* His*/ -0.40f,
      /* Ile*/  1.38f,
      /* Leu*/  1.06f,
      /* Lys*/ -1.50f,
      /* Met*/  0.64f,
      /* Phe*/  1.19f,
      /* Pro*/  0.12f,
      /* Ser*/ -0.18f,
      /* Thr*/ -0.05f,
      /* Trp*/  0.81f,
      /* Tyr*/  0.26f,
      /* Val*/  1.08f
  };


  static {
    // if the length of these tables is all the same then the
    // java compiler should eliminate all of this code.
    if ((elementNames.length != elementNumberMax) ||
        (vanderwaalsMars.length / 4 != elementNumberMax) ||
        (covalentMars.length  != elementNumberMax)) {
      Logger.error("ERROR!!! Element table length mismatch:" +
                         "\n elementSymbols.length=" + elementSymbols.length +
                         "\n elementNames.length=" + elementNames.length +
                         "\n vanderwaalsMars.length=" + vanderwaalsMars.length+
                         "\n covalentMars.length=" +
                         covalentMars.length);
    }
  }

  private static float[] electroNegativities = {
    // from http://chemwiki.ucdavis.edu/Physical_Chemistry/Physical_Properties_of_Matter/Atomic_and_Molecular_Properties/Allred-Rochow_Electronegativity
    0,
    2.2f,//H
    0,//He
    0.97f,//Li
    1.47f,//Be
    2.01f,//B
    2.5f,//C
    3.07f,//N
    3.5f,//O
    4.1f,//F
    0f,//
    1.01f,//Na
    1.23f,//Mg
    1.47f,//Al
    1.74f,//Si
    2.06f,//P
    2.44f,//S
    2.83f,//Cl
    0f,//
    0.91f,//K
    1.04f,//Ca
    1.2f,//Sc
    1.32f,//Ti
    1.45f,//V
    1.56f,//Cr
    1.6f,//Mn
    1.64f,//Fe
    1.7f,//Co
    1.75f,//Ni
    1.75f,//Cu
    1.66f,//Zn
    1.82f,//Ga
    2.02f,//Ge
    2.2f,//As
    2.48f,//Se
    2.74f,//Br
    0f,//
    0.89f,//Rb
    0.99f,//Sr
    1.11f,//Y
    1.22f,//Zr
    1.23f,//Nb
    1.3f,//Mo
    1.36f,//Te
    1.42f,//Ru
    1.45f,//Rh
    1.35f,//Pd
    1.42f,//Ag
    1.46f,//Cd
    1.49f,//In
    1.72f,//Sn
    1.82f,//Sb
    2.01f,//Te
    2.21f//I    
  };
  public static float getAllredRochowElectroNeg(int elemno) {
    return (elemno > 0 && elemno < electroNegativities.length ? electroNegativities[elemno] : 0);
  }

}
