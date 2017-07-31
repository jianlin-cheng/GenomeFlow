/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-09-13 11:14:50 -0500 (Thu, 13 Sep 2012) $
 * $Revision: 17560 $
 *
 * Copyright (C) 2003-2005  Miguel, Jmol Development, www.jmol.org
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

import org.jmol.constant.EnumPalette;

/**
 *<p>
 * Implements a color index model using a colix as a
 * <strong>COLor IndeX</strong>.
 *</p>
 *<p>
 * A colix is a color index represented as a short int.
 *</p>
 *<p>
 * The value 0 is considered a null value ... for no color. In Jmol this
 * generally means that the value is inherited from some other object.
 *</p>
 *<p>
 * The value 1 is used to indicate that color only is to be inherited. 
 * 
 * 0x0001 INHERIT_OPAQUE -- opaque, but with the color coming from the parent.
 * 0x4001 INHERIT_TRANSLUCENT -- translucent but with the color coming from the parent.
 * 
 * The value 2 is used to indicate that one of the palettes is to be used. 
 * 
 * 0x0002 PALETTE, opaque
 * 0x4002 PALETTE, translucent
 * 
 * Palettes themselves are coded separately in a Palette ID that is tracked with
 *</p>
 *
 * @author Miguel, miguel@jmol.org 
 */

public class Colix {

  /* ***************************************************************
   * color indexes -- colix
   * ***************************************************************/

  /* entries 0 and 1 are reserved and are special inheritance
     0 INHERIT_ALL inherits both color and translucency
     1 INHERIT_COLOR is used to inherit just the color
     
     
     0x8000 changeable flag (elements and isotopes, about 200; negative)
     0x7800 translucent flag set

     NEW:
     0x0000 translucent level 0  (opaque)
     0x0800 translucent level 1
     0x1000 translucent level 2
     0x1800 translucent level 3
     0x2000 translucent level 4
     0x2800 translucent level 5
     0x3000 translucent level 6
     0x3800 translucent level 7
     0x4000 translucent level 8 (invisible)

     0x0000 inherit color and translucency
     0x0001 inherit color; translucency determined by mask     
     0x0002 special palette ("group", "structure", etc.); translucency by mask

     Note that inherited colors and special palettes are not handled here. 
     They could be anything, including totally variable quantities such as 
     distance to an object. So there are two stages of argb color determination
     from a colix. The special palette flag is only used transiently - just to
     indicate that the color selected isn't a known color. The actual palette-based
     colix is saved here, and and the atom or shape's byte paletteID is set as well.
     
     Shapes/ColorManager: responsible for assigning argb colors based on 
     color palettes. These argb colors are then used directly.
     
     Graphics3D: responsible for "system" colors and caching of user-defined rgbs.
     
     
     
     0x0004 black...
       ....
     0x0017  ...gold
     0x00?? additional colors used from JavaScript list or specified by user
     
     0x0177 last available colix

     Bob Hanson 3/2007
     
  */

  public final static short INHERIT_ALL = 0;
  public final static short INHERIT_COLOR = 1;
  public final static short USE_PALETTE = 2;
  public final static short RAW_RGB = 3;
  public final static short SPECIAL_COLIX_MAX = 4;

  private static int colixMax = SPECIAL_COLIX_MAX;
  private static int[] argbs = new int[128];
  private static int[] argbsGreyscale;
  private static int[][] ashades = new int[128][];
  private static int[][] ashadesGreyscale;
  private static final Int2IntHash colixHash = new Int2IntHash();
  private static final int RAW_RGB_INT = RAW_RGB;
  public final static short UNMASK_CHANGEABLE_TRANSLUCENT = 0x07FF;
  public final static short CHANGEABLE_MASK = (short) 0x8000; // negative
  public final static int LAST_AVAILABLE_COLIX = UNMASK_CHANGEABLE_TRANSLUCENT;
  public final static int TRANSLUCENT_SHIFT = 11;
  public final static int ALPHA_SHIFT = 24 - TRANSLUCENT_SHIFT;
  public final static int TRANSLUCENT_MASK = 0xF << TRANSLUCENT_SHIFT; //0x7800
  public final static int TRANSLUCENT_SCREENED = TRANSLUCENT_MASK;
  public final static int TRANSPARENT = 8 << TRANSLUCENT_SHIFT; //0x4000
  public final static int TRANSLUCENT_50 = 4 << TRANSLUCENT_SHIFT; //0x2000
  public final static short OPAQUE_MASK = ~TRANSLUCENT_MASK;

