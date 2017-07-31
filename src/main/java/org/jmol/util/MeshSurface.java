package org.jmol.util;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Point4f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import org.jmol.script.Token;

public class MeshSurface {

  protected static final int SEED_COUNT = 25;

  public Vector3f[] spanningVectors;
  
  public String meshType;
  public int vertexCount;
  public Point3f[] vertices;
  public float[] vertexValues;
  public int[] vertexSource;
  
  public int polygonCount;
  public int[][] polygonIndexes;
  
  public boolean isTriangleSet; // just a set of flat polygons
  public boolean haveQuads;
  public short colix;
  public boolean isColorSolid = true;
  public Point3f offset;
  public Tuple3f[] altVertices;

  public short[] polygonColixes;
  public short[] vertexColixes;
  public Tuple3f[] normals; // for export only
  public int normalCount;   // for export only
  protected int normixCount;
  public BitSet bsPolygons;
  public Matrix4f mat4;
  public BitSet[] surfaceSet;
  public int[] vertexSets;
  public int nSets = 0;

  private int checkCount = 2;
  
  public MeshSurface() {
  }
  
  public MeshSurface(int[][] polygonIndexes, Tuple3f[] vertices, int nVertices,
      Tuple3f[] normals, int nNormals) {
    this.polygonIndexes = polygonIndexes;
    if (vertices instanceof Point3f[])
      this.vertices = (Point3f[]) vertices;
    else
      this.altVertices = vertices;
    this.vertexCount = (nVertices == 0 ? vertices.length : nVertices);
    this.normals = normals;
    this.normalCount = (nNormals == 0  && normals != null ? normals.length : nNormals);
  }
  
  public MeshSurface(Point3f[] vertices, float[] vertexValues, int vertexCount,
      int[][] polygonIndexes, int polygonCount, int checkCount) {
    // from DRAW only
    this.vertices = vertices;
    this.vertexValues = vertexValues;
    this.vertexCount = vertexCount;
    this.polygonIndexes = polygonIndexes;
    this.polygonCount = polygonCount;
    this.checkCount = checkCount; // will be 1
  }

  /**
   * @return The vertices.
   */
  public Tuple3f[] getVertices() {
    return (altVertices == null ? vertices : altVertices);
  }
  
  /**
   * @return  faces, if defined (in exporter), otherwise polygonIndexes
   */
  public int[][] getFaces() {
    return polygonIndexes;
  }

  public void setColix(short colix) {
    this.colix = colix;
  }

  public int addVertexCopy(Point3f vertex) { //used by mps and surfaceGenerator
    if (vertexCount == 0)
      vertices = new Point3f[SEED_COUNT];
    else if (vertexCount == vertices.length)
      vertices = (Point3f[]) ArrayUtil.doubleLength(vertices);
    vertices[vertexCount] = new Point3f(vertex);
    return vertexCount++;
  }

  public void addTriangle(int vertexA, int vertexB, int vertexC) {
    addPolygon(new int[] { vertexA, vertexB, vertexC }, null);
  }

  public void addQuad(int vertexA, int vertexB, int vertexC, int vertexD) {
    haveQuads = true;
    addPolygon(new int[] { vertexA, vertexB, vertexC, vertexD }, null);
  }

  public void setPolygonCount(int polygonCount) {    
    this.polygonCount = polygonCount;
    if (polygonCount < 0)
      return;
    if (polygonIndexes == null || polygonCount > polygonIndexes.length)
      polygonIndexes = new int[polygonCount][];
  }

  public int addVertexCopy(Point3f vertex, float value) {
    if (vertexCount == 0)
      vertexValues = new float[SEED_COUNT];
    else if (vertexCount >= vertexValues.length)
      vertexValues = ArrayUtil.doubleLength(vertexValues);
    vertexValues[vertexCount] = value;
    return addVertexCopy(vertex);
  } 

  public int addTriangleCheck(int vertexA, int vertexB, int vertexC, int check,
                              int check2, int color) {
    return (vertices == null
        || vertexValues != null
        && (Float.isNaN(vertexValues[vertexA])
            || Float.isNaN(vertexValues[vertexB]) 
            || Float.isNaN(vertexValues[vertexC])) 
        || Float.isNaN(vertices[vertexA].x)
        || Float.isNaN(vertices[vertexB].x) 
        || Float.isNaN(vertices[vertexC].x) 
        ? -1 
      : addPolygon(vertexA, vertexB, vertexC, check, check2, color, null));
  }

  private int addPolygon(int vertexA, int vertexB, int vertexC, int check,
                         int check2, int color, BitSet bs) {
    return (checkCount == 2 ? 
        addPolygon(new int[] { vertexA, vertexB, vertexC, check, check2 }, color, bs)
      : addPolygon(new int[] { vertexA, vertexB, vertexC, check }, bs) );
  }

