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

import javax.vecmath.Point3f;
import javax.vecmath.Point4f;

import org.jmol.jvxl.data.JvxlCoder;

class IsoFxyReader extends AtomDataReader {
  
  IsoFxyReader(SurfaceGenerator sg) {
    super(sg);
    isXLowToHigh = true;
    precalculateVoxelData = false;
    atomDataServer = sg.getAtomDataServer();
    params.fullyLit = true;
    isPlanarMapping = (params.thePlane != null || params.state == Parameters.STATE_DATA_COLORED);
    if (params.func != null)
      volumeData.sr = this;
  }

  private float[][] data;
  private boolean isPlanarMapping;
  private Object[] func;
  
  @Override
  protected void setup(boolean isMapData) {
    if (params.functionInfo.size() > 5)
      data = (float[][]) params.functionInfo.get(5);
    setup("functionXY");
  }

  protected void setup(String type) {
    func = (Object[]) params.func;
    String functionName = (String) params.functionInfo.get(0);
    jvxlFileHeaderBuffer = new StringBuffer();
    jvxlFileHeaderBuffer.append(type).append("\n").append(functionName).append("\n");
    if (params.thePlane != null || data == null && !useOriginStepsPoints)
      setVolumeForPlane();
    else if (data == null)
      setVolumeDataParams();
    else
      setVolumeData();
    JvxlCoder.jvxlCreateHeaderWithoutTitleOrAtoms(volumeData, jvxlFileHeaderBuffer);
  }

  @Override
  protected void setVolumeData() {
    if (data == null) {
      super.setVolumeData(); 
      return;
    }
    volumetricOrigin.set((Point3f) params.functionInfo.get(1));
    for (int i = 0; i < 3; i++) {
      Point4f info = (Point4f) params.functionInfo.get(i + 2);
      voxelCounts[i] = Math.abs((int) info.x);
      volumetricVectors[i].set(info.y, info.z, info.w);      
    }
    if (isAnisotropic)
      setVolumetricAnisotropy();
  }

  @Override
  protected void readSurfaceData(boolean isMapData) throws Exception {
    if (volumeData.sr != null)
      return;
    super.readSurfaceData(isMapData);
  }

  @Override
  public float[] getPlane(int x) {
    float[] plane = super.getPlane(x);
    getPlane(x, plane);
    return plane;
  }

  private void getPlane(int x, float[] plane) {
      for (int y = 0, ptyz = 0; y < nPointsY; ++y)
        for (int z = 0; z < nPointsZ; ++z)
          plane[ptyz++] = getValue(x, y, z);
  }

  protected float getValue(int x, int y, int z) {
    float value;
    if (data == null) {
      value = evaluateValue(x, y, z);
    } else {
      volumeData.voxelPtToXYZ(x, y, z, ptTemp);
      value = data[x][y]; 
    }
    return (isPlanarMapping ? value : value - ptTemp.z);
  }
    
  private final float[] values = new float[3];

  
  @Override
  public float getValueAtPoint(Point3f pt) {
    if (params.func == null)
      return 0;
    values[0] = pt.x;
    values[1] = pt.y;
    values[2] = pt.z;
    return atomDataServer.evalFunctionFloat(func[0], func[1], values);
  }

  protected float evaluateValue(int x, int y, int z) {
    volumeData.voxelPtToXYZ(x, y, z, ptTemp);
    return getValueAtPoint(ptTemp);
  }
}
