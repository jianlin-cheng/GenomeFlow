/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2006-07-14 16:23:28 -0500 (Fri, 14 Jul 2006) $
 * $Revision: 5305 $
 *
 * Copyright (C) 2005 Miguel, Jmol Development
 *
 * Contact: jmol-developers@lists.sf.net,jmol-developers@lists.sourceforge.net
 * Contact: hansonr@stolaf.edu
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

package org.jmol.shapesurface;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.jmol.api.AtomIndexIterator;
import org.jmol.atomdata.AtomData;
import org.jmol.atomdata.RadiusData;
import org.jmol.atomdata.RadiusData.EnumType;
import org.jmol.constant.EnumHBondType;
import org.jmol.constant.EnumVdw;
import org.jmol.jvxl.data.MeshData;
import org.jmol.jvxl.data.VolumeData;
import org.jmol.jvxl.readers.Parameters;
import org.jmol.modelset.Atom;
import org.jmol.script.Token;
import org.jmol.util.BitSetUtil;
import org.jmol.util.ColorEncoder;
import org.jmol.util.ColorUtil;
import org.jmol.util.ContactPair;
import org.jmol.util.Escape;
import org.jmol.util.Logger;
import org.jmol.util.Measure;
import org.jmol.util.MeshSurface;

public class Contact extends Isosurface {

  
  @Override
  public void initShape() {
    super.initShape();
    myType = "contact";
  }

  @Override
  public void setProperty(String propertyName, Object value, BitSet bs) {

    if ("set" == propertyName) {
      setContacts((Object[]) value, !viewer.getTestFlag(4));
      return;
    }
    if ("init" == propertyName) {
      translucentLevel = 0;
    }

    super.setProperty(propertyName, value, bs);
  }
    
  protected Atom[] atoms;
  private int atomCount;
  private float minData, maxData;
  //private final static String hbondH = "_H & connected(_O|_N and his and not *.N |_S)";
  //private final static float HBOND_CUTOFF = -0.8f;
  private final static RadiusData rdVDW =  new RadiusData(1, EnumType.FACTOR, EnumVdw.AUTO);
  
