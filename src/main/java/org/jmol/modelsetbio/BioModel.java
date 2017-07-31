/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2011-08-05 21:10:46 -0500 (Fri, 05 Aug 2011) $
 * $Revision: 15943 $
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
package org.jmol.modelsetbio;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.vecmath.Point3f;

import org.jmol.constant.EnumStructure;
import org.jmol.modelset.Atom;
import org.jmol.modelset.Bond;
import org.jmol.modelset.Group;
import org.jmol.modelset.HBond;
import org.jmol.modelset.LabelToken;
import org.jmol.modelset.Model;
import org.jmol.modelset.ModelSet;
import org.jmol.script.Token;
import org.jmol.util.ArrayUtil;
import org.jmol.util.BitSetUtil;
import org.jmol.util.Escape;
import org.jmol.util.JmolEdge;
import org.jmol.util.OutputStringBuffer;
import org.jmol.util.TextFormat;
import org.jmol.viewer.JmolConstants;
import org.jmol.viewer.Viewer;

public final class BioModel extends Model{

  /*
   *   
   * Note that "monomer" extends group. A group only becomes a 
   * monomer if it can be identified as one of the following 
   * PDB/mmCIF types:
   * 
   *   amino  -- has an N, a C, and a CA
   *   alpha  -- has just a CA
   *   nucleic -- has C1',C2',C3',C4',C5',O3', and O5'
   *   phosphorus -- has P
   *   
   * The term "conformation" is a bit loose. It means "what you get
   * when you go with one or another set of alternative locations.
   *
   *  
   */
  
  private int bioPolymerCount = 0;
  private BioPolymer[] bioPolymers;

  BioModel(ModelSet modelSet, int modelIndex, int trajectoryBaseIndex, 
      String jmolData, Properties properties, Map<String, Object> auxiliaryInfo) {
    super(modelSet, modelIndex, trajectoryBaseIndex, jmolData, properties, auxiliaryInfo);
    isBioModel = true;
    clearBioPolymers();
  }

  @Override
  public void freeze() {
    super.freeze();
    bioPolymers = (BioPolymer[])ArrayUtil.setLength(bioPolymers, bioPolymerCount);
  }
  
  @Override
  public void addSecondaryStructure(EnumStructure type, 
                             String structureID, int serialID, int strandCount,
                             char startChainID, int startSeqcode,
                             char endChainID, int endSeqcode) {
    for (int i = bioPolymerCount; --i >= 0; )
      bioPolymers[i].addSecondaryStructure(type, structureID, serialID, strandCount, startChainID, startSeqcode,
                                    endChainID, endSeqcode);
  }

  @Override
  public String calculateStructures(boolean asDSSP, boolean doReport, 
                             boolean dsspIgnoreHydrogen, boolean setStructure,
                             boolean includeAlpha) {
    if (bioPolymerCount == 0 || !setStructure && !asDSSP)
      return "";
    modelSet.proteinStructureTainted = structureTainted = true;
    if (setStructure)
      for (int i = bioPolymerCount; --i >= 0;)
        if (!asDSSP || bioPolymers[i].getGroups()[0].getNitrogenAtom() != null)
          bioPolymers[i].clearStructures();
    if (!asDSSP || includeAlpha)
      for (int i = bioPolymerCount; --i >= 0;)
        bioPolymers[i].calculateStructures(includeAlpha);
    return (asDSSP ? bioPolymers[0].calculateStructures(bioPolymers, bioPolymerCount,
          null, doReport, dsspIgnoreHydrogen, setStructure) : "");
  }

  @Override
  public void setConformation(BitSet bsConformation) {
    if (nAltLocs > 0)
      for (int i = bioPolymerCount; --i >= 0; )
        bioPolymers[i].setConformation(bsConformation);
  }

  @Override
  public boolean getPdbConformation(BitSet bsConformation, int conformationIndex) {
    if (nAltLocs > 0)
      for (int i = bioPolymerCount; --i >= 0;)
        bioPolymers[i].getConformation(bsConformation, conformationIndex);
    return true;
  }

  @Override
  public int getBioPolymerCount() {
    return bioPolymerCount;
  }

