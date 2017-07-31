package org.jmol.api;

import java.util.BitSet;
import java.util.Map;
import java.util.Properties;

import org.jmol.modelset.Atom;
import org.jmol.modelset.Chain;
import org.jmol.modelset.Group;
import org.jmol.modelset.Model;
import org.jmol.modelset.ModelLoader;
import org.jmol.modelset.ModelSet;

public interface JmolBioResolver {

  public Group distinguishAndPropagateGroup(Chain chain, String group3, int seqcode,
                                                  int firstAtomIndex, int maxAtomIndex, 
                                                  int modelIndex, int[] specialAtomIndexes,
                                                  Atom[] atoms);
  
  public void initializeHydrogenAddition(ModelLoader modelSet, int bondCount);

  public void finalizeHydrogens();

  public void setHaveHsAlready(boolean b);

  public void addImplicitHydrogenAtoms(JmolAdapter adapter, int i);

  public void initialize(ModelSet modelSet);

  public String fixPropertyValue(BitSet bsAtoms, String data);

  public Model getBioModel(ModelSet modelSet, int modelIndex,
                        int trajectoryBaseIndex, String jmolData,
                        Properties modelProperties,
                        Map<String, Object> modelAuxiliaryInfo);

  }

