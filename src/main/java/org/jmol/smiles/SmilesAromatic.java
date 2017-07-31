/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2010-05-08 19:20:44 -0500 (Sat, 08 May 2010) $
 * $Revision: 13038 $
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

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.jmol.util.JmolEdge;
import org.jmol.util.JmolNode;

public class SmilesAromatic {
  /** 
   * 3D-SEARCH aromaticity test.
   * 
   * A simple and unambiguous test for aromaticity based on 3D geometry 
   * and connectivity only, not Hueckel theory.
   * @param atoms
   *       a set of atoms with coordinate positions and associated bonds.
   * @param bs
   *       a bitset of atoms within the set of atoms, defining the ring 
   * @param bsSelected
   *       must not be null 
   * @param cutoff
   *       an arbitrary value to test the standard deviation against. 
   *       0.01 is appropriate here.   
   * @return
   *        true if standard deviation of vNorm.dot.vMean is less than cutoff
   */

  public final static boolean isFlatSp2Ring(JmolNode[] atoms,
                                            BitSet bsSelected, BitSet bs,
                                            float cutoff) {
    /*
     * 
     * Bob Hanson, hansonr@stolaf.edu
     * 
     *   Given a ring of N atoms...
     *   
     *                 1
     *               /   \
     *              2     6 -- 6a
     *              |     |
     *        5a -- 5     4
     *               \   /
     *                 3  
     *   
     *   with arbitrary order and up to N substituents
     *   
     *   1) Check to see if all ring atoms have no more than 3 connections.
     *      Note: An alternative definition might include "and no substituent
     *      is explicitly double-bonded to its ring atom, as in quinone.
     *      Here we opt to allow the atoms of quinone to be called "aromatic."
     *   2) Select a cutoff value close to zero. We use 0.01 here. 
     *   3) Generate a set of normals as follows:
     *      a) For each ring atom, construct the normal associated with the plane
     *         formed by that ring atom and its two nearest ring-atom neighbors.
     *      b) For each ring atom with a substituent, construct a normal 
     *         associated with the plane formed by its connecting substituent
     *         atom and the two nearest ring-atom neighbors.
     *      c) If this is the first normal, assign vMean to it. 
     *      d) If this is not the first normal, check vNorm.dot.vMean. If this
     *         value is less than zero, scale vNorm by -1.
     *      e) Add vNorm to vMean. 
     *   4) Calculate the standard deviation of the dot products of the 
     *      individual vNorms with the normalized vMean. 
     *   5) The ring is deemed flat if this standard deviation is less 
     *      than the selected cutoff value. 
     *      
     *   Efficiencies:
     *   
     *   1) Precheck bond counts.
     *   
     *   2) Each time a normal is added to the running mean, test to see if 
     *      its dot product with the mean is within 5 standard deviations. 
     *      If it is not, return false. Note that it can be shown that for 
     *      a set of normals, even if all are aligned except one, with dot product
     *      to the mean x, then the standard deviation will be (1 - x) / sqrt(N).
     *      Given even an 8-membered ring, this still
     *      results in a minimum value of x of about 1-4c (allowing for as many as
     *      8 substituents), considerably better than our 1-5c. 
     *      So 1-5c is a very conservative test.   
     *      
     *   3) One could probably dispense with the actual standard deviation 
     *      calculation, as it is VERY unlikely that an actual nonaromatic rings
     *      (other than quinones and other such compounds)
     *      would have any chance of passing the first two tests.
     *   
     */

    for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
      JmolNode ringAtom = atoms[i];
      JmolEdge[] bonds = ringAtom.getEdges();
      if (bonds.length < 3)
        continue;
      if (bonds.length > 3)
        return false;
    }
    if (cutoff == Float.MAX_VALUE)
      return true;
    
    if (cutoff <= 0)
      cutoff = 0.01f;

