/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2007-11-23 12:49:25 -0600 (Fri, 23 Nov 2007) $
 * $Revision: 8655 $
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
 *  Lesser General License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jmol.minimize.forcefield;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jmol.minimize.MinAngle;
import org.jmol.minimize.MinAtom;
import org.jmol.minimize.MinBond;
import org.jmol.minimize.MinObject;
import org.jmol.minimize.MinTorsion;
import org.jmol.util.TextFormat;

/**
 * @author  Bob Hanson  5/10/12 - 5/15/12
 * 
 */

class CalculationsMMFF extends Calculations {

  final static double FPAR = 143.9325;

  public static final int DA_D = 'D';
  public static final int DA_DA = DA_D + 'A';

  private Map<Integer, Object> ffParams;

  DistanceCalc bondCalc;
  AngleCalc angleCalc;
  TorsionCalc torsionCalc;
  OOPCalc oopCalc;
  VDWCalc vdwCalc;
  ESCalc esCalc;
  SBCalc sbCalc;
  
  ForceFieldMMFF mmff;
  
  CalculationsMMFF(ForceField ff, Map<Integer, Object> ffParams, 
      MinAtom[] minAtoms, MinBond[] minBonds, 
      MinAngle[] minAngles, MinTorsion[] minTorsions,
      List<Object[]> constraints) {
    super(ff, minAtoms, minBonds, minAngles, minTorsions, constraints);
    mmff = (ForceFieldMMFF) ff;
    this.ffParams = ffParams;
    bondCalc = new DistanceCalc();
    angleCalc = new AngleCalc();
    sbCalc = new SBCalc();
    torsionCalc = new TorsionCalc();
    oopCalc = new OOPCalc();
    vdwCalc = new VDWCalc();
    esCalc = new ESCalc();
  }
  
  @Override
  String getUnits() {
    return "kcal"; 
  }

  @Override
  boolean setupCalculations() {

    List<Object[]> calc;

    DistanceCalc distanceCalc = new DistanceCalc();
    calc = calculations[CALC_DISTANCE] = new ArrayList<Object[]>();
    for (int i = 0; i < bondCount; i++)
      distanceCalc.setData(calc, minBonds[i]);

    calc = calculations[CALC_ANGLE] = new ArrayList<Object[]>();
    AngleCalc angleCalc = new AngleCalc();
    for (int i = 0; i < angleCount; i++)
      angleCalc.setData(calc, minAngles[i]);

    calc = calculations[CALC_STRETCH_BEND] = new ArrayList<Object[]>();
    SBCalc sbCalc = new SBCalc();
    for (int i = 0; i < angleCount; i++)
      sbCalc.setData(calc, minAngles[i]);

    calc = calculations[CALC_TORSION] = new ArrayList<Object[]>();
    TorsionCalc torsionCalc = new TorsionCalc();
    for (int i = 0; i < torsionCount; i++)
      torsionCalc.setData(calc, minTorsions[i]);

    calc = calculations[CALC_OOP] = new ArrayList<Object[]>();
    // set up the special atom arrays
    OOPCalc oopCalc = new OOPCalc();
    for (int i = 0; i < atomCount; i++)
      if (isInvertible(minAtoms[i]))
        oopCalc.setData(calc, i);

    pairSearch(calculations[CALC_VDW] = new ArrayList<Object[]>(), new VDWCalc(),
        calculations[CALC_ES] = new ArrayList<Object[]>(), new ESCalc());

    return true;
  }

  protected boolean isLinear(int i) {
    return MinAtom.isLinear(minAtoms[i]);
  }