  public final static short BLACK = 4;
  public final static short ORANGE = 5;
  public final static short PINK = 6;
  public final static short BLUE = 7;
  public final static short WHITE = 8;
  public final static short CYAN = 9;
  public final static short RED = 10;
  public final static short GREEN = 11;
  public final static short GRAY = 12;
  public final static short SILVER = 13;
  public final static short LIME = 14;
  public final static short MAROON = 15;
  public final static short NAVY = 16;
  public final static short OLIVE = 17;
  public final static short PURPLE = 18;
  public final static short TEAL = 19;
  public final static short MAGENTA = 20;
  public final static short YELLOW = 21;
  public final static short HOTPINK = 22;
  public final static short GOLD = 23;

  public Colix() {
  }
  
  public static short getColix(int argb) {
    if (argb == 0)
      return 0;
    int translucentFlag = 0;
    // in JavaScript argb & 0xFF000000 will be a long
    if ((argb & 0xFF000000) != (0xFF000000 & 0xFF000000)) {
      argb |= 0xFF000000;
      translucentFlag = TRANSLUCENT_50;
    }
    int c = colixHash.get(argb);
    if ((c & RAW_RGB_INT) == RAW_RGB_INT)
      translucentFlag = 0;
    return (c > 0 ? (short) (c | translucentFlag)
        : (short) (allocateColix(argb) | translucentFlag));
  }

  public synchronized static int allocateColix(int argb) {
    // double-check to make sure that someone else did not allocate
    // something of the same color while we were waiting for the lock
    // in JavaScript argb & 0xFF000000 will be a long
    if ((argb & 0xFF000000) != (0xFF000000 & 0xFF000000))
      throw new IndexOutOfBoundsException();
    for (int i = colixMax; --i >= SPECIAL_COLIX_MAX; )
      if (argb == argbs[i])
        return (short)i;
    if (colixMax == argbs.length) {
      int oldSize = colixMax;
      int newSize = oldSize * 2;
      if (newSize > LAST_AVAILABLE_COLIX + 1)
        newSize = LAST_AVAILABLE_COLIX + 1;
      int[] t0 = new int[newSize];
      System.arraycopy(argbs, 0, t0, 0, oldSize);
      argbs = t0;

      if (argbsGreyscale != null) {
        t0 = new int[newSize];
        System.arraycopy(argbsGreyscale, 0, t0, 0, oldSize);
        argbsGreyscale = t0;
      }

      int[][] t2 = new int[newSize][];
      System.arraycopy(ashades, 0, t2, 0, oldSize);
      ashades = t2;

      if (ashadesGreyscale != null) {
        t2 = new int[newSize][];
        System.arraycopy(ashadesGreyscale, 0, t2, 0, oldSize);
        ashadesGreyscale = t2;
      }
    }
    argbs[colixMax] = argb;
    //System.out.println("Colix "+colixMax + " = "+Integer.toHexString(argb) + " " + argb);
    if (argbsGreyscale != null)
      argbsGreyscale[colixMax] = ColorUtil.calcGreyscaleRgbFromRgb(argb);
    colixHash.put(argb, colixMax);
    return (colixMax < LAST_AVAILABLE_COLIX ? colixMax++ : colixMax);
  }

  private synchronized static void calcArgbsGreyscale() {
    if (argbsGreyscale != null)
      return;
    int[] a = new int[argbs.length];
    for (int i = argbs.length; --i >= SPECIAL_COLIX_MAX;)
      a[i] = ColorUtil.calcGreyscaleRgbFromRgb(argbs[i]);
    argbsGreyscale = a;
  }

  public final static int getArgbGreyscale(short colix) {
    if (argbsGreyscale == null)
      calcArgbsGreyscale();
    return argbsGreyscale[colix & OPAQUE_MASK];
  }

  public final static int[] getShades(int argb, boolean asGrey) {
    if (asGrey) {
      if (argbsGreyscale == null)
        calcArgbsGreyscale();
      argbsGreyscale[LAST_AVAILABLE_COLIX] = ColorUtil.calcGreyscaleRgbFromRgb(argb);
    }
    return ashades[LAST_AVAILABLE_COLIX] = Shader.getShades(argb, false);
  }

  public final static int[] getShades(short colix) {
    colix &= OPAQUE_MASK;
    int[] shades = ashades[colix];
    if (shades == null)
      shades = ashades[colix] = Shader.getShades(argbs[colix], false);
    return shades;
  }

