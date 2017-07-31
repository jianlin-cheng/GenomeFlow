/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-09-12 06:45:51 -0500 (Wed, 12 Sep 2012) $
 * $Revision: 17557 $

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


import java.util.BitSet;
import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import org.jmol.api.SymmetryInterface;
import org.jmol.atomdata.RadiusData;
import org.jmol.atomdata.RadiusData.EnumType;
import org.jmol.constant.EnumPalette;
import org.jmol.constant.EnumStructure;
import org.jmol.constant.EnumVdw;
import org.jmol.script.Token;
import org.jmol.util.Colix;
import org.jmol.util.ColorUtil;
import org.jmol.util.Elements;
import org.jmol.util.JmolEdge;
import org.jmol.util.JmolNode;
import org.jmol.util.Point3fi;
import org.jmol.util.Quadric;
import org.jmol.viewer.JmolConstants;
import org.jmol.viewer.Viewer;

final public class Atom extends Point3fi implements JmolNode {

  private final static byte VIBRATION_VECTOR_FLAG = 1;
  private final static byte IS_HETERO_FLAG = 2;
  private final static byte FLAG_MASK = 3;
  
  public static final int RADIUS_MAX = 16;

  char alternateLocationID = '\0';
  public byte atomID;
  int atomSite;
  Group group;
  private float userDefinedVanDerWaalRadius;
  byte valence;
  
  private short atomicAndIsotopeNumber;
  private BitSet atomSymmetry;
  private byte formalChargeAndFlags;

  public byte getAtomID() {
    return atomID;
  }
  
  public short madAtom;

  short colixAtom;
  byte paletteID = EnumPalette.CPK.id;

  Bond[] bonds;
  
  /**
   * 
   * @return  bonds -- WHICH MAY BE NULL
   * 
   */
  public Bond[] getBonds() {
    return bonds;
  }

  
  public void setBonds(Bond[] bonds) {
    this.bonds = bonds;  // for Smiles equating
  }
  
  int nBondsDisplayed = 0;
  int nBackbonesDisplayed = 0;
  
  public int getNBackbonesDisplayed() {
    return nBackbonesDisplayed;
  }
  
  int clickabilityFlags;
  int shapeVisibilityFlags;
  public static final int BACKBONE_VISIBILITY_FLAG = JmolConstants.getShapeVisibilityFlag(JmolConstants.SHAPE_BACKBONE);

  public Atom(int modelIndex, int atomIndex,
        float x, float y, float z, float radius,
        BitSet atomSymmetry, int atomSite,
        short atomicAndIsotopeNumber, int formalCharge, 
        boolean isHetero) {
    this.modelIndex = (short)modelIndex;
    this.atomSymmetry = atomSymmetry;
    this.atomSite = atomSite;
    this.index = atomIndex;
    this.atomicAndIsotopeNumber = atomicAndIsotopeNumber;
    if (isHetero)
      formalChargeAndFlags = IS_HETERO_FLAG;
    setFormalCharge(formalCharge);
    userDefinedVanDerWaalRadius = radius;
    set(x, y, z);
  }

  public void setAltLoc(String altLoc) {
    alternateLocationID = altLoc.charAt(0);
  }
  
  public void setAltLoc(char altLoc) {
    alternateLocationID = altLoc;
  }
  
  public final void setShapeVisibilityFlags(int flag) {
    shapeVisibilityFlags = flag;
  }

  public final void setShapeVisibility(int flag, boolean isVisible) {
    if(isVisible) {
      shapeVisibilityFlags |= flag;        
    } else {
      shapeVisibilityFlags &=~flag;
    }
  }
  
  public boolean isCovalentlyBonded(Atom atomOther) {
    if (bonds != null)
      for (int i = bonds.length; --i >= 0;)
        if (bonds[i].isCovalent() 
            && bonds[i].getOtherAtom(this) == atomOther)
          return true;
    return false;
  }

  public boolean isBonded(Atom atomOther) {
    if (bonds != null)
      for (int i = bonds.length; --i >= 0;)
        if (bonds[i].getOtherAtom(this) == atomOther)
          return true;
    return false;
  }

  public Bond getBond(Atom atomOther) {
    if (bonds != null)
      for (int i = bonds.length; --i >= 0;)
        if (bonds[i].getOtherAtom(atomOther) != null)
          return bonds[i];
    return null;
  }

  void addDisplayedBond(int stickVisibilityFlag, boolean isVisible) {
    nBondsDisplayed += (isVisible ? 1 : -1);
    setShapeVisibility(stickVisibilityFlag, (nBondsDisplayed > 0));
  } 
  
  public void addDisplayedBackbone(int backboneVisibilityFlag, boolean isVisible) {
    nBackbonesDisplayed += (isVisible ? 1 : -1);
    setShapeVisibility(backboneVisibilityFlag, isVisible);
  }
  
  void deleteBond(Bond bond) {
    // this one is used -- from Bond.deleteAtomReferences
    if (bonds != null)
      for (int i = bonds.length; --i >= 0;)
        if (bonds[i] == bond) {
          deleteBond(i);
          return;
        }
  }

  private void deleteBond(int i) {
    int newLength = bonds.length - 1;
    if (newLength == 0) {
      bonds = null;
      return;
    }
    Bond[] bondsNew = new Bond[newLength];
    int j = 0;
    for ( ; j < i; ++j)
      bondsNew[j] = bonds[j];
    for ( ; j < newLength; ++j)
      bondsNew[j] = bonds[j + 1];
    bonds = bondsNew;
  }

  void clearBonds() {
    bonds = null;
  }

  @Override
public int getBondedAtomIndex(int bondIndex) {
    return bonds[bondIndex].getOtherAtom(this).index;
  }

  /*
   * What is a MAR?
   *  - just a term that Miguel made up
   *  - an abbreviation for Milli Angstrom Radius
   * that is:
   *  - a *radius* of either a bond or an atom
   *  - in *millis*, or thousandths of an *angstrom*
   *  - stored as a short
   *
   * However! In the case of an atom radius, if the parameter
   * gets passed in as a negative number, then that number
   * represents a percentage of the vdw radius of that atom.
   * This is converted to a normal MAR as soon as possible
   *
   * (I know almost everyone hates bytes & shorts, but I like them ...
   *  gives me some tiny level of type-checking ...
   *  a rudimentary form of enumerations/user-defined primitive types)
   */