  private static boolean isInvertible(MinAtom a) {
    
    // defined for typeB = 2, 3, 8, 10, 17, 26, 30, 37, 39, 40, 41, 43, 
    // 45, 49, 54, 55, 56, 57, 58, 63, 64, 67, 69, 73, 78, 80, 81, 82
    // but is 0 for 8 (amines), 17 (sulfoxides), 
    // 26 (PD3), 43 (N-S), 73 (O-S(=O)R, 82 (N-oxide) 
    // that is, just the planar ones:
    // 2, 3, 10, 30, 37, 39, 40, 41, 
    // 45, 49, 54, 55, 56, 57, 58, 63, 
    // 64, 67, 69, 78, 80, 81
    switch (a.ffType) {
    default:
      return false;
    case 2:
    case 3:
    case 10:
    case 30:
    case 37:
    case 39:
    case 40:
    case 41:
    case 45:
    case 49:
    case 54:
    case 55:
    case 56:
    case 57:
    case 58:
    case 63:
    case 64:
    case 67:
    case 69:
    case 78:
    case 80:
    case 81:
      return true;
    }
  }

  @Override
  double compute(int iType, Object[] dataIn) {

    switch (iType) {
    case CALC_DISTANCE:
      return bondCalc.compute(dataIn);
    case CALC_ANGLE:
      return angleCalc.compute(dataIn);
    case CALC_STRETCH_BEND:
      return sbCalc.compute(dataIn);
    case CALC_TORSION:
      return torsionCalc.compute(dataIn);
    case CALC_OOP:
      return oopCalc.compute(dataIn);
    case CALC_VDW:
      return vdwCalc.compute(dataIn);
    case CALC_ES:
      return esCalc.compute(dataIn);
    }
    return 0.0;
  }

  Object getParameter(MinObject a) {
    return (a.key == null || a.ddata != null ? a.ddata : ffParams.get(a.key));
  }

  Object getParameter(Integer key) {
    return ffParams.get(key);
  }
  class DistanceCalc extends Calculation {

    final static double FSTRETCH = FPAR / 2;
    final static double CS = -2.0;
    final static double CS2 = ((7.0/12.0)*(CS * CS));
    
    double r0, kb;
    double delta2;

    void setData(List<Object[]> calc, MinBond bond) {
      ia = bond.data[0];
      ib = bond.data[1];
      Object data = getParameter(bond);
      if (data == null)
        return;
      calc.add(new Object[] { new int[] { ia, ib },  data });
    }

    @Override
    double compute(Object[] dataIn) {
      
      getPointers(dataIn);
      kb = dData[0];
      r0 = dData[1];
      setPairVariables(this);
      
      delta = rab - r0; 
      delta2 = delta * delta;
      energy = FSTRETCH * kb * delta2 * (1 + CS * delta + CS2  * (delta2));

      if (gradients) {
        dE = FSTRETCH * kb * delta * (2 + 3 * CS * delta + 4 * CS2 * delta2);
        addForces(this, 2);
      }
      
      if (logging)
        appendLogData(getDebugLine(CALC_DISTANCE, this));
      
      return energy;
    }
  }

  
  class AngleCalc extends Calculation {

    void setData(List<Object[]> calc, MinAngle angle) {
      Object data = getParameter(angle);
      if (data == null)
        return;
      calc.add(new Object[] { angle.data, data, angle.key });      
    }

    final static double CB = -0.4 * DEG_TO_RAD;
    
    @Override
    double compute(Object[] dataIn) {
      
      key = (Integer) dataIn[2];

      getPointers(dataIn);
      double ka = dData[0];
      double t0 = dData[1];
      setAngleVariables(this);

      double dt = (theta * RAD_TO_DEG - t0);

      // could have problems here for very distorted structures.

      if (t0 == 180) {
        energy = FPAR * ka * (1 + Math.cos(theta));
        if (gradients)
          dE = -FPAR * ka * Math.sin(theta);
      } else {
        energy = 0.021922 * ka * Math.pow(dt, 2) * (1 + CB * dt); // 0.043844/2
        if (gradients)
          dE = 0.021922 * ka * dt * (2 + 3 * CB * dt);
      }
      if (gradients)
        addForces(this, 3);
      
      if (logging)
        appendLogData(getDebugLine(CALC_ANGLE, this));
      
      return energy;
    }

  }

