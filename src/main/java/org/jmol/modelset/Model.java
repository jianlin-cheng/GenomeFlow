/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-05-28 18:58:52 -0500 (Mon, 28 May 2012) $
 * $Revision: 17227 $
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
package org.jmol.modelset;

import java.util.BitSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.vecmath.Point3f;

import org.jmol.api.SymmetryInterface;
import org.jmol.constant.EnumStructure;
import org.jmol.util.ArrayUtil;
import org.jmol.util.BitSetUtil;
import org.jmol.util.OutputStringBuffer;
import org.jmol.viewer.StateManager.Orientation;
import org.jmol.viewer.Viewer;

public class Model {

  /*
   * In Jmol all atoms and bonds are kept as a set of arrays in 
   * the AtomCollection and BondCollection objects. 
   * Thus, "Model" is not atoms and bonds. 
   * It is a description of all the:
   * 
   * chains (as defined in the file)
   *   and their associated file-associated groups,  
   * polymers (same, I think, but in terms of secondary structure)
   *   and their associated monomers
   * molecules (as defined by connectivity)
   *  
   * A Model then is just a small set of fields, a few arrays pointing
   * to other objects, and a couple of hash tables for information storage
   * 
   * Additional information here includes
   * how many atoms there were before symmetry was applied
   * as well as a bit about insertions and alternative locations.
   * 
   * 
   * one model = one animation "frame", but we don't use the "f" word
   * here because that would confuse the issue.
   * 
   * If multiple files are loaded, then they will appear here in 
   * at least as many Model objects. Each vibration will be a complete
   * set of atoms as well.
   * 
   * Jmol 11.3.58 developed the trajectory idea -- where
   * multiple models may share the same structures, bonds, etc., but
   * just differ in atom positions, saved in the Trajectories Vector
   * in ModelCollection.
   *  
   */
  
  public ModelSet modelSet;
 
  /**
   * BE CAREFUL: FAILURE TO NULL REFERENCES TO modelSet WILL PREVENT FINALIZATION
   * AND CREATE A MEMORY LEAK.
   * 
   * @return associated ModelSet
   */
  public ModelSet getModelSet() {
    return modelSet;
  }

  public int modelIndex;   // our 0-based reference
  int fileIndex;   // 0-based file reference

  public int hydrogenCount;
  public boolean isBioModel;
  public boolean isPdbWithMultipleBonds;
  public int trajectoryBaseIndex;
  protected boolean hasRasmolHBonds;
  
  String loadState = "";
  StringBuffer loadScript = new StringBuffer();

  boolean isModelKit;
  public boolean isModelkit() {
    return isModelKit;
  }
  
  boolean isTrajectory;
  int selectedTrajectory = -1;
  
  Map<String, Integer> dataFrames;
  int dataSourceFrame = -1;
  String jmolData; // from a PDB remark "Jmol PDB-encoded data"
  String jmolFrameType;
  
  // set in ModelLoader phase:
  protected int firstAtomIndex;  
  protected int atomCount = 0; // includes deleted atoms
  protected final BitSet bsAtoms = new BitSet();
  final BitSet bsAtomsDeleted = new BitSet();
  // this one is variable and calculated only if necessary:
  public int getTrueAtomCount() {
    return bsAtoms.cardinality() - bsAtomsDeleted.cardinality();
  }
  
  private int bondCount = -1;

  public void resetBoundCount() {
    bondCount = -1;    
  }
  
  protected int getBondCount() {
    if (bondCount >= 0)
      return bondCount;
    Bond[] bonds = modelSet.getBonds();
    bondCount = 0;
    for (int i = modelSet.getBondCount(); --i >= 0;)
      if (bonds[i].atom1.modelIndex == modelIndex)
        bondCount++;
    return bondCount;
  }
  
  int firstMoleculeIndex;
  int moleculeCount;
  
  public int nAltLocs;
  int nInsertions;
  
  int groupCount = -1;

  protected int chainCount = 0;
  protected Chain[] chains = new Chain[8];

  int biosymmetryCount;

  protected Map<String, Object> auxiliaryInfo;
  Properties properties;
  float defaultRotationRadius;
  String defaultStructure;

  Orientation orientation;

