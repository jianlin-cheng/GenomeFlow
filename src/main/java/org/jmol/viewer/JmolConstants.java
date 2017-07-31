/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-09-03 22:08:29 -0500 (Mon, 03 Sep 2012) $
 * $Revision: 17505 $

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
package org.jmol.viewer;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.vecmath.Vector3f;

import org.jmol.script.Token;
import org.jmol.util.Elements;
import org.jmol.util.J2SRequireImport;
import org.jmol.util.Logger;
import org.jmol.util.Parser;

@J2SRequireImport({java.util.Properties.class,java.io.BufferedInputStream.class,java.lang.StringBuffer.class})
public class JmolConstants {

  // axes mode constants --> org.jmol.constant.EnumAxesMode
  // callback constants --> org.jmol.constant.EnumCallback
  // draw constants --> org.jmol.shapespecial.draw.EnumCallback
  
  public final static String copyright = "(C) 2012 Jmol Development";
  public final static String version;
  public final static String date;

  static {
    String tmpVersion = null;
    String tmpDate = null;
    Properties props = new Properties();

    // Reading version from resource   inside jar
    BufferedInputStream bis = null;
    InputStream is = null;
    try {
      is = JmolConstants.class.getClassLoader().getResourceAsStream(
          "org/jmol/viewer/Jmol.properties");
      bis = new BufferedInputStream(is);
      props.load(bis);
      tmpVersion = props.getProperty("___version", tmpVersion);
      tmpDate = props.getProperty("___date", tmpDate);
      if (tmpDate != null) {
        tmpDate = tmpDate.substring(7, 23);
        // NOTE : date is update in the properties by SVN, and is in the format
        // $Date: 2012-09-03 22:08:29 -0500 (Mon, 03 Sep 2012) $"
      }
    } catch (Exception e) {
      // Nothing to do
    } finally {
      if (bis != null) {
        try {
          bis.close();
        } catch (Exception e) {
          // Nothing to do
        }
      }
      if (is != null) {
        try {
          is.close();
        } catch (Exception e) {
          // Nothing to do
        }
      }
    }
    version = (tmpVersion != null ? tmpVersion : "(Unknown version)");
    date = (tmpDate != null ? tmpDate : "(Unknown date)");
  }

  public final static boolean officialRelease = false;

  public final static String CLASSBASE_OPTIONS = "org.jmol.";

  public final static String DEFAULT_HELP_PATH = "http://chemapps.stolaf.edu/jmol/docs/index.htm";

  public final static String EMBEDDED_SCRIPT_TAG = "**** Jmol Embedded Script ****";

  public static final String NOTE_SCRIPT_FILE = "NOTE: file recognized as a script file: ";

  public static String embedScript(String s) {
    return "\n/**" + EMBEDDED_SCRIPT_TAG + " \n" + s + "\n**/";
  }
  
  public static final String SCRIPT_EDITOR_IGNORE = "\1## EDITOR_IGNORE ##";

  public static final String LOAD_ATOM_DATA_TYPES = "xyz;vxyz;vibration;temperature;occupancy;partialcharge";

  public final static float radiansPerDegree = (float) (Math.PI / 180);

  public static final String allowedQuaternionFrames = "RC;RP;a;b;c;n;p;q;x;";

  //note: Eval.write() processing requires drivers to be first-letter-capitalized.
  //do not capitalize any other letter in the word. Separate by semicolon.
  public static final String EXPORT_DRIVER_LIST = "Idtf;Js;Maya;Povray;Vrml;X3d;Tachyon;Obj"; 

  public final static Vector3f center = new Vector3f(0, 0, 0);
  public final static Vector3f axisX = new Vector3f(1, 0, 0);
  public final static Vector3f axisY = new Vector3f(0, 1, 0);
  public final static Vector3f axisZ = new Vector3f(0, 0, 1);
  public final static Vector3f axisNX = new Vector3f(-1, 0, 0);
  public final static Vector3f axisNY = new Vector3f(0, -1, 0);
  public final static Vector3f axisNZ = new Vector3f(0, 0, -1);
  public final static Vector3f[] unitAxisVectors = {
    axisX, axisY, axisZ, axisNX, axisNY, axisNZ };

  public final static int XY_ZTOP = 100; // Z value for [x y] positioned echos and axis origin
  public final static int DEFAULT_PERCENT_VDW_ATOM = 23; // matches C sizes of AUTO with 20 for Jmol set
  public final static float DEFAULT_BOND_RADIUS = 0.15f;
  public final static short DEFAULT_BOND_MILLIANGSTROM_RADIUS = (short) (DEFAULT_BOND_RADIUS * 1000);
  public final static float DEFAULT_STRUT_RADIUS = 0.3f;
  //angstroms of slop ... from OpenBabel ... mth 2003 05 26
  public final static float DEFAULT_BOND_TOLERANCE = 0.45f;
  //minimum acceptable bonding distance ... from OpenBabel ... mth 2003 05 26
  public final static float DEFAULT_MIN_BOND_DISTANCE = 0.4f;
  public final static float DEFAULT_MAX_CONNECT_DISTANCE = 100000000f;
  public final static float DEFAULT_MIN_CONNECT_DISTANCE = 0.1f;
  public static final int MINIMIZATION_ATOM_MAX = 200;
  public static final float MINIMIZE_FIXED_RANGE = 5.0f;

  public static final int MOUSE_NONE = -1;

  public final static byte MULTIBOND_NEVER =     0;
  public final static byte MULTIBOND_WIREFRAME = 1;
  public final static byte MULTIBOND_NOTSMALL =  2;
  public final static byte MULTIBOND_ALWAYS =    3;

  // maximum number of bonds that an atom can have when
  // autoBonding
  // All bonding is done by distances
  // this is only here for truly pathological cases
  public final static int MAXIMUM_AUTO_BOND_COUNT = 20;
  
  public final static short madMultipleBondSmallMaximum = 500;

  /* .cube files need this */
  public final static float ANGSTROMS_PER_BOHR = 0.5291772f;

  public final static int[] altArgbsCpk = {
    0xFFFF1493, // Xx 0
    0xFFBFA6A6, // Al 13
    0xFFFFFF30, // S  16
    0xFF57178F, // Cs 55
    0xFFFFFFC0, // D 2H
    0xFFFFFFA0, // T 3H
    0xFFD8D8D8, // 11C  6 - lighter
    0xFF505050, // 13C  6 - darker
    0xFF404040, // 14C  6 - darker still
    0xFF105050, // 15N  7 - darker
  };
  
  public final static int[] argbsAmino = {
    0xFFBEA06E, // default tan
    // note that these are the rasmol colors and names, not xwindows
    0xFFC8C8C8, // darkGrey   ALA
    0xFF145AFF, // blue       ARG
    0xFF00DCDC, // cyan       ASN
    0xFFE60A0A, // brightRed  ASP
    0xFFE6E600, // yellow     CYS
    0xFF00DCDC, // cyan       GLN
    0xFFE60A0A, // brightRed  GLU
    0xFFEBEBEB, // lightGrey  GLY
    0xFF8282D2, // paleBlue   HIS
    0xFF0F820F, // green      ILE
    0xFF0F820F, // green      LEU
    0xFF145AFF, // blue       LYS
    0xFFE6E600, // yellow     MET
    0xFF3232AA, // midBlue    PHE
    0xFFDC9682, // mauve      PRO
    0xFFFA9600, // orange     SER
    0xFFFA9600, // orange     THR
    0xFFB45AB4, // purple     TRP
    0xFF3232AA, // midBlue    TYR
    0xFF0F820F, // green      VAL

    0xFFFF69B4, // pick a new color ASP/ASN ambiguous
    0xFFFF69B4, // pick a new color GLU/GLN ambiguous
    0xFFBEA06E, // default tan UNK
  };