  public final static int[] getShadesGreyscale(short colix) {
    colix &= OPAQUE_MASK;
    if (ashadesGreyscale == null)
      ashadesGreyscale = new int[ashades.length][];
    int[] shadesGreyscale = ashadesGreyscale[colix];
    if (shadesGreyscale == null)
      shadesGreyscale = ashadesGreyscale[colix] =
        Shader.getShades(argbs[colix], true);
    return shadesGreyscale;
  }

  public final synchronized static void flushShades() {
    for (int i = colixMax; --i >= 0; )
      ashades[i] = null;
    Shader.sphereShadingCalculated = false;
  }

  /*
  Int2IntHash hashMix2 = new Int2IntHash(32);

  short getColixMix(short colixA, short colixB) {
    if (colixA == colixB)
      return colixA;
    if (colixA <= 0)
      return colixB;
    if (colixB <= 0)
      return colixA;
    int translucentMask = colixA & colixB & TRANSLUCENT_MASK;
    colixA &= ~TRANSLUCENT_MASK;
    colixB &= ~TRANSLUCENT_MASK;
    int mixId = ((colixA < colixB)
                 ? ((colixA << 16) | colixB)
                 : ((colixB << 16) | colixA));
    int mixed = hashMix2.get(mixId);
    if (mixed == Integer.MIN_VALUE) {
      int argbA = argbs[colixA];
      int argbB = argbs[colixB];
      int r = (((argbA & 0x00FF0000)+(argbB & 0x00FF0000)) >> 1) & 0x00FF0000;
      int g = (((argbA & 0x0000FF00)+(argbB & 0x0000FF00)) >> 1) & 0x0000FF00;
      int b = (((argbA & 0x000000FF)+(argbB & 0x000000FF)) >> 1);
      int argbMixed = 0xFF000000 | r | g | b;
      mixed = getColix(argbMixed);
      hashMix2.put(mixId, mixed);
    }
    return (short)(mixed | translucentMask);
  }
  */
  
  public final static int[] predefinedArgbs = {
    0xFF000000, // black
    0xFFFFA500, // orange
    0xFFFFC0CB, // pink
    0xFF0000FF, // blue
    0xFFFFFFFF, // white
    0xFF00FFFF, // cyan
    0xFFFF0000, // red
    0xFF008000, // green -- really!
    0xFF808080, // gray
    0xFFC0C0C0, // silver
    0xFF00FF00, // lime  -- no kidding!
    0xFF800000, // maroon
    0xFF000080, // navy
    0xFF808000, // olive
    0xFF800080, // purple
    0xFF008080, // teal
    0xFFFF00FF, // magenta
    0xFFFFFF00, // yellow
    0xFFFF69B4, // hotpink
    0xFFFFD700, // gold
  };

  static {
    for (int i = 0; i < predefinedArgbs.length; ++i)
      getColix(predefinedArgbs[i]);
  }

  public static short getColix(Object obj) {
    if (obj == null)
      return INHERIT_ALL;
    if (obj instanceof EnumPalette)
      return (((EnumPalette) obj) == EnumPalette.NONE ? INHERIT_ALL
          : USE_PALETTE);
    if (obj instanceof Integer)
      return getColix(((Integer) obj).intValue());
    if (obj instanceof String)
      return getColix((String) obj);
    if (obj instanceof Byte)
      return (((Byte) obj).byteValue() == 0 ? INHERIT_ALL : USE_PALETTE);
    if (Logger.debugging) {
      Logger.debug("?? getColix(" + obj + ")");
    }
    return HOTPINK;
  }

  public final static short applyColorTranslucencyLevel(short colix,
                                                         float translucentLevel) {
    // 0.0 to 1.0 ==> MORE translucent   
    //                 1/8  1/4 3/8 1/2 5/8 3/4 7/8 8/8
    //     t            32  64  96  128 160 192 224 255 or 256
    //     t >> 5        1   2   3   4   5   6   7   8
    //     (t >> 5) + 1  2   3   4   5   6   7   8   9 
    // 15 is reserved for screened, so 9-14 just map to 9, "invisible"
  
    if (translucentLevel == 0) //opaque
      return (short) (colix & ~TRANSLUCENT_MASK);
    if (translucentLevel < 0) //screened
      return (short) (colix & ~TRANSLUCENT_MASK | TRANSLUCENT_SCREENED);
    if (Float.isNaN(translucentLevel) || translucentLevel >= 255
        || translucentLevel == 1.0)
      return (short) ((colix & ~TRANSLUCENT_MASK) | TRANSPARENT);
    int iLevel = (int) (translucentLevel < 1 ? translucentLevel * 256
        : translucentLevel <= 9 ? ((int) (translucentLevel - 1)) << 5
            : translucentLevel < 15 ? 8 << 5 : translucentLevel);
    iLevel = (iLevel >> 5) % 16;
    return (short) (colix & ~TRANSLUCENT_MASK | (iLevel << TRANSLUCENT_SHIFT));
  }