  private void setContacts(Object[] value, boolean doEditCpList) {
    Logger.startTimer();
    int contactType = ((Integer) value[0]).intValue();
    int displayType = ((Integer) value[1]).intValue();
    boolean colorDensity = ((Boolean) value[2]).booleanValue();
    boolean colorByType = ((Boolean) value[3]).booleanValue();
    BitSet bsA = (BitSet) value[4];
    BitSet bsB = (BitSet) value[5];
    RadiusData rd = (RadiusData) value[6];
    float saProbeRadius = ((Float) value[7]).floatValue();
    float[] parameters = (float[]) value[8];
    String command = (String) value[9];
    if (Float.isNaN(saProbeRadius))
      saProbeRadius = 0;
    if (rd == null)
      rd = new RadiusData(saProbeRadius, EnumType.OFFSET, EnumVdw.AUTO);
    if (colorDensity) {
      switch (displayType) {
      case Token.full:
      case Token.trim:
      case Token.plane:
        displayType = Token.trim;
        break;
      case Token.connect:
      case Token.nci:
      case Token.surface:
      case Token.sasurface:
        // ok as is
        break;
      case Token.cap:
        colorDensity = false;
        break;
      }
    }

    BitSet bs;
    atomCount = viewer.getAtomCount();
    atoms = viewer.getModelSet().atoms;

    int intramolecularMode = (int) (parameters == null || parameters.length < 2 ? 0
        : parameters[1]);
    float ptSize = (colorDensity && parameters != null && parameters[0] < 0 ? Math
        .abs(parameters[0])
        : 0.15f);
    if (Logger.debugging) {
      Logger.info("Contact intramolecularMode " + intramolecularMode);
      Logger.info("Contacts for " + bsA.cardinality() + ": "
          + Escape.escape(bsA));
      Logger.info("Contacts to " + bsB.cardinality() + ": "
          + Escape.escape(bsB));
    }
    super.setProperty("newObject", null, null);
    thisMesh.setMerged(true);
    thisMesh.nSets = 0;
    thisMesh.info = null;
    Parameters params = sg.getParams();

    String func = null;
    switch (displayType) {
    case Token.full:
      func = "(a>b?a:b)";
      break;
    case Token.plane:
    case Token.cap:
      func = "a-b";
      break;
    case Token.connect:
      func = "a+b";
      break;
    }
    VolumeData volumeData;
    switch (displayType) {
    case Token.nci:
      colorByType = false;
      bs = BitSetUtil.copy(bsA);
      bs.or(bsB); // for now -- TODO -- need to distinguish ligand
      if (parameters[0] < 0)
        parameters[0] = 0; // reset to default for density
      params.colorDensity = colorDensity;
      params.bsSelected = bs;
      params.bsSolvent = bsB;
      sg.setParameter("parameters", parameters);
      super.setProperty("nci", Boolean.TRUE, null);
      break;
    case Token.sasurface:
    case Token.surface:
      colorByType = false;
      thisMesh.nSets = 1;
      newSurface(Token.surface, null, bsA, bsB, rd, null, null, colorDensity,
          null, saProbeRadius);
      break;
    case Token.cap:
      colorByType = false;
      thisMesh.nSets = 1;
      newSurface(Token.slab, null, bsA, bsB, rd, null, null, false, null, 0);
      volumeData = sg.getVolumeData();
      sg.initState();
      newSurface(Token.plane, null, bsA, bsB, rd, parameters, func,
          colorDensity, volumeData, 0);
      mergeMesh(null);
      break;
    case Token.full:
    case Token.trim:
      colorByType = false;
      newSurface(Token.trim, null, bsA, bsB, rd, null, null, colorDensity, null, 0);
      if (displayType == Token.full) {
        sg.initState();
        newSurface(Token.trim, null, bsB, bsA, rd, parameters, func,
            colorDensity, null, 0);
        mergeMesh(null);
      } else {
        MeshData meshData = new MeshData();
        fillMeshData(meshData, MeshData.MODE_GET_VERTICES, null);
        meshData.getSurfaceSet();
        fillMeshData(meshData, MeshData.MODE_PUT_SETS, null);
      }

      break;
    case Token.connect:
    case Token.plane:
      /*      if (rd == null)
              rd = new RadiusData(0.25f, EnumType.OFFSET,
                  EnumVdw.AUTO);
      */
      float volume = 0;
      List<ContactPair> pairs = getPairs(bsA, bsB, rd, intramolecularMode, doEditCpList);
      thisMesh.info = pairs;
      volume += combineSurfaces(pairs, contactType, displayType, parameters,
          func, colorDensity, colorByType);
      thisMesh.calculatedVolume = Float.valueOf(volume);
      mergeMesh(null);
      break;
    }
    thisMesh.setMerged(false);
    thisMesh.jvxlData.vertexDataOnly = true;
    thisMesh.reinitializeLightingAndColor();
    if (contactType != Token.nci) {
      thisMesh.bsVdw = new BitSet();
      thisMesh.bsVdw.or(bsA);
      thisMesh.bsVdw.or(bsB);
    }
    super.setProperty("finalize", command, null);
    if (colorDensity) {
      super.setProperty("pointSize", Float.valueOf(ptSize), null);
    } else {
      super.setProperty("token", Integer.valueOf(Token.fullylit), null);
    }
    if (thisMesh.slabOptions != null) {
      thisMesh.slabOptions = null;
      thisMesh.polygonCount0 = -1; // disable slabbing.
    }
    discardTempData(true);
    String defaultColor = null;
    switch (contactType) {
    case Token.hbond:
      defaultColor = "lightgreen";
      break;
    case Token.clash:
      defaultColor = "yellow";
      break;
    case Token.surface:
      defaultColor = "skyblue";
      break;
    }
    ColorEncoder ce = null;
    if (colorByType) {
      ce = viewer.getColorEncoder("rwb");
      ce.setRange(-0.5f, 0.5f, false);
    } else if (defaultColor != null) {
      super.setProperty("color", Integer.valueOf(ColorUtil
          .getArgbFromString(defaultColor)), null);
    } else if (displayType == Token.nci) {
      ce = viewer.getColorEncoder("bgr");
      ce.setRange(-0.03f, 0.03f, false);
    } else {
      ce = viewer.getColorEncoder("rgb");
      if (colorDensity)
        ce.setRange(-0.3f, 0.3f, false);
      else
        ce.setRange(-0.5f, 1f, false);
    }
    if (ce != null)
      thisMesh.remapColors(ce, translucentLevel);
    Logger.checkTimer("contact");
  }

