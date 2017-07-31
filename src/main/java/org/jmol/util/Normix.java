/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-09-03 16:27:33 -0500 (Mon, 03 Sep 2012) $
 * $Revision: 17501 $
 *
 * Copyright (C) 2005  Miguel, Jmol Development, www.jmol.org
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

package org.jmol.util;


import java.util.BitSet;
import java.util.Random;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;


/**
 * Provides quantization of normalized vectors so that shading for
 * lighting calculations can be handled by a simple index lookup
 *<p>
 * A 'normix' is a normal index, represented as a short
 *
 * @author Miguel, miguel@jmol.org
 */
public class Normix {

  
  // these inital 
  final static int NORMIX_GEODESIC_LEVEL = Geodesic.standardLevel;

  private final static int normixCount = Geodesic.getVertexCount(NORMIX_GEODESIC_LEVEL);
  private final static Vector3f[] vertexVectors = Geodesic.getVertexVectors(); 
  private final static short[] inverseNormixes = new short[normixCount];
  
  public static Vector3f getVector(short normix) {
    return vertexVectors[normix];
  }
  
  public static short getInverseNormix(short normix) {
    return inverseNormixes[normix];
  }

  private final static short[][] neighborVertexesArrays = Geodesic.getNeighborVertexesArrays();
  private final static boolean TIMINGS = false;

  public static final short NORMIX_NULL = 9999;
  
  //private final static boolean DEBUG_WITH_SEQUENTIAL_SEARCH = false;

  
  static {
    // level 0 1 2 3
    // vertices 12, 42, 162, 642
    BitSet bsTemp = new BitSet();
    for (int n = normixCount; --n >= 0;) {
      Vector3f v = vertexVectors[n];
      inverseNormixes[n] = getNormix(-v.x, -v.y, -v.z, NORMIX_GEODESIC_LEVEL,
          bsTemp);
    }

    if (TIMINGS) {
      Logger.info("begin timings!");
      for (int i = 0; i < normixCount; ++i) {
        short normix = getNormix(vertexVectors[i], bsTemp);
        Logger.info("draw normix" + i + " {" + vertexVectors[i].x + " "
            + vertexVectors[i].y + " " + vertexVectors[i].z + "} {0 0 0} \""
            + i + "\"");
        if (normix != i)
          if (Logger.debugging) {
            Logger.debug("" + i + " -> " + normix);
          }
      }
      Random rand = new Random();
      Vector3f vFoo = new Vector3f();
      Vector3f vBar = new Vector3f();
      Vector3f vSum = new Vector3f();

      int runCount = 100000;
      short[] neighborVertexes = neighborVertexesArrays[NORMIX_GEODESIC_LEVEL];
      Logger.startTimer();
      for (int i = 0; i < runCount; ++i) {
        short foo = (short) (rand.nextDouble() * normixCount);
        int offsetNeighbor;
        short bar;
        do {
          offsetNeighbor = foo * 6 + (int) (rand.nextDouble() * 6);
          bar = neighborVertexes[offsetNeighbor];
        } while (bar == -1);
        vFoo.set(vertexVectors[foo]);
        vFoo.scale(rand.nextFloat());
        vBar.set(vertexVectors[bar]);
        vBar.scale(rand.nextFloat());
        vSum.add(vFoo, vBar);
        vSum.normalize();
      }
      Logger.checkTimer("base runtime for " + runCount);
      Logger.startTimer();
      for (int i = 0; i < runCount; ++i) {
        short foo = (short) (rand.nextDouble() * normixCount);
        int offsetNeighbor;
        short bar;
        do {
          offsetNeighbor = foo * 6 + (int) (rand.nextDouble() * 6);
          bar = neighborVertexes[offsetNeighbor];
        } while (bar == -1);
        vFoo.set(vertexVectors[foo]);
        vFoo.scale(rand.nextFloat());
        vBar.set(vertexVectors[bar]);
        vBar.scale(rand.nextFloat());
        vSum.add(vFoo, vBar);
        short sum = getNormix(vSum, bsTemp);
        if (sum != foo && sum != bar) {
          /*
           * if (Logger.debugging) { Logger.debug( "foo:" + foo + " -> " +
           * vertexVectors[foo] + "\n" + "bar:" + bar + " -> " +
           * vertexVectors[bar] + "\n" + "sum:" + sum + " -> " +
           * vertexVectors[sum] + "\n" + "foo.dist="+dist2(vSum,
           * vertexVectors[foo])+"\n"+ "bar.dist="+dist2(vSum,
           * vertexVectors[bar])+"\n"+ "sum.dist="+dist2(vSum,
           * vertexVectors[sum])+"\n"+ "\nvSum:" + vSum + "\n"); }
           */
          throw new NullPointerException();
        }
        short sum2 = getNormix(vSum, bsTemp);
        if (sum != sum2) {
          Logger.debug("normalized not the same answer?");
          throw new NullPointerException();
        }
      }
      Logger.checkTimer("normix2 runtime for " + runCount);
    }
  }
  