  private int lastColor;
  private short lastColix;
    
  protected int addPolygon(int[] polygon, int color, BitSet bs) {
    if (color != 0) {
      if (polygonColixes == null || polygonCount == 0)
        lastColor = 0;
      short colix = (color == lastColor ? lastColix : (lastColix = Colix
          .getColix(lastColor = color)));      
      setPolygonColix(polygonCount, colix);
    }
    return addPolygon(polygon, bs);
  }

  private int addPolygon(int[] polygon, BitSet bs) {
    int n = polygonCount;
    if (polygonCount == 0)
      polygonIndexes = new int[SEED_COUNT][];
    else if (polygonCount == polygonIndexes.length)
      polygonIndexes = (int[][]) ArrayUtil.doubleLength(polygonIndexes);
    if (bs != null)
      bs.set(polygonCount);
    polygonIndexes[polygonCount++] = polygon;
    return n;
  }

  private void setPolygonColix(int index, short colix) {
    if (polygonColixes == null) {
      polygonColixes = new short[SEED_COUNT];
    } else if (index >= polygonColixes.length) {
      polygonColixes = ArrayUtil.doubleLength(polygonColixes);
    }
    polygonColixes[index] = colix;
  }
  
  public void invalidatePolygons() {
    for (int i = polygonCount; --i >= mergePolygonCount0;)
      if ((bsSlabDisplay == null || bsSlabDisplay.get(i)) && !setABC(i))
        polygonIndexes[i] = null;
  }

  protected int iA, iB, iC;
  
  protected boolean setABC(int i) {
    if (bsSlabDisplay != null && !bsSlabDisplay.get(i)
        && (bsSlabGhost == null || !bsSlabGhost.get(i)))
      return false;
    int[] vertexIndexes = polygonIndexes[i];
    if (vertexIndexes == null)
      return false;
    iA = vertexIndexes[0];
    iB = vertexIndexes[1];
    iC = vertexIndexes[2];
    return vertexValues == null || !(Float.isNaN(vertexValues[iA])
            || Float.isNaN(vertexValues[iB]) 
            || Float.isNaN(vertexValues[iC]));
  }

  public int polygonCount0;
  public int vertexCount0;
  
  public BitSet bsSlabDisplay;
  public BitSet bsSlabGhost;
  public int slabMeshType;
  public short slabColix;
  
  public void setSlab(BitSet bsDisplay, BitSet bsGhost, String type,
                      String color, float translucency) {
    bsSlabDisplay = bsDisplay;
    bsSlabGhost = bsGhost;
    slabMeshType = (type.equalsIgnoreCase("mesh") ? Token.mesh : Token.fill);
    slabColix = Colix.getColixTranslucent(Colix.getColix(color),
        true, translucency);
  }


  public BitSet bsDisplay;
  public String getSlabColor() {
    return (bsSlabGhost == null ? null : Colix.getHexCode(slabColix));
  }

  public String getSlabTranslucency() {
    return (bsSlabGhost == null ? null : "" + Colix.getColixTranslucencyFractional(slabColix));
  }

  public String getSlabType() {
    return (bsSlabGhost != null && slabMeshType == Token.mesh ? "mesh" : null);
  }

  public StringBuffer slabOptions;
  
  
  public static Object[] getSlabWithinRange(float min, float max) {
    return new Object[] { Integer.valueOf(Token.range), 
        new Float[] {Float.valueOf(min), Float.valueOf(max)}, Boolean.FALSE, null };
  }

  public void resetSlab() {
    slabPolygons(getSlabObject(Token.none, null, false, null), false);
  }

  public static Object[] getSlabObject(int tok, Object data, boolean isCap, Object colorData) {
    return new Object[] { Integer.valueOf(tok), data, Boolean.valueOf(isCap), colorData };
  }

  /**
   * legacy -- for some scripts with early isosurface slabbing
   * 
   * @param s
   * @param isCap
   * @return slabInfo object
   */
  public static Object[] getCapSlabObject(String s, boolean isCap) {
    try {
      if (s.indexOf("array") == 0) {
        String[] pts = TextFormat.split(s.substring(6, s.length() - 1), ",");
        return getSlabObject(Token.boundbox, new Point3f[] {
            (Point3f) Escape.unescapePoint(pts[0]),
            (Point3f) Escape.unescapePoint(pts[1]),
            (Point3f) Escape.unescapePoint(pts[2]),
            (Point3f) Escape.unescapePoint(pts[3]) }, isCap, null);
      }
      Object plane = Escape.unescapePoint(s);
      if (plane instanceof Point4f)
        return getSlabObject(Token.plane, plane, isCap, null);
    } catch (Exception e) {
      //
    }
    return null;
  }



  
  public void slabPolygons(List<Object[]> slabInfo, boolean allowCap) {
    for (int i = 0; i < slabInfo.size(); i++)
      if (!slabPolygons(slabInfo.get(i), allowCap))
          break;
  }
  
