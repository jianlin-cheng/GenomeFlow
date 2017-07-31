/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2007-03-30 11:40:16 -0500 (Fri, 30 Mar 2007) $
 * $Revision: 7273 $
 *
 * Copyright (C) 2007 Miguel, Bob, Jmol Development
 *
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
 *  Lesser General License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jmol.jvxl.readers;

import java.util.BitSet;
import java.util.Date;

import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;

import org.jmol.atomdata.AtomData;
import org.jmol.atomdata.RadiusData;
import org.jmol.atomdata.RadiusData.EnumType;
import org.jmol.constant.EnumVdw;
import org.jmol.jvxl.data.JvxlCoder;
import org.jmol.jvxl.data.MeshData;
import org.jmol.util.ArrayUtil;
import org.jmol.util.BitSetUtil;
import org.jmol.util.ContactPair;
import org.jmol.util.Logger;
import org.jmol.util.TextFormat;

abstract class AtomDataReader extends VolumeDataReader {

  protected float maxDistance;
  protected ContactPair contactPair;

  AtomDataReader(SurfaceGenerator sg) {
    super(sg);
    precalculateVoxelData = true;
    atomDataServer = sg.getAtomDataServer();
  }

  protected String fileName;
  protected String fileDotModel;
  protected int modelIndex;

  protected AtomData atomData = new AtomData();

  protected Point3f[] atomXyz;
  protected float[] atomRadius;
  protected float[] atomProp;
  protected int[] atomNo;
  protected int[] atomIndex;
  protected int[] myIndex;
  protected int atomCount;
  protected int myAtomCount;
  protected int nearbyAtomCount;
  protected int firstNearbyAtom;
  protected BitSet bsMySelected = new BitSet();
  protected BitSet  bsMyIgnored = new BitSet();
  protected BitSet bsNearby;

  protected boolean doAddHydrogens;
  protected boolean havePlane;
  protected boolean doUseIterator;
  private float minPtsPerAng;

  @Override
  protected void setup(boolean isMapData) {
    //CANNOT BE IN HERE IF atomDataServer is not valid
    contactPair = params.contactPair;
    doAddHydrogens = (sg.getAtomDataServer() != null && params.addHydrogens); //Jvxl cannot do this on its own
    modelIndex = params.modelIndex;
    if (params.bsIgnore != null)
      bsMyIgnored = params.bsIgnore;
    if (params.volumeData != null) {
      setVolumeData(params.volumeData);
      setBoundingBox(volumeData.volumetricOrigin, 0);
      ptXyzTemp.set(volumeData.volumetricOrigin);
      for (int i = 0; i < 3; i++)
        ptXyzTemp.scaleAdd(volumeData.voxelCounts[i] - 1, 
            volumeData.volumetricVectors[i], ptXyzTemp);
      setBoundingBox(ptXyzTemp, 0);
    }
    havePlane = (params.thePlane != null);
    if (havePlane)
      volumeData.setPlaneParameters(params.thePlane);
  }

  protected void markPlaneVoxels(Point3f p, float r) {
    for (int i = 0, pt = thisX * yzCount, pt1 = pt + yzCount; pt < pt1; pt++, i++) {
      volumeData.getPoint(pt, ptXyzTemp);
      thisPlane[i] = ptXyzTemp.distance(p) - r;
    }    
  }

  protected void setVolumeForPlane() {
    if (useOriginStepsPoints) {
      xyzMin = new Point3f(params.origin);
      xyzMax = new Point3f(params.origin);
      xyzMax.x += (params.points.x - 1) * params.steps.x;
      xyzMax.y += (params.points.y - 1) * params.steps.y;
      xyzMax.z += (params.points.z - 1) * params.steps.z;
    } else {
      getAtoms(params.bsSelected, false, true, false, false, false, false, params.mep_marginAngstroms);
      if (xyzMin == null) {
        xyzMin = new Point3f(-10,-10,-10);
        xyzMax = new Point3f(10, 10, 10);
      }
    }
    setRanges(params.plane_ptsPerAngstrom, params.plane_gridMax, 0); 
  }

