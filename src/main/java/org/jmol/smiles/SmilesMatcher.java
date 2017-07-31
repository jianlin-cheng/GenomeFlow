/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2007-04-26 16:57:51 -0500 (Thu, 26 Apr 2007) $
 * $Revision: 7502 $
 *
 * Copyright (C) 2005  The Jmol Development Team
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

package org.jmol.smiles;

import java.util.BitSet;
import java.util.List;

import org.jmol.api.SmilesMatcherInterface;
import org.jmol.util.BitSetUtil;
import org.jmol.util.JmolNode;
import org.jmol.util.TextFormat;

/**
 * Originating author: Nicholas Vervelle
 * 
 * A class to handle a variety of SMILES/SMARTS-related functions, including:
 *  -- determining if two SMILES strings are equivalent
 *  -- determining the molecular formula of a SMILES or SMARTS string
 *  -- searching for specific runs of atoms in a 3D model
 *  -- searching for specific runs of atoms in a SMILES description
 *  -- generating valid (though not canonical) SMILES and bioSMILES strings
 *  -- getting atom-atom correlation maps to be used with biomolecular alignment methods
 *  
 * <p>
 * The original SMILES description can been found at the
 * <a href="http://www.daylight.com/smiles/">SMILES Home Page</a>.
 * 
 * Specification for this implementation can be found in package.html.
 * 
 * <p>
 * <pre><code>
 * public methods:
 * 
 * int areEqual  -- checks a SMILES string against a reference (-1 for error; 0 for no finds; >0 for number of finds)
 * 
 * BitSet[] find  -- finds one or more occurances of a SMILES or SMARTS string within a SMILES string
 * 
 * int[][] getCorrelationMaps  -- returns correlated arrays of atoms
 * 
 * String getLastError  -- returns any error that was last encountered.
 * 
 * String getMolecularFormula   -- returns the MF of a SMILES or SMARTS string
 * 
 * String getRelationship -- returns isomeric relationship
 * 
 * String getSmiles  -- returns a standard SMILES string or a
 *                  Jmol BIOSMILES string with comment header.
 * 
 * BitSet getSubstructureSet  -- returns a single BitSet with all found atoms included
 *   
 *   
 *   in Jmol script:
 *   
 *   string2.find("SMILES", string1)
 *   string2.find("SMARTS", string1)
 *   
 *   e.g.
 *   
 *     print "CCCC".find("SMILES", "C[C]")
 *
 *   select search("smartsString")
 *   
 *   All bioSMARTS strings begin with ~ (tilde).
 *   
 * </code></pre>
 * 
 * @author Bob Hanson
 * 
 */
public class SmilesMatcher implements SmilesMatcherInterface {

  private final static int MODE_BITSET = 1;
  private final static int MODE_ARRAY = 2;
  private final static int MODE_MAP = 3;

  @Override
public String getLastException() {
    return InvalidSmilesException.getLastError();
  }

  @Override
public String getMolecularFormula(String pattern, boolean isSmarts) {
    InvalidSmilesException.setLastError(null);
    try {
      // note: Jmol may undercount the number of hydrogen atoms
      // for aromatic amines where the ring bonding to N is 
      // not explicit. Each "n" will be assigned a bonding count
      // of two unless explicitly indicated as -n-.
      // Thus, we take the position that "n" is the 
      // N of pyridine unless otherwise indicated.
      //
      // For example:
      //   $ print "c1ncccc1C".find("SMILES","MF")
      //   H 7 C 5 N 1   (correct)
      //   $ print "c1nc-n-c1C".find("SMILES","MF")
      //   H 6 C 4 N 2   (correct)
      // but
      //   $ print "c1ncnc1C".find("SMILES","MF")
      //   H 5 C 4 N 2   (incorrect)
      SmilesSearch search = SmilesParser.getMolecule(pattern, isSmarts);
      search.createTopoMap(null);
      search.nodes = search.jmolAtoms;
      return search.getMolecularFormula(!isSmarts);
    } catch (InvalidSmilesException e) {
      if (InvalidSmilesException.getLastError() == null)
        InvalidSmilesException.setLastError(e.getMessage());
      return null;
    }
  }

  @Override
public String getSmiles(JmolNode[] atoms, int atomCount, BitSet bsSelected,
                          boolean asBioSmiles, boolean allowUnmatchedRings, boolean addCrossLinks, String comment) {    
    InvalidSmilesException.setLastError(null);
    try {
      if (asBioSmiles)
        return (new SmilesGenerator()).getBioSmiles(atoms, atomCount,
            bsSelected, allowUnmatchedRings, addCrossLinks, comment);
      return (new SmilesGenerator()).getSmiles(atoms, atomCount, bsSelected);
    } catch (InvalidSmilesException e) {
      if (InvalidSmilesException.getLastError() == null)
        InvalidSmilesException.setLastError(e.getMessage());
      return null;
    }
  }

  @Override
public int areEqual(String smiles1, String smiles2) {
    BitSet[] result = find(smiles1, smiles2, false, false);
    return (result == null ? -1 : result.length);
  }

