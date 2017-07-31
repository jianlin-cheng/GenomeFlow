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

import org.jmol.api.AtomIndexIterator;
import org.jmol.api.Interface;
import org.jmol.api.MepCalculationInterface;
import org.jmol.jvxl.data.MeshData;
import org.jmol.util.Logger;

class AtomPropertyMapper extends AtomDataReader {

  private MepCalculationInterface mepCalc;
  private String mepType;
  private int calcType = 0;
  AtomPropertyMapper(SurfaceGenerator sg, String mepType) {
    super(sg);
    this.mepType = mepType;
  }
  //// maps property data ////
  
  private boolean doSmoothProperty;
  private AtomIndexIterator iter;
  private float smoothingPower;

  
  @Override
  protected void setup(boolean isMapData) {
    super.setup(isMapData);
    // MAP only
    haveSurfaceAtoms = true;
    volumeData.sr = this;
    volumeData.doIterate = false;
    point = params.point;
    doSmoothProperty = params.propertySmoothing;
    doUseIterator = true;
    if (doSmoothProperty) {
      smoothingPower = params.propertySmoothingPower;
      if (smoothingPower < 0)
        smoothingPower = 0;
      else if (smoothingPower > 10)
        smoothingPower = 10;
      if (smoothingPower == 0)
        doSmoothProperty = false;
      smoothingPower = (smoothingPower - 11) / 2f;
      // 0 to 10 becomes d^-10 to d^-1, and we'll be using distance^2
    }
    maxDistance = params.propertyDistanceMax;
    if (mepType != null) {
      doSmoothProperty = true;
      if (params.mep_calcType >= 0)
        calcType = params.mep_calcType;
      mepCalc = (MepCalculationInterface) Interface.getOptionInterface("quantum."
          + mepType + "Calculation");
    }
    if (!doSmoothProperty && maxDistance == Integer.MAX_VALUE)
      maxDistance = 5; // usually just local to a group
    //if (maxDistance == Integer.MAX_VALUE && calcType != params.mep_calcType)
      //maxDistance = 5; // max distance just for mep 
    getAtoms(params.bsSelected, doAddHydrogens, true, false, false, true, false, Float.NaN);
    if (meshDataServer != null)
      meshDataServer.fillMeshData(meshData, MeshData.MODE_GET_VERTICES, null);
    if (!doSmoothProperty && meshData.vertexSource != null) {
      hasColorData = true;
      for (int i = meshData.vertexCount; --i >= 0;) {
        int iAtom = meshData.vertexSource[i];
        if (iAtom >= 0) {
          meshData.vertexValues[i] = params.theProperty[iAtom];
        } else {
          hasColorData = false;
          break;
        }
      }
    }

    setHeader("property", params.calculationType);
    // for plane mapping
    setRanges(params.solvent_ptsPerAngstrom, params.solvent_gridMax, 0);
    params.cutoff = 0;
  }

  @Override
  protected void setVolumeData() {
    if (params.thePlane != null)
      super.setVolumeData();// unnecessary -- point-specific property mapper only    
  }

  @Override
  protected void initializeMapping() {
    if (Logger.debugging)
      Logger.startTimer();
    if (bsNearby != null)
      bsMySelected.or(bsNearby);
    iter = atomDataServer.getSelectedAtomIterator(bsMySelected, false, false, false);
  }
  
  @Override
  protected void finalizeMapping() {
    iter.release();
    iter = null;
    if (Logger.debugging)
      Logger.checkTimer("property mapping time");
  }
  
  //////////// meshData extensions ////////////

  /////////////// calculation methods //////////////
    
  @Override
  protected void generateCube() {
    // not applicable
  }

  
  private int iAtomSurface;
  @Override
  public int getSurfaceAtomIndex() {
    return iAtomSurface;
  }
  
  @Override
  public float getValueAtPoint(Point3f pt) {
    float dmin = Float.MAX_VALUE;
    float dminNearby = Float.MAX_VALUE;
    float value = (doSmoothProperty ? 0 : Float.NaN);
    float vdiv = 0;
    atomDataServer.setIteratorForPoint(iter, modelIndex, pt, maxDistance);
    iAtomSurface = -1;
    while (iter.hasNext()) {
      int ia = iter.next();
      int iAtom = myIndex[ia];
      boolean isNearby = (iAtom >= firstNearbyAtom);
      Point3f ptA = atomXyz[iAtom];
      float p = atomProp[iAtom];
      //System.out.println(iAtom + " " + ia + ptA + " " + isNearby + " " + p);
      if (Float.isNaN(p))
        continue;
      float d2 = pt.distanceSquared(ptA);
      if (isNearby) {
        if (d2 < dminNearby) {
          dminNearby = d2;
          if (!doSmoothProperty && dminNearby < dmin) {
            dmin = d2;
            value = Float.NaN;
          }
        }
      } else if (d2 < dmin) {
        dmin = d2;
        iAtomSurface = iAtom;
        if (!doSmoothProperty)
          value = p;
      }
      if (mepCalc != null) {
        value += mepCalc.valueFor(p, d2, calcType);
      } else if (doSmoothProperty) {
        d2 = (float) Math.pow(d2, smoothingPower);
        vdiv += d2;
        value += d2 * p;
      }
    }
    //System.out.println(pt + " " + value + " " + vdiv + " " + value / vdiv);
    return (mepCalc != null ? value : doSmoothProperty ? (vdiv == 0
        || dminNearby < dmin ? Float.NaN : value / vdiv) : value);
  }

}