  public static boolean isColixLastAvailable(short colix) {
    return (colix > 0 && (colix & UNMASK_CHANGEABLE_TRANSLUCENT) == UNMASK_CHANGEABLE_TRANSLUCENT);
  }

  public static int getArgb(short colix) {
    return argbs[colix & OPAQUE_MASK];
  }

  public final static boolean isColixColorInherited(short colix) {
    switch (colix) {
    case INHERIT_ALL:
    case INHERIT_COLOR:
      return true;
    default: //could be translucent of some sort
      return (colix & OPAQUE_MASK) == INHERIT_COLOR;
    }
  }

  public final static short getColixInherited(short myColix, short parentColix) {
    switch (myColix) {
    case INHERIT_ALL:
      return parentColix;
    case INHERIT_COLOR:
      return (short) (parentColix & OPAQUE_MASK);
    default:
      //check this colix irrespective of translucency, and if inherit, then
      //it must be inherit color but not translucent level; 
      return ((myColix & OPAQUE_MASK) == INHERIT_COLOR ? (short) (parentColix
          & OPAQUE_MASK | myColix & TRANSLUCENT_MASK) : myColix);
    }
  }

  public final static boolean isColixTranslucent(short colix) {
    return ((colix & TRANSLUCENT_MASK) != 0);
  }

  public final static short getChangeableColixIndex(short colix) {
    return (colix >= 0 ? -1 : (short) (colix & UNMASK_CHANGEABLE_TRANSLUCENT));
  }

  public final static short getColixTranslucent(short colix,
                                                boolean isTranslucent,
                                                float translucentLevel) {
    if (colix == INHERIT_ALL)
      colix = INHERIT_COLOR;
    colix &= ~TRANSLUCENT_MASK;
    return (isTranslucent ? applyColorTranslucencyLevel(colix, translucentLevel)
        : colix);
  }

  public final static short copyColixTranslucency(short colixFrom, short colixTo) {
    return getColixTranslucent(colixTo, isColixTranslucent(colixFrom),
        getColixTranslucencyLevel(colixFrom));
  }

  public static float getColixTranslucencyFractional(short colix) {
    int translevel = getColixTranslucencyLevel(colix);
    return (translevel == -1 ? 0.5f : translevel == 0 ? 0
        : translevel == 255 ? 1 : translevel / 256f);
  }

  public final static int getColixTranslucencyLevel(short colix) {
    int logAlpha = (colix >> TRANSLUCENT_SHIFT) & 0xF;
    switch (logAlpha) {
    case 0:
      return 0;
    case 1: //  32
    case 2: //  64
    case 3: //  96
    case 4: // 128
    case 5: // 160
    case 6: // 192
    case 7: // 224
      return logAlpha << 5;
    case 15:
      return -1;
    default:
      return 255;
    }
  }

  public static short getColix(String colorName) {
    int argb = ColorUtil.getArgbFromString(colorName);
    if (argb != 0)
      return getColix(argb);
    if ("none".equalsIgnoreCase(colorName))
      return INHERIT_ALL;
    if ("opaque".equalsIgnoreCase(colorName))
      return INHERIT_COLOR;
    return USE_PALETTE;
  }

  public static short[] getColixArray(String colorNames) {
    if (colorNames == null || colorNames.length() == 0)
      return null;
    String[] colors = Parser.getTokens(colorNames);
    short[] colixes = new short[colors.length];
    for (int j = 0; j < colors.length; j++) {
      colixes[j] = getColix(ColorUtil.getArgbFromString(colors[j]));
      if (colixes[j] == 0)
        return null;
    }
    return colixes;
  }

  public static String getHexCode(short colix) {
    return Escape.escapeColor(getArgb(colix));
  }

  public static String getHexCodes(short[] colixes) {
    if (colixes == null)
      return null;
    StringBuffer s = new StringBuffer();
    for (int i = 0; i < colixes.length; i++)
      s.append(i == 0 ? "" : " ").append(getHexCode(colixes[i]));
    return s.toString();
  }

  public static short getColixTranslucent(int argb) {
    int a = (argb >> 24) & 0xFF;
    if (a == 0xFF)
      return getColix(argb);
    return getColixTranslucent(getColix(argb), true, a / 255f);
  }

}