  /**
   * @param pairs 
   * @param contactType 
   * @param displayType 
   * @param parameters 
   * @param func 
   * @param isColorDensity 
   * @param colorByType  
   * @return               volume
   */
  private float combineSurfaces(List<ContactPair> pairs, int contactType,
                                int displayType, float[] parameters,
                                Object func, boolean isColorDensity,
                                boolean colorByType) {
    VolumeData volumeData = new VolumeData();
    int logLevel = Logger.getLogLevel();
    Logger.setLogLevel(0);
    float resolution = sg.getParams().resolution;
    int nContacts = pairs.size();
    double volume = 0;
    if (displayType == Token.full && resolution == Float.MAX_VALUE)
      resolution = (nContacts > 1000 ? 3 : 10);

    for (int i = nContacts; --i >= 0;) {
      ContactPair cp = pairs.get(i);
      float oldScore = cp.score;
      boolean isVdwClash = (displayType == Token.plane 
          && (contactType == Token.vanderwaals || contactType == Token.nada) 
          && cp.setForVdwClash(true));
      if (isVdwClash)
        cp.score = 0; // for now
      if (contactType != Token.nada && cp.contactType != contactType)
        continue;
      int nV = thisMesh.vertexCount;
      thisMesh.nSets++;
      if (contactType != Token.nada || cp.contactType != Token.vanderwaals)
        volume += cp.volume;
      setVolumeData(displayType, volumeData, cp, resolution, nContacts);
      switch (displayType) {
      case Token.full:
        newSurface(displayType, cp, null, null, null, null, func,
            isColorDensity, volumeData, 0);
        cp.switchAtoms();
        newSurface(displayType, cp, null, null, null, null, null,
            isColorDensity, volumeData, 0);
        break;
      case Token.trim:
      case Token.plane:
      case Token.connect:
        newSurface(displayType, cp, null, null, null, parameters, func,
            isColorDensity, volumeData, 0);
        if (isVdwClash && cp.setForVdwClash(false)) {
          if (colorByType)
            nV = setColorByScore(cp.score, nV);
          cp.score = oldScore;
          volume += cp.volume;
          newSurface(displayType, cp, null, null, null, parameters, func,
              isColorDensity, volumeData, 0);          
        }
        break;
      }
      if (i > 0 && (i % 1000) == 0 && logLevel == 4) {
        Logger.setLogLevel(4);
        Logger.info("contact..." + i);
        Logger.setLogLevel(0);
      }
      if (colorByType)
        setColorByScore((cp.contactType == Token.hbond ? 4 : cp.score), nV);
    }
    Logger.setLogLevel(logLevel);
    return (float) volume;
  }
  
  private int setColorByScore(float score, int nV) {
    for (int iv = thisMesh.vertexCount; --iv >= nV;)
      thisMesh.vertexValues[iv] = score;
    return thisMesh.vertexCount;
  }