  public Model(ModelSet modelSet, int modelIndex, int trajectoryBaseIndex, 
      String jmolData, Properties properties, Map<String, Object> auxiliaryInfo) {
    this.modelSet = modelSet;
    dataSourceFrame = this.modelIndex = modelIndex;
    isTrajectory = (trajectoryBaseIndex >= 0);
    this.trajectoryBaseIndex = (isTrajectory ? trajectoryBaseIndex : modelIndex);
    if (auxiliaryInfo == null) {
      auxiliaryInfo = new Hashtable<String, Object>();
    }
    this.auxiliaryInfo = auxiliaryInfo;
    if (auxiliaryInfo.containsKey("biosymmetryCount"))
      biosymmetryCount = ((Integer)auxiliaryInfo.get("biosymmetryCount")).intValue();
    this.properties = properties;
    if (jmolData == null) {
      jmolFrameType = "modelSet";
    } else {
      this.jmolData = jmolData;
      isJmolDataFrame = true;
      auxiliaryInfo.put("jmolData", jmolData);
      auxiliaryInfo.put("title", jmolData);
      jmolFrameType = (jmolData.indexOf("ramachandran") >= 0 ? "ramachandran"
          : jmolData.indexOf("quaternion") >= 0 ? "quaternion" 
          : "data");
    }
  }

  void setNAltLocs(int nAltLocs) {
    this.nAltLocs = nAltLocs;  
  }
  
  void setNInsertions(int nInsertions) {
    this.nInsertions = nInsertions;  
  }
  
  protected boolean structureTainted;
  boolean isJmolDataFrame;
  public long frameDelay;
  public SymmetryInterface unitCell;
  
  public String getModelNumberDotted() {
    return modelSet.getModelNumberDotted(modelIndex);
  }

  public String getModelTitle() {
    return modelSet.getModelTitle(modelIndex);
  }
    
  public boolean isStructureTainted() {
    return structureTainted;
  }
  
  public Chain[] getChains() {
    return chains;
  }

  public int getChainCount(boolean countWater) {
    if (chainCount > 1 && !countWater)
      for (int i = 0; i < chainCount; i++)
        if (chains[i].chainID == '\0')
          return chainCount - 1;
    return chainCount;
  }

  public int getGroupCount(boolean isHetero) {
    int n = 0;
    for (int i = chainCount; --i >= 0;)
      for (int j = chains[i].groupCount; --j >= 0;)
        if (chains[i].groups[j].isHetero() == isHetero)
          n++;
    return n;
  }
  
  void calcSelectedGroupsCount(BitSet bsSelected) {
    for (int i = chainCount; --i >= 0; )
      chains[i].calcSelectedGroupsCount(bsSelected);
  }

  int getGroupCount() {
    if (groupCount < 0) {
      groupCount = 0;
      for (int i = chainCount; --i >= 0;)
        groupCount += chains[i].getGroupCount();
    }
    return groupCount;
  }

  Chain getChain(int i) {
    return (i < chainCount ? chains[i] : null);
  }

  Chain getChain(char chainID) {
    for (int i = chainCount; --i >= 0; ) {
      Chain chain = chains[i];
      if (chain.chainID == chainID)
        return chain;
    }
    return null;
  }

  public void fixIndices(int modelIndex, int nAtomsDeleted, BitSet bsDeleted) {
    if (dataSourceFrame > modelIndex)
      dataSourceFrame--;
    if (trajectoryBaseIndex > modelIndex)
      trajectoryBaseIndex--;
    firstAtomIndex -= nAtomsDeleted;
    for (int i = 0; i < chainCount; i++)
      chains[i].fixIndices(nAtomsDeleted, bsDeleted);
    BitSetUtil.deleteBits(bsAtoms, bsDeleted);
    BitSetUtil.deleteBits(bsAtomsDeleted, bsDeleted);
  }

  public void freeze() {
    chains = (Chain[])ArrayUtil.setLength(chains, chainCount);
    groupCount = -1;
    getGroupCount();      
    for (int i = 0; i < chainCount; ++i)
      chains[i].groups = (Group[])ArrayUtil.setLength(chains[i].groups, chains[i].groupCount);
  }


  /////// BioModel only ///////
  
  /**
   * @param viewer  
   * @param type 
   * @param ctype 
   * @param isDraw 
   * @param bsSelected 
   * @param sb 
   * @param bsWritten 
   * @param pdbCONECT 
   * @param tokens 
   */
  public void getPdbData(Viewer viewer, String type, char ctype,
                         boolean isDraw, BitSet bsSelected,
                         OutputStringBuffer sb, LabelToken[] tokens, StringBuffer pdbCONECT, BitSet bsWritten) {
  }
  
  /**
   * @param sb  
   * @param maxAtoms 
   */
  public void getDefaultLargePDBRendering(StringBuffer sb, int maxAtoms) {
  }
  
  /**
   * @param bioBranches 
   * @return  updated bioBranches 
   */
  public List<BitSet> getBioBranches(List<BitSet> bioBranches) {
    return bioBranches;
  }

  /**
   * @param nResidues  
   * @param bs 
   * @param bsResult 
   */
  public void getGroupsWithin(int nResidues, BitSet bs, BitSet bsResult) {
  }

  /**
   * @param specInfo  
   * @param bs 
   * @param bsResult 
   */
  public void getSequenceBits(String specInfo, BitSet bs, BitSet bsResult) {
  }

