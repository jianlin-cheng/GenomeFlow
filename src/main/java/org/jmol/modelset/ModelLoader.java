/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2007-05-18 08:19:45 -0500 (Fri, 18 May 2007) $
 * $Revision: 7742 $

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
import java.util.Arrays;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.jmol.api.Interface;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolAdapterAtomIterator;
import org.jmol.api.JmolAdapterBondIterator;
import org.jmol.api.JmolAdapterStructureIterator;
import org.jmol.api.JmolBioResolver;
import org.jmol.api.SymmetryInterface;
import org.jmol.atomdata.RadiusData;
import org.jmol.constant.EnumStructure;
import org.jmol.constant.EnumVdw;
import org.jmol.script.Token;
import org.jmol.util.ArrayUtil;
import org.jmol.util.BitSetUtil;
import org.jmol.util.Elements;
import org.jmol.util.JmolEdge;
import org.jmol.util.JmolMolecule;
import org.jmol.util.Logger;
import org.jmol.util.Quadric;
import org.jmol.util.Quaternion;
import org.jmol.util.TextFormat;
import org.jmol.viewer.JmolConstants;
import org.jmol.viewer.Viewer;

/* 
 * 
 * This class contains only the private methods 
 * used to load a model. Methods exclusively after 
 * file loading are included only in ModelSet,
 * and its superclasses, ModelCollection, BondCollection, and AtomCollection.
 * 
 * Bob Hanson, 5/2007; refactored 7/2011
 *  
 */

public final class ModelLoader {
  
  //public void finalize() {
  //  System.out.println("ModelLoader " + this + " finalized");
  //}
  
  private Viewer viewer;
  private ModelSet modelSet;
  private ModelSet mergeModelSet;

  private boolean merging;

  private String jmolData; // from a PDB remark "Jmol PDB-encoded data"
  private String[] group3Lists;
  private int[][] group3Counts;
  private final int[] specialAtomIndexes = new int[JmolConstants.ATOMID_MAX];
  
  public ModelLoader(Viewer viewer, String name) {
    this.viewer = viewer;
    modelSet = new ModelSet(viewer, name);
    viewer.resetShapes(false);
    modelSet.preserveState = viewer.getPreserveState();
    initializeInfo(name, null);
    createModelSet(null, null, null);
    viewer.setStringProperty("_fileType", "");
  }

  
//added -hcf
  public ModelLoader(Viewer viewer, StringBuffer loadScript,
	      Object atomSetCollection, ModelSet mergeModelSet, String modelSetName,
	      BitSet bsNew, int currentSelectedScale, int[] selectedPath, boolean addAtomInfo) {
	    this.viewer = viewer;
	    modelSet = new ModelSet(viewer, modelSetName);
	    this.mergeModelSet = mergeModelSet;
	    JmolAdapter adapter = viewer.getModelAdapter();
	    merging = (mergeModelSet != null && mergeModelSet.atomCount > 0);
	    if (merging) {
	      modelSet.canSkipLoad = false;
	    } else {
	      viewer.resetShapes(false);
	    }
	    modelSet.preserveState = viewer.getPreserveState();
	    if (!modelSet.preserveState)
	      modelSet.canSkipLoad = false;
	    Map<String, Object> info = adapter.getAtomSetCollectionAuxiliaryInfo(atomSetCollection);
	    info.put("loadScript", loadScript);
	    initializeInfo(adapter.getFileTypeName(atomSetCollection).toLowerCase().intern(), info);
	    if (addAtomInfo == true)
	    	createModelSet(adapter, atomSetCollection, bsNew, currentSelectedScale, selectedPath, addAtomInfo);
	  }
  
//added end -hcf
  
  
  
  
  
  public ModelLoader(Viewer viewer, StringBuffer loadScript,
      Object atomSetCollection, ModelSet mergeModelSet, String modelSetName,
      BitSet bsNew) {
    this.viewer = viewer;
    modelSet = new ModelSet(viewer, modelSetName);
    this.mergeModelSet = mergeModelSet;
    JmolAdapter adapter = viewer.getModelAdapter();
    merging = (mergeModelSet != null && mergeModelSet.atomCount > 0);
    if (merging) {
      modelSet.canSkipLoad = false;
    } else {
      viewer.resetShapes(false);
    }
    modelSet.preserveState = viewer.getPreserveState();
    if (!modelSet.preserveState)
      modelSet.canSkipLoad = false;
    Map<String, Object> info = adapter.getAtomSetCollectionAuxiliaryInfo(atomSetCollection);
    info.put("loadScript", loadScript);
    initializeInfo(adapter.getFileTypeName(atomSetCollection).toLowerCase().intern(), info);
    createModelSet(adapter, atomSetCollection, bsNew);
    // dumpAtomSetNameDiagnostics(adapter, atomSetCollection);
  }
/*
  private void dumpAtomSetNameDiagnostics(JmolAdapter adapter, Object atomSetCollection) {
    int frameModelCount = modelCount;
    int adapterAtomSetCount = adapter.getAtomSetCount(atomSetCollection);
    if (Logger.debugging) {
      Logger.debug(
          "----------------\n" + "debugging of AtomSetName stuff\n" +
          "\nframeModelCount=" + frameModelCount +
          "\nadapterAtomSetCount=" + adapterAtomSetCount + "\n -- \n");
      for (int i = 0; i < adapterAtomSetCount; ++i) {
        Logger.debug(
            "atomSetName[" + i + "]=" + adapter.getAtomSetName(atomSetCollection, i) +
            " atomSetNumber[" + i + "]=" + adapter.getAtomSetNumber(atomSetCollection, i));
      }
    }
  }
*/

  private boolean someModelsHaveUnitcells;
  private boolean isTrajectory; 
  private boolean doMinimize;
  private boolean doAddHydrogens;
  private boolean doRemoveAddedHydrogens;
  private String fileHeader;
  private JmolBioResolver jbr;
  private boolean isPDB;
  private Group[] groups;
  private int groupCount;
  

  @SuppressWarnings("unchecked")
  private void initializeInfo(String name, Map<String, Object> info) {
    modelSet.g3d = viewer.getGraphicsData();
    //long timeBegin = System.currentTimeMillis();
    modelSet.modelSetTypeName = name;
    modelSet.isXYZ = (name == "xyz");
    modelSet.modelSetAuxiliaryInfo = info;
    modelSet.modelSetProperties = (Properties) modelSet
        .getModelSetAuxiliaryInfo("properties");
    //isMultiFile = getModelSetAuxiliaryInfoBoolean("isMultiFile"); -- no longer necessary
    isPDB = modelSet.isPDB = modelSet.getModelSetAuxiliaryInfoBoolean("isPDB");
    if (isPDB) {
      try {
        Class<?> shapeClass = Class.forName("org.jmol.modelsetbio.Resolver");
        jbr = (JmolBioResolver) shapeClass.newInstance();
        jbr.initialize(modelSet);
      } catch (Exception e) {
        Logger
            .error("developer error: org.jmol.modelsetbio.Resolver could not be found");
      }
    }
    jmolData = (String) modelSet.getModelSetAuxiliaryInfo("jmolData");
    fileHeader = (String) modelSet.getModelSetAuxiliaryInfo("fileHeader");
    modelSet.trajectorySteps = (List<Point3f[]>) modelSet
        .getModelSetAuxiliaryInfo("trajectorySteps");
    isTrajectory = (modelSet.trajectorySteps != null);
    if (isTrajectory) {
      info.remove("trajectorySteps");
      modelSet.vibrationSteps = (List<Vector3f[]>) info.get("vibrationSteps");
      info.remove("vibrationSteps");
    }
    doAddHydrogens = (jbr != null && !isTrajectory
        && modelSet.getModelSetAuxiliaryInfo("pdbNoHydrogens") == null
        && viewer.getBooleanProperty("pdbAddHydrogens"));
    if (info != null)
      info.remove("pdbNoHydrogens");
    noAutoBond = modelSet.getModelSetAuxiliaryInfoBoolean("noAutoBond");
    is2D = modelSet.getModelSetAuxiliaryInfoBoolean("is2D");
    doMinimize = is2D && modelSet.getModelSetAuxiliaryInfoBoolean("doMinimize");
    adapterTrajectoryCount = (modelSet.trajectorySteps == null ? 0
        : modelSet.trajectorySteps.size());
    modelSet.someModelsHaveSymmetry = modelSet
        .getModelSetAuxiliaryInfoBoolean("someModelsHaveSymmetry");
    someModelsHaveUnitcells = modelSet
        .getModelSetAuxiliaryInfoBoolean("someModelsHaveUnitcells");
    modelSet.someModelsHaveFractionalCoordinates = modelSet
        .getModelSetAuxiliaryInfoBoolean("someModelsHaveFractionalCoordinates");
    if (merging) {
      modelSet.isPDB |= mergeModelSet.isPDB;
      modelSet.someModelsHaveSymmetry |= mergeModelSet
          .getModelSetAuxiliaryInfoBoolean("someModelsHaveSymmetry");
      someModelsHaveUnitcells |= mergeModelSet
          .getModelSetAuxiliaryInfoBoolean("someModelsHaveUnitcells");
      modelSet.someModelsHaveFractionalCoordinates |= mergeModelSet
          .getModelSetAuxiliaryInfoBoolean("someModelsHaveFractionalCoordinates");
      modelSet.someModelsHaveAromaticBonds |= mergeModelSet.someModelsHaveAromaticBonds;
      modelSet.modelSetAuxiliaryInfo.put("someModelsHaveSymmetry", Boolean
          .valueOf(modelSet.someModelsHaveSymmetry));
      modelSet.modelSetAuxiliaryInfo.put("someModelsHaveUnitcells", Boolean
          .valueOf(someModelsHaveUnitcells));
      modelSet.modelSetAuxiliaryInfo.put("someModelsHaveFractionalCoordinates",
          Boolean.valueOf(modelSet.someModelsHaveFractionalCoordinates));
      modelSet.modelSetAuxiliaryInfo.put("someModelsHaveAromaticBonds", Boolean
          .valueOf(modelSet.someModelsHaveAromaticBonds));
    }
  }

