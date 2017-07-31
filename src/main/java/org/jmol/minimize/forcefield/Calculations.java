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

import java.util.BitSet;
import java.util.List;

import javax.vecmath.Vector3d;

import org.jmol.minimize.MinAngle;
import org.jmol.minimize.MinAtom;
import org.jmol.minimize.MinBond;
import org.jmol.minimize.MinTorsion;
import org.jmol.minimize.Util;
import org.jmol.util.ArrayUtil;
import org.jmol.util.TextFormat;

abstract class Calculations {

  final public static double RAD_TO_DEG = (180.0 / Math.PI);
  final public static double DEG_TO_RAD = (Math.PI / 180.0);

  final static double KCAL_TO_KJ = 4.1868;

  final static int CALC_DISTANCE = 0; 
  final static int CALC_ANGLE = 1; 
  final static int CALC_STRETCH_BEND = 2;
  final static int CALC_TORSION = 3;  // first 4 are calculated for constraint energies
  final static int CALC_OOP = 4;
  final static int CALC_VDW = 5;
  final static int CALC_ES = 6;
  final static int CALC_MAX = 7;

  ForceField ff;
  List<Object[]>[] calculations = ArrayUtil.createArrayOfArrayList(CALC_MAX);

  int atomCount;
  int bondCount;
  int angleCount;
  int torsionCount;
  MinAtom[] minAtoms;
  MinBond[] minBonds;
  MinAngle[] minAngles;
  MinTorsion[] minTorsions;
  List<Object[]> constraints;
  boolean isPreliminary;

  public void setConstraints(List<Object[]> constraints) {
    this.constraints = constraints;
  }

  Calculations(ForceField ff, 
      MinAtom[] minAtoms, MinBond[] minBonds,
      MinAngle[] minAngles, MinTorsion[] minTorsions, 
      List<Object[]> constraints) {
    this.ff = ff;
    this.minAtoms = minAtoms;
    this.minBonds = minBonds;
    this.minAngles = minAngles;
    this.minTorsions = minTorsions;
    atomCount = minAtoms.length;
    bondCount = minBonds.length;
    angleCount = minAngles.length;
    torsionCount = minTorsions.length;
    this.constraints = constraints;
  }

  abstract boolean setupCalculations();

  abstract String getUnits();

  abstract double compute(int iType, Object[] dataIn);

  void addForce(Vector3d v, int i, double dE) {
    minAtoms[i].force[0] += v.x * dE;
    minAtoms[i].force[1] += v.y * dE;
    minAtoms[i].force[2] += v.z * dE;
  }

  boolean gradients;

  boolean silent;

  public void setSilent(boolean TF) {
    silent = TF;
  }

  StringBuffer logData = new StringBuffer();

  public String getLogData() {
    return logData.toString();
  }

  void appendLogData(String s) {
    logData.append(s).append("\n");
  }

  boolean logging;
  boolean loggingEnabled;

  void setLoggingEnabled(boolean TF) {
    loggingEnabled = TF;
    if (loggingEnabled)
      logData = new StringBuffer();
  }

  void setPreliminary(boolean TF) {
    isPreliminary = TF;
  }

  abstract class PairCalc extends Calculation {
    
    abstract void setData(List<Object[]> calc, int ia, int ib);

  }
  
  protected void pairSearch(List<Object[]> calc1, PairCalc pc1, 
                            List<Object[]> calc2, PairCalc pc2) {
    for (int i = 0; i < atomCount - 1; i++) {
      BitSet bsVdw = minAtoms[i].bsVdw;
      for (int j = bsVdw.nextSetBit(0); j >= 0; j = bsVdw.nextSetBit(j + 1)) {
        pc1.setData(calc1, i, j);
        if (pc2 != null)
          pc2.setData(calc2, i, j);
      }
    }
  }

  private double calc(int iType, boolean gradients) {
    logging = loggingEnabled && !silent;
    this.gradients = gradients;
    List<Object[]> calc = calculations[iType];
    int nCalc;
    double energy = 0;
    if (calc == null || (nCalc = calc.size()) == 0)
      return 0;
    if (logging)
      appendLogData(getDebugHeader(iType));
    for (int ii = 0; ii < nCalc; ii++)
      energy += compute(iType, calculations[iType].get(ii));
    if (logging)
      appendLogData(getDebugFooter(iType, energy));
    if (constraints != null && iType <= CALC_TORSION)
      energy += constraintEnergy(iType);
    return energy;
  }

  double energyStrBnd(@SuppressWarnings("unused") boolean gradients) {
    return 0.0f;
  }

  double energyBond(boolean gradients) {
    return calc(CALC_DISTANCE, gradients);
  }

