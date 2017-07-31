package org.jmol.api;

import java.util.BitSet;
import java.util.List;

import org.jmol.util.JmolNode;

public interface SmilesMatcherInterface {

  public abstract String getLastException();

  public int areEqual(String smiles1, String smiles2);

  public abstract BitSet[] find(String pattern,/* ...in... */String smiles,
                                boolean isSmarts, boolean firstMatchOnly);

  public abstract BitSet getSubstructureSet(String pattern, JmolNode[] atoms,
                                            int atomCount, BitSet bsSelected,
                                            boolean isSmarts,
                                            boolean firstMatchOnly);

  public abstract BitSet[] getSubstructureSetArray(String pattern,
                                                   JmolNode[] atoms,
                                                   int atomCount,
                                                   BitSet bsSelected,
                                                   BitSet bsAromatic,
                                                   boolean isSmarts,
                                                   boolean firstMatchOnly);

  public abstract int[][] getCorrelationMaps(String pattern, JmolNode[] atoms,
                                             int atomCount, BitSet bsSelected,
                                             boolean isSmarts,
                                             boolean firstMatchOnly);

  public abstract String getMolecularFormula(String pattern, boolean isSearch);

  public abstract String getSmiles(JmolNode[] atoms, int atomCount,
                                   BitSet bsSelected, boolean asBioSmiles,
                                   boolean allowUnmatchedRings, boolean addCrossLinks, String comment);

  public abstract String getRelationship(String smiles1, String smiles2);

  public abstract String reverseChirality(String smiles);

  public abstract void getSubstructureSets(String[] smarts, JmolNode[] atoms, int atomCount,
                                           int flags,
                         BitSet bsSelected, List<BitSet> bitSets, List<BitSet>[] vRings);
}