  private final Map<Object, Atom> htAtomMap = new Hashtable<Object, Atom>();

  private final static int defaultGroupCount = 32;
  private Chain[] chainOf;

  private String[] group3Of;
  public String getGroup3(int iGroup) {
    return (iGroup >= group3Of.length ? null : group3Of[iGroup]);
  }

  private int[] seqcodes;
  private int[] firstAtomIndexes;

  public int getFirstAtomIndex(int iGroup) {
    return firstAtomIndexes[iGroup];
  }
  
  private int currentModelIndex;
  private Model currentModel;
  private char currentChainID;
  private Chain currentChain;
  private int currentGroupSequenceNumber;
  private char currentGroupInsertionCode;
  private String currentGroup3;

  private Group nullGroup; // used in Atom

  private int baseModelIndex = 0;
  private int baseModelCount = 0;
  private int baseAtomIndex = 0;
  private int baseGroupIndex = 0;

  private int baseTrajectoryCount = 0;
  private boolean appendNew;
  private int adapterModelCount = 0;
  private int adapterTrajectoryCount = 0;
  private boolean noAutoBond;
  private boolean is2D;
  
  public ModelSet getModelSet() {
    return modelSet;
  }

  public int getAtomCount() {
    return modelSet.atomCount;
  }
//added -hcf

  private void createModelSet(JmolAdapter adapter, Object atomSetCollection,
                              BitSet bsNew, int currentSelectedScale, int[] selectedPath, boolean addAtomInfo) {
    int nAtoms = (adapter == null ? 0 : adapter.getAtomCount(atomSetCollection));
    if (nAtoms > 0)
      Logger.info("reading " + nAtoms + " atoms");
    adapterModelCount = (adapter == null ? 1 : adapter
        .getAtomSetCount(atomSetCollection));
    // cannot append a trajectory into a previous model
    appendNew = (!merging || adapter == null || adapterModelCount > 1
        || isTrajectory || viewer.getAppendNew());
    htAtomMap.clear();
    chainOf = new Chain[defaultGroupCount];
    group3Of = new String[defaultGroupCount];
    seqcodes = new int[defaultGroupCount];
    firstAtomIndexes = new int[defaultGroupCount];
    currentChainID = '\uFFFF';
    currentChain = null;
    currentGroupInsertionCode = '\uFFFF';
    currentGroup3 = "xxxxx";
    currentModelIndex = -1;
    currentModel = null;
    if (merging) {
      baseModelCount = mergeModelSet.modelCount;
      baseTrajectoryCount = mergeModelSet.getMergeTrajectoryCount(isTrajectory);
      if (baseTrajectoryCount > 0) {
        if (isTrajectory) {
          if (mergeModelSet.vibrationSteps == null) {
            mergeModelSet.vibrationSteps = new ArrayList<Vector3f[]>();
            for (int i = mergeModelSet.trajectorySteps.size(); --i >= 0; )
              mergeModelSet.vibrationSteps.add(null);
          }
          for (int i = 0; i < modelSet.trajectorySteps.size(); i++) {
            mergeModelSet.trajectorySteps.add(modelSet.trajectorySteps.get(i));
            mergeModelSet.vibrationSteps.add(modelSet.vibrationSteps == null ? null  : modelSet.vibrationSteps.get(i));
          }
        }
        modelSet.trajectorySteps = mergeModelSet.trajectorySteps;
        modelSet.vibrationSteps = mergeModelSet.vibrationSteps;
      }
    }
    initializeAtomBondModelCounts(nAtoms);
    if (bsNew != null && (doMinimize || is2D)) {
      bsNew.set(baseAtomIndex, baseAtomIndex + nAtoms);
    }
    if (adapter == null) {
      setModelNameNumberProperties(0, -1, "", 1, null, null, null);
    } else {
      if (adapterModelCount > 0) {
        Logger.info("ModelSet: haveSymmetry:" + modelSet.someModelsHaveSymmetry
            + " haveUnitcells:" + someModelsHaveUnitcells
            + " haveFractionalCoord:" + modelSet.someModelsHaveFractionalCoordinates);
        Logger.info(adapterModelCount + " model" + (modelSet.modelCount == 1 ? "" : "s")
            + " in this collection. Use getProperty \"modelInfo\" or"
            + " getProperty \"auxiliaryInfo\" to inspect them.");
      }
      Quaternion q = (Quaternion) modelSet.getModelSetAuxiliaryInfo("defaultOrientationQuaternion");
      if (q != null) {
        Logger.info("defaultOrientationQuaternion = " + q);
        Logger
            .info("Use \"set autoLoadOrientation TRUE\" before loading or \"restore orientation DEFAULT\" after loading to view this orientation.");
      }
      //add the scale information - hcf
      addScaleInfo(currentSelectedScale, selectedPath);      
      //added end -hcf
      iterateOverAllNewModels(adapter, atomSetCollection);
      if (addAtomInfo == true)
    		  iterateOverAllNewAtoms(adapter, atomSetCollection, addAtomInfo);
      iterateOverAllNewBonds(adapter, atomSetCollection);
      if (merging && !appendNew) {
        Map<String, Object> info = adapter.getAtomSetAuxiliaryInfo(
            atomSetCollection, 0);
        modelSet.setModelAuxiliaryInfo(baseModelIndex, "initialAtomCount", info
            .get("initialAtomCount"));
        modelSet.setModelAuxiliaryInfo(baseModelIndex, "initialBondCount", info
            .get("initialBondCount"));
      }
      initializeUnitCellAndSymmetry();
      initializeBonding();
    }

    finalizeGroupBuild(); // set group offsets and build monomers

    // only now can we access all of the atom's properties

    if (is2D) {
      applyStereochemistry();
    }

    if (doAddHydrogens)
      jbr.finalizeHydrogens();

    if (adapter != null) {
      modelSet.calculatePolymers(groups, groupCount, baseGroupIndex, null);
      iterateOverAllNewStructures(adapter, atomSetCollection);
      adapter.finish(atomSetCollection);
    }

    setDefaultRendering(viewer.getSmallMoleculeMaxAtoms());

    RadiusData rd = viewer.getDefaultRadiusData();
    int atomCount = modelSet.atomCount;
    Atom[] atoms = modelSet.atoms;
    for (int i = baseAtomIndex; i < atomCount; i++)
      atoms[i].setMadAtom(viewer, rd);
    Model[] models = modelSet.models;
    for (int i = models[baseModelIndex].firstAtomIndex; i < atomCount; i++)
      models[atoms[i].modelIndex].bsAtoms.set(i);
    setAtomProperties();
    freeze();
    finalizeShapes();
    if (mergeModelSet != null) {
      mergeModelSet.releaseModelSet();
    }
    mergeModelSet = null;
  }

  private void addScaleInfo (int currentSelectedScale, int[] selectedPath) {
	  int i = currentSelectedScale;
	  modelSet.setScaleNumber(i, selectedPath);
  }
  //added end -hcf
  
  
  
  
  private void createModelSet(JmolAdapter adapter, Object atomSetCollection,
                              BitSet bsNew) {
    int nAtoms = (adapter == null ? 0 : adapter.getAtomCount(atomSetCollection));
    if (nAtoms > 0)
      Logger.info("reading " + nAtoms + " atoms");
    adapterModelCount = (adapter == null ? 1 : adapter
        .getAtomSetCount(atomSetCollection));
    // cannot append a trajectory into a previous model
    appendNew = (!merging || adapter == null || adapterModelCount > 1
        || isTrajectory || viewer.getAppendNew());
    htAtomMap.clear();
    chainOf = new Chain[defaultGroupCount];
    group3Of = new String[defaultGroupCount];
    seqcodes = new int[defaultGroupCount];
    firstAtomIndexes = new int[defaultGroupCount];
    currentChainID = '\uFFFF';
    currentChain = null;
    currentGroupInsertionCode = '\uFFFF';
    currentGroup3 = "xxxxx";
    currentModelIndex = -1;
    currentModel = null;
    if (merging) {
      baseModelCount = mergeModelSet.modelCount;
      baseTrajectoryCount = mergeModelSet.getMergeTrajectoryCount(isTrajectory);
      if (baseTrajectoryCount > 0) {
        if (isTrajectory) {
          if (mergeModelSet.vibrationSteps == null) {
            mergeModelSet.vibrationSteps = new ArrayList<Vector3f[]>();
            for (int i = mergeModelSet.trajectorySteps.size(); --i >= 0; )
              mergeModelSet.vibrationSteps.add(null);
          }
          for (int i = 0; i < modelSet.trajectorySteps.size(); i++) {
            mergeModelSet.trajectorySteps.add(modelSet.trajectorySteps.get(i));
            mergeModelSet.vibrationSteps.add(modelSet.vibrationSteps == null ? null  : modelSet.vibrationSteps.get(i));
          }
        }
        modelSet.trajectorySteps = mergeModelSet.trajectorySteps;
        modelSet.vibrationSteps = mergeModelSet.vibrationSteps;
      }
    }
    initializeAtomBondModelCounts(nAtoms);
    if (bsNew != null && (doMinimize || is2D)) {
      bsNew.set(baseAtomIndex, baseAtomIndex + nAtoms);
    }
    if (adapter == null) {
      setModelNameNumberProperties(0, -1, "", 1, null, null, null);
    } else {
      if (adapterModelCount > 0) {
        Logger.info("ModelSet: haveSymmetry:" + modelSet.someModelsHaveSymmetry
            + " haveUnitcells:" + someModelsHaveUnitcells
            + " haveFractionalCoord:" + modelSet.someModelsHaveFractionalCoordinates);
        Logger.info(adapterModelCount + " model" + (modelSet.modelCount == 1 ? "" : "s")
            + " in this collection. Use getProperty \"modelInfo\" or"
            + " getProperty \"auxiliaryInfo\" to inspect them.");
      }
      Quaternion q = (Quaternion) modelSet.getModelSetAuxiliaryInfo("defaultOrientationQuaternion");
      if (q != null) {
        Logger.info("defaultOrientationQuaternion = " + q);
        Logger
            .info("Use \"set autoLoadOrientation TRUE\" before loading or \"restore orientation DEFAULT\" after loading to view this orientation.");
      }
      iterateOverAllNewModels(adapter, atomSetCollection);
      iterateOverAllNewAtoms(adapter, atomSetCollection);
      iterateOverAllNewBonds(adapter, atomSetCollection);
      if (merging && !appendNew) {
        Map<String, Object> info = adapter.getAtomSetAuxiliaryInfo(
            atomSetCollection, 0);
        modelSet.setModelAuxiliaryInfo(baseModelIndex, "initialAtomCount", info
            .get("initialAtomCount"));
        modelSet.setModelAuxiliaryInfo(baseModelIndex, "initialBondCount", info
            .get("initialBondCount"));
      }
      initializeUnitCellAndSymmetry();
      initializeBonding();
    }

    finalizeGroupBuild(); // set group offsets and build monomers

    // only now can we access all of the atom's properties

    if (is2D) {
      applyStereochemistry();
    }

    if (doAddHydrogens)
      jbr.finalizeHydrogens();

    if (adapter != null) {
      modelSet.calculatePolymers(groups, groupCount, baseGroupIndex, null);
      iterateOverAllNewStructures(adapter, atomSetCollection);
      adapter.finish(atomSetCollection);
    }

    setDefaultRendering(viewer.getSmallMoleculeMaxAtoms());

    RadiusData rd = viewer.getDefaultRadiusData();
    int atomCount = modelSet.atomCount;
    Atom[] atoms = modelSet.atoms;
    for (int i = baseAtomIndex; i < atomCount; i++)
      atoms[i].setMadAtom(viewer, rd);
    Model[] models = modelSet.models;
    for (int i = models[baseModelIndex].firstAtomIndex; i < atomCount; i++)
      models[atoms[i].modelIndex].bsAtoms.set(i);

    setAtomProperties();

    freeze();

    finalizeShapes();
    if (mergeModelSet != null) {
      mergeModelSet.releaseModelSet();
    }
    mergeModelSet = null;
  }

