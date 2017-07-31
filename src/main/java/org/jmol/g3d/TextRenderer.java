/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-09-03 16:27:33 -0500 (Mon, 03 Sep 2012) $
 * $Revision: 17501 $
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

package org.jmol.g3d;

import java.util.Hashtable;
import java.util.Map;

import org.jmol.api.JmolRendererInterface;
import org.jmol.util.JmolFont;


/**
 * implementation for text rendering
 *<p>
 * uses java fonts by rendering into an offscreen buffer.
 * strings are rasterized and stored as a bitmap in an int[].
 *<p>
 * needs work
 *
 *
 * @author Miguel, miguel@jmol.org
 */ 
class TextRenderer {
  /*
    we have a few problems here
    a message is probably going to vary in size with z depth
    a message is probably going to be repeated by more than one atom
    fonts?
      just one?
      a restricted number?
      any font?
      if we want to support more than one then a fontindex is probably
      called for in order to prevent a hashtable lookup
    color - can be applied by the painter
    rep
      array of booleans - uncompressed
      array of bits - uncompressed - i like this
      some type of run-length, using bytes
  */
  private int height; // this height is just ascent + descent ... no reason for leading
  private int ascent;
  private int width;
  private int mapWidth;
  private int size;
  private int[] bitmap;
  private boolean isInvalid;
  private static boolean working;
  private final static Map<JmolFont, Map<String, TextRenderer>> htFont3d = new Hashtable<JmolFont, Map<String, TextRenderer>>();
  private final static Map<JmolFont, Map<String, TextRenderer>> htFont3dAntialias = new Hashtable<JmolFont, Map<String, TextRenderer>>();

  synchronized static void clearFontCache() {
    if (working)
      return;
    htFont3d.clear();
    htFont3dAntialias.clear();
  }

  static int plot(int x, int y, int z, int argb, String text,
                         JmolFont font3d, Graphics3D g3d,
                         JmolRendererInterface jmolRenderer, boolean antialias) {
    if (text.length() == 0)
      return 0;
    //System.out.println(x + "  " + y + " " + text);
    if (text.indexOf("<su") >= 0)
      return plotByCharacter(x, y, z, argb, text, font3d, g3d, jmolRenderer,
          antialias);
    int offset = font3d.getAscent();
    //if (antialias)
      //offset += offset;
    y -= offset;

    //setColix has presumably been carried out for argb, and the two 
    //are assumed to be both the same -- translucent or not. 
    TextRenderer text3d = getPlotText3D(x, y, g3d, text, font3d, antialias);
    if (text3d.isInvalid)
      return text3d.width;
    //TODO: text width/height are calculated 4x correct size here when antialiased.
    // this is wasteful, as it requires drawing larger than necessary images
    if (antialias && (argb & 0xC0C0C0) == 0) {
      // an interesting problem with antialiasing occurs if 
      // the label is black or almost black.
      argb = argb | 0x040404;
    }
    if (jmolRenderer != null
        || (x < 0 || x + text3d.width > g3d.width || y < 0 || y + text3d.height > g3d.height))
      plotClipped(x, y, z, argb, g3d, jmolRenderer, text3d.mapWidth,
          text3d.height, text3d.bitmap);
    else
      plotUnclipped(x, y, z, argb, g3d, text3d.mapWidth, text3d.height,
          text3d.bitmap);
    return text3d.width;
  }

  private static int plotByCharacter(int x, int y, int z, int argb, 
                                      String text, JmolFont font3d, 
                                      Graphics3D g3d, JmolRendererInterface jmolRenderer,
                                      boolean antialias) {
    //int subscale = 1; //could be something less than that
    int w = 0;
    int len = text.length();
    int suboffset = (int)(font3d.getHeight() * 0.25);
    int supoffset = -(int)(font3d.getHeight() * 0.3);
    for (int i = 0; i < len; i++) {
      if (text.charAt(i) == '<') {
        if (i + 4 < len && text.substring(i, i + 5).equals("<sub>")) {
          i += 4;
          y += suboffset;
          continue;
        }
        if (i + 4 < len && text.substring(i, i + 5).equals("<sup>")) {
          i += 4;
          y += supoffset;
          continue;
        }
        if (i + 5 < len  && text.substring(i, i + 6).equals("</sub>")) {
          i += 5;
          y -= suboffset;
          continue;
        }
        if (i + 5 < len  && text.substring(i, i + 6).equals("</sup>")) {
          i += 5;
          y -= supoffset;
          continue;
        }
      }
      int width = plot(x + w, y, z, argb, text.substring(i, i + 1), font3d, 
          g3d, jmolRenderer, antialias);
      w += width;
    }
    //System.out.println("w=" + w);
    return w;
  }
  