  public void setMadAtom(Viewer viewer, RadiusData rd) {
    madAtom = calculateMad(viewer, rd);
  }
  
  public short calculateMad(Viewer viewer, RadiusData rd) {
    if (rd == null)
      return 0;
    float f = rd.value;
    if (f == 0)
      return 0;
    switch (rd.factorType) {
    case SCREEN:
       return (short) f;
    case FACTOR:
    case OFFSET:
      float r = 0;
      switch (rd.vdwType) {
      case TEMP:
        float tmax = viewer.getBfactor100Hi();
        r = (tmax > 0 ? getBfactor100() / tmax : 0);
        break;
      case HYDRO:
        r = Math.abs(getHydrophobicity());
        break;
      case IONIC:
        r = getBondingRadiusFloat();
        break;
      case ADPMIN:
      case ADPMAX:
        r = getADPMinMax(rd.vdwType == EnumVdw.ADPMAX);
        break;
      default:
        r = getVanderwaalsRadiusFloat(viewer, rd.vdwType);
      }
      if (rd.factorType == EnumType.FACTOR)
        f *= r;
      else
        f += r;
      break;
    case ABSOLUTE:
      break;
    }
    short mad = (short) (f < 0 ? f: f * 2000);
    if (mad < 0 && f > 0)
      mad = 0;
    return mad; 
  }

  public float getADPMinMax(boolean isMax) {
    Quadric[] ellipsoid = getEllipsoid();
    return (ellipsoid == null ? 0 : ellipsoid[0] == null ? 
        ellipsoid[1].lengths[isMax ? 2 : 0] * ellipsoid[1].scale 
        : ellipsoid[0].lengths[isMax ? 2 : 0] * ellipsoid[0].scale);
  }

  public int getRasMolRadius() {
    return Math.abs(madAtom / 8); //  1000r = 1000d / 2; rr = (1000r / 4);
  }

  @Override
public int getCovalentBondCount() {
    if (bonds == null)
      return 0;
    int n = 0;
    Bond b;
    for (int i = bonds.length; --i >= 0; )
      if (((b = bonds[i]).order & JmolEdge.BOND_COVALENT_MASK) != 0
          && !b.getOtherAtom(this).isDeleted())
        ++n;
    return n;
  }

  @Override
public int getCovalentHydrogenCount() {
    if (bonds == null)
      return 0;
    int n = 0;
    for (int i = bonds.length; --i >= 0; ) {
      if ((bonds[i].order & JmolEdge.BOND_COVALENT_MASK) == 0)
        continue;
      Atom a = bonds[i].getOtherAtom(this);
      if (a.valence >= 0 && a.getElementNumber() == 1)
        ++n;
    }
    return n;
  }

  @Override
public JmolEdge[] getEdges() {
    return bonds;
  }
  
  public void setColixAtom(short colixAtom) {
    this.colixAtom = colixAtom;
  }

  public void setPaletteID(byte paletteID) {
    this.paletteID = paletteID;
  }

  public void setTranslucent(boolean isTranslucent, float translucentLevel) {
    colixAtom = Colix.getColixTranslucent(colixAtom, isTranslucent, translucentLevel);    
  }

  public boolean isTranslucent() {
    return Colix.isColixTranslucent(colixAtom);
  }

  @Override
public short getElementNumber() {
    return Elements.getElementNumber(atomicAndIsotopeNumber);
  }
  
  @Override
public short getIsotopeNumber() {
    return Elements.getIsotopeNumber(atomicAndIsotopeNumber);
  }
  
  @Override
public short getAtomicAndIsotopeNumber() {
    return atomicAndIsotopeNumber;
  }

  public void setAtomicAndIsotopeNumber(int n) {
    if (n < 0 || (n % 128) >= Elements.elementNumberMax || n > Short.MAX_VALUE)
      n = 0;
    atomicAndIsotopeNumber = (short) n;
  }

  public String getElementSymbol(boolean withIsotope) {
    return Elements.elementSymbolFromNumber(withIsotope ? atomicAndIsotopeNumber : atomicAndIsotopeNumber % 128);    
  }
  
  public String getElementSymbol() {
    return getElementSymbol(true);
  }

  public char getAlternateLocationID() {
    return alternateLocationID;
  }
  
  boolean isAlternateLocationMatch(String strPattern) {
    if (strPattern == null)
      return (alternateLocationID == '\0');
    if (strPattern.length() != 1)
      return false;
    char ch = strPattern.charAt(0);
    return (ch == '*' 
        || ch == '?' && alternateLocationID != '\0' 
        || alternateLocationID == ch);
  }

  public boolean isHetero() {
    return (formalChargeAndFlags & IS_HETERO_FLAG) != 0;
  }

  public boolean hasVibration() {
    return (formalChargeAndFlags & VIBRATION_VECTOR_FLAG) != 0;
  }

  public void setFormalCharge(int charge) {
    formalChargeAndFlags = (byte)((formalChargeAndFlags & FLAG_MASK) 
        | ((charge == Integer.MIN_VALUE ? 0 : charge > 7 ? 7 : charge < -3 ? -3 : charge) << 2));
  }
  
  void setVibrationVector() {
    formalChargeAndFlags |= VIBRATION_VECTOR_FLAG;
  }
  
  @Override
public int getFormalCharge() {
    //System.out.println("Atom " + this + " " + this.formalChargeAndFlags);
    return formalChargeAndFlags >> 2;
  }

  // a percentage value in the range 0-100
  public int getOccupancy100() {
    byte[] occupancies = group.chain.model.modelSet.occupancies;
    return occupancies == null ? 100 : occupancies[index];
  }

  // This is called bfactor100 because it is stored as an integer
  // 100 times the bfactor(temperature) value
  public int getBfactor100() {
    short[] bfactor100s = group.chain.model.modelSet.bfactor100s;
    if (bfactor100s == null)
      return 0;
    return bfactor100s[index];
  }

  private float getHydrophobicity() {
    float[] values = group.chain.model.modelSet.hydrophobicities;
    if (values == null)
      return Elements.getHydrophobicity(group.getGroupID());
    return values[index];
  }

