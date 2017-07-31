/* $RCSfile$
 * $Author: egonw $
 * $Date: 2005-11-10 09:52:44 -0600 (Thu, 10 Nov 2005) $
 * $Revision: 4255 $
 *
 * Copyright (C) 2004-2005  The Jmol Development Team
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

package org.jmol.modelsetbio;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.vecmath.Point3f;
import javax.vecmath.Point4f;
import javax.vecmath.Vector3f;

import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolAdapterAtomIterator;
import org.jmol.api.JmolBioResolver;
import org.jmol.modelset.Atom;
import org.jmol.modelset.Bond;
import org.jmol.modelset.Chain;
import org.jmol.modelset.Group;
import org.jmol.modelset.Model;
import org.jmol.modelset.ModelLoader;
import org.jmol.modelset.ModelSet;
import org.jmol.util.JmolEdge;
import org.jmol.util.Logger;
import org.jmol.util.Measure;
import org.jmol.util.TextFormat;
import org.jmol.viewer.JmolConstants;

/**
 * a class used by ModelLoader only to handle all loading
 * of operations specific to PDB/mmCIF files. By loading
 * only by class name, only loaded if PDB file is called. 
 * 
 */
public final class Resolver implements JmolBioResolver {

  public Resolver() {
    // only implemented via reflection, and only for PDB/mmCIF files
  }
  
  @Override
public Model getBioModel(ModelSet modelSet, int modelIndex,
                        int trajectoryBaseIndex, String jmolData,
                        Properties modelProperties,
                        Map<String, Object> modelAuxiliaryInfo) {
    return new BioModel(modelSet, modelIndex, trajectoryBaseIndex,
        jmolData, modelProperties, modelAuxiliaryInfo);
  }

  @Override
public Group distinguishAndPropagateGroup(Chain chain, String group3,
                                            int seqcode, int firstAtomIndex,
                                            int maxAtomIndex, int modelIndex,
                                            int[] specialAtomIndexes,
                                            Atom[] atoms) {
    /*
     * called by finalizeGroupBuild()
     * 
     * first: build array of special atom names, for example "CA" for the alpha
     * carbon is assigned #2 see JmolConstants.specialAtomNames[] the special
     * atoms all have IDs based on Atom.lookupSpecialAtomID(atomName) these will
     * be the same for each conformation
     * 
     * second: creates the monomers themselves based on this information thus
     * building the byte offsets[] array for each monomer, indicating which
     * position relative to the first atom in the group is which atom. Each
     * monomer.offsets[i] then points to the specific atom of that type these
     * will NOT be the same for each conformation
     */

    int lastAtomIndex = maxAtomIndex - 1;

    int distinguishingBits = 0;

    // clear previous specialAtomIndexes
    for (int i = JmolConstants.ATOMID_MAX; --i >= 0;)
      specialAtomIndexes[i] = Integer.MIN_VALUE;

    // go last to first so that FIRST confirmation is default
    for (int i = maxAtomIndex; --i >= firstAtomIndex;) {
      int specialAtomID = atoms[i].getAtomID();
      if (specialAtomID <= 0)
        continue;
      if (specialAtomID < JmolConstants.ATOMID_DISTINGUISHING_ATOM_MAX) {
        /*
         * save for future option -- turns out the 1jsa bug was in relation to
         * an author using the same group number for two different groups
         * 
         * if ((distinguishingBits & (1 << specialAtomID) != 0) {
         * 
         * //bh 9/21/2006: //
         * "if the group has two of the same, that cannot be right." // Thus,
         * for example, two C's doth not make a protein "carbonyl C"
         * distinguishingBits = 0; break; }
         */
        distinguishingBits |= (1 << specialAtomID);
      }
      specialAtomIndexes[specialAtomID] = i;
    }

    if (lastAtomIndex < firstAtomIndex)
      throw new NullPointerException();

    Monomer m = null;
    if ((distinguishingBits & JmolConstants.ATOMID_PROTEIN_MASK) == JmolConstants.ATOMID_PROTEIN_MASK)
      m = AminoMonomer.validateAndAllocate(chain, group3, seqcode,
          firstAtomIndex, lastAtomIndex, specialAtomIndexes, atoms);
    else if (distinguishingBits == JmolConstants.ATOMID_ALPHA_ONLY_MASK)
      m = AlphaMonomer.validateAndAllocate(chain, group3, seqcode,
          firstAtomIndex, lastAtomIndex, specialAtomIndexes);
    else if (((distinguishingBits & JmolConstants.ATOMID_NUCLEIC_MASK) == JmolConstants.ATOMID_NUCLEIC_MASK))
      m = NucleicMonomer.validateAndAllocate(chain, group3, seqcode,
          firstAtomIndex, lastAtomIndex, specialAtomIndexes);
    else if (distinguishingBits == JmolConstants.ATOMID_PHOSPHORUS_ONLY_MASK)
      m = PhosphorusMonomer.validateAndAllocate(chain, group3, seqcode,
          firstAtomIndex, lastAtomIndex, specialAtomIndexes);
    else if (JmolConstants.checkCarbohydrate(group3))
      m = CarbohydrateMonomer.validateAndAllocate(chain, group3, seqcode,
          firstAtomIndex, lastAtomIndex);
    return ( m != null && m.leadAtomIndex >= 0 ? m : null);
  }   
  