  class SBCalc extends Calculation {
    
    void setData(List<Object[]> calc, MinAngle angle) {
      // not applicable for linear types
      if (isLinear(angle.data[1]))
        return;
      double[] data = (double[]) getParameter(angle.sbKey);
      double[] datakat0 = (double[]) getParameter(angle);
      double[] dataij = (double[]) getParameter(minBonds[angle.data[ForceField.ABI_IJ]]);
      double[] datajk = (double[]) getParameter(minBonds[angle.data[ForceField.ABI_JK]]);
      if (data == null || datakat0 == null || dataij == null || datajk == null)
        return;
      double theta0 = datakat0[1];
      double r0ij = dataij[1];
      double r0jk = datajk[1];
      calc.add(new Object[] { angle.data, new double[] { data[0], theta0, r0ij } });
      calc.add(new Object[] { new int[] {angle.data[2], angle.data[1], angle.data[0]}, 
          new double[] { data[1], theta0, r0jk } });
    }

    @Override
    double compute(Object[] dataIn) {
      getPointers(dataIn);
      double k = 2.51210 * dData[0];
      double t0 = dData[1];
      double r0_ab = dData[2];

      setPairVariables(this);
      setAngleVariables(this);
      double dr_ab = rab - r0_ab;
      delta = theta * RAD_TO_DEG - t0;
      // equation 5
      energy = k * dr_ab * delta;

      if (logging)
        appendLogData(getDebugLine(CALC_STRETCH_BEND, this));
      
      if (gradients) {
        dE = k * dr_ab;
        addForces(this, 3);
        setPairVariables(this);
        dE = k * delta;
        addForces(this, 2);        
      }
      
      return energy;
    }
  }

  class TorsionCalc extends Calculation {

    void setData(List<Object[]> calc, MinTorsion t) {
      if (isLinear(t.data[1]) || isLinear(t.data[2]))
        return;
      Object data = getParameter(t);
      if (data == null)
        return;
      calc.add(new Object[] { t.data, data, t.key });
    }
    
    @Override
    double compute(Object[] dataIn) {
      
      key = (Integer) dataIn[2];

      getPointers(dataIn);
      double v1 = dData[0];
      double v2 = dData[1];
      double v3 = dData[2];
      
      setTorsionVariables(this);

      // use one single cosine calculation 
      
      double cosTheta = Math.cos(theta);
      double cosTheta2 = cosTheta * cosTheta;
      
      energy = 0.5 * (v1 * (1 + cosTheta)
          + v2 * (2 - 2 * cosTheta2)
          + v3 * (1 + cosTheta * (4 * cosTheta2 - 3)));

/*          
        energy = 0.5 * (v1 * (1.0 + Math.cos(theta)) 
            + v2 * (1 - Math.cos(2 * theta)) 
            + v3 * (1 + Math.cos(3 * theta)));
*/
      if (gradients) {
        double sinTheta = Math.sin(theta);        
        dE = 0.5 * (-v1 * sinTheta 
            + 4 * v2 * sinTheta * cosTheta 
            + 3 * v3 * sinTheta * (1 - 4 * cosTheta2));
/*
        dE = 0.5 * (-v1 * sinTheta 
        + 2 * v2 * Math.sin(2 * theta) 
        - 3 * v3 * Math.sin(3 * theta));
*/        
        addForces(this, 4);
      }
      
      if (logging)
        appendLogData(getDebugLine(CALC_TORSION, this));
      
      return energy;
    }
  }
  
  class OOPCalc extends Calculation {

    final static double FOOPD = 0.043844 * RAD_TO_DEG;
    final static double FOOP = FOOPD / 2 * RAD_TO_DEG;

    int[] list = new int[4];
    