  private void setDefaultRendering(int maxAtoms) {
    StringBuffer sb = new StringBuffer();
    int modelCount = modelSet.modelCount;
    Model[] models = modelSet.models;
    for (int i = baseModelIndex; i < modelCount; i++)
      if (models[i].isBioModel)
        models[i].getDefaultLargePDBRendering(sb, maxAtoms);
    if (sb.length() == 0)
      return;
    sb.append("select *;");
    String script = (String) modelSet.getModelSetAuxiliaryInfo("jmolscript");
    if (script == null)
      script = "";
    sb.append(script);
    modelSet.modelSetAuxiliaryInfo.put("jmolscript", sb.toString());
  }

  @SuppressWarnings("unchecked")
  private void setAtomProperties() {
    // Crystal reader, PDB tlsGroup
    int atomIndex = baseAtomIndex;
    int modelAtomCount = 0;
    int modelCount = modelSet.modelCount;
    Model[] models = modelSet.models;
    for (int i = baseModelIndex; i < modelCount; atomIndex += modelAtomCount, i++) {
      modelAtomCount = models[i].bsAtoms.cardinality();
      Map<String, String> atomProperties = (Map<String, String>) modelSet.getModelAuxiliaryInfo(i,
          "atomProperties");
      if (atomProperties == null)
        continue;
      for (Map.Entry<String, String> entry : atomProperties.entrySet()) {
        String key = entry.getKey();
        String value = entry.getValue();
        // no deletions yet...
        BitSet bs = modelSet.getModelAtomBitSetIncludingDeleted(i, true);
        if (doAddHydrogens)
          value = jbr.fixPropertyValue(bs, value);
        key = "property_" + key.toLowerCase();
        Logger.info("creating " + key + " for model " + modelSet.getModelName(i));
        viewer.setData(key, new Object[] { key, value, bs }, modelSet.atomCount, 0,
            0, Integer.MAX_VALUE, 0);
      }
    }
  }

  private Group[] mergeGroups;
  
  private void initializeAtomBondModelCounts(int nAtoms) {
    int trajectoryCount = adapterTrajectoryCount;
    if (merging) {
      if (appendNew) {
        baseModelIndex = baseModelCount;
        modelSet.modelCount = baseModelCount + adapterModelCount;
      } else {
        baseModelIndex = viewer.getCurrentModelIndex();
        if (baseModelIndex < 0)
          baseModelIndex = baseModelCount - 1;
        modelSet.modelCount = baseModelCount;
      }
      modelSet.atomCount = baseAtomIndex = mergeModelSet.atomCount;
      modelSet.bondCount = mergeModelSet.bondCount;
      mergeGroups = mergeModelSet.getGroups();
      groupCount = baseGroupIndex = mergeGroups.length;
      modelSet.mergeModelArrays(mergeModelSet);
      modelSet.growAtomArrays(modelSet.atomCount + nAtoms);
    } else {
      modelSet.modelCount = adapterModelCount;
      modelSet.atomCount = 0;
      modelSet.bondCount = 0;
      modelSet.atoms = new Atom[nAtoms];
      modelSet.bonds = new Bond[250 + nAtoms]; // was "2 *" -- WAY overkill.
    }
    if (doAddHydrogens)
      jbr.initializeHydrogenAddition(this, modelSet.bondCount);
    if (trajectoryCount > 1)
      modelSet.modelCount += trajectoryCount - 1;
    modelSet.models = (Model[]) ArrayUtil.setLength(modelSet.models, modelSet.modelCount);
    modelSet.modelFileNumbers = ArrayUtil.setLength(modelSet.modelFileNumbers, modelSet.modelCount);
    modelSet.modelNumbers = ArrayUtil.setLength(modelSet.modelNumbers, modelSet.modelCount);
    modelSet.modelNumbersForAtomLabel = ArrayUtil.setLength(modelSet.modelNumbersForAtomLabel, modelSet.modelCount);
    modelSet.modelNames = ArrayUtil.setLength(modelSet.modelNames, modelSet.modelCount);
    modelSet.frameTitles = ArrayUtil.setLength(modelSet.frameTitles, modelSet.modelCount);
    if (merging)
      for (int i = 0; i < mergeModelSet.modelCount; i++)
        (modelSet.models[i] = mergeModelSet.models[i]).modelSet = modelSet;
  }

  private void mergeGroups() {
    Map<String, Object> info = mergeModelSet.getAuxiliaryInfo(null);
    String[] mergeGroup3Lists = (String[]) info.get("group3Lists");
    int[][] mergeGroup3Counts = (int[][]) info.get("group3Counts");
    if (mergeGroup3Lists != null) {
      for (int i = 0; i < baseModelCount; i++) {
        group3Lists[i + 1] = mergeGroup3Lists[i + 1];
        group3Counts[i + 1] = mergeGroup3Counts[i + 1];
        structuresDefinedInFile.set(i);
      }
      group3Lists[0] = mergeGroup3Lists[0];
      group3Counts[0] = mergeGroup3Counts[0];
    }
    //if merging PDB data into an already-present model, and the 
    //structure is defined, consider the current structures in that 
    //model to be undefined. Not guarantee to work.
    if (!appendNew && isPDB) 
      structuresDefinedInFile.clear(baseModelIndex);
  }

