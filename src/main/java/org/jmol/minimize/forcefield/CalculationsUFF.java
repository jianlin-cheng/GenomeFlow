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
import org.jmol.minimize.MinTorsion;
import org.jmol.minimize.Util;

/*
 * Java implementation by Bob Hanson 3/2008
 * based on OpenBabel code in C++ by Tim Vandermeersch 
 * and Geoffrey Hutchison, with permission.
 *    
 * Original comments:
 * 
 * http://towhee.sourceforge.net/forcefields/uff.html
 * http://rdkit.org/
 * http://franklin.chm.colostate.edu/mmac/uff.html
 *(for the last, use the Wayback Machine: http://www.archive.org/
 * As well, the main UFF paper:
 * Rappe, A. K., et. al.; J. Am. Chem. Soc. (1992) 114(25) p. 10024-10035.
 */

class CalculationsUFF extends Calculations {

  private Map<Object, FFParam> ffParams;
  protected FFParam parA, parB, parC;  

  public final static int PAR_R = 0;       // covalent radius
  public final static int PAR_THETA = 1;   // covalent angle
  public final static int PAR_X = 2;       // nonbond distance
  public final static int PAR_D = 3;       // nonbond energy
  public final static int PAR_ZETA = 4;    // nonbond scale   -- not used
  public final static int PAR_Z = 5;       // effective charge
  public final static int PAR_V = 6;       // sp3 torsional barrier parameter
  public final static int PAR_U = 7;       // sp2 torsional barrier parameter
  public final static int PAR_XI = 8;      // GMP electronegativity
  public final static int PAR_HARD = 9;    // not used?
  public final static int PAR_RADIUS = 10; // not used?

  DistanceCalc bondCalc;
  AngleCalc angleCalc;
  TorsionCalc torsionCalc;
  OOPCalc oopCalc;
  VDWCalc vdwCalc;
  //ESCalc esCalc;
    
  CalculationsUFF(ForceField ff, Map<Object, FFParam> ffParams, 
      MinAtom[] minAtoms, MinBond[] minBonds, 
      MinAngle[] minAngles, MinTorsion[] minTorsions, 
      List<Object[]> constraints) {
    super(ff, minAtoms, minBonds, minAngles, minTorsions, constraints);    
    this.ffParams = ffParams;
    bondCalc = new DistanceCalc();
    angleCalc = new AngleCalc();
    torsionCalc = new TorsionCalc();
    oopCalc = new OOPCalc();
    vdwCalc = new VDWCalc();
    //esCalc = new ESCalc();
  }
  
  @Override
  String getUnits() {
    return "kJ";
  }

  @Override
  boolean setupCalculations() {

    List<Object[]> calc;

    DistanceCalc distanceCalc = new DistanceCalc();
    calc = calculations[CALC_DISTANCE] = new ArrayList<Object[]>();
    for (int i = 0; i < bondCount; i++) {
      MinBond bond = minBonds[i];
      double bondOrder = bond.order;
      if (bond.isAromatic)
        bondOrder = 1.5;
      if (bond.isAmide)
        bondOrder = 1.41;  
      distanceCalc.setData(calc, bond.data[0], bond.data[1], bondOrder);
    }

    calc = calculations[CALC_ANGLE] = new ArrayList<Object[]>();
    AngleCalc angleCalc = new AngleCalc();
    for (int i = minAngles.length; --i >= 0;)
      angleCalc.setData(calc, minAngles[i].data);

    calc = calculations[CALC_TORSION] = new ArrayList<Object[]>();
    TorsionCalc torsionCalc = new TorsionCalc();
    for (int i = minTorsions.length; --i >= 0;)
      torsionCalc.setData(calc, minTorsions[i].data);

    calc = calculations[CALC_OOP] = new ArrayList<Object[]>();
    // set up the special atom arrays
    OOPCalc oopCalc = new OOPCalc();
    int elemNo;
    for (int i = 0; i < atomCount; i++) {
      MinAtom a = minAtoms[i];
      if (a.nBonds == 3 && isInvertible(elemNo = a.atom.getElementNumber()))
        oopCalc.setData(calc, i, elemNo);
    }

    // Note that while the UFF paper mentions an electrostatic term,
    // it does not actually use it. Both Towhee and the UFF FAQ
    // discourage the use of electrostatics with UFF.

    pairSearch(calculations[CALC_VDW] = new ArrayList<Object[]>(), new VDWCalc(), null, null);
    return true;
  }