    void setData(List<Object[]> calc, int i) {
      if (minAtoms[i].nBonds != 3)
        return;// should not be possible...
      int[] indices = minAtoms[i].getBondedAtomIndexes();
      // our calculation is for first, not last, relative to plane of others, 
      list[0] = indices[2];
      list[1] = i;
      list[2] = indices[1];
      list[3] = indices[0];
      double koop = mmff.getOutOfPlaneParameter(list);
      if (koop == 0)
        return;
      double[] dk = new double[] { koop };
      calc.add(new Object[] { new int[] { indices[0], i, indices[1], indices[2] },  dk });
      calc.add(new Object[] { new int[] { indices[1], i, indices[2], indices[0] },  dk });
      calc.add(new Object[] { new int[] { indices[2], i, indices[0], indices[1] },  dk });
    }

    @Override
    double compute(Object[] dataIn) {
      
      getPointers(dataIn);
      setOopVariables(this, false);
      double koop = dData[0];
      
      energy = FOOP * koop * theta * theta; // theta in radians
      
      if (gradients) {
        dE = FOOPD * koop * theta;
        addForces(this, 4);
      }

      if (logging)
        appendLogData(getDebugLine(CALC_OOP, this));

      return energy;
    }
  }
  
  class VDWCalc extends PairCalc {
    
    @Override
    void setData(List<Object[]> calc, int ia, int ib) {
      a = minAtoms[ia];
      b = minAtoms[ib];
      double[] dataA = (double[]) getParameter(a.vdwKey);
      double[] dataB = (double[]) getParameter(b.vdwKey);
      if (dataA == null || dataB == null)
        return;
      
      double alpha_a = dataA[0]; 
      double N_a = dataA[1]; 
      double A_a = dataA[2]; 
      double G_a = dataA[3]; 
      int DA_a = (int) dataA[4];
      
      double alpha_b = dataB[0]; 
      double N_b = dataB[1]; 
      double A_b = dataB[2]; 
      double G_b = dataB[3]; 
      int DA_b = (int) dataB[4]; 
      
      double rs_aa = A_a * Math.pow(alpha_a, 0.25);
      double rs_bb = A_b * Math.pow(alpha_b, 0.25);
      double gamma = (rs_aa - rs_bb) / (rs_aa + rs_bb);
      double rs = 0.5 * (rs_aa + rs_bb);
      if (DA_a != DA_D && DA_b != DA_D)
        rs *= (1.0 + 0.2 * (1.0 - Math.exp(-12.0 * gamma * gamma)));
      double eps = ((181.16 * G_a * G_b * alpha_a * alpha_b) 
          / (Math.sqrt(alpha_a / N_a) + Math.sqrt(alpha_b / N_b))) * Math.pow(rs, -6.0);

      if(DA_a + DA_b == DA_DA) {
        rs *= 0.8;
        eps *= 0.5;
      }
      calc.add(new Object[] { new int[] {ia, ib}, new double[] { rs, eps } });
    }

    @Override
    double compute(Object[] dataIn) {
      getPointers(dataIn);
      setPairVariables(this);
      double rs = dData[0];
      double eps = dData[1];
      double r_rs = rab / rs;
      double f1 = 1.07 / (r_rs + 0.07);
      double f2 = 1.12 / (Math.pow(r_rs, 7) + 0.12);
      
      energy = eps * Math.pow(f1, 7)  * (f2 - 2);
      
      if (gradients) {
        // dE = eps ( 7(f1^6)df1(f2-2) + (f1^7)df2 )
        // dE = eps f1^6 ( 7df1(f2-2) + f1(df2) )
        // df1/dr = -1.07 / (r_rs + 0.07)^2 * 1/rs 
        //        = -f1^2 / 1.07 * 1/rs
        // df2/dr = -1.12 / (r_rs^7 + 0.12)^2 * 7(r_rs)^6 * 1/rs 
        //        = -f2^2 / 1.12 * 7(r_rs)^6 * 1/rs
        // dE = -7 eps f1^7 / rs ( (f2-2)(f1 /1.07) + f2^2 / 1.12 * r_rs^6
        dE = -7 * eps * Math.pow(f1, 7) /rs 
            * (f1 / 1.07 * (f2 - 2) + f2 * f2 * Math.pow(r_rs, 6));
        addForces(this, 2);
      }

      if (logging && Math.abs(energy) > 0.1)
        appendLogData(getDebugLine(CALC_VDW, this));

      return energy;
    } 
  }
  