  public static short getNormix(Vector3f v, BitSet bsTemp) {
    return getNormix(v.x, v.y, v.z, NORMIX_GEODESIC_LEVEL, bsTemp);
  }

  public static short get2SidedNormix(Vector3f v, BitSet bsTemp) {
    return (short)~getNormix(v.x, v.y, v.z, Normix.NORMIX_GEODESIC_LEVEL, bsTemp);
  }

  static short getNormix(double x, double y, double z, int geodesicLevel, BitSet bsConsidered) {
    short champion;
    double t;
    if (z >= 0) {
      champion = 0;
      t = z - 1;
    } else {
      champion = 11;
      t = z - (-1);
    }
    bsConsidered.clear();
    bsConsidered.set(champion);
    double championDist2 = x*x + y*y + t*t;
    for (int lvl = 0; lvl <= geodesicLevel; ++lvl) {
      short[] neighborVertexes = neighborVertexesArrays[lvl];
      for (int offsetNeighbors = 6 * champion,
             i = offsetNeighbors + (champion < 12 ? 5 : 6);
           --i >= offsetNeighbors; ) {
        short challenger = neighborVertexes[i];
        if (bsConsidered.get(challenger))
            continue;
        bsConsidered.set(challenger);
        //Logger.debug("challenger=" + challenger);
        Vector3f v = vertexVectors[challenger];
        double d;
        // d = dist2(v, x, y, z);
        //Logger.debug("challenger d2=" + (d*d));
        d = v.x - x;
        double d2 = d * d;
        if (d2 >= championDist2)
          continue;
        d = v.y - y;
        d2 += d * d;
        if (d2 >= championDist2)
          continue;
        d = v.z - z;
        d2 += d * d;
        if (d2 >= championDist2)
          continue;
        champion = challenger;
        championDist2 = d2;
      }
    }

/*    
    if (DEBUG_WITH_SEQUENTIAL_SEARCH) {
      int champSeq = 0;
      double champSeqD2 = dist2(vertexVectors[champSeq], x, y, z);
      for (int k = Geodesic.getVertexCount(geodesicLevel); --k > 0; ) {
        double challengerD2 = dist2(vertexVectors[k], x, y, z);
        if (challengerD2 < champSeqD2) {
          champSeq = k;
          champSeqD2 = challengerD2;
        }
      }
      if (champion != champSeq) {
        if (champSeqD2 + .01 < championDist2) {
          Logger.debug("?que? getNormix is messed up?");
          boolean considered = bsConsidered.get(champSeq);
          if (Logger.debugging) {
            Logger.debug("Was the sequential winner considered? " + considered);
            Logger.debug(
                "champion " + champion + " @ " + championDist2 +
                " sequential champ " + champSeq + " @ " + champSeqD2 + "\n");
          }
          return (short)champSeq;
        }
      }
    }
*/
    return champion;
  }

  // only these three instance variables depend upon current orientation:
  
  private final Vector3f[] transformedVectors = new Vector3f[normixCount];
  {
    for (int i = normixCount; --i >= 0; )
      transformedVectors[i] = new Vector3f();
  }
  private final byte[] shadeIndexes = new byte[normixCount];
  private final byte[] shadeIndexes2Sided = new byte[normixCount];

  public Vector3f[] getTransformedVectors() {
    return transformedVectors;
  }
  
  public boolean isDirectedTowardsCamera(short normix) {
    // normix < 0 means a double sided normix, so always visible
    return (normix < 0) || (transformedVectors[normix].z > 0);
  }

  public void setRotationMatrix(Matrix3f rotationMatrix) {
    for (int i = normixCount; --i >= 0; ) {
      Vector3f tv = transformedVectors[i];
      rotationMatrix.transform(vertexVectors[i], tv);
//        if (i == 0)
  //        System.out.println(i + " " + shadeIndexes[i]);

      shadeIndexes[i] = Shader.getShadeIndexNormalized(tv.x, -tv.y, tv.z);
    //  if (i == 0 || i == 219 || i == 162 || i == 193)
      //  System.out.println(i + " " + shadeIndexes[i]);
      shadeIndexes2Sided[i] = (tv.z >= 0 ? shadeIndexes[i] 
          : Shader.getShadeIndexNormalized(-tv.x, tv.y, -tv.z));
    }
  }

  private static byte nullShadeIndex = 50;
  
  public int getShadeIndex(short normix) {
    return (normix == ~NORMIX_NULL
        || normix == NORMIX_NULL 
        ? nullShadeIndex
        : normix < 0 ? shadeIndexes2Sided[~normix] : shadeIndexes[normix]);
  }

}