  @Override
  public void calcSelectedMonomersCount(BitSet bsSelected) {
    for (int i = bioPolymerCount; --i >= 0; )
      bioPolymers[i].calcSelectedMonomersCount(bsSelected);
  }

  public BioPolymer getBioPolymer(int polymerIndex) {
    return bioPolymers[polymerIndex];
  }

  @Override
  public void getDefaultLargePDBRendering(StringBuffer sb, int maxAtoms) {
    BitSet bs = new BitSet();
    if (getBondCount() == 0)
      bs = bsAtoms;
    // all biopolymer atoms...
    if (bs != bsAtoms)
      for (int i = 0; i < bioPolymerCount; i++)
        bioPolymers[i].getRange(bs);
    if (bs.nextSetBit(0) < 0)
      return;
    // ...and not connected to backbone:
    BitSet bs2 = new BitSet();
    if (bs == bsAtoms) {
      bs2 = bs;
    } else {
      for (int i = 0; i < bioPolymerCount; i++)
        if (bioPolymers[i].getType() == BioPolymer.TYPE_NOBONDING)
          bioPolymers[i].getRange(bs2);
    }
    if (bs2.nextSetBit(0) >= 0)
      sb.append("select ").append(Escape.escape(bs2)).append(";backbone only;");
    if (atomCount <= maxAtoms)
      return;
    // ...and it's a large model, to wireframe:
      sb.append("select ").append(Escape.escape(bs)).append(" & connected; wireframe only;");
    // ... and all non-biopolymer and not connected to stars...
    if (bs != bsAtoms) {
      bs2.clear();
      bs2.or(bsAtoms);
      bs2.andNot(bs);
      if (bs2.nextSetBit(0) >= 0)
        sb.append("select " + Escape.escape(bs2) + " & !connected;stars 0.5;");
    }
  }
  
  @Override
  public void fixIndices(int modelIndex, int nAtomsDeleted, BitSet bsDeleted) {
    super.fixIndices(modelIndex, nAtomsDeleted, bsDeleted);
    for (int i = 0; i < bioPolymerCount; i++)
      bioPolymers[i].recalculateLeadMidpointsAndWingVectors();
  }

  @Override
  public int calculateStruts(ModelSet modelSet, BitSet bs1, BitSet bs2) {

    // only check the atoms in THIS model
    List<Atom> vCA = new ArrayList<Atom>();
    Atom a1 = null;
    BitSet bsCheck;
    if (bs1.equals(bs2)) {
      bsCheck = bs1;
    } else {
      bsCheck = BitSetUtil.copy(bs1);
      bsCheck.or(bs2);
    }
    Atom[] atoms = modelSet.atoms;
    Viewer viewer = modelSet.viewer;
    bsCheck.and(viewer.getModelUndeletedAtomsBitSet(modelIndex));
    for (int i = bsCheck.nextSetBit(0); i >= 0; i = bsCheck.nextSetBit(i + 1))
      if (atoms[i].isVisible(0)
          && atoms[i].atomID == JmolConstants.ATOMID_ALPHA_CARBON
          && atoms[i].getGroupID() != JmolConstants.GROUPID_CYSTEINE)
        vCA.add((a1 = atoms[i]));
    if (vCA.size() == 0)
      return 0;    
    float thresh = viewer.getStrutLengthMaximum();
    short mad = (short) (viewer.getStrutDefaultRadius() * 2000);
    int delta = viewer.getStrutSpacingMinimum();
    boolean strutsMultiple = viewer.getStrutsMultiple();
    List<Atom[]> struts = getBioPolymer(a1.getPolymerIndexInModel())
        .calculateStruts(modelSet, bs1, bs2, vCA, thresh, delta, strutsMultiple);
    for (int i = 0; i < struts.size(); i++) {
      Atom[] o = struts.get(i);
      modelSet.bondAtoms(o[0], o[1], JmolEdge.BOND_STRUT, mad, null, 0, false, true);
    }
    return struts.size(); 
  }
  
  @Override
  public void setStructureList(Map<EnumStructure, float[]> structureList) {
    bioPolymers = (BioPolymer[])ArrayUtil.setLength(bioPolymers, bioPolymerCount);
    for (int i = bioPolymerCount; --i >= 0; )
      bioPolymers[i].setStructureList(structureList);
  }

