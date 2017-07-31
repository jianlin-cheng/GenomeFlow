/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-09-11 19:29:26 -0500 (Tue, 11 Sep 2012) $
 * $Revision: 17556 $

 *
 * Copyright (C) 2003-2005  The Jmol Development Team
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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Point4f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import org.jmol.api.Interface;
import org.jmol.api.SymmetryInterface;
import org.jmol.atomdata.RadiusData;
import org.jmol.modelset.Bond.BondSet;
import org.jmol.script.Token;
import org.jmol.shape.Shape;
import org.jmol.util.BitSetUtil;
import org.jmol.util.Escape;
import org.jmol.util.JmolEdge;
import org.jmol.util.JmolMolecule;
import org.jmol.util.Measure;
import org.jmol.util.Quaternion;
import org.jmol.viewer.JmolConstants;
import org.jmol.viewer.Viewer;

/*
 * An abstract class always created using new ModelLoader(...)
 * 
 * Merged with methods in Mmset and ModelManager 10/2007  Jmol 11.3.32
 * 
 * ModelLoader simply pulls out all private classes that are
 * necessary only for file loading (and structure recalculation).
 * 
 * What is left here are all the methods that are 
 * necessary AFTER a model is loaded, when it is being 
 * accessed by Viewer, primarily.
 * 
 * Please:
 * 
 * 1) designate any methods used only here as private
 * 2) designate any methods accessed only by ModelLoader as protected
 * 3) designate any methods accessed within modelset as nothing
 * 4) designate any methods accessed only by Viewer as public
 * 
 * Bob Hanson, 5/2007, 10/2007
 * 
 */
 public class ModelSet extends ModelCollection {

  ////////////////////////////////////////////////////////////////

  public ModelSet(Viewer viewer, String name) {
    this.viewer = viewer;
    modelSetName = name;

  }

  @Override
  protected void releaseModelSet() {
    models = null;
    closest[0] = null;
    super.releaseModelSet();
  }

  //variables that will be reset when a new frame is instantiated

  private boolean selectionHaloEnabled = false;
  private boolean echoShapeActive = false;
 
  
  public void setSelectionHaloEnabled(boolean selectionHaloEnabled) {
    this.selectionHaloEnabled = selectionHaloEnabled;
  }

  public boolean getSelectionHaloEnabled() {
    return selectionHaloEnabled;
  }

  public boolean getEchoStateActive() {
    return echoShapeActive;
  }

  public void setEchoStateActive(boolean TF) {
    echoShapeActive = TF;
  }
  
  //added -hcf
  public int chrScaleNumber = 0;
  public int lociScaleNumber = 0;
  public int fiberScaleNumber = 0;
  public int nucleoScaleNumber = 0;
  public int currentScale = 0;
  public void setScaleNumber(int i, int[] selectedPath) {
	  currentScale = i;
	  chrScaleNumber = selectedPath[1];
	  lociScaleNumber = selectedPath[2];
	  fiberScaleNumber = selectedPath[3];
	  nucleoScaleNumber = selectedPath[4];
  }
  //added end -hcf
  
  
  
  
  
  
  
  
  
  

  protected String modelSetTypeName;

  public String getModelSetTypeName() {
    return modelSetTypeName;
  }

  public int getModelNumberIndex(int modelNumber, boolean useModelNumber,
                                 boolean doSetTrajectory) {
    if (useModelNumber) {
      for (int i = 0; i < modelCount; i++)
        if (modelNumbers[i] == modelNumber 
            || modelNumber < 1000000 && modelNumbers[i] == 1000000 + modelNumber)
          return i;
      return -1;
    }
    //new decimal format:   frame 1.2 1.3 1.4
    for (int i = 0; i < modelCount; i++)
      if (modelFileNumbers[i] == modelNumber) {
        if (doSetTrajectory && isTrajectory(i))
          setTrajectory(i);
        return i;
      }
    return -1;
  }

  public String getTrajectoryInfo() {
    String s = "";
    if (trajectorySteps == null)
      return "";
    for (int i = modelCount; --i >= 0; )
      if (models[i].selectedTrajectory >= 0) {
        s = " or " + getModelNumberDotted(models[i].selectedTrajectory) + s;
        i = models[i].trajectoryBaseIndex; //skip other trajectories
      }
    if (s.length() > 0)
      s = "set trajectory {" + s.substring(4) + "}"; 
    return s;
  }

  public BitSet getBitSetTrajectories() {
    if (trajectorySteps == null)
      return null;
    BitSet bsModels = new BitSet();
    for (int i = modelCount; --i >= 0;)
      if (models[i].selectedTrajectory >= 0) {
        bsModels.set(models[i].selectedTrajectory);
        i = models[i].trajectoryBaseIndex; //skip other trajectories
      }
    return bsModels;
  }

  public void setTrajectory(BitSet bsModels) {
    for (int i = 0; i < modelCount; i++)
      if (bsModels.get(i))
        setTrajectory(i);
  }

  public void setTrajectory(int modelIndex) {
    if (modelIndex < 0 || !isTrajectory(modelIndex))
      return;
    // The user has used the MODEL command to switch to a new set of atom coordinates
    // Or has specified a trajectory in a select, display, or hide command.

    // Assign the coordinates and the model index for this set of atoms
    if (atoms[models[modelIndex].firstAtomIndex].modelIndex == modelIndex)
      return;
    int baseModelIndex = models[modelIndex].trajectoryBaseIndex;
    models[baseModelIndex].selectedTrajectory = modelIndex;
    setAtomPositions(baseModelIndex, modelIndex, trajectorySteps.get(modelIndex), 
        (vibrationSteps == null ? null : vibrationSteps.get(modelIndex)), true);    
    int m = viewer.getCurrentModelIndex();
    if (m >= 0 && m != modelIndex 
        && models[m].fileIndex == models[modelIndex].fileIndex)
      viewer.setCurrentModelIndex(modelIndex, false);
  }  

  /**
   * A generic way to set atom positions, possibly from trajectories but also
   * possibly from an array. Takes care of all associated issues of changing
   * coordinates.
   * 
   * @param baseModelIndex
   * @param modelIndex
   * @param trajectory
   * @param vibrations
   * @param isFractional
   */
  private void setAtomPositions(int baseModelIndex, int modelIndex,
                                Point3f[] trajectory, Vector3f[] vibrations,
                                boolean isFractional) {
    BitSet bs = new BitSet();
    Vector3f vib = new Vector3f();
    int iFirst = models[baseModelIndex].firstAtomIndex;
    int iMax = iFirst + getAtomCountInModel(baseModelIndex);
    for (int pt = 0, i = iFirst; i < iMax && pt < trajectory.length
        && trajectory[pt] != null; i++, pt++) {
      if (isFractional)
        atoms[i].setFractionalCoord(trajectory[pt], true);
      else
        atoms[i].set(trajectory[pt]);
      atoms[i].modelIndex = (short) modelIndex;
      if (vibrationSteps != null) {
        if (vibrations != null && vibrations[pt] != null)
          vib = vibrations[pt];
        setVibrationVector(i, vib.x, vib.y, vib.z);
      }
      bs.set(i);
    }
    // Clear the Binary Search so that select within(),
    // isosurface, and dots will work properly
    initializeBspf();
    validateBspf(baseModelIndex, false);
    // Recalculate critical points for cartoons and such
    // note that models[baseModel] and models[modelIndex]
    // point to the same model. So there is only one copy of 
    // the shape business.

    recalculateLeadMidpointsAndWingVectors(baseModelIndex);
    // Recalculate all measures that involve trajectories

    shapeManager.refreshShapeTrajectories(baseModelIndex, bs, null);

    if (models[baseModelIndex].hasRasmolHBonds) {
      models[baseModelIndex].clearRasmolHydrogenBonds(null);
      models[baseModelIndex].getRasmolHydrogenBonds(bs, bs, null, false,
          Integer.MAX_VALUE, false, null);
    }
  }

  public Point3f[] getFrameOffsets(BitSet bsAtoms) {
    if (bsAtoms == null)
      return null;
    Point3f[] offsets = new Point3f[modelCount];
    for (int i = 0; i < modelCount; i++)
      offsets[i] = new Point3f();
    int lastModel = 0;
    int n = 0;
    Point3f offset = offsets[0];
    boolean asTrajectory = (trajectorySteps != null && trajectorySteps.size() == modelCount);
    int m1 = (asTrajectory ? modelCount : 1);
    for (int m = 0; m < m1; m++) {
      if (asTrajectory)
        setTrajectory(m);
      for (int i = 0; i <= atomCount; i++) {
        if (i == atomCount || atoms[i].modelIndex != lastModel) {
          if (n > 0) {
            offset.scale(-1.0f / n);
            if (lastModel != 0)
              offset.sub(offsets[0]);
            n = 0;
          }
          if (i == atomCount)
            break;
          lastModel = atoms[i].modelIndex;
          offset = offsets[lastModel];
        }
        if (!bsAtoms.get(i))
          continue;
        offset.add(atoms[i]);
        n++;
      }
    }
    offsets[0].set(0, 0, 0);
    return offsets;
  }

  /**
   * general lookup for integer type -- from Eval
   * @param tokType   
   * @param specInfo  
   * @return bitset; null only if we mess up with name
   */
  @Override
  public BitSet getAtomBits(int tokType, Object specInfo) {
    switch (tokType) {
    case Token.spec_model:
      int modelNumber = ((Integer) specInfo).intValue();
      int modelIndex = getModelNumberIndex(modelNumber, true, true);
      return (modelIndex < 0 && modelNumber > 0 ? new BitSet()
          : viewer.getModelUndeletedAtomsBitSet(modelIndex));
    }
    return super.getAtomBits(tokType, specInfo);
  }

  public String getAtomLabel(int i) {
    return (String) viewer.getShapeProperty(JmolConstants.SHAPE_LABELS, "label", i);
  }
  
  protected final Atom[] closest = new Atom[1];

  public int findNearestAtomIndex(int x, int y, BitSet bsNot) {
    if (atomCount == 0)
      return -1;
    closest[0] = null;
    if (g3d.isAntialiased()) {
      x <<= 1;
      y <<= 1;
    }
    findNearestAtomIndex(x, y, closest, bsNot);
    shapeManager.findNearestShapeAtomIndex(x, y, closest, bsNot);
    int closestIndex = (closest[0] == null ? -1 : closest[0].index);
    closest[0] = null;
    return closestIndex;
  }

  /*
  private Map userProperties;

  void putUserProperty(String name, Object property) {
    if (userProperties == null)
      userProperties = new Hashtable();
    if (property == null)
      userProperties.remove(name);
    else
      userProperties.put(name, property);
  }
*/  

  ///////// atom and shape selecting /////////

  public String calculateStructures(BitSet bsAtoms, boolean asDSSP,
                                    boolean dsspIgnoreHydrogen,
                                    boolean setStructure) {
    BitSet bsAllAtoms = new BitSet();
    BitSet bsModelsExcluded = BitSetUtil.copyInvert(modelsOf(bsAtoms, bsAllAtoms),
        modelCount);
    if (!setStructure)
      return calculateStructuresAllExcept(bsModelsExcluded, asDSSP, true,
          dsspIgnoreHydrogen, false, false);
    for (int i = 0; i < modelCount; i++)
      if (!bsModelsExcluded.get(i))
        models[i].clearBioPolymers();
    calculatePolymers(null, 0, 0, bsModelsExcluded);
    String ret = calculateStructuresAllExcept(bsModelsExcluded, asDSSP, true,
        dsspIgnoreHydrogen, true, false);
    viewer.resetBioshapes(bsAllAtoms);
    setStructureIds();
    return ret;
  }

  public String calculatePointGroup(BitSet bsAtoms) {
    return (String) calculatePointGroupForFirstModel(bsAtoms, false,
        false, false, null, 0, 0);
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> getPointGroupInfo(BitSet bsAtoms) {
    return (Map<String, Object>) calculatePointGroupForFirstModel(bsAtoms, false,
        false, true, null, 0, 0);
  }
  
  public String getPointGroupAsString(BitSet bsAtoms, boolean asDraw,
                                      String type, int index, float scale) {
    return (String) calculatePointGroupForFirstModel(bsAtoms, true,
        asDraw, false, type, index, scale);
  }

  private SymmetryInterface pointGroup;
  private Object calculatePointGroupForFirstModel(BitSet bsAtoms,
                                                  boolean doAll,
                                                  boolean asDraw,
                                                  boolean asInfo, String type,
                                                  int index, float scale) {
    int modelIndex = viewer.getCurrentModelIndex();
    int iAtom = (bsAtoms == null ? -1 : bsAtoms.nextSetBit(0));
    if (modelIndex < 0 && iAtom >= 0)
      modelIndex = atoms[iAtom].getModelIndex();
    if (modelIndex < 0) {
      modelIndex = viewer.getVisibleFramesBitSet().nextSetBit(0);
      bsAtoms = null;
    }
    BitSet bs = viewer.getModelUndeletedAtomsBitSet(modelIndex);
    if (bsAtoms != null)
      bs.and(bsAtoms);
    iAtom = bs.nextSetBit(0);
    if (iAtom < 0) {
      bs = viewer.getModelUndeletedAtomsBitSet(modelIndex);
      iAtom = bs.nextSetBit(0);
    }
    Object obj = viewer.getShapeProperty(JmolConstants.SHAPE_VECTORS, "mad", iAtom);
    boolean haveVibration = (obj != null && ((Integer) obj).intValue() != 0 || viewer
        .isVibrationOn());
    SymmetryInterface symmetry = (SymmetryInterface) Interface
        .getOptionInterface("symmetry.Symmetry");
    pointGroup = symmetry.setPointGroup(pointGroup, atoms, bs, haveVibration,
        viewer.getPointGroupTolerance(0), viewer.getPointGroupTolerance(1));
    if (!doAll && !asInfo)
      return pointGroup.getPointGroupName();
    Object ret = pointGroup.getPointGroupInfo(modelIndex, asDraw, asInfo, type,
        index, scale);
    if (asInfo)
      return ret;
    return (modelCount > 1 ? "frame " + getModelNumberDotted(modelIndex) + "; "
        : "") + ret;
  }

  private BitSet modelsOf(BitSet bsAtoms, BitSet bsAllAtoms) {
    BitSet bsModels = new BitSet(modelCount);
    boolean isAll = (bsAtoms == null);
    int i0 = (isAll ? atomCount - 1 : bsAtoms.nextSetBit(0));
    for (int i = i0; i >= 0; i = (isAll ? i - 1 : bsAtoms.nextSetBit(i + 1))) {
      int modelIndex = models[atoms[i].modelIndex].trajectoryBaseIndex;
      if (isJmolDataFrame(modelIndex))
        continue;
      bsModels.set(modelIndex);
      bsAllAtoms.set(i);
    }
    return bsModels;
  }

  public String getDefaultStructure(BitSet bsAtoms, BitSet bsAllAtoms) {
    BitSet bsModels = modelsOf(bsAtoms, bsAllAtoms);
    StringBuffer ret = new StringBuffer();
    for (int i = bsModels.nextSetBit(0); i >= 0; i = bsModels.nextSetBit(i + 1)) 
      if (models[i].isBioModel && models[i].defaultStructure != null)
        ret.append(models[i].defaultStructure);
    return ret.toString();
  }


  
  ///// super-overloaded methods ///////
  
  
  protected void assignAromaticBonds(boolean isUserCalculation) {
    super.assignAromaticBonds(isUserCalculation, null);
    // send a message to STICKS indicating that these bonds
    // should be part of the state of the model. They will 
    // appear in the state as bondOrder commands.
    
    if (isUserCalculation)
      shapeManager.setShapeSize(JmolConstants.SHAPE_STICKS, Integer.MIN_VALUE, null, bsAromatic);
  }

  @Override
  public int[] makeConnections(float minDistance, float maxDistance, int order,
                               int connectOperation, BitSet bsA, BitSet bsB,
                               BitSet bsBonds, boolean isBonds, boolean addGroup, float energy) {
    if (connectOperation == Token.auto
        && order != JmolEdge.BOND_H_REGULAR) {
      String stateScript = "connect ";
      if (minDistance != JmolConstants.DEFAULT_MIN_CONNECT_DISTANCE)
        stateScript += minDistance + " ";
      if (maxDistance != JmolConstants.DEFAULT_MAX_CONNECT_DISTANCE)
        stateScript += maxDistance + " ";
      addStateScript(stateScript, (isBonds ? bsA : null),
          (isBonds ? null : bsA), (isBonds ? null : bsB), " auto", false, true);
    }
    moleculeCount = 0;
    return super.makeConnections(minDistance, maxDistance, order,
        connectOperation, bsA, bsB, bsBonds, isBonds, addGroup, energy);
  }
  
  @SuppressWarnings("unchecked")
  public void setPdbConectBonding(int baseAtomIndex, int baseModelIndex,
                                  BitSet bsExclude) {
    short mad = viewer.getMadBond();
    for (int i = baseModelIndex; i < modelCount; i++) {
      List<int[]> vConnect = (List<int[]>) getModelAuxiliaryInfo(i, "PDB_CONECT_bonds");
      if (vConnect == null)
        continue;
      int nConnect = vConnect.size();
      setModelAuxiliaryInfo(i, "initialBondCount", Integer.valueOf(nConnect));
      int[] atomInfo = (int[]) getModelAuxiliaryInfo(i, "PDB_CONECT_firstAtom_count_max");
      int firstAtom = atomInfo[0] +  baseAtomIndex;
      int atomMax = firstAtom + atomInfo[1];
      int max = atomInfo[2];
      int[] serialMap = new int[max + 1];
      int iSerial;
      for (int iAtom = firstAtom; iAtom < atomMax; iAtom++)
        if ((iSerial = atomSerials[iAtom]) > 0)
          serialMap[iSerial] = iAtom + 1;
      for (int iConnect = 0; iConnect < nConnect; iConnect++) {
        int[] pair = vConnect.get(iConnect);
        int sourceSerial = pair[0];
        int targetSerial = pair[1];
        short order = (short) pair[2];
        if (sourceSerial < 0 || targetSerial < 0 || sourceSerial > max
            || targetSerial > max)
          continue;
        int sourceIndex = serialMap[sourceSerial] - 1;
        int targetIndex = serialMap[targetSerial] - 1;
        if (sourceIndex < 0 || targetIndex < 0)
          continue;
        if (bsExclude != null) {
          if (atoms[sourceIndex].isHetero())
            bsExclude.set(sourceIndex);
          if (atoms[targetIndex].isHetero())
            bsExclude.set(targetIndex);
        }
        checkValencesAndBond(atoms[sourceIndex], atoms[targetIndex], order,
            (order == JmolEdge.BOND_H_REGULAR ? 1 : mad), null);
      }
    }
  }
  
  @Override
  public void deleteAllBonds() {
    moleculeCount = 0;
    for (int i = stateScripts.size(); --i >= 0;) { 
      if (stateScripts.get(i).isConnect()) {
        stateScripts.remove(i);
      }
    }
    super.deleteAllBonds();
  }

  /* ******************************************************
   * 
   * methods for definining the state 
   * 
   ********************************************************/

  public String getDefinedState(StringBuffer sfunc, boolean isAll) {
    int len = stateScripts.size();
    if (len == 0)
      return "";
    
    boolean haveDefs = false;    
    StringBuffer commands = new StringBuffer();
    String cmd;
    for (int i = 0; i < len; i++) {
      StateScript ss = stateScripts.get(i); 
      if (ss.inDefinedStateBlock && (cmd = ss.toString()).length() > 0) {
        commands.append("  ").append(cmd).append("\n");
        haveDefs = true;
      }
    }

    if (!haveDefs)
      return "";
    cmd = "";
    if (isAll && sfunc != null) {
      sfunc.append("  _setDefinedState;\n");
      cmd = "function _setDefinedState() {\n\n";
    }

    if (sfunc != null)
      commands.append("\n}\n\n");
    return cmd + commands.toString();
  }
  
  public String getState(StringBuffer sfunc, boolean isAll,
                         boolean withProteinStructure) {
    StringBuffer commands = new StringBuffer();
    if (isAll && sfunc != null) {
      sfunc.append("  _setModelState;\n");
      commands.append("function _setModelState() {\n");
    }
    String cmd;

    // connections

    if (isAll) {

      int len = stateScripts.size();
      for (int i = 0; i < len; i++) {
        StateScript ss = stateScripts.get(i);
        if (!ss.inDefinedStateBlock && (cmd = ss.toString()).length() > 0) {
          commands.append("  ").append(cmd).append("\n");
        }
      }

      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < bondCount; i++)
        if (!models[bonds[i].atom1.modelIndex].isModelKit)
          if (bonds[i].isHydrogen()
              || (bonds[i].order & JmolEdge.BOND_NEW) != 0) {
            Bond bond = bonds[i];
            int index = bond.atom1.index;
            if (bond.atom1.getGroup().isAdded(index))
              index = -1 - index;
            sb.append(index).append('\t').append(bond.atom2.index).append('\t')
                .append(bond.order & ~JmolEdge.BOND_NEW).append('\t').append(
                    bond.mad / 1000f).append('\t').append(bond.getEnergy())
                .append('\t').append(
                    JmolEdge.getBondOrderNameFromOrder(bond.order)).append(
                    ";\n");
          }
      if (sb.length() > 0)
        commands.append("data \"connect_atoms\"\n").append(sb).append(
            "end \"connect_atoms\";\n");
      commands.append("\n");
    }

    // bond visibility

    if (haveHiddenBonds) {
      BondSet bs = new BondSet();
      for (int i = bondCount; --i >= 0;)
        if (bonds[i].mad != 0
            && (bonds[i].shapeVisibilityFlags & Bond.myVisibilityFlag) == 0)
          bs.set(i);
      if (bs.isEmpty())
        haveHiddenBonds = false;
      else
        commands.append("  hide ").append(Escape.escapeBs(bs, false)).append(
            ";\n");
    }

    // shape construction

    viewer.setModelVisibility();

    // unnecessary. Removed in 11.5.35 -- oops!

    if (withProteinStructure)
      commands.append(getProteinStructureState(null, isAll, false, 0));

    viewer.getShapeState(commands, isAll, Integer.MAX_VALUE);

    if (isAll) {
      boolean needOrientations = false;
      for (int i = 0; i < modelCount; i++)
        if (models[i].isJmolDataFrame) {
          needOrientations = true;
          break;
        }
      for (int i = 0; i < modelCount; i++) {
        String fcmd = "  frame " + getModelNumberDotted(i);
        String s = (String) getModelAuxiliaryInfo(i, "modelID");
        if (s != null && !s.equals(getModelAuxiliaryInfo(i, "modelID0")))
          commands.append(fcmd).append("; frame ID ").append(Escape.escapeStr(s))
              .append(";\n");
        String t = frameTitles[i];
        if (t != null && t.length() > 0)
          commands.append(fcmd).append("; frame title ").append(
              Escape.escapeStr(t)).append(";\n");
        if (needOrientations && models[i].orientation != null
            && !isTrajectorySubFrame(i))
          commands.append(fcmd).append("; ").append(
              models[i].orientation.getMoveToText(false)).append(";\n");
        if (models[i].frameDelay != 0 && !isTrajectorySubFrame(i))
          commands.append(fcmd).append("; frame delay ").append(
              models[i].frameDelay / 1000f).append(";\n");
        if (models[i].unitCell != null) {
          commands.append(fcmd).append("; unitcell ").append(
              Escape.escape(models[i].unitCell.getUnitCellVectors()))
              .append(";\n");
          viewer.getShapeState(commands, isAll, JmolConstants.SHAPE_UCCAGE);
        }
      }

      if (unitCells != null) {
        for (int i = 0; i < modelCount; i++) {
          SymmetryInterface symmetry = getUnitCell(i);
          if (symmetry == null)
            continue;
          commands.append("  frame ").append(getModelNumberDotted(i));
          Point3f pt = symmetry.getFractionalOffset();
          if (pt != null)
            commands.append("; set unitcell ").append(Escape.escapePt(pt));
          pt = symmetry.getUnitCellMultiplier();
          if (pt != null)
            commands.append("; set unitcell ").append(Escape.escapePt(pt));
          commands.append(";\n");
        }
        viewer.getShapeState(commands, isAll, JmolConstants.SHAPE_UCCAGE);
//        if (viewer.getObjectMad(StateManager.OBJ_UNITCELL) == 0)
  //        commands.append("  unitcell OFF;\n");
      }
      commands.append("  set fontScaling " + viewer.getFontScaling() + ";\n");
      if (viewer.isModelKitMode())
        commands.append("  set modelKitMode true;\n");
    }
    if (sfunc != null)
      commands.append("\n}\n\n");
    return commands.toString();
  }

  private void includeAllRelatedFrames(BitSet bsModels) {
    int j;
    for (int i = 0; i < modelCount; i++) {
      if (bsModels.get(i)) {
       // if (isJmolDataFrame(i) && !bsModels.get(j = models[i].dataSourceFrame)) {
         // bsModels.set(j);
        //  includeAllRelatedFrames(bsModels);
          //return;
       // }
        if (isTrajectory(i) && !bsModels.get(j = models[i].trajectoryBaseIndex)) {
          bsModels.set(j);
          includeAllRelatedFrames(bsModels);
          return;
        }
        continue;
      }
      if (isTrajectory(i) && bsModels.get(models[i].trajectoryBaseIndex)
          || isJmolDataFrame(i) && bsModels.get(models[i].dataSourceFrame))
        bsModels.set(i);
    }
  }
  
  public BitSet deleteModels(BitSet bsAtoms) {
    // full models are deleted for any model containing the specified atoms
    moleculeCount = 0;
    BitSet bsModels = getModelBitSet(bsAtoms, false);
    includeAllRelatedFrames(bsModels);
    int nAtomsDeleted = 0;

    int nModelsDeleted = BitSetUtil.cardinalityOf(bsModels);
    if (nModelsDeleted == 0)
      return null;

    // clear references to this frame if it is a dataFrame

    for (int i = bsModels.nextSetBit(0); i >= 0; i = bsModels.nextSetBit(i + 1))
      clearDataFrameReference(i);

    BitSet bsDeleted;
    if (nModelsDeleted == modelCount) {
      bsDeleted = getModelAtomBitSetIncludingDeleted(-1, true);
      viewer.zap(true, false, false);
      return bsDeleted;
    }

    // zero out reproducible arrays

    validateBspf(false);

    // create a new models array,
    // and pre-calculate Model.bsAtoms and Model.atomCount
    Model[] newModels = new Model[modelCount - nModelsDeleted];
    Model[] oldModels = models;
    bsDeleted = new BitSet();
    for (int i = 0, mpt = 0; i < modelCount; i++)
      if (bsModels.get(i)) { // get a good count now
        getAtomCountInModel(i);
        bsDeleted.or(getModelAtomBitSetIncludingDeleted(i, false));
      } else {
        models[i].modelIndex = mpt;
        newModels[mpt++] = models[i];
      }
    models = newModels;
    int oldModelCount = modelCount;
    // delete bonds
    BitSet bsBonds = getBondsForSelectedAtoms(bsDeleted, true);
    deleteBonds(bsBonds, true);

    // main deletion cycle
    
    for (int i = 0, mpt = 0; i < oldModelCount; i++) {
      if (!bsModels.get(i)) {
        mpt++;
        continue;
      }
      int nAtoms = oldModels[i].atomCount;
      if (nAtoms == 0)
        continue;
      nAtomsDeleted += nAtoms;
      BitSet bs = oldModels[i].bsAtoms;
      int firstAtomIndex = oldModels[i].firstAtomIndex;

      // delete from symmetry set
      BitSetUtil.deleteBits(bsSymmetry, bs);

      // delete from stateScripts, model arrays and bitsets,
      // atom arrays, and atom bitsets
      deleteModel(mpt, firstAtomIndex, nAtoms, bs, bsBonds);

      // adjust all models after this one
      for (int j = oldModelCount; --j > i;)
        oldModels[j].fixIndices(mpt, nAtoms, bs);

      // adjust all shapes
      viewer.deleteShapeAtoms(new Object[] { newModels, atoms,
          new int[] { mpt, firstAtomIndex, nAtoms } }, bs);
      modelCount--;
    }

    // set final values
    deleteModel(-1, 0, 0, null, null);
    return bsDeleted;
  }

  @Override
  public void setAtomProperty(BitSet bs, int tok, int iValue, float fValue,
                              String sValue, float[] values, String[] list) {
    switch (tok) {
    case Token.backbone:
    case Token.cartoon:
    case Token.meshRibbon:
    case Token.ribbon:
    case Token.rocket:
    case Token.strands:
    case Token.trace:
      if (fValue > Shape.RADIUS_MAX)
        fValue = Shape.RADIUS_MAX;
      //$FALL-THROUGH$
    case Token.halo:
    case Token.star:
      RadiusData rd = null;
      int mar = 0;
      if (values == null) {
        if (fValue > Atom.RADIUS_MAX)
          fValue = Atom.RADIUS_MAX;
        if (fValue < 0)
          fValue = 0;
        mar = (int) (fValue * 2000);
      } else {
        rd = new RadiusData(values);
      }
      shapeManager
          .setShapeSize(JmolConstants.shapeTokenIndex(tok), mar, rd, bs);
      return;
    }
    super.setAtomProperty(bs, tok, iValue, fValue, sValue, values, list);
  }
  
  @SuppressWarnings("unchecked")
  public Object getFileData(int modelIndex) {
    if (modelIndex < 0)
      return "";
    Map<String, Object> fileData = (Map<String, Object>) getModelAuxiliaryInfo(modelIndex, "fileData");
    if (fileData != null)
      return fileData;
    if (!getModelAuxiliaryInfoBoolean(modelIndex, "isCIF"))
      return getPDBHeader(modelIndex);
    fileData = viewer.getCifData(modelIndex);
    setModelAuxiliaryInfo(modelIndex, "fileData", fileData);
    return fileData;
  }
  
  /** see comments in org.jmol.modelsetbio.AlphaPolymer.java
   * 
   * Struts are calculated for atoms in bs1 connecting to atoms in bs2.
   * The two bitsets may overlap. 
   * 
   * @param bs1
   * @param bs2
   * @return     number of struts found
   */
  @Override
  public int calculateStruts(BitSet bs1, BitSet bs2) {
    viewer.setModelVisibility();
    return super.calculateStruts(bs1, bs2);
  }

  /**
   * these are hydrogens that are being added due to a load 2D command and are
   * therefore not to be flagged as NEW
   * 
   * @param vConnections
   * @param pts
   * @return            BitSet of new atoms
   */
  public BitSet addHydrogens(List<Atom> vConnections, Point3f[] pts) {
    int modelIndex = modelCount - 1;
    BitSet bs = new BitSet();
    if (isTrajectory(modelIndex) || models[modelIndex].getGroupCount() > 1) {
      return bs; // can't add atoms to a trajectory or a system with multiple groups!
    }
    growAtomArrays(atomCount + pts.length);
    RadiusData rd = viewer.getDefaultRadiusData();
    short mad = getDefaultMadFromOrder(1);
    for (int i = 0, n = models[modelIndex].atomCount + 1; i < vConnections.size(); i++, n++) {
      Atom atom1 = vConnections.get(i);
      // hmm. atom1.group will not be expanded, though...
      // something like within(group,...) will not select these atoms!
      Atom atom2 = addAtom(modelIndex, atom1.group, (short) 1, "H"
          + n, n, n, pts[i].x, pts[i].y, pts[i].z);
      atom2.setMadAtom(viewer, rd);
      bs.set(atom2.index);
      bondAtoms(atom1, atom2, JmolEdge.BOND_COVALENT_SINGLE, mad, null, 0, false, false);
    }
    // must reset the shapes to give them new atom counts and arrays
    shapeManager.loadDefaultShapes(this);
    return bs;
  }

  public void setAtomCoordRelative(Tuple3f offset, BitSet bs) {
    setAtomCoordRelative(bs, offset.x, offset.y, offset.z);
    mat4.setIdentity();
    vTemp.set(offset);
    mat4.setTranslation(vTemp);
    recalculatePositionDependentQuantities(bs, mat4);
  }

  @Override
  public void setAtomCoord(BitSet bs, int tokType, Object xyzValues) {
    super.setAtomCoord(bs, tokType, xyzValues);
    switch(tokType) {
    case Token.vibx:
    case Token.viby:
    case Token.vibz:
    case Token.vibxyz:
      break;
    default:
      recalculatePositionDependentQuantities(bs, null);
    }
  }

  public void invertSelected(Point3f pt, Point4f plane, int iAtom,
                             BitSet invAtoms, BitSet bs) {
    if (pt != null) {
      for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
        float x = (pt.x - atoms[i].x) * 2;
        float y = (pt.y - atoms[i].y) * 2;
        float z = (pt.z - atoms[i].z) * 2;
        setAtomCoordRelative(i, x, y, z);
      }
      return;
    }
    if (plane != null) {
      // ax + by + cz + d = 0
      Vector3f norm = new Vector3f(plane.x, plane.y, plane.z);
      norm.normalize();
      float d = (float) Math.sqrt(plane.x * plane.x + plane.y * plane.y
          + plane.z * plane.z);
      for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
        float twoD = -Measure.distanceToPlane(plane, d, atoms[i]) * 2;
        float x = norm.x * twoD;
        float y = norm.y * twoD;
        float z = norm.z * twoD;
        setAtomCoordRelative(i, x, y, z);
      }
      return;
    }
    if (iAtom >= 0) {
      Atom thisAtom = atoms[iAtom];
      // stereochemical inversion at iAtom
      Bond[] bonds = thisAtom.bonds;
      if (bonds == null)
        return;
      BitSet bsAtoms = new BitSet();
      List<Point3f> vNot = new ArrayList<Point3f>();
      BitSet bsModel = viewer.getModelUndeletedAtomsBitSet(thisAtom.modelIndex);
      for (int i = 0; i < bonds.length; i++) {
        Atom a = bonds[i].getOtherAtom(thisAtom);
        if (invAtoms.get(a.index)) {
            bsAtoms.or(JmolMolecule.getBranchBitSet(atoms, a.index, bsModel, null, iAtom, true, true));
        } else {
          vNot.add(a);
        }
      }
      if (vNot.size() == 0)
        return;
      pt = Measure.getCenterAndPoints(vNot)[0];
      Vector3f v = new Vector3f(thisAtom);
      v.sub(pt);
      Quaternion q = new Quaternion(v, 180);
      moveAtoms(null, q.getMatrix(), null, bsAtoms, thisAtom, true);
    }
  }

  private final Matrix3f matTemp = new Matrix3f();
  private final Matrix3f matInv = new Matrix3f();
  private final Matrix4f mat4 = new Matrix4f();
  private final Matrix4f mat4t = new Matrix4f();
  private final Vector3f vTemp = new Vector3f();

  public void moveAtoms(Matrix3f mNew, Matrix3f matrixRotate,
                        Vector3f translation, BitSet bs, Point3f center,
                        boolean isInternal) {
    if (mNew == null) {
      matTemp.set(matrixRotate);
    } else {
      matInv.set(matrixRotate);
      matInv.invert();
      ptTemp.set(0, 0, 0);
      matTemp.mul(mNew, matrixRotate);
      matTemp.mul(matInv, matTemp);
    }
    if (isInternal) {
      vTemp.set(center);
      mat4.setIdentity();
      mat4.setTranslation(vTemp);
      mat4t.set(matTemp);
      mat4.mul(mat4t);
      mat4t.setIdentity();
      vTemp.scale(-1);
      mat4t.setTranslation(vTemp);
      mat4.mul(mat4t);
    } else {
      mat4.set(matTemp);
    }
    for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
      if (isInternal) {
        mat4.transform(atoms[i]);
      } else {
        ptTemp.add(atoms[i]);
        mat4.transform(atoms[i]);
        ptTemp.sub(atoms[i]);
      }
      taint(i, TAINT_COORD);
    }
    if (!isInternal) {
      ptTemp.scale(1f / bs.cardinality());
      if (translation == null)
        translation = new Vector3f();
      translation.add(ptTemp);
    }
    if (translation != null) {
      for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1))
        atoms[i].add(translation);
      mat4t.setIdentity();
      mat4t.setTranslation(translation);
      mat4.mul(mat4t, mat4);
    }
    recalculatePositionDependentQuantities(bs, mat4);
  }

  
  /*
 
 static {

    Point3f pt = new Point3f(-1, 2, 3);
    Point3f center = new Point3f(.2f,.4f,.5f);
    Matrix3f matTemp = (new Quaternion(.2f, .3f, .4f, .5f)).getMatrix();
    
    Matrix4f mat4 = new Matrix4f();
    Matrix4f mat4t = new Matrix4f();
    Vector3f vTemp = new Vector3f();

    vTemp.set(center);
    mat4.setIdentity();
    mat4.setTranslation(vTemp);
    mat4t.set(matTemp);
    mat4.mul(mat4t);
    mat4t.setIdentity();
    vTemp.scale(-1);
    mat4t.setTranslation(vTemp);
    mat4.mul(mat4t);

    Point3f pt1 = new Point3f(pt);
    System.out.println(pt);    
    pt1.sub(center);
    matTemp.transform(pt1);
    pt1.add(center);
    System.out.println(pt1);
    //mat4.transform(pt);

    vTemp.set(2,3,4);
    pt1.set(pt);
    mat4.transform(pt1);
    pt1.add(vTemp);
    System.out.println(pt1);
    
    
    mat4t.setIdentity();
    mat4t.setTranslation(vTemp);
    mat4.mul(mat4t, mat4);
    pt1.set(pt);
    mat4.transform(pt1);
    System.out.println(pt1);
    
    
    
    
    // mat4 == (1) rot, then (2) trans
    
    System.out.println("HHH MODELSET");
  }
*/
  public void recalculatePositionDependentQuantities(BitSet bs, Matrix4f mat) {
    if (getHaveStraightness())
      calculateStraightness();
    recalculateLeadMidpointsAndWingVectors(-1);
    BitSet bsModels = getModelBitSet(bs, false);
    for (int i = bsModels.nextSetBit(0); i >= 0; i = bsModels.nextSetBit(i + 1))
      shapeManager.refreshShapeTrajectories(i, bs, mat);
    /* but we would need to somehow indicate this in the state
    if (ellipsoids != null)
      for (int i = bs.nextSetBit(0); i >= 0 && i < ellipsoids.length; i = bs.nextSetBit(i + 1))
        ellipsoids[i].rotate(mat);
    if (vibrationVectors != null)
      for (int i = bs.nextSetBit(0); i >= 0 && i < vibrationVectors.length; i = bs.nextSetBit(i + 1))
        if (vibrationVectors[i] != null)
            mat.transform(vibrationVectors[i]);
            */
  }

}

