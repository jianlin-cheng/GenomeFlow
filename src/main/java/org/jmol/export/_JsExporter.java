/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2007-05-18 15:41:42 -0500 (Fri, 18 May 2007) $
 * $Revision: 7752 $

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
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jmol.export;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import org.jmol.util.Escape;
import org.jmol.util.GData;
import org.jmol.util.JmolFont;
import org.jmol.util.Quaternion;

public class _JsExporter extends __CartesianExporter {

  /*
   * A very very preliminary exporter just used to check feasibility
   * of delivering JavaScript code for server-side creation of surfaces.
   * It is expected that this will be removed at some point.
   * 
   * Bob Hanson hansonr@stolaf.edu Aug 2012
   * 
   */

  public _JsExporter() {
    useTable = new UseTable("USE: ");
  }
  
  private UseTable useTable;

  @Override
  protected void output(Tuple3f pt) {
    output(getTriad(pt));
  }
  
  @Override
  protected String getTriad(Tuple3f pt) {
    return "["+round(pt.x) + "," + round(pt.y) + "," + round(pt.z) + "]";
  }
  
  private int iChild;
  
  @Override
  protected void outputHeader() {
    output("function() {var UseTable={};var addUse=function(def,value){UseTable[def]=value;return value};\nreturn {");
    output("Background: " + rgbFractionalFromColix(backgroundColix) + ",");
    // next is an approximation only
    float angle = (float) (aperatureAngle * Math.PI / 180);
    viewer.getAxisAngle(viewpoint);
    output("fieldOfView: " + angle 
        + ", position: " + getTriad(cameraPosition) 
        + ", orientation: [" + viewpoint.x + ", " + viewpoint.y + ", " + (viewpoint.angle == 0 ? 1 : viewpoint.z) + ", " + -viewpoint.angle + "]\n");
//    output(getJmolPerspective());
    output(",\nchildren: [0 \n");
  }

  @Override
  protected void outputFooter() {
    useTable = null;
    output("\n] // children\n} // return\n} // function\n();\n");
  }

  private void outputAppearance(short colix, boolean isText) {
    String color = rgbFractionalFromColix(colix);
    output(", appearance: {c:" + color);
    if (!isText)
      output(", t: " + translucencyFractionalFromColix(colix) + "}");
  }
  
  @Override
  protected void outputCircle(Point3f pt1, Point3f pt2, float radius, short colix,
                            boolean doFill) {
    if (true)
      return;
    if (doFill) {
      // draw filled circle

      output("Transform{translation: ");
      tempV1.set(pt1);
      tempV1.add(pt2);
      tempV1.scale(0.5f);
      output(tempV1);
      output(", children: {type:'circle', rotation:[ 1, 0, 0, 1.5708],");
      outputCylinderChild(pt1, pt2, colix, GData.ENDCAPS_FLAT,
          (int) (radius * 2000));
      output("}}}\n");
      return;
    }

    // draw a thin torus

    outputTransRot(pt1, pt2, 0, 0, 1);
    tempP3.set(1, 1, 1);
    tempP3.scale(radius);
    output(", scale: ");
    output(tempP3);
    output(", type:'torus'");
    outputAppearance(colix, false);
    output("}}}");
    output("}\n");
  }

  @Override
  protected void outputCone(Point3f ptBase, Point3f ptTip, float radius,
                            short colix) {
    if (true)
      return;
    float height = tempP1.distance(tempP2);
    outputTransRot(tempP1, tempP2, 0, 1, 0);
    output(", child" + (++iChild) + ": {");
    output("type:'cone', height: " + round(height) + ", bottomRadius: "
        + round(radius));
    outputAppearance(colix, false);
    output("}\n");
  }

  @Override
  protected boolean outputCylinder(Point3f ptCenter, Point3f pt1, Point3f pt2,
                                   short colix, byte endcaps, float radius,
                                   Point3f ptX, Point3f ptY, boolean checkRadius) {
    if (true)
      return false;
    if (ptX == null) {
      outputTransRot(pt1, pt2, 0, 1, 0);
    } else {
      output("Transform{translation ");
      output(ptCenter);
      outputQuaternionFrame(ptCenter, ptY, pt1, ptX, 2, " ", "");
      pt1.set(0, 0, -1);
      pt2.set(0, 0, 1);
    }
    outputCylinderChild(pt1, pt2, colix, endcaps, radius);
    output("}\n");
    if (endcaps == GData.ENDCAPS_SPHERICAL) {
      outputSphere(pt1, radius * 1.01f, colix, checkRadius);
      outputSphere(pt2, radius * 1.01f, colix, checkRadius);
    }
    return true;
  }