  double energyAngle(boolean gradients) {
    return calc(CALC_ANGLE, gradients);
  }

  double energyTorsion(boolean gradients) {
    return calc(CALC_TORSION, gradients);
  }

  double energyStretchBend(boolean gradients) {
    return calc(CALC_STRETCH_BEND, gradients);
  }

  double energyOOP(boolean gradients) {
    return calc(CALC_OOP, gradients);
  }

  double energyVDW(boolean gradients) {
    return calc(CALC_VDW, gradients);
  }

  double energyES(boolean gradients) {
    return calc(CALC_ES, gradients);
  }

  final Vector3d da = new Vector3d();
  final Vector3d db = new Vector3d();
  final Vector3d dc = new Vector3d();
  final Vector3d dd = new Vector3d();
  int ia, ib, ic, id;

  final Vector3d v1 = new Vector3d();
  final Vector3d v2 = new Vector3d();
  final Vector3d v3 = new Vector3d();

  private final static double PI_OVER_2 = Math.PI / 2;
  private final static double TWO_PI = Math.PI * 2;

  private double constraintEnergy(int iType) {

    double value = 0;
    double k = 0;
    double energy = 0;

    for (int i = constraints.size(); --i >= 0;) {
      Object[] c = constraints.get(i);
      int nAtoms = ((int[]) c[0])[0];
      if (nAtoms != iType + 2)
        continue;
      int[] minList = (int[]) c[1];
      double targetValue = ((Float) c[2]).doubleValue();

      switch (iType) {
      case CALC_TORSION:
        id = minList[3];
        if (gradients)
          dd.set(minAtoms[id].coord);
        //$FALL-THROUGH$
      case CALC_ANGLE:
        ic = minList[2];
        if (gradients)
          dc.set(minAtoms[ic].coord);
        //$FALL-THROUGH$
      case CALC_DISTANCE:
        ib = minList[1];
        ia = minList[0];
        if (gradients) {
          db.set(minAtoms[ib].coord);
          da.set(minAtoms[ia].coord);
        }
      }

      k = 10000.0;

      switch (iType) {
      case CALC_TORSION:
        targetValue *= DEG_TO_RAD;
        value = (gradients ? Util.restorativeForceAndTorsionAngleRadians(da,
            db, dc, dd) : Util.getTorsionAngleRadians(minAtoms[ia].coord,
            minAtoms[ib].coord, minAtoms[ic].coord, minAtoms[id].coord, v1, v2, v3));
        if (value < 0 && targetValue >= PI_OVER_2)
          value += TWO_PI;
        else if (value > 0 && targetValue <= -PI_OVER_2)
          targetValue += TWO_PI;
        break;
      case CALC_ANGLE:
        targetValue *= DEG_TO_RAD;
        value = (gradients ? Util.restorativeForceAndAngleRadians(da, db, dc)
            : Util.getAngleRadiansABC(minAtoms[ia].coord, minAtoms[ib].coord,
                minAtoms[ic].coord));
        break;
      case CALC_DISTANCE:
        value = (gradients ? Util.restorativeForceAndDistance(da, db, dc)
            : Math.sqrt(Util.distance2(minAtoms[ia].coord, minAtoms[ib].coord)));
        break;
      }
      energy += constrainQuadratic(value, targetValue, k, iType);
    }
    return energy;
  }

  private double constrainQuadratic(double value, double targetValue, double k,
                                    int iType) {

    if (!Util.isFinite(value))
      return 0;

    double delta = value - targetValue;

    if (gradients) {
      double dE = 2.0 * k * delta;
      switch (iType) {
      case CALC_TORSION:
        addForce(dd, id, dE);
        //$FALL-THROUGH$
      case CALC_ANGLE:
        addForce(dc, ic, dE);
        //$FALL-THROUGH$
      case CALC_DISTANCE:
        addForce(db, ib, dE);
        addForce(da, ia, dE);
      }
    }
    return k * delta * delta;
  }

