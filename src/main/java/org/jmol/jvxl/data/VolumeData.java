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
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

/*
 
 * The JVXL file format
 * --------------------
 * 
 * as of 3/29/07 this code is COMPLETELY untested. It was hacked out of the
 * Jmol code, so there is probably more here than is needed.
 * 
 * 
 * 
 * see http://www.stolaf.edu/academics/chemapps/jmol/docs/misc/JVXL-format.pdf
 *
 * The JVXL (Jmol VoXeL) format is a file format specifically designed
 * to encode an isosurface or planar slice through a set of 3D scalar values
 * in lieu of a that set. A JVXL file can contain coordinates, and in fact
 * it must contain at least one coordinate, but additional coordinates are
 * optional. The file can contain any finite number of encoded surfaces. 
 * However, the compression of 300-500:1 is based on the reduction of the 
 * data to a SINGLE surface. 
 * 
 * 
 * The original Marching Cubes code was written by Miguel Howard in 2005.
 * The classes Parser, ArrayUtil, and TextFormat are condensed versions
 * of the classes found in org.jmol.util.
 * 
 * All code relating to JVXL format is copyrighted 2006/2007 and invented by 
 * Robert M. Hanson, 
 * Professor of Chemistry, 
 * St. Olaf College, 
 * 1520 St. Olaf Ave.
 * Northfield, MN. 55057.
 * 
 * Implementations of the JVXL format should reference 
 * "Robert M. Hanson, St. Olaf College" and the opensource Jmol project.
 * 
 * 
 * implementing marching squares; see 
 * http://www.secam.ex.ac.uk/teaching/ug/studyres/COM3404/COM3404-2006-Lecture15.pdf
 * 
 * lines through coordinates are identical to CUBE files
 * after that, we have a line that starts with a negative number to indicate this
 * is a JVXL file:
 * 
 * line1:  (int)-nSurfaces  (int)edgeFractionBase (int)edgeFractionRange  
 * (nSurface lines): (float)cutoff (int)nBytesData (int)nBytesFractions
 * 
 * definition1
 * edgedata1
 * fractions1
 * colordata1
 * ....
 * definition2
 * edgedata2
 * fractions2
 * colordata2
 * ....
 * 
 * definitions: a line with detail about what sort of compression follows
 * 
 * edgedata: a list of the count of vertices ouside and inside the cutoff, whatever
 * that may be, ordered by nested for loops for(x){for(y){for(z)}}}.
 * 
 * nOutside nInside nOutside nInside...
 * 
 * fractions: an ascii list of characters represting the fraction of distance each
 * encountered surface point is along each voxel cube edge found to straddle the 
 * surface. The order written is dictated by the reader algorithm and is not trivial
 * to describe. Each ascii character is constructed by taking a base character and 
 * adding onto it the fraction times a range. This gives a character that can be
 * quoted EXCEPT for backslash, which MAY be substituted for by '!'. Jmol uses the 
 * range # - | (35 - 124), reserving ! and } for special meanings.
 * 
 * colordata: same deal here, but with possibility of "double precision" using two bytes.
 * 
 * 
 * 
 * THIS READER
 * -----------
 * 
 * This is a first attempt at a generic JVXL file reader and writer class.
 * It is an extraction of Jmol org.jmol.viewer.Isosurface.Java and related pieces.
 * 
 * The goal of the reader is to be able to read CUBE-like data and 
 * convert that data to JVXL file data.
 * 
 * 
 */

package org.jmol.jvxl.data;

import java.util.Hashtable;
import java.util.Map;

import javax.vecmath.Matrix3f;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Point4f;
import javax.vecmath.Vector3f;

import org.jmol.api.VolumeDataInterface;
import org.jmol.jvxl.readers.SurfaceReader;
import org.jmol.util.Escape;
import org.jmol.util.Logger;
import org.jmol.util.XmlUtil;

public class VolumeData implements VolumeDataInterface {

  public SurfaceReader sr; // used for delivering point-specific values, particularly when mapping
  public boolean doIterate = true;
  
  public final Point3f volumetricOrigin = new Point3f();
  public final float[] origin = new float[3];
  public final Vector3f[] volumetricVectors = new Vector3f[3];
  public final int[] voxelCounts = new int[3];
  public int nPoints;
  private float[][][] voxelData; 
  @Override
public float[][][] getVoxelData() {
    return voxelData;
  }
  