  private static void plotUnclipped(int x, int y, int z, int argb,
                                    Graphics3D g3d, int textWidth,
                                    int textHeight, int[] bitmap) {
    int offset = 0;
    int shiftregister = 0;
    int i = 0, j = 0;
    int[] zbuf = g3d.zbuf;
    int renderWidth = g3d.width;
    int pbufOffset = y * renderWidth + x;
    while (i < textHeight) {
      while (j < textWidth) {
        if ((offset & 31) == 0)
          shiftregister = bitmap[offset >> 5];
        if (shiftregister == 0) {
          int skip = 32 - (offset & 31);
          j += skip;
          offset += skip;
          pbufOffset += skip;
          continue;
        }
        if (shiftregister < 0 && z < zbuf[pbufOffset])
          g3d.addPixel(pbufOffset, z, argb);
        shiftregister <<= 1;
        ++offset;
        ++j;
        ++pbufOffset;
      }
      while (j >= textWidth) {
        ++i;
        j -= textWidth;
        pbufOffset += (renderWidth - textWidth);
      }
    }
  }
  
  private static void plotClipped(int x, int y, int z, int argb,
                                  Graphics3D g3d,
                                  JmolRendererInterface jmolRenderer,
                                  int textWidth, int textHeight, int[] bitmap) {
    if (jmolRenderer == null)
      jmolRenderer = g3d;
    int offset = 0;
    int shiftregister = 0;
    int i = 0, j = 0;
    while (i < textHeight) {
      while (j < textWidth) {
        if ((offset & 31) == 0)
          shiftregister = bitmap[offset >> 5];
        if (shiftregister == 0) {
          int skip = 32 - (offset & 31);
          j += skip;
          offset += skip;
          continue;
        }
        if (shiftregister < 0)
          jmolRenderer.plotPixelClippedNoSlab(argb, x + j, y + i, z);
        shiftregister <<= 1;
        ++offset;
        ++j;
      }
      while (j >= textWidth) {
        ++i;
        j -= textWidth;
      }
    }
  }

  /**
   * 
   * @param text
   * @param font3d
   */
  private TextRenderer(String text, JmolFont font3d) {
    ascent = font3d.getAscent();
    height = font3d.getHeight();
    width = font3d.stringWidth(text);
    if (width == 0)
      return;
    //System.out.println(text + " " + antialias + " "  + ascent + " " + height + " " + width );
    mapWidth = width;
    size = mapWidth * height;
  }

  private synchronized static TextRenderer getPlotText3D(int x, int y, Graphics3D g3d,
                                               String text, JmolFont font3d,
                                               boolean antialias) {
    TextRenderer.working = true;
    Map<JmolFont, Map<String, TextRenderer>> ht = (antialias ? TextRenderer.htFont3dAntialias : TextRenderer.htFont3d);
    Map<String, TextRenderer> htForThisFont = ht.get(font3d);
    TextRenderer text3d = null;
    boolean newFont = false;
    boolean newText = false;
    if (htForThisFont != null) {
      text3d = htForThisFont.get(text);
    } else {
      htForThisFont = new Hashtable<String, TextRenderer>();
      newFont = true;
    }
    if (text3d == null) {
      text3d = new TextRenderer(text, font3d);
      newText = true;
    }
    text3d.isInvalid = (text3d.width == 0 || x + text3d.width <= 0
        || x >= g3d.width || y + text3d.height <= 0 || y >= g3d.height);
    if (text3d.isInvalid)
      return text3d;
    if (newFont)
      ht.put(font3d, htForThisFont);
    if (newText) {
      //System.out.println(text + " " + x + " " + text3d.width + " " + g3d.width + " " + y + " " + g3d.height);
      text3d.setBitmap(text, font3d, g3d);
      htForThisFont.put(text, text3d);
    }
    TextRenderer.working = false;
    return text3d;
  }

  private void setBitmap(String text, JmolFont font3d, Graphics3D g3d) {
    rasterize(g3d.apiPlatform.getTextPixels(text, font3d, 
        g3d.platform.getOffScreenGraphics(mapWidth, height), 
        g3d.platform.offscreenImage, mapWidth, height, ascent));
  }

  /**
   * 
   * @param pixels 
   * 
   */
  private void rasterize(int[] pixels) {
    if (pixels == null)
      return;
    int bitmapSize = (size + 31) >> 5;
    bitmap = new int[bitmapSize];

    int offset, shifter;
    for (offset = shifter = 0; offset < size; ++offset, shifter <<= 1) {
      if ((pixels[offset] & 0x00FFFFFF) != 0)
        shifter |= 1;
      if ((offset & 31) == 31)
        bitmap[offset >> 5] = shifter;
    }
    if ((offset & 31) != 0) {
      shifter <<= 31 - (offset & 31);
      bitmap[offset >> 5] = shifter;
    }
    /*      // error checking
      // shifter error checking
      boolean[] bits = new boolean[size];
      for (int i = 0; i < size; ++i)
        bits[i] = (pixels[i] & 0x00FFFFFF) != 0;
      //
      for (offset = 0; offset < size; ++offset, shifter <<= 1) {
        if ((offset & 31) == 0)
          shifter = bitmap[offset >> 5];
        if (shifter < 0) {
          if (!bits[offset]) {
            Logger.debug("false positive @" + offset);
            Logger.debug("size = " + size);
          }
        } else {
          if (bits[offset]) {
            Logger.debug("false negative @" + offset);
            Logger.debug("size = " + size);
          }
        }
      }
      // error checking
    */
  }

}