  //////////// ADDITION OF HYDROGEN ATOMS /////////////
  // Bob Hanson and Erik Wyatt, Jmol 12.1.51, 7/1/2011
  
  /*
   * for each group, as it is finished in the file reading:
   * 
   * 1) get and store atom/bond information for group type
   * 2) add placeholder (deleted) hydrogen atoms to a group
   * 
   * in the end:
   * 
   * 3) set multiple bonding and charges
   * 4) determine actual number of required hydrogen atoms
   * 5) set hydrogen atom names, atom numbers, and positions 
   * 6) undelete those atoms  
   * 
   */
  private ModelLoader modelLoader;
  private ModelSet modelSet;
  private BitSet bsAddedHydrogens;
  private BitSet bsAtomsForHs;
  private Map<String, String>htBondMap;
  private Map<String, Boolean>htGroupBonds;
  private String[] hNames;
  private int lastSetH = Integer.MIN_VALUE;
  private int maxSerial = 0;
  private int baseBondIndex = 0;
  private boolean haveHsAlready;
  
  @Override
public void setHaveHsAlready(boolean b) {
    haveHsAlready = b;
  }

  private Vector3f vAB;
  private Vector3f vAC;
  private Vector3f vNorm;
  private Point4f plane;

  @Override
public void initialize(ModelSet modelSet) {
    this.modelSet = modelSet;
  }
  @Override
public void initializeHydrogenAddition(ModelLoader modelLoader, int bondCount) {
    this.modelLoader = modelLoader;
    baseBondIndex = bondCount;
    bsAddedHydrogens = new BitSet();
    bsAtomsForHs = new BitSet();
    htBondMap = new Hashtable<String, String>();
    htGroupBonds = new Hashtable<String, Boolean>();
    hNames = new String[3];
    vAB = new Vector3f();
    vAC = new Vector3f();
    vNorm = new Vector3f();
    plane = new Point4f();
  }
  
  @Override
public void addImplicitHydrogenAtoms(JmolAdapter adapter, int iGroup) {
    String group3 = modelLoader.getGroup3(iGroup);
    int nH;
    if (haveHsAlready || group3 == null
        || (nH = JmolConstants.getStandardPdbHydrogenCount(Group
        .lookupGroupID(group3))) == 0)
      return;
    Object model = null;
    int iFirst = modelLoader.getFirstAtomIndex(iGroup);
    int atomCount = modelSet.getAtomCount();
    if (nH < 0) {
      if (atomCount - iFirst == 1) // CA or P-only, or simple metals, also HOH, DOD
        return;
      model = modelSet.viewer.getLigandModel(group3);
      if (model == null)
        return;
      nH = adapter.getHydrogenAtomCount(model);
      if (nH < 1)
        return;
    }
    getBondInfo(adapter, group3, model);
    modelSet.getModels()[modelSet.atoms[iFirst].modelIndex].isPdbWithMultipleBonds = true;
    bsAtomsForHs.set(iFirst, atomCount);
    bsAddedHydrogens.set(atomCount, atomCount + nH);
    boolean isHetero = modelSet.atoms[iFirst].isHetero();
    for (int i = 0; i < nH; i++)
      modelSet.addAtom(modelSet.atoms[iFirst].modelIndex, modelSet.atoms[iFirst].getGroup(), (short) 1, "H", 0,
          0, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, 0, 0, 1, 0,
          null, isHetero, (byte) 0, null).delete(null);
  }