  public boolean slabPolygons(Object[] slabObject, boolean allowCap) {
    if (polygonCount0 < 0)
      return false; // disabled for some surface types
    int slabType = ((Integer) slabObject[0]).intValue();
    if (slabType == Token.none || slabType == Token.brillouin) {
      if (bsSlabDisplay != null && (polygonCount0 != 0 || vertexCount0 != 0)) {
        polygonCount = polygonCount0;
        vertexCount = vertexCount0;
        polygonCount0 = vertexCount0 = 0;
        normixCount = (isTriangleSet ? polygonCount : vertexCount);
        bsSlabDisplay.set(0, (polygonCount == 0 ? vertexCount : polygonCount));
        slabOptions = new StringBuffer(meshType + " slab none");
        bsSlabGhost = null;
        slabMeshType = Token.none;
      }
      if (slabType == Token.none)
        return false;
    }
    Object slabbingObject = slabObject[1];
    boolean andCap = ((Boolean) slabObject[2]).booleanValue() && !(slabType == Token.brillouin);
    if (andCap && !allowCap)
      return false;
    Object[] colorData = (Object[]) slabObject[3];
    boolean isGhost = (colorData != null);
    if (bsSlabDisplay == null || polygonCount0 == 0 && vertexCount0 == 0) {
      polygonCount0 = polygonCount;
      vertexCount0 = vertexCount;
      bsSlabDisplay = BitSetUtil.setAll(polygonCount == 0 ? vertexCount
          : polygonCount);
      bsSlabGhost = null;
      if (polygonCount == 0 && vertexCount == 0)
        return false;
    } else if (isMerged) {
      if (polygonCount == 0)
        bsSlabDisplay.set(mergeVertexCount0, vertexCount);
      else
        bsSlabDisplay.set(mergePolygonCount0, polygonCount);
    }

    if (isGhost) {
      if (bsSlabGhost == null)
        bsSlabGhost = new BitSet();
      slabMeshType = ((Integer) colorData[0]).intValue();
      slabColix = ((Short) colorData[1]).shortValue();
      if (Colix.isColixColorInherited(slabColix))
        slabColix = Colix.copyColixTranslucency(slabColix, colix);
      andCap = false;
      colix = Colix.getColixTranslucent(colix, false, 0);
    }

    
    StringBuffer sb = new StringBuffer();
    sb.append(andCap ? " cap " : " slab ");
    if (isGhost)
      sb.append("translucent ").append(
          Colix.getColixTranslucencyFractional(slabColix)).append(" ")
          .append(Colix.getHexCode(slabColix)).append(" ");
    switch (slabType) {
    case Token.brillouin:
      sb.append("brillouin");
      slabBrillouin((Point3f[]) slabbingObject);
      break;
    case Token.decimal:
      getIntersection(0, null, null, null, null, (BitSet) slabbingObject, null, andCap,
          false, Token.decimal, isGhost);
      break;
    case Token.plane:
      Point4f plane = (Point4f) slabbingObject;
      sb.append(Escape.escape(plane));
      getIntersection(0, plane, null, null, null, null, null, andCap,
          false, Token.plane, isGhost);
      break;
    case Token.unitcell:
    case Token.boundbox:
      Point3f[] box = (Point3f[]) slabbingObject;
      sb.append("within ").append(Escape.escape(box));
      Point4f[] faces = BoxInfo.getFacesFromCriticalPoints(box);
      for (int i = 0; i < faces.length; i++) {
        getIntersection(0, faces[i], null, null, null, null, null, andCap,
            false, Token.plane, isGhost);
      }
      break;
    case Token.data:
      getIntersection(0, null, null, null, (float[]) slabbingObject, null,
          null, false, false, Token.min, isGhost);
      break;
    case Token.within:
    case Token.range:
    case Token.mesh:
      Object[] o = (Object[]) slabbingObject;
      float distance = ((Float) o[0]).floatValue();
      switch (slabType) {
      case Token.within:
        Point3f[] points = (Point3f[]) o[1];
        BitSet bs = (BitSet) o[2];
        sb.append("within ").append(distance).append(
            bs == null ? Escape.escape(points) : Escape.escape(bs));
        getIntersection(distance, null, points, null, null, null, null,
            andCap, false, (distance > 0 ? Token.distance : Token.sphere), isGhost);
        break;
      case Token.range:
        // isosurface slab within range x.x y.y
        // if y.y < x.x then this effectively means "NOT within range y.y x.x"
        if (vertexValues == null)
          return false;
        float distanceMax = ((Float) o[1]).floatValue();
        sb.append("within range ").append(distance).append(" ").append(
            distanceMax);
        bs = (distanceMax < distance ? BitSetUtil.copy(bsSlabDisplay) : null);
        getIntersection(distance, null, null, null, null, null, null, andCap,
            false, Token.min, isGhost);
        BitSet bsA = (bs == null ? null : BitSetUtil.copy(bsSlabDisplay));
        BitSetUtil.copy(bs, bsSlabDisplay);
        getIntersection(distanceMax, null, null, null, null, null, null,
            andCap, false, Token.max, isGhost);
        if (bsA != null)
          bsSlabDisplay.or(bsA);
        break;
      case Token.mesh:
        //NOT IMPLEMENTED
        MeshSurface mesh = (MeshSurface) o[1];
        //distance = -1;
        getIntersection(0, null, null, null, null, null, mesh, andCap,
            false, distance < 0 ? Token.min : Token.max, isGhost);
        //TODO: unresolved how exactly to store this in the state
        // -- must indicate exact set of triangles to slab and how!
        break;
      }
      break;
    }
    String newOptions = sb.toString();
    if (slabOptions == null)
      slabOptions = new StringBuffer();
    if (slabOptions.indexOf(newOptions) < 0)
      slabOptions.append(slabOptions.length() > 0 ? "; ": "").append(meshType).append(newOptions);      	
    return true;
  }