  public boolean setRadius(float radius) {
    return !Float.isNaN(userDefinedVanDerWaalRadius = (radius > 0 ? radius : Float.NaN));  
  }
  
  public void delete(BitSet bsBonds) {
    valence = -1;
    if (bonds != null)
      for (int i = bonds.length; --i >= 0; ) {
        Bond bond = bonds[i];
        bond.getOtherAtom(this).deleteBond(bond);
        bsBonds.set(bond.index);
      }
    bonds = null;
  }

  @Override
public boolean isDeleted() {
    return (valence < 0);
  }

  public void setValence(int nBonds) {
    if (isDeleted()) // no resurrection
      return;
    valence = (byte) (nBonds < 0 ? 0 : nBonds < 0xEF ? nBonds : 0xEF);
  }

  @Override
public int getValence() {
    if (isDeleted())
      return -1;
    int n = valence;
    if (n == 0 && bonds != null)
      for (int i = bonds.length; --i >= 0;)
        n += bonds[i].getValence();
    return n;
  }

  @Override
public int getImplicitHydrogenCount() {
    return group.chain.model.modelSet.getImplicitHydrogenCount(this);
  }

  int getTargetValence() {
    switch (getElementNumber()) {
    case 6: //C
    case 14: //Si      
      return 4;
    case 5:  // B
    case 7:  // N
    case 15: // P
      return 3;
    case 8: //O
    case 16: //S
      return 2;
    case 9: // F
    case 17: // Cl
    case 35: // Br
    case 53: // I
      return 1;
    }
    return -1;
  }


  public float getDimensionValue(int dimension) {
    return (dimension == 0 ? x : (dimension == 1 ? y : z));
  }

  public float getVanderwaalsRadiusFloat(Viewer viewer, EnumVdw type) {
    // called by atomPropertyFloat as VDW_AUTO,
    // AtomCollection.fillAtomData with VDW_AUTO or VDW_NOJMOL
    // AtomCollection.findMaxRadii with VDW_AUTO
    // AtomCollection.getAtomPropertyState with VDW_AUTO
    // AtomCollection.getVdwRadius with passed on type
    return (Float.isNaN(userDefinedVanDerWaalRadius) 
        ? viewer.getVanderwaalsMar(atomicAndIsotopeNumber % 128, getVdwType(type)) / 1000f
        : userDefinedVanDerWaalRadius);
  }

  /**
   * 
   * @param type 
   * @return if VDW_AUTO, will return VDW_AUTO_JMOL, VDW_AUTO_RASMOL, or VDW_AUTO_BABEL
   *         based on the model type
   */
  @SuppressWarnings("incomplete-switch")
  private EnumVdw getVdwType(EnumVdw type) {
    switch (type) {
    case AUTO:
      type = group.chain.model.modelSet.getDefaultVdwType(modelIndex);
      break;
    case NOJMOL:
      type = group.chain.model.modelSet.getDefaultVdwType(modelIndex);
      if (type == EnumVdw.AUTO_JMOL)
        type = EnumVdw.AUTO_BABEL;
      break;
    }
    return type;
  }

  private float getCovalentRadiusFloat() {
    return Elements.getBondingRadiusFloat(atomicAndIsotopeNumber, 0);
  }

  float getBondingRadiusFloat() {
    float[] ionicRadii = group.chain.model.modelSet.ionicRadii;
    float r = (ionicRadii == null ? 0 : ionicRadii[index]);
    return (r == 0 ? Elements.getBondingRadiusFloat(atomicAndIsotopeNumber,
        getFormalCharge()) : r);
  }

  float getVolume(Viewer viewer, EnumVdw vType) {
    float r1 = (vType == null ? userDefinedVanDerWaalRadius : Float.NaN);
    if (Float.isNaN(r1))
      r1 = viewer.getVanderwaalsMar(getElementNumber(), getVdwType(vType)) / 1000f;
    double volume = 0;
    if (bonds != null)
      for (int j = 0; j < bonds.length; j++) {
        if (!bonds[j].isCovalent())
          continue;
        Atom atom2 = bonds[j].getOtherAtom(this);
        float r2 = (vType == null ? atom2.userDefinedVanDerWaalRadius : Float.NaN);
        if (Float.isNaN(r2))
          r2 = viewer.getVanderwaalsMar(atom2.getElementNumber(), atom2
              .getVdwType(vType)) / 1000f;
        float d = distance(atom2);
        if (d > r1 + r2)
          continue;
        if (d + r1 <= r2)
          return 0;

        // calculate hidden spherical cap height and volume
        // A.Bondi, J. Phys. Chem. 68, 1964, 441-451.

        double h = r1 - (r1 * r1 + d * d - r2 * r2) / (2.0 * d);
        volume -= Math.PI / 3 * h * h * (3 * r1 - h);
      }
    return (float) (volume + 4 * Math.PI / 3 * r1 * r1 * r1);
  }

  int getCurrentBondCount() {
    return bonds == null ? 0 : bonds.length;
  }

  public short getColix() {
    return colixAtom;
  }

  public byte getPaletteID() {
    return paletteID;
  }

  float getRadius() {
    return Math.abs(madAtom / (1000f * 2));
  }

  @Override
public int getIndex() {
    return index;
  }

  @Override
public int getAtomSite() {
    return atomSite;
  }

  public void setAtomSymmetry(BitSet bsSymmetry) {
    atomSymmetry = bsSymmetry;
  }

  public BitSet getAtomSymmetry() {
    return atomSymmetry;
  }

   void setGroup(Group group) {
     this.group = group;
   }

   public Group getGroup() {
     return group;
   }
   
   @Override
public void getGroupBits(BitSet bs) {
     group.selectAtoms(bs);
   }
   
   @Override
public String getAtomName() {
     return (atomID > 0 ? JmolConstants.getSpecialAtomName(atomID) 
         : group.chain.model.modelSet.atomNames[index]);
   }
   
   //added -hcf
   public int getChrScaleNumber() {
	     return chrScaleNumber;
	}
   public int getLociScaleNumber() {
	     return lociScaleNumber;
	}
   public int getFiberScaleNumber() {
	     return fiberScaleNumber;
	}
   public int getNucleoScaleNumber() {
	     return nucleoScaleNumber;
	}
   public int getChrID() {
	   return chrID;
   }
   public int getFromPos() {
	   return fromPos;
   }
   public int getEndPos() {
	   return endPos;
   }