  @Override
  public void calculateStraightness(Viewer viewer, char ctype, char qtype,
                                    int mStep) {
    for (int p = 0; p < bioPolymerCount; p++)
      bioPolymers[p].getPdbData(viewer, ctype, qtype, mStep, 2, null, 
          null, false, false, false, null, null, null, new BitSet());
  }
  
  
  @Override
  public void getPolymerPointsAndVectors(BitSet bs, List<Point3f[]> vList,
                                         boolean isTraceAlpha,
                                         float sheetSmoothing) {
    int last = Integer.MAX_VALUE - 1;
    for (int ip = 0; ip < bioPolymerCount; ip++)
      last = bioPolymers[ip]
          .getPolymerPointsAndVectors(last, bs, vList, isTraceAlpha, sheetSmoothing);
  }

  @Override
  public Point3f[] getPolymerLeadMidPoints(int iPolymer) {
    return bioPolymers[iPolymer].getLeadMidpoints();
  }

  @Override
  public void recalculateLeadMidpointsAndWingVectors() {
    for (int ip = 0; ip < bioPolymerCount; ip++)
      bioPolymers[ip].recalculateLeadMidpointsAndWingVectors();
  }

  
  @Override
  public List<BitSet> getBioBranches(List<BitSet> biobranches) {
    // scan through biopolymers quickly -- 
    BitSet bsBranch;
    for (int j = 0; j < bioPolymerCount; j++) {
      bsBranch = new BitSet();
      bioPolymers[j].getRange(bsBranch);
      int iAtom = bsBranch.nextSetBit(0);
      if (iAtom >= 0) {
        if (biobranches == null)
          biobranches = new ArrayList<BitSet>();
        biobranches.add(bsBranch);
      }
    }
    return biobranches;
  }

  @Override
  public void getGroupsWithin(int nResidues, BitSet bs, BitSet bsResult) {
    for (int i = bioPolymerCount; --i >= 0;)
      bioPolymers[i].getRangeGroups(nResidues, bs, bsResult);
  }
  
  @Override
  public void getSequenceBits(String specInfo, BitSet bs, BitSet bsResult) {
    int lenInfo = specInfo.length();
    for (int ip = 0; ip < bioPolymerCount; ip++) {
      String sequence = bioPolymers[ip].getSequence();
      int j = -1;
      while ((j = sequence.indexOf(specInfo, ++j)) >=0)
        bioPolymers[ip].getPolymerSequenceAtoms(j, lenInfo, bs, bsResult);
    }
  }

  @Override
  public void selectSeqcodeRange(int seqcodeA, int seqcodeB, char chainID,
                                 BitSet bs, boolean caseSensitive) {
    char ch;
    for (int i = chainCount; --i >= 0;)
      if (chainID == (ch = chains[i].chainID) || chainID == '\t' || !caseSensitive
          && chainID == Character.toUpperCase(ch))
        for (int index = 0; index >= 0;)
          index = chains[i].selectSeqcodeRange(index, seqcodeA, seqcodeB, bs);
  }