    Vector3f vTemp = new Vector3f();
    Vector3f vA = new Vector3f();
    Vector3f vB = new Vector3f();
    Vector3f vMean = null;
    int nPoints = bs.cardinality();
    Vector3f[] vNorms = new Vector3f[nPoints * 2];
    int nNorms = 0;
    float maxDev = (1 - cutoff * 5);
    for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
      JmolNode ringAtom = atoms[i];
      JmolEdge[] bonds = ringAtom.getEdges();
      // if more than three connections, ring cannot be fully conjugated
      // identify substituent and two ring atoms
      int iSub = -1;
      int r1 = -1;
      int r2 = -1;
      for (int k = bonds.length; --k >= 0;) {
        int iAtom = ringAtom.getBondedAtomIndex(k);
        if (!bsSelected.get(iAtom))
          continue;
        if (!bs.get(iAtom))
          iSub = iAtom;
        else if (r1 < 0)
          r1 = iAtom;
        else
          r2 = iAtom;
      }
      // get the normals through r1 - k - r2 and r1 - iSub - r2
      getNormalThroughPoints(atoms[r1], atoms[i], atoms[r2], vTemp, vA, vB);
      if (vMean == null)
        vMean = new Vector3f();
      if (!addNormal(vTemp, vMean, maxDev))
        return false;
      vNorms[nNorms++] = new Vector3f(vTemp);
      if (iSub >= 0) {
        getNormalThroughPoints(atoms[r1], atoms[iSub], atoms[r2], vTemp, vA, vB);
        if (!addNormal(vTemp, vMean, maxDev))
          return false;
        vNorms[nNorms++] = new Vector3f(vTemp);
      }
    }
    boolean isFlat = checkStandardDeviation(vNorms, vMean, nNorms, cutoff);
    //System.out.println(Escape.escape(bs) + " aromatic ? " + isAromatic);
    return isFlat;
  }

  private final static boolean addNormal(Vector3f vTemp, Vector3f vMean,
                                         float maxDev) {
    float similarity = vMean.dot(vTemp);
    if (similarity != 0 && Math.abs(similarity) < maxDev)
      return false;
    if (similarity < 0)
      vTemp.scale(-1);
    vMean.add(vTemp);
    vMean.normalize();
    return true;
  }

  private final static boolean checkStandardDeviation(Vector3f[] vNorms,
                                                      Vector3f vMean, int n,
                                                      float cutoff) {
    double sum = 0;
    double sum2 = 0;
    for (int i = 0; i < n; i++) {
      float v = vNorms[i].dot(vMean);
      sum += v;
      sum2 += ((double) v) * v;
    }
    sum = Math.sqrt((sum2 - sum * sum / n) / (n - 1));
    //System.out.println("stdev = " + sum);
    return (sum < cutoff);
  }

  static float getNormalThroughPoints(JmolNode pointA, JmolNode pointB,
                                      JmolNode pointC, Vector3f vNorm,
                                      Vector3f vAB, Vector3f vAC) {
    vAB.sub((Point3f) pointB, (Point3f) pointA);
    vAC.sub((Point3f) pointC, (Point3f) pointA);
    vNorm.cross(vAB, vAC);
    vNorm.normalize();
    // ax + by + cz + d = 0
    // so if a point is in the plane, then N dot X = -d
    vAB.set((Point3f) pointA);
    return -vAB.dot(vNorm);
  }

  /**
   * set aromatic atoms based on predefined BOND_AROMATIC definitions
   * @param jmolAtoms
   * @param bsAtoms
   * @return bsAromatic
   */
  static BitSet checkAromaticDefined(JmolNode[] jmolAtoms, BitSet bsAtoms) {
    BitSet bsDefined = new BitSet();
    for (int i = bsAtoms.nextSetBit(0); i >= 0; i = bsAtoms.nextSetBit(i + 1)) {
      JmolEdge[] bonds = jmolAtoms[i].getEdges();
      for (int j = 0; j < bonds.length; j++) {
        switch (bonds[j].order) {
        case JmolEdge.BOND_AROMATIC:
        case JmolEdge.BOND_AROMATIC_DOUBLE:
        case JmolEdge.BOND_AROMATIC_SINGLE:
          bsDefined.set(bonds[j].getAtomIndex1());
          bsDefined.set(bonds[j].getAtomIndex2());
        }
      }
    }
    return bsDefined;
  }
  
  static void checkAromaticStrict(JmolNode[] jmolAtoms,
                                         BitSet bsAromatic, List<Object> v5,
                                         List<Object> v6) {
    BitSet bsStrict = new BitSet();
    BitSet bsTest = new BitSet();
    for (int i = v5.size(); --i >= 0; ) {
      BitSet bs = (BitSet) v5.get(i);
      if (isAromaticRing(bsAromatic, bsTest, bs, 5))
        checkAromaticStrict(jmolAtoms, bsStrict, v5, v6, bs, true);
    }
    for (int i = v6.size(); --i >= 0; ) {
      BitSet bs = (BitSet) v6.get(i);
      if (isAromaticRing(bsAromatic, bsTest, bs, 6))
        checkAromaticStrict(jmolAtoms, bsStrict, v5, v6, bs, false);
    }
    bsAromatic.clear();
    bsAromatic.or(bsStrict);
  }

  private static boolean isAromaticRing(BitSet bsAromatic, BitSet bsTest,
                                        BitSet bs, int n) {
    bsTest.clear();
    bsTest.or(bs);
    bsTest.and(bsAromatic);
    return (bsTest.cardinality() == n);
  }

  /**
   * uses an MMFF94 strategy for determining aromaticity for a specific ring.
   * 
   * @param jmolAtoms
   * @param bsStrict  growing list of aromatic atoms
   * @param v5
   * @param v6
   * @param bsRing  this ring's atoms
   * @param is5
   */
  private static void checkAromaticStrict(JmolNode[] jmolAtoms,
                                          BitSet bsStrict, List<Object> v5,
                                          List<Object> v6, BitSet bsRing,
                                          boolean is5) {
    // I believe this gives the wrong answer for mmff94_dative.mol2 CIKSEU10
    // but at least it agrees with MMFF94.  -- Bob Hanson

    //System.out.println(bsRing);
    int piElectronCount = countInternalPairs(jmolAtoms, bsRing, is5) << 1;
    switch (piElectronCount) {
    case -3:
      break;
    default:
      for (int i = bsRing.nextSetBit(0); i >= 0; i = bsRing.nextSetBit(i + 1)) {
        JmolEdge[] bonds = jmolAtoms[i].getEdges();
        for (int j = 0; j < bonds.length; j++)
          if (bonds[j].order == JmolEdge.BOND_COVALENT_DOUBLE) {
            int i2 = bonds[j].getOtherAtom(jmolAtoms[i]).getIndex();
            if (!bsRing.get(i2)) {
              boolean piShared = false;
              for (int k = v5.size(); --k >= 0 && !piShared;) {
                BitSet bs = (BitSet) v5.get(k);
                if (bs.get(i2)
                    && (bsStrict.get(i2) || Math.abs(countInternalPairs(
                        jmolAtoms, bs, true)) == 3))
                  piShared = true;
              }
              for (int k = v6.size(); --k >= 0 && !piShared;) {
                BitSet bs = (BitSet) v6.get(k);
                if (bs.get(i2)
                    && (bsStrict.get(i2) || Math.abs(countInternalPairs(
                        jmolAtoms, bs, false)) == 3))
                  piShared = true;
              }
              if (!piShared)
                return;
              piElectronCount++;
            }
          }
      }
      break;
    }
    if (piElectronCount == 6)
      bsStrict.or(bsRing);
  }

  /**
   * Counts the electron pairs that are internal to this ring. 
   * Allows for aromatic bond types.
   * Note that Jmol has already determined that the ring is flat
   * so there is no need to worry about hybridization.
   * 
   * @param jmolAtoms
   * @param bsRing
   * @param is5
   * @return  number of pairs
   */
  private static int countInternalPairs(JmolNode[] jmolAtoms, BitSet bsRing,
                                        boolean is5) {
    int nDouble = 0;
    int nAromatic = 0;
    int nLonePairs = 0;
    for (int i = bsRing.nextSetBit(0); i >= 0; i = bsRing.nextSetBit(i + 1)) {
      JmolNode atom = jmolAtoms[i];
      JmolEdge[] bonds = atom.getEdges();
      boolean haveDouble = false;
      for (int k = 0; k < bonds.length; k++) {
        int j = bonds[k].getOtherAtom(atom).getIndex();
        if (bsRing.get(j)) {
          switch (bonds[k].order) {
          case JmolEdge.BOND_AROMATIC_DOUBLE:
          case JmolEdge.BOND_AROMATIC_SINGLE:
          case JmolEdge.BOND_AROMATIC:
            nAromatic++;
            break;
          case JmolEdge.BOND_COVALENT_DOUBLE:
            nDouble++;
            haveDouble = true;
          }
        }
      }
      if (is5 && nAromatic == 0) {
        switch (atom.getElementNumber()) {
        case 7:
        case 8:
        case 16:
          if (!haveDouble)
            nLonePairs++;
          break;
        }
      }
    }
    return (nAromatic == 0 ? nDouble / 2 + nLonePairs
        : nAromatic == (is5 ? 5 : 6) ? -3 : 0);
  }

}