  void getConstraintList() {
    if (constraints == null || constraints.size() == 0)
      return;
    appendLogData("C O N S T R A I N T S\n---------------------");
    for (int i = constraints.size(); --i >= 0;) {
      Object[] c = constraints.get(i);
      int[] indexes = (int[]) c[0];
      int[] minList = (int[]) c[1];
      double targetValue = ((Float) c[2]).doubleValue();
      int iType = indexes[0] - 2;
      switch (iType) {
      case CALC_TORSION:
        id = minList[3];
        //$FALL-THROUGH$
      case CALC_ANGLE:
        ic = minList[2];
        //$FALL-THROUGH$
      case CALC_DISTANCE:
        ib = minList[1];
        ia = minList[0];
      }
      switch (iType) {
      case CALC_DISTANCE:
        appendLogData(TextFormat.sprintf("%3d %3d  %-5s %-5s  %12.6f",
            new Object[] {
                minAtoms[ia].atom.getAtomName(),
                minAtoms[ib].atom.getAtomName(),
                new float[] { (float) targetValue },
                new int[] { minAtoms[ia].atom.getAtomNumber(),
                    minAtoms[ib].atom.getAtomNumber(), } }));
        break;
      case CALC_ANGLE:
        appendLogData(TextFormat.sprintf("%3d %3d %3d  %-5s %-5s %-5s  %12.6f",
            new Object[] {
                minAtoms[ia].atom.getAtomName(),
                minAtoms[ib].atom.getAtomName(),
                minAtoms[ic].atom.getAtomName(),
                new float[] { (float) targetValue },
                new int[] { minAtoms[ia].atom.getAtomNumber(),
                    minAtoms[ib].atom.getAtomNumber(),
                    minAtoms[ic].atom.getAtomNumber(), } }));
        break;
      case CALC_TORSION:
        appendLogData(TextFormat
            .sprintf(
                "%3d %3d %3d %3d  %-5s %-5s %-5s %-5s  %3d %8.3f     %8.3f     %8.3f     %8.3f",
                new Object[] {
                    minAtoms[ia].atom.getAtomName(),
                    minAtoms[ib].atom.getAtomName(),
                    minAtoms[ic].atom.getAtomName(),
                    minAtoms[id].atom.getAtomName(),
                    new float[] { (float) targetValue },
                    new int[] { minAtoms[ia].atom.getAtomNumber(),
                        minAtoms[ib].atom.getAtomNumber(),
                        minAtoms[ic].atom.getAtomNumber(),
                        minAtoms[id].atom.getAtomNumber() } }));
        break;
      }
    }
    appendLogData("---------------------\n");
  }

  String getAtomList(String title) {
    String trailer =
          "----------------------------------------"
          + "-------------------------------------------------------\n";  
    StringBuffer sb = new StringBuffer();
    sb.append("\n" + title + "\n\n"
        + " ATOM    X        Y        Z    TYPE     GRADX    GRADY    GRADZ  "
        + "---------BONDED ATOMS--------\n"
        + trailer);
    for (int i = 0; i < atomCount; i++) {
      MinAtom atom = minAtoms[i];
      int[] others = atom.getBondedAtomIndexes();
      int[] iVal = new int[others.length + 1];
      iVal[0] = atom.atom.getAtomNumber();
      String s = "   ";
      for (int j = 0; j < others.length; j++) {
        s += " %3d";
        iVal[j + 1] = minAtoms[others[j]].atom.getAtomNumber();
      }
      sb.append(TextFormat.sprintf("%3d %8.3f %8.3f %8.3f  %-5s %8.3f %8.3f %8.3f" + s + "\n", 
          new Object[] { atom.sType,
          new float[] { (float) atom.coord[0], (float) atom.coord[1],
            (float) atom.coord[2], (float) atom.force[0], (float) atom.force[1],
            (float) atom.force[2] }, iVal}));
    }
    sb.append(trailer + "\n\n");
    return sb.toString();
  }