   public String getSpName() {
	   return spName;
   }
   public String getEnsChr() {
	   return ensChr;
   }
   public String getLcChr() {
	   return lcChr;
   }
   //added end -hcf
   
   
   @Override
public String getAtomType() {
    String[] atomTypes = group.chain.model.modelSet.atomTypes;
    String type = (atomTypes == null ? null : atomTypes[index]);
    return (type == null ? getAtomName() : type);
  }
   
   public int getAtomNumber() {
     int[] atomSerials = group.chain.model.modelSet.atomSerials;
     // shouldn't ever be null.
     return (atomSerials != null ? atomSerials[index] : index);
//        : group.chain.model.modelSet.isZeroBased ? atomIndex : atomIndex);
   }

   public boolean isInFrame() {
     return ((shapeVisibilityFlags & JmolConstants.ATOM_IN_FRAME) != 0);
   }

   public int getShapeVisibilityFlags() {
     return shapeVisibilityFlags;
   }
   
   public boolean isShapeVisible(int shapeVisibilityFlag) {
     return ((shapeVisibilityFlags & shapeVisibilityFlag) != 0);
   }

   public float getPartialCharge() {
     float[] partialCharges = group.chain.model.modelSet.partialCharges;
     return partialCharges == null ? 0 : partialCharges[index];
   }

   public Quadric[] getEllipsoid() {
     return group.chain.model.modelSet.getEllipsoid(index);
   }

   public void scaleEllipsoid(int size, int iSelect) {
     Quadric[] ellipsoid = getEllipsoid();
     if (ellipsoid == null || iSelect >= ellipsoid.length || ellipsoid[iSelect] == null)
       return;
     ellipsoid[iSelect].setSize(size);
   }

   /**
    * Given a symmetry operation number, the set of cells in the model, and the
    * number of operations, this method returns either 0 or the cell number (555, 666)
    * of the translated symmetry operation corresponding to this atom.
    * 
    * atomSymmetry is a bitset that is created in adapter.smarter.AtomSetCollection
    * 
    * It is arranged as follows:
    * 
    * |--overall--|---cell1---|---cell2---|---cell3---|...
    * 
    * |012..nOps-1|012..nOps-1|012..nOp-1s|012..nOps-1|...
    * 
    * If a bit is set, it means that the atom was created using that operator
    * operating on the base file set and translated for that cell.
    * 
    * If any bit is set in any of the cell blocks, then the same
    * bit will also be set in the overall block. This allows for
    * rapid determination of special positions and also of
    * atom membership in any operation set.
    * 
    *  Note that it is not necessarily true that an atom is IN the designated
    *  cell, because one can load {nnn mmm 0}, and then, for example, the {-x,-y,-z}
    *  operator sends atoms from 555 to 444. Still, those atoms would be marked as
    *  cell 555 here, because no translation was carried out. 
    *  
    *  That is, the numbers 444 in symop=3444 do not refer to a cell, per se. 
    *  What they refer to is the file-designated operator plus a translation of
    *  {-1 -1 -1/1}. 
    * 
    * @param symop        = 0, 1, 2, 3, ....
    * @param cellRange    = {444, 445, 446, 454, 455, 456, .... }
    * @param nOps         = 2 for x,y,z;-x,-y,-z, for example
    * @return cell number such as 565
    */
   public int getSymmetryTranslation(int symop, int[] cellRange, int nOps) {
     int pt = symop;
     for (int i = 0; i < cellRange.length; i++)
       if (atomSymmetry.get(pt += nOps))
         return cellRange[i];
     return 0;
   }
   
   /**
    * Looks for a match in the cellRange list for this atom within the specified translation set
    * select symop=0NNN for this
    * 
    * @param cellNNN
    * @param cellRange
    * @param nOps
    * @return     matching cell number, if applicable
    */
   public int getCellTranslation(int cellNNN, int[] cellRange, int nOps) {
     int pt = nOps;
     for (int i = 0; i < cellRange.length; i++)
       for (int j = 0; j < nOps;j++, pt++)
       if (atomSymmetry.get(pt) && cellRange[i] == cellNNN)
         return cellRange[i];
     return 0;
   }
   
   String getSymmetryOperatorList() {
    String str = "";
    ModelSet f = group.chain.model.modelSet;
    int nOps = f.getModelSymmetryCount(modelIndex);
    if (nOps == 0 || atomSymmetry == null)
      return "";
    int[] cellRange = f.getModelCellRange(modelIndex);
    int pt = nOps;
    int n = (cellRange == null ? 1 : cellRange.length);
    for (int i = 0; i < n; i++)
      for (int j = 0; j < nOps; j++)
        if (atomSymmetry.get(pt++))
          str += "," + (j + 1) + "" + cellRange[i];
    return str.substring(1);
  }
   
  @Override
public int getModelIndex() {
    return modelIndex;
  }
   
  int getMoleculeNumber(boolean inModel) {
    return (group.chain.model.modelSet.getMoleculeIndex(index, inModel) + 1);
  }
   
  private float getFractionalCoord(char ch, boolean asAbsolute) {
    Point3f pt = getFractionalCoord(asAbsolute);
    return (ch == 'X' ? pt.x : ch == 'Y' ? pt.y : pt.z);
  }
    
  private float getFractionalUnitCoord(char ch) {
    Point3f pt = getFractionalUnitCoord(false);
    return (ch == 'X' ? pt.x : ch == 'Y' ? pt.y : pt.z);
  }

  private Point3f getFractionalCoord(boolean asAbsolute) {
    // asAbsolute TRUE uses the original unshifted matrix
    SymmetryInterface c = group.chain.model.modelSet.getUnitCell(modelIndex);
    if (c == null) 
      return this;
    Point3f pt = new Point3f(this);
    c.toFractional(pt, asAbsolute);
    return pt;
  }
  
  Point3f getFractionalUnitCoord(boolean asCartesian) {
    SymmetryInterface c = group.chain.model.modelSet.getUnitCell(modelIndex);
    if (c == null)
      return this;
    Point3f pt = new Point3f(this);
    if (group.chain.model.isJmolDataFrame) {
      c.toFractional(pt, false);
      if (asCartesian)
        c.toCartesian(pt, false);
    } else {
      c.toUnitCell(pt, null);
      if (!asCartesian)
        c.toFractional(pt, false);
    }
    return pt;
  }
  