  private void iterateOverAllNewModels(JmolAdapter adapter, Object atomSetCollection) {

    // set private values

    group3Lists = new String[modelSet.modelCount + 1];
    group3Counts = new int[modelSet.modelCount + 1][];

    structuresDefinedInFile = new BitSet();

    if (merging)
      mergeGroups();

    int iTrajectory = (isTrajectory ? baseTrajectoryCount : -1);
    int ipt = baseModelIndex;
    for (int i = 0; i < adapterModelCount; ++i, ++ipt) {
      int modelNumber = adapter.getAtomSetNumber(atomSetCollection, i);
      String modelName = adapter.getAtomSetName(atomSetCollection, i);
      Map<String, Object> modelAuxiliaryInfo = adapter.getAtomSetAuxiliaryInfo(
          atomSetCollection, i);
      if (modelAuxiliaryInfo.containsKey("modelID"))
        modelAuxiliaryInfo.put("modelID0", modelAuxiliaryInfo.get("modelID"));
      Properties modelProperties = (Properties) modelAuxiliaryInfo.get("modelProperties");
      viewer.setStringProperty("_fileType", (String) modelAuxiliaryInfo
          .get("fileType"));
      if (modelName == null)
        modelName = (jmolData != null && jmolData.indexOf(";") > 2 ? jmolData.substring(jmolData
            .indexOf(":") + 2, jmolData.indexOf(";"))
            : appendNew ? "" + (modelNumber % 1000000): "");
      boolean isPDBModel = setModelNameNumberProperties(ipt, iTrajectory,
          modelName, modelNumber, modelProperties, modelAuxiliaryInfo,
          jmolData);
      if (isPDBModel) {
        group3Lists[ipt + 1] = JmolConstants.group3List;
        group3Counts[ipt + 1] = new int[JmolConstants.group3Count + 10];
        if (group3Lists[0] == null) {
          group3Lists[0] = JmolConstants.group3List;
          group3Counts[0] = new int[JmolConstants.group3Count + 10];
        }
      }
      if (modelSet.getModelAuxiliaryInfo(ipt, "periodicOriginXyz") != null)
        modelSet.someModelsHaveSymmetry = true;
    }
    Model m = modelSet.models[baseModelIndex];
    viewer.setSmilesString((String) modelSet.modelSetAuxiliaryInfo.get("smilesString"));
    String loadState = (String) modelSet.modelSetAuxiliaryInfo.remove("loadState");
    StringBuffer loadScript = (StringBuffer)modelSet.modelSetAuxiliaryInfo.remove("loadScript");
    if (loadScript.indexOf("Viewer.AddHydrogens") < 0 || !m.isModelKit) {
      String[] lines = TextFormat.split(loadState, '\n');
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < lines.length; i++) {
        int pt = m.loadState.indexOf(lines[i]);
        if (pt < 0 || pt != m.loadState.lastIndexOf(lines[i]))
          sb.append(lines[i]).append('\n');
      }
      m.loadState += m.loadScript.toString() + sb.toString();
      m.loadScript = new StringBuffer();
      m.loadScript.append("  ").append(loadScript).append(";\n");
      
    }
    if (isTrajectory) {
      // fill in the rest of the data
      int n = (modelSet.modelCount - ipt + 1);
      Logger.info(n + " trajectory steps read");
      modelSet.setModelAuxiliaryInfo(baseModelCount, "trajectoryStepCount", Integer.valueOf(n));
      for (int ia = adapterModelCount, i = ipt; i < modelSet.modelCount; i++, ia++) {
        modelSet.models[i] = modelSet.models[baseModelCount];
        modelSet.modelNumbers[i] = adapter.getAtomSetNumber(atomSetCollection, ia);
        modelSet.modelNames[i] = adapter.getAtomSetName(atomSetCollection, ia);
        structuresDefinedInFile.set(i);
      }
    }
    finalizeModels(baseModelCount);
  }
    
  private boolean setModelNameNumberProperties(
                                               int modelIndex,
                                               int trajectoryBaseIndex,
                                               String modelName,
                                               int modelNumber,
                                               Properties modelProperties,
                                               Map<String, Object> modelAuxiliaryInfo,
                                               String jmolData) {
    boolean modelIsPDB = (modelAuxiliaryInfo != null 
        && Boolean.TRUE.equals(modelAuxiliaryInfo.get("isPDB")));
    if (appendNew) {
      modelSet.models[modelIndex] = (modelIsPDB ? 
          jbr.getBioModel(modelSet, modelIndex, trajectoryBaseIndex,
          jmolData, modelProperties, modelAuxiliaryInfo)
          : new Model(modelSet, modelIndex, trajectoryBaseIndex,
              jmolData, modelProperties, modelAuxiliaryInfo));
      modelSet.modelNumbers[modelIndex] = modelNumber;
      modelSet.modelNames[modelIndex] = modelName;
    } else {
      // set appendNew false
      Object atomInfo = modelAuxiliaryInfo.get("PDB_CONECT_firstAtom_count_max"); 
      if (atomInfo != null)
        modelSet.setModelAuxiliaryInfo(modelIndex, "PDB_CONECT_firstAtom_count_max", atomInfo); 
    }
    // this next sets the bitset length to avoid 
    // unnecessary calls to System.arrayCopy
    Model[] models = modelSet.models;
    Atom[] atoms = modelSet.atoms;
    models[modelIndex].bsAtoms.set(atoms.length + 1);
    models[modelIndex].bsAtoms.clear(atoms.length + 1);
    String codes = (String) modelSet.getModelAuxiliaryInfo(modelIndex, "altLocs");
    models[modelIndex].setNAltLocs(codes == null ? 0 : codes.length());
    if (codes != null) {
      char[] altlocs = codes.toCharArray();
      Arrays.sort(altlocs);
      codes = String.valueOf(altlocs);
      modelSet.setModelAuxiliaryInfo(modelIndex, "altLocs", codes);
    }
    codes = (String) modelSet.getModelAuxiliaryInfo(modelIndex, "insertionCodes");
    models[modelIndex].setNInsertions(codes == null ? 0 : codes.length());
    boolean isModelKit = (modelSet.modelSetName != null
        && modelSet.modelSetName.startsWith("Jmol Model Kit")
        || modelName.startsWith("Jmol Model Kit") || "Jme"
        .equals(modelSet.getModelAuxiliaryInfo(modelIndex, "fileType")));
    models[modelIndex].isModelKit = isModelKit;
    return modelIsPDB;
  }

  /**
   * Model numbers are considerably more complicated in Jmol 11.
   * 
   * int modelNumber
   *  
   *   The adapter gives us a modelNumber, but that is not necessarily
   *   what the user accesses. If a single files is loaded this is:
   *   
   *   a) single file context:
   *   
   *     1) the sequential number of the model in the file , or
   *     2) if a PDB file and "MODEL" record is present, that model number
   *     
   *   b) multifile context:
   *   
   *     always 1000000 * (fileIndex + 1) + (modelIndexInFile + 1)
   *   
   *   
   * int fileIndex
   * 
   *   The 0-based reference to the file containing this model. Used
   *   when doing   "_modelnumber3.2" in a multifile context
   *   
   * int modelFileNumber
   * 
   *   An integer coding both the file and the model:
   *   
   *     file * 1000000 + modelInFile (1-based)
   *     
   *   Used all over the place. Note that if there is only one file,
   *   then modelFileNumber < 1000000.
   * 
   * String modelNumberDotted
   *   
   *   A number the user can use "1.3"
   *   
   * String modelNumberForAtomLabel
   * 
   *   Either the dotted number or the PDB MODEL number, if there is only one file
   *   
   * @param baseModelCount
   *    
   */
  private void finalizeModels(int baseModelCount) {
    int modelCount= modelSet.modelCount;
    if (modelCount == baseModelCount)
      return;
    String sNum;
    int modelnumber = 0;
    int lastfilenumber = -1;
    int[] modelNumbers = modelSet.modelNumbers;
    String[] modelNames = modelSet.modelNames;
    if (isTrajectory)
      for (int i = baseModelCount; ++i < modelSet.modelCount;)
        modelNumbers[i] = modelNumbers[i - 1] + 1;
    if (baseModelCount > 0) {
      // load append
      if (modelNumbers[0] < 1000000) {
        // initially we had just one file
        for (int i = 0; i < baseModelCount; i++) {
          // create 1000000 model numbers for the original file models
          if (modelNames[i].length() == 0)
            modelNames[i] = "" + modelNumbers[i];
          modelNumbers[i] += 1000000;
          modelSet.modelNumbersForAtomLabel[i] = "1." + (i + 1);
        }
      }
      // update file number
      int filenumber = modelNumbers[baseModelCount - 1];
      filenumber -= filenumber % 1000000;
      if (modelNumbers[baseModelCount] < 1000000)
        filenumber += 1000000;
      for (int i = baseModelCount; i < modelCount; i++)
        modelNumbers[i] += filenumber;
    }
    Model[] models = modelSet.models;
    for (int i = baseModelCount; i < modelCount; ++i) {
      if (fileHeader != null)
        modelSet.setModelAuxiliaryInfo(i, "fileHeader", fileHeader);
      int filenumber = modelNumbers[i] / 1000000;
      if (filenumber != lastfilenumber) {
        modelnumber = 0;
        lastfilenumber = filenumber;
      }
      modelnumber++;
      if (filenumber == 0) {
        // only one file -- take the PDB number or sequential number as given by adapter
        sNum = "" + modelSet.getModelNumber(i);
        filenumber = 1;
      } else {
        //        //if only one file, just return the integer file number
        //      if (modelnumber == 1
        //        && (i + 1 == modelCount || models[i + 1].modelNumber / 1000000 != filenumber))
        //    sNum = filenumber + "";
        // else
        sNum = filenumber + "." + modelnumber;
      }
      modelSet.modelNumbersForAtomLabel[i] = sNum;
      models[i].fileIndex = filenumber - 1;
      modelSet.modelFileNumbers[i] = filenumber * 1000000 + modelnumber;
      if (modelNames[i] == null || modelNames[i].length() == 0)
        modelNames[i] = sNum;
   }
    
    if (merging)
      for (int i = 0; i < baseModelCount; i++)
        models[i].modelSet = modelSet;
    
    // this won't do in the case of trajectories
    for (int i = 0; i < modelCount; i++) {
      modelSet.setModelAuxiliaryInfo(i, "modelName", modelNames[i]);
      modelSet.setModelAuxiliaryInfo(i, "modelNumber", Integer.valueOf(modelNumbers[i] % 1000000));
      modelSet.setModelAuxiliaryInfo(i, "modelFileNumber", Integer.valueOf(modelSet.modelFileNumbers[i]));
      modelSet.setModelAuxiliaryInfo(i, "modelNumberDotted", modelSet.getModelNumberDotted(i));
      String codes = (String) modelSet.getModelAuxiliaryInfo(i, "altLocs");
      if (codes != null) {
        Logger.info("model " + modelSet.getModelNumberDotted(i)
            + " alternative locations: " + codes);
      }
    }
  }

  