  private static boolean isInvertible(int n) {
    switch (n) {
    case 6: // C
    case 7: // N
    case 8: // O
    case 15: // P
    case 33: // As
    case 51: // Sb
    case 83: // Bi
      return true;
    default: 
      return false;// no inversion term for this element
    }
  }

  static double calculateR0(double ri, double rj, double chiI, double chiJ,
                            double bondorder) {
    // precompute the equilibrium geometry
    // From equation 3
    double rbo = -0.1332 * (ri + rj) * Math.log(bondorder);
    // From equation 4
    
    double dchi = Math.sqrt(chiI) - Math.sqrt(chiJ);
    double ren = ri * rj * dchi * dchi / (chiI * ri + chiJ * rj);
    // From equation 2
    // NOTE: See http://towhee.sourceforge.net/forcefields/uff.html
    // There is a typo in the published paper
    return (ri + rj + rbo - ren);
  }

  @Override
  double compute(int iType, Object[] dataIn) {

    switch (iType) {
    case CALC_DISTANCE:
      return bondCalc.compute(dataIn);
    case CALC_ANGLE:
      return angleCalc.compute(dataIn);
    case CALC_TORSION:
      return torsionCalc.compute(dataIn);
    case CALC_OOP:
      return oopCalc.compute(dataIn);
    case CALC_VDW:
      return vdwCalc.compute(dataIn);
    //case CALC_ES:
      //return esCalc.compute(dataIn);
    }
    return 0.0;
  }

  FFParam getParameter(Object a) {
    return ffParams.get(a);
  }

  final static double KCAL332 = KCAL_TO_KJ * 332.0637;

  class DistanceCalc extends Calculation {

    double r0, kb;

    void setData(List<Object[]> calc, int ia, int ib, double bondOrder) {
      parA = getParameter(minAtoms[ia].sType);
      parB = getParameter(minAtoms[ib].sType);
      r0 = calculateR0(parA.dVal[PAR_R], parB.dVal[PAR_R], parA.dVal[PAR_XI],
          parB.dVal[PAR_XI], bondOrder);

      // here we fold the 1/2 into the kij from equation 1a
      // Otherwise, this is equation 6 from the UFF paper.

      kb = KCAL332 * parA.dVal[PAR_Z] * parB.dVal[PAR_Z] / (r0 * r0 * r0);
      calc.add(new Object[] { new int[] { ia, ib },
          new double[] { r0, kb, bondOrder } });
    }

    @Override
    double compute(Object[] dataIn) {
      getPointers(dataIn);
      r0 = dData[0];
      kb = dData[1];     
      setPairVariables(this);

      // Er = 0.5 k (r - r0)^2
      
      delta = rab - r0;     // we pre-compute the r0 below
      energy = kb * delta * delta; // 0.5 factor was precalculated

      if (gradients) {
        dE = 2.0 * kb * delta;
        addForces(this, 2);
      }
      
      if (logging)
        appendLogData(getDebugLine(CALC_DISTANCE, this));
      
      return energy;
    }
  }

  
  final static double KCAL644 = 644.12 * KCAL_TO_KJ;
  
  class AngleCalc extends Calculation {
  