  // hmmm ... what is shapely backbone? seems interesting
  public final static int argbShapelyBackbone = 0xFFB8B8B8;
  public final static int argbShapelySpecial =  0xFF5E005E;
  public final static int argbShapelyDefault =  0xFFFF00FF;

  /**
   * colors used for chains
   *
   */

  /****************************************************************
   * some pastel colors
   * 
   * C0D0FF - pastel blue
   * B0FFB0 - pastel green
   * B0FFFF - pastel cyan
   * FFC0C8 - pink
   * FFC0FF - pastel magenta
   * FFFF80 - pastel yellow
   * FFDEAD - navajowhite
   * FFD070 - pastel gold

   * FF9898 - light coral
   * B4E444 - light yellow-green
   * C0C000 - light olive
   * FF8060 - light tomato
   * 00FF7F - springgreen
   * 
cpk on; select atomno>100; label %i; color chain; select selected & hetero; cpk off
   ****************************************************************/

  public final static int[] argbsChainAtom = {
    // ' '->0 'A'->1, 'B'->2
    0xFFffffff, // ' ' & '0' white
    //
    0xFFC0D0FF, // skyblue
    0xFFB0FFB0, // pastel green
    0xFFFFC0C8, // pink
    0xFFFFFF80, // pastel yellow
    0xFFFFC0FF, // pastel magenta
    0xFFB0F0F0, // pastel cyan
    0xFFFFD070, // pastel gold
    0xFFF08080, // lightcoral

    0xFFF5DEB3, // wheat
    0xFF00BFFF, // deepskyblue
    0xFFCD5C5C, // indianred
    0xFF66CDAA, // mediumaquamarine
    0xFF9ACD32, // yellowgreen
    0xFFEE82EE, // violet
    0xFF00CED1, // darkturquoise
    0xFF00FF7F, // springgreen
    0xFF3CB371, // mediumseagreen

    0xFF00008B, // darkblue
    0xFFBDB76B, // darkkhaki
    0xFF006400, // darkgreen
    0xFF800000, // maroon
    0xFF808000, // olive
    0xFF800080, // purple
    0xFF008080, // teal
    0xFFB8860B, // darkgoldenrod
    0xFFB22222, // firebrick
  };

  public final static int[] argbsChainHetero = {
    // ' '->0 'A'->1, 'B'->2
    0xFFffffff, // ' ' & '0' white
    //
    0xFFC0D0FF - 0x00303030, // skyblue
    0xFFB0FFB0 - 0x00303018, // pastel green
    0xFFFFC0C8 - 0x00303018, // pink
    0xFFFFFF80 - 0x00303010, // pastel yellow
    0xFFFFC0FF - 0x00303030, // pastel magenta
    0xFFB0F0F0 - 0x00303030, // pastel cyan
    0xFFFFD070 - 0x00303010, // pastel gold
    0xFFF08080 - 0x00303010, // lightcoral

    0xFFF5DEB3 - 0x00303030, // wheat
    0xFF00BFFF - 0x00001830, // deepskyblue
    0xFFCD5C5C - 0x00181010, // indianred
    0xFF66CDAA - 0x00101818, // mediumaquamarine
    0xFF9ACD32 - 0x00101808, // yellowgreen
    0xFFEE82EE - 0x00301030, // violet
    0xFF00CED1 - 0x00001830, // darkturquoise
    0xFF00FF7F - 0x00003010, // springgreen
    0xFF3CB371 - 0x00081810, // mediumseagreen

    0xFF00008B + 0x00000030, // darkblue
    0xFFBDB76B - 0x00181810, // darkkhaki
    0xFF006400 + 0x00003000, // darkgreen
    0xFF800000 + 0x00300000, // maroon
    0xFF808000 + 0x00303000, // olive
    0xFF800080 + 0x00300030, // purple
    0xFF008080 + 0x00003030, // teal
    0xFFB8860B + 0x00303008, // darkgoldenrod
    0xFFB22222 + 0x00101010, // firebrick
  };

  public final static short FORMAL_CHARGE_COLIX_RED =
    (short)Elements.elementSymbols.length;
  public final static short FORMAL_CHARGE_COLIX_WHITE =
    (short)(FORMAL_CHARGE_COLIX_RED + 4);
  public final static short FORMAL_CHARGE_COLIX_BLUE =
    (short)(FORMAL_CHARGE_COLIX_WHITE + 7);
  public final static int FORMAL_CHARGE_RANGE_SIZE = 12;

  public final static int[] argbsFormalCharge = {
    0xFFFF0000, // -4
    0xFFFF4040, // -3
    0xFFFF8080, // -2
    0xFFFFC0C0, // -1
    0xFFFFFFFF, // 0
    0xFFD8D8FF, // 1
    0xFFB4B4FF, // 2
    0xFF9090FF, // 3
    0xFF6C6CFF, // 4
    0xFF4848FF, // 5
    0xFF2424FF, // 6
    0xFF0000FF, // 7
  };

  public final static int FORMAL_CHARGE_INDEX_WHITE = 4;
  public final static int FORMAL_CHARGE_INDEX_MAX = argbsFormalCharge.length;

  public final static short PARTIAL_CHARGE_COLIX_RED =
    (short)(FORMAL_CHARGE_COLIX_BLUE + 1);
  public final static short PARTIAL_CHARGE_COLIX_WHITE =
    (short)(PARTIAL_CHARGE_COLIX_RED + 15);
  public final static short PARTIAL_CHARGE_COLIX_BLUE =
    (short)(PARTIAL_CHARGE_COLIX_WHITE + 15);
  public final static int PARTIAL_CHARGE_RANGE_SIZE = 31;

  public final static int[] argbsRwbScale = {
    0xFFFF0000, // red
    0xFFFF1010, //
    0xFFFF2020, //
    0xFFFF3030, //
    0xFFFF4040, //
    0xFFFF5050, //
    0xFFFF6060, //
    0xFFFF7070, //
    0xFFFF8080, //
    0xFFFF9090, //
    0xFFFFA0A0, //
    0xFFFFB0B0, //
    0xFFFFC0C0, //
    0xFFFFD0D0, //
    0xFFFFE0E0, //
    0xFFFFFFFF, // white
    0xFFE0E0FF, //
    0xFFD0D0FF, //
    0xFFC0C0FF, //
    0xFFB0B0FF, //
    0xFFA0A0FF, //
    0xFF9090FF, //
    0xFF8080FF, //
    0xFF7070FF, //
    0xFF6060FF, //
    0xFF5050FF, //
    0xFF4040FF, //
    0xFF3030FF, //
    0xFF2020FF, //
    0xFF1010FF, //
    0xFF0000FF, // blue
  };

  public final static int[] argbsRoygbScale = {
    // must be multiple of THREE for high/low
    0xFFFF0000,
    0xFFFF2000,
    0xFFFF4000,
    0xFFFF6000,
    0xFFFF8000,
    0xFFFFA000,
    0xFFFFC000,
    0xFFFFE000,

    0xFFFFF000, // yellow gets compressed, so give it an extra boost

    0xFFFFFF00,
    0xFFF0F000, // yellow gets compressed, so give it a little boost
    0xFFE0FF00,
    0xFFC0FF00,
    0xFFA0FF00,
    0xFF80FF00,
    0xFF60FF00,
    0xFF40FF00,
    0xFF20FF00,

    0xFF00FF00,
    0xFF00FF20,
    0xFF00FF40,
    0xFF00FF60,
    0xFF00FF80,
    0xFF00FFA0,
    0xFF00FFC0,
    0xFF00FFE0,

    0xFF00FFFF,
    0xFF00E0FF,
    0xFF00C0FF,
    0xFF00A0FF,
    0xFF0080FF,
    0xFF0060FF,
    0xFF0040FF,
    0xFF0020FF,

    0xFF0000FF,
  };