  /**
   * 
   * @param bsSelected
   *        TODO
   * @param doAddHydrogens
   *        TODO
   * @param getRadii
   *        TODO
   * @param getMolecules
   *        TODO
   * @param getAllModels
   *        TODO
   * @param addNearbyAtoms
   * @param getAtomMinMax
   *        TODO
   * @param marginAtoms
   */
  protected void getAtoms(BitSet bsSelected, boolean doAddHydrogens,
                          boolean getRadii, boolean getMolecules,
                          boolean getAllModels, boolean addNearbyAtoms,
                          boolean getAtomMinMax, float marginAtoms) {

    if (addNearbyAtoms)
      getRadii = true;
    // set atomRadiusData to 100% if it has not been set already
    // if it hasn't already been set.
    if (getRadii) {
      if (params.atomRadiusData == null)
        params.atomRadiusData = new RadiusData(1, EnumType.FACTOR,
            EnumVdw.AUTO);
      atomData.radiusData = params.atomRadiusData;
      atomData.radiusData.valueExtended = params.solventExtendedAtomRadius;
      if (doAddHydrogens)
        atomData.radiusData.vdwType = EnumVdw.NOJMOL;
    }
    atomData.modelIndex = modelIndex; // -1 here means fill ALL atoms; any other
    // means "this model only"
    atomData.bsSelected = bsSelected;
    atomData.bsIgnored = bsMyIgnored;
    sg.fillAtomData(atomData, AtomData.MODE_FILL_COORDS
        | (getAllModels ? AtomData.MODE_FILL_MULTIMODEL : 0)
        | (getMolecules ? AtomData.MODE_FILL_MOLECULES : 0)
        | (getRadii ? AtomData.MODE_FILL_RADII : 0));
    if (doUseIterator)
      atomData.bsSelected = null;
    atomCount = atomData.atomCount;
    modelIndex = atomData.firstModelIndex;
    int nSelected = 0;
    boolean needRadius = false;
    for (int i = 0; i < atomCount; i++) {
      if ((bsSelected == null || bsSelected.get(i)) && (!bsMyIgnored.get(i))) {
        if (havePlane
            && Math.abs(volumeData.distancePointToPlane(atomData.atomXyz[i])) > 2 * (atomData.atomRadius[i] = getWorkingRadius(
                i, marginAtoms)))
          continue;
        bsMySelected.set(i);
        nSelected++;
        needRadius = !havePlane;
      }
      if (getRadii && (addNearbyAtoms || needRadius))
        atomData.atomRadius[i] = getWorkingRadius(i, marginAtoms);
    }

    float rH = (getRadii && doAddHydrogens ? getWorkingRadius(-1, marginAtoms)
        : 0);
    myAtomCount = BitSetUtil.cardinalityOf(bsMySelected);
    BitSet atomSet = BitSetUtil.copy(bsMySelected);
    int nH = 0;
    atomProp = null;
    float[] props = params.theProperty;
    if (myAtomCount > 0) {
      Point3f[] hAtoms = null;
      if (doAddHydrogens) {
        atomData.bsSelected = atomSet;
        atomDataServer.fillAtomData(atomData,
            AtomData.MODE_GET_ATTACHED_HYDROGENS);
        hAtoms = new Point3f[nH = atomData.hydrogenAtomCount];
        for (int i = 0; i < atomData.hAtoms.length; i++)
          if (atomData.hAtoms[i] != null)
            for (int j = atomData.hAtoms[i].length; --j >= 0;)
              hAtoms[--nH] = atomData.hAtoms[i][j];
        nH = hAtoms.length;
        Logger.info(nH + " attached hydrogens added");
      }
      int n = nH + myAtomCount;
      if (getRadii)
        atomRadius = new float[n];
      atomXyz = new Point3f[n];
      if (params.theProperty != null)
        atomProp = new float[n];
      atomNo = new int[n];
      atomIndex = new int[n];
      myIndex = new int[atomCount];

      for (int i = 0; i < nH; i++) {
        if (getRadii)
          atomRadius[i] = rH;
        atomXyz[i] = hAtoms[i];
        atomNo[i] = -1;
        if (atomProp != null)
          atomProp[i] = Float.NaN;
        // if (params.logMessages)
        // Logger.debug("draw {" + hAtoms[i].x + " " + hAtoms[i].y + " "
        // + hAtoms[i].z + "};");
      }
      myAtomCount = nH;
      for (int i = atomSet.nextSetBit(0); i >= 0; i = atomSet.nextSetBit(i + 1)) {
        if (atomProp != null)
          atomProp[myAtomCount] = (props != null && i < props.length ? props[i]
              : Float.NaN);
        atomXyz[myAtomCount] = atomData.atomXyz[i];
        atomNo[myAtomCount] = atomData.atomicNumber[i];
        atomIndex[myAtomCount] = i;
        myIndex[i] = myAtomCount;
        if (getRadii)
          atomRadius[myAtomCount] = atomData.atomRadius[i];
        myAtomCount++;
      }
    }
    firstNearbyAtom = myAtomCount;
    Logger.info(myAtomCount + " atoms will be used in the surface calculation");

    if (myAtomCount == 0) {
      setBoundingBox(new Point3f(10, 10, 10), 0);
      setBoundingBox(new Point3f(-10, -10, -10), 0);
    }
    for (int i = 0; i < myAtomCount; i++)
      setBoundingBox(atomXyz[i], getRadii ? atomRadius[i] + 0.5f : 0);
    if (!Float.isNaN(params.scale)) {
      Vector3f v = new Vector3f(xyzMax);
      v.sub(xyzMin);
      v.scale(0.5f);
      xyzMin.add(v);
      v.scale(params.scale);
      xyzMax.set(xyzMin);
      xyzMax.add(v);
      xyzMin.sub(v);
    }

    // fragment idea

    if (!addNearbyAtoms || myAtomCount == 0)
      return;
    Point3f pt = new Point3f();

    bsNearby = new BitSet();
    for (int i = 0; i < atomCount; i++) {
      if (atomSet.get(i) || bsMyIgnored.get(i))
        continue;
      float rA = atomData.atomRadius[i];
      if (params.thePlane != null
          && Math.abs(volumeData.distancePointToPlane(atomData.atomXyz[i])) > 2 * rA)
        continue;
      if (params.theProperty != null)
        rA += maxDistance;
      pt = atomData.atomXyz[i];
      if (pt.x + rA > xyzMin.x && pt.x - rA < xyzMax.x && pt.y + rA > xyzMin.y
          && pt.y - rA < xyzMax.y && pt.z + rA > xyzMin.z
          && pt.z - rA < xyzMax.z) {
        bsNearby.set(i);
        nearbyAtomCount++;
      }
    }
    int nAtoms = myAtomCount;
    if (nearbyAtomCount != 0) {
      nAtoms += nearbyAtomCount;
      atomRadius = ArrayUtil.setLength(atomRadius, nAtoms);
      atomXyz = (Point3f[]) ArrayUtil.setLength(atomXyz, nAtoms);
      if (atomIndex != null)
        atomIndex = ArrayUtil.setLength(atomIndex, nAtoms);

      if (props != null)
        atomProp = ArrayUtil.setLength(atomProp, nAtoms);
      for (int i = bsNearby.nextSetBit(0); i >= 0; i = bsNearby
          .nextSetBit(i + 1)) {
        if (props != null)
          atomProp[myAtomCount] = props[i];
        myIndex[i] = myAtomCount;
        atomIndex[myAtomCount] = i;
        atomXyz[myAtomCount] = atomData.atomXyz[i];
        atomRadius[myAtomCount++] = atomData.atomRadius[i];
      }
    }
  }