  /**
   * @param unitCellVectors 
   */
  protected void slabBrillouin(Point3f[] unitCellVectors) {
    // isosurfaceMesh only
    return;
  }

  protected int addIntersectionVertex(Point3f vertex, float value, int source, 
                                    int set, Map<String, Integer> mapEdge, int i1, int i2) {
    
    String key = (i1 > i2 ? i2 + "_" + i1 : i1 + "_" + i2);
    if (i1 >= 0) {
      Integer v = mapEdge.get(key);
      if (v != null) {
        return v.intValue();
      }
    }
    
    if (vertexSource != null) {
      if (vertexCount >= vertexSource.length)
        vertexSource = ArrayUtil.doubleLength(vertexSource);
      vertexSource[vertexCount] = source;
    }
    if (vertexSets != null) {
      if (vertexCount >= vertexSets.length)
        vertexSets = ArrayUtil.doubleLength(vertexSets);
      vertexSets[vertexCount] = set;
    }
    int i = addVertexCopy(vertex, value, true, -4);
    mapEdge.put(key, Integer.valueOf(i));
    return i;    
  } 

  /**
   * @param vertex 
   * @param value 
   * @param assocNormals  
   * @param iNormal 
   * @return   new vertex index
   */
  protected int addVertexCopy(Point3f vertex, float value, boolean assocNormals, int iNormal) {
    // isosurface only
    return addVertexCopy(vertex, value);
  }

  private boolean doClear;
  private boolean doGhost;
  private boolean doCap;
  private int iD, iE;

