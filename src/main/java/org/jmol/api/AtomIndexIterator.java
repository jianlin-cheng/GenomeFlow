package org.jmol.api;

import java.util.BitSet;

import javax.vecmath.Point3f;

import org.jmol.atomdata.RadiusData;
import org.jmol.modelset.ModelCollection;


/**
 * note: YOU MUST RELEASE THE ITERATOR
 */
public interface AtomIndexIterator {
  /**
   * @param modelSet 
   * @param modelIndex
   * @param zeroBase    an offset used in the AtomIteratorWithinSet only
   * @param atomIndex
   * @param center
   * @param distance
   * @param rd 
   */
  public void set(ModelCollection modelSet, int modelIndex, int zeroBase, int atomIndex, Point3f center, float distance, RadiusData rd);
  public void set(Point3f center, float distance);
  public void addAtoms(BitSet bsResult);
  public boolean hasNext();
  public int next();
  public float foundDistance2();
  public void release();
}