  private void outputCylinderChild(Point3f pt1, Point3f pt2, short colix,
                                   byte endcaps, float radius) {
    output(" children ");    
    float length = pt1.distance(pt2);
    String child = useTable.getDef("C" + colix + "_" + (int) (length * 100) + "_" + radius
        + "_" + endcaps);
    if (child.charAt(0) == '_') {
      output("DEF " + child);
      output(" Shape{geometry ");
      String cyl = useTable.getDef("c" + round(length) + "_" + endcaps + "_" + radius);
      if (cyl.charAt(0) == '_') {
        output("DEF " + cyl + " Cylinder{height " 
            + round(length) + " radius " + radius 
            + (endcaps == GData.ENDCAPS_FLAT ? "" : " top FALSE bottom FALSE") + "}");
      } else {
        output(cyl);
      }
      outputAppearance(colix, false);
      output("}");
    } else {
      output(child);
    }
  }

  @Override
  protected void outputEllipsoid(Point3f ptCenter, Point3f[] points, short colix) {
    if (true)
      return;
    output("Transform{translation ");
    output(ptCenter);

    // Hey, hey -- quaternions to the rescue!
    // Just send three points to Quaternion to define a plane and return
    // the AxisAngle required to rotate to that position. That's all there is to
    // it.

    outputQuaternionFrame(ptCenter, points[1], points[3], points[5], 1, " ", "");
    output(" children ");
    tempP3.set(0, 0, 0);
    outputSphereChild(tempP3, 1.0f, colix);
    output("}\n");
  }

  private Point3f tempQ1 = new Point3f();
  private Point3f tempQ2 = new Point3f();

  private void outputQuaternionFrame(Point3f ptCenter, Point3f ptX,
                                       Point3f ptY, Point3f ptZ, float yScale,
                                       String pre, String post) {

    //Hey, hey -- quaternions to the rescue!
    // Just send three points to Quaternion to define a plane and return
    // the AxisAngle required to rotate to that position. That's all there is to it.

    tempQ1.set(ptX);
    tempQ2.set(ptY);
    AxisAngle4f a = Quaternion.getQuaternionFrame(ptCenter, tempQ1, tempQ2)
        .toAxisAngle4f();
    if (!Float.isNaN(a.x)) {
      output(" rotation");
      output(pre);
      output(a.x + " " + a.y + " " + a.z + " " + a.angle);
      output(post);
    }
    float sx = ptX.distance(ptCenter);
    float sy = ptY.distance(ptCenter) * yScale;
    float sz = ptZ.distance(ptCenter);
    output(" scale");
    output(pre);
    output(sx + " " + sy + " " + sz);
    output(post);
  }

  @Override
  protected void outputSurface(Point3f[] vertices, Vector3f[] normals,
                               short[] colixes, int[][] indices,
                               short[] polygonColixes,
                               int nVertices, int nPolygons, int nFaces, BitSet bsPolygons,
                               int faceVertexMax, short colix,
                               List<Short> colorList, Map<Short, Integer> htColixes, Point3f offset) {
    output("\n, child" + (++iChild) + ": {type:'surface'\n");
    outputAppearance(colix, false);
    if (polygonColixes != null)
      output(", colorPerVertex:false\n");

    // coordinates

    output("\n, coordinates: [\n");
    outputVertices(vertices, nVertices, offset);
    output("   ]\n");
    output(", coordIndices [\n");
    int[] map = new int[nVertices];
    getCoordinateMap(vertices, map, null);
    outputIndices(indices, map, nPolygons, bsPolygons, faceVertexMax);
    output("  ] //coordinates\n");

    // normals

    if (normals != null) {
      List<String> vNormals = new ArrayList<String>();
      map = getNormalMap(normals, nVertices, null, vNormals);
      output(", normalPerVertex:true\n, normals: [\n");
      outputNormals(vNormals);
      output("  ] // normals\n");
      output(", normalIndices: [\n");
      outputIndices(indices, map, nPolygons, bsPolygons, faceVertexMax);
      output("  ] // normalIndices\n");
    }

    map = null;
    
    // colors

    if (colorList != null) {
      output(", colors: [\n");
      outputColors(colorList);
      output("  ] // colors\n");
      output(", colorIndices: [\n");
      outputColorIndices(indices, nPolygons, bsPolygons, faceVertexMax, htColixes, colixes, polygonColixes);
      output("  ] // colorIndices\n");
    }
    output(" } // surface\n");
  }

