/* $RCSfile$
 * $Author: nicove $
 * $Date: 2007-03-25 06:09:49 -0500 (Sun, 25 Mar 2007) $
 * $Revision: 7221 $
 *
 * Copyright (C) 2000-2005  The Jmol Development Team
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3f;

import org.jmol.constant.EnumPalette;
import org.jmol.viewer.JmolConstants;

/*
 * 
 * just a simple class using crude color encoding
 * 
 * 
 * NOT THREAD-SAFE! TOO MANY STATIC FIELDS!!
 * 
 * The idea was that isosurface would have access to user-defined applet-wide color schemes.
 * but what we have is a set of globals that any applet could use to mess up any other applet.
 * 
 */


 public class ColorEncoder {

   public ColorEncoder(ColorEncoder propertyColorEncoder) {
    if (propertyColorEncoder == null) {
      schemes = new Hashtable<String, int[]>();
      argbsCpk = EnumPalette.argbsCpk;
      argbsRoygb = JmolConstants.argbsRoygbScale;
      argbsRwb = JmolConstants.argbsRwbScale;
      argbsShapely = JmolConstants.argbsShapely;
      argbsAmino = JmolConstants.argbsAmino;
      ihalf = JmolConstants.argbsRoygbScale.length / 3;
      this.propertyColorEncoder = this;
    } else {
      this.propertyColorEncoder = propertyColorEncoder;
      schemes = propertyColorEncoder.schemes;
    }
  }
    
  private final static int GRAY = 0xFF808080;
  

  public final static String BYELEMENT_PREFIX  = "byelement";
  public final static String BYRESIDUE_PREFIX = "byresidue";
  private final static String BYELEMENT_JMOL = BYELEMENT_PREFIX + "_jmol"; 
  private final static String BYELEMENT_RASMOL = BYELEMENT_PREFIX + "_rasmol";
  private final static String BYRESIDUE_SHAPELY = BYRESIDUE_PREFIX + "_shapely"; 
  private final static String BYRESIDUE_AMINO = BYRESIDUE_PREFIX + "_amino"; 
  
  public final static int CUSTOM = -1;
  public final static int ROYGB = 0;
  public final static int BGYOR = 1;
  public final static int JMOL = 2;
  public final static int RASMOL = 3;
  public final static int SHAPELY = 4;
  public final static int AMINO = 5;
  public final static int RWB   = 6;
  public final static int BWR   = 7;
  public final static int LOW   = 8;
  public final static int HIGH  = 9;
  public final static int BW  = 10;
  public final static int WB  = 11;
  public final static int USER = -12;
  public final static int RESU = -13;
  public final static int ALT = 14; // == 0

  private final static String[] colorSchemes = {
    "roygb", "bgyor", 
    BYELEMENT_JMOL, BYELEMENT_RASMOL, BYRESIDUE_SHAPELY, 
    BYRESIDUE_AMINO, 
    "rwb", "bwr", "low", "high", "bw", "wb",
    // custom
    "user", "resu", 
    // ALT_NAMES:
    "rgb", "bgr", 
    "jmol", "rasmol", BYRESIDUE_PREFIX 
  };

  private final static int getSchemeIndex(String colorScheme) {
    for (int i = 0; i < colorSchemes.length; i++)
      if (colorSchemes[i].equalsIgnoreCase(colorScheme))
        return (i >= ALT ? i - ALT : i < -USER ? i : -i);
    return CUSTOM;
  }

  private final static String fixName(String name) {
    if (name.equalsIgnoreCase(BYELEMENT_PREFIX)) 
        return BYELEMENT_JMOL;
    int ipt = getSchemeIndex(name);
    return (ipt >= 0 ? colorSchemes[ipt] : name.toLowerCase());
  }
  
  // these are only implemented in the MASTER colorEncoder
  private int[] paletteBW;
  private int[] paletteWB;
  private int[] argbsCpk;
  private int[] argbsRoygb;
  private int[] argbsRwb;
  private int[] argbsShapely;
  private int[] argbsAmino;
  private int ihalf;
  private static int[] rasmolScale;
  public Map<String, int[]> schemes;


  public int currentPalette = ROYGB;
  public int currentSegmentCount = 1;
  public boolean isTranslucent = false;
  public float lo;
  public float hi;
  public boolean isReversed;


  //TODO  NONE OF THESE SHOULD BE STATIC:
  
  int[] userScale = new int[] { GRAY };
  int[] thisScale = new int[] { GRAY };
  String thisName = "scheme";
  boolean isColorIndex;
  
  ColorEncoder propertyColorEncoder;

  /**
   * 
   * @param name
   * @param scale  if null, then this is a reset.
   * @param isOverloaded  if TRUE, 
   * @return  >= 0 for a default color scheme
   */
  private synchronized int makeColorScheme(String name, int[] scale,
                                                  boolean isOverloaded) {
    // from getColorScheme, setUserScale, ColorManager.setDefaultColors
    name = fixName(name);
    if (scale == null) {
      // resetting scale
      schemes.remove(name);
      int iScheme = getColorScheme(name, false, isOverloaded);
      if (isOverloaded)
        switch (iScheme) {
        case BW:
          paletteBW = getPaletteBW();
          break;
        case WB:
          paletteWB = getPaletteWB();
          break;
        case ROYGB:
        case BGYOR:
          argbsRoygb = JmolConstants.argbsRoygbScale;
          break;
        case RWB:
        case BWR:
          argbsRwb = JmolConstants.argbsRwbScale;
          break;
        case JMOL:
          argbsCpk = EnumPalette.argbsCpk;
          break;
        case RASMOL:
          getRasmolScale();
          break;
        case AMINO:
          argbsAmino = JmolConstants.argbsAmino;
          break;
        case SHAPELY:
          argbsShapely = JmolConstants.argbsShapely;
          break;
        }
      return (iScheme == Integer.MAX_VALUE ? ROYGB : iScheme);
    }
    schemes.put(name, scale);
    setThisScheme(name, scale);
    int iScheme = getColorScheme(name, false, isOverloaded);
    if (isOverloaded)
      switch (iScheme) {
      case BW:
        paletteBW = thisScale;
        break;
      case WB:
        paletteWB = thisScale;
        break;
      case ROYGB:
      case BGYOR:
        argbsRoygb = thisScale;
        ihalf = argbsRoygb.length / 3;
        break;
      case RWB:
      case BWR:
        argbsRwb = thisScale;
        break;
      case JMOL:
        argbsCpk = thisScale;
        break;
      case RASMOL:
        break;
      case AMINO:
        argbsAmino = thisScale;
        break;
      case SHAPELY:
        argbsShapely = thisScale;
        break;
      }
    return CUSTOM;
  }

  /**
   * 
   * @param colorScheme    name or name= or name=[x......] [x......] ....
   * @param defaultToRoygb
   * @param isOverloaded
   * @return paletteID
   */
  public int getColorScheme(String colorScheme,
                                          boolean defaultToRoygb,
                                          boolean isOverloaded) {
    // main method for creating a new scheme or modifying an old one
    // ScriptmathProcessor.evaluateColor
    // makeColorScheme
    // setColorScheme
    // ColorManager.getColorSchemeList
    // ColorManager.setColorScheme
    // ColorManager.setCurrentColorRange
    
    colorScheme = colorScheme.toLowerCase();
    
    
    // check for "name = [x...] [x...] ..." 
    // or "[x...] [x...] ..."
    int pt = Math.max(colorScheme.indexOf("=")
        , colorScheme.indexOf("["));
    if (pt >= 0) {
      String name = TextFormat.replaceAllCharacters(colorScheme
          .substring(0, pt), " =", "");
      if (name.length() > 0)
        isOverloaded = true;
      int n = 0;
      if (colorScheme.length() > pt + 1 && !colorScheme.contains("[")) {
        // also allow xxx=red,blue,green
        
        colorScheme = "[" + colorScheme.substring(pt + 1).trim() + "]";
        colorScheme = TextFormat.simpleReplace(colorScheme.replace('\n', ' '), "  ", " ");
        colorScheme = TextFormat.simpleReplace(colorScheme, ", ", ",").replace(' ',',');
        colorScheme = TextFormat.simpleReplace(colorScheme, ",", "][");
      }
      pt = -1;
      while ((pt = colorScheme.indexOf("[", pt + 1)) >= 0)
        n++;
      // if just "name=", then we overload it with no scale -- which will clear it
      if (n == 0)
        return makeColorScheme(name, null, isOverloaded);
      
      // create the scale -- error returns ROYGB
      
      int[] scale = new int[n];
      n = 0;
      while ((pt = colorScheme.indexOf("[", pt + 1)) >= 0) {
        int pt2 = colorScheme.indexOf("]", pt);
        if (pt2 < 0)
          pt2 = colorScheme.length() - 1;
        int c = ColorUtil.getArgbFromString(colorScheme.substring(pt,
            pt2 + 1));
        if (c == 0) // try without the brackets
          c = ColorUtil.getArgbFromString(colorScheme.substring(pt + 1, pt2).trim());        
        if (c == 0) {
          Logger.error("error in color value: "
              + colorScheme.substring(pt, pt2 + 1));
          return ROYGB;
        }
        scale[n++] = c;
      }
      
      // set the user scale if that is what this is
      
      if (name.equals("user")) {
        setUserScale(scale);
        return USER;
      }
      
      // otherwise, make a new scheme for it with the specified scale, which will NOT be null
      
      return makeColorScheme(name, scale, isOverloaded);
    }
    
    // wasn't a definition. 
    
    colorScheme = fixName(colorScheme);
    int ipt = getSchemeIndex(colorScheme);
    if (schemes.containsKey(colorScheme)) {
      setThisScheme(colorScheme, schemes.get(colorScheme));
      return ipt; // -1 means custom -- use "thisScale", otherwise a scheme number
    }
    
    // return a positive value for a known scheme or ROYGB if a default is ok, or MAX_VALUE
    return (ipt != CUSTOM ? ipt : defaultToRoygb ? ROYGB 
        : Integer.MAX_VALUE);
  }

  public void setUserScale(int[] scale) {
    // getColorScheme
    // ColorManager.setUserScale
    propertyColorEncoder.userScale = scale;  
    makeColorScheme("user", scale, false);
  }
  
  public int[] getColorSchemeArray(int palette) {
    // ColorManager.getColorSchemeList
    int[] b;
    switch (palette) {
    /*    case RGB:
     c = quantizeRgb(val, lo, hi, rgbRed, rgbGreen, rgbBlue);
     break;
     */
    case CUSTOM:
      return thisScale;      
    case ROYGB:
      return propertyColorEncoder.argbsRoygb;
    case BGYOR:
      return ArrayUtil.arrayCopy(propertyColorEncoder.argbsRoygb, 0, -1, true);
    case LOW:
      return ArrayUtil.arrayCopy(propertyColorEncoder.argbsRoygb, 0, propertyColorEncoder.ihalf, false);
    case HIGH:
      int[] a = ArrayUtil.arrayCopy(propertyColorEncoder.argbsRoygb, propertyColorEncoder.argbsRoygb.length - 2 * propertyColorEncoder.ihalf, -1, false);
      b = new int[propertyColorEncoder.ihalf];
      for (int i = b.length, j = a.length; --i >= 0 && --j >= 0;)
        b[i] = a[j--];
      return b;
    case BW:
      return getPaletteBW();
    case WB:
      return getPaletteWB();
    case RWB:
      return propertyColorEncoder.argbsRwb;
    case BWR:
      return ArrayUtil.arrayCopy(propertyColorEncoder.argbsRwb, 0, -1, true);
    case JMOL:
      return propertyColorEncoder.argbsCpk;
    case RASMOL:
      return getRasmolScale();
    case SHAPELY:
      return propertyColorEncoder.argbsShapely;
    case AMINO:
      return propertyColorEncoder.argbsAmino;
    case USER:
      return propertyColorEncoder.userScale;
    case RESU:
      return ArrayUtil.arrayCopy(propertyColorEncoder.userScale, 0, -1, true);
    default:
      return null;
    }

  }
  
  public short getColorIndexFromPalette(float val, float lo,
                                                     float hi, int palette,
                                                     boolean isTranslucent) {
    short colix = Colix.getColix(getArgbFromPalette(val, lo, hi, palette));
    if (isTranslucent) {
      float f = (hi - val) / (hi - lo); 
      if (f > 1)
        f = 1; // transparent
      else if (f < 0.125f) // never fully opaque
        f = 0.125f;
      colix = Colix.getColixTranslucent(colix, true, f);
    }
    return colix;
  }

  public int getPaletteColorCount(int palette) {
    switch (palette) {
    case CUSTOM:
      return thisScale.length;
    case BW:
    case WB:
      getPaletteBW();
      return propertyColorEncoder.paletteBW.length;
    case ROYGB:
    case BGYOR:
      return propertyColorEncoder.argbsRoygb.length;
    case LOW:
    case HIGH:
      return propertyColorEncoder.ihalf;
    case RWB:
    case BWR:
      return propertyColorEncoder.argbsRwb.length;
    case USER:
    case RESU:
      return propertyColorEncoder.userScale.length;
    case JMOL:
      return argbsCpk.length;
    case RASMOL:
      return getRasmolScale().length;
    case SHAPELY:
      return propertyColorEncoder.argbsShapely.length;
    case AMINO:
      return propertyColorEncoder.argbsAmino.length;
    default:
      return 0;
    }
  }
  
  public int getArgbFromPalette(float val, float lo, float hi, int palette) {
    if (Float.isNaN(val))
      return GRAY;
    int n = getPaletteColorCount(palette);
    switch (palette) {
    case CUSTOM:
      if (isColorIndex) {
        lo = 0;
        hi = thisScale.length;
      }
      return thisScale[quantize(val, lo, hi, n)];
    case BW:
      return getPaletteBW()[quantize(val, lo, hi, n)];
    case WB:
      return getPaletteWB()[quantize(val, lo, hi, n)];
    case ROYGB:
      return propertyColorEncoder.argbsRoygb[quantize(val, lo, hi, n)];
    case BGYOR:
      return propertyColorEncoder.argbsRoygb[quantize(-val, -hi, -lo, n)];
    case LOW:
      return propertyColorEncoder.argbsRoygb[quantize(val, lo, hi, n)];
    case HIGH:
      return propertyColorEncoder.argbsRoygb[propertyColorEncoder.ihalf + quantize(val, lo, hi, n) * 2];
    case RWB:
      return propertyColorEncoder.argbsRwb[quantize(val, lo, hi, n)];
    case BWR:
      return propertyColorEncoder.argbsRwb[quantize(-val, -hi, -lo, n)];
    case USER:
      return (propertyColorEncoder.userScale.length == 0 ? GRAY : propertyColorEncoder.userScale[quantize(val, lo, hi, n)]);
    case RESU:
      return (propertyColorEncoder.userScale.length == 0 ? GRAY : propertyColorEncoder.userScale[quantize(-val, -hi, -lo, n)]);
    case JMOL:
      return propertyColorEncoder.argbsCpk[colorIndex((int)val, n)];
    case RASMOL:
      return getRasmolScale()[colorIndex((int)val, n)];
    case SHAPELY:
      return propertyColorEncoder.argbsShapely[colorIndex((int)val, n)];
    case AMINO:
      return propertyColorEncoder.argbsAmino[colorIndex((int)val, n)];
    default:
      return GRAY;
    }
  }

  private void setThisScheme(String name, int[] scale) {
    thisName = name;
    thisScale = scale;
    if (name.equals("user"))
      userScale = scale;
    isColorIndex = (name.indexOf(BYELEMENT_PREFIX) == 0 
        || name.indexOf(BYRESIDUE_PREFIX) == 0);
  }

  
  // nonstatic methods:
  
  public int getArgb(float val) {
    return (isReversed ? getArgbFromPalette(-val, -hi, -lo, currentPalette)
        : getArgbFromPalette(val, lo, hi, currentPalette));
  }
  
  public short getColorIndex(float val) {
    return (isReversed ? getColorIndexFromPalette(-val, -hi, -lo, currentPalette, isTranslucent)
        : getColorIndexFromPalette(val, lo, hi, currentPalette, isTranslucent));
  }

  public Map<String, Object> getColorKey() {
    Map<String, Object> info = new Hashtable<String, Object>();
    int segmentCount = getPaletteColorCount(currentPalette);
    List<Point3f> colors = new ArrayList<Point3f>(segmentCount);
/*    
    boolean isReverse = isReversed;
    
    switch (currentPalette) {
    case BGYOR:
    case BWR:
    case RESU:
      isReverse = !isReverse;
      break;
    }
    */
    float[] values = new float[segmentCount + 1];
    float quantum = (hi - lo) / segmentCount;
    float f = quantum * (isReversed ? -0.5f : 0.5f);

    for (int i = 0; i < segmentCount; i++) {
      values[i] = (isReversed ? hi - i * quantum : lo + i * quantum);
      colors.add(ColorUtil.colorPointFromInt2(getArgb(values[i] + f)));
    }
    values[segmentCount] = (isReversed ? lo : hi);
    info.put("values", values);
    info.put("colors", colors);
    info.put("min", Float.valueOf(lo));
    info.put("max", Float.valueOf(hi));
    info.put("reversed", Boolean.valueOf(isReversed));
    info.put("name", getColorSchemeName());
    return info;
  }

  /**
   * 
   * @param colorScheme
   * @param isTranslucent
   */
  public void setColorScheme(String colorScheme, boolean isTranslucent) {
    this.isTranslucent = isTranslucent;
    if (colorScheme != null)
      currentPalette = getColorScheme(colorScheme, true, false);
  }

  public void setRange(float lo, float hi, boolean isReversed) {
    if (hi == Float.MAX_VALUE) {
      lo = 1; 
      hi = getPaletteColorCount(currentPalette) + 1;
    }
    this.lo = Math.min(lo, hi);
    this.hi = Math.max(lo, hi);
    this.isReversed = isReversed;
  }
  
  public String getColorSchemeName() {
    return getColorSchemeName(currentPalette);  
  }
  
  public String getColorSchemeName(int i) {
    int absi = Math.abs(i);
    return (i == CUSTOM ? thisName : absi < colorSchemes.length && absi >= 0 ? colorSchemes[absi] : null);  
  }

  // legitimate static methods:
  
  public final static String getColorSchemeList(int[] scheme) {
    if (scheme == null)
      return "";
    String colors = "";
    for (int i = 0; i < scheme.length; i++)
      colors += (i == 0 ? "" : " ") + Escape.escapeColor(scheme[i]);
    return colors;
  }

  public final static synchronized int[] getRasmolScale() {
    if (rasmolScale != null)
      return rasmolScale;
    rasmolScale = new int[EnumPalette.argbsCpk.length];
    int argb = EnumPalette.argbsCpkRasmol[0] | 0xFF000000;
    for (int i = rasmolScale.length; --i >= 0;)
      rasmolScale[i] = argb;
    for (int i = EnumPalette.argbsCpkRasmol.length; --i >= 0;) {
      argb = EnumPalette.argbsCpkRasmol[i];
      rasmolScale[argb >> 24] = argb | 0xFF000000;
    }
    return rasmolScale;
  }

  private int[] getPaletteWB() {
    if (propertyColorEncoder.paletteWB != null) 
      return propertyColorEncoder.paletteWB;
    int[] b = new int[JmolConstants.argbsRoygbScale.length];
    for (int i = 0; i < b.length; i++) {
      float xff = (1f / b.length * (b.length - i));        
      b[i] = ColorUtil.colorTriadToInt(xff, xff, xff);
    }
    return propertyColorEncoder.paletteWB = b;
  }

  public static int[] getPaletteAtoB(int color1, int color2, int n) {
    if (n < 2)
      n = JmolConstants.argbsRoygbScale.length;
    int[] b = new int[n];
    float red1 = (((color1 & 0xFF0000) >> 16) & 0xFF) / 255f;
    float green1 = (((color1 & 0xFF00) >> 8) & 0xFF) / 255f;
    float blue1 = (color1 & 0xFF) / 255f;
    float red2 = (((color2 & 0xFF0000) >> 16) & 0xFF) / 255f;
    float green2 = (((color2 & 0xFF00) >> 8) & 0xFF) / 255f;
    float blue2 = (color2 & 0xFF) / 255f;
    float dr = (red2 - red1) / (n - 1);
    float dg = (green2 - green1) / (n - 1);
    float db = (blue2 - blue1) / (n - 1);
    for (int i = 0; i < n; i++)
      b[i] = ColorUtil.colorTriadToInt(red1 + dr * i, green1 + dg * i, blue1
          + db * i);
    return b;
  }
  private int[] getPaletteBW() {
    if (propertyColorEncoder.paletteBW != null) 
      return propertyColorEncoder.paletteBW;
    int[] b = new int[JmolConstants.argbsRoygbScale.length];
    for (int i = 0; i < b.length; i++) {
      float xff = (1f / b.length * i); 
      b[i] = ColorUtil.colorTriadToInt(xff, xff, xff);
    }
    return propertyColorEncoder.paletteBW = b;
  }

  /**
   * gets the value at the color boundary for this color range fraction 
   * @param x
   * @param isLowEnd
   * @return quantized value
   */
  public float quantize(float x, boolean isLowEnd) {
    int n = getPaletteColorCount(currentPalette);
    x = (((int) (x * n)) + (isLowEnd ? 0f : 1f)) / n;
    return (x <= 0 ? lo : x >= 1 ? hi : lo + (hi - lo) * x);
  }
  
  public final static int quantize(float val, float lo, float hi, int segmentCount) {
    /* oy! Say you have an array with 10 values, so segmentCount=10
     * then we expect 0,1,2,...,9  EVENLY
     * If f = fractional distance from lo to hi, say 0.0 to 10.0 again,
     * then one might expect 10 even placements. BUT:
     * (int) (f * segmentCount + 0.5) gives
     * 
     * 0.0 ---> 0
     * 0.5 ---> 1
     * 1.0 ---> 1
     * 1.5 ---> 2
     * 2.0 ---> 2
     * ...
     * 8.5 ---> 9
     * 9.0 ---> 9
     * 9.5 ---> 10 --> 9
     * 
     * so the first bin is underloaded, and the last bin is overloaded.
     * With integer quantities, one would not notice this, because
     * 0, 1, 2, 3, .... --> 0, 1, 2, 3, .....
     * 
     * but with fractional quantities, it will be noticeable.
     * 
     * What we really want is:
     * 
     * 0.0 ---> 0
     * 0.5 ---> 0
     * 1.0 ---> 1
     * 1.5 ---> 1
     * 2.0 ---> 2
     * ...
     * 8.5 ---> 8
     * 9.0 ---> 9
     * 9.5 ---> 9
     * 
     * that is, no addition of 0.5. 
     * Instead, I add 0.0001, just for discreteness sake.
     * 
     * Bob Hanson, 5/2006
     * 
     */
    float range = hi - lo;
    if (range <= 0 || Float.isNaN(val))
      return segmentCount / 2;
    float t = val - lo;
    if (t <= 0)
      return 0;
    float quanta = range / segmentCount;
    int q = (int)(t / quanta + 0.0001f);  //was 0.5f!
    if (q >= segmentCount)
      q = segmentCount - 1;
    return q;
  }

  private final static int colorIndex(int q, int segmentCount) {
    return (q <= 0 | q >= segmentCount ? 0 : q);
  }
/*  
  static {
    for (int i = 0; i < 10; i++) {
      System.out.println(i + " " + quantize(i, 0, 10, 10));
    }
    for (int i = -10; i < 0; i++) {
      System.out.println((i) + " " + quantize(i, -10, 0, 10));
    }
    System.out.println("ColorEncoder test");
  }
*/

  public int getState(StringBuffer s) {
    int n = 0;
    for (Map.Entry<String, int[]> entry : schemes.entrySet()) {
      String name = entry.getKey();
      if (name.length() > 0 & n++ >= 0)
        s.append("color \"" + name + "="
            + getColorSchemeList(entry.getValue()) + "\";\n");
    }
    return n;
  }

  public String getColorScheme() {
    return (isTranslucent ? "translucent " : "")
        + (currentPalette < 0 ? getColorSchemeList(getColorSchemeArray(currentPalette))
            : getColorSchemeName(currentPalette));
  }

  static public void RGBtoHSL(float r, float g, float b, float[] ret) {
    //http://tips4java.wordpress.com/2009/07/05/hsl-color/
 
    //  Get RGB values in the range 0 - 1

   
      //  Minimum and Maximum RGB values are used in the HSL calculations

      r /= 255;
      g /= 255;
      b /= 255;
      if (r > 1)
        r = 1;
      if (g > 1)
        g = 1;
      if (b > 1)
        b = 1;
      float min = Math.min(r, Math.min(g, b));
      float max = Math.max(r, Math.max(g, b));

      //  Calculate the Hue

      float h = 0;

      if (max == min)
        h = 0;
      else if (max == r)
        h = ((60 * (g - b) / (max - min)) + 360) % 360;
      else if (max == g)
        h = (60 * (b - r) / (max - min)) + 120;
      else if (max == b)
        h = (60 * (r - g) / (max - min)) + 240;

      //  Calculate the Luminance

      float l = (max + min) / 2;

      //  Calculate the Saturation

      float s = 0;

      if (max == min)
        s = 0;
      else if (l <= .5f)
        s = (max - min) / (max + min);
      else
        s = (max - min) / (2 - max - min);
      
      //if (s == 1 && l != 0.5f) {
      //    s = Float.NaN;
      // }
      
      ret[0] = h / 360;
      ret[1] = s;
      ret[2] = l;
    }


}