  public int mergeVertexCount0;
  public int mergePolygonCount0;
  public boolean isMerged;

  
  /**
   * @param distance      a distance from a plane or point 
   * @param plane         a slabbing plane
   * @param ptCenters     a set of atoms to measure distance from
   * @param vData         when not null, this is a query, not an actual slabbing
   * @param fData         vertex values or other data to overlay
   * @param bsSource TODO
   * @param meshSurface   second surface; not implemented -- still some problems there
   * @param andCap        to cap this off, crudely only
   * @param doClean       compact set - draw only
   * @param tokType       type of slab
   * @param isGhost       translucent slab, so we mark slabbed triangles
   */
  public void getIntersection(float distance, Point4f plane,
                              Point3f[] ptCenters, List<Point3f[]> vData,
                              float[] fData, BitSet bsSource,
                              MeshSurface meshSurface, boolean andCap, boolean doClean,
                              int tokType, boolean isGhost) {
    boolean isSlab = (vData == null);
    Point3f[] pts = null;
    if (fData == null)
      fData = vertexValues;
    Map<String, Integer> mapEdge = new Hashtable<String, Integer>();
    

    /*    
    Vector3f vNorm = null;
    Vector3f vBC = null;
    Vector3f vAC = null;
    Point3f[] pts2 = null;
    Vector3f vTemp3 = null;
    Point4f planeTemp = null;
    boolean isMeshIntersect = (meshSurface != null);
    if (isMeshIntersect) {
      // NOT IMPLEMENTED
      vBC = new Vector3f();
      vAC = new Vector3f();
      vNorm = new Vector3f();
      plane = new Point4f();
      planeTemp = new Point4f();
      vTemp3 = new Vector3f();
      pts2 = new Point3f[] { null, new Point3f(), new Point3f() };
    }
    */
    if (ptCenters != null || isGhost)
      andCap = false; // can only cap faces, and no capping of ghosts
    float[] values = new float[2];
    float[] fracs = new float[2];
    double absD = Math.abs(distance);
    float d1, d2, d3, valA, valB, valC;
    int sourceA = 0, sourceB = 0, sourceC = 0, setA = 0;
    List<int[]> iPts = (andCap ? new ArrayList<int[]>() : null);
    if (polygonCount == 0) {
      for (int i = mergeVertexCount0; i < vertexCount; i++) {
        if (Float.isNaN(fData[i])
            || checkSlab(tokType, vertices[i], fData[i], distance, plane,
                ptCenters, bsSource) > 0)
          bsSlabDisplay.clear(i);
      }
      return;
    }
    int iLast = polygonCount;
    for (int i = mergePolygonCount0; i < iLast; i++) {
      if (!setABC(i))
        continue;
      BitSet bsSlab = (bsSlabGhost != null && bsSlabGhost.get(i) ? bsSlabGhost : bsSlabDisplay);
      int check1 = polygonIndexes[i][3];
      int check2 = (checkCount == 2 ? polygonIndexes[i][4] : 0);
      Point3f vA = vertices[iA];
      Point3f vB = vertices[iB];
      Point3f vC = vertices[iC];
      valA = fData[iA];
      valB = fData[iB];
      valC = fData[iC];
      if (vertexSource != null) {
        sourceA = vertexSource[iA];
        sourceB = vertexSource[iB];
        sourceC = vertexSource[iC];
      }
      if (vertexSets != null) 
        setA = vertexSets[iA];
      d1 = checkSlab(tokType, vA, valA, (bsSource == null ? distance : sourceA), plane, ptCenters, bsSource);
      d2 = checkSlab(tokType, vB, valB, (bsSource == null ? distance : sourceB), plane, ptCenters, bsSource);
      d3 = checkSlab(tokType, vC, valC, (bsSource == null ? distance : sourceC), plane, ptCenters, bsSource);
      int test1 = (d1 != 0 && d1 < 0 ? 1 : 0) + (d2 != 0 && d2 < 0 ? 2 : 0)
          + (d3 != 0 && d3 < 0 ? 4 : 0);

/*      
      if (iA == 955 || iB == 955 || iC == 955) {
        System.out.println(i + " " + iA + " " + iB + " " + iC + " "+ d1 + " " + d2 + " " + d3 + " " + test1);
        System.out.println("testing messhsurf ");
      }
*/     
      /*      
      if (isMeshIntersect && test1 != 7 && test1 != 0) {
        // NOT IMPLEMENTED
        boolean isOK = (d1 == 0 && d2 * d3 >= 0 || d2 == 0 && (d1 * d3) >= 0 || d3 == 0
            && d1 * d2 >= 0);
        if (isOK)
          continue;
        // We have a potential crossing. Now to find the exact point of crossing
        // the other isosurface.
        if (checkIntersection(vA, vB, vC, meshSurface, pts2, vNorm, vBC, vAC,
            plane, planeTemp, vTemp3)) {
          iD = addIntersectionVertex(pts2[0], 0, sourceA, mapEdge, -1, -1); // have to choose some source
          addPolygon(iA, iB, iD, check1 & 1, check2, 0, bsSlabDisplay);
          addPolygon(iD, iB, iC, check1 & 2, check2, 0, bsSlabDisplay);
          addPolygon(iA, iD, iC, check1 & 4, check2, 0, bsSlabDisplay);
          test1 = 0; // toss original    
          iLast = polygonCount;
        } else {
          // process normally for now  
          // not fully implemented -- need to check other way as well.
        }
      }
      */      
      switch (test1) {
      default:
      case 7:
      case 0:
        // all on the same side
        break;
      case 1:
      case 6:
        // BC on same side
        if (ptCenters == null)
          pts = new Point3f[] {
              interpolatePoint(vA, vB, -d1, d2, valA, valB, values, fracs, 0),
              interpolatePoint(vA, vC, -d1, d3, valA, valC, values, fracs, 1) };
        else
          pts = new Point3f[] {
              interpolateSphere(vA, vB, -d1, -d2, absD, valA, valB, values,
                  fracs, 0),
              interpolateSphere(vA, vC, -d1, -d3, absD, valA, valC, values,
                  fracs, 1) };
        break;
      case 2:
      case 5:
        //AC on same side
        if (ptCenters == null)
          pts = new Point3f[] {
              interpolatePoint(vB, vA, -d2, d1, valB, valA, values, fracs, 1),
              interpolatePoint(vB, vC, -d2, d3, valB, valC, values, fracs, 0) };
        else
          pts = new Point3f[] {
              interpolateSphere(vB, vA, -d2, -d1, absD, valB, valA, values,
                  fracs, 1),
              interpolateSphere(vB, vC, -d2, -d3, absD, valB, valC, values,
                  fracs, 0) };
        break;
      case 3:
      case 4:
        //AB on same side need A-C, B-C
        if (ptCenters == null)
          pts = new Point3f[] {
              interpolatePoint(vC, vA, -d3, d1, valC, valA, values, fracs, 0),
              interpolatePoint(vC, vB, -d3, d2, valC, valB, values, fracs, 1) };
        else
          pts = new Point3f[] {
              interpolateSphere(vC, vA, -d3, -d1, absD, valC, valA, values,
                  fracs, 0),
              interpolateSphere(vC, vB, -d3, -d2, absD, valC, valB, values,
                  fracs, 1) };
        break;
      }
      doClear = true;
      doGhost = isGhost;
      doCap = andCap;
      BitSet bs;
      // adjust for minor discrepencies 
      //for (int j = 0; j < 2; j++) 
      //if (fracs[j] == 0)
      //fracs[1 - j] = (fracs[1 - j] < 0.5 ? 0 : 1);

      if (isSlab) {
        //        iD = iE = -1;
        switch (test1) {
        //             A
        //            / \
        //           B---C
        case 0:
          // all on the same side -- toss
          doCap = false;
          break;
        case 7:
          // all on the same side -- keep
          continue;
        case 1:
        case 6:
          //          0  A  0
          //            / \
          //        0 -------- 1 
          //          /     \
          //       1 B-------C  1
          boolean tossBC = (test1 == 1);
          if (tossBC || isGhost) {
            // 1: BC on side to toss -- +tossBC+isGhost  -tossBC+isGhost
            if (!getDE(fracs, 0, iA, iB, iC, tossBC))
              break;
            if (iD < 0)
              iD = addIntersectionVertex(pts[0], values[0], sourceA, setA,
                  mapEdge, iA, iB);
            if (iE < 0)
              iE = addIntersectionVertex(pts[1], values[1], sourceA, setA,
                  mapEdge, iA, iC);
            bs = (tossBC ? bsSlab : bsSlabGhost);
            addPolygon(iA, iD, iE, check1 & 5 | 2, check2, 0, bs);
            if (!isGhost)
              break;
          }
          // BC on side to keep -- -tossBC+isGhost,  +tossBC+isGhost
          if (!getDE(fracs, 1, iA, iC, iB, tossBC))
            break;
          bs = (tossBC ? bsSlabGhost : bsSlab);
          if (iE < 0) {
            iE = addIntersectionVertex(pts[0], values[0], sourceB, setA, mapEdge,
                iA, iB);
            addPolygon(iE, iB, iC, check1 & 3, check2, 0, bs);
          }
          if (iD < 0) {
            iD = addIntersectionVertex(pts[1], values[1], sourceC, setA, mapEdge,
                iA, iC);
            addPolygon(iD, iE, iC, check1 & 4 | 1, check2, 0, bs);
          }
          break;
        case 5:
        case 2:
          //              A
          //            \/ \
          //            /\  \
          //           B--\--C
          //               \
          //
          boolean tossAC = (test1 == 2);
          if (tossAC || isGhost) {
            //AC on side to toss
            if (!getDE(fracs, 0, iB, iC, iA, tossAC))
              break;
            bs = (tossAC ? bsSlab : bsSlabGhost);
            if (iE < 0)
              iE = addIntersectionVertex(pts[0], values[0], sourceB, setA,
                  mapEdge, iB, iA);
            if (iD < 0)
              iD = addIntersectionVertex(pts[1], values[1], sourceB, setA,
                  mapEdge, iB, iC);
            addPolygon(iE, iB, iD, check1 & 3 | 4, check2, 0, bs);
            if (!isGhost)
              break;
          }
          // AC on side to keep
          if (!getDE(fracs, 1, iB, iA, iC, tossAC))
            break;
          bs = (tossAC ? bsSlabGhost : bsSlab);
          if (iD < 0) {
            iD = addIntersectionVertex(pts[0], values[0], sourceA, setA, mapEdge,
                iB, iA);
            addPolygon(iA, iD, iC, check1 & 5, check2, 0, bs);
          }
          if (iE < 0) {
            iE = addIntersectionVertex(pts[1], values[1], sourceC, setA, mapEdge,
                iB, iC);
            addPolygon(iD, iE, iC, check1 & 2 | 1, check2, 0, bs);
          }
          break;
        case 4:
        case 3:
          //              A
          //             / \/
          //            /  /\
          //           B--/--C
          //             /
          //
          boolean tossAB = (test1 == 4);
          if (tossAB || isGhost) {
            if (!getDE(fracs, 0, iC, iA, iB, tossAB))
              break;
            if (iD < 0)
              iD = addIntersectionVertex(pts[0], values[0], sourceC, setA,
                  mapEdge, iA, iC); //CA
            if (iE < 0)
              iE = addIntersectionVertex(pts[1], values[1], sourceC, setA,
                  mapEdge, iB, iC); //CB
            bs = (tossAB ? bsSlab : bsSlabGhost);
            addPolygon(iD, iE, iC, check1 & 6 | 1, check2, 0, bs);
            if (!isGhost)
              break;
          }
          //AB on side to keep
          if (!getDE(fracs, 1, iC, iB, iA, tossAB))
            break;
          bs = (tossAB ? bsSlabGhost : bsSlab);
          if (iE < 0) {
            iE = addIntersectionVertex(pts[0], values[0], sourceA, setA, mapEdge,
                iA, iC); //CA
            addPolygon(iA, iB, iE, check1 & 5, check2, 0, bs);
          }
          if (iD < 0) {
            iD = addIntersectionVertex(pts[1], values[1], sourceB, setA, mapEdge,
                iB, iC); //CB
            addPolygon(iE, iB, iD, check1 & 2 | 4, check2, 0, bs);
          }
          break;
        }
        if (doClear) {
          bsSlab.clear(i);
          if (doGhost)
            bsSlabGhost.set(i);
        }
        if (doCap) {
          iPts.add(new int[] { iD, iE });
        }
      } else if (pts != null) {
        vData.add(pts);
      }
    }
    if (andCap && iPts.size() > 0) {
      Point3f center = new Point3f();
      for (int i = iPts.size(); --i >= 0;) {
        int[] ipts = iPts.get(i);
        center.add(vertices[ipts[0]]);
        center.add(vertices[ipts[1]]);
      }
      center.scale(0.5f / iPts.size());
      int v0 = addIntersectionVertex(center, 0, -1, setA, mapEdge, -1, -1);
      for (int i = iPts.size(); --i >= 0;) {
        int[] ipts = iPts.get(i);
        //int p =
        addPolygon(ipts[0], v0, ipts[1], 0, 0, 0, bsSlabDisplay);
      }
    }
    
    if (!doClean)
      return;
    BitSet bsv = new BitSet();
    BitSet bsp = new BitSet();
    for (int i = 0; i < polygonCount; i++) {
      if (polygonIndexes[i] == null)
        continue;
      bsp.set(i);
      for (int j = 0; j < 3; j++)
        bsv.set(polygonIndexes[i][j]);
    }
    int n = 0;
    int nPoly = bsp.cardinality();
    if (nPoly != polygonCount) {
      int[] map = new int[vertexCount];
      for (int i = 0; i < vertexCount; i++)
        if (bsv.get(i))
          map[i] = n++;
      Point3f[] vTemp = new Point3f[n];
      n = 0;
      for (int i = 0; i < vertexCount; i++)
        if (bsv.get(i))
          vTemp[n++] = vertices[i];
      int[][] pTemp = new int[nPoly][];
      nPoly = 0;
      for (int i = 0; i < polygonCount; i++)
        if (polygonIndexes[i] != null) {
          for (int j = 0; j < 3; j++)
            polygonIndexes[i][j] = map[polygonIndexes[i][j]];
          pTemp[nPoly++] = polygonIndexes[i];
        }
      vertices = vTemp;
      vertexCount = n;
      polygonIndexes = pTemp;
      polygonCount = nPoly;
    }
  }