  // positive and negative default colors used for
  // isosurface rendering of .cube files
  // multiple colors removed -- RMH 3/2008 11.1.28
  
  public final static int argbsIsosurfacePositive = 0xFF5020A0;
  public final static int argbsIsosurfaceNegative = 0xFFA02050;

  private final static String[] specialAtomNames = {
    
    ////////////////////////////////////////////////////////////////
    // The ordering of these entries can be changed ... BUT ...
    // the offsets must be kept consistent with the ATOMID definitions
    // below.
    //
    // Used in Atom to look up special atoms. Any "*" in a PDB entry is
    // changed to ' for comparison here
    // 
    // null is entry 0
    // The first 32 entries are reserved for null + 31 'distinguishing atoms'
    // see definitions below. 32 is magical because bits are used in an
    // int to distinguish groups. If we need more then we can go to 64
    // bits by using a long ... but code must change. See Resolver.java
    //
    // All entries from 64 on are backbone entries
    ////////////////////////////////////////////////////////////////
    null, // 0
    
    // protein backbone
    //
    "N",   //  1 - amino nitrogen        SPINE
    "CA",  //  2 - alpha carbon          SPINE
    "C",   //  3 - carbonyl carbon       SPINE
    "O",   //  4 - carbonyl oxygen
    "O1",  //  5 - carbonyl oxygen in some protein residues (4THN)

    // nucleic acid backbone sugar
    //
    "O5'", //  6 - sugar 5' oxygen       SPINE
    "C5'", //  7 - sugar 5' carbon       SPINE
    "C4'", //  8 - sugar ring 4' carbon  SPINE
    "C3'", //  9 - sugar ring 3' carbon  SPINE
    "O3'", // 10 - sugar 3' oxygen
    "C2'", // 11 - sugar ring 2' carbon
    "C1'", // 12 - sugar ring 1' carbon
    // Phosphorus is not required for a nucleic group because
    // at the terminus it could have H5T or O5T ...
    "P",   // 13 - phosphate phosphorus  SPINE

    // END OF FIRST BACKBONE SET
    
    // ... But we need to distinguish phosphorus separately because
    // it could be found in phosphorus-only nucleic polymers
 
    "OD1",   // 14  ASP/ASN carbonyl/carbonate
    "OD2",   // 15  ASP carbonyl/carbonate
    "OE1",   // 16  GLU/GLN carbonyl/carbonate
    "OE2",   // 17  GLU carbonyl/carbonate
    "SG",    // 18  CYS sulfur
    // reserved for future expansion ... lipids & carbohydrates
    // 9/2006 -- carbohydrates are just handled as group3 codes
    // see below
    null, // 18 - 19
    null, null, null, null, // 20 - 23
    null, null, null, null, // 24 - 27
    null, null, null, null, // 28 - 31

    // nucleic acid bases
    //
    "N1",   // 32
    "C2",   // 33
    "N3",   // 34
    "C4",   // 35
    "C5",   // 36
    "C6",   // 37 -- currently defined as the nucleotide wing
            // this determines the vector for the sheet
            // could be changed if necessary

    // pyrimidine O2
    //
    "O2",   // 38

    // purine stuff
    //
    "N7",   // 39
    "C8",   // 40
    "N9",   // 41
    
    // nucleic acid base ring functional groups
    // DO NOT CHANGE THESE NUMBERS WITHOUT ALSO CHANGING
    // NUMBERS IN THE PREDEFINED SETS _a=...
    
    "N4",  // 42 - base ring N4, unique to C
    "N2",  // 43 - base amino N2, unique to G
    "N6",  // 44 - base amino N6, unique to A
    "C5M", // 45 - base methyl carbon, unique to T

    "O6",  // 46 - base carbonyl O6, only in G and I
    "O4",  // 47 - base carbonyl O4, only in T and U
    "S4",  // 48 - base thiol sulfur, unique to thio-U

    "C7", // 49 - base methyl carbon, unique to DT

    "H1",  // 50  - NOT backbone
    "H2",  // 51 - NOT backbone -- see 1jve
    "H3",  // 52 - NOT backbone
    null, null, //53
    null, null, null, null, null, //55
    null, null, null, null,       //60 - 63
    
    // everything from here on is backbone

    // protein backbone
    //
    "OXT", // 64 - second carbonyl oxygen, C-terminus only

    // protein backbone hydrogens
    //
    "H",   // 65 - amino hydrogen
    // these appear on the N-terminus end of 1ALE & 1LCD
    "1H",  // 66 - N-terminus hydrogen
    "2H",  // 67 - second N-terminus hydrogen
    "3H",  // 68 - third N-terminus hydrogen
    "HA",  // 69 - H on alpha carbon
    "1HA", // 70 - H on alpha carbon in Gly only
    "2HA", // 71 - 1ALE calls the two GLY hdrogens 1HA & 2HA

    // Terminal nuclic acid

    "H5T", // 72 - 5' terminus hydrogen which replaces P + O1P + O2P
    "O5T", // 73 - 5' terminus oxygen which replaces P + O1P + O2P
    "O1P", // 74 - first equivalent oxygen on phosphorus of phosphate
    "OP1", // 75 - first equivalent oxygen on phosphorus of phosphate -- new designation
    "O2P", // 76 - second equivalent oxygen on phosphorus of phosphate    
    "OP2", // 77 - second equivalent oxygen on phosphorus of phosphate -- new designation

    "O4'", // 78 - sugar ring 4' oxygen ... not present in +T ... maybe others
    "O2'", // 79 - sugar 2' oxygen, unique to RNA

    // nucleic acid backbone hydrogens
    //
    "1H5'", // 80 - first  equivalent H on sugar 5' carbon
    "2H5'", // 81 - second  equivalent H on sugar 5' carbon 
    "H4'",  // 82 - H on sugar ring 4' carbon
    "H3'",  // 83 - H on sugar ring 3' carbon
    "1H2'", // 84 - first equivalent H on sugar ring 2' carbon
    "2H2'", // 85 - second equivalent H on sugar ring 2' carbon
    "2HO'", // 86 - H on sugar 2' oxygen, unique to RNA 
    "H1'",  // 87 - H on sugar ring 1' carbon 
    "H3T",  // 88 - 3' terminus hydrogen    
        
    // add as many as necessary -- backbone only

    "HO3'", // 89 - 3' terminus hydrogen (new)
    "HO5'", // 90 - 5' terminus hydrogen (new)
    "HA2",
    "HA3",
    "HA2", 
    "H5'", 
    "H5''",
    "H2'",
    "H2''",
    "HO2'",

    "O3P", //    - third equivalent oxygen on phosphorus of phosphate    
    "OP3", //    - third equivalent oxygen on phosphorus of phosphate -- new designation
        
  };

  public final static String getSpecialAtomName(int atomID) {
    return specialAtomNames[atomID];
  }
  
  ////////////////////////////////////////////////////////////////
  // currently, ATOMIDs must be >= 0 && <= 127
  // if we need more then we can go to 255 by:
  //  1. applying 0xFF mask ... as in atom.specialAtomID & 0xFF;
  //  2. change the interesting atoms table to be shorts
  //     so that we can store negative numbers
  ////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////
  // keep this table in order to make it easier to maintain
  ////////////////////////////////////////////////////////////////

