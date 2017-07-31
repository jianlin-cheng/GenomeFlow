/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-09-03 16:27:33 -0500 (Mon, 03 Sep 2012) $
 * $Revision: 17501 $

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

package org.jmol.shape;

import java.util.BitSet;
import java.util.Hashtable;
import java.util.Map;

import javax.vecmath.Point3f;
import javax.vecmath.Point3i;

import org.jmol.constant.EnumPalette;
import org.jmol.modelset.Atom;
import org.jmol.modelset.Bond;
import org.jmol.modelset.BondIterator;
import org.jmol.util.BitSetUtil;
import org.jmol.util.Colix;
import org.jmol.util.Escape;
import org.jmol.util.JmolEdge;

public class Sticks extends Shape {

  private int myMask;
  private boolean reportAll;
  
  private BitSet bsOrderSet;
  private BitSet bsSizeSet;
  private BitSet bsColixSet;
  private BitSet selectedBonds;

  @Override
  public void initShape() {
    super.initShape();
    myMask = JmolEdge.BOND_COVALENT_MASK;
    reportAll = false;
  }

  /**
   * sets the size of a bond, or sets the selectedBonds set
   * 
   * @param size
   * @param bsSelected
   */
  @Override
  protected void setSize(int size, BitSet bsSelected) {
    if (size == Integer.MAX_VALUE) {
      selectedBonds = BitSetUtil.copy(bsSelected);
      return;
    }
    if (size == Integer.MIN_VALUE) { // smartaromatic has set the orders directly 
      if (bsOrderSet == null)
        bsOrderSet = new BitSet();
      bsOrderSet.or(bsSelected);
      return;
    }
    if (bsSizeSet == null)
      bsSizeSet = new BitSet();
    BondIterator iter = (selectedBonds != null ? modelSet.getBondIterator(selectedBonds)
        : modelSet.getBondIterator(myMask, bsSelected));
    short mad = (short) size;
    while (iter.hasNext()) {
      bsSizeSet.set(iter.nextIndex());
      iter.next().setMad(mad);
    }
  }

  @Override
  public void setProperty(String propertyName, Object value, BitSet bs) {

    if ("type" == propertyName) {
      myMask = ((Integer) value).intValue();
      return;
    }
    if ("reportAll" == propertyName) {
      // when connections are restored, all we can do is report them all
      reportAll = true;
      return;
    }

    if ("reset" == propertyName) {
      // all bonds have been deleted -- start over
      bsOrderSet = null;
      bsSizeSet = null;
      bsColixSet = null;
      selectedBonds = null;
      return;
    }

    if ("bondOrder" == propertyName) {
      if (bsOrderSet == null)
        bsOrderSet = new BitSet();
      int order = ((Integer) value).shortValue();
      BondIterator iter = (selectedBonds != null ? modelSet.getBondIterator(selectedBonds)
          : modelSet.getBondIterator(JmolEdge.BOND_ORDER_ANY, bs));
      while (iter.hasNext()) {
        bsOrderSet.set(iter.nextIndex());
        iter.next().setOrder(order);
      }
      return;
    }
    if ("color" == propertyName) {
      if (bsColixSet == null)
        bsColixSet = new BitSet();
      short colix = Colix.getColix(value);
      EnumPalette pal = (value instanceof EnumPalette ? (EnumPalette) value : null);
      if (pal == EnumPalette.TYPE || pal == EnumPalette.ENERGY) {
        //only for hydrogen bonds
        boolean isEnergy = (pal == EnumPalette.ENERGY);
        BondIterator iter = (selectedBonds != null ? modelSet.getBondIterator(selectedBonds)
            : modelSet.getBondIterator(myMask, bs));
        while (iter.hasNext()) {
          bsColixSet.set(iter.nextIndex());
          Bond bond = iter.next();
          if (isEnergy) {
              bond.setColix(setColix(colix, pal.id, bond));
              bond.setPaletteID(pal.id);
          } else {
            bond.setColix(Colix.getColix(JmolEdge.getArgbHbondType(bond.order)));
          }
        }
        return;
      }
      if (colix == Colix.USE_PALETTE && pal != EnumPalette.CPK)
        return; //palettes not implemented for bonds
      BondIterator iter = (selectedBonds != null ? modelSet.getBondIterator(selectedBonds)
          : modelSet.getBondIterator(myMask, bs));
      while (iter.hasNext()) {
        int iBond = iter.nextIndex();
        Bond bond = iter.next();
        bond.setColix(colix);
        bsColixSet.set(iBond, (colix != Colix.INHERIT_ALL
            && colix != Colix.USE_PALETTE));
      }
      return;
    }
    if ("translucency" == propertyName) {
      if (bsColixSet == null)
        bsColixSet = new BitSet();
      boolean isTranslucent = (((String) value).equals("translucent"));
      BondIterator iter = (selectedBonds != null ? modelSet.getBondIterator(selectedBonds)
          : modelSet.getBondIterator(myMask, bs));
      while (iter.hasNext()) {
        bsColixSet.set(iter.nextIndex());
        iter.next().setTranslucent(isTranslucent, translucentLevel);
      }
      return;
    }
    
    if ("deleteModelAtoms" == propertyName) {
      return;
    }
    
    super.setProperty(propertyName, value, bs);
  }

  @Override
  public Object getProperty(String property, int index) {
    if (property.equals("selectionState"))
      return (selectedBonds != null ? "select BONDS " + Escape.escape(selectedBonds) + "\n":"");
    if (property.equals("sets"))
      return new BitSet[] { bsOrderSet, bsSizeSet, bsColixSet };
    return null;
  }