  /**
   * 
   * @param bsA
   * @param bsB
   * @param rd
   * @param intramolecularMode
   * @param doEditCpList 
   * @return a list of pairs of atoms to process
   */
  private List<ContactPair> getPairs(BitSet bsA, BitSet bsB, RadiusData rd,
                                     int intramolecularMode, boolean doEditCpList) {
    List<ContactPair> list = new ArrayList<ContactPair>();
    AtomData ad = new AtomData();
    ad.radiusData = rd;
    BitSet bs = BitSetUtil.copy(bsA);
    bs.or(bsB);
    if (bs.isEmpty())
      return list;
    ad.bsSelected = bs;
    boolean isMultiModel = (atoms[bs.nextSetBit(0)].modelIndex != atoms[bs
        .length() - 1].modelIndex);
    boolean isSelf = bsA.equals(bsB);
    viewer.fillAtomData(ad, AtomData.MODE_FILL_RADII
        | (isMultiModel ? AtomData.MODE_FILL_MULTIMODEL : 0)
        | AtomData.MODE_FILL_MOLECULES);
    float maxRadius = 0;
    for (int ib = bsB.nextSetBit(0); ib >= 0; ib = bsB.nextSetBit(ib + 1))
      if (ad.atomRadius[ib] > maxRadius)
        maxRadius = ad.atomRadius[ib];
    AtomIndexIterator iter = viewer.getSelectedAtomIterator(bsB, isSelf, false,
        isMultiModel);
    for (int ia = bsA.nextSetBit(0); ia >= 0; ia = bsA.nextSetBit(ia + 1)) {
      Atom atomA = atoms[ia];
      float vdwA = atomA.getVanderwaalsRadiusFloat(viewer, EnumVdw.AUTO);
      if (isMultiModel)
        viewer.setIteratorForPoint(iter, -1, ad.atomXyz[ia], ad.atomRadius[ia]
            + maxRadius);
      else
        viewer.setIteratorForAtom(iter, ia, ad.atomRadius[ia] + maxRadius);
      while (iter.hasNext()) {
        int ib = iter.next();
        if (isMultiModel && !bsB.get(ib))
          continue;
        Atom atomB = atoms[ib];
        boolean isSameMolecule = (ad.atomMolecule[ia] == ad.atomMolecule[ib]);
        if (ia == ib || isSameMolecule && atomA.isWithinFourBonds(atomB))
          continue;
        switch (intramolecularMode) {
        case 0:
          break;
        case 1:
        case 2:
          if (isSameMolecule != (intramolecularMode == 1))
            continue;
        }
        float vdwB = atomB.getVanderwaalsRadiusFloat(viewer, EnumVdw.AUTO);
        float ra = ad.atomRadius[ia];
        float rb = ad.atomRadius[ib];
        float d = atomA.distance(atomB);
        if (d > ra + rb)
          continue;
        ContactPair cp = new ContactPair(atoms, ia, ib, ra, rb, vdwA, vdwB);

        if (cp.score < 0)
          getVdwClashRadius(cp, ra - vdwA, vdwA, vdwB, d);

        // check for O--H...N or O...H--N and not considering
        // hydrogens and still have a filter
        // a bit of asymmetry here: set A may or may not have H atoms added.
        // This is particularly important for amines
        EnumHBondType typeA = EnumHBondType.getType(atomA);
        EnumHBondType typeB = (typeA == EnumHBondType.NOT ? EnumHBondType.NOT
            : EnumHBondType.getType(atomB));
        boolean isHBond = EnumHBondType.isPossibleHBond(typeA, typeB);
        //float hbondCutoff = -1.0f;//HBOND_CUTOFF;
        float hbondCutoff = (atomA.getElementNumber() == 1 || atomB.getElementNumber() == 1 ? -1.2f : -1.0f);
        
        if (isHBond && cp.score < hbondCutoff)
          isHBond = false;
        if (isHBond && cp.score < 0)
          cp.contactType = Token.hbond;
        list.add(cp);
      }
    }
    iter.release();
    iter = null;
    if (!doEditCpList)
      return list;
    int n = list.size() - 1;
    BitSet bsBad = new BitSet();
    for (int i = 0; i < n; i++) {
      ContactPair cp1 = list.get(i);
      for (int j = i + 1; j <= n; j++) {
        ContactPair cp2 = list.get(j);
        for (int m = 0; m < 2; m++) {
          for (int p = 0; p < 2; p++) {
            switch (checkCp(cp1, cp2, m, p)) {
            case 1:
              bsBad.set(i);
              break;
            case 2:
              bsBad.set(j);
              break;
            default:
            }
          }
        }
      }
    }
    for (int i = bsBad.length(); --i >= 0;)
      if (bsBad.get(i))
        list.remove(i);
    if (Logger.debugging)
      for (int i = 0; i < list.size(); i++)
        Logger.info(list.get(i).toString());
    Logger.info("Contact pairs: " + list.size());
    return list;
  }