  String getDebugHeader(int iType) {
    switch (iType){
    case -1:
      //Override to give reference
      break;
    case CALC_DISTANCE:
      return
           "\nB O N D   S T R E T C H I N G (" + bondCount + " bonds)\n\n"
          +"  ATOMS  ATOM TYPES   BOND    BOND       IDEAL      FORCE\n"
          +"  I   J   I     J     TYPE   LENGTH     LENGTH    CONSTANT      DELTA     ENERGY\n"
          +"--------------------------------------------------------------------------------";
    case CALC_ANGLE:
      return 
           "\nA N G L E   B E N D I N G (" + minAngles.length + " angles)\n\n"
          +"    ATOMS      ATOM TYPES        VALENCE    IDEAL        FORCE\n"
          +"  I   J   K   I     J     K       ANGLE     ANGLE      CONSTANT     ENERGY\n"
          +"--------------------------------------------------------------------------";
    case CALC_STRETCH_BEND:
      return 
           "\nS T R E T C H   B E N D I N G (" + (minAngles.length * 2) + " angles)\n\n"
          +"    ATOMS      ATOM TYPES        VALENCE    IDEAL        FORCE\n"
          +"  I   J   K   I     J     K       ANGLE     ANGLE      CONSTANT     ENERGY\n"
          +"--------------------------------------------------------------------------";
    case CALC_TORSION:
      return 
           "\nT O R S I O N A L (" + minTorsions.length + " torsions)\n\n"
          +"      ATOMS           ATOM TYPES            n    COS          FORCE      TORSION\n"
          +"  I   J   K   L   I     J     K     L          (n phi0)      CONSTANT     ANGLE        ENERGY\n"
          +"---------------------------------------------------------------------------------------------";
    case CALC_OOP:
      return 
           "\nO U T - O F - P L A N E   B E N D I N G\n\n"
          +"      ATOMS           ATOM TYPES             OOP        FORCE \n"
          +"  I   J   K   L   I     J     K     L       ANGLE     CONSTANT      ENERGY\n"
          +"--------------------------------------------------------------------------";
    case CALC_VDW:
      return 
           "\nV A N   D E R   W A A L S  (partial list)\n\n"
          +"  ATOMS  ATOM TYPES\n"
          +"  I   J   I     J      Rij       kij     ENERGY\n"
          +"-----------------------------------------------";
    case CALC_ES:
      return 
          "\nE L E C T R O S T A T I C   I N T E R A C T I O N S  (partial list)\n\n"
          +"  ATOMS  ATOM TYPES \n"
          +"  I   J   I     J      Rij      f          Qi          Qj    ENERGY\n"
          +"-------------------------------------------------------------------";
    }
    return "";
  }

  String getDebugLine(int iType, Calculation c) {
    float energy = ff.toUserUnits(c.energy);
    switch (iType) {
    case CALC_DISTANCE:
      return TextFormat.sprintf(
          "%3d %3d  %-5s %-5s  %4.2f%8.3f   %8.3f     %8.3f   %8.3f   %8.3f",
          new Object[] { minAtoms[c.ia].sType, minAtoms[c.ib].sType, 
          new float[] { 0, (float)c.rab, 
              (float)c.dData[1], (float)c.dData[0], 
              (float)c.delta, energy },
          new int[] { minAtoms[c.ia].atom.getAtomNumber(), minAtoms[c.ib].atom.getAtomNumber() }});
    case CALC_ANGLE:
    case CALC_STRETCH_BEND:
      return TextFormat.sprintf(
          "%3d %3d %3d  %-5s %-5s %-5s  %8.3f  %8.3f     %8.3f   %8.3f", 
          new Object[] { minAtoms[c.ia].sType, minAtoms[c.ib].sType, 
              minAtoms[c.ic].sType,
          new float[] { (float)(c.theta * RAD_TO_DEG), (float) c.dData[1] /*THETA0*/, 
              (float)c.dData[0]/*Kijk*/, energy },
          new int[] { minAtoms[c.ia].atom.getAtomNumber(), minAtoms[c.ib].atom.getAtomNumber(),
              minAtoms[c.ic].atom.getAtomNumber()} });
      case CALC_TORSION:
        return TextFormat.sprintf(
           "%3d %3d %3d %3d  %-5s %-5s %-5s %-5s  %3d %8.3f     %8.3f     %8.3f     %8.3f", 
           new Object[] { minAtoms[c.ia].sType, minAtoms[c.ib].sType, 
               minAtoms[c.ic].sType, minAtoms[c.id].sType, 
           new float[] { (float) c.dData[1]/*cosNphi0*/, (float) c.dData[0]/*V*/, 
               (float) (c.theta * RAD_TO_DEG), energy },
           new int[] { minAtoms[c.ia].atom.getAtomNumber(), minAtoms[c.ib].atom.getAtomNumber(),
               minAtoms[c.ic].atom.getAtomNumber(), minAtoms[c.id].atom.getAtomNumber(), c.iData[4] } });
    case CALC_OOP:
      return TextFormat.sprintf("" +
          "%3d %3d %3d %3d  %-5s %-5s %-5s %-5s  %8.3f   %8.3f     %8.3f",
          new Object[] { minAtoms[c.ia].sType, minAtoms[c.ib].sType, 
              minAtoms[c.ic].sType, minAtoms[c.id].sType,
          new float[] { (float)(c.theta * RAD_TO_DEG), 
              (float)c.dData[0]/*koop*/, energy },
          new int[] { minAtoms[c.ia].atom.getAtomNumber(), minAtoms[c.ib].atom.getAtomNumber(),
              minAtoms[c.ic].atom.getAtomNumber(), minAtoms[c.id].atom.getAtomNumber() } });
    case CALC_VDW:
      return TextFormat.sprintf("%3d %3d  %-5s %-5s %6.3f  %8.3f  %8.3f", 
          new Object[] { minAtoms[c.iData[0]].sType, minAtoms[c.iData[1]].sType,
          new float[] { (float)c.rab, (float)c.dData[0]/*kab*/, energy},
          new int[] { minAtoms[c.ia].atom.getAtomNumber(), minAtoms[c.ib].atom.getAtomNumber() } });
    case CALC_ES:
      return TextFormat.sprintf("%3d %3d  %-5s %-5s %6.3f  %8.3f  %8.3f  %8.3f  %8.3f", 
          new Object[] { minAtoms[c.iData[0]].sType, minAtoms[c.iData[1]].sType,
          new float[] { (float)c.rab, (float)c.dData[0]/*q1*/, (float)c.dData[1]/*q2*/, (float)c.dData[2]/*f*/, energy },
          new int[] { minAtoms[c.ia].atom.getAtomNumber(), minAtoms[c.ib].atom.getAtomNumber() } });
    }
    return "";
  }

