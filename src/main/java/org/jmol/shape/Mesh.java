/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2007-04-24 20:49:07 -0500 (Tue, 24 Apr 2007) $
 * $Revision: 7483 $
 *
 * Copyright (C) 2005  Miguel, The Jmol Development Team
 *
 * Contact: jmol-developers@lists.sf.net, jmol-developers@lists.sf.net
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

package org.jmol.shape;

import java.util.BitSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

//import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Point4f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import org.jmol.api.SymmetryInterface;
import org.jmol.script.Token;
import org.jmol.util.ArrayUtil;
import org.jmol.util.BitSetUtil;
import org.jmol.util.Colix;
import org.jmol.util.Escape;
import org.jmol.util.GData;
import org.jmol.util.Measure;
import org.jmol.util.MeshSurface;
import org.jmol.util.Normix;
import org.jmol.util.Quaternion;

public class Mesh extends MeshSurface {
  
  public final static String PREVIOUS_MESH_ID = "+PREVIOUS_MESH+";

  public String[] title;
  
  public short meshColix;
  public short[] normixes;
  public List<Point3f[]> lineData;
  public String thisID;
  public boolean isValid = true;
  public String scriptCommand;
  public String colorCommand;
  public Point3f lattice;
  public boolean visible = true;
  public int lighting = Token.frontlit;

  public float scale = 1;
  public boolean haveXyPoints;
  public int diameter;
  public float width;
  public Point3f ptCenter = new Point3f(0,0,0);
  public Mesh linkedMesh; //for lcaoOrbitals
  public Map<String, BitSet> vertexColorMap;
  
  public int color;
  public SymmetryInterface unitCell;
  
  public float scale3d = 0;

  public int index;
  public int atomIndex = -1;
  public int modelIndex = -1;  // for Isosurface and Draw
  public int visibilityFlags;
  public boolean insideOut;
  public int checkByteCount;

  public void setVisibilityFlags(int n) {
    visibilityFlags = n;//set to 1 in mps
  }

  public boolean showContourLines = false;
  public boolean showPoints = false;
  public boolean drawTriangles = false;
  public boolean fillTriangles = true;
  public boolean showTriangles = false; //as distinct entitities
  public boolean frontOnly = false;
  public boolean isTwoSided = true;
  public boolean havePlanarContours = false;
  
  /**
   * 
   * @param thisID
   * @param colix
   * @param index
   */
  public Mesh(String thisID, short colix, int index) {
    if (PREVIOUS_MESH_ID.equals(thisID))
      thisID = null;
    this.thisID = thisID;
    this.colix = colix;
    this.index = index;
    //System.out.println("Mesh " + this + " constructed");
  }

  //public void finalize() {
  //  System.out.println("Mesh " + this + " finalized");
  //}
  

  public void clear(String meshType) {
    altVertices = null;
    bsDisplay = null;
    bsSlabDisplay = null;
    bsSlabGhost = null;
    cappingObject = null;
    colix = Colix.GOLD;
    colorDensity = false;
    connections = null;
    diameter = 0;
    drawTriangles = false;
    fillTriangles = true;
    frontOnly = false;
    havePlanarContours = false;
    haveXyPoints = false;
    isTriangleSet = false;
    isTwoSided = false;
    lattice = null;
    mat4 = null;
    normixes = null;
    scale3d = 1;
    polygonIndexes = null;
    scale = 1;
    showContourLines = false;
    showPoints = false;
    showTriangles = false; //as distinct entities
    slabbingObject = null;
    slabOptions = null;
    title = null;
    unitCell = null;
    vertexCount0 = polygonCount0 = vertexCount = polygonCount = 0;
    vertices = null;
    spanningVectors = null;    
    this.meshType = meshType;
  }