  float getFractionalUnitDistance(Point3f pt, Point3f ptTemp1, Point3f ptTemp2) {
    SymmetryInterface c = group.chain.model.modelSet.getUnitCell(modelIndex);
    if (c == null) 
      return distance(pt);
    ptTemp1.set(this);
    ptTemp2.set(pt);
    if (group.chain.model.isJmolDataFrame) {
      c.toFractional(ptTemp1, true);
      c.toFractional(ptTemp2, true);
    } else {
      c.toUnitCell(ptTemp1, null);
      c.toUnitCell(ptTemp2, null);
    }
    return ptTemp1.distance(ptTemp2);
  }
  
  void setFractionalCoord(int tok, float fValue, boolean asAbsolute) {
    SymmetryInterface c = group.chain.model.modelSet.getUnitCell(modelIndex);
    if (c != null)
      c.toFractional(this, asAbsolute);
    switch (tok) {
    case Token.fux:
    case Token.fracx:
      x = fValue;
      break;
    case Token.fuy:
    case Token.fracy:
      y = fValue;
      break;
    case Token.fuz:
    case Token.fracz:
      z = fValue;
      break;
    }
    if (c != null)
      c.toCartesian(this, asAbsolute);
  }
  
  void setFractionalCoord(Point3f ptNew, boolean asAbsolute) {
    setFractionalCoord(this, ptNew, asAbsolute);
  }
  
  void setFractionalCoord(Point3f pt, Point3f ptNew, boolean asAbsolute) {
    pt.set(ptNew);
    SymmetryInterface c = group.chain.model.modelSet.getUnitCell(modelIndex);
    if (c != null)
      c.toCartesian(pt, asAbsolute && !group.chain.model.isJmolDataFrame);
  }

  boolean isCursorOnTopOf(int xCursor, int yCursor,
                        int minRadius, Atom competitor) {
    int r = screenDiameter / 2;
    if (r < minRadius)
      r = minRadius;
    int r2 = r * r;
    int dx = screenX - xCursor;
    int dx2 = dx * dx;
    if (dx2 > r2)
      return false;
    int dy = screenY - yCursor;
    int dy2 = dy * dy;
    int dz2 = r2 - (dx2 + dy2);
    if (dz2 < 0)
      return false;
    if (competitor == null)
      return true;
    int z = screenZ;
    int zCompetitor = competitor.screenZ;
    int rCompetitor = competitor.screenDiameter / 2;
    if (z < zCompetitor - rCompetitor)
      return true;
    int dxCompetitor = competitor.screenX - xCursor;
    int dx2Competitor = dxCompetitor * dxCompetitor;
    int dyCompetitor = competitor.screenY - yCursor;
    int dy2Competitor = dyCompetitor * dyCompetitor;
    int r2Competitor = rCompetitor * rCompetitor;
    int dz2Competitor = r2Competitor - (dx2Competitor + dy2Competitor);
    return (z - Math.sqrt(dz2) < zCompetitor - Math.sqrt(dz2Competitor));
  }

  /*
   *  DEVELOPER NOTE (BH):
   *  
   *  The following methods may not return 
   *  correct values until after modelSet.finalizeGroupBuild()
   *  
   */
   
  public String getInfo() {
    return getIdentity(true);
  } 

  String getInfoXYZ(boolean useChimeFormat) {
    // for atom picking
    if (useChimeFormat) {
      String group3 = getGroup3(true);
      char chainID = getChainID();
      Point3f pt = getFractionalCoord(true);
      return "Atom: " + (group3 == null ? getElementSymbol() : getAtomName()) + " " + getAtomNumber() 
          + (group3 != null && group3.length() > 0 ? 
              (isHetero() ? " Hetero: " : " Group: ") + group3 + " " + getResno() 
              + (chainID != 0 && chainID != ' ' ? " Chain: " + chainID : "")              
              : "")
          + " Model: " + getModelNumber()
          + " Coordinates: " + x + " " + y + " " + z
          + (pt == null ? "" : " Fractional: "  + pt.x + " " + pt.y + " " + pt.z); 
    }
    return getIdentityXYZ(true);
  }

  String getIdentityXYZ(boolean allInfo) {
    Point3f pt = (group.chain.model.isJmolDataFrame ? getFractionalCoord(false) : this);
    //modified -hcf
    //return getIdentity(allInfo) + " " + pt.x + " " + pt.y + " " + pt.z;
    return getUnitIdentity(allInfo);
    //return getUnitIdentity(allInfo) + " " + pt.x + " " + pt.y + " " + pt.z;//for correction checking
    //modified end -hcf
  }
  //added -hcf
  String getUnitIdentity(boolean allInfo) {
	  StringBuffer info = new StringBuffer();
	  int chrScale = getChrScaleNumber();
	  int lociScale = getLociScaleNumber();
	  int fiberScale = getFiberScaleNumber();
	  int nucleoScale = getNucleoScaleNumber();
	  
	  if (nucleoScale != 0) {
		  String tmpString = "<CHR(" + chrScale + ")," + "LOC(" + lociScale + ")," + "FIB(" + fiberScale + ")," + "NUC(" + nucleoScale + ")>";
		  info.append(tmpString);
	  }
	  else if (fiberScale != 0) {
		  String tmpString = "<CHR(" + chrScale + ")," + "LOC(" + lociScale + ")," + "FIB(" + fiberScale + ")>";
		  info.append(tmpString);
	  }
	  else if (lociScale != 0) {
		  String tmpString = "<CHR(" + chrScale + ")," + "LOC(" + lociScale + ")>";
		  info.append(tmpString);
	  }
	  else if (chrScale != 0) {
		  String tmpString = "<CHR(" + chrScale + ")>";
		  info.append(tmpString);
	  }
	  else {
		  //no info
	  }
	/*  
	  if (allInfo) {
		  info.append("<");
		  info.append(getChrScaleNumber());
		  info.append(",");
		  info.append(getLociScaleNumber());
		  info.append(",");		  
		  info.append(getFiberScaleNumber());
		  info.append(",");	
		  info.append(getNucleoScaleNumber());
		  info.append(">");		  
	  }
	  */
	  return info.toString();
  }
  