  @Override
  public void getRasmolHydrogenBonds(BitSet bsA, BitSet bsB,
                                     List<Bond> vHBonds, boolean nucleicOnly,
                                     int nMax, boolean dsspIgnoreHydrogens,
                                     BitSet bsHBonds) {    
    boolean doAdd = (vHBonds == null);
    if (doAdd)
      vHBonds = new ArrayList<Bond>();
    if (nMax < 0)
      nMax = Integer.MAX_VALUE;
    boolean asDSSP = (bsB == null);
    BioPolymer bp, bp1;
    if (asDSSP && bioPolymerCount > 0) {
      bioPolymers[0].calculateStructures(bioPolymers, bioPolymerCount,
          vHBonds, false, dsspIgnoreHydrogens, false);
    } else {
      for (int i = bioPolymerCount; --i >= 0;) {
        bp = bioPolymers[i];
        int type = bp.getType();
        if ((nucleicOnly || type != BioPolymer.TYPE_AMINO)
            && type != BioPolymer.TYPE_NUCLEIC)
          continue;
        boolean isRNA = bp.isRna();
        boolean isAmino = (type == BioPolymer.TYPE_AMINO);
        if (isAmino)
          bp.calcRasmolHydrogenBonds(null, bsA, bsB, vHBonds, nMax, null, true,
              false);
        for (int j = bioPolymerCount; --j >= 0;) {
          if ((bp1 = bioPolymers[j]) != null && (isRNA || i != j)
              && type == bp1.getType()) {
            bp1.calcRasmolHydrogenBonds(bp, bsA, bsB, vHBonds, nMax, null,
                true, false);
          }
        }
      }
    }
    
    if (vHBonds.size() == 0 || !doAdd)
      return;
    hasRasmolHBonds = true;
    for (int i = 0; i < vHBonds.size(); i++) {
      HBond bond = (HBond) vHBonds.get(i);
      Atom atom1 = bond.getAtom1();
      Atom atom2 = bond.getAtom2();
      if (atom1.isBonded(atom2))
        continue;
      int index = modelSet.addHBond(atom1, atom2, bond.order, bond.getEnergy());
      if (bsHBonds != null)
        bsHBonds.set(index);
    }
  }

  @Override
  public void clearRasmolHydrogenBonds(BitSet bsAtoms) {
    //called by calcRasmolHydrogenBonds (bsAtoms not null) from autoHBond
    //      and setAtomPositions (bsAtoms null)
    BitSet bsDelete = new BitSet();
    hasRasmolHBonds = false;
    Model[] models = modelSet.getModels();
    Bond[] bonds = modelSet.getBonds();
    for (int i = modelSet.getBondCount(); --i >= 0;) {
      Bond bond = bonds[i];
      Atom atom1 = bond.getAtom1();
      Model m = models[atom1.modelIndex];
      if (!m.isBioModel || m.trajectoryBaseIndex != modelIndex
          || (bond.order & JmolEdge.BOND_H_CALC_MASK) == 0)
        continue;
      if (bsAtoms != null && !bsAtoms.get(atom1.index)) {
        hasRasmolHBonds = true;
        continue;
      }
      bsDelete.set(i);
    }
    if (bsDelete.nextSetBit(0) >= 0)
      modelSet.deleteBonds(bsDelete, false);
  }

  @Override
  public void calculatePolymers(Group[] groups, int groupCount,
                                int baseGroupIndex, BitSet modelsExcluded) {
    if (groups == null) {
      groups = modelSet.getGroups();
      groupCount = groups.length;
    }
    if (modelsExcluded != null)
      for (int i = 0; i < groupCount; ++i) {
        Group group = groups[i];
        if (group instanceof Monomer) {
          Monomer monomer = (Monomer) group;
          if (monomer.getBioPolymer() != null
              && (!modelsExcluded.get(monomer.getModelIndex())))
            monomer.setBioPolymer(null, -1);
        }
      }
    boolean checkPolymerConnections = !modelSet.viewer.isPdbSequential();
    for (int i = baseGroupIndex; i < groupCount; ++i) {
      Group g = groups[i];
      Model model = g.getModel();
      if (!model.isBioModel || ! (g instanceof Monomer))
        continue;
      boolean doCheck = checkPolymerConnections 
        && !modelSet.isJmolDataFrame(modelSet.atoms[g.firstAtomIndex].modelIndex);
      BioPolymer bp = (((Monomer) g).getBioPolymer() == null ?
          BioPolymer.allocateBioPolymer(groups, i, doCheck) : null);
      if (bp == null || bp.monomerCount == 0)
        continue;
      ((BioModel) model).addBioPolymer(bp);
      i += bp.monomerCount - 1;
    }
  }  

  private void addBioPolymer(BioPolymer polymer) {
    if (bioPolymers.length == 0)
      clearBioPolymers();
    if (bioPolymerCount == bioPolymers.length)
      bioPolymers = (BioPolymer[])ArrayUtil.doubleLength(bioPolymers);
    polymer.bioPolymerIndexInModel = bioPolymerCount;
    bioPolymers[bioPolymerCount++] = polymer;
  }