  private static int setPoint(float[] fracs, int i, int i0, int i1) {
    return (fracs[i] == 0 ? i0 : fracs[i] == 1 ? i1 : -1);
  }

  private boolean getDE(float[] fracs, int fD, int i1, int i2, int i3, boolean toss23) {
    
    //          0 (1) 0
    //            / \
    //     iD 0 -fracs- 1 iE 
    //          /     \
    //      1 (2)-------(3) 1
    iD = setPoint(fracs, fD, i1, i2);
    iE = setPoint(fracs, 1 - fD, i1, i3);

    // initially: doClear=true, doCap = andCap, doGhost = isGhost

    if (iD == i1 && iE == i1) {
      // toss all if tossing 23, otherwise ignore
      doClear = toss23;
      doCap = false;
      return false;
    }
    if (iD == i2 && iE == i3) {
      // cap but don't toss if tossing 23
      doClear = !toss23;
      return false;
    }
    if (iD == i1 || iE == i1) {
      // other is i2 or i3 -- along an edge
      // cap but toss all if tossing 23
      doClear = toss23;
      if (iD < 0) {
        iD = (toss23 ? i2 : i3);
      } else if (iE < 0) {
        iE = (toss23 ? i3 : i2);
      }
      return doCap;
    }
    
    doGhost = false;
    return true;
  }            