  @Override
public void setVoxelData(float[][][] voxelData) {
    this.voxelData = voxelData;
  }

  private Map<Integer, Float> voxelMap; // alternative to voxelData for sparse (plane interesected) data
  public final float[] volumetricVectorLengths = new float[3];
  private float maxVectorLength;
  private float minToPlaneDistance;
  private int yzCount;

  public final Vector3f[] unitVolumetricVectors = new Vector3f[3];
  private final Matrix3f volumetricMatrix = new Matrix3f();
  private final Matrix3f inverseMatrix = new Matrix3f();
  private Point4f thePlane;

  public boolean hasPlane() {
    return (thePlane != null);
  }
  
  private float thePlaneNormalMag;
  private final Point3f ptXyzTemp = new Point3f();
  public String xmlData;
  
  public VolumeData() {
    volumetricVectors[0] = new Vector3f();
    volumetricVectors[1] = new Vector3f();
    volumetricVectors[2] = new Vector3f();
    unitVolumetricVectors[0] = new Vector3f();
    unitVolumetricVectors[1] = new Vector3f();
    unitVolumetricVectors[2] = new Vector3f();
  }

  public Point4f mappingPlane;
  float mappingPlaneNormalMag;
  
  public void setMappingPlane(Point4f plane) {
    //if(true)return;
    mappingPlane = plane;
    if (plane == null)
      return;
    mappingPlaneNormalMag = (float) Math.sqrt(plane.x * plane.x + plane.y * plane.y + plane.z * plane.z);
  }

  public float distanceToMappingPlane(Point3f pt) {
    return (mappingPlane.x * pt.x + mappingPlane.y * pt.y + mappingPlane.z * pt.z + mappingPlane.w)
    / mappingPlaneNormalMag;
  }


  @Override
public void setVolumetricOrigin(float x, float y, float z) {
    volumetricOrigin.set(x, y, z);
  }

  @Override
public float[] getOriginFloat() {
    return origin;
  }

  public float minGrid;
  public float maxGrid;
  public float voxelVolume;
  private Vector3f[] spanningVectors;
  
  public Vector3f[] getSpanningVectors() {
    return spanningVectors;
  }
  
  public int getYzCount() {
    
    minGrid = volumetricVectors[0].length();
    minGrid = Math.min(minGrid, volumetricVectors[1].length());
    minGrid = Math.min(minGrid, volumetricVectors[2].length());

    maxGrid = volumetricVectors[0].length();
    maxGrid = Math.max(maxGrid, volumetricVectors[1].length());
    maxGrid = Math.max(maxGrid, volumetricVectors[2].length());

    nPoints = voxelCounts[0] * voxelCounts[1] * voxelCounts[2];
    
    return yzCount = voxelCounts[1] * voxelCounts[2];
  }
  
  @Override
public float[] getVolumetricVectorLengths() {
    return volumetricVectorLengths;
  }
  
  @Override
public void setVolumetricVector(int i, float x, float y, float z) {
    volumetricVectors[i].x = x;
    volumetricVectors[i].y = y;
    volumetricVectors[i].z = z;
    setUnitVectors();
  }

  @Override
public int[] getVoxelCounts() {
    return voxelCounts;
  }
  
  @Override
public int setVoxelCounts(int nPointsX, int nPointsY, int nPointsZ) {
    voxelCounts[0] = nPointsX;
    voxelCounts[1] = nPointsY;
    voxelCounts[2] = nPointsZ;
    return nPointsX * nPointsY * nPointsZ;
  }

  public float getVoxelData(int pt) {
    int ix = pt / yzCount;
    pt -= ix * yzCount;
    int iy = pt / voxelCounts[2];
    int iz = pt - iy * voxelCounts[2];
    return voxelData[ix][iy][iz]; 
  }
  
  public int getPointIndex(int x, int y, int z) {
    return x * yzCount + y * voxelCounts[2] + z;  
  }
  
  public void getPoint(int ipt, Point3f pt) {
    int ix = ipt / yzCount;
    ipt -= ix * yzCount;
    int iy = ipt / voxelCounts[2];
    int iz = ipt - iy * voxelCounts[2];
    voxelPtToXYZ(ix, iy, iz, pt);
  }
  