  /**
   * 
   * @param cp1
   * @param cp2
   * @param i1
   * @param i2
   * @return    0 (no clash); 1 (remove #1); 2 (remove #2)
   */
  private static int checkCp(ContactPair cp1, ContactPair cp2, int i1, int i2) {
    if (cp1.myAtoms[i1] != cp2.myAtoms[i2])
      return 0;
    boolean clash1 = (cp1.pt.distance(cp2.myAtoms[1 - i2]) < cp2.radii[1 - i2]);
    boolean clash2 = (cp2.pt.distance(cp1.myAtoms[1 - i1]) < cp1.radii[1 - i1]);
    // remove higher score (less overlap)
    return (!clash1 && !clash2 ? 0 : cp1.score > cp2.score ? 1 : 2);
  }

  private void newSurface(int displayType, ContactPair cp, BitSet bs1, BitSet bs2,
                          RadiusData rd, float[] parameters, Object func,
                          boolean isColorDensity, VolumeData volumeData, float sasurfaceRadius) {
    Parameters params = sg.getParams();
    params.isSilent = true;
    if (cp == null) {
      bs2.andNot(bs1);
      if (bs1.isEmpty() || bs2.isEmpty())
        return;
    } else {
      params.contactPair = cp;
    }
    int iSlab0 = 0, iSlab1 = 0;
    sg.initState();
    switch (displayType) {
    case Token.sasurface:
    case Token.surface:
    case Token.slab:
    case Token.trim:
    case Token.full:
      RadiusData rdA, rdB;
      if (displayType == Token.surface) {
        rdA = rdVDW;
        rdB = new RadiusData((rd.factorType == EnumType.OFFSET ? rd.value * 2 : (rd.value - 1) * 2 + 1), 
            rd.factorType, rd.vdwType);
      } else {
        rdA = rdB = rd;
      }
      params.colorDensity = isColorDensity;
      if (isColorDensity) {
        super.setProperty("cutoffRange", new float[] { -100f, 0f }, null);
      }
      if (cp == null) {
        params.atomRadiusData = rdA;
        params.bsIgnore = BitSetUtil.copyInvert(bs1, atomCount);
        params.bsSelected = bs1;
        params.bsSolvent = null;
      }
      params.volumeData = volumeData;
      super.setProperty("sasurface", Float.valueOf(sasurfaceRadius), null);
      super.setProperty("map", Boolean.TRUE, null);
      if (cp == null) {
        params.atomRadiusData = rdB;
        params.bsIgnore = BitSetUtil.copyInvert(bs2, atomCount);
        params.bsSelected = bs2;
      }
      params.volumeData = volumeData;
      super.setProperty("sasurface", Float.valueOf(sasurfaceRadius), null);
      switch (displayType) {
      case Token.full:
      case Token.trim:
        iSlab0 = -100;
        break;
      case Token.sasurface:
      case Token.surface:
        if (isColorDensity)
          iSlab0 = -100;
        break;
      case Token.slab:
        iSlab1 = -100;
      }
      break;
    case Token.plane:
    case Token.connect:
      if (displayType == Token.connect)
        sg.setParameter("parameters", parameters);
      if (cp == null) {
        params.atomRadiusData = rd;
        params.bsIgnore = BitSetUtil.copyInvert(bs2, atomCount);
        params.bsIgnore.andNot(bs1);
      }
      params.func = func;
      params.intersection = new BitSet[] { bs1, bs2 };
      params.volumeData = volumeData;
      params.colorDensity = isColorDensity;
      if (isColorDensity)
        super.setProperty("cutoffRange", new float[] { -5f, 0f }, null);
      super.setProperty("sasurface", Float.valueOf(0), null);
      // mapping
      super.setProperty("map", Boolean.TRUE, null);
      params.volumeData = volumeData;
      super.setProperty("sasurface", Float.valueOf(0), null);
      if (displayType != Token.connect)
        iSlab0 = -100;
    }
    if (iSlab0 != iSlab1)
      thisMesh.slabPolygons(MeshSurface.getSlabWithinRange(iSlab0, iSlab1),
          false);
    if (displayType != Token.surface)
      thisMesh.setMerged(true);
  }

