package org.jmol.atomdata;

import java.io.BufferedInputStream;
import java.util.BitSet;

import javax.vecmath.Point3f;

import org.jmol.api.AtomIndexIterator;



public interface AtomDataServer {
  public AtomIndexIterator getSelectedAtomIterator(BitSet bsSelected,
                                                    boolean isGreaterOnly,
                                                    boolean modelZeroBased, boolean isMultiModel);

  public void setIteratorForAtom(AtomIndexIterator iterator, int atomIndex, float distance);

  public void setIteratorForPoint(AtomIndexIterator iter, int modelIndex, Point3f pt,
                                  float maxDistance);

  public void fillAtomData(AtomData atomData, int mode);
  
  public BufferedInputStream getBufferedInputStream(String fullPathName);

  public void log(String msg);
  
  public float evalFunctionFloat(Object func, Object params, float[] values);

}