  public final static int ATOMID_MAX = specialAtomNames.length;
  // atomID 0 => nothing special, just an ordinary atom
  public final static byte ATOMID_AMINO_NITROGEN  = 1;
  public final static byte ATOMID_ALPHA_CARBON    = 2;
  public final static byte ATOMID_CARBONYL_CARBON = 3;
  public final static byte ATOMID_CARBONYL_OXYGEN = 4;
  public final static byte ATOMID_O1              = 5;
  
  // this is for groups that only contain an alpha carbon
  public final static int ATOMID_ALPHA_ONLY_MASK = 1 << ATOMID_ALPHA_CARBON;

  //this is entries 1 through 3 ... 3 bits ... N, CA, C
  public final static int ATOMID_PROTEIN_MASK =  0x7 << ATOMID_AMINO_NITROGEN;

  public final static byte ATOMID_O5_PRIME        = 6;
  public final static byte ATOMID_C4_PRIME        = 8;
  public final static byte ATOMID_C3_PRIME        = 9;
  public final static byte ATOMID_O3_PRIME        = 10;
  public final static byte ATOMID_C1_PRIME        = 12;
  
  // this is entries 6 through through 12 ... 7 bits
  public final static int ATOMID_NUCLEIC_MASK = 0x7F << ATOMID_O5_PRIME;

  public final static byte ATOMID_NUCLEIC_PHOSPHORUS = 13;
  
  // this is for nucleic groups that only contain a phosphorus
  public final static int ATOMID_PHOSPHORUS_ONLY_MASK =
    1 << ATOMID_NUCLEIC_PHOSPHORUS;

  // this can be increased as far as 32, but not higher.
  public final static int ATOMID_DISTINGUISHING_ATOM_MAX = 14;
  
  public final static byte ATOMID_CARBONYL_OD1 = 14;
  public final static byte ATOMID_CARBONYL_OD2 = 15;
  public final static byte ATOMID_CARBONYL_OE1 = 16;
  public final static byte ATOMID_CARBONYL_OE2 = 17;
  public final static byte ATOMID_SG = 18;
  
  public final static byte ATOMID_N1 = 32;
  public final static byte ATOMID_C2 = 33;
  public final static byte ATOMID_N3 = 34;
  public final static byte ATOMID_C4 = 35;
  public final static byte ATOMID_C5 = 36;
  public final static byte ATOMID_C6 = 37; // wing
  public final static byte ATOMID_O2 = 38;
  public final static byte ATOMID_N7 = 39;
  public final static byte ATOMID_C8 = 40;
  public final static byte ATOMID_N9 = 41;
  public final static byte ATOMID_N4 = 42;
  public final static byte ATOMID_N2 = 43;
  public final static byte ATOMID_N6 = 44;
  public final static byte ATOMID_C5M= 45;
  public final static byte ATOMID_O6 = 46;
  public final static byte ATOMID_O4 = 47;
  public final static byte ATOMID_S4 = 48;
  public final static byte ATOMID_C7 = 49;
  
  private final static int ATOMID_BACKBONE_MIN = 64;

  public final static byte ATOMID_TERMINATING_OXT = 64;
  public final static byte ATOMID_H5T_TERMINUS    = 72;
  public final static byte ATOMID_O5T_TERMINUS    = 73;
  public final static byte ATOMID_O1P             = 74;
  public final static byte ATOMID_OP1             = 75;
  public final static byte ATOMID_O2P             = 76;
  public final static byte ATOMID_OP2             = 77;
  public final static byte ATOMID_O2_PRIME        = 79;
  public final static byte ATOMID_H3T_TERMINUS    = 88;
  public final static byte ATOMID_HO3_PRIME       = 89;
  public final static byte ATOMID_HO5_PRIME       = 90;

  private static Map<String, Integer> htSpecialAtoms = new Hashtable<String, Integer>();
  static {
    for (int i = specialAtomNames.length; --i >= 0; ) {
      String specialAtomName = specialAtomNames[i];
      if (specialAtomName != null)
        htSpecialAtoms.put(specialAtomName,  Integer.valueOf(i));
    }
  }

  public static byte lookupSpecialAtomID(String atomName) {
    Integer boxedAtomID = htSpecialAtoms.get(atomName);
    if (boxedAtomID != null)
      return (byte) (boxedAtomID.intValue());
    return 0;
  }

  ////////////////////////////////////////////////////////////////
  // GROUP_ID related stuff for special groupIDs
  ////////////////////////////////////////////////////////////////
  
  public final static int GROUPID_ARGININE          = 2;
  public final static int GROUPID_ASPARAGINE        = 3;
  public final static int GROUPID_ASPARTATE         = 4;
  public final static int GROUPID_CYSTEINE          = 5;
  public final static int GROUPID_GLUTAMINE        =  6;
  public final static int GROUPID_GLUTAMATE        =  7;
  public final static int GROUPID_HISTIDINE        =  9;
  public final static int GROUPID_LYSINE           = 12;
  public final static int GROUPID_PROLINE          = 15;
  public final static int GROUPID_TRYPTOPHAN       = 19;
  public final static int GROUPID_AMINO_MAX        = 24;
  public final static int GROUPID_NUCLEIC_MAX      = 42;  
  private final static int GROUPID_WATER           = 42;
  private final static int GROUPID_SOLVENT_MIN     = 45; // urea only
  private final static int GROUPID_ION_MIN         = 46;
  private final static int GROUPID_ION_MAX         = 48;
  
  public final static String[] predefinedGroup3Names = {
    // taken from PDB spec
    "", //  0 this is the null group
    "ALA", // 1
    "ARG", // 2 arginine -- hbond donor
    "ASN", // 3 asparagine -- hbond donor
    "ASP", // 4 aspartate -- hbond acceptor
    "CYS",
    "GLN", // 6 glutamine -- hbond donor
    "GLU", // 7 glutamate -- hbond acceptor
    "GLY",
    "HIS", // 9 histidine -- hbond ambiguous
    "ILE",
    "LEU",
    "LYS", // 12 lysine -- hbond donor
    "MET",
    "PHE",
    "PRO", // 15 proline -- no NH
    "SER",
    "THR",
    "TRP",
    "TYR", // 19 tryptophan -- hbond donor
    "VAL",
    "ASX", // 21 ASP/ASN ambiguous
    "GLX", // 22 GLU/GLN ambiguous
    "UNK", // 23 unknown -- 23

    // if you change these numbers you *must* update
    // the predefined sets below

    // with the deprecation of +X, we will need a new
    // way to handle these. 
    
    "G", // 24 starts nucleics 
    "C", 
    "A",
    "T", 
    "U", 
    "I", 
    
    "DG", // 30 
    "DC",
    "DA",
    "DT",
    "DU",
    "DI",
    
    "+G", // 36
    "+C",
    "+A",
    "+T",
    "+U",
    "+I",
    /* removed bh 7/1/2011 this is isolated inosine, not a polymer "NOS", // inosine */
    
    // solvent types: -- if these numbers change, also change GROUPID_WATER,_SOLVENT,and_SULFATE
    
    "HOH", // 42 water
    "DOD", // 43
    "WAT", // 44
    "UREA",// 45 urea, a cosolvent
    "PO4", // 46 phosphate ions  -- from here on is "ligand"
    "SO4", // 47 sulphate ions

  };
  