  private static float checkSlab(int tokType, Point3f v, float val, float distance,
                          Point4f plane, Point3f[] ptCenters, BitSet bs) {
    float d;
    switch (tokType) {
    case Token.decimal:
      return (bs.get((int)val) ? 1 : -1);
    case Token.min:
      d = distance - val;
      break;
    case Token.max:
      d = val - distance;
      break;
    case Token.plane:
      d = Measure.distanceToPlane(plane, v);
      break;
    case Token.distance:
      d = minDist(v, ptCenters) - distance;
      break;
    default:  
      d = -minDist(v, ptCenters) - distance;
      break;
    }
    return (Math.abs(d) < 0.0001f ? 0 : d);
  }
/*
  private static boolean checkIntersection(Point3f vA, Point3f vB, Point3f vC,
                                    MeshSurface meshSurface, Point3f[] pts, 
                                    Vector3f vNorm, Vector3f vAB, Vector3f vAC, Point4f plane, Point4f pTemp, Vector3f vTemp3) {
    
    Measure.getPlaneThroughPoints(vA, vB, vC, vNorm, vAB, vAC, plane);
    for (int i = 0; i < meshSurface.polygonCount; i++) {
      Point3f pt = meshSurface.getTriangleIntersection(i, vA, vB, vC, plane, vNorm, vAB, pts[1], pts[2], vAC, pTemp, vTemp3);
      if (pt != null) {
        pts[0] = new Point3f(pt);
        return true; 
      }
    }
    return false;
  }
  
  private Point3f getTriangleIntersection(int i, Point3f vA, Point3f vB, Point3f vC, Point4f plane, Vector3f vNorm, Vector3f vTemp, 
                                          Point3f ptRet, Point3f ptTemp, Vector3f vTemp2, Point4f pTemp, Vector3f vTemp3) {
    return (setABC(i) ? Measure.getTriangleIntersection(vA, vB, vC, plane, vertices[iA], vertices[iB], vertices[iC], vNorm, vTemp, ptRet, ptTemp, vTemp2, pTemp, vTemp3) : null);
  }
*/