  private float getWorkingRadius(int i, float marginAtoms) {
    float r = (i < 0 ? atomData.hAtomRadius : atomData.atomRadius[i]);
    return (Float.isNaN(marginAtoms) ? Math.max(r, 0.1f) : r + marginAtoms);
  }

  protected void setHeader(String calcType, String line2) {
    jvxlFileHeaderBuffer = new StringBuffer();
    if (atomData.programInfo != null)
      jvxlFileHeaderBuffer.append("#created by ").append(atomData.programInfo)
          .append(" on ").append(new Date()).append("\n");
    jvxlFileHeaderBuffer.append(calcType).append("\n").append(line2).append(
        "\n");
  }

  protected void setRanges(float ptsPerAngstrom, int maxGrid, float minPtsPerAng) {
    if (xyzMin == null)
      return;
    this.ptsPerAngstrom = ptsPerAngstrom;
    this.maxGrid = maxGrid;
    this.minPtsPerAng = minPtsPerAng;
    setVolumeData();
    JvxlCoder.jvxlCreateHeader(volumeData, jvxlFileHeaderBuffer);
  }

  @Override
  protected void setVolumeData() {
    if (!setVolumeDataParams()) {
      setVoxelRange(0, xyzMin.x, xyzMax.x, ptsPerAngstrom, maxGrid, minPtsPerAng);
      setVoxelRange(1, xyzMin.y, xyzMax.y, ptsPerAngstrom, maxGrid, minPtsPerAng);
      setVoxelRange(2, xyzMin.z, xyzMax.z, ptsPerAngstrom, maxGrid, minPtsPerAng);
    }
  }