  public void getBondInfo(JmolAdapter adapter, String group3, Object model) {
    if (htGroupBonds.get(group3) != null)
      return;
    String[][] bondInfo;
    if (model == null) {
      bondInfo = modelSet.viewer.getPdbBondInfo(group3);
    } else {
      bondInfo = getLigandBondInfo(adapter, model, group3);
    }
    if (bondInfo == null)
      return;
    htGroupBonds.put(group3, Boolean.TRUE);
    for (int i = 0; i < bondInfo.length; i++) {
      if (bondInfo[i] == null)
        continue;
      if (bondInfo[i][1].charAt(0) == 'H')
        htBondMap.put(group3 + "." + bondInfo[i][0], bondInfo[i][1]);
      else
        htBondMap.put(group3 + ":" + bondInfo[i][0] + ":" + bondInfo[i][1], bondInfo[i][2]);
    }
  }

  /**
   * reads PDB ligand CIF info and creates a bondInfo object.
   * 
   * @param adapter
   * @param model
   * @param group3 
   * @return      [[atom1, atom2, order]...]
   */
  private String[][] getLigandBondInfo(JmolAdapter adapter, Object model, String group3) {
    String[][] dataIn = adapter.getBondList(model);
    Map<String, Point3f> htAtoms = new Hashtable<String, Point3f>();
    JmolAdapterAtomIterator iterAtom = adapter.getAtomIterator(model);
    while (iterAtom.hasNext())
      htAtoms.put(iterAtom.getAtomName(), iterAtom.getXYZ());      
    String[][] bondInfo = new String[dataIn.length * 2][];
    int n = 0;
    for (int i = 0; i < dataIn.length; i++) {
      String[] b = dataIn[i];
      if (b[0].charAt(0) != 'H')
        bondInfo[n++] = new String[] { b[0], b[1], b[2],
            b[1].startsWith("H") ? "0" : "1" };
      if (b[1].charAt(0) != 'H')
        bondInfo[n++] = new String[] { b[1], b[0], b[2],
            b[0].startsWith("H") ? "0" : "1" };
    }
    Arrays.sort(bondInfo, new BondSorter());
    // now look for 
    String[] t;
    for (int i = 0; i < n;) {
      t = bondInfo[i];
      String a1 = t[0];
      int nH = 0;
      int nC = 0;
      for (; i < n && (t = bondInfo[i])[0].equals(a1); i++) {
        if (t[3].equals("0")) {
          nH++;
          continue;
        }
        if (t[3].equals("1"))
          nC++;
      }
      int pt = i - nH - nC;
      if (nH == 1)
        continue;
      switch (nC) {
      case 1:
        char sep = (nH == 2 ? '@' : '|');
        for (int j = 1; j < nH; j++) {
          bondInfo[pt][1] += sep + bondInfo[pt + j][1];
          bondInfo[pt + j] = null;
        }
        continue;
      case 2:
        if (nH != 2)
          continue;
        String name = bondInfo[pt][0];
        String name1 = bondInfo[pt + nH][1];
        String name2 = bondInfo[pt + nH + 1][1];
        int factor = name1.compareTo(name2);
        Measure.getPlaneThroughPoints(htAtoms.get(name1), htAtoms.get(name), htAtoms.get(name2), vNorm, vAB, vAC,
            plane);
        float d = Measure.distanceToPlane(plane, htAtoms.get(bondInfo[pt][1])) * factor;
        bondInfo[pt][1] = (d > 0 ? bondInfo[pt][1] + "@" + bondInfo[pt + 1][1]
            :  bondInfo[pt + 1][1] + "@" + bondInfo[pt][1]);
        bondInfo[pt + 1] = null;
      }
    }
    for (int i = 0; i < n; i++) {
      if ((t = bondInfo[i]) != null && t[1].charAt(0) != 'H' && t[0].compareTo(t[1]) > 0) {
        bondInfo[i] = null;
        continue;
      }
      if (t != null)
        Logger.info(" ligand " + group3 + ": " + bondInfo[i][0] + " - " + bondInfo[i][1] + " order " + bondInfo[i][2]);
    }
    return bondInfo;
  }
  