  private Vector3f vZ = new Vector3f();
  private Vector3f vY = new Vector3f();
  private Vector3f vX = new Vector3f();
  private Point3f pt1 = new Point3f();
  private Point3f pt2 = new Point3f();
  
  private void setVolumeData(int type, VolumeData volumeData, ContactPair cp,
                             float resolution, int nPairs) {
    pt1.set(cp.myAtoms[0]);
    pt2.set(cp.myAtoms[1]);
    vX.sub(pt2, pt1);
    float dAB = vX.length();
    float dYZ = (cp.radii[0] * cp.radii[0] + dAB * dAB - cp.radii[1] * cp.radii[1])/(2 * dAB * cp.radii[0]);
    dYZ = 2.1f * (float) (cp.radii[0] * Math.sin(Math.acos(dYZ)));
    Measure.getNormalToLine(pt1, pt2, vZ);
    vZ.scale(dYZ);
    vY.cross(vZ, vX);
    vY.normalize();
    vY.scale(dYZ);
    if (type != Token.connect) {
      vX.normalize();
      pt1.scaleAdd((dAB - cp.radii[1]) * 0.95f, vX, pt1);
      pt2.scaleAdd((cp.radii[0] - dAB) * 0.95f, vX, pt2);
      vX.sub(pt2, pt1);
    }
    if (resolution == Float.MAX_VALUE)
      resolution = (nPairs > 100 ? 3 : 10);
    
    // now set voxel counts and vectors, and grid origin

    int nX = Math.max(5, (int) (pt1.distance(pt2) * resolution + 1));
    if ((nX % 2) == 0)
      nX++;
    int nYZ = Math.max(7, (int) (dYZ * resolution + 1));
    if ((nYZ % 2) == 0)
      nYZ++;
    volumeData.setVoxelCounts(nX, nYZ, nYZ);
    pt1.scaleAdd(-0.5f, vY, pt1);
    pt1.scaleAdd(-0.5f, vZ, pt1);
    volumeData.setVolumetricOrigin(pt1.x, pt1.y, pt1.z);
    /*
    System.out.println("draw pt1 @{point"+pt1+"} color red");
    System.out.println("draw vx vector @{point"+pt1+"} @{point"+vX+"} color red");
    System.out.println("draw vy vector @{point"+pt1+"} @{point"+vY+"} color green");
    System.out.println("draw vz vector @{point"+pt1+"} @{point"+vZ+"} color blue");
    */

    vX.scale(1f/(nX-1));
    vY.scale(1f/(nYZ-1));
    vZ.scale(1f/(nYZ-1));
    volumeData.setVolumetricVector(0, vX.x, vX.y, vX.z);
    volumeData.setVolumetricVector(1, vY.x, vY.y, vY.z);
    volumeData.setVolumetricVector(2, vZ.x, vZ.y, vZ.z);

  }

  private void mergeMesh(MeshData md) {
    thisMesh.merge(md);
    if (minData == Float.MAX_VALUE) {
      // just assign it
    } else if (jvxlData.mappedDataMin == Float.MAX_VALUE) {
      jvxlData.mappedDataMin = minData;
      jvxlData.mappedDataMax = maxData;
    } else {
      jvxlData.mappedDataMin = Math.min(minData, jvxlData.mappedDataMin);
      jvxlData.mappedDataMax = Math.max(maxData, jvxlData.mappedDataMax);
    }
    minData = jvxlData.mappedDataMin;
    maxData = jvxlData.mappedDataMax;
    jvxlData.valueMappedToBlue = minData;
    jvxlData.valueMappedToRed = maxData;

  }

