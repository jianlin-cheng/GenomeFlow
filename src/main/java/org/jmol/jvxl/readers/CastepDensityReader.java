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

import java.io.BufferedReader;

import javax.vecmath.Vector3f;

class CastepDensityReader extends VolumeFileReader {

  private int nFilePoints;
  
  CastepDensityReader(SurfaceGenerator sg, BufferedReader br) {
    super(sg, br);
    canDownsample = isProgressive = false;
    isAngstroms = true;
  }

  /*
   * 
  BEGIN header
  
           Real Lattice(A)               Lattice parameters(A)    Cell Angles
   4.5532597   0.0000000   0.0000000     a =    4.553260  alpha =   90.000000
   0.0000000   4.5532597   0.0000000     b =    4.553260  beta  =   90.000000
   0.0000000   0.0000000   2.9209902     c =    2.920990  gamma =   90.000000
  
   1                            ! nspins
  50    50    32                ! fine FFT grid along <a,b,c>
  END header: data is "<a b c> charge" in units of electrons/grid_point * number of grid_points
  
     1     1     1          591.571292
  */

  @Override
  protected void readParameters() throws Exception {
    jvxlFileHeaderBuffer = new StringBuffer();
    while (readLine() != null && line.indexOf(".") < 0) {
      // skip front stuff
    }
    for (int i = 0; i < 3; ++i) {
      Vector3f voxelVector = volumetricVectors[i];
      voxelVector.set(parseFloat(line), parseFloat(), parseFloat());
      readLine();
    }
    nSurfaces = parseInt(readLine());
    readLine();
    voxelCounts[0] = (nPointsX = parseInt(line)) + 1;
    voxelCounts[1] = (nPointsY = parseInt()) + 1;
    voxelCounts[2] = (nPointsZ = parseInt()) + 1;
    nFilePoints = (nPointsX++) * (nPointsY++) * (nPointsZ++);
    for (int i = 0; i < 3; i++) {
      volumetricVectors[i].scale(1f/(voxelCounts[i] - 1));
      if (isAnisotropic)
        setVectorAnisotropy(volumetricVectors[i]);
    }
    while (readLine().trim().length() > 0) {
      //
    }
  }

  private int nSkip;
  
  @Override
  protected void gotoData(int n, int nPoints) throws Exception {
    nSkip = n;
  }
  
  @Override
  protected void readSurfaceData(boolean isMapData) throws Exception {
    initializeSurfaceData();
    voxelData = new float[nPointsX][nPointsY][nPointsZ];
    readLine();
    String[] tokens = getTokens();
    if (nSkip > 0 && tokens.length < 3 + nSurfaces) {
      for (int j = 0; j < nSkip; j++)
        for (int i = 0; i < nFilePoints; i++)
          readLine();
      nSkip = 0;
    }
    for (int i = 0; i < nFilePoints; i++) {
      int x = parseInt(line) - 1;
      int y = parseInt() - 1;
      int z = parseInt() - 1;
      if (nSkip > 0)
        skipPoints(nSkip);
      voxelData[x][y][z] = recordData(parseFloat());
      readLine();
    }

    // add in periodic face data

    int n;
    n = nPointsX - 1;
    for (int i = 0; i < nPointsY; ++i)
      for (int j = 0; j < nPointsZ; ++j)
        voxelData[n][i][j] = voxelData[0][i][j];
    n = nPointsY - 1;
    for (int i = 0; i < nPointsX; ++i)
      for (int j = 0; j < nPointsZ; ++j)
        voxelData[i][n][j] = voxelData[i][0][j];
    n = nPointsZ - 1;
    for (int i = 0; i < nPointsX; ++i)
      for (int j = 0; j < nPointsY; ++j)
        voxelData[i][j][n] = voxelData[i][j][0];

    // for map data, just pick out near points and get rid of voxelData

    if (isMapData && volumeData.hasPlane()) {
      volumeData.setVoxelMap();
      for (int x = 0; x < nPointsX; ++x) {
        for (int y = 0; y < nPointsY; ++y) {
          for (int z = 0; z < nPointsZ; ++z) {
            float f = volumeData.getToPlaneParameter();
            if (volumeData.isNearPlane(x, y, z, f))
              volumeData.setVoxelMapValue(x, y, z, voxelData[x][y][z]);
          }
        }
      }
      voxelData = null;
    }
    volumeData.setVoxelData(voxelData);
    if (dataMin > params.cutoff)
      params.cutoff = 2 * dataMin;
  }

  private void skipPoints(int n) {
    int pt = next[0];
    for (int i = 0; i < n; i++) {
      while (pt < line.length() && Character.isWhitespace(line.charAt(pt++))) {
        // skip white space
      }
      while (pt < line.length() && !Character.isWhitespace(line.charAt(pt++))) {
        // skip not white space
      }
    }
    next[0] = pt;
  }
  

}