  /**
   * for JUnit test, mainly
   * 
   * @param smiles
   * @param molecule
   * @return        true only if the SMILES strings match and there are no errors
   */
  public boolean areEqual(String smiles, SmilesSearch molecule) {
    BitSet[] ret = find(smiles, molecule, false, true, true);
    return (ret != null && ret.length == 1);
  }

  /**
   * 
   * Searches for all matches of a pattern within a SMILES string.
   * If SMILES (not isSmarts), requires that all atoms be part of the match.
   * 
   * 
   * @param pattern
   *          SMILES or SMARTS pattern.
   * @param smiles
   * @param isSmarts TRUE for SMARTS strings, FALSE for SMILES strings
   * @param firstMatchOnly 
   * @return number of occurances of pattern within smiles
   */
  @Override
public BitSet[] find(String pattern, String smiles, boolean isSmarts,
                       boolean firstMatchOnly) {

    InvalidSmilesException.setLastError(null);
    try {
      SmilesSearch search = SmilesParser.getMolecule(smiles, false);
      return find(pattern, search, isSmarts, !isSmarts, firstMatchOnly);
    } catch (Exception e) {
      if (InvalidSmilesException.getLastError() == null)
        InvalidSmilesException.setLastError(e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  @Override
public String getRelationship(String smiles1, String smiles2) {
    if (smiles1 == null || smiles2 == null
        || smiles1.length() == 0 || smiles2.length() == 0)
      return "";
    String mf1 = getMolecularFormula(smiles1, false);
    String mf2 = getMolecularFormula(smiles2, false);
    if (!mf1.equals(mf2))
      return "none";
    boolean check;
      // note: find smiles1 IN smiles2 here
    int n1 = countStereo(smiles1);
    int n2 = countStereo(smiles2);
    check = (n1 == n2 && areEqual(smiles2, smiles1) > 0);
    if (!check) {
      // MF matched, but didn't match SMILES
      String s = smiles1 + smiles2;
      if (s.indexOf("/") >= 0 || s.indexOf("\\") >= 0 || s.indexOf("@") >= 0) {
        if (n1 == n2 && n1 > 0) {
          // reverse chirality centers
          smiles1 = reverseChirality(smiles1);
            check = (areEqual(smiles1, smiles2) > 0);
          if (check)
            return "enantiomers";
        }
        // remove all stereochemistry from SMILES string
          check = (areEqual("/nostereo/" + smiles2, smiles1) > 0);
        if (check)
          return (n1 == n2 ? "diastereomers" : "ambiguous stereochemistry!");
      }
      // MF matches, but not enantiomers or diasteriomers
      return "constitutional isomers";
    }
    return "identical";
  }
    

  @Override
public String reverseChirality(String smiles) {
    smiles = TextFormat.simpleReplace(smiles, "@@", "!@");
    smiles = TextFormat.simpleReplace(smiles, "@", "@@");
    smiles = TextFormat.simpleReplace(smiles, "!@@", "@");
    smiles = TextFormat.simpleReplace(smiles, "@@SP", "@SP");
    smiles = TextFormat.simpleReplace(smiles, "@@OH", "@OH");
    smiles = TextFormat.simpleReplace(smiles, "@@TB", "@TB");
    return smiles;
  }

  /**
   * Returns a bitset matching the pattern within atoms.
   * 
   * @param pattern
   *          SMILES or SMARTS pattern.
   * @param atoms
   * @param atomCount
   * @param bsSelected
   * @param isSmarts
   * @param firstMatchOnly
   * @return BitSet indicating which atoms match the pattern.
   */

  @Override
public BitSet getSubstructureSet(String pattern, JmolNode[] atoms,
                                   int atomCount, BitSet bsSelected,
                                   boolean isSmarts, boolean firstMatchOnly) {
    return (BitSet) match(pattern, atoms, atomCount, bsSelected, null, isSmarts,
        false, firstMatchOnly, MODE_BITSET);
  }

  @Override
public void getSubstructureSets(String[] smarts, JmolNode[] atoms, int atomCount,
                                  int flags, BitSet bsSelected, List<BitSet> ret, 
                                  List<BitSet>[] vRings) {
    InvalidSmilesException.setLastError(null);
    SmilesParser sp = new SmilesParser(true);
    SmilesSearch search = null;
    try {
      search = sp.parse("");
      search.firstMatchOnly = false;
      search.matchAllAtoms = false;
      search.jmolAtoms = atoms;
      search.jmolAtomCount = Math.abs(atomCount);
      search.setSelected(bsSelected);
      search.getRingData(true, flags, vRings);
      search.asVector = false;
      search.subSearches = new SmilesSearch[1];
      search.getSelections();
    } catch (InvalidSmilesException e) {
      // I think this is impossible.
    }
    BitSet bsDone = new BitSet();
    
    for (int i = 0; i < smarts.length; i++) {
      if (smarts[i] == null || smarts[i].length() == 0 || smarts[i].startsWith("#")) {
        ret.add(null);
        continue;
      }
      try {
        search.clear();
        SmilesSearch ss = sp.getSearch(search, SmilesParser.cleanPattern(smarts[i]), flags);
        search.subSearches[0] = ss;
        BitSet bs = BitSetUtil.copy((BitSet) search.search(false));//.subsearch(ss, false, false));
        //System.out.println(i + " " + bs);
        ret.add(bs);
        bsDone.or(bs);
        if (bsDone.cardinality() == atomCount)
          return;
        //if (ret[i] != null && ret[i].nextSetBit(0) >= 0)
          //System.out.println(smarts[i] + "  "+ ret[i]);
      } catch (Exception e) {
        if (InvalidSmilesException.getLastError() == null)
          InvalidSmilesException.setLastError(e.getMessage());
        e.printStackTrace();
        // ret[i] will be null in that case
      }
    }
  }

  /**
   * Returns a vector of bitsets indicating which atoms match the pattern.
   * 
   * @param pattern
   *          SMILES or SMARTS pattern.
   * @param atoms 
   * @param atomCount 
   * @param bsSelected 
   * @param bsAromatic 
   * @param isSmarts 
   * @param firstMatchOnly
   * @return BitSet Array indicating which atoms match the pattern.
   */
  @Override
public BitSet[] getSubstructureSetArray(String pattern, JmolNode[] atoms,
                                          int atomCount, BitSet bsSelected,
                                          BitSet bsAromatic, boolean isSmarts,
                                          boolean firstMatchOnly) {
    return (BitSet[]) match(pattern, atoms, atomCount, bsSelected, bsAromatic,
        isSmarts, false, firstMatchOnly, MODE_ARRAY);
  }

  /**
   * Rather than returning bitsets, this method returns the
   * sets of matching atoms in array form so that a direct 
   * atom-atom correlation can be made.
   * 
   * @param pattern 
   *          SMILES or SMARTS pattern.
   * @param atoms 
   * @param atomCount 
   * @param bsSelected 
   * @param isSmarts 
   * @param firstMatchOnly
   * @return      a set of atom correlations
   * 
   */
  @Override
public int[][] getCorrelationMaps(String pattern, JmolNode[] atoms,
                                    int atomCount, BitSet bsSelected,
                                    boolean isSmarts, boolean firstMatchOnly) {
    return (int[][]) match(pattern, atoms, atomCount, bsSelected, null, isSmarts,
        false, firstMatchOnly, MODE_MAP);
  }

  
  /////////////// private methods ////////////////
  
  private BitSet[] find(String pattern, SmilesSearch search, boolean isSmarts,
                        boolean matchAllAtoms, boolean firstMatchOnly) {
    // create a topological model set from smiles
    // do not worry about stereochemistry -- this
    // will be handled by SmilesSearch.setSmilesCoordinates
    BitSet bsAromatic = new BitSet();
    search.createTopoMap(bsAromatic);
    return (BitSet[]) match(pattern, search.jmolAtoms,
        -search.jmolAtoms.length, null, bsAromatic, isSmarts, matchAllAtoms,
        firstMatchOnly, MODE_ARRAY);
  }

  @SuppressWarnings({ "unchecked" })
  private Object match(String pattern, JmolNode[] atoms, int atomCount,
                       BitSet bsSelected, BitSet bsAromatic, boolean isSmarts,
                       boolean matchAllAtoms, boolean firstMatchOnly,
                       int mode) {
    InvalidSmilesException.setLastError(null);
    try {
      SmilesSearch search = SmilesParser.getMolecule(pattern, isSmarts);
      search.jmolAtoms = atoms;
      search.jmolAtomCount = Math.abs(atomCount);
      if (atomCount < 0)
        search.isSmilesFind = true;
      search.setSelected(bsSelected);
      search.getSelections();
      search.bsRequired = null;//(bsRequired != null && bsRequired.cardinality() > 0 ? bsRequired : null);
      search.setRingData(bsAromatic);
      search.firstMatchOnly = firstMatchOnly;
      search.matchAllAtoms = matchAllAtoms;
      switch (mode) {
      case MODE_BITSET:
        search.asVector = false;
        return search.search(false);
      case MODE_ARRAY:
        search.asVector = true;
        List<BitSet> vb = (List<BitSet>) search.search(false);
        return vb.toArray(new BitSet[vb.size()]);
      case MODE_MAP:
        search.getMaps = true;
        List<int[]> vl = (List<int[]>) search.search(false);
        return vl.toArray(new int[vl.size()][]);
      }
    } catch (Exception e) {
      if (InvalidSmilesException.getLastError() == null)
        InvalidSmilesException.setLastError(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  private int countStereo(String s) {
    s = TextFormat.simpleReplace(s, "@@","@");
    int i = s.lastIndexOf('@') + 1;
    int n = 0;
    for (; --i >= 0;)
      if (s.charAt(i) == '@')
        n++;
    return n;
  }


}