  @SuppressWarnings("unchecked")
  @Override
  protected void addMeshInfo(IsosurfaceMesh mesh, Map<String, Object> info) {
    if (mesh.info == null)
      return;
    List<Map<String, Object>> pairInfo = new ArrayList<Map<String, Object>>();
    info.put("pairInfo", pairInfo);
    List<ContactPair> list = (List<ContactPair>) mesh.info;
    for (int i = 0; i < list.size(); i++) {
      Map<String, Object> cpInfo = new Hashtable<String, Object>();
      pairInfo.add(cpInfo);
      ContactPair cp = list.get(i);
      cpInfo.put("type", Token.nameOf(cp.contactType));
      cpInfo.put("volume", Double.valueOf(cp.volume));
      cpInfo.put("vdwVolume", Double.valueOf(cp.vdwVolume));
      if (!Float.isNaN(cp.xVdwClash)) {
        cpInfo.put("xVdwClash", Double.valueOf(cp.xVdwClash));
      }
      cpInfo.put("score", Double.valueOf(cp.score));
      cpInfo.put("atoms", cp.myAtoms);
      cpInfo.put("radii", cp.radii);
      cpInfo.put("vdws", cp.vdws);
    }
  }

  /**
   * 
   * well, heh, heh... This calculates the VDW extension x at a given distance
   * for a clashing pair that will produce a volume that is equivalent to the
   * volume for the vdw contact at the point of touching (d0 = vdwA + vdwB) and
   * the transition to clash. This will provide the surface that will surround
   * the clash until the clash size is larger than it.
   * @param cp 
   * 
   * @param x0
   * @param vdwA
   * @param vdwB
   * @param d
   */
  private static void getVdwClashRadius(ContactPair cp, double x0, double vdwA, double vdwB,
                                         double d) {

    /// Volume = pi/12 * (r + R - d)^2 * (d + 2(r + R) - 3(r-R)^2/d)
    /// for +vdw +x: pi/12 * (va + vb - d + 2x)^2 * (d + 2(va + vb) + 4x - 3(va-vb)^2/d)
    
    double sum = vdwA + vdwB;
    double dif2 = vdwA - vdwB;
    dif2 *= dif2;
    double v0_nopi = x0 * x0 * (sum + 4.0/3 * x0 - dif2 / sum);
    cp.vdwVolume = cp.volume - v0_nopi * Math.PI;
    //System.out.println("v0 = " + Math.PI * v0_nopi + " v0_nopi =" + v0_nopi);
    
    /// (a + x)^2(b + 2x) = c; where x here is probe DIAMETER

    double a = (sum - d);
    double b = d + 2 * sum - 3 * dif2 / d;
    double c = v0_nopi * 12;

    
    /* from Sage:
     * 
    
a = var('a')
b = var('b')
c = var('c')
x = var('x')
eqn = (a + x)^2 * (b + 2 * x) == c
solve(eqn, x)

[

x == -1/72*(-I*sqrt(3) + 1)*(4*a^2 - 4*a*b + b^2)/(1/27*a^3 -
1/18*a^2*b + 1/36*a*b^2 - 1/216*b^3 + 1/36*sqrt((8*a^3 - 12*a^2*b +
6*a*b^2 - b^3 + 27*c)*c)*sqrt(3) + 1/4*c)^(1/3) - 1/2*(I*sqrt(3) +
1)*(1/27*a^3 - 1/18*a^2*b + 1/36*a*b^2 - 1/216*b^3 + 1/36*sqrt((8*a^3 -
12*a^2*b + 6*a*b^2 - b^3 + 27*c)*c)*sqrt(3) + 1/4*c)^(1/3) - 2/3*a -
1/6*b, 

x == -1/72*(I*sqrt(3) + 1)*(4*a^2 - 4*a*b + b^2)/(1/27*a^3 -
1/18*a^2*b + 1/36*a*b^2 - 1/216*b^3 + 1/36*sqrt((8*a^3 - 12*a^2*b +
6*a*b^2 - b^3 + 27*c)*c)*sqrt(3) + 1/4*c)^(1/3) - 1/2*(-I*sqrt(3) +
1)*(1/27*a^3 - 1/18*a^2*b + 1/36*a*b^2 - 1/216*b^3 + 1/36*sqrt((8*a^3 -
12*a^2*b + 6*a*b^2 - b^3 + 27*c)*c)*sqrt(3) + 1/4*c)^(1/3) - 2/3*a -
1/6*b, 

x == -2/3*a - 1/6*b + 1/36*(4*a^2 - 4*a*b + b^2)/(1/27*a^3 -
1/18*a^2*b + 1/36*a*b^2 - 1/216*b^3 + 1/36*sqrt((8*a^3 - 12*a^2*b +
6*a*b^2 - b^3 + 27*c)*c)*sqrt(3) + 1/4*c)^(1/3) + (1/27*a^3 - 1/18*a^2*b
+ 1/36*a*b^2 - 1/216*b^3 + 1/36*sqrt((8*a^3 - 12*a^2*b + 6*a*b^2 - b^3 +
27*c)*c)*sqrt(3) + 1/4*c)^(1/3)

]

*/
    
/* so...

x1 == f - g*(1/2-I*sqrt(3)/2)/h^(1/3) - (1/2+I*sqrt(3)/2)*h^(1/3)
x2 == f - g*(1/2+I*sqrt(3)/2)/h^(1/3) - (1/2-I*sqrt(3)/2)*h^(1/3)
x3 == f + g/h^(1/3) + h^(1/3)

where

f = -2/3*a - 1/6*b
g = (4*a^2 - 4*a*b + b^2)/36 
h = a^3/27 - a^2*b/18 + a*b^2/36 - b^3/216 + c/4 
     + sqrt(c/432*(8*a^3 - 12*a^2*b + 6*a*b^2 - b^3 + 27*c))

The problem is, that sqrt is imaginary, so the cube root is as well. 

v = a^3/27 - a^2*b/18 + a*b^2/36 - b^3/216 + c/4
u = -c/432*(8*a^3 - 12*a^2*b + 6*a*b^2 - b^3 + 27*c)

*/

    double a2 = a * a;
    double a3 = a * a2;
    double b2 = b * b;
    double b3 = b * b2;

    double f = -a * 2/3 - b/6;
    double g = (4*a2 - 4*a*b + b2)/36;
    double v = a3/27 - a2*b/18 + a*b2/36 - b3/216 + c/4;
    double u = -c/432*(8*a3 - 12*a2*b + 6*a*b2 - b3 + 27*c);
    
    
/*
Then 

h = v + sqrt(u)*I

and we can express h^1/3 as 

vvu (cos theta + i sin theta)

where

vvu = (v^2 + u)^(1/6)
theta = atan2(sqrt(u),v)/3

Now, we know this is a real solution, so we take the real part of that.
The third root seems to be our root (thankfully!)

x3 == f + g/h^(1/3) + h^(1/3)
    = f + (2*g/vvu + vvu) costheta

     */
    

    double theta = Math.atan2(Math.sqrt(u), v);
    
    double vvu = Math.pow(v*v + u, 1.0/6.0);
    double costheta = Math.cos(theta/3);
    
    // x == f + g/h^(1/3) + h^(1/3) = f + g/vvu + vvu)*costheta

    //System.out.println ("a = " + a + ";b = " + b + ";c = " + c + ";f = " + f + ";g = " + g + "");

    double x;
    
    x = f + (g/vvu + vvu) * costheta;
    //System.out.println(d + "\t" + x + "\t" + ((a + x)*(a + x) * (b + 2 * x)) + " = " + c);
    if (x > 0) {
      cp.xVdwClash = ((float) (x / 2));
    }
  }

}
