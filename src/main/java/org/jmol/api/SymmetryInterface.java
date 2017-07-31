package org.jmol.api;

import java.util.BitSet;
import java.util.Map;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import org.jmol.modelset.Atom;
import org.jmol.util.Quadric;

public interface SymmetryInterface {

  public SymmetryInterface setPointGroup(
                                     SymmetryInterface pointGroupPrevious,
                                     Atom[] atomset, BitSet bsAtoms,
                                     boolean haveVibration,
                                     float distanceTolerance,
                                     float linearTolerance);

  public String getPointGroupName();

  public Object getPointGroupInfo(int modelIndex, boolean asDraw,
                                           boolean asInfo, String type,
                                           int index, float scale);

  public void setSpaceGroup(boolean doNormalize);

  public int addSpaceGroupOperation(String xyz, int opId);

  /**
   * set symmetry lattice type using Hall rotations
   * 
   * @param latt SHELX index or character lattice character P I R F A B C S T or \0
   * 
   */
  public void setLattice(int latt);

  public String getSpaceGroupName();

  public Object getSpaceGroup();

  public void setSpaceGroup(SymmetryInterface symmetry);

  public boolean createSpaceGroup(int desiredSpaceGroupIndex,
                                           String name,
                                           float[] notionalUnitCell);

  public boolean haveSpaceGroup();

  public String getSpaceGroupInfo(String name, SymmetryInterface cellInfo);

  public Object getLatticeDesignation();

  public void setFinalOperations(Point3f[] atoms, int iAtomFirst,
                                          int noSymmetryCount,
                                          boolean doNormalize);

  public int getSpaceGroupOperationCount();

  public Matrix4f getSpaceGroupOperation(int i);

  public String getSpaceGroupXyz(int i, boolean doNormalize);

  public void newSpaceGroupPoint(int i, Point3f atom1, Point3f atom2,
                                          int transX, int transY, int transZ);

  public Vector3f[] rotateEllipsoid(int i, Point3f ptTemp,
                                         Vector3f[] axes, Point3f ptTemp1,
                                         Point3f ptTemp2);

  public void setUnitCellAllFractionalRelative(boolean TF);
  
  public void setUnitCell(float[] notionalUnitCell);

  public void toCartesian(Point3f pt, boolean asAbsolue);

  public Quadric getEllipsoid(float[] parBorU);

  public Point3f ijkToPoint3f(int nnn);

  public void toFractional(Point3f pt, boolean isAbsolute);

  public Point3f[] getUnitCellVertices();

  public Point3f[] getCanonicalCopy(float scale);

  public Point3f getCartesianOffset();

  public float[] getNotionalUnitCell();

  public float[] getUnitCellAsArray(boolean vectorsOnly);

  public void toUnitCell(Point3f pt, Point3f offset);

  public void setUnitCellOffset(Point3f pt);

  public void setOffset(int nnn);

  public Point3f getUnitCellMultiplier();

  public float getUnitCellInfo(int infoType);

  public boolean getCoordinatesAreFractional();

  public int[] getCellRange();

  public String getSymmetryInfoString();

  public String[] getSymmetryOperations();

  public boolean haveUnitCell();

  public String getUnitCellInfo();

  public boolean isPeriodic();

  public void setSymmetryInfo(int modelIndex, Map<String, Object> modelAuxiliaryInfo);

  public Object[] getSymmetryOperationDescription(int iSym,
                                                         SymmetryInterface cellInfo, 
                                                         Point3f pt1, Point3f pt2, String id);

  public boolean isPolymer();

  public boolean isSlab();

  public void addSpaceGroupOperation(Matrix4f mat);

  public void setMinMaxLatticeParameters(Point3i minXYZ, Point3i maxXYZ);

  public void setUnitCellOrientation(Matrix3f matUnitCellOrientation);

  public String getMatrixFromString(String xyz, float[] temp, boolean allowScaling);

  public boolean checkDistance(Point3f f1, Point3f f2, float distance, 
                                        float dx, int iRange, int jRange, int kRange, Point3f ptOffset);

  public Point3f getFractionalOffset();

  public String fcoord(Tuple3f p);

  public void setCartesianOffset(Tuple3f origin);

  public Point3f[] getUnitCellVectors();

  public SymmetryInterface getUnitCell(Tuple3f[] points);

  public Point3f toSupercell(Point3f fpt);

  public boolean isSupercell();

}