  @Override
  public void setModelClickability() {
    Bond[] bonds = modelSet.getBonds();
    for (int i = modelSet.getBondCount(); --i >= 0;) {
      Bond bond = bonds[i];
      if ((bond.getShapeVisibilityFlags() & myVisibilityFlag) == 0
          || modelSet.isAtomHidden(bond.getAtomIndex1())
          || modelSet.isAtomHidden(bond.getAtomIndex2()))
        continue;
      bond.getAtom1().setClickable(myVisibilityFlag);
      bond.getAtom2().setClickable(myVisibilityFlag);
    }
  }

  @Override
  public String getShapeState() {
    Map<String, BitSet> temp = new Hashtable<String, BitSet>();
    Map<String, BitSet> temp2 = new Hashtable<String, BitSet>();
    boolean haveTainted = false;
    Bond[] bonds = modelSet.getBonds();
    short r;
    int bondCount = modelSet.getBondCount();

    if (reportAll || bsSizeSet != null) {
      int i0 = (reportAll ? bondCount - 1 : bsSizeSet.nextSetBit(0));
      for (int i = i0; i >= 0; i = (reportAll ? i - 1 : bsSizeSet
          .nextSetBit(i + 1)))
        setStateInfo(temp, i, "wireframe "
            + ((r = bonds[i].getMad()) == 1 ? "on" : "" + (r / 2000f)));
    }
    if (reportAll || bsOrderSet != null) {
      int i0 = (reportAll ? bondCount - 1 : bsOrderSet.nextSetBit(0));
      for (int i = i0; i >= 0; i = (reportAll ? i - 1 : bsOrderSet
          .nextSetBit(i + 1))) {
        Bond bond = bonds[i];
        if (reportAll || (bond.order & JmolEdge.BOND_NEW) == 0)
          setStateInfo(temp, i, "bondOrder "
              + JmolEdge.getBondOrderNameFromOrder(bond.order));
      }
    }
    if (bsColixSet != null)
      for (int i = bsColixSet.nextSetBit(0); i >= 0; i = bsColixSet
          .nextSetBit(i + 1)) {
        short colix = bonds[i].getColix();
        if ((colix & Colix.OPAQUE_MASK) == Colix.USE_PALETTE)
          setStateInfo(temp, i, getColorCommand("bonds",
              EnumPalette.CPK.id, colix));
        else
          setStateInfo(temp, i, getColorCommand("bonds", colix));
      }

    return getShapeCommands(temp, null, "select BONDS")
        + "\n"
        + (haveTainted ? getShapeCommands(temp2, null, "select BONDS")
            + "\n" : "");
  }
  
  @Override
  public boolean checkObjectHovered(int x, int y, BitSet bsVisible) {
    Point3f pt = new Point3f();
    Bond bond = findPickedBond(x, y, bsVisible, pt);
    if (bond == null)
      return false;
    viewer.highlightBond(bond.index, true);
    return true;
  }
  

  @Override
  public Map<String, Object> checkObjectClicked(int x, int y, int modifiers,
                                    BitSet bsVisible) {
    Point3f pt = new Point3f();
    Bond bond = findPickedBond(x, y, bsVisible, pt);
    if (bond == null)
      return null;
    int modelIndex = bond.getAtom1().modelIndex;
    String info = bond.getIdentity();
    Map<String, Object> map = new Hashtable<String, Object>();
    map.put("pt", pt);
    map.put("index", Integer.valueOf(bond.index));
    map.put("modelIndex", Integer.valueOf(modelIndex));
    map.put("model", viewer.getModelNumberDotted(modelIndex));
    map.put("type", "bond");
    map.put("info", info);
    viewer.setStatusAtomPicked(-3, "[\"bond\",\"" + bond.getIdentity() + "\"," + pt.x + "," + pt.y + "," + pt.z + "]");
    return map;
  }

  private final static int MAX_BOND_CLICK_DISTANCE_SQUARED = 10 * 10;
  private final Point3i ptXY = new Point3i();

  /**
   * 
   * @param x
   * @param y
   * @param bsVisible  UNUSED?
   * @param pt
   * @return picked bond or null
   */
  private Bond findPickedBond(int x, int y, BitSet bsVisible, Point3f pt) {
    int dmin2 = MAX_BOND_CLICK_DISTANCE_SQUARED;
    if (gdata.isAntialiased()) {
      x <<= 1;
      y <<= 1;
      dmin2 <<= 1;
    }
    Bond pickedBond = null;
    Point3f v = new Point3f();
    Bond[] bonds = modelSet.getBonds();
    for (int i = modelSet.getBondCount(); --i >= 0;) {
      Bond bond = bonds[i];
      if (bond.getShapeVisibilityFlags() == 0)
        continue;
      Atom atom1 = bond.getAtom1();
      Atom atom2 = bond.getAtom2();
      if (!atom1.isVisible(0) || !atom2.isVisible(0))
        continue;
      v.set(atom1);
      v.add(atom2);
      v.scale(0.5f);
      int d2 = coordinateInRange(x, y, v, dmin2, ptXY);
      if (d2 >= 0) {
        float f = 1f * (ptXY.x - atom1.screenX) / (atom2.screenX - atom1.screenX);
        if (f < 0.4f || f > 0.6f)
          continue;
        dmin2 = d2;
        pickedBond = bond;
        pt.set(v);
      }
    }
    return pickedBond;
  }
}