  class BondSorter implements Comparator<String[]>{
    @Override
	public int compare(String[] a, String[] b) {
      return (b == null ? (a == null ? 0 : -1) : a == null ? 1 : a[0]
          .compareTo(b[0]) < 0 ? -1 : a[0].compareTo(b[0]) > 0 ? 1 : a[3]
          .compareTo(b[3]) < 0 ? -1 : a[3].compareTo(b[3]) > 0 ? 1 : a[1]
          .compareTo(b[1]) < 0 ? -1 : a[1].compareTo(b[1]) > 0 ? 1 : 0);
    }
  }
  
  @Override
public void finalizeHydrogens() {
    modelSet.viewer.getLigandModel(null);
    finalizePdbMultipleBonds();
    if (bsAddedHydrogens.nextSetBit(0) >= 0) {
      finalizePdbCharges();
      int[] nTotal = new int[1];
      Point3f[][] pts = modelSet.calculateHydrogens(bsAtomsForHs, nTotal, true,
          false, null);
      Group groupLast = null;
      int ipt = 0;
      for (int i = 0; i < pts.length; i++) {
        if (pts[i] == null)
          continue;
        Atom atom = modelSet.atoms[i];
        Group g = atom.getGroup();
        if (g != groupLast) {
          groupLast = g;
          ipt = g.lastAtomIndex;
          while (bsAddedHydrogens.get(ipt))
            ipt--;
        }
        String gName = atom.getGroup3(false);
        String aName = atom.getAtomName();
        String hName = htBondMap.get(gName + "." + aName);
        if (hName == null)
          continue;
        boolean isChiral = hName.contains("@");
        boolean isMethyl = (hName.endsWith("?") || hName.indexOf("|") >= 0);
        int n = pts[i].length;
        if (n == 3 && !isMethyl && hName.equals("H@H2")) {
          hName = "H|H2|H3";
          isMethyl = true;
          isChiral = false;
        }
        if (isChiral && n == 3 || isMethyl != (n == 3)) {
          Logger.info("Error adding H atoms to " + gName + g.getResno() + ": "
              + pts[i].length + " atoms should not be added to " + aName);
          continue;
        }
        int pt = hName.indexOf("@");
        switch (pts[i].length) {
        case 1:
          if (pt > 0)
            hName = hName.substring(0, pt);
          setHydrogen(i, ++ipt, hName, pts[i][0]);
          break;
        case 2:
          String hName1,
          hName2;
          float d = -1;
          Bond[] bonds = atom.getBonds();
          if (bonds != null)
            switch (bonds.length) {
            case 2:
              // could be nitrogen?
              Atom atom1 = bonds[0].getOtherAtom(atom);
              Atom atom2 = bonds[1].getOtherAtom(atom);
              int factor = atom1.getAtomName().compareTo(atom2.getAtomName());
              Measure.getPlaneThroughPoints(atom1, atom, atom2, vNorm, vAB,
                  vAC, plane);
              d = Measure.distanceToPlane(plane, pts[i][0]) * factor;
              break;
            }
          if (pt < 0) {
            Logger.info("Error adding H atoms to " + gName + g.getResno()
                + ": expected to only need 1 H but needed 2");
            hName1 = hName2 = "H";
          } else if (d < 0) {
            hName2 = hName.substring(0, pt);
            hName1 = hName.substring(pt + 1);
          } else {
            hName1 = hName.substring(0, pt);
            hName2 = hName.substring(pt + 1);
          }
          setHydrogen(i, ++ipt, hName1, pts[i][0]);
          setHydrogen(i, ++ipt, hName2, pts[i][1]);
          break;
        case 3:
          int pt1 = hName.indexOf('|');
          if (pt1 >= 0) {
            int pt2 = hName.lastIndexOf('|');
            hNames[0] = hName.substring(0, pt1);
            hNames[1] = hName.substring(pt1 + 1, pt2);
            hNames[2] = hName.substring(pt2 + 1);
          } else {
            hNames[0] = hName.replace('?', '1');
            hNames[1] = hName.replace('?', '2');
            hNames[2] = hName.replace('?', '3');
          }
//          Measure.getPlaneThroughPoints(pts[i][0], pts[i][1], pts[i][2], vNorm, vAB,
  //            vAC, plane);
    //      d = Measure.distanceToPlane(plane, atom);
      //    int hpt = (d < 0 ? 1 : 2);
          setHydrogen(i, ++ipt, hNames[0], pts[i][0]);
          setHydrogen(i, ++ipt, hNames[1], pts[i][2]);
          setHydrogen(i, ++ipt, hNames[2], pts[i][1]);          
          break;
        }
      }
      deleteUnneededAtoms();
    }
  }

