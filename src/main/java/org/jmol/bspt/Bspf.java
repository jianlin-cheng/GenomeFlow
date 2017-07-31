/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-08-25 18:42:49 -0500 (Sat, 25 Aug 2012) $
 *
 * Copyright (C) 2003-2005  Miguel, Jmol Development, www.jmol.org
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
package org.jmol.bspt;

import java.util.BitSet;

import javax.vecmath.Point3f;

import org.jmol.util.Logger;

/**
 * A Binary Space Partitioning Forest
 *<p>
 * This is simply an array of Binary Space Partitioning Trees identified
 * by indexes
 *
 * @author Miguel, miguel@jmol.org
*/

public final class Bspf {

  int dimMax;
  Bspt[] bspts;
  protected boolean isValid = false;
  boolean[] bsptsValid;
  
  public void validate(boolean isValid) {
    this.isValid = isValid;
  }

  public void validate(int i, boolean isValid) {
    bsptsValid[i] = isValid;
  }

  public boolean isInitialized() {
    return isValid;
  }

  public boolean isInitialized(int bsptIndex) {
    return bspts.length > bsptIndex && bspts[bsptIndex] != null
        && bsptsValid[bsptIndex];
  }
  
  CubeIterator[] cubeIterators;
  
  public Bspf(int dimMax) {
    this.dimMax = dimMax;
    bspts = new Bspt[0];
    bsptsValid = new boolean[0];
    cubeIterators = new CubeIterator[0];
  }

  public int getBsptCount() {
    return bspts.length;
  }
  
  public void addTuple(int bsptIndex, Point3f tuple) {
    if (bsptIndex >= bspts.length) {
      Bspt[] t = new Bspt[bsptIndex + 1];
      System.arraycopy(bspts, 0, t, 0, bspts.length);
      bspts = t;
      boolean[] b = new boolean[bsptIndex + 1];
      System.arraycopy(bsptsValid, 0, b, 0, bsptsValid.length);
      bsptsValid = b;
    }
    Bspt bspt = bspts[bsptIndex];
    if (bspt == null) {
      bspt = bspts[bsptIndex] = new Bspt(dimMax, bsptIndex);
    }
    bspt.addTuple(tuple);
  }

  public void stats() {
    for (int i = 0; i < bspts.length; ++i)
      if (bspts[i] != null)
        bspts[i].stats();
  }

  
  public void dump() {
    for (int i = 0; i < bspts.length; ++i) {
      Logger.info(">>>>\nDumping bspt #" + i + "\n>>>>");
      bspts[i].dump();
    }
    Logger.info("<<<<");
  }
  
  /**
   * @param bsptIndex  a model index
   * @return           either a cached or a new CubeIterator
   * 
   */
  public CubeIterator getCubeIterator(int bsptIndex) {
    if (bsptIndex < 0)
      return getNewCubeIterator(-1 - bsptIndex);
    if (bsptIndex >= cubeIterators.length) {
      CubeIterator[] t = new CubeIterator[bsptIndex + 1];
      System.arraycopy(cubeIterators, 0, t, 0, cubeIterators.length);
      cubeIterators = t;
    }
    if (cubeIterators[bsptIndex] == null &&
        bspts[bsptIndex] != null)
      cubeIterators[bsptIndex] = getNewCubeIterator(bsptIndex);
    cubeIterators[bsptIndex].set(bspts[bsptIndex]);
    return cubeIterators[bsptIndex];
  }

  public CubeIterator getNewCubeIterator(int bsptIndex) {
      return bspts[bsptIndex].allocateCubeIterator();
  }

  public synchronized void initialize(int modelIndex, Point3f[] atoms, BitSet modelAtomBitSet) {
    if (bspts[modelIndex] != null)
      bspts[modelIndex].reset();
    for (int i = modelAtomBitSet.nextSetBit(0); i >= 0; i = modelAtomBitSet.nextSetBit(i + 1))
      addTuple(modelIndex, atoms[i]);
    bsptsValid[modelIndex] = true;
  }

}