  public void initialize(int lighting, Point3f[] vertices, Point4f plane) {
    if (vertices == null)
      vertices = this.vertices;
    Vector3f[] normals = getNormals(vertices, plane);
    normixes = new short[normixCount];
    BitSet bsTemp = new BitSet();
    if (haveXyPoints)
      for (int i = normixCount; --i >= 0;)
        normixes[i] = GData.NORMIX_NULL;
    else
      for (int i = normixCount; --i >= 0;)
        normixes[i] = Normix.getNormix(normals[i], bsTemp);
    this.lighting = Token.frontlit;
    if (insideOut)
      invertNormixes();
    setLighting(lighting);
  }

  public Vector3f[] getNormals(Point3f[] vertices, Point4f plane) {
    normixCount = (isTriangleSet ? polygonCount : vertexCount);
    Vector3f[] normals = new Vector3f[normixCount];
    for (int i = normixCount; --i >= 0;)
      normals[i] = new Vector3f();
    if (plane == null) {
      sumVertexNormals(vertices, normals);
    }else {
      Vector3f normal = new Vector3f(plane.x, plane.y, plane.z); 
      for (int i = normixCount; --i >= 0;)
        normals[i] = normal;
    }
    if (!isTriangleSet)
      for (int i = normixCount; --i >= 0;)
        normals[i].normalize();
    return normals;
  }
  
  public void setLighting(int lighting) {
    isTwoSided = (lighting == Token.fullylit);
    if (lighting == this.lighting)
      return;
    flipLighting(this.lighting);
    flipLighting(this.lighting = lighting);
  }
  
  private void flipLighting(int lighting) {
    if (lighting == Token.fullylit)
      for (int i = normixCount; --i >= 0;)
        normixes[i] = (short)~normixes[i];
    else if ((lighting == Token.frontlit) == insideOut)
      invertNormixes();
  }

  private void invertNormixes() {
    for (int i = normixCount; --i >= 0;)
      normixes[i] = Normix.getInverseNormix(normixes[i]);
  }

  public void setTranslucent(boolean isTranslucent, float iLevel) {
    colix = Colix.getColixTranslucent(colix, isTranslucent, iLevel);
  }

  public final Vector3f vAB = new Vector3f();
  public final Vector3f vAC = new Vector3f();
  public final Vector3f vTemp = new Vector3f();

  //public Vector data1;
  //public Vector data2;
  public List<Object> xmlProperties;
  public boolean colorDensity;
  public Object cappingObject;
  public Object slabbingObject;

  public int[] connections;

  public boolean recalcAltVertices;
  
  protected void sumVertexNormals(Point3f[] vertices, Vector3f[] normals) {
    // subclassed in IsosurfaceMesh
    int adjustment = checkByteCount;
    float min = getMinDistanceForVertexGrouping();
    min *= min;
    for (int i = polygonCount; --i >= 0;) {
      try {
        if (!setABC(i))
          continue;
        Point3f vA = vertices[iA];
        Point3f vB = vertices[iB];
        Point3f vC = vertices[iC];
        // no skinny triangles
        if (vA.distanceSquared(vB) < min || vB.distanceSquared(vC) < min
            || vA.distanceSquared(vC) < min)
          continue;
        Measure.calcNormalizedNormal(vA, vB, vC, vTemp, vAB, vAC);
        if (isTriangleSet) {
          normals[i].set(vTemp);
          continue;
        }
        float l = vTemp.length();
        if (l > 0.9 && l < 1.1) // test for not infinity or -infinity or isNaN
          for (int j = polygonIndexes[i].length - adjustment; --j >= 0;) {
            int k = polygonIndexes[i][j];
            normals[k].add(vTemp);
          }
      } catch (Exception e) {
      }
    }
  }

  protected float getMinDistanceForVertexGrouping() {
    return 0.0001f; // different for an isosurface
  }