    void setData(List<Object[]> calc, int[] angle) {
      a = minAtoms[ia = angle[0]];
      b = minAtoms[ib = angle[1]];
      c = minAtoms[ic = angle[2]];
      double preliminaryMagnification = (a.sType == "H_" && c.sType == "H_" ? 10 : 1);
      parA = getParameter(a.sType);
      parB = getParameter(b.sType);
      parC = getParameter(c.sType);

      int coordination = parB.iVal[0]; // coordination of central atom

      double zi = parA.dVal[PAR_Z];
      double zk = parC.dVal[PAR_Z];
      double theta0 = parB.dVal[PAR_THETA];
      double cosT0 = Math.cos(theta0);
      double sinT0 = Math.sin(theta0);
      double c0, c1, c2;
      switch (coordination) {
      case 1:
      case 2:
      case 4:
      case 6:
        c0 = c1 = c2 = 0;
        break;
      default:  
        c2 = 1.0 / (4.0 * sinT0 * sinT0);
        c1 = -4.0 * c2 * cosT0;
        c0 = c2 * (2.0 * cosT0 * cosT0 + 1.0);
      }

      // Precompute the force constant
      MinBond bond = a.getBondTo(ib);
      double bondorder = bond.order;
      if (bond.isAromatic)
        bondorder = 1.5;
      if (bond.isAmide)
        bondorder = 1.41;
      rab = calculateR0(parA.dVal[PAR_R], parB.dVal[PAR_R], parA.dVal[PAR_XI], parB.dVal[PAR_XI], bondorder);

      bond = c.getBondTo(ib);
      bondorder = bond.order;
      if (bond.isAromatic)
        bondorder = 1.5;
      if (bond.isAmide)
        bondorder = 1.41;
      double rbc = calculateR0(parB.dVal[PAR_R], parC.dVal[PAR_R], 
          parB.dVal[PAR_XI], parC.dVal[PAR_XI], bondorder);
      double rac = Math.sqrt(rab * rab + rbc * rbc - 2.0 * rab * rbc * cosT0);

      // Equation 13 from paper -- corrected by Towhee
      // Note that 1/(rij * rjk) cancels with rij*rjk in eqn. 13
      double ka = (KCAL644) * (zi * zk / (Math.pow(rac, 5.0)))
          * (3.0 * rab * rbc * (1.0 - cosT0 * cosT0) - rac * rac * cosT0);
      calc.add(new Object[] {
          new int[] { ia, ib, ic, coordination },
          new double[] { ka, theta0 * RAD_TO_DEG, c0 - c2, c1, 2 * c2, preliminaryMagnification * ka } });
    }

    @Override
    double compute(Object[] dataIn) {
      
      getPointers(dataIn);
      int coordination = iData[3];
      double ka = (isPreliminary ? dData[5] : dData[0]);
      double a0 = dData[2];
      double a1 = dData[3];
      double a2 = dData[4];
      setAngleVariables(this);

      //problem here for square planar cis or trans
      if ((coordination == 4 || coordination == 6) && 
          (theta > 2.35619 || theta < 0.785398)) // 135o, 45o
        coordination = 1;
      double cosT = Math.cos(theta);
      double sinT = Math.sin(theta);
      switch (coordination) {
      case 0: //constraint
      case 1: //sp
        energy = ka * (1.0 + cosT) * (1.0 + cosT) / 4.0;
        break;
      case 2: //sp2
         //(1 + 4cos(theta) + 4cos(theta)^2)/9 
        energy = ka * (1.0  + (4.0 * cosT) * (1.0 + cosT)) / 9.0;
        break;
      case 4: //dsp2
      case 6: //d2sp3
        energy = ka * cosT * cosT;
        break;
      default:
        // 
        energy = ka * (a0 + a1 * cosT + a2 * cosT * cosT);
      }

      if (gradients) {
        // da = dTheta/dx * dE/dTheta
        switch (coordination) {
        case 0: //constraint
        case 1:
          dE = -0.5 * ka * sinT * (1 + cosT);
          break;
        case 2:
          dE = -4.0 * sinT * ka * (1.0 - 2.0 * cosT)/9.0;
          break;
        case 4:
        case 6:
          dE = -ka * sinT * cosT;
          break;
        default:
          dE = -ka * (a1 * sinT - 2.0 * a2 * cosT * sinT);
        }
        addForces(this, 3);
      }
      
      if (logging)
        appendLogData(getDebugLine(CALC_ANGLE, this));
      
      return energy;
    }
  }

  class TorsionCalc extends Calculation {