  String getGenomeInfo() {
	  StringBuffer info = new StringBuffer();
	  String sepc = getSpName();
	  String ensChr = getEnsChr();
	  int chrScale = getChrScaleNumber();
	  int fromPos = getFromPos();
	  int endPos = getEndPos();
	  String lcSeqFile = getLcChr();
	  String tmpString = sepc + "," + chrScale + "," + ensChr + "," + fromPos + "," + endPos + "," + lcSeqFile;
	  return info.append(tmpString).toString();
  }

//added end -hcf  

  
  String getIdentity(boolean allInfo) {
    StringBuffer info = new StringBuffer();
    String group3 = getGroup3(true);
    if (group3 != null && group3.length() > 0) {
      info.append("[");
      info.append(group3);
      info.append("]");
      String seqcodeString = getSeqcodeString();
      if (seqcodeString != null)
        info.append(seqcodeString);
      char chainID = getChainID();
      if (chainID != 0 && chainID != ' ') {
        info.append(":");
        info.append(chainID);
      }
      if (!allInfo)
        return info.toString();
      info.append(".");
    }
    //info.append(getAtomName());
    if (info.length() == 0) {
      // since atomName cannot be null, this is unreachable
      //info.append(getElementSymbol(false));
      info.append(" ");
      //info.append(getAtomNumber());
    }
    if (alternateLocationID != 0) {
      info.append("%");
      info.append(alternateLocationID);
    }
    if (group.chain.model.modelSet.getModelCount() > 1) {
      info.append("/");
      info.append(getModelNumberForLabel());
    }
    //info.append(" #");
    //info.append(getAtomNumber());
    info.append(getUnitIdentity(true));
    return info.toString();
  }

  @Override
public String getGroup3(boolean allowNull) {
    String group3 = group.getGroup3();
    return (allowNull 
        || group3 != null && group3.length() > 0 
        ? group3 : "UNK");
  }

  @Override
public String getGroup1(char c0) {
    char c = group.getGroup1();
    return (c != '\0' ? "" + c : c0 != '\0' ? "" + c0 : "");
  }

  @Override
public boolean isProtein() {
    return group.isProtein();
  }

  boolean isCarbohydrate() {
    return group.isCarbohydrate();
  }

  @Override
public boolean isNucleic() {
    return group.isNucleic();
  }

  @Override
public boolean isDna() {
    return group.isDna();
  }
  
  @Override
public boolean isRna() {
    return group.isRna();
  }

  @Override
public boolean isPurine() {
    return group.isPurine();
  }

  @Override
public boolean isPyrimidine() {
    return group.isPyrimidine();
  }

  int getSeqcode() {
    return group.getSeqcode();
  }

  @Override
public int getResno() {
    return group.getResno();   
  }

  public boolean isClickable() {
    // certainly if it is not visible, then it can't be clickable
    if (!isVisible(0))
      return false;
    int flags = shapeVisibilityFlags | group.shapeVisibilityFlags;
    return ((flags & clickabilityFlags) != 0);
  }

  public int getClickabilityFlags() {
    return clickabilityFlags;
  }
  
  public void setClickable(int flag) {
    if (flag == 0)
      clickabilityFlags = 0;
    else
      clickabilityFlags |= flag;
  }
  
  /**
   * determine if an atom or its PDB group is visible
   * @param flags TODO
   * @return true if the atom is in the "select visible" set
   */
  public boolean isVisible(int flags) {
    // Is the atom's model visible? Is the atom NOT hidden?
    if (!isInFrame() || group.chain.model.modelSet.isAtomHidden(index))
      return false;
    // Is any shape associated with this atom visible?
    if (flags != 0)
      return (isShapeVisible(flags));  
    flags = shapeVisibilityFlags;
    // Is its PDB group visible in any way (cartoon, e.g.)?
    //  An atom is considered visible if its PDB group is visible, even
    //  if it does not show up itself as part of the structure
    //  (this will be a difference in terms of *clickability*).
    // except BACKBONE -- in which case we only see the lead atoms
    if (group.shapeVisibilityFlags != Atom.BACKBONE_VISIBILITY_FLAG
        || isLeadAtom())
      flags |= group.shapeVisibilityFlags;

    // We know that (flags & AIM), so now we must remove that flag
    // and check to see if any others are remaining.
    // Only then is the atom considered visible.
    return ((flags & ~JmolConstants.ATOM_IN_FRAME) != 0);
  }

  @Override
public boolean isLeadAtom() {
    return group.isLeadAtom(index);
  }
  
  public float getGroupParameter(int tok) {
    return group.getGroupParameter(tok);
  }

  @Override
public char getChainID() {
    return group.chain.chainID;
  }

  public int getSurfaceDistance100() {
    return group.chain.model.modelSet.getSurfaceDistance100(index);
  }

  public Vector3f getVibrationVector() {
    return group.chain.model.modelSet.getVibrationVector(index, false);
  }

  public float getVibrationCoord(char ch) {
    return group.chain.model.modelSet.getVibrationCoord(index, ch);
  }


  public int getPolymerLength() {
    return group.getBioPolymerLength();
  }

  public int getPolymerIndexInModel() {
    return group.getBioPolymerIndexInModel();
  }

  public int getMonomerIndex() {
    return group.getMonomerIndex();
  }
  
  public int getSelectedGroupCountWithinChain() {
    return group.chain.selectedGroupCount;
  }

  public int getSelectedGroupIndexWithinChain() {
    return group.getSelectedGroupIndex();
  }

  public int getSelectedMonomerCountWithinPolymer() {
    return group.getSelectedMonomerCount();
  }

  public int getSelectedMonomerIndexWithinPolymer() {
    return group.getSelectedMonomerIndex();
  }

  Chain getChain() {
    return group.chain;
  }

  String getModelNumberForLabel() {
    return group.chain.model.modelSet.getModelNumberForAtomLabel(modelIndex);
  }
  
  public int getModelNumber() {
    return group.chain.model.modelSet.getModelNumber(modelIndex) % 1000000;
  }
  
  public int getModelFileIndex() {
    return group.chain.model.fileIndex;
  }
  
  public int getModelFileNumber() {
    return group.chain.model.modelSet.getModelFileNumber(modelIndex);
  }
  