  /**
   * returns an array if we have special hybridization or charge
   * 
   * @param res
   * @param name
   * @param ret
   *        [0] (target valence) may be reduced by one for sp2 for C or O only
   *        [1] will be set to 1 if positive (lysine or terminal N) or -1 if negative (OXT)
   *        [2] will be set to 2 if sp2 
   *        [3] is supplied covalent bond count
   * @return true for special; false if not
   */
  public static boolean getAminoAcidValenceAndCharge(String res, String name,
                                                     int[] ret) {
    if (res == null || res.length() == 0 || res.length() > 3 || name.equals("CA")
        || name.equals("CB"))
      return false;
    char ch0 = name.charAt(0);
    char ch1 = (name.length() == 1 ? 0 : name.charAt(1));
    boolean isSp2 = false;
    int bondCount = ret[3];
    switch (res.length()) {
    case 3:
      // protein, but also carbohydrate?
      if (name.length() == 1) {
        switch (ch0) {
        case 'N':
          // terminal N?
          if (bondCount > 1)
            return false;
          ret[1] = 1;
          break;
        case 'O':
          isSp2 = ("HOH;DOD;WAT".indexOf(res) < 0);
          break;
        default:
          isSp2 = true;
        }
      } else {
        String id = res + ch0;
        isSp2 = (aaSp2.indexOf(id) >= 0);
        if (aaPlus.indexOf(id) >= 0) {
          // LYS N is 1+
          ret[1] = 1;
        } else if (ch0 == 'O' && ch1 == 'X') {
          // terminal O is 1-
          ret[1] = -1;
        }
      }
      break;
    case 1:
    case 2:
      // dna/rna
      if (name.length() > 2 && name.charAt(2) == '\'')
        return false;
      switch (ch0) {
      case 'C':
        if (ch1 == '7') // T CH3
          return false;
        break;
      case 'N':
        switch (ch1) {
        case '1':
        case '3':
          if (naNoH.indexOf("" + res.charAt(res.length() - 1) + ch1) >= 0)
            ret[0]--;
          break;
        case '7':
          ret[0]--;
          break;
        }
        break;
      }
      isSp2 = true;
    }
    if (isSp2) {
      switch (ch0) {
      case 'N':
        ret[2] = 2;
        break;
      case 'C':
        ret[2] = 2;
        ret[0]--;
        break;
      case 'O':
        ret[0]--;
        break;
      }
    }
    return true;
  }
  private final static String naNoH = 
  		"A3;A1;C3;G3;I3";
  
  private final static String aaSp2 = 
    "ARGN;ASNN;ASNO;ASPO;" +
    "GLNN;GLNO;GLUO;" +
    "HISN;HISC;PHEC" +
    "TRPC;TRPN;TYRC";
  
  private final static String aaPlus = 
    "LYSN";

  public static int getStandardPdbHydrogenCount(int pt) {
    return (pt < 0 || pt >= pdbHydrogenCount.length ? -1 : pdbHydrogenCount[pt]);
  }

  public static String[][] getPdbBondInfo(int pt) {
    if (pt < 0 || pt > pdbBondInfo.length)
      return null;
    String s = pdbBondInfo[pt];
    String[] temp = Parser.getTokens(s);
    String[][] info = new String[temp.length / 2][];
    for (int i = 0, p = 0; i < info.length; i++) {
      String source = temp[p++];
      String target = temp[p++];
      // a few shortcuts here:
      if (target.length() == 1)
        switch (target.charAt(0)) {
        case 'N':
          target = "H@H2";
          break;
        case 'B': // CB
          target = "HB3@HB2";
          break;
        case 'D': // CD
          target = "HD2@HD3";
          break;
        case 'G': // CG
          target = "HG3@HG2";
          break;
        case '2': // C2'
          target = "H2''@H2'";
          break;
        case '5': // C5'
          target = "H5''@H5'";
          break;
        }
      if (target.charAt(0) != 'H' && source.compareTo(target) > 0) {
        s = target;
        target = source;
        source = s;
      }

      info[i] = new String[] { source, target,
          (target.startsWith("H") ? "1" : "2") };
    }
    return info;
  }
  
  private final static String[] pdbBondInfo = {
    "",
    /*ALA*/ "N N CA HA C O CB HB?",
    /*ARG*/ "N N CA HA C O CB HB2@HB3 CG HG2@HG3 CD D NE HE CZ NH1 NH1 HH11@HH12 NH2 HH21@HH22", 
    /*ASN*/ "N N CA HA C O CB B CG OD1 ND2 HD21@HD22", 
    /*ASP*/ "N N CA HA C O CB B CG OD1", 
    /*CYS*/ "N N CA HA C O CB B SG HG", 
    /*GLN*/ "N N CA HA C O CB B CG G CD OE1 NE2 HE21@HE22", 
    /*GLU*/ "N N CA HA C O CB B CG G CD OE1", 
    /*GLY*/ "N N CA HA2@HA3 C O", 
    /*HIS*/ "N N CA HA C O CB B CG CD2 ND1 CE1 ND1 HD1 CD2 HD2 CE1 HE1 NE2 HE2", 
    /*ILE*/ "N N CA HA C O CB HB CG1 HG12@HG13 CG2 HG2? CD1 HD1?", 
    /*LEU*/ "N N CA HA C O CB HB2@HB3 CG HG CD1 HD1? CD2 HD2?", 
    /*LYS*/ "N N CA HA C O CB B CG G CD HD2@HD3 CE HE3@HE2 NZ HZ?", 
    /*MET*/ "N N CA HA C O CB HB2@HB3 CG HG2@HG3 CE HE?", 
    /*PHE*/ "N N CA HA C O CB B CG CD1 CD1 HD1 CD2 CE2 CD2 HD2 CE1 CZ CE1 HE1 CE2 HE2 CZ HZ", 
    /*PRO*/ "N H CA HA C O CB B CG G CD HD2@HD3", 
    /*SER*/ "N N CA HA C O CB B OG HG", 
    /*THR*/ "N N CA HA C O CB HB OG1 HG1 CG2 HG2?", 
    /*TRP*/ "N N CA HA C O CB B CG CD1 CD1 HD1 CD2 CE2 NE1 HE1 CE3 CZ3 CE3 HE3 CZ2 CH2 CZ2 HZ2 CZ3 HZ3 CH2 HH2", 
    /*TYR*/ "N N CA HA C O CB B CG CD1 CD1 HD1 CD2 CE2 CD2 HD2 CE1 CZ CE1 HE1 CE2 HE2 OH HH", 
    /*VAL*/ "N N CA HA C O CB HB CG1 HG1? CG2 HG2?",
    /*ASX*/ "CA HA C O CB HB2@HB1 C H",
    /*GLX*/ "CA HA C O CB HB1 CB HB2 CG HG1 CG HG2", 
    /*UNK*/ "",
    /*G*/ "P OP1 C5' 5 C4' H4' C3' H3' C2' H2' O2' HO2' C1' H1' C8 N7 C8 H8 C5 C4 C6 O6 N1 H1 C2 N3 N2 H21@H22", 
    /*C*/ "P OP1 C5' 5 C4' H4' C3' H3' C2' H2' O2' HO2' C1' H1' C2 O2 N3 C4 N4 H41@H42 C5 C6 C5 H5 C6 H6", 
    /*A*/ "P OP1 C5' 5 C4' H4' C3' H3' C2' H2' O2' HO2' C1' H1' C8 N7 C8 H8 C5 C4 C6 N1 N6 H61@H62 C2 N3 C2 H2",
    /*T*/ "P OP1 C5' 5 C4' H4' C3' H3' C2' 2 C1' H1' C2 O2 N3 H3 C4 O4 C5 C6 C7 H7? C6 H6",
    /*U*/ "P OP1 C5' 5 C4' H4' C3' H3' C2' H2' O2' HO2' C1' H1' C2 O2 N3 H3 C4 O4 C5 C6 C5 H5 C6 H6", 
    /*I*/ "P OP1 C5' 5 C4' H4' C3' H3' C2' H2' O2' HO2' C1' H1' C8 N7 C8 H8 C5 C4 C6 O6 N1 H1 C2 N3 C2 H2",
    /*DG*/ "P OP1 C5' 5 C4' H4' C3' H3' C2' 2 C1' H1' C8 N7 C8 H8 C5 C4 C6 O6 N1 H1 C2 N3 N2 H21@H22", 
    /*DC*/ "P OP1 C5' 5 C4' H4' C3' H3' C2' 2 C1' H1' C2 O2 N3 C4 N4 H41@H42 C5 C6 C5 H5 C6 H6", 
    /*DA*/ "P OP1 C5' 5 C4' H4' C3' H3' C2' 2 C1' H1' C8 N7 C8 H8 C5 C4 C6 N1 N6 H61@H62 C2 N3 C2 H2", 
    /*DT*/ "P OP1 C5' H5'@H5'' C4' H4' C3' H3' C2' H2'@H2'' C1' H1' C2 O2 N3 H3 C4 O4 C5 C6 C7 H7? C6 H6",
    /*DU*/ "P OP1 C5' 5 C4' H4' C3' H3' C2' H2'@H2'' C1' H1' C2 O2 N3 H3 C4 O4 C5 C6 C5 H5 C6 H6",  
    /*DI*/ "P OP1 C5' 5 C4' H4' C3' H3' C2' 2 C1' H1' C8 N7 C8 H8 C5 C4 C6 O6 N1 H1 C2 N3 C2 H2",  
      };