  String getDebugFooter(int iType, double energy) {
    String s = "";
    switch (iType){
    case CALC_DISTANCE:
      s = "BOND STRETCHING";
      break;
    case CALC_ANGLE:
      s = "ANGLE BENDING";
      break;
    case CALC_TORSION:
      s = "TORSIONAL";
      break;
    case CALC_OOP:
      s = "OUT-OF-PLANE BENDING";
      break;
    case CALC_STRETCH_BEND:
      s = "STRETCH BENDING";
      break;
    case CALC_VDW:
      s = "VAN DER WAALS";
      break;
    case CALC_ES:
      s = "ELECTROSTATIC ENERGY";
      break;
    }
    return TextFormat.sprintf("\n     TOTAL %s ENERGY = %8.3f %s/mol\n", 
        new Object[] { s, ff.minimizer.units, Float.valueOf(ff.toUserUnits(energy)), 
        ff.minimizer.units });
  }

  ////////////////////////////////////////////////////
  
  void setPairVariables(Calculation c) {
    if (gradients) {
      setCoords(c, 2);
      c.rab = Util.restorativeForceAndDistance(da, db, dc);
    } else {
      c.rab = Math.sqrt(Util.distance2(minAtoms[c.ia].coord, minAtoms[c.ib].coord));
    }
    if (Util.isNearZero(c.rab, 1.0e-3))
      c.rab = 1.0e-3;
  }
  
  void setAngleVariables(Calculation c) {
    if (gradients) {
      setCoords(c, 3);
      c.theta = Util.restorativeForceAndAngleRadians(da, db, dc);
    } else {
      c.theta = Util.getAngleRadiansABC(minAtoms[c.ia].coord, minAtoms[c.ib].coord, minAtoms[c.ic].coord);
    }
    if (!Util.isFinite(c.theta))
      c.theta = 0.0;
  }

  void setOopVariables(Calculation c, boolean fixTheta) {
    setCoords(c, 4);
    if (gradients) {
      c.theta = Util.restorativeForceAndOutOfPlaneAngleRadians(da, db, dc, dd, v1, v2, v3);
    } else {
      c.theta = Util.pointPlaneAngleRadians(da, db, dc, dd, v1, v2, v3, fixTheta);
    }
    if (!Util.isFinite(c.theta))
      c.theta = 0.0;
  }
  
  void setTorsionVariables(Calculation c) {
    if (gradients) {
      setCoords(c, 4);
      c.theta = Util.restorativeForceAndTorsionAngleRadians(da, db, dc, dd);
      if (!Util.isFinite(c.theta))
        c.theta = 0.001 * DEG_TO_RAD;
    } else {
      c.theta = Util.getTorsionAngleRadians(minAtoms[c.ia].coord, minAtoms[c.ib].coord, 
          minAtoms[c.ic].coord, minAtoms[c.id].coord, v1, v2, v3);
    }
  }
  
  void setCoords(Calculation c, int n) {
    switch(n) {
    case 4:
      da.set(minAtoms[c.ia].coord);
      //$FALL-THROUGH$
    case 3:
      db.set(minAtoms[c.ib].coord);
      //$FALL-THROUGH$
    case 2:
      dc.set(minAtoms[c.ic].coord);
      //$FALL-THROUGH$
    case 1:
      dd.set(minAtoms[c.id].coord);
    }
  }

  void addForces(Calculation c, int n) {
    switch (n) {
    case 4:
      addForce(dd, c.id, c.dE);
      //$FALL-THROUGH$
    case 3:
      addForce(dc, c.ic, c.dE);
      //$FALL-THROUGH$
    case 2:
      addForce(db, c.ib, c.dE);
      //$FALL-THROUGH$
    case 1:
      addForce(da, c.ia, c.dE);
    }
  }

}
