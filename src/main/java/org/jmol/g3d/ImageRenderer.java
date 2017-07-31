/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2011-10-10 20:09:00 -0500 (Mon, 10 Oct 2011) $
 * $Revision: 16309 $
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

import org.jmol.api.JmolRendererInterface;

class ImageRenderer {

  /**
   * 
   * @param x
   * @param y
   * @param z
   * @param image
   * @param g3d
   * @param jmolRenderer
   * @param antialias  UNUSED
   * @param argbBackground
   * @param width
   * @param height
   */
  static void plotImage(int x, int y, int z, Object image,
                               Graphics3D g3d,
                               JmolRendererInterface jmolRenderer,
                               boolean antialias, int argbBackground,
                               int width, int height) {
    boolean isBackground = (x == Integer.MIN_VALUE);
    int bgcolor = (isBackground ? g3d.bgcolor : argbBackground);
    /*
     *  this was for transparent background, which we have disabled, I think, in Jmol 12
    boolean haveTranslucent = false;
    PixelGrabber pg1 = new PixelGrabber(image, 0, 0, width0, height0, true);
    if (pg1.getColorModel().hasAlpha())
      try {
        pg1.grabPixels();
        int[] buffer = (int[]) pg1.getPixels();
        for (int i = 0; i < buffer.length; i++)
          if ((buffer[i] & 0xFF00000) != 0xFF000000) {
            haveTranslucent = true;
            break;
          }
        System.out.println(buffer.length + " " + haveTranslucent + " "
            + pg1.getColorModel().hasAlpha());
      } catch (InterruptedException e) {
        // impossible?
        return;
      }
      */
    if (isBackground) {
      x = 0;
      z = Integer.MAX_VALUE - 1;
      width = g3d.width;
      height = g3d.height;
    }
    if (x + width <= 0 || x >= g3d.width || y + height <= 0 || y >= g3d.height)
      return;
    int[] buffer = g3d.apiPlatform.drawImageToBuffer(
        g3d.platform.getOffScreenGraphics(width, height),
        g3d.platform.offscreenImage, image, width, height, 
        isBackground ? bgcolor : 0);
    if (buffer == null)
      return; // not supported on this platform (yet)
/*    
    int n = 0;
    for (int i = 0; i < buffer.length; i++) {
      if ((buffer[i] & 0xFF000000) != 0xFF000000) {
        // System.out.println("testing " + i + " " + buffer[i]);
        n++;
      }
    }
    System.out.println(n + " transparent argbBackground=" + argbBackground);
*/
    if (jmolRenderer != null
        || (x < 0 || x + width > g3d.width || y < 0 || y + height > g3d.height))
      plotImageClipped(x, y, z, g3d, jmolRenderer, width, height, buffer,
          bgcolor);
    else
      plotImageUnClipped(x, y, z, g3d, width, height, buffer, bgcolor);
    return;
  }

  private static void plotImageClipped(int x, int y, int z, Graphics3D g3d,
                                       JmolRendererInterface jmolRenderer,
                                       int width, int height,
                                       int[] buffer, int bgcolor) {
    if (jmolRenderer == null)
      jmolRenderer = g3d;
    for (int i = 0, offset = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        int argb = buffer[offset++];
        if (argb != bgcolor && (argb & 0xFF000000) == 0xFF000000)
          jmolRenderer.plotPixelClippedNoSlab(argb, x + j, y + i, z);
        else if (argb == 0 && bgcolor != 0)
          jmolRenderer.plotPixelClippedNoSlab(bgcolor, x + j, y + i, z);
      }
    }
  }

  private static void plotImageUnClipped(int x, int y, int z, Graphics3D g3d,
                                         int textWidth, int textHeight,
                                         int[] buffer, int bgcolor) {
    int[] zbuf = g3d.zbuf;
    int renderWidth = g3d.width;
    int pbufOffset = y * renderWidth + x;
    int i = 0;
    int j = 0;
    int offset = 0;
    while (i < textHeight) {
      while (j < textWidth) {
        if (z < zbuf[pbufOffset]) {
          int argb = buffer[offset];
          if (argb != bgcolor && (argb & 0xFF000000) == 0xFF000000)
            g3d.addPixel(pbufOffset, z, argb);
          else if (argb == 0 && bgcolor != 0)
            g3d.addPixel(pbufOffset, z, bgcolor);
        }
        ++offset;
        ++j;
        ++pbufOffset;
      }
      ++i;
      j -= textWidth;
      pbufOffset += (renderWidth - textWidth);
    }
  }

}