  private final static int[] pdbHydrogenCount = {
            0,
    /*ALA*/ 6,
    /*ARG*/ 16,
    /*ASN*/ 7,
    /*ASP*/ 6,
    /*CYS*/ 6,
    /*GLN*/ 9,
    /*GLU*/ 8,
    /*GLY*/ 4,
    /*HIS*/ 9,
    /*ILE*/ 12,
    /*LEU*/ 12,
    /*LYS*/ 14,
    /*MET*/ 10,
    /*PHE*/ 10,
    /*PRO*/ 8,
    /*SER*/ 6,
    /*THR*/ 8,
    /*TRP*/ 11,
    /*TYR*/ 10,
    /*VAL*/ 10,  
    /*ASX*/ 3,
    /*GSX*/ 5,
    /*UNK*/ 0,
    /*G*/ 13,
    /*C*/ 13,
    /*A*/ 13,
    /*T*/ -1,
    /*U*/ 12,
    /*I*/ 12,
    /*DG*/ 13,
    /*DC*/ 13,
    /*DA*/ 13,
    /*DT*/ 14,
    /*DU*/ 12,
    /*DI*/ 12,
  };
  
  public final static int[] argbsShapely = {
    0xFFFF00FF, // default
    // these are rasmol values, not xwindows colors
    0xFF00007C, // ARG
    0xFFFF7C70, // ASN
    0xFF8CFF8C, // ALA
    0xFFA00042, // ASP
    0xFFFFFF70, // CYS
    0xFFFF4C4C, // GLN
    0xFF660000, // GLU
    0xFFFFFFFF, // GLY
    0xFF7070FF, // HIS
    0xFF004C00, // ILE
    0xFF455E45, // LEU
    0xFF4747B8, // LYS
    0xFF534C52, // PHE
    0xFFB8A042, // MET
    0xFF525252, // PRO
    0xFFFF7042, // SER
    0xFFB84C00, // THR
    0xFF4F4600, // TRP
    0xFF8C704C, // TYR
    0xFFFF8CFF, // VAL

    0xFFFF00FF, // ASX ASP/ASN ambiguous
    0xFFFF00FF, // GLX GLU/GLN ambiguous
    0xFFFF00FF, // UNK unknown -- 23

    0xFFFF7070, // G  
    0xFFFF8C4B, // C
    0xFFA0A0FF, // A
    0xFFA0FFA0, // T
    0xFFFF8080, // U miguel made up this color
    0xFF80FFFF, // I miguel made up this color

    0xFFFF7070, // DG
    0xFFFF8C4B, // DC
    0xFFA0A0FF, // DA
    0xFFA0FFA0, // DT
    0xFFFF8080, // DU
    0xFF80FFFF, // DI
    
    0xFFFF7070, // +G
    0xFFFF8C4B, // +C
    0xFFA0A0FF, // +A
    0xFFA0FFA0, // +T
    0xFFFF8080, // +U
    0xFF80FFFF, // +I

    // what to do about remediated +X names?
    // we will need a map
    
  };


  // this form is used for counting groups in ModelSet
  private final static String allCarbohydrates = 
    ",[AHR],[AMU],[ARA],[ARB],[BDF],[BDR],[BGC],[BMA]" +
    ",[FCA],[FCB],[FRU],[FUC],[FUL],[GAL],[GLA],[GLC]" +
    ",[GUP],[LXC],[MAN],[RAM],[RIB],[RIP],[XYP],[XYS]" +
    ",[CBI],[CT3],[CTR],[CTT],[LAT],[MAB],[MAL],[MLR],[MTT]" +
    ",[SUC],[TRE],[GCU],[MTL],[NAG],[NDG],[RHA],[SOR],[SOL],[SOE]" +  
    ",[XYL],[A2G],[LBT],[NGA],[SIA],[SLB]" + 
    ",[AFL],[AGC],[GLB],[NAN],[RAA]"; //these 4 are deprecated in PDB
    // from Eric Martz; revision by Angel Herraez

  /**
   * @param group3 a potential group3 name
   * @return whether this is a carbohydrate from the list
   */
  public final static boolean checkCarbohydrate(String group3) {
    return (group3 != null 
        && allCarbohydrates.indexOf("[" + group3.toUpperCase() + "]") >= 0);
  }

  private static String getGroup3List() {
    StringBuffer s = new StringBuffer();
    //for menu presentation order
    for (int i = 1; i < GROUPID_WATER; i++)
      s.append(",[").append((predefinedGroup3Names[i]+"   ").substring(0,3)+"]");
    s.append(allCarbohydrates);
    return s.toString();
  }
  
  public final static boolean isHetero(String group3) {
    return getGroup3Pt(group3) >= GROUPID_WATER;
  }

  private static int getGroup3Pt(String group3) {
    StringBuffer sb = new StringBuffer("[");
    sb.append(group3);
    switch (group3.length()){
    case 1:
      sb.append("  ");
      break;
    case 2:
      sb.append(" ");
      break;
    }
    int pt = group3List.indexOf(sb.toString());
    return (pt < 0 ? Integer.MAX_VALUE : pt / 6 + 1);
  }

  public final static String group3List = getGroup3List();
  public final static int group3Count = group3List.length() / 6;

  public final static char[] predefinedGroup1Names = {
    /* rmh
     * 
     * G   Glycine   Gly                   P   Proline   Pro
     * A   Alanine   Ala                   V   Valine    Val
     * L   Leucine   Leu                   I   Isoleucine    Ile
     * M   Methionine    Met               C   Cysteine    Cys
     * F   Phenylalanine   Phe             Y   Tyrosine    Tyr
     * W   Tryptophan    Trp               H   Histidine   His
     * K   Lysine    Lys                   R   Arginine    Arg
     * Q   Glutamine   Gln                 N   Asparagine    Asn
     * E   Glutamic Acid   Glu             D   Aspartic Acid   Asp
     * S   Serine    Ser                   T   Threonine   Thr
     */
    '\0', //  0 this is the null group
    
    'A', // 1
    'R',
    'N',
    'D',
    'C', // 5 Cysteine
    'Q',
    'E',
    'G',
    'H',
    'I',
    'L',
    'K',
    'M',
    'F',
    'P', // 15 Proline
    'S',
    'T',
    'W',
    'Y',
    'V',
    'A', // 21 ASP/ASN ambiguous
    'G', // 22 GLU/GLN ambiguous
    '?', // 23 unknown -- 23

    'G', // X nucleics
    'C',
    'A',
    'T',
    'U',
    'I',
    
    'G', // DX nucleics
    'C',
    'A',
    'T',
    'U',
    'I',
    
    'G', // +X nucleics
    'C',
    'A',
    'T',
    'U',
    'I',
    'I',
    };