  /**
   * Delete hydrogen atoms that are still in bsAddedHydrogens, 
   * because they were not actually added.
   * Also delete ligand hydrogen atoms from CO2- and PO3(2-)
   * 
   * Note that we do this AFTER all atoms have been added. That means that
   * this operation will not mess up atom indexing
   * 
   */
  private void deleteUnneededAtoms() {
    BitSet bsBondsDeleted = new BitSet();
    for (int i = bsAtomsForHs.nextSetBit(0); i >= 0; i = bsAtomsForHs
        .nextSetBit(i + 1)) {
      Atom atom = modelSet.atoms[i];
      // specifically look for neutral HETATM O with a bond count of 2: 
      if (!atom.isHetero() || atom.getElementNumber() != 8 || atom.getFormalCharge() != 0
          || atom.getCovalentBondCount() != 2)
        continue;
      Bond[] bonds = atom.getBonds();
      Atom atom1 = bonds[0].getOtherAtom(atom);
      Atom atomH = bonds[1].getOtherAtom(atom);
      if (atom1.getElementNumber() == 1) {
        Atom a = atom1;
        atom1 = atomH;
        atomH = a;
      }
      
      // Does it have an H attached?
      if (atomH.getElementNumber() != 1)
        continue;
      // If so, does it have an attached atom that is doubly bonded to O?
      // so this could be RSO4H or RPO3H2 or RCO2H
      Bond[] bonds1 = atom1.getBonds();
      for (int j = 0; j < bonds1.length; j++) {
        if (bonds1[j].order == 2) {
          Atom atomO = bonds1[j].getOtherAtom(atom1);
          if (atomO.getElementNumber() == 8) {
            bsAddedHydrogens.set(atomH.index);
            atomH.delete(bsBondsDeleted);
            // could do this... atom.setFormalCharge(atom.getFormalCharge() - 1);
            break;
          }
        }

      }
    }
    modelSet.deleteBonds(bsBondsDeleted, true);
    modelLoader.deleteAtoms(bsAddedHydrogens);
  }

  private void finalizePdbCharges() {
    Atom[] atoms = modelSet.atoms;
    // fix terminal N groups as +1
    for (int i = bsAtomsForHs.nextSetBit(0); i >= 0; i = bsAtomsForHs.nextSetBit(i + 1)) {
      if (atoms[i].getGroup().getNitrogenAtom() == atoms[i] && atoms[i].getCovalentBondCount() == 1)
        atoms[i].setFormalCharge(1);
      if ((i = bsAtomsForHs.nextClearBit(i + 1)) < 0)
        break;
    }
  }
  