   void setData(List<Object[]> calc, int[] t) {
      double cosNPhi0 = -1; // n * phi0 = 180; max at 0 
      int n = 0;
      double V = 0;
      a = minAtoms[ia = t[0]];
      b = minAtoms[ib = t[1]];
      c = minAtoms[ic = t[2]];
      d = minAtoms[id = t[3]];
      MinBond bc = c.getBondTo(ib);
      double bondOrder = bc.order;
      if (bc.isAromatic)
        bondOrder = 1.5;
      if (bc.isAmide)
        bondOrder = 1.41;

      parB = getParameter(b.sType);
      parC = getParameter(c.sType);

      switch (parB.iVal[0] * parC.iVal[0]) {
      case 9: // sp3 sp3
        // max at 0; minima at 60, 180, 240
        n = 3; 
        double vi = parB.dVal[PAR_V];
        double vj = parC.dVal[PAR_V];

        // exception for (group 6 -- group 6) sp3 atoms
        double viNew = 0;
        switch (b.atom.getElementNumber()) {
        case 8:
          viNew = 2.0;
          break;
        case 16:
        case 34:
        case 52:
        case 84:
          viNew = 6.8;
        }
        if (viNew != 0)
          switch (c.atom.getElementNumber()) {
          case 8:
            // max at 0; minima at 90
            vi = viNew;
            vj = 2.0;
            n = 2; 
            break;
          case 16:
          case 34:
          case 52:
          case 84:
            // max at 0; minima at 90
            vi = viNew;
            vj = 6.8;
            n = 2; 
          }
        V = 0.5 * KCAL_TO_KJ * Math.sqrt(vi * vj);
        break;
      case 4: //sp2 sp2
        // max at 90; minima at 0 and 180
        cosNPhi0 = 1; 
        n = 2; 
        V = 0.5 * KCAL_TO_KJ * 5.0
            * Math.sqrt(parB.dVal[PAR_U] * parC.dVal[PAR_U])
            * (1.0 + 4.18 * Math.log(bondOrder));
        break;
      case 6: //sp2 sp3
        // maximim at 30, 90, 150; minima at 0, 60, 120, 180
        cosNPhi0 = 1;  
        n = 6; 
        // exception for group 6 sp3 attached to non-group 6 sp2
        // maximim at 30, 90, 150; minima at 0, 60, 120, 180
        boolean sp3C = (parC.iVal[0] == 3); 
        switch ((sp3C ? c : b).atom.getElementNumber()) {
        case 8:
        case 16:
        case 34:
        case 52:
        case 84:
          switch ((sp3C ? b : c).atom.getElementNumber()) {
          case 8:
          case 16:
          case 34:
          case 52:
          case 84:
            break;
          default:
            n = 2;
            cosNPhi0 = -1; 
          }
          break;
        }
        V = 0.5 * KCAL_TO_KJ;
      }

      if (Util.isNearZero(V)) // don't bother calcuating this torsion
        return;

      calc.add(new Object[] { new int[] { ia, ib, ic, id, n },
          new double[] { V, cosNPhi0 } });
    }

    
    @Override
    double compute(Object[] dataIn) {
       
      getPointers(dataIn);
      int n = iData[4];
      double V = dData[0];
      double cosNPhi0 = dData[1];      
      setTorsionVariables(this);

      energy = V * (1.0 - cosNPhi0 * Math.cos(theta * n));

      if (gradients) {
        dE = V * n * cosNPhi0 * Math.sin(n * theta);
        addForces(this, 4);
      }
      
      if (logging)
        appendLogData(getDebugLine(CALC_TORSION, this));
      
      return energy;
    }
  }

  final static double KCAL6 = 6.0 * KCAL_TO_KJ;
  final static double KCAL22 = 22.0 * KCAL_TO_KJ;
  final static double KCAL44 = 44.0 * KCAL_TO_KJ;
  
  class OOPCalc extends Calculation {