  protected boolean fixTitleLine(int iLine) {
    if (params.title == null)
      return false;
    String line = params.title[iLine];
    if (line.indexOf("%F") > 0)
      line = params.title[iLine] = TextFormat.formatString(line, "F",
          atomData.fileName);
    if (line.indexOf("%M") > 0)
      params.title[iLine] = TextFormat.formatString(line, "M",
          atomData.modelName);
    return true;
  }
  
  protected void setVertexSource() {
    if (meshDataServer != null)
      meshDataServer.fillMeshData(meshData, MeshData.MODE_GET_VERTICES, null);
    if (params.vertexSource != null) {
      params.vertexSource = ArrayUtil.setLength(params.vertexSource,
          meshData.vertexCount);
      for (int i = 0; i < meshData.vertexCount; i++)
        params.vertexSource[i] = Math.abs(params.vertexSource[i]) - 1;
    }
  }

  protected void resetPlane(float value) {
    for (int i = 0; i < yzCount; i++)
      thisPlane[i] = value;
  }

  protected void resetVoxelData(float value) {
    for (int x = 0; x < nPointsX; ++x)
      for (int y = 0; y < nPointsY; ++y)
        for (int z = 0; z < nPointsZ; ++z)
          voxelData[x][y][z] = value;
  }

  protected float[] thisPlane;
  protected BitSet thisAtomSet;
  protected int thisX;
  
  private float getVoxel(int i, int j, int k, int ipt) {
    return (isProgressive ? thisPlane[ipt % yzCount] : voxelData[i][j][k]);
  }

  protected void unsetVoxelData() {
    if (isProgressive)
      for (int i = 0; i < yzCount; i++) {
        if (thisPlane[i] == Float.MAX_VALUE)
          thisPlane[i] = Float.NaN;
      }
    else
      for (int x = 0; x < nPointsX; ++x)
        for (int y = 0; y < nPointsY; ++y)
          for (int z = 0; z < nPointsZ; ++z)
            if (voxelData[x][y][z] == Float.MAX_VALUE)
              voxelData[x][y][z] = Float.NaN;
  }

  protected float margin;

  protected void setGridLimitsForAtom(Point3f ptA, float rA, Point3i pt0,
                                      Point3i pt1) {
    rA += margin; // to span corner-to-corner possibility
    volumeData.xyzToVoxelPt(ptA.x, ptA.y, ptA.z, pt0);
    int x = (int) (rA / volumeData.volumetricVectorLengths[0]);
    int y = (int) (rA / volumeData.volumetricVectorLengths[1]);
    int z = (int) (rA / volumeData.volumetricVectorLengths[2]);
    pt1.set(pt0.x + x, pt0.y + y, pt0.z + z);
    pt0.set(pt0.x - x, pt0.y - y, pt0.z - z);
    pt0.x = Math.max(pt0.x - 1, 0);
    pt0.y = Math.max(pt0.y - 1, 0);
    pt0.z = Math.max(pt0.z - 1, 0);
    pt1.x = Math.min(pt1.x + 1, nPointsX);
    pt1.y = Math.min(pt1.y + 1, nPointsY);
    pt1.z = Math.min(pt1.z + 1, nPointsZ);
  }