  /**
   * @param bsA  
   * @param bsB 
   * @param vHBonds 
   * @param nucleicOnly 
   * @param nMax 
   * @param dsspIgnoreHydrogens 
   * @param bsHBonds 
   */
  public void getRasmolHydrogenBonds(BitSet bsA, BitSet bsB,
                                     List<Bond> vHBonds, boolean nucleicOnly,
                                     int nMax, boolean dsspIgnoreHydrogens,
                                     BitSet bsHBonds) {
  }

  /**
   * @param bsAtoms   
   */
  public void clearRasmolHydrogenBonds(BitSet bsAtoms) {
  }


  public void clearBioPolymers() {
  }

  /**
   * @param bsSelected  
   */
  public void calcSelectedMonomersCount(BitSet bsSelected) {
    // BioModel only
  }

  /**
   * @param groups  
   * @param groupCount 
   * @param baseGroupIndex 
   * @param modelsExcluded 
   */
  public void calculatePolymers(Group[] groups, int groupCount,
                                int baseGroupIndex, BitSet modelsExcluded) {
  }

  /**
   * @param bs  
   * @param finalInfo 
   * @param modelVector 
   */
  public void getAllPolymerInfo(
                                BitSet bs,
                                Map<String, List<Map<String, Object>>> finalInfo,
                                List<Map<String, Object>> modelVector) {
  }

  public int getBioPolymerCount() {
    return 0;
  }

  /**
   * @param bs  
   * @param vList 
   * @param isTraceAlpha 
   * @param sheetSmoothing 
   */
  public void getPolymerPointsAndVectors(BitSet bs, List<Point3f[]> vList,
                                         boolean isTraceAlpha,
                                         float sheetSmoothing) {
  }

  /**
   * @param iPolymer  
   * @return list of points or null
   */
  public Point3f[] getPolymerLeadMidPoints(int iPolymer) {
    return null;
  }

  public void recalculateLeadMidpointsAndWingVectors() {
  }

  /**
   * 
   * @param type
   * @param structureID
   * @param serialID
   * @param strandCount
   * @param startChainID
   * @param startSeqcode
   * @param endChainID
   * @param endSeqcode
   */
  public void addSecondaryStructure(EnumStructure type, 
                             String structureID, int serialID, int strandCount,
                             char startChainID, int startSeqcode,
                             char endChainID, int endSeqcode) {
    
  }

  /**
   * @param asDSSP  
   * @param doReport 
   * @param dsspIgnoreHydrogen 
   * @param setStructure 
   * @param includeAlpha 
   * @return structure list
   */
  public String calculateStructures(boolean asDSSP, boolean doReport, 
                             boolean dsspIgnoreHydrogen, boolean setStructure,
                             boolean includeAlpha) {
    return "";
  }

  /**
   * @param structureList  
   */
  public void setStructureList(Map<EnumStructure, float[]> structureList) {
  }

  public void getChimeInfo(StringBuffer sb, int nHetero) {
    sb.append("\nNumber of Atoms ..... " + (modelSet.atomCount - nHetero));
    if (nHetero > 0)
      sb.append(" (" + nHetero + ")");
    sb.append("\nNumber of Bonds ..... " + modelSet.bondCount);
    sb.append("\nNumber of Models ...... " + modelSet.modelCount);
  }

  /**
   * @param modelSet  
   * @param bs1 
   * @param bs2 
   * @return number of struts
   */
  public int calculateStruts(ModelSet modelSet, BitSet bs1, BitSet bs2) {
    return 0;
  }

  /**
   * @param viewer  
   * @param ctype 
   * @param qtype 
   * @param mStep 
   */
  public void calculateStraightness(Viewer viewer, char ctype, char qtype,
                                    int mStep) {
  }

  /**
   * @param seqcodeA  
   * @param seqcodeB 
   * @param chainID 
   * @param bs 
   * @param caseSensitive 
   */
  public void selectSeqcodeRange(int seqcodeA, int seqcodeB, char chainID, BitSet bs,
                                 boolean caseSensitive) {
  }

  /**
   * @param bsConformation  
   */
  public void setConformation(BitSet bsConformation) {
    //
  }

  /**
   * @param bsConformation  
   * @param conformationIndex 
   * @return true for BioModel
   */
  public boolean getPdbConformation(BitSet bsConformation, int conformationIndex) {
    return false;
  }

  /**
   * @param bsAtoms  
   * @param taintedOnly 
   * @param needPhiPsi 
   * @param mode 
   * @return     only for BioModel
   */
  public String getProteinStructureState(BitSet bsAtoms, boolean taintedOnly,
                                         boolean needPhiPsi, int mode) {
    return null;
  }

  public String getFullPDBHeader() {
    return null;
  }

}