  public String getState(String type) {
    //String sxml = null; // problem here is that it can be WAY to large. Shape.getXmlPropertyString(xmlProperties, type);
    StringBuffer s = new StringBuffer();
    //if (sxml != null)
      //s.append("/** XML ** ").append(sxml).append(" ** XML **/\n");
    s.append(type);
    if (!type.equals("mo"))
      s.append(" ID ").append(Escape.escapeStr(thisID));
    if (lattice != null)
      s.append(" lattice ").append(Escape.escapePt(lattice));
    if (meshColix != 0)
      s.append(" color mesh ").append(Colix.getHexCode(meshColix));
    s.append(getRendering());
    if (!visible)
      s.append(" hidden");
    if (bsDisplay != null) {
      s.append(";\n  ").append(type);
      if (!type.equals("mo"))
        s.append(" ID ").append(Escape.escapeStr(thisID));
      s.append(" display " + Escape.escape(bsDisplay));
    }
    return s.toString();
  }

  protected String getRendering() {
    StringBuffer s = new StringBuffer();
    s.append(fillTriangles ? " fill" : " noFill");
    s.append(drawTriangles ? " mesh" : " noMesh");
    s.append(showPoints ? " dots" : " noDots");
    s.append(frontOnly ? " frontOnly" : " notFrontOnly");
    if (showContourLines)
      s.append(" contourlines");
    if (showTriangles)
      s.append(" triangles");
    s.append(" ").append(Token.nameOf(lighting));
    return s.toString();
  }

  public Point3f[] getOffsetVertices(Point4f thePlane) {
    if (altVertices != null && !recalcAltVertices)
      return (Point3f[]) altVertices;
    altVertices = new Point3f[vertexCount];
    for (int i = 0; i < vertexCount; i++)
      altVertices[i] = new Point3f(vertices[i]);
    Vector3f normal = null;
    float val = 0;
    if (scale3d != 0 && vertexValues != null && thePlane != null) {
        normal = new Vector3f(thePlane.x, thePlane.y, thePlane.z);
        normal.normalize();
        normal.scale(scale3d);
        if (mat4 != null) {
          Matrix3f m3 = new Matrix3f();
          mat4.get(m3); 
          m3.transform(normal);
        }
    }
    for (int i = 0; i < vertexCount; i++) {
      if (vertexValues != null && Float.isNaN(val = vertexValues[i]))
        continue;
      if (mat4 != null)
        mat4.transform((Point3f) altVertices[i]);
      Point3f pt = (Point3f) altVertices[i];
      if (normal != null && val != 0)
        pt.scaleAdd(val, normal, pt);
    }
    
    initialize(lighting, (Point3f[]) altVertices, null);
    recalcAltVertices = false;
    return (Point3f[]) altVertices;
  }

  /**
   * 
   * @param showWithinPoints
   * @param showWithinDistance2
   * @param isWithinNot
   */
  public void setShowWithin(List<Point3f> showWithinPoints,
                            float showWithinDistance2, boolean isWithinNot) {
    if (showWithinPoints.size() == 0) {
      bsDisplay = (isWithinNot ? BitSetUtil.newBitSet(0, vertexCount) : null);
      return;
    }
    bsDisplay = new BitSet();
    for (int i = 0; i < vertexCount; i++)
      if (checkWithin(vertices[i], showWithinPoints, showWithinDistance2, isWithinNot))
        bsDisplay.set(i);
  }

  public static boolean checkWithin(Point3f pti, List<Point3f> withinPoints,
                                    float withinDistance2, boolean isWithinNot) {
    if (withinPoints.size() != 0)
      for (int i = withinPoints.size(); --i >= 0;)
        if (pti.distanceSquared(withinPoints.get(i)) <= withinDistance2)
          return !isWithinNot;
    return isWithinNot;
  }

  public int getVertexIndexFromNumber(int vertexIndex) {
    if (--vertexIndex < 0)
      vertexIndex = vertexCount + vertexIndex;
    return (vertexCount <= vertexIndex ? vertexCount - 1
        : vertexIndex < 0 ? 0 : vertexIndex);
  }