  @Override
public String getBioStructureTypeName() {
    return getProteinStructureType().getBioStructureTypeName(true);
  }
  
  public EnumStructure getProteinStructureType() {
    return group.getProteinStructureType();
  }
  
  public EnumStructure getProteinStructureSubType() {
    return group.getProteinStructureSubType();
  }
  
  public int getStrucNo() {
    return group.getStrucNo();
  }

  public String getStructureId() {
    return group.getStructureId();
  }

  public String getProteinStructureTag() {
    return group.getProteinStructureTag();
  }

  public short getGroupID() {
    return group.groupID;
  }

  public String getSeqcodeString() {
    return group.getSeqcodeString();
  }

  int getSeqNumber() {
    return group.getSeqNumber();
  }

  public char getInsertionCode() {
    return group.getInsertionCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    return (this == obj);
  }

  @Override
  public int hashCode() {
    //this overrides the Point3fi hashcode, which would
    //give a different hashcode for an atom depending upon
    //its screen location! Bug fix for 11.1.43 Bob Hanson
    return index;
  }
  
  public Atom findAromaticNeighbor(int notAtomIndex) {
    if (bonds == null)
      return null;
    for (int i = bonds.length; --i >= 0; ) {
      Bond bondT = bonds[i];
      Atom a = bondT.getOtherAtom(this);
      if (bondT.isAromatic() && a.index != notAtomIndex)
        return a;
    }
    return null;
  }

  /**
   * called by isosurface and int comparator via atomProperty()
   * and also by getBitsetProperty() 
   * 
   * @param atom
   * @param tokWhat
   * @return         int value or Integer.MIN_VALUE
   */
  public static int atomPropertyInt(Atom atom, int tokWhat) {
    switch (tokWhat) {
    case Token.atomno:
      return atom.getAtomNumber();
    case Token.atomid:
      return atom.atomID;
    case Token.atomindex:
      return atom.getIndex();
    case Token.bondcount:
      return atom.getCovalentBondCount();
    case Token.color:
      return atom.group.chain.model.modelSet.viewer.getColorArgbOrGray(atom.getColix());
    case Token.element:
    case Token.elemno:
      return atom.getElementNumber();
    case Token.elemisono:
      return atom.atomicAndIsotopeNumber;
    case Token.file:
      return atom.getModelFileIndex() + 1;
    case Token.formalcharge:
      return atom.getFormalCharge();
    case Token.groupid:
      return atom.getGroupID(); //-1 if no group
    case Token.groupindex:
      return atom.group.getGroupIndex();
    case Token.model:
      //integer model number -- could be PDB/sequential adapter number
      //or it could be a sequential model in file number when multiple files
      return atom.getModelNumber();
    case -Token.model:
      //float is handled differently
      return atom.getModelFileNumber();
    case Token.modelindex:
      return atom.modelIndex;
    case Token.molecule:
      return atom.getMoleculeNumber(true);
    case Token.occupancy:
      return atom.getOccupancy100();
    case Token.polymer:
      return atom.getGroup().getBioPolymerIndexInModel() + 1;
    case Token.polymerlength:
      return atom.getPolymerLength();
    case Token.radius:
      // the comparator uses rasmol radius, unfortunately, for integers
      return atom.getRasMolRadius();        
    case Token.resno:
      return atom.getResno();
    case Token.site:
      return atom.getAtomSite();
    case Token.structure:
      return atom.getProteinStructureType().getId();
    case Token.substructure:
      return atom.getProteinStructureSubType().getId();
    case Token.strucno:
      return atom.getStrucNo();
    case Token.valence:
      return atom.getValence();
    }
    return 0;      
  }

  /**
   * called by isosurface and int comparator via atomProperty() and also by
   * getBitsetProperty()
   * 
   * @param viewer
   * 
   * @param atom
   * @param tokWhat
   * @return float value or value*100 (asInt=true) or throw an error if not
   *         found
   * 
   */
  public static float atomPropertyFloat(Viewer viewer, Atom atom, int tokWhat) {
    switch (tokWhat) {
    case Token.radius:
      return atom.getRadius();
    case Token.selected:
      return (viewer.isAtomSelected(atom.index) ? 1 : 0);
    case Token.surfacedistance:
      atom.group.chain.model.modelSet.getSurfaceDistanceMax();
      return atom.getSurfaceDistance100() / 100f;
    case Token.atomsequence:                             //added lxq35
        atom.group.chain.model.modelSet.getAtomSequenceMax();
        return atom.getSurfaceDistance100() / 100f;
    case Token.temperature: // 0 - 9999
      return atom.getBfactor100() / 100f;
    case Token.hydrophobic:
      return atom.getHydrophobicity();
    case Token.volume:
      return atom.getVolume(viewer, EnumVdw.AUTO);

      // these next have to be multiplied by 100 if being compared
      // note that spacefill here is slightly different than radius -- no integer option

    case Token.adpmax:
      return atom.getADPMinMax(true);
    case Token.adpmin:
      return atom.getADPMinMax(false);
    case Token.atomx:
    case Token.x:
      return atom.x;
    case Token.atomy:
    case Token.y:
      return atom.y;
    case Token.atomz:
    case Token.z:
      return atom.z;
    case Token.covalent:
      return atom.getCovalentRadiusFloat();
    case Token.fracx:
      return atom.getFractionalCoord('X', true);
    case Token.fracy:
      return atom.getFractionalCoord('Y', true);
    case Token.fracz:
      return atom.getFractionalCoord('Z', true);
    case Token.fux:
      return atom.getFractionalCoord('X', false);
    case Token.fuy:
      return atom.getFractionalCoord('Y', false);
    case Token.fuz:
      return atom.getFractionalCoord('Z', false);
    case Token.screenx:
      return atom.screenX;
    case Token.screeny:
      return atom.group.chain.model.modelSet.viewer.getScreenHeight() - atom.screenY;
    case Token.screenz:
      return atom.screenZ;
    case Token.ionic:
      return atom.getBondingRadiusFloat();
    case Token.mass:
      return atom.getMass();
    case Token.occupancy:
      return atom.getOccupancy100() / 100f;
    case Token.partialcharge:
      return atom.getPartialCharge();
    case Token.phi:
    case Token.psi:
    case Token.omega:
      if (atom.group.chain.model.isJmolDataFrame
          && atom.group.chain.model.jmolFrameType
              .startsWith("plot ramachandran")) {
        switch (tokWhat) {
        case Token.phi:
          return atom.getFractionalCoord('X', false);
        case Token.psi:
          return atom.getFractionalCoord('Y', false);
        case Token.omega:
          if (atom.group.chain.model.isJmolDataFrame
              && atom.group.chain.model.jmolFrameType
                  .equals("plot ramachandran")) {
            float omega = atom.getFractionalCoord('Z', false) - 180;
            return (omega < -180 ? 360 + omega : omega);
          }
        }
      }
      return atom.getGroupParameter(tokWhat);
    case Token.eta:
    case Token.theta:
    case Token.straightness:
      return atom.getGroupParameter(tokWhat);
    case Token.spacefill:
      return atom.getRadius();
    case Token.backbone:
    case Token.cartoon:
    case Token.dots:
    case Token.ellipsoid:
    case Token.geosurface:
    case Token.halo:
    case Token.meshRibbon:
    case Token.ribbon:
    case Token.rocket:
    case Token.star:
    case Token.strands:
    case Token.trace:
      return viewer.getAtomShapeValue(tokWhat, atom.group, atom.index);
    case Token.unitx:
      return atom.getFractionalUnitCoord('X');
    case Token.unity:
      return atom.getFractionalUnitCoord('Y');
    case Token.unitz:
      return atom.getFractionalUnitCoord('Z');
    case Token.vanderwaals:
      return atom.getVanderwaalsRadiusFloat(viewer, EnumVdw.AUTO);
    case Token.vibx:
      return atom.getVibrationCoord('X');
    case Token.viby:
      return atom.getVibrationCoord('Y');
    case Token.vibz:
      return atom.getVibrationCoord('Z');
    case Token.vectorscale:
      Vector3f v = atom.getVibrationVector();
      return (v == null ? 0 : v.length() * viewer.getVectorScale());

    }
    return atomPropertyInt(atom, tokWhat);
  }