  @Override
  protected void outputVertices(Point3f[] vertices, int nVertices, Point3f offset) {
    for (int i = 0; i < nVertices; i++) {
      if (Float.isNaN(vertices[i].x))
        continue;
      if (i > 0)
        output(",");
      outputVertex(vertices[i], offset);
      output("\n");
    }
  }

  @Override
  protected void outputIndices(int[][] indices, int[] map, int nPolygons,
                               BitSet bsPolygons, int faceVertexMax) {
    boolean isAll = (bsPolygons == null);
    int i0 = (isAll ? nPolygons - 1 : bsPolygons.nextSetBit(0));
    boolean isFirst = true;
    for (int i = i0; i >= 0; i = (isAll ? i - 1 : bsPolygons.nextSetBit(i + 1))) {
      if (isFirst)
        isFirst = false;
      else
        output(",");
      outputFace(indices[i], map, faceVertexMax);
    }
  }

  @Override
  protected void outputFace(int[] face, int[] map, int faceVertexMax) {
    output("[" + map[face[0]] + "," + map[face[1]] + "," + map[face[2]] + "]\n");
    if (faceVertexMax == 4 && face.length == 4)
      output("[" + map[face[0]] + "," + map[face[2]] + "," + map[face[3]] + "]\n");
  }

  private void outputNormals(List<String> vNormals) {
    int n = vNormals.size();
    for (int i = 0; i < n; i++) {
      if (i > 0)
        output(",");
      output(vNormals.get(i));
    }
  }

  private void outputColors(List<Short> colorList) {
    int nColors = colorList.size();
    for (int i = 0; i < nColors; i++) {
      String color = rgbFractionalFromColix(colorList.get(i).shortValue());
      if (i > 0)
        output(",");
      output(color);
      output("\n");
    }
  }

  private void outputColorIndices(int[][] indices, int nPolygons, BitSet bsPolygons,
                                  int faceVertexMax, Map<Short, Integer> htColixes,
                                  short[] colixes, short[] polygonColixes) {
    boolean isAll = (bsPolygons == null);
    int i0 = (isAll ? nPolygons - 1 : bsPolygons.nextSetBit(0));
    for (int i = i0; i >= 0; i = (isAll ? i - 1 : bsPolygons.nextSetBit(i + 1))) {
      if (i != i0)
        output(",");
      if (polygonColixes == null) {
        output("[" + htColixes.get(Short.valueOf(colixes[indices[i][0]])) + ","
            + htColixes.get(Short.valueOf(colixes[indices[i][1]])) + ","
            + htColixes.get(Short.valueOf(colixes[indices[i][2]])) + "]\n");
        if (faceVertexMax == 4 && indices[i].length == 4)
          output("[" + htColixes.get(Short.valueOf(colixes[indices[i][0]])) + ","
              + htColixes.get(Short.valueOf(colixes[indices[i][2]])) + ","
              + htColixes.get(Short.valueOf(colixes[indices[i][3]])) + "]\n");
      } else {
        output(htColixes.get(Short.valueOf(polygonColixes[i])) + "\n");
      }
    }
  }

  private Map<String, Boolean> htSpheresRendered = new Hashtable<String, Boolean>();

  @Override
  protected void outputSphere(Point3f ptCenter, float radius, short colix, boolean checkRadius) {
    if (true)
      return;
    String check = round(ptCenter) + (checkRadius ? " " + (int) (radius * 100) : "");
    if (htSpheresRendered.get(check) != null)
      return;
    htSpheresRendered.put(check, Boolean.TRUE);
    outputSphereChild(ptCenter, radius, colix);
  }

  private void outputSphereChild(Point3f ptCenter, float radius, short colix) {
    int iRad = (int) (radius * 100);
    String child = useTable.getDef("S" + colix + "_" + iRad);
    output("Transform{translation ");
    output(ptCenter);
    output(" children ");
    if (child.charAt(0) == '_') {
      output("DEF " + child);
      output(" Shape{geometry Sphere{radius " + radius + "}");
      outputAppearance(colix, false);
      output("}");
    } else {
      output(child);
    }
    output("}\n");
  }