  @Override
  public void clearBioPolymers() {
    bioPolymers = new BioPolymer[8];
    bioPolymerCount = 0;
  }

  @Override
  public void getAllPolymerInfo(
                                BitSet bs,
                                Map<String, List<Map<String, Object>>> finalInfo,
                                List<Map<String, Object>> modelVector) {
    Map<String, Object> modelInfo = new Hashtable<String, Object>();
    List<Map<String, Object>> info = new ArrayList<Map<String, Object>>();
    for (int ip = 0; ip < bioPolymerCount; ip++) {
      Map<String, Object> polyInfo = bioPolymers[ip].getPolymerInfo(bs); 
      if (!polyInfo.isEmpty())
        info.add(polyInfo);
    }
    if (info.size() > 0) {
      modelInfo.put("modelIndex", Integer.valueOf(modelIndex));
      modelInfo.put("polymers", info);
      modelVector.add(modelInfo);
    }
  }
  
  @SuppressWarnings("incomplete-switch")
  @Override
  public void getChimeInfo(StringBuffer sb, int nHetero) {
    int n = 0;
    Model[] models = modelSet.getModels();
    int modelCount = modelSet.getModelCount();
    int atomCount = modelSet.getAtomCount();
    Atom[] atoms = modelSet.atoms;
    sb.append("\nMolecule name ....... "
        + modelSet.getModelSetAuxiliaryInfo("COMPND"));
    sb.append("\nSecondary Structure . PDB Data Records");
    sb.append("\nBrookhaven Code ..... " + modelSet.modelSetName);
    for (int i = modelCount; --i >= 0;)
      n += models[i].getChainCount(false);
    sb.append("\nNumber of Chains .... " + n);
    n = 0;
    for (int i = modelCount; --i >= 0;)
      n += models[i].getGroupCount(false);
    nHetero = 0;
    for (int i = modelCount; --i >= 0;)
      nHetero += models[i].getGroupCount(true);
    sb.append("\nNumber of Groups .... " + n);
    if (nHetero > 0)
      sb.append(" (" + nHetero + ")");
    for (int i = atomCount; --i >= 0;)
      if (atoms[i].isHetero())
        nHetero++;
    super.getChimeInfo(sb, nHetero);
    int nH = 0;
    int nS = 0;
    int nT = 0;
    int id;
    int lastid = -1;
    for (int i = 0; i < atomCount; i++) {
      if (atoms[i].modelIndex != 0)
        break;
      if ((id = atoms[i].getStrucNo()) != lastid && id != 0) {
        lastid = id;
        switch (atoms[i].getProteinStructureType()) {
        case HELIX:
          nH++;
          break;
        case SHEET:
          nS++;
          break;
        case TURN:
          nT++;
          break;
        }
      }
    }
    sb.append("\nNumber of Helices ... " + nH);
    sb.append("\nNumber of Strands ... " + nS);
    sb.append("\nNumber of Turns ..... " + nT);
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public String getProteinStructureState(BitSet bsAtoms, boolean taintedOnly,
                                         boolean needPhiPsi, int mode) {
    boolean showMode = (mode == 3);
    boolean pdbFileMode = (mode == 1);
    boolean scriptMode = (mode == 0);
    BitSet bs = null;
    StringBuffer cmd = new StringBuffer();
    StringBuffer sbTurn = new StringBuffer();
    StringBuffer sbHelix = new StringBuffer();
    StringBuffer sbSheet = new StringBuffer();
    EnumStructure type = EnumStructure.NONE;
    EnumStructure subtype = EnumStructure.NONE;
    int id = 0;
    int iLastAtom = 0;
    int iLastModel = -1;
    int lastId = -1;
    int res1 = 0;
    int res2 = 0;
    String sid = "";
    String group1 = "";
    String group2 = "";
    String chain1 = "";
    String chain2 = "";
    int n = 0;
    int nHelix = 0;
    int nTurn = 0;
    int nSheet = 0;
    BitSet bsTainted = null;
    Model[] models = modelSet.getModels();
    Atom[] atoms = modelSet.atoms;
    int atomCount = modelSet.getAtomCount();
    
    if (taintedOnly) {
      if (!modelSet.proteinStructureTainted)
        return "";
      bsTainted = new BitSet();
      for (int i = firstAtomIndex; i < atomCount; i++)
        if (models[atoms[i].modelIndex].isStructureTainted())
          bsTainted.set(i);
      bsTainted.set(atomCount);
    }
    for (int i = 0; i <= atomCount; i++)
      if (i == atomCount || bsAtoms == null || bsAtoms.get(i)) {
        if (taintedOnly && !bsTainted.get(i))
          continue;
        id = 0;
        if (i == atomCount || (id = atoms[i].getStrucNo()) != lastId) {
          if (bs != null) {
            switch  (type) {
            case HELIX:
            case TURN:
            case SHEET:
              n++;
              if (scriptMode) {
                int iModel = atoms[iLastAtom].modelIndex;
                String comment = "    \t# model="
                    + modelSet.getModelNumberDotted(iModel);
                if (iLastModel != iModel) {
                  iLastModel = iModel;
                    cmd.append("  structure none ").append(
                        Escape.escape(modelSet.getModelAtomBitSetIncludingDeleted(
                            iModel, false))).append(comment).append(";\n");
                }
                comment += " & (" + res1 + " - " + res2 + ")";
                String stype = subtype.getBioStructureTypeName(false);
                  cmd.append("  structure ").append(stype).append(" ").append(
                      Escape.escape(bs)).append(comment).append(";\n");
              } else {
                String str;
                int nx;
                StringBuffer sb;
                // NNN III GGG C RRRR GGG C RRRR
                // HELIX 99 99 LYS F 281 LEU F 293 1
                // NNN III 2 GGG CRRRR GGG CRRRR
                // SHEET 1 A 8 ILE A 43 ASP A 45 0
                // NNN III GGG CRRRR GGG CRRRR
                // TURN 1 T1 PRO A 41 TYR A 44
                switch (type) {
                case HELIX:
                  nx = ++nHelix;
                  if (sid == null || pdbFileMode)
                    sid = TextFormat.formatString("%3N %3N", "N", nx);
                  str = "HELIX  %ID %3GROUPA %1CA %4RESA  %3GROUPB %1CB %4RESB";
                  sb = sbHelix;
                  String stype = null;
                  switch (subtype) {
                  case HELIX:
                  case HELIXALPHA:
                    stype = "  1";
                    break;
                  case HELIX310:
                    stype = "  5";
                    break;
                  case HELIXPI:
                    stype = "  3";
                    break;
                  }
                  if (stype != null)
                    str += stype;
                  break;
                case SHEET:
                  nx = ++nSheet;
                  if (sid == null || pdbFileMode) {
                    sid = TextFormat.formatString("%3N %3A 0", "N", nx);
                    sid = TextFormat.formatString(sid, "A", "S" + nx);
                  }
                  str = "SHEET  %ID %3GROUPA %1CA%4RESA  %3GROUPB %1CB%4RESB";
                  sb = sbSheet;
                  break;
                case TURN:
                default:
                  nx = ++nTurn;
                  if (sid == null || pdbFileMode)
                    sid = TextFormat.formatString("%3N %3N", "N", nx);
                  str = "TURN   %ID %3GROUPA %1CA%4RESA  %3GROUPB %1CB%4RESB";
                  sb = sbTurn;
                  break;
                }
                str = TextFormat.formatString(str, "ID", sid);
                str = TextFormat.formatString(str, "GROUPA", group1);
                str = TextFormat.formatString(str, "CA", chain1);
                str = TextFormat.formatString(str, "RESA", res1);
                str = TextFormat.formatString(str, "GROUPB", group2);
                str = TextFormat.formatString(str, "CB", chain2);
                str = TextFormat.formatString(str, "RESB", res2);
                sb.append(str);
                if (showMode)
                  sb.append(" strucno= ").append(lastId);
                sb.append("\n");

                /*
                 * HELIX 1 H1 ILE 7 PRO 19 1 3/10 CONFORMATION RES 17,19 1CRN 55
                 * HELIX 2 H2 GLU 23 THR 30 1 DISTORTED 3/10 AT RES 30 1CRN 56
                 * SHEET 1 S1 2 THR 1 CYS 4 0 1CRNA 4 SHEET 2 S1 2 CYS 32 ILE 35
                 */
              }
            }
            bs = null;
          }
          if (id == 0
              || bsAtoms != null
              && needPhiPsi
              && (Float.isNaN(atoms[i].getGroupParameter(Token.phi)) || Float
                  .isNaN(atoms[i].getGroupParameter(Token.psi))))
            continue;
        }
        char ch = atoms[i].getChainID();
        if (ch == 0)
          ch = ' ';
        if (bs == null) {
          bs = new BitSet();
          res1 = atoms[i].getResno();
          group1 = atoms[i].getGroup3(false);
          chain1 = "" + ch;
        }
        type = atoms[i].getProteinStructureType();
        subtype = atoms[i].getProteinStructureSubType();
        sid = atoms[i].getProteinStructureTag();
        bs.set(i);
        lastId = id;
        res2 = atoms[i].getResno();
        group2 = atoms[i].getGroup3(false);
        chain2 = "" + ch;
        iLastAtom = i;
      }
    if (n > 0)
      cmd.append("\n");
    return (scriptMode ? cmd.toString() : sbHelix.append(sbSheet).append(
        sbTurn).append(cmd).toString());
  }

  private final static String[] pdbRecords = { "ATOM  ", "MODEL ", "HETATM" };

  @Override
  public String getFullPDBHeader() {
    if (modelIndex < 0)
      return "";
    String info = (String) auxiliaryInfo.get("fileHeader");
    if (info != null)
      return info;
    info = modelSet.viewer.getCurrentFileAsString();
    int ichMin = info.length();
    for (int i = pdbRecords.length; --i >= 0;) {
      int ichFound;
      String strRecord = pdbRecords[i];
      switch (ichFound = (info.startsWith(strRecord) ? 0 : info.indexOf("\n"
          + strRecord))) {
      case -1:
        continue;
      case 0:
        auxiliaryInfo.put("fileHeader", "");
        return "";
      default:
        if (ichFound < ichMin)
          ichMin = ++ichFound;
      }
    }
    info = info.substring(0, ichMin);
    auxiliaryInfo.put("fileHeader", info);
    return info;
  }

  @Override
  public void getPdbData(Viewer viewer, String type, char ctype,
                         boolean isDraw, BitSet bsSelected,
                         OutputStringBuffer sb, LabelToken[] tokens, StringBuffer pdbCONECT, BitSet bsWritten) {
    boolean bothEnds = false;
    char qtype = (ctype != 'R' ? 'r' : type.length() > 13
        && type.indexOf("ramachandran ") >= 0 ? type.charAt(13) : 'R');
    if (qtype == 'r')
      qtype = viewer.getQuaternionFrame();
    int mStep = viewer.getHelixStep();
    int derivType = (type.indexOf("diff") < 0 ? 0 : type.indexOf("2") < 0 ? 1
        : 2);
    if (!isDraw) {
      sb.append("REMARK   6 Jmol PDB-encoded data: " + type + ";");
      if (ctype != 'R') {
        sb.append("  quaternionFrame = \"" + qtype + "\"");
        bothEnds = true; //???
      }
      sb.append("\nREMARK   6 Jmol Version ").append(Viewer.getJmolVersion())
          .append('\n');
      if (ctype == 'R')
        sb
            .append("REMARK   6 Jmol data min = {-180 -180 -180} max = {180 180 180} "
                + "unScaledXyz = xyz * {1 1 1} + {0 0 0} plotScale = {100 100 100}\n");
      else
        sb
            .append("REMARK   6 Jmol data min = {-1 -1 -1} max = {1 1 1} "
                + "unScaledXyz = xyz * {0.1 0.1 0.1} + {0 0 0} plotScale = {100 100 100}\n");
    }
    
    for (int p = 0; p < bioPolymerCount; p++)
      bioPolymers[p].getPdbData(viewer, ctype, qtype, mStep, derivType,
          bsAtoms, bsSelected, bothEnds, isDraw, p == 0, tokens, sb, 
          pdbCONECT, bsWritten);
  }

}