  private float getMass() {
    float mass = getIsotopeNumber();
    return (mass > 0 ? mass : Elements.getAtomicMass(getElementNumber()));
  }

  public static String atomPropertyString(Viewer viewer, Atom atom, int tokWhat) {
    char ch;
    switch (tokWhat) {
    case Token.altloc:
      ch = atom.getAlternateLocationID();
      return (ch == '\0' ? "" : "" + ch);
    case Token.atomname:
      return atom.getAtomName();
    case Token.atomtype:
      return atom.getAtomType();
    case Token.chain:
      ch = atom.getChainID();
      return (ch == '\0' ? "" : "" + ch);
    case Token.sequence:
      return atom.getGroup1('?');
    case Token.group1:
      return atom.getGroup1('\0');
    case Token.group:
      return atom.getGroup3(false);
    case Token.element:
      return atom.getElementSymbol(true);
    case Token.identify:
      //modified -hcf
      //return atom.getIdentity(true);
      return atom.getUnitIdentity(true);
    case Token.insertion:
      ch = atom.getInsertionCode();
      return (ch == '\0' ? "" : "" + ch);
    case Token.label:
    case Token.format:
      String s = atom.group.chain.model.modelSet.getAtomLabel(atom.getIndex());
      if (s == null)
        s = "";
      return s;
    case Token.structure:
      return atom.getProteinStructureType().getBioStructureTypeName(false);
    case Token.substructure:
      return atom.getProteinStructureSubType().getBioStructureTypeName(false);
    case Token.strucid:
      return atom.getStructureId();
    case Token.shape:
      return viewer.getHybridizationAndAxes(atom.index, null, null, "d");
    case Token.symbol:
      return atom.getElementSymbol(false);
    case Token.symmetry:
      return atom.getSymmetryOperatorList();
    }
    return ""; 
  }

  public static Tuple3f atomPropertyTuple(Atom atom, int tok) {
    switch (tok) {
    case Token.fracxyz:
      return atom.getFractionalCoord(!atom.group.chain.model.isJmolDataFrame);
    case Token.fuxyz:
      return atom.getFractionalCoord(false);
    case Token.unitxyz:
      return (atom.group.chain.model.isJmolDataFrame ? atom.getFractionalCoord(false) 
          : atom.getFractionalUnitCoord(false));
    case Token.screenxyz:
      return new Point3f(atom.screenX, atom.group.chain.model.modelSet.viewer.getScreenHeight() - atom.screenY, atom.screenZ);
    case Token.vibxyz:
      Vector3f v = atom.getVibrationVector();
      if (v == null)
        v = new Vector3f();
      return v;
    case Token.xyz:
      return atom;
    case Token.color:
      return ColorUtil.colorPointFromInt2(
          atom.group.chain.model.modelSet.viewer.getColorArgbOrGray(atom.getColix())
          );
    }
    return null;
  }

  boolean isWithinStructure(EnumStructure type) {
    return group.isWithinStructure(type);
  }
  
  @Override
public int getOffsetResidueAtom(String name, int offset) {
    return group.chain.model.modelSet.getGroupAtom(this, offset, name);
  }
  
  @Override
public boolean isCrossLinked(JmolNode node) {
    return group.isCrossLinked(((Atom) node).getGroup());
  }

  @Override
public boolean getCrossLinkLeadAtomIndexes(List<Integer> vReturn) {
    return group.getCrossLinkLeadAtomIndexes(vReturn);
  }
  
  @Override
  public String toString() {
    return getInfo();
  }

  public boolean isWithinFourBonds(Atom atomOther) {
    if (modelIndex != atomOther.modelIndex)
      return  false;
    if (isCovalentlyBonded(atomOther))
      return true; 
    Bond[] bondsOther = atomOther.bonds;
    for (int i = 0; i < bondsOther.length; i++) {
      Atom atom2 = bondsOther[i].getOtherAtom(atomOther);
      if (isCovalentlyBonded(atom2))
        return true;
      for (int j = 0; j < bonds.length; j++)
        if (bonds[j].getOtherAtom(this).isCovalentlyBonded(atom2))
          return true;
    }
    return false;
  }

  @Override
public BitSet findAtomsLike(String atomExpression) {
    // for SMARTS searching
    return group.chain.model.modelSet.viewer.getAtomBitSet(atomExpression);
  }

}