  @Override
  protected void outputTextPixel(Point3f pt, int argb) {
    if (true)
      return;
    String color = rgbFractionalFromArgb(argb);
    output("Transform{translation ");
    output(pt);
    output(" children ");
    String child = useTable.getDef("p" + argb);
    if (child.charAt(0) == '_') {
      output("DEF " + child + " Shape{geometry Sphere{radius 0.01}");
      output(" appearance Appearance{material Material{diffuseColor 0 0 0 specularColor 0 0 0 ambientIntensity 0.0 shininess 0.0 emissiveColor "
          + color + " }}}");
    } else {
      output(child);
    }
    output("}\n");
  }

  private void outputTransRot(Point3f pt1, Point3f pt2, int x, int y, int z) {    
    output("Transform{");
    outputTransRot(pt1, pt2, x, y, z, " ", "");
  }
  
  private void outputTransRot(Point3f pt1, Point3f pt2, int x, int y, int z,
                                String pre, String post) {
    tempV1.set(pt2);
    tempV1.add(pt1);
    tempV1.scale(0.5f);
    output("translation");
    output(pre);
    output(tempV1);
    output(post);
    tempV1.sub(pt1);
    tempV1.normalize();
    tempV2.set(x, y, z);
    tempV2.add(tempV1);
    tempA.set(tempV2.x, tempV2.y, tempV2.z, 3.14159f);
    output(" rotation");
    output(pre);
    output(round(tempA.x) + " " + round(tempA.y) + " " + round(tempA.z) + " "
        + round(tempA.angle));
    output(post);
  }

  @Override
  protected void outputTriangle(Point3f pt1, Point3f pt2, Point3f pt3, short colix) {
    if (true)
      return;
    // nucleic base
    // cartoons
    output("Shape{geometry IndexedFaceSet{solid FALSE coord Coordinate{point[");
    output(pt1);
    output(" ");
    output(pt2);
    output(" ");
    output(pt3);
    output("]}coordIndex[ 0 1 2 -1 ]}");
    outputAppearance(colix, false);
    output("}\n");
  }

  @Override
  void plotText(int x, int y, int z, short colix, String text, JmolFont font3d) {
    if (true)
      return;
    if (z < 3)
      z = viewer.getFrontPlane();
    String useFontStyle = font3d.fontStyle.toUpperCase();
    String preFontFace = font3d.fontFace.toUpperCase();
    String useFontFace = (preFontFace.equals("MONOSPACED") ? "TYPEWRITER"
        : preFontFace.equals("SERIF") ? "SERIF" : "SANS");
    output("Transform{translation ");
    tempP3.set(x, y, z);
    viewer.unTransformPoint(tempP3, tempP1);
    output(tempP1);
    // These x y z are 3D coordinates of echo or the atom the label is attached
    // to.
    output(" children ");
    String child = useTable.getDef("T" + colix + useFontFace + useFontStyle + "_" + text);
    if (child.charAt(0) == '_') {
      output("DEF " + child + " Billboard{axisOfRotation 0 0 0 children Transform{children Shape{");
      outputAppearance(colix, true);
      output(" geometry Text{fontStyle ");
      String fontstyle = useTable.getDef("F" + useFontFace + useFontStyle);
      if (fontstyle.charAt(0) == '_') {
        output("DEF " + fontstyle + " FontStyle{size 0.4 family \"" + useFontFace
            + "\" style \"" + useFontStyle + "\"}");      
      } else {
        output(fontstyle);
      }
      output(" string " + Escape.escapeStr(text) + "}}}}");
    } else {
      output(child);
    }
    output("}\n");
  }

  /*
   * Unsolved issues: # Non-label texts: echos, measurements :: need to get
   * space coordinates, not screen coord. # Font size: not implemented; 0.4A
   * is hardcoded (resizes with zoom) Java VRML font3d.fontSize = 13.0 size
   * (numeric), but in angstroms, not pixels font3d.fontSizeNominal = 13.0 #
   * Label offsets: not implemented; hardcoded to 0.25A in each x,y,z #
   * Multi-line labels: only the first line is received # Sub/superscripts not
   * interpreted
   */

}