  private static float minDist(Point3f pt, Point3f[] ptCenters) {
    float dmin = Integer.MAX_VALUE;
    for (int i = ptCenters.length; --i >= 0;) {
      float d = ptCenters[i].distance(pt);
      if (d < dmin)
        dmin = d;
    }
    return dmin;
  }

  private Point3f interpolateSphere(Point3f v1, Point3f v2, float d1, float d2,
                                    double absD, float val1, float val2, float[] values, float[] fracs, int i) {
    return interpolateFraction(v1, v2, getSphericalInterpolationFraction(absD, d1,
        d2, v1.distance(v2)), val1, val2, values, fracs, i);
  }

  private static Point3f interpolatePoint(Point3f v1, Point3f v2, float d1, float d2, float val1, float val2, float[] values, float[] fracs, int i) {
    return interpolateFraction(v1, v2, d1 / (d1 + d2), val1, val2, values, fracs, i);
  }

  private static Point3f interpolateFraction(Point3f v1, Point3f v2, float f, float val1, float val2, float[] values, float[] fracs, int i) {
    if (f < 0.0001)
      f = 0;
    else if (f > 0.9999)
      f = 1;
    fracs[i] = f;
    values[i] = (val2 - val1) * f + val1;
    return new Point3f(v1.x + (v2.x - v1.x) * f, 
        v1.y + (v2.y - v1.y) * f, 
        v1.z + (v2.z - v1.z) * f);
  }

  public static float getSphericalInterpolationFraction(double r, double valueA,
                                                      double valueB, double d) {
    double ra = Math.abs(r + valueA) / d;
    double rb = Math.abs(r + valueB) / d;
    r /= d;
    double ra2 = ra * ra;
    double q = ra2 - rb * rb + 1;
    double p = 4 * (r * r - ra2);
    double factor = (ra < rb ? 1 : -1);
    return (float) (((q) + factor * Math.sqrt(q * q + p)) / 2);
  }

}