  // for isoSolventReader and isoIntersectReader

  protected BitSet bsSurfaceVoxels;
  protected BitSet validSpheres, noFaceSpheres;
  protected int[] voxelSource;

  protected void getAtomMinMax(BitSet bs, BitSet[] bsAtomMinMax) {
    for (int i = 0; i < nPointsX; i++)
      bsAtomMinMax[i] = new BitSet();
    for (int iAtom = myAtomCount; --iAtom >= 0;) {
      if (bs != null && !bs.get(iAtom))
        continue;
      setGridLimitsForAtom(atomXyz[iAtom], atomRadius[iAtom], pt0, pt1);
      for (int i = pt0.x; i < pt1.x; i++)
        bsAtomMinMax[i].set(iAtom);
      //System.out.println("for atom " + iAtom + " " + ptA + " " + min + " " + max);
    }
  }


  protected final Point3f ptY0 = new Point3f();
  protected final Point3f ptZ0 = new Point3f();
  protected final Point3i pt0 = new Point3i();
  protected final Point3i pt1 = new Point3i();
  protected final Point3f ptXyzTemp = new Point3f();

  protected void markSphereVoxels(float r0, float distance) {
    boolean isWithin = (distance != Float.MAX_VALUE && point != null);
    for (int iAtom = thisAtomSet.nextSetBit(0); iAtom >= 0; iAtom = thisAtomSet.nextSetBit(iAtom + 1)) {
      if (!havePlane && validSpheres != null && !validSpheres.get(iAtom))
        continue;
      boolean isSurface = (noFaceSpheres != null && noFaceSpheres.get(iAtom));
      boolean isNearby = (iAtom >= firstNearbyAtom);
      Point3f ptA = atomXyz[iAtom];
      float rA = atomRadius[iAtom];
      if (isWithin && ptA.distance(point) > distance + rA + 0.5)
        continue;
      float rA0 = rA + r0;
      setGridLimitsForAtom(ptA, rA0, pt0, pt1);
      //pt1.y = nPointsY;
      //pt1.z = nPointsZ;
      if (isProgressive) {
        pt0.x = thisX;
        pt1.x = thisX + 1;
      }
      volumeData.voxelPtToXYZ(pt0.x, pt0.y, pt0.z, ptXyzTemp);
      for (int i = pt0.x; i < pt1.x; i++, ptXyzTemp.scaleAdd(1,
          volumetricVectors[0], ptY0)) {
        ptY0.set(ptXyzTemp);
        for (int j = pt0.y; j < pt1.y; j++, ptXyzTemp.scaleAdd(1,
            volumetricVectors[1], ptZ0)) {
          ptZ0.set(ptXyzTemp);
          for (int k = pt0.z; k < pt1.z; k++, ptXyzTemp
              .add(volumetricVectors[2])) {
            float value = ptXyzTemp.distance(ptA) - rA;            
            int ipt = volumeData.getPointIndex(i, j, k);
            if ((r0 == 0 || value <= rA0) && value < getVoxel(i, j, k, ipt)) {
              if (isNearby || isWithin && ptXyzTemp.distance(point) > distance)
                value = Float.NaN;
              setVoxel(i, j, k, ipt, value);
              if (!Float.isNaN(value)) {
                if (voxelSource != null)
                  voxelSource[ipt] = iAtom + 1;
                if (value < 0 && isSurface)
                  bsSurfaceVoxels.set(ipt);
              }
            }
          }
        }
      }
    }
  }
  
  protected void setVoxel(int i, int j, int k, int ipt, float value) {
    if (isProgressive)
      thisPlane[ipt % yzCount] = value;
    else
      voxelData[i][j][k] = value;
  }

}