  public void setVoxelData(int pt, float value) {
    int ix = pt / yzCount;
    pt -= ix * yzCount;
    int iy = pt / voxelCounts[2];
    int iz = pt - iy * voxelCounts[2];
    voxelData[ix][iy][iz] = value; 
  }
  
  public void setVoxelMap() {
    voxelMap = new Hashtable<Integer, Float>();
    getYzCount();
  }
  
  private boolean setMatrix() {
    for (int i = 0; i < 3; i++)
      volumetricMatrix.setColumn(i, volumetricVectors[i]);
    try {
      inverseMatrix.invert(volumetricMatrix);
    } catch (Exception e) {
      Logger.error("VolumeData error setting matrix -- bad unit vectors? ");
      return false;
    }
    return true;
  }

  @Override
public void transform(Vector3f v1, Vector3f v2) {
    volumetricMatrix.transform(v1, v2);
  }

  @Override
public void setPlaneParameters(Point4f plane) {
    thePlane = plane;
    thePlaneNormalMag = (float) Math.sqrt(plane.x * plane.x + plane.y * plane.y + plane.z * plane.z);
  }

  @Override
public float calcVoxelPlaneDistance(int x, int y, int z) {
    voxelPtToXYZ(x, y, z, ptXyzTemp);
    return distancePointToPlane(ptXyzTemp);
  }

  public float getToPlaneParameter() {
    return (float) (Math.sqrt(thePlane.x * thePlane.x
        + thePlane.y * thePlane.y + thePlane.z * thePlane.z) * minToPlaneDistance);
  }
  
  public boolean isNearPlane(int x, int y, int z, float toPlaneParameter) {
    voxelPtToXYZ(x, y, z, ptXyzTemp);
    return ((thePlane.x * ptXyzTemp.x + thePlane.y * ptXyzTemp.y
        + thePlane.z * ptXyzTemp.z + thePlane.w) < toPlaneParameter);
  }

  @Override
public float distancePointToPlane(Point3f pt) {
    return (thePlane.x * pt.x + thePlane.y * pt.y + thePlane.z * pt.z + thePlane.w)
        / thePlaneNormalMag;
  }

  @Override
public void voxelPtToXYZ(int x, int y, int z, Point3f pt) {
    pt.scaleAdd(x, volumetricVectors[0], volumetricOrigin);
    pt.scaleAdd(y, volumetricVectors[1], pt);
    pt.scaleAdd(z, volumetricVectors[2], pt);
  }

  public boolean setUnitVectors() {
    maxVectorLength = 0;
    voxelVolume = 1;
    for (int i = 0; i < 3; i++) {
      float d = volumetricVectorLengths[i] = volumetricVectors[i].length();
      if (d == 0)
        return false;
      if (d > maxVectorLength)
        maxVectorLength = d;
      voxelVolume *= d;
      unitVolumetricVectors[i].normalize(volumetricVectors[i]);
    }
    minToPlaneDistance = maxVectorLength * 2;
    origin[0] = volumetricOrigin.x;
    origin[1] = volumetricOrigin.y;
    origin[2] = volumetricOrigin.z;
    spanningVectors = new Vector3f[4];
    spanningVectors[0] = new Vector3f(volumetricOrigin);
    for (int i = 0; i < 3; i++) {
      Vector3f v = spanningVectors[i + 1] = new Vector3f();
      v.scaleAdd(voxelCounts[i] - 1, volumetricVectors[i], v);
    }
    return setMatrix();
  }

  @Override
public void xyzToVoxelPt(float x, float y, float z, Point3i pt3i) {
    ptXyzTemp.set(x, y, z);
    ptXyzTemp.sub(volumetricOrigin);
    inverseMatrix.transform(ptXyzTemp);
    pt3i.set((int) ptXyzTemp.x, (int) ptXyzTemp.y, (int) ptXyzTemp.z);
  }

  public boolean isPeriodic;
  