  public BitSet getVisibleVertexBitSet() {
    BitSet bs = new BitSet();
    if (polygonCount == 0 && bsSlabDisplay != null)
      BitSetUtil.copy(bsSlabDisplay, bs);
    else
      for (int i = polygonCount; --i >= 0;)
        if (bsSlabDisplay == null || bsSlabDisplay.get(i)) {
          int[] vertexIndexes = polygonIndexes[i];
          if (vertexIndexes == null)
            continue;
          bs.set(vertexIndexes[0]);
          bs.set(vertexIndexes[1]);
          bs.set(vertexIndexes[2]);
        }
    return bs;
  }

  BitSet getVisibleGhostBitSet() {
    BitSet bs = new BitSet();
    if (polygonCount == 0 && bsSlabGhost != null)
      BitSetUtil.copy(bsSlabGhost, bs);
    else
      for (int i = polygonCount; --i >= 0;)
        if (bsSlabGhost == null || bsSlabGhost.get(i)) {
          int[] vertexIndexes = polygonIndexes[i];
          if (vertexIndexes == null)
            continue;
          bs.set(vertexIndexes[0]);
          bs.set(vertexIndexes[1]);
          bs.set(vertexIndexes[2]);
        }
    return bs;
  }

  public void setTokenProperty(int tokProp, boolean bProp) {
    switch (tokProp) {
    case Token.notfrontonly:
    case Token.frontonly:
      frontOnly = (tokProp == Token.frontonly ? bProp : !bProp);
      return;
    case Token.frontlit:
    case Token.backlit:
    case Token.fullylit:
      setLighting(tokProp);
      return;
    case Token.nodots:
    case Token.dots:
      showPoints =  (tokProp == Token.dots ? bProp : !bProp);
      return;
    case Token.nomesh:
    case Token.mesh:
      drawTriangles =  (tokProp == Token.mesh ? bProp : !bProp);
      return;
    case Token.nofill:
    case Token.fill:
      fillTriangles =  (tokProp == Token.fill ? bProp : !bProp);
      return;
    case Token.notriangles:
    case Token.triangles:
      showTriangles =  (tokProp == Token.triangles ? bProp : !bProp);
      return;
    case Token.nocontourlines:
    case Token.contourlines:
      showContourLines =  (tokProp == Token.contourlines ? bProp : !bProp);
      return;
    }
  }
  
  Object getInfo() {
    Hashtable<String, Object> info = new Hashtable<String, Object>();
    info.put("id", thisID);
    info.put("vertexCount", Integer.valueOf(vertexCount));
    info.put("polygonCount", Integer.valueOf(polygonCount));
    info.put("haveQuads", Boolean.valueOf(haveQuads));
    if (vertexCount > 0)
      info.put("vertices", ArrayUtil.setLength(vertices, vertexCount));
    if (vertexValues != null)
      info.put("vertexValues", ArrayUtil.setLength(vertexValues, vertexCount));
    if (polygonCount > 0)
      info.put("polygons", ArrayUtil.setLength(polygonIndexes, polygonCount));
    return info;
  }

  public Point3f[] getBoundingBox() {
    return null;
  }

  public SymmetryInterface getUnitCell() {
    // isosurface only
    return null;
  }

  public void rotateTranslate(Quaternion q, Tuple3f offset, boolean isAbsolute) {
    if (q == null && offset == null) {
      mat4 = null;
      return;
    }
    Matrix3f m3 = new Matrix3f();
    Vector3f v = new Vector3f();
    if (mat4 == null) {
      mat4 = new Matrix4f();
      mat4.setIdentity();
    }
    float f = mat4.get(m3, v);
    if (q == null) {
      if (isAbsolute)
        v.set(offset);
      else
        v.add(offset);
    } else {
      m3.mul(q.getMatrix());
    }
    mat4 = new Matrix4f(m3, v, f);
    recalcAltVertices = true;
  }

}
