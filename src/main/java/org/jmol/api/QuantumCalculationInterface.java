package org.jmol.api;

import java.util.BitSet;
import java.util.List;

import javax.vecmath.Point3f;


public interface QuantumCalculationInterface {

  public abstract boolean setupCalculation(VolumeDataInterface volumeData, BitSet bsSelected,
                                 BitSet bsExclude,
                                 BitSet[] bsMolecules,
                                 String calculationType, Point3f[] atomCoordAngstroms,
                                 int firstAtomOffset, List<int[]> shells,
                                 float[][] gaussians,
                                 int[][] dfCoefMaps, 
                                 Object slaters, float[] moCoefficients,
                                 float[] linearCombination, float[][] coefs, float[] partialCharges, boolean doNormalize, Point3f[] points, float[] parameters, int testFlags);
  
  public abstract void createCube();
  public abstract float process(Point3f pt);
  
}