  @Override
public float lookupInterpolatedVoxelValue(Point3f point) {
    if (mappingPlane != null)
      return distanceToMappingPlane(point);
    if (sr != null) {
      float v = sr.getValueAtPoint(point);
      return (isSquared ? v * v : v);
    }
    ptXyzTemp.sub(point, volumetricOrigin);
    inverseMatrix.transform(ptXyzTemp);
    int iMax;
    int xLower = indexLower(ptXyzTemp.x, iMax = voxelCounts[0] - 1);
    int xUpper = indexUpper(ptXyzTemp.x, xLower, iMax);
    int yLower = indexLower(ptXyzTemp.y, iMax = voxelCounts[1] - 1);
    int yUpper = indexUpper(ptXyzTemp.y, yLower, iMax);
    int zLower = indexLower(ptXyzTemp.z, iMax = voxelCounts[2] - 1);
    int zUpper = indexUpper(ptXyzTemp.z, zLower, iMax);
    float v1 = getFractional2DValue(mantissa(ptXyzTemp.x - xLower), mantissa(ptXyzTemp.y - yLower),
        getVoxelValue(xLower, yLower, zLower), getVoxelValue(xUpper, yLower, zLower),
        getVoxelValue(xLower, yUpper, zLower), getVoxelValue(xUpper, yUpper, zLower));
    float v2 = getFractional2DValue(mantissa(ptXyzTemp.x - xLower), mantissa(ptXyzTemp.y - yLower),
        getVoxelValue(xLower, yLower, zUpper), getVoxelValue(xUpper, yLower, zUpper),
        getVoxelValue(xLower, yUpper, zUpper), getVoxelValue(xUpper, yUpper, zUpper));
    return v1 + mantissa(ptXyzTemp.z - zLower) * (v2 - v1);
  }

  private float mantissa(float f) {
    return (isPeriodic ? f - (float) Math.floor(f) : f);
  }

  public float getVoxelValue(int x, int y, int z) {
    if (voxelMap == null)
      return voxelData[x][y][z];
    Float f = voxelMap.get(new Integer(getPointIndex(x, y, z)));
    return (f == null ? Float.NaN : f.floatValue());
  }

  public static float getFractional2DValue(float fx, float fy, float x11,
                                           float x12, float x21, float x22) {
    float v1 = x11 + fx * (x12 - x11);
    float v2 = x21 + fx * (x22 - x21);
    return v1 + fy * (v2 - v1);
  }

  /**
   * periodic grids should have val[0] == val[xMax]
   * 
   * voxelCount: 1....2....3....4....5
   * xMax/index: 0....1....2....3....4....
   * nonper.  ^ ---> [0,0]             ^ --> [4, 4]
   * periodic ^ ---> [3,4]             ^ --> [0, 1]
   * 
   * @param x
   * @param xMax
   * @return  lower index in range
   */
  private int indexLower(float x, int xMax) {
    if (isPeriodic && xMax > 0) {
      while (x < 0)
        x += xMax;
      while (x >= xMax)
        x -= xMax;
      return (int) x;
    } 
    if (x < 0)
      return 0;
    int floor = (int) x;
    return (floor > xMax ? xMax : floor);
  }

  private int indexUpper(float x, int xLower, int xMax) {
    return (!isPeriodic && x < 0 || xLower == xMax ? xLower : xLower + 1);
  }

  void offsetCenter(Point3f center) {
    Point3f pt = new Point3f();
    pt.scaleAdd((voxelCounts[0] - 1) / 2f, volumetricVectors[0], pt);
    pt.scaleAdd((voxelCounts[1] - 1) / 2f, volumetricVectors[1], pt);
    pt.scaleAdd((voxelCounts[2] - 1) / 2f, volumetricVectors[2], pt);
    volumetricOrigin.sub(center, pt);
  }

  @Override
public void setDataDistanceToPlane(Point4f plane) {
    //TODO REMOVE THIS METHOD.
    setPlaneParameters(plane);
    int nx = voxelCounts[0];
    int ny = voxelCounts[1];
    int nz = voxelCounts[2];
    voxelData = new float[nx][ny][nz];
    for (int x = 0; x < nx; x++)
      for (int y = 0; y < ny; y++)
        for (int z = 0; z < nz; z++)
          voxelData[x][y][z] = calcVoxelPlaneDistance(x, y, z);
  }