  private void finalizePdbMultipleBonds() {
    Map<String, Boolean> htKeysUsed = new Hashtable<String, Boolean>();
    int bondCount = modelSet.getBondCount();
    Bond[] bonds = modelSet.getBonds();
    for (int i = baseBondIndex; i < bondCount; i++) {
      Atom a1 = bonds[i].getAtom1();
      Atom a2 = bonds[i].getAtom2();
      Group g = a1.getGroup();
      if (g != a2.getGroup())
        continue;
      StringBuffer key = new StringBuffer(g.getGroup3());
      key.append(":");
      String n1 = a1.getAtomName();
      String n2 = a2.getAtomName();
      if (n1.compareTo(n2) > 0)
        key.append(n2).append(":").append(n1);
      else
        key.append(n1).append(":").append(n2);
      String skey = key.toString();
      String type = htBondMap.get(skey);
      if (type == null)
        continue;
      htKeysUsed.put(skey, Boolean.TRUE);
      bonds[i].setOrder(Integer.valueOf(type).intValue());
    }

    for (String key : htBondMap.keySet()) {
      if (htKeysUsed.get(key) != null)
        continue;
      if (key.indexOf(":") < 0) {
        htKeysUsed.put(key, Boolean.TRUE);
        continue;
      }
      String value = htBondMap.get(key);
      Logger.info("bond " + key + " was not used; order=" + value);
      if (htBondMap.get(key).equals("1")) {
        htKeysUsed.put(key, Boolean.TRUE);
        continue; // that's ok
      }
    }
    Map<String, String> htKeysBad = new Hashtable<String, String>();
    for (String key : htBondMap.keySet()) {
      if (htKeysUsed.get(key) != null)
        continue;
      htKeysBad.put(key.substring(0, key.lastIndexOf(":")), htBondMap.get(key));
    }
    if (htKeysBad.isEmpty())
      return;
    for (int i = 0; i < bondCount; i++) {
      Atom a1 = bonds[i].getAtom1();
      Atom a2 = bonds[i].getAtom2();
      if (a1.getGroup() == a2.getGroup())
        continue;
      String value;
      if ((value = htKeysBad.get(a1.getGroup3(false) + ":" + a1.getAtomName())) == null
          && ((value = htKeysBad.get(a2.getGroup3(false) + ":" + a2.getAtomName())) == null))
        continue;
      bonds[i].setOrder(Integer.valueOf(value).intValue());
      Logger.info("assigning order " + bonds[i].order + " to bond " + bonds[i]);
    }
  }

  private void setHydrogen(int iTo, int iAtom, String name, Point3f pt) {
    if (!bsAddedHydrogens.get(iAtom))
      return;
    Atom[] atoms = modelSet.atoms;
    if (lastSetH == Integer.MIN_VALUE || atoms[iAtom].modelIndex != atoms[lastSetH].modelIndex) 
      maxSerial = ((int[]) modelSet.getModelAuxiliaryInfo(atoms[lastSetH = iAtom].modelIndex, "PDB_CONECT_firstAtom_count_max"))[2];
    bsAddedHydrogens.clear(iAtom);
    modelSet.setAtomName(iAtom, name);
    atoms[iAtom].set(pt);
    modelSet.setAtomNumber(iAtom, ++maxSerial);
    atoms[iAtom].setAtomSymmetry(atoms[iTo].getAtomSymmetry());
    modelLoader.undeleteAtom(iAtom);

    modelSet.bondAtoms(atoms[iTo], atoms[iAtom], JmolEdge.BOND_COVALENT_SINGLE, 
        modelSet.getDefaultMadFromOrder(JmolEdge.BOND_COVALENT_SINGLE), null, 0, true, false);
  }

  @Override
public String fixPropertyValue(BitSet bsAtoms, String data) {
    String[] aData = TextFormat.split(data, '\n');
    Atom[] atoms = modelSet.atoms;
    String[] newData = new String[bsAtoms.cardinality()];
    String lastData = "";
    for (int pt = 0, iAtom = 0, i = bsAtoms.nextSetBit(0); i >= 0; i = bsAtoms
        .nextSetBit(i), iAtom++) {
      if (atoms[i].getElementNumber() != 1)
        lastData = aData[pt++];
      newData[iAtom] = lastData;
    }
    return TextFormat.join(newData, '\n', 0);
  }

}