  class ESCalc extends PairCalc {

    private static final double BUFF = 0.05;

    @Override
    void setData(List<Object[]> calc, int ia, int ib) {
      if (minAtoms[ia].partialCharge == 0 || minAtoms[ib].partialCharge == 0)
        return;
      calc.add(new Object[] { new int[] { ia, ib }, new double[] {
           minAtoms[ia].partialCharge, minAtoms[ib].partialCharge, 
           (minAtoms[ia].bs14.get(ib) ? 249.0537 : 332.0716) }
      });
    }

    @Override
    double compute(Object[] dataIn) {
      getPointers(dataIn);
      double f = dData[0] * dData[1] * dData[2];
      setPairVariables(this);
      double d = rab + BUFF;
      energy = f / d; // DIEL = 1 here
      
      if (gradients) {
        dE = -energy / d;
        addForces(this, 2);
      }

      if (logging && Math.abs(energy) > 20)
        appendLogData(getDebugLine(CALC_ES, this));

      return energy;
    }
  }
  
  ///////// REPORTING /////////////
  
  @Override
  String getDebugHeader(int iType) {
    switch (iType){
    case -1:
      return  "MMFF94 Force Field -- " +
          "T. A. Halgren, J. Comp. Chem. 5 & 6 490-519ff (1996).\n";
    case CALC_TORSION:
      return 
           "\nT O R S I O N A L (" + minTorsions.length + " torsions)\n\n"
           +"      ATOMS           ATOM TYPES          TORSION\n"
           +"  I   J   K   L   I     J     K     L      ANGLE       V1       V2       V3     ENERGY\n"
           +"--------------------------------------------------------------------------------------\n";
    default:
      return super.getDebugHeader(iType);
    }
  }

  @Override
  String getDebugLine(int iType, Calculation c) {
    float energy = ff.toUserUnits(c.energy);
    switch (iType) {
    case CALC_ANGLE:
    case CALC_STRETCH_BEND:
      return TextFormat.sprintf(
          "%15s  %-5s %-5s %-5s  %8.3f  %8.3f     %8.3f   %8.3f", 
          new Object[] {  MinObject.decodeKey(c.key), minAtoms[c.ia].sType, minAtoms[c.ib].sType, 
              minAtoms[c.ic].sType,
          new float[] { (float)(c.theta * RAD_TO_DEG), (float) c.dData[1] /*THETA0*/, 
              (float)c.dData[0]/*Kijk*/, energy },
          new int[] { minAtoms[c.ia].atom.getAtomNumber(), minAtoms[c.ib].atom.getAtomNumber(),
              minAtoms[c.ic].atom.getAtomNumber()} });
      case CALC_TORSION:
        return TextFormat.sprintf(
              "%15s  %-5s %-5s %-5s %-5s  %8.3f %8.3f %8.3f %8.3f %8.3f", 
            new Object[] { MinObject.decodeKey(c.key), 
                 minAtoms[c.ia].sType, minAtoms[c.ib].sType, 
                 minAtoms[c.ic].sType, minAtoms[c.id].sType, 
            new float[] { (float) (c.theta * RAD_TO_DEG), (float) c.dData[0]/*v1*/, (float) c.dData[1]/*v2*/, (float) c.dData[2]/*v3*/, 
              energy } });
      default:
        return super.getDebugLine(iType, c);
    }
  }


}