  private boolean isSquared;
  @Override
public void filterData(boolean isSquared, float invertCutoff) {
    boolean doInvert = (!Float.isNaN(invertCutoff));
    if (sr != null) {
      this.isSquared = isSquared;
      return;
    }
    int nx = voxelCounts[0];
    int ny = voxelCounts[1];
    int nz = voxelCounts[2];
    if (isSquared) 
      for (int x = 0; x < nx; x++)
        for (int y = 0; y < ny; y++)
          for (int z = 0; z < nz; z++)
            voxelData[x][y][z] *= voxelData[x][y][z];
    if (doInvert) 
      for (int x = 0; x < nx; x++)
        for (int y = 0; y < ny; y++)
          for (int z = 0; z < nz; z++)
            voxelData[x][y][z] = invertCutoff - voxelData[x][y][z];
  }

  @Override
public void capData(Point4f plane, float cutoff) {
    if (voxelData == null)
      return;
    int nx = voxelCounts[0];
    int ny = voxelCounts[1];
    int nz = voxelCounts[2];
    Vector3f normal = new Vector3f(plane.x, plane.y, plane.z);
    normal.normalize();
    float f = 1f;
    for (int x = 0; x < nx; x++)
      for (int y = 0; y < ny; y++)
        for (int z = 0; z < nz; z++) {
          float value = voxelData[x][y][z] - cutoff;
          voxelPtToXYZ(x, y, z, ptXyzTemp);
          float d = (ptXyzTemp.x * normal.x + ptXyzTemp.y * normal.y + ptXyzTemp.z * normal.z + plane.w - cutoff) / f;
          if (d >= 0 || d > value)
            voxelData[x][y][z] = d;
        }
  }

  public String setVolumetricXml() {
    StringBuffer sb = new StringBuffer();
    if (voxelCounts[0] == 0) {
      XmlUtil.appendTag(sb, "jvxlVolumeData", null);
    } else {   
      XmlUtil.openTag(sb, "jvxlVolumeData", new String[] {
          "origin", Escape.escapePt(volumetricOrigin) });
      for (int i = 0; i < 3; i++) 
        XmlUtil.appendTag(sb, "jvxlVolumeVector", new String[] {
            "type", "" + i,
            "count", "" + voxelCounts[i],
            "vector", Escape.escapePt(volumetricVectors[i]) } );
      XmlUtil.closeTag(sb, "jvxlVolumeData");
    }
    return xmlData = sb.toString();
  }

  /**
   * for sparse data mapping, as for a plane
   * 
   * @param x
   * @param y
   * @param z
   * @param v
   */
  public void setVoxelMapValue(int x, int y, int z, float v) {
    if (voxelMap == null)
      return;
    voxelMap.put(new Integer(getPointIndex(x, y, z)), Float.valueOf(v));    
  }

  private final Vector3f edgeVector = new Vector3f();
  
  private Point3f ptTemp = new Point3f();
  
  public float calculateFractionalPoint(float cutoff, Point3f pointA,
                                        Point3f pointB, float valueA,
                                        float valueB, Point3f pt) {
    float d = (valueB - valueA);
    float fraction = (cutoff - valueA) / d;
    edgeVector.sub(pointB, pointA);
    pt.scaleAdd(fraction, edgeVector, pointA);
    if (sr == null || !doIterate || valueB == valueA 
        || fraction < 0.01f || fraction > 0.99f 
        || (edgeVector.length()) < 0.01f)
      return cutoff;
    // Do a nonlinear interpolation here and get a better value
    // such is the case for atomic orbitals.
    // In some cases we will find that we cannot get there, either
    // because the actual point is not between valueA and valueB
    // or because the projected point is not between pointA and pointB
    // In that case we invalidate the point.
    int n = 0;
    ptTemp.set(pt);
    float v = lookupInterpolatedVoxelValue(ptTemp);
    float v0 = Float.NaN;
    
    while (++n < 10) {
      float fnew = (v - valueA) / d;
      if (fnew < 0 || fnew > 1)
        break;
      float diff = (cutoff - v) / d / 2;
      fraction += diff;
      if (fraction < 0 || fraction > 1)
        break;
      pt.set(ptTemp);
      v0 = v;
      if (Math.abs(diff) < 0.005f)
        break;
      ptTemp.scaleAdd(diff, edgeVector, pt);
      v = lookupInterpolatedVoxelValue(ptTemp);
    }
    return v0;
  }

}