  ////////////////////////////////////////////////////////////////
  // predefined sets
  ////////////////////////////////////////////////////////////////

  // these must be removed after various script commands so that they stay current
  
  public static String[] predefinedVariable = {
    //  
    // main isotope (variable because we can do {xxx}.element = n;
    //
    "@_1H _H & !(_2H,_3H)",
    "@_12C _C & !(_13C,_14C)",
    "@_14N _N & !(_15N)",

    //
    // solvent
    //
    "@water _g>=" + GROUPID_WATER + " & _g<" + GROUPID_SOLVENT_MIN
        + ", oxygen & connected(2) & connected(2, hydrogen), (hydrogen) & connected(oxygen & connected(2) & connected(2, hydrogen))",
    "@solvent water, (_g>=" + GROUPID_SOLVENT_MIN + " & _g<" + GROUPID_ION_MAX + ")", // water, other solvent or ions
    "@ligand !(_g<"+ GROUPID_ION_MIN + ",protein,nucleic,water)",

    // structure
    "@turn structure=1",
    "@sheet structure=2",
    "@helix structure=3",
    "@helix310 substructure=7",
    "@helixalpha substructure=8",
    "@helixpi substructure=9",
    "@bonded bondcount>0",
  };
  
  // these are only updated once per file load or file append
  
  public static String[] predefinedStatic = {
    //
    // protein related
    //
    // protein is hardwired
    "@amino _g>0 & _g<=23",
    "@acidic asp,glu",
    "@basic arg,his,lys",
    "@charged acidic,basic",
    "@negative acidic",
    "@positive basic",
    "@neutral amino&!(acidic,basic)",
    "@polar amino&!hydrophobic",

    "@cyclic his,phe,pro,trp,tyr",
    "@acyclic amino&!cyclic",
    "@aliphatic ala,gly,ile,leu,val",
    "@aromatic his,phe,trp,tyr",
    "@cystine within(group, (cys.sg or cyx.sg) and connected(cys.sg or cyx.sg))",

    "@buried ala,cys,ile,leu,met,phe,trp,val",
    "@surface amino&!buried",

    // doc on hydrophobic is inconsistent
    // text description of hydrophobic says this
    //    "@hydrophobic ala,leu,val,ile,pro,phe,met,trp",
    // table says this
    "@hydrophobic ala,gly,ile,leu,met,phe,pro,trp,tyr,val",
    "@mainchain backbone",
    "@small ala,gly,ser",
    "@medium asn,asp,cys,pro,thr,val",
    "@large arg,glu,gln,his,ile,leu,lys,met,phe,trp,tyr",

    //
    // nucleic acid related

    // nucleic, dna, rna, purine, pyrimidine are hard-wired
    //
    "@c nucleic & ([C] or [DC] or within(group,_a="+ATOMID_N4+"))",
    "@g nucleic & ([G] or [DG] or within(group,_a="+ATOMID_N2+"))",
    "@cg c,g",
    "@a nucleic & ([A] or [DA] or within(group,_a="+ATOMID_N6+"))",
    "@t nucleic & ([T] or [DT] or within(group,_a="+ATOMID_C5M+" | _a="+ATOMID_C7+"))",
    "@at a,t",
    "@i nucleic & ([I] or [DI] or within(group,_a="+ATOMID_O6+") & !g)",
    "@u nucleic & ([U] or [DU] or within(group,_a="+ATOMID_O4+") & !t)",
    "@tu nucleic & within(group,_a="+ATOMID_S4+")",

    //
    // ions
    //
    "@ions _g>="+GROUPID_ION_MIN+"&_g<"+GROUPID_ION_MAX,

    //
    // structure related
    //
    "@alpha _a=2", // rasmol doc says "approximately *.CA" - whatever?
    "@backbone (protein,nucleic) & (_a>0 & _a<14 || _a>="+ATOMID_BACKBONE_MIN+") | _H & protein & connected(*.N)",
    "@spine protein & _a>0 & _a<= 3 || nucleic & (_a >= 6 & _a <= 10 || _a=" + ATOMID_NUCLEIC_PHOSPHORUS + ")",
    "@sidechain (protein,nucleic) & !backbone",
    "@base nucleic & !backbone",
    "@dynamic_flatring search('[a]')"

    //    "@hetero", handled specially

  };
  public final static String MODELKIT_ZAP_STRING = "1 0 C 0 0";
  public static final String MODELKIT_ZAP_TITLE = "Jmol Model Kit";//do not ever change this -- it is in the state
  public final static String ADD_HYDROGEN_TITLE = "Viewer.AddHydrogens"; //do not ever change this -- it is in the state

  ////////////////////////////////////////////////////////////////
  // font-related
  ////////////////////////////////////////////////////////////////

  public final static String DEFAULT_FONTFACE = "SansSerif";
  public final static String DEFAULT_FONTSTYLE = "Plain";

  public final static int LABEL_MINIMUM_FONTSIZE = 6;
  public final static int LABEL_MAXIMUM_FONTSIZE = 63;
  public final static int LABEL_DEFAULT_FONTSIZE = 13;
  public final static int LABEL_DEFAULT_X_OFFSET = 4;
  public final static int LABEL_DEFAULT_Y_OFFSET = 4;

  public final static int MEASURE_DEFAULT_FONTSIZE = 15;
  public final static int AXES_DEFAULT_FONTSIZE = 14;

  ////////////////////////////////////////////////////////////////
  // do not rearrange/modify these shapes without
  // updating the String[] shapeBaseClasses below &&
  // also creating a token for this shape in Token.java &&
  // also updating shapeToks to confirm consistent
  // conversion from tokens to shapes
  ////////////////////////////////////////////////////////////////

  public final static int SHAPE_BALLS      = 0;
  public final static int SHAPE_STICKS     = 1;
  public final static int SHAPE_HSTICKS    = 2;  //placeholder only; handled by SHAPE_STICKS
  public final static int SHAPE_SSSTICKS   = 3;  //placeholder only; handled by SHAPE_STICKS
  public final static int SHAPE_STRUTS     = 4;  //placeholder only; handled by SHAPE_STICKS
  public final static int SHAPE_LABELS     = 5;
  public final static int SHAPE_MEASURES   = 6;
  public final static int SHAPE_STARS      = 7;
  public final static int SHAPE_HALOS      = 8;

  public final static int SHAPE_MIN_SECONDARY = 9; //////////
  
    public final static int SHAPE_BACKBONE   = 9;
    public final static int SHAPE_TRACE      = 10;
    public final static int SHAPE_CARTOON    = 11;
    public final static int SHAPE_STRANDS    = 12;
    public final static int SHAPE_MESHRIBBON = 13;
    public final static int SHAPE_RIBBONS    = 14;
    public final static int SHAPE_ROCKETS    = 15;
  
  public final static int SHAPE_MAX_SECONDARY = 16; //////////
  public final static int SHAPE_MIN_SPECIAL    = 16; //////////

    public final static int SHAPE_DOTS       = 16;
    public final static int SHAPE_DIPOLES    = 17;
    public final static int SHAPE_VECTORS    = 18;
    public final static int SHAPE_GEOSURFACE = 19;
    public final static int SHAPE_ELLIPSOIDS = 20;

  public final static int SHAPE_MAX_SIZE_ZERO_ON_RESTRICT = 21; //////////
  
    public final static int SHAPE_POLYHEDRA  = 21;  // for restrict, uses setProperty(), not setSize()