//added -hcf

  private void iterateOverAllNewAtoms(JmolAdapter adapter, Object atomSetCollection, boolean addAtomInfo) {
    // atom is created, but not all methods are safe, because it
    // has no group -- this is only an issue for debugging
    int iLast = -1;
    boolean isPdbThisModel = false;
    boolean addH = false;
    JmolAdapterAtomIterator iterAtom = adapter.getAtomIterator(atomSetCollection);
    int nRead = 0;
    Model[] models = modelSet.models;
    if (modelSet.modelCount > 0)
      nullGroup = new Group(new Chain(modelSet.models[baseModelIndex], ' '), "",
          0, -1, -1);
    while (iterAtom.hasNext()) {
      nRead++;
      int modelIndex = iterAtom.getAtomSetIndex() + baseModelIndex;
     // int modelIndex = 0 + baseModelIndex;
      if (modelIndex != iLast) {
        currentModelIndex = modelIndex;
        currentModel = models[modelIndex];
        currentChainID = '\uFFFF';
        models[modelIndex].bsAtoms.clear();
        isPdbThisModel = models[modelIndex].isBioModel;
        iLast = modelIndex;
        addH = isPdbThisModel && doAddHydrogens;
        if (jbr != null)
          jbr.setHaveHsAlready(false);
      }
      String group3 = iterAtom.getGroup3();
      checkNewGroup(adapter, iterAtom.getChainID(), group3, iterAtom.getSequenceNumber(), 
          iterAtom.getInsertionCode(), addH);
      short isotope = iterAtom.getElementNumber();
      if (addH && Elements.getElementNumber(isotope) == 1)
        jbr.setHaveHsAlready(true);
      String name = iterAtom.getAtomName(); 
      int charge = (addH ? getPdbCharge(group3, name) : iterAtom.getFormalCharge());
      addAtom(isPdbThisModel, iterAtom.getAtomSymmetry(), 
          iterAtom.getAtomSite(),
          iterAtom.getUniqueID(),
          isotope,
          name,
          charge, 
          iterAtom.getPartialCharge(),
          iterAtom.getEllipsoid(), 
          iterAtom.getOccupancy(), 
          iterAtom.getBfactor(), 
          iterAtom.getX(),
          iterAtom.getY(), 
          iterAtom.getZ(), 
          iterAtom.getIsHetero(), 
          iterAtom.getAtomSerial(), 
          group3,
          iterAtom.getVectorX(), 
          iterAtom.getVectorY(), 
          iterAtom.getVectorZ(),
          iterAtom.getAlternateLocationID(),
          iterAtom.getRadius(),
          //added -hcf
          iterAtom.getSequenceNumber(),
          iterAtom.getChrScaleNumber(),
          iterAtom.getLociScaleNumber(),
          iterAtom.getFiberScaleNumber(),
          iterAtom.getNucleoScaleNumber(),
          iterAtom.getChrID(),
          iterAtom.getFromPos(),
          iterAtom.getEndPos(),
          iterAtom.getSpName(),
          iterAtom.getEnsChr(),
          iterAtom.getLcChr()
          );
    }
    
    if (groupCount > 0 && addH)
      jbr.addImplicitHydrogenAtoms(adapter, groupCount - 1);    
    iLast = -1;
    EnumVdw vdwtypeLast = null;
    Atom[] atoms = modelSet.atoms;
    for (int i = 0; i < modelSet.atomCount; i++) {
      if (atoms[i].modelIndex != iLast) {
        iLast = atoms[i].modelIndex;
        models[iLast].firstAtomIndex = i;
        EnumVdw vdwtype = modelSet.getDefaultVdwType(iLast);
        if (vdwtype != vdwtypeLast) {
          Logger.info("Default Van der Waals type for model" + " set to " + vdwtype.getVdwLabel());
          vdwtypeLast = vdwtype;
        }
      }
    }
    Logger.info(nRead + " atoms created");    
  }
  
  
//added end -hcf  
  
  
  
  private void iterateOverAllNewAtoms(JmolAdapter adapter, Object atomSetCollection) {
    // atom is created, but not all methods are safe, because it
    // has no group -- this is only an issue for debugging
    int iLast = -1;
    boolean isPdbThisModel = false;
    boolean addH = false;
    JmolAdapterAtomIterator iterAtom = adapter.getAtomIterator(atomSetCollection);
    int nRead = 0;
    Model[] models = modelSet.models;
    if (modelSet.modelCount > 0)
      nullGroup = new Group(new Chain(modelSet.models[baseModelIndex], ' '), "",
          0, -1, -1);
    while (iterAtom.hasNext()) {
      nRead++;
      int modelIndex = iterAtom.getAtomSetIndex() + baseModelIndex;
      if (modelIndex != iLast) {
        currentModelIndex = modelIndex;
        currentModel = models[modelIndex];
        currentChainID = '\uFFFF';
        models[modelIndex].bsAtoms.clear();
        isPdbThisModel = models[modelIndex].isBioModel;
        iLast = modelIndex;
        addH = isPdbThisModel && doAddHydrogens;
        if (jbr != null)
          jbr.setHaveHsAlready(false);
      }
      String group3 = iterAtom.getGroup3();
      checkNewGroup(adapter, iterAtom.getChainID(), group3, iterAtom.getSequenceNumber(), 
          iterAtom.getInsertionCode(), addH);
      short isotope = iterAtom.getElementNumber();
      if (addH && Elements.getElementNumber(isotope) == 1)
        jbr.setHaveHsAlready(true);
      String name = iterAtom.getAtomName(); 
      int sequenceNumber = iterAtom.getSequenceNumber();
      int charge = (addH ? getPdbCharge(group3, name) : iterAtom.getFormalCharge());
      addAtom(isPdbThisModel, iterAtom.getAtomSymmetry(), 
          iterAtom.getAtomSite(),
          iterAtom.getUniqueID(),
          isotope,
          name,
          charge, 
          iterAtom.getPartialCharge(),
          iterAtom.getEllipsoid(), 
          iterAtom.getOccupancy(), 
          iterAtom.getBfactor(), 
          iterAtom.getX(),
          iterAtom.getY(), 
          iterAtom.getZ(), 
          iterAtom.getIsHetero(), 
          iterAtom.getAtomSerial(), 
          group3,
          iterAtom.getVectorX(), 
          iterAtom.getVectorY(), 
          iterAtom.getVectorZ(),
          iterAtom.getAlternateLocationID(),
          iterAtom.getRadius()
          );
    }
    
    if (groupCount > 0 && addH)
      jbr.addImplicitHydrogenAtoms(adapter, groupCount - 1);    
    iLast = -1;
    EnumVdw vdwtypeLast = null;
    Atom[] atoms = modelSet.atoms;
    for (int i = 0; i < modelSet.atomCount; i++) {
      if (atoms[i].modelIndex != iLast) {
        iLast = atoms[i].modelIndex;
        models[iLast].firstAtomIndex = i;
        EnumVdw vdwtype = modelSet.getDefaultVdwType(iLast);
        if (vdwtype != vdwtypeLast) {
          Logger.info("Default Van der Waals type for model" + " set to " + vdwtype.getVdwLabel());
          vdwtypeLast = vdwtype;
        }
      }
    }
    Logger.info(nRead + " atoms created");    
  }

  private int getPdbCharge(String group3, String name) {
    if (group3.equals("ARG") && name.equals("NH1")
        || group3.equals("LYS") && name.equals("NZ")
        || group3.equals("HIS") && name.equals("ND1")
        )
      return 1;
    return 0;
  }

  
  