    void setData(List<Object[]> calc, int ib, int elemNo) {

      // The original Rappe paper in JACS isn't very clear about the parameters
      // The following was adapted from Towhee

      /*
       *         a
       *         |
       *         b      theta defines the angle of b->c relative to the plane abd
       *        / \     note that we want the theta >= 0. 
       *       c   d
       * 
       *
       *   E = K [ c0 + c1 cos(theta) + c2 cos(2 theta) ]
       * 
       *   But we only allow one or the other, c1 or c2, to be nonzero. 
       *   
       *   trigonal planar species (CH2=CH2, for example), we use
       *    
       *     c0 = 1
       *     c1 = -1
       *     c2 = 0 
       *     
       *   so that the function is 
       *   
       *     E = K [1 - cos(theta)] 
       *     
       *   with a minimum at theta=0 and "barrier" height of K when theta = 90.
       * 
       *   For trigonal pyramidal species (NH3, PX3), we want the minimum at
       *   some particular angle near 90 degrees. If we wanted exactly 90 degrees, 
       *   then we would use 
       *   
       *     c0 = c1 = 0 and c2 = 1
       *     
       *   so that we would have
       *   
       *     E = K cos(2 theta)
       *     
       *   with minimum at theta=90 degrees and a barrier of K at theta=0
       *   
       *   But NH3, PH3, etc. are not exactly at 90 degrees, so we use the known hydride
       *   angle as the basis angle PHI and use the following function instead:
       *   
       *     E = K {  [cos(phi) - cos(theta)]^2 }
       *     
       *   At least, that's what I would do, because then we have a minimum at theta = phi and 
       *   a barrier approx = 0 when E = K, provided  
       *   
       *   
       *   . This works out to:
       *   
       *     E/K = cos(phi)^2 - 2cos(phi)cos(theta) + cos(theta)^2
       *     
       *   Now,  cos(theta)^2 = 1/2 cos(2 theta) + 1/2, so we have:
       *   
       *    E/K = 1/2 + cos(phi)^2 - 2 cos(phi) cos(theta) + 1/2 cos(2 theta)
       *    
       *    giving
       *    
       * [1]  c0 = 1/2 + cos(phi)^2
       *      c1 = -2 cos(phi)
       *      c2 = 1/2
       *      
       *   which has the proper barrier of E = K at theta = 0, considering phi is about 90.
       *   
       *   Oddly enough, the C++ code in OpenBabel uses
       *   
       *      c0 = 4 cos(phi)^2 + cos(2 phi)
       *      c1 = -4 cos(phi)
       *      c2 = 1
       *      
       *   I think this should be a - cos(2 phi) and all coefficients multiplied by
       *   1/2 to be consistent with this analysis. 
       *   Otherwise the barrier is too large at theta=0.
       *   
       *   What happens is we cast this as the following? 
       *   
       *     E = K [ a0 + a1 cos(theta) + a2 cos(theta)^2]
       *     
       *   We get
       *   
       *     E = K [ c0 + c1 cos(theta) + c2 cos(2 theta)]
       *     
       *       = K [ c0 + c1 cos(theta) + c2 (2 cos(theta)^2 - 1) ]
       *       
       *       = K [ (c0 - c2) + c1 cos(theta) + 2 c2 cos(theta)^2]
       *   
       *   so
       *   
       *     ao = (c0 - c2)
       *     a1 = c1
       *     a2 = 2 c2
       *     
       *   And we don't have to take two cos operations. For our three cases then we get:
       *   
       *   
       *   trigonal planar species (no change):
       *    
       *     c0 = 1       a0 =  1
       *     c1 = -1      a1 = -1
       *     c2 = 0       a2 =  0
       *     
       *   NH3/PH3, etc.:
       *   
       *     c0 = 1/2 + cos(phi)^2    a0 = cos(phi)^2
       *     c1 = -2 cos(phi)         a1 = -2 cos(phi)
       *     c2 = 1/2                 a2 = 1
       *     
       *   I have to say I like these better!
       *   
       *      
       */

      
      b = minAtoms[ib];
      int[] atomList = b.getBondedAtomIndexes();
      a = minAtoms[ia = atomList[0]];
      c = minAtoms[ic = atomList[1]];
      d = minAtoms[id = atomList[2]];

      double a0 = 1.0;
      double a1 = -1.0;
      double a2 = 0.0;
      double koop = KCAL6;
      switch (elemNo) {
      case 6: // carbon could be a carbonyl, which is considerably stronger
        // added b.sType == "C_2+" for cations 12.0.RC9
        // added b.typ "C_2" check for H-connected 12.0.RC13
        if (b.sType == "C_2" && b.hCount > 1
            || b.sType == "C_2+" || a.sType == "O_2" || c.sType == "O_2" || d.sType == "O_2") {
          koop += KCAL44;
          break;
        }/* else if (b.sType.lastIndexOf("R") == 2) 
          koop *= 10; // Bob's idea to force flat aromatic rings. 
           // Who would EVER want otherwise?
*/        break;
      case 7:
      case 8:
        break;
      default:
        koop = KCAL22;
        double phi = DEG_TO_RAD;
        switch (elemNo) {
        case 15: // P
          phi *= 84.4339;
          break;
        case 33: // As
          phi *= 86.9735;
          break;
        case 51: // Sb
          phi *= 87.7047;
          break;
        case 83: // Bi     
          phi *= 90.0;
          break;
        }
        double cosPhi = Math.cos(phi);
        a0 = cosPhi * cosPhi;
        a1 = -2.0 * cosPhi;
        a2 = 1.0;
        //
        // same as:
        //
        // E = K [ cos(theta) - cos(phi)]^2
        //
        //phi ~ 90, so c0 ~ 0, c1 ~ 0.5, and E(0) ~ K 
      }

      koop /= 3.0;

      // A-BCD 
      calc.add(new Object[] { new int[] { ia, ib, ic, id },
          new double[] { koop, a0, a1, a2, koop * 10 } });

      // C-BDA
      calc.add(new Object[] { new int[] { ic, ib, id, ia },
          new double[] { koop, a0, a1, a2, koop * 10 } });

      // D-BAC
      calc.add(new Object[] { new int[] { id, ib, ia, ic },
          new double[] { koop, a0, a1, a2, koop * 10 } });
    }

