/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2011-11-12 15:53:54 -0600 (Sat, 12 Nov 2011) $
 * $Revision: 16460 $
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

public final class Chain {

  public Model model;
  public char chainID;
  public boolean isDna, isRna;
  
  int groupCount;
  Group[] groups = new Group[16];
  int selectedGroupCount;

  public Atom getAtom(int index) {
    return model.modelSet.atoms[index];
  }
  
  Chain(Model model, char chainID) {
    this.model = model;
    this.chainID = chainID;
  }

  Group getGroup(int groupIndex) {
    return groups[groupIndex];
  }
  
  int getGroupCount() {
    return groupCount;
  }

  /**
   * prior to coloring by group, we need the chain count per chain that is
   * selected
   * 
   * @param bsSelected
   */
  void calcSelectedGroupsCount(BitSet bsSelected) {
    selectedGroupCount = 0;
    for (int i = 0; i < groupCount; i++)
      groups[i].selectedIndex = (groups[i].isSelected(bsSelected) ? selectedGroupCount++
          : -1);
  }

  public int selectSeqcodeRange(int index0, int seqcodeA, int seqcodeB,
                                BitSet bs) {
    int seqcode, indexA, indexB, minDiff;
    boolean isInexact = false;
    for (indexA = index0; indexA < groupCount
        && groups[indexA].seqcode != seqcodeA; indexA++) {
    }
    if (indexA == groupCount) {
      // didn't find A exactly -- go find the nearest that is GREATER than this value
      if (index0 > 0)
        return -1;
      isInexact = true;
      minDiff = Integer.MAX_VALUE;
      for (int i = groupCount; --i >= 0;)
        if ((seqcode = groups[i].seqcode) > seqcodeA
            && (seqcode - seqcodeA) < minDiff) {
          indexA = i;
          minDiff = seqcode - seqcodeA;
        }
      if (minDiff == Integer.MAX_VALUE)
        return -1;
    }
    if (seqcodeB == Integer.MAX_VALUE) {
      indexB = groupCount - 1;
      isInexact = true;
    } else {
      for (indexB = indexA; indexB < groupCount
          && groups[indexB].seqcode != seqcodeB; indexB++) {
      }
      if (indexB == groupCount) {
        // didn't find B exactly -- get the nearest that is LESS than this value
        if (index0 > 0)
          return -1;
        isInexact = true;
        minDiff = Integer.MAX_VALUE;
        for (int i = indexA; i < groupCount; i++)
          if ((seqcode = groups[i].seqcode) < seqcodeB
              && (seqcodeB - seqcode) < minDiff) {
            indexB = i;
            minDiff = seqcodeB - seqcode;
          }
        if (minDiff == Integer.MAX_VALUE)
          return -1;
      }
    }
    for (int i = indexA; i <= indexB; ++i)
      groups[i].selectAtoms(bs);
    return (isInexact ? -1 : indexB + 1);
  }
  
  void fixIndices(int atomsDeleted, BitSet bsDeleted) {
    for (int i = 0; i < groupCount; i++)
      groups[i].fixIndices(atomsDeleted, bsDeleted);
  }

  void setAtomBitSet(BitSet bs) {
    for (int i = 0; i < groupCount; i++)
      groups[i].selectAtoms(bs);
  }
}