//added -hcf
  
  private void addAtom(boolean isPDB, BitSet atomSymmetry, int atomSite,
          Object atomUid, short atomicAndIsotopeNumber,
          String atomName, int formalCharge, float partialCharge,
          Quadric[] ellipsoid, int occupancy, float bfactor,
          float x, float y, float z, boolean isHetero,
          int atomSerial, String group3,
          float vectorX, float vectorY, float vectorZ,
          char alternateLocationID, float radius, int sequenceNumber, int chrScaleNumber, 
          int lociScaleNumber, int fiberScaleNumber, int nucleoScaleNumber, int chrID, int fromPos, int endPos,
          String spName, String ensChr, String lcChr) {
					byte specialAtomID = 0;
					if (atomName != null) {
					if (isPDB && atomName.indexOf('*') >= 0)
					atomName = atomName.replace('*', '\'');
					specialAtomID = JmolConstants.lookupSpecialAtomID(atomName);
					if (isPDB && specialAtomID == JmolConstants.ATOMID_ALPHA_CARBON
					&& "CA".equalsIgnoreCase(group3)) // calcium
					specialAtomID = 0;
					}
					Atom atom = modelSet.addAtom(currentModelIndex, nullGroup, atomicAndIsotopeNumber,
					atomName, atomSerial, atomSite, x, y, z, radius, vectorX, vectorY,
					vectorZ, formalCharge, partialCharge, occupancy, bfactor, ellipsoid,
					isHetero, specialAtomID, atomSymmetry, sequenceNumber, chrScaleNumber, lociScaleNumber, fiberScaleNumber, nucleoScaleNumber, chrID, fromPos, endPos,
					spName, ensChr, lcChr);
					atom.setAltLoc(alternateLocationID);
					htAtomMap.put(atomUid, atom);
}

  
//added end -hcf  
  
   
  
  
  
  
  private void addAtom(boolean isPDB, BitSet atomSymmetry, int atomSite,
                       Object atomUid, short atomicAndIsotopeNumber,
                       String atomName, int formalCharge, float partialCharge,
                       Quadric[] ellipsoid, int occupancy, float bfactor,
                       float x, float y, float z, boolean isHetero,
                       int atomSerial, String group3,
                       float vectorX, float vectorY, float vectorZ,
                       char alternateLocationID, float radius) {
    byte specialAtomID = 0;
    if (atomName != null) {
      if (isPDB && atomName.indexOf('*') >= 0)
        atomName = atomName.replace('*', '\'');
      specialAtomID = JmolConstants.lookupSpecialAtomID(atomName);
      if (isPDB && specialAtomID == JmolConstants.ATOMID_ALPHA_CARBON
          && "CA".equalsIgnoreCase(group3)) // calcium
        specialAtomID = 0;
    }
    Atom atom = modelSet.addAtom(currentModelIndex, nullGroup, atomicAndIsotopeNumber,
        atomName, atomSerial, atomSite, x, y, z, radius, vectorX, vectorY,
        vectorZ, formalCharge, partialCharge, occupancy, bfactor, ellipsoid,
        isHetero, specialAtomID, atomSymmetry);
    atom.setAltLoc(alternateLocationID);
    htAtomMap.put(atomUid, atom);
  }

  private void checkNewGroup(JmolAdapter adapter, char chainID,
                             String group3, int groupSequenceNumber,
                             char groupInsertionCode, boolean addH) {
    String group3i = (group3 == null ? null : group3.intern());
    if (chainID != currentChainID) {
      currentChainID = chainID;
      currentChain = getOrAllocateChain(currentModel, chainID);
      currentGroupInsertionCode = '\uFFFF';
      currentGroupSequenceNumber = -1;
      currentGroup3 = "xxxx";
    }
    if (groupSequenceNumber != currentGroupSequenceNumber
        || groupInsertionCode != currentGroupInsertionCode
        || group3i != currentGroup3) {
      if (groupCount > 0 && addH) {
        jbr.addImplicitHydrogenAtoms(adapter, groupCount - 1);
        jbr.setHaveHsAlready(false);
      }
      currentGroupSequenceNumber = groupSequenceNumber;
      currentGroupInsertionCode = groupInsertionCode;
      currentGroup3 = group3i;
      while (groupCount >= group3Of.length) {
        chainOf = (Chain[]) ArrayUtil.doubleLength(chainOf);
        group3Of = ArrayUtil.doubleLength(group3Of);
        seqcodes = ArrayUtil.doubleLength(seqcodes);
        firstAtomIndexes = ArrayUtil.doubleLength(firstAtomIndexes);
      }
      firstAtomIndexes[groupCount] = modelSet.atomCount;
      chainOf[groupCount] = currentChain;
      group3Of[groupCount] = group3;
      seqcodes[groupCount] = Group.getSeqcode(groupSequenceNumber,
          groupInsertionCode);
      ++groupCount;      
    }
  }

  private Chain getOrAllocateChain(Model model, char chainID) {
    //Logger.debug("chainID=" + chainID + " -> " + (chainID + 0));
    Chain chain = model.getChain(chainID);
    if (chain != null)
      return chain;
    if (model.chainCount == model.chains.length)
      model.chains = (Chain[])ArrayUtil.doubleLength(model.chains);
    return model.chains[model.chainCount++] = new Chain(model, chainID);
  }

  private void iterateOverAllNewBonds(JmolAdapter adapter, Object atomSetCollection) {
    JmolAdapterBondIterator iterBond = adapter.getBondIterator(atomSetCollection);
    if (iterBond == null)
      return;
    short mad = viewer.getMadBond();
    short order;
    modelSet.defaultCovalentMad = (jmolData == null ? mad : 0);
    boolean haveMultipleBonds = false;
    while (iterBond.hasNext()) {
      order = (short) iterBond.getEncodedOrder();
      bondAtoms(iterBond.getAtomUniqueID1(), iterBond.getAtomUniqueID2(), order);
      if (order > 1 && order != JmolEdge.BOND_STEREO_NEAR && order != JmolEdge.BOND_STEREO_FAR)
        haveMultipleBonds = true; 
    }
    if (haveMultipleBonds && modelSet.someModelsHaveSymmetry && !viewer.getApplySymmetryToBonds())
      Logger.info("ModelSet: use \"set appletSymmetryToBonds TRUE \" to apply the file-based multiple bonds to symmetry-generated atoms.");
    modelSet.defaultCovalentMad = mad;
  }
  
  private List<Bond> vStereo;
  private void bondAtoms(Object atomUid1, Object atomUid2, short order) {
    Atom atom1 = htAtomMap.get(atomUid1);
    if (atom1 == null) {
      Logger.error("bondAtoms cannot find atomUid1?:" + atomUid1);
      return;
    }
    Atom atom2 = htAtomMap.get(atomUid2);
    if (atom2 == null) {
      Logger.error("bondAtoms cannot find atomUid2?:" + atomUid2);
      return;
    }
    
    // note that if the atoms are already bonded then
    // Atom.bondMutually(...) will return null
    if (atom1.isBonded(atom2))
      return;
    boolean isNear = (order == JmolEdge.BOND_STEREO_NEAR);
    boolean isFar = (order == JmolEdge.BOND_STEREO_FAR);
    Bond bond;
    if (isNear || isFar) {
      bond = modelSet.bondMutually(atom1, atom2, (is2D ? order : 1), modelSet.getDefaultMadFromOrder(1), 0);
      if (vStereo == null) {
        vStereo = new ArrayList<Bond>();
      }
      vStereo.add(bond);
    } else {
      bond = modelSet.bondMutually(atom1, atom2, order, modelSet.getDefaultMadFromOrder(order), 0);
      if (bond.isAromatic()) {
        modelSet.someModelsHaveAromaticBonds = true;
      }
    }
    if (modelSet.bondCount == modelSet.bonds.length) {
      modelSet.bonds = (Bond[]) ArrayUtil.setLength(modelSet.bonds, modelSet.bondCount + BondCollection.BOND_GROWTH_INCREMENT);
    }
    modelSet.setBond(modelSet.bondCount++, bond);
  }

  /**
   * Pull in all spans of helix, etc. in the file(s)
   * 
   * We do turn first, because sometimes a group is defined
   * twice, and this way it gets marked as helix or sheet
   * if it is both one of those and turn.
   * 
   * @param adapter
   * @param atomSetCollection
   */
  private void iterateOverAllNewStructures(JmolAdapter adapter,
                                           Object atomSetCollection) {
    JmolAdapterStructureIterator iterStructure = adapter
        .getStructureIterator(atomSetCollection);
    if (iterStructure != null)
      while (iterStructure.hasNext()) {
        if (iterStructure.getStructureType() != EnumStructure.TURN) {
          defineStructure(iterStructure.getModelIndex(),
              iterStructure.getSubstructureType(),
              iterStructure.getStructureID(), 
              iterStructure.getSerialID(),
              iterStructure.getStrandCount(),
              iterStructure.getStartChainID(), iterStructure
                  .getStartSequenceNumber(), iterStructure
                  .getStartInsertionCode(), iterStructure.getEndChainID(),
              iterStructure.getEndSequenceNumber(), iterStructure
                  .getEndInsertionCode());
        }
      }

    // define turns LAST. (pulled by the iterator first)
    // so that if they overlap they get overwritten:

    iterStructure = adapter.getStructureIterator(atomSetCollection);
    if (iterStructure != null)
      while (iterStructure.hasNext()) {
        if (iterStructure.getStructureType() == EnumStructure.TURN)
          defineStructure(iterStructure.getModelIndex(),
              iterStructure.getSubstructureType(),
              iterStructure.getStructureID(), 1, 1,
              iterStructure.getStartChainID(), iterStructure
                  .getStartSequenceNumber(), iterStructure
                  .getStartInsertionCode(), iterStructure.getEndChainID(),
              iterStructure.getEndSequenceNumber(), iterStructure
                  .getEndInsertionCode());
      }
  }
  
  private BitSet structuresDefinedInFile = new BitSet();

  private void defineStructure(int modelIndex, EnumStructure subType,
                               String structureID, int serialID,
                               int strandCount, char startChainID,
                               int startSequenceNumber,
                               char startInsertionCode, char endChainID,
                               int endSequenceNumber, char endInsertionCode) {
    EnumStructure type = (subType == EnumStructure.NOT ? EnumStructure.NONE : subType);
    int startSeqCode = Group.getSeqcode(startSequenceNumber,
        startInsertionCode);
    int endSeqCode = Group.getSeqcode(endSequenceNumber, endInsertionCode);
    Model[] models = modelSet.models;
    if (modelIndex >= 0 || isTrajectory) { //from PDB file
      if (isTrajectory)
        modelIndex = 0;
      modelIndex += baseModelIndex;
      structuresDefinedInFile.set(modelIndex);
      models[modelIndex].addSecondaryStructure(type,
          structureID, serialID, strandCount,
          startChainID, startSeqCode, endChainID, endSeqCode);
      return;
    }
    for (int i = baseModelIndex; i < modelSet.modelCount; i++) {
      structuresDefinedInFile.set(i);
      models[i].addSecondaryStructure(type,
          structureID, serialID, strandCount,
          startChainID, startSeqCode, endChainID, endSeqCode);
    }
  }
  
  ////// symmetry ///////

  private void initializeUnitCellAndSymmetry() {
    /*
     * really THREE issues here:
     * 1) does a model have an associated unit cell that could be displayed?
     * 2) are the coordinates fractional and so need to be transformed?
     * 3) does the model have symmetry operations that were applied?
     * 
     * This must be done for each model individually.
     * 
     */

    if (someModelsHaveUnitcells) {
      modelSet.unitCells = new SymmetryInterface[modelSet.modelCount];
      boolean haveMergeCells = (mergeModelSet != null && mergeModelSet.unitCells != null);
      for (int i = 0; i < modelSet.modelCount; i++) {
        if (haveMergeCells && i < baseModelCount) {
          modelSet.unitCells[i] = mergeModelSet.unitCells[i];
        } else {
          modelSet.unitCells[i] = (SymmetryInterface) Interface
              .getOptionInterface("symmetry.Symmetry");
          modelSet.unitCells[i].setSymmetryInfo(i, modelSet.getModelAuxiliaryInfo(i));
        }
      }
    }
    if (appendNew && modelSet.someModelsHaveSymmetry) {
      modelSet.getAtomBits(Token.symmetry, null);
      Atom[] atoms = modelSet.atoms;
      for (int iAtom = baseAtomIndex, iModel = -1, i0 = 0; iAtom < modelSet.atomCount; iAtom++) {
        if (atoms[iAtom].modelIndex != iModel) {
          iModel = atoms[iAtom].modelIndex;
          i0 = baseAtomIndex
              + modelSet.getModelAuxiliaryInfoInt(iModel,
                  "presymmetryAtomIndex")
              + modelSet.getModelAuxiliaryInfoInt(iModel,
                  "presymmetryAtomCount");
        }
        if (iAtom >= i0)
          modelSet.bsSymmetry.set(iAtom);
      }
    }
    if (appendNew && modelSet.someModelsHaveFractionalCoordinates) {
      Atom[] atoms = modelSet.atoms;
      int modelIndex = -1;
      SymmetryInterface c = null;
      for (int i = baseAtomIndex; i < modelSet.atomCount; i++) {
        if (atoms[i].modelIndex != modelIndex) {
          modelIndex = atoms[i].modelIndex;
          c = modelSet.getUnitCell(modelIndex);
        }
        if (c != null && c.getCoordinatesAreFractional())
          c.toCartesian(c.toSupercell(atoms[i]), false);
      }
      for (int imodel = baseModelIndex; imodel < modelSet.modelCount; imodel++) {
        if (modelSet.isTrajectory(imodel)) {
          c = modelSet.getUnitCell(imodel);
          if (c != null && c.getCoordinatesAreFractional() && c.isSupercell()) {
            Point3f[] list = modelSet.trajectorySteps.get(imodel);
            for (int i = list.length; --i >= 0;)
              if (list[i] != null)
                c.toSupercell(list[i]);
          }
        }
      }
    }
  }

  private void initializeBonding() {
    // perform bonding if necessary

    // 1. apply CONECT records and set bsExclude to omit them
    // 2. apply stereochemistry from JME

    BitSet bsExclude = (modelSet.getModelSetAuxiliaryInfo("someModelsHaveCONECT") == null ? null
        : new BitSet());
    if (bsExclude != null)
      modelSet.setPdbConectBonding(baseAtomIndex, baseModelIndex, bsExclude);

    // 2. for each model in the collection,
    int atomIndex = baseAtomIndex;
    int modelAtomCount = 0;
    boolean symmetryAlreadyAppliedToBonds = viewer.getApplySymmetryToBonds();
    boolean doAutoBond = viewer.getAutoBond();
    boolean forceAutoBond = viewer.getForceAutoBond();
    BitSet bs = null;
    boolean autoBonding = false;
    int modelCount = modelSet.modelCount;
    Model[] models = modelSet.models;
    if (!noAutoBond)
      for (int i = baseModelIndex; i < modelCount; atomIndex += modelAtomCount, i++) {
        modelAtomCount = models[i].bsAtoms.cardinality();
        int modelBondCount = modelSet.getModelAuxiliaryInfoInt(i, "initialBondCount");
        
        boolean modelIsPDB = models[i].isBioModel;
        if (modelBondCount < 0) {
          modelBondCount = modelSet.bondCount;
        }
        boolean modelHasSymmetry = modelSet.getModelAuxiliaryInfoBoolean(i,
            "hasSymmetry");
        // check for PDB file with fewer than one bond per every two atoms
        // this is in case the PDB format is being usurped for non-RCSB uses
        // In other words, say someone uses the PDB format to indicate atoms and
        // connectivity. We do NOT want to mess up that connectivity here.
        // It would be OK if people used HETATM for every atom, but I think
        // people
        // use ATOM, so that's a problem. Those atoms would not be excluded from
        // the
        // automatic bonding, and additional bonds might be made.
        boolean doBond = (forceAutoBond || doAutoBond
            && (modelBondCount == 0 || modelIsPDB && jmolData == null
                && modelBondCount < modelAtomCount / 2 || modelHasSymmetry
                && !symmetryAlreadyAppliedToBonds 
                && !modelSet.getModelAuxiliaryInfoBoolean(i, "hasBonds")
                ));
        if (!doBond)
          continue;
        autoBonding = true;
        if (merging || modelCount > 1) {
          if (bs == null)
            bs = new BitSet(modelSet.atomCount);
          if (i == baseModelIndex || !isTrajectory)
            bs.or(models[i].bsAtoms);
        }
      }
    if (autoBonding) {
      modelSet.autoBond(bs, bs, bsExclude, null, modelSet.defaultCovalentMad, viewer.checkAutoBondLegacy());
      Logger
          .info("ModelSet: autobonding; use  autobond=false  to not generate bonds automatically");
    } else {
      Logger
          .info("ModelSet: not autobonding; use  forceAutobond=true  to force automatic bond creation");
    }
  }

  private void finalizeGroupBuild() {
    // run this loop in increasing order so that the
    // groups get defined going up
    groups = new Group[groupCount];
    if (merging)
      for (int i = 0; i < mergeGroups.length; i++) {
        groups[i] = mergeGroups[i];
        groups[i].setModelSet(modelSet);
      }
    for (int i = baseGroupIndex; i < groupCount; ++i)
      distinguishAndPropagateGroup(i, chainOf[i], group3Of[i], seqcodes[i],
          firstAtomIndexes[i], (i == groupCount - 1 ? modelSet.atomCount
              : firstAtomIndexes[i + 1]));
    if (group3Lists != null)
      if (modelSet.modelSetAuxiliaryInfo != null) {
        modelSet.modelSetAuxiliaryInfo.put("group3Lists", group3Lists);
        modelSet.modelSetAuxiliaryInfo.put("group3Counts", group3Counts);
      }
  }

  private void distinguishAndPropagateGroup(int groupIndex, Chain chain,
                                            String group3, int seqcode,
                                            int firstAtomIndex, int maxAtomIndex) {
    /*
     * called by finalizeGroupBuild()
     * 
     * first: build array of special atom names, 
     * for example "CA" for the alpha carbon is assigned #2
     * see JmolConstants.specialAtomNames[]
     * the special atoms all have IDs based on Atom.lookupSpecialAtomID(atomName)
     * these will be the same for each conformation
     * 
     * second: creates the monomers themselves based on this information
     * thus building the byte offsets[] array for each monomer, indicating which
     * position relative to the first atom in the group is which atom.
     * Each monomer.offsets[i] then points to the specific atom of that type
     * these will NOT be the same for each conformation  
     * 
     */
    int lastAtomIndex = maxAtomIndex - 1;

    if (lastAtomIndex < firstAtomIndex)
      throw new NullPointerException();
    int modelIndex = modelSet.atoms[firstAtomIndex].modelIndex;

    Group group = null;
    if (group3 != null && jbr != null) {
      group = jbr.distinguishAndPropagateGroup(chain, group3, seqcode,
          firstAtomIndex, maxAtomIndex, modelIndex, specialAtomIndexes, modelSet.atoms);
    }
    String key;
    if (group == null) {
      group = new Group(chain, group3, seqcode, firstAtomIndex, lastAtomIndex);
      key = "o>";
    } else {
      key = (group.isProtein() ? "p>" : group.isNucleic() ? "n>" : group
          .isCarbohydrate() ? "c>" : "o>");
    }
    if (group3 != null)
      countGroup(modelIndex, key, group3);
    addGroup(chain, group);
    groups[groupIndex] = group;
    group.setGroupIndex(groupIndex);

    for (int i = maxAtomIndex; --i >= firstAtomIndex;)
      modelSet.atoms[i].setGroup(group);

  }

  private void addGroup(Chain chain, Group group) {
    if (chain.groupCount == chain.groups.length)
      chain.groups = (Group[])ArrayUtil.doubleLength(chain.groups);
    chain.groups[chain.groupCount++] = group;
  }

  private void countGroup(int modelIndex, String code, String group3) {
    int ptm = modelIndex + 1;
    if (group3Lists == null || group3Lists[ptm] == null)
      return;
    String g3code = (group3 + "   ").substring(0, 3);
    int pt = group3Lists[ptm].indexOf(g3code);
    if (pt < 0) {
      group3Lists[ptm] += ",[" + g3code + "]";
      pt = group3Lists[ptm].indexOf(g3code);
      group3Counts[ptm] = ArrayUtil.setLength(
          group3Counts[ptm], group3Counts[ptm].length + 10);
    }
    group3Counts[ptm][pt / 6]++;
    pt = group3Lists[ptm].indexOf(",[" + g3code);
    if (pt >= 0)
      group3Lists[ptm] = group3Lists[ptm].substring(0, pt) + code
          + group3Lists[ptm].substring(pt + 2);
    //becomes x> instead of ,[ 
    //these will be used for setting up the popup menu
    if (modelIndex >= 0)
      countGroup(-1, code, group3);
  }

  private void freeze() {
    htAtomMap.clear();
    // resize arrays
    if (modelSet.atomCount < modelSet.atoms.length)
      modelSet.growAtomArrays(modelSet.atomCount);
    if (modelSet.bondCount < modelSet.bonds.length)
      modelSet.bonds = (Bond[]) ArrayUtil.setLength(modelSet.bonds, modelSet.bondCount);

    // free bonds cache 

    for (int i = BondCollection.MAX_BONDS_LENGTH_TO_CACHE; --i > 0;) { // .GT. 0
      modelSet.numCached[i] = 0;
      Bond[][] bondsCache = modelSet.freeBonds[i];
      for (int j = bondsCache.length; --j >= 0;)
        bondsCache[j] = null;
    }

    modelSet.setAtomNamesAndNumbers(0, baseAtomIndex, mergeModelSet);

    // find elements for the popup menus

    findElementsPresent();

    modelSet.resetMolecules();
    currentModel = null;
    currentChain = null;

    // finalize all structures

    if (!isPDB) {
      modelSet.freezeModels();
      return;
    }
    boolean asDSSP = viewer.getDefaultStructureDSSP();
    String ret = modelSet.calculateStructuresAllExcept(structuresDefinedInFile, 
          asDSSP, 
          false, true, true, asDSSP); // now DSSP
    if (ret.length() > 0)
      Logger.info(ret);
  }

  private void findElementsPresent() {
    modelSet.elementsPresent = new BitSet[modelSet.modelCount];
    for (int i = 0; i < modelSet.modelCount; i++)
      modelSet.elementsPresent[i] = new BitSet(64);
    for (int i = modelSet.atomCount; --i >= 0;) {
      int n = modelSet.atoms[i].getAtomicAndIsotopeNumber();
      if (n >= Elements.elementNumberMax)
        n = Elements.elementNumberMax
            + Elements.altElementIndexFromNumber(n);
      modelSet.elementsPresent[modelSet.atoms[i].modelIndex].set(n);
    }
  }

  private void applyStereochemistry() {

    // 1) implicit stereochemistry 
    
    set2dZ(baseAtomIndex, modelSet.atomCount);

    // 2) explicit stereochemistry
    
    if (vStereo != null) {
      BitSet bsToTest = new BitSet();
      bsToTest.set(baseAtomIndex, modelSet.atomCount);
      for (int i = vStereo.size(); --i >= 0;) {
        Bond b = vStereo.get(i);
        float dz2 = (b.order == JmolEdge.BOND_STEREO_NEAR ? 3 : -3);
        b.order = 1;
        if (b.atom2.z != b.atom1.z && (dz2 < 0) == (b.atom2.z < b.atom1.z))
          dz2 /= 3;
        //float dz1 = dz2/3;
        //b.atom1.z += dz1;
        BitSet bs = JmolMolecule.getBranchBitSet(modelSet.atoms, b.atom2.index, bsToTest, null, b.atom1.index, false, true);
        bs.set(b.atom2.index); // ring structures
        for (int j = bs.nextSetBit(0); j >= 0; j = bs.nextSetBit(j + 1))
          modelSet.atoms[j].z += dz2;
        // move atom2 somewhat closer to but not directly above atom1
        b.atom2.x = (b.atom1.x + b.atom2.x) /2;
        b.atom2.y = (b.atom1.y + b.atom2.y) /2;
      }
      vStereo = null;
    } 
    is2D = false;
  }

  private void set2dZ(int iatom1, int iatom2) {
    BitSet atomlist = new BitSet(iatom2);
    BitSet bsBranch = new BitSet();
    Vector3f v = new Vector3f();
    Vector3f v0 = new Vector3f(0, 1, 0);
    Vector3f v1 = new Vector3f();
    BitSet bs0 = new BitSet();
    bs0.set(iatom1, iatom2);
    for (int i = iatom1; i < iatom2; i++)
      if (!atomlist.get(i) && !bsBranch.get(i)) {
        bsBranch = getBranch2dZ(i, -1, bs0, bsBranch, v, v0, v1);
        atomlist.or(bsBranch);
      }
  }
  
  
  /**
   * @param atomIndex 
   * @param atomIndexNot 
   * @param bs0 
   * @param bsBranch  
   * @param v 
   * @param v0 
   * @param v1 
   * @return   atom bitset
   */
  private BitSet getBranch2dZ(int atomIndex, int atomIndexNot, BitSet bs0, 
                              BitSet bsBranch, Vector3f v, Vector3f v0, Vector3f v1) {
    BitSet bs = new BitSet(modelSet.atomCount);
    if (atomIndex < 0)
      return bs;
    BitSet bsToTest = new BitSet();
    bsToTest.or(bs0);
    if (atomIndexNot >= 0)
      bsToTest.clear(atomIndexNot);
    setBranch2dZ(modelSet.atoms[atomIndex], bs, bsToTest, v, v0, v1);
    return bs;
  }

  private static void setBranch2dZ(Atom atom, BitSet bs,
                                            BitSet bsToTest, Vector3f v,
                                            Vector3f v0, Vector3f v1) {
    int atomIndex = atom.index;
    if (!bsToTest.get(atomIndex))
      return;
    bsToTest.clear(atomIndex);
    bs.set(atomIndex);
    if (atom.bonds == null)
      return;
    for (int i = atom.bonds.length; --i >= 0;) {
      Bond bond = atom.bonds[i];
      if (bond.isHydrogen())
        continue;
      Atom atom2 = bond.getOtherAtom(atom);
      setAtom2dZ(atom, atom2, v, v0, v1);
      setBranch2dZ(atom2, bs, bsToTest, v, v0, v1);
    }
  }

  private static void setAtom2dZ(Atom atomRef, Atom atom2, Vector3f v, Vector3f v0, Vector3f v1) {
    v.set(atom2);
    v.sub(atomRef);
    v.z = 0;
    v.normalize();
    v1.cross(v0, v);
    double theta = Math.acos(v.dot(v0));
    atom2.z = atomRef.z + (float) (0.8f * Math.sin(4 * theta));
  }

  ///////////////  shapes  ///////////////
  
  private void finalizeShapes() {
    modelSet.shapeManager = viewer.getShapeManager();
    if (!merging)
      modelSet.shapeManager.resetShapes();
    modelSet.shapeManager.loadDefaultShapes(modelSet);
    if (modelSet.someModelsHaveAromaticBonds && viewer.getSmartAromatic())
      modelSet.assignAromaticBonds(false);
    if (merging && baseModelCount == 1)
        modelSet.shapeManager.setShapeProperty(JmolConstants.SHAPE_MEASURES, "clearModelIndex", null, null);
  }

  /**
   * called from org.jmol.modelsetbio.resolver when adding hydrogens.
   * 
   * @param iAtom
   */
  public void undeleteAtom(int iAtom) {
    modelSet.atoms[iAtom].valence = 0; 
  }

  /**
   * called from org.jmol.modelsetbio.resolver when adding hydrogens.
   * 
   * @param bsDeletedAtoms
   */
  public void deleteAtoms(BitSet bsDeletedAtoms) {
    doRemoveAddedHydrogens = true;
    if (doRemoveAddedHydrogens) {
      // get map
      int[] mapOldToNew = new int[modelSet.atomCount];
      int[] mapNewToOld = new int[modelSet.atomCount
          - bsDeletedAtoms.cardinality()];
      int n = baseAtomIndex;
      Model[] models = modelSet.models;
      Atom[] atoms = modelSet.atoms;
      for (int i = baseAtomIndex; i < modelSet.atomCount; i++) {
        models[atoms[i].modelIndex].bsAtoms.clear(i);
        models[atoms[i].modelIndex].bsAtomsDeleted.clear(i);
        if (bsDeletedAtoms.get(i)) {
          mapOldToNew[i] = n - 1;
          models[atoms[i].modelIndex].atomCount--;
        } else {
          mapNewToOld[n] = i;
          mapOldToNew[i] = n++;
        }
      }
      modelSet.modelSetAuxiliaryInfo.put("bsDeletedAtoms", bsDeletedAtoms);
      // adjust group pointers
      for (int i = baseGroupIndex; i < groups.length; i++) {
        Group g = groups[i];
        if (g.firstAtomIndex >= baseAtomIndex) {
          g.firstAtomIndex = mapOldToNew[g.firstAtomIndex];
          g.lastAtomIndex = mapOldToNew[g.lastAtomIndex];
          if (g.leadAtomIndex >= 0)
            g.leadAtomIndex = mapOldToNew[g.leadAtomIndex];
        }
      }
      // adjust atom arrays
      modelSet.adjustAtomArrays(mapNewToOld, baseAtomIndex, n);
    } else {
      modelSet.viewer.deleteAtoms(bsDeletedAtoms, false);
    }

    modelSet.calcBoundBoxDimensions(null, 1);
    modelSet.resetMolecules();
    modelSet.validateBspf(false);
  }

  
  public static void createAtomDataSet(Viewer viewer, ModelSet modelSet, int tokType, Object atomSetCollection,
                                BitSet bsSelected) {
    if (atomSetCollection == null)
      return;
    // must be one of JmolConstants.LOAD_ATOM_DATA_TYPES
    JmolAdapter adapter = viewer.getModelAdapter();
    Point3f pt = new Point3f();
    Point3f v = new Point3f();
    Atom[] atoms = modelSet.atoms;
    float tolerance = viewer.getLoadAtomDataTolerance();
    if (modelSet.unitCells != null)
      for (int i = bsSelected.nextSetBit(0); i >= 0; i = bsSelected
          .nextSetBit(i + 1))
        if (atoms[i].getAtomSymmetry() != null) {
          tolerance = -tolerance;
          break;
        }
    int i = -1;
    int n = 0;
    boolean loadAllData = (BitSetUtil.cardinalityOf(bsSelected) == viewer
        .getAtomCount());
    for (JmolAdapterAtomIterator iterAtom = adapter
        .getAtomIterator(atomSetCollection); iterAtom.hasNext();) {
      float x = iterAtom.getX();
      float y = iterAtom.getY();
      float z = iterAtom.getZ();
      if (Float.isNaN(x + y + z))
        continue;

      if (tokType == Token.xyz) {
        // we are loading selected coordinates only
        i = bsSelected.nextSetBit(i + 1);
        if (i < 0)
          break;
        n++;
        if (Logger.debugging)
          Logger.debug("atomIndex = " + i + ": " + atoms[i]
              + " --> (" + x + "," + y + "," + z);
        modelSet.setAtomCoord(i, x, y, z);
        continue;
      }
      pt.set(x, y, z);
      BitSet bs = new BitSet(modelSet.atomCount);
      modelSet.getAtomsWithin(tolerance, pt, bs, -1);
      bs.and(bsSelected);
      if (loadAllData) {
        n = BitSetUtil.cardinalityOf(bs);
        if (n == 0) {
          Logger.warn("createAtomDataSet: no atom found at position " + pt);
          continue;
        } else if (n > 1 && Logger.debugging) {
          Logger.debug("createAtomDataSet: " + n + " atoms found at position "
              + pt);
        }
      }
      switch (tokType) {
      case Token.vibxyz:
        float vx = iterAtom.getVectorX();
        float vy = iterAtom.getVectorY();
        float vz = iterAtom.getVectorZ();
        if (Float.isNaN(vx + vy + vz))
          continue;
        v.set(vx, vy, vz);
        if (Logger.debugging)
          Logger.info("xyz: " + pt + " vib: " + v);
        modelSet.setAtomCoord(bs, Token.vibxyz, v);
        break;
      case Token.occupancy:
        // [0 to 100], default 100
        modelSet.setAtomProperty(bs, tokType, iterAtom.getOccupancy(), 0, null, null,
            null);
        break;
      case Token.partialcharge:
        // anything but NaN, default NaN
        modelSet.setAtomProperty(bs, tokType, 0, iterAtom.getPartialCharge(), null,
            null, null);
        break;
      case Token.temperature:
        // anything but NaN but rounded to 0.01 precision and stored as a short (-32000 - 32000), default NaN
        modelSet.setAtomProperty(bs, tokType, 0, iterAtom.getBfactor(), null, null, null);
        break;
      }
    }
    //finally:
    switch (tokType) {
    case Token.vibxyz:
      String vibName = adapter.getAtomSetName(atomSetCollection, 0);
      Logger.info("_vibrationName = " + vibName);
      viewer.setStringProperty("_vibrationName", vibName);
      break;
    case Token.xyz:
      Logger.info(n + " atom positions read");
      modelSet.recalculateLeadMidpointsAndWingVectors(-1);
      break;
    }    
  }

}