    @Override
    double compute(Object[] dataIn) {

      getPointers(dataIn);
      double koop = (isPreliminary ? dData[4] : dData[0]);
      double a0 = dData[1];
      double a1 = dData[2];
      double a2 = dData[3];
      setOopVariables(this, true);
      
      double cosTheta = Math.cos(theta);

      //energy = koop * (c0 + c1 * Math.cos(theta) + c2 * Math.cos(2.0 * theta));
      //
      //using

      energy = koop * (a0 + a1 * cosTheta + a2 * cosTheta * cosTheta);

      if (gradients) {
        // somehow we already get the -1 from the OOPDeriv -- so we'll omit it here
        // not checked in Java
        dE = koop
            * (a1 * Math.sin(theta) + a2 * 2.0 * Math.sin(theta) * cosTheta);
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
      
      FFParam parA = getParameter(a.sType);
      FFParam parB = getParameter(b.sType);

      double Xa = parA.dVal[PAR_X];
      double Da = parA.dVal[PAR_D];
      double Xb = parB.dVal[PAR_X];
      double Db = parB.dVal[PAR_D];

      //this calculations only need to be done once for each pair, 
      //we do them now and save them for later use
      double Dab = KCAL_TO_KJ * Math.sqrt(Da * Db);

      // 1-4 scaling
      // This isn't mentioned in the UFF paper, but is common for other methods
      //       if (a.IsOneFour(b))
      //         kab *= 0.5;

      // Xab is xij in equation 20 -- the expected vdw distance
      double Xab = Math.sqrt(Xa * Xb);
      calc.add(new Object[] {
          new int[] { ia, ib },
          new double[] { Xab, Dab } });
    }

    @Override
    double compute(Object[] dataIn) {

      getPointers(dataIn);
      double Xab = dData[0];
      double Dab = dData[1];
      
      setPairVariables(this);
      
      // Evdw = Dab [(Xab/r)^12 - 2(Xab/r)^6]      Lennard-Jones
      //      = Dab (Xab/r)^6[(Xab/r)^6 - 2]
      
      double term = Xab / rab;
      double term6 = term * term * term;
      term6 *= term6;
      energy = Dab * term6 * (term6 - 2.0);

      if (gradients) {
        dE = Dab * 12.0 * (1.0 - term6) * term6 * term / Xab; // unchecked
        addForces(this, 2);
      }
      
      if (logging)
        appendLogData(getDebugLine(CALC_VDW, this));
      
      return energy;
    }
  } 
/*
  
  class ESCalc extends PairCalc {

    @Override
    void setData(List<Object[]> calc, int ia, int ib) {
      a = minAtoms[ia];
      b = minAtoms[ib];
      double qq = KCAL332 * partialCharges[ia]
          * partialCharges[ib];
      if (qq != 0)
        calc.add(new Object[] {
            new int[] { ia, ib },
            new double[] { qq } });
    }

    @Override
    double compute(Object[] dataIn) {      
      getPointers(dataIn);
      double qq = dData[0];
      setPairVariables(this);

      energy = qq / rab;

      if (gradients) {
        dE = -qq / (rab * rab);
        addForces(this, 2);
      }
      
      if (logging)
        appendLogData(getDebugLine(CALC_ES, this));
      
      return energy;
    }
  }

*/  
  ///////// REPORTING /////////////
  
  @Override
  String getDebugHeader(int iType) {
    switch (iType){
    case -1:
      return  "Universal Force Field -- " +
          "Rappe, A. K., et. al.; J. Am. Chem. Soc. (1992) 114(25) p. 10024-10035\n";
    default:
      return super.getDebugHeader(iType);
    }
  }

}