  public final static int SHAPE_MIN_HAS_ID          = 22; //////////
  public final static int SHAPE_MIN_MESH_COLLECTION = 22; //////////
  
    public final static int SHAPE_DRAW        = 22;
  
  public final static int SHAPE_MAX_SPECIAL = 23; //////////
  public final static int SHAPE_MIN_SURFACE = 23; //////////

    public final static int SHAPE_ISOSURFACE  = 23;
    public final static int SHAPE_CONTACT     = 24;
    public final static int SHAPE_LCAOCARTOON = 25;
    public final static int SHAPE_MO          = 26;  //but no ID for MO
    public final static int SHAPE_PMESH       = 27;
    public final static int SHAPE_PLOT3D      = 28;

  public final static int SHAPE_MAX_SURFACE         = 29; //////////
  public final static int SHAPE_MAX_MESH_COLLECTION = 29; //////////
  
    public final static int SHAPE_ECHO       = 29;
  
  public final static int SHAPE_MAX_HAS_ID = 30;
  
  public final static int SHAPE_AXES       = 30;
  public final static int SHAPE_BBCAGE     = 31;
  public final static int SHAPE_UCCAGE     = 32;
  public final static int SHAPE_HOVER      = 33;
  
  //added -hcf
  public final static int SHAPE_SCALES      = 34;
  //added end -hcf
  
  // last should be frank: - modified -hcf
  public final static int SHAPE_FRANK      = 35;

  public final static int SHAPE_MAX        = SHAPE_FRANK + 1;
  //modified end -hcf
  
  public final static boolean isShapeSecondary(int i ) {
    return i >= JmolConstants.SHAPE_MIN_SECONDARY && i < JmolConstants.SHAPE_MAX_SECONDARY;
  }
  
  // note that these next two arrays *MUST* be in the same sequence 
  // given in SHAPE_* and they must be capitalized exactly as in their class name 

  public final static String[] shapeClassBases = {
    "Balls", "Sticks", "Hsticks", "Sssticks", "Struts",
      //Hsticks, Sssticks, and Struts classes do not exist, but this returns Token for them
    "Labels", "Measures", "Stars", "Halos",
    "Backbone", "Trace", "Cartoon", "Strands", "MeshRibbon", "Ribbons", "Rockets", 
    "Dots", "Dipoles", "Vectors", "GeoSurface", "Ellipsoids", "Polyhedra", 
    "Draw", "Isosurface", "Contact", "LcaoCartoon", "MolecularOrbital", "Pmesh", "Plot3D", 
    "Echo", "Axes", "Bbcage", "Uccage", "Hover", 
    "Frank", 
    "Scales"//scales should be put in the last, don't know why - hcf
     };
  // .hbond and .ssbonds will return a class,
  // but the class is never loaded, so it is skipped in each case.
  // coloring and sizing of hydrogen bonds and S-S bonds is now
  // done by Sticks.

  public final static int shapeTokenIndex(int tok) {
    switch (tok) {
    case Token.atoms:
      return SHAPE_BALLS;
    case Token.bonds:
    case Token.wireframe:
      return SHAPE_STICKS;
    case Token.hbond:
      return SHAPE_HSTICKS;
    case Token.ssbond:
      return SHAPE_SSSTICKS;
    case Token.struts:
      return SHAPE_STRUTS;
    case Token.label:
      return SHAPE_LABELS;
    case Token.measure:
    case Token.measurements:
      return SHAPE_MEASURES;
    case Token.star:
      return SHAPE_STARS;
    case Token.halo:
      return SHAPE_HALOS;
    case Token.backbone:
      return SHAPE_BACKBONE;
    case Token.trace:
      return SHAPE_TRACE;
    case Token.cartoon:
      return SHAPE_CARTOON;
    case Token.strands:
      return SHAPE_STRANDS;
    case Token.meshRibbon:
      return SHAPE_MESHRIBBON;
    case Token.ribbon:
      return SHAPE_RIBBONS;
    case Token.rocket:
      return SHAPE_ROCKETS;
    case Token.dots:
      return SHAPE_DOTS;
    case Token.dipole:
      return SHAPE_DIPOLES;
    case Token.vector:
      return SHAPE_VECTORS;
    case Token.geosurface:
      return SHAPE_GEOSURFACE;
    case Token.ellipsoid:
      return SHAPE_ELLIPSOIDS;
    case Token.polyhedra:
      return SHAPE_POLYHEDRA;
    case Token.draw:
      return SHAPE_DRAW;
    case Token.isosurface:
      return SHAPE_ISOSURFACE;
    case Token.contact:
      return SHAPE_CONTACT;
    case Token.lcaocartoon:
      return SHAPE_LCAOCARTOON;
    case Token.mo:
      return SHAPE_MO;
    case Token.pmesh:
      return SHAPE_PMESH;
    case Token.plot3d:
      return SHAPE_PLOT3D;
    case Token.echo:
      return SHAPE_ECHO;
    case Token.axes:
      return SHAPE_AXES;
    case Token.boundbox:
      return SHAPE_BBCAGE;
    case Token.unitcell:
      return SHAPE_UCCAGE;
    case Token.hover:
      return SHAPE_HOVER;
    case Token.scales:
      return SHAPE_SCALES;  	  
    case Token.frank:
      return SHAPE_FRANK;
    }
    return -1;
  }
  
  public final static String getShapeClassName(int shapeID, boolean isRenderer) {
    if (shapeID < 0)
      return shapeClassBases[~shapeID];
    return CLASSBASE_OPTIONS + (isRenderer ? "render" : "shape") 
        + (shapeID >= SHAPE_MIN_SECONDARY && shapeID < SHAPE_MAX_SECONDARY 
            ? "bio."
        : shapeID >= SHAPE_MIN_SPECIAL && shapeID < SHAPE_MAX_SPECIAL 
            ? "special." 
        : shapeID >= SHAPE_MIN_SURFACE && shapeID < SHAPE_MAX_SURFACE 
            ? "surface." 
        : ".") + shapeClassBases[shapeID];
  }

  
  // this atom flag simply associates an atom with the current model
  // but doesn't necessarily mean it is visible

  public final static int ATOM_IN_FRAME    = 1;
  public final static int ATOM_SLABBED     = 2;  // reserved for future use
  
  
  public final static int CURSOR_DEFAULT = 0;
  public final static int CURSOR_HAND = 1;
  public final static int CURSOR_CROSSHAIR = 2;
  public final static int CURSOR_MOVE = 3;
  public final static int CURSOR_WAIT = 4;
  public final static int CURSOR_ZOOM = 5;

  public final static int getShapeVisibilityFlag(int shapeID) {
    return (4 << shapeID);
  }

  static {
    if (argbsFormalCharge.length != Elements.FORMAL_CHARGE_MAX
        - Elements.FORMAL_CHARGE_MIN + 1) {
      Logger.error("formal charge color table length");
      throw new NullPointerException();
    }
    if (shapeClassBases.length != SHAPE_MAX) {
      Logger.error("shapeClassBases wrong length");
      throw new NullPointerException();
    }
    if (argbsAmino.length != GROUPID_AMINO_MAX) {
      Logger.error("argbsAmino wrong length");
      throw new NullPointerException();
    }
    if (argbsShapely.length != GROUPID_WATER) {
      Logger.error("argbsShapely wrong length");
      throw new NullPointerException();
    }
    if (argbsChainHetero.length != argbsChainAtom.length) {
      Logger.error("argbsChainHetero wrong length");
      throw new NullPointerException();
    }
    if (shapeClassBases.length != SHAPE_MAX) {
      Logger.error("the shapeClassBases array has the wrong length");
      throw new NullPointerException();
    }
  }


  
}
