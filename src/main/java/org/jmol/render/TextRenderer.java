/* $RCSfile$
 * $Author: nicove $
 * $Date: 2010-07-31 04:51:00 -0500 (Sat, 31 Jul 2010) $
 * $Revision: 13783 $

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

package org.jmol.render;

import org.jmol.api.JmolRendererInterface;
import org.jmol.shape.Object2d;
import org.jmol.shape.Text;
import org.jmol.util.JmolFont;

public class TextRenderer {
  
  public static void render(Text text, JmolRendererInterface g3d,
                            float scalePixelsPerMicron, float imageFontScaling,
                            boolean isExact, float[] boxXY) {
    if (text == null)
      return;
    text.setPosition(g3d.getRenderWidth(), g3d.getRenderHeight(), 
        scalePixelsPerMicron, imageFontScaling, isExact, boxXY);
    // draw the box if necessary
    if (text.image == null && text.bgcolix != 0 && g3d.setColix(text.bgcolix))
      showBox(g3d, text.colix, (int) text.boxX, (int) text.boxY, text.z + 2, text.zSlab, 
          (int) text.boxWidth,
          (int) text.boxHeight, text.fontScale, text.isLabelOrHover);
    if (g3d.setColix(text.colix)) {

      // now set x and y positions for text from (new?) box position

      if (text.image != null) {
        g3d.drawImage(text.image, (int) text.boxX, (int) text.boxY, text.z, text.zSlab, text.bgcolix,
            (int) text.boxWidth, (int) text.boxHeight);
      } else if (text.lines != null) {
        // now write properly aligned text
        float[] xy = new float[3];
        for (int i = 0; i < text.lines.length; i++) {
          text.setXY(xy, i);
          g3d.drawString(text.lines[i], text.font, (int) xy[0], (int) xy[1], text.z, text.zSlab);
        }
      }
    }
    drawPointer(text, g3d);
  }

  protected static void drawPointer(Text text, JmolRendererInterface g3d) {
    // now draw the pointer, if requested

    if ((text.pointer & Object2d.POINTER_ON) != 0) {
      if (!g3d.setColix((text.pointer & Object2d.POINTER_BACKGROUND) != 0 && text.bgcolix != 0 ? text.bgcolix
              : text.colix))
        return;
      if (text.boxX > text.movableX)
        g3d.drawLine(text.movableX, text.movableY, text.zSlab , 
            (int) text.boxX, (int) (text.boxY + text.boxHeight / 2),
            text.zSlab);
      else if (text.boxX + text.boxWidth < text.movableX)
        g3d.drawLine(text.movableX, text.movableY, text.zSlab, 
            (int) (text.boxX + text.boxWidth), 
            (int) (text.boxY + text.boxHeight / 2), text.zSlab);
    }
  }

  private static void showBox(JmolRendererInterface g3d, short colix,
                              int x, int y, int z, int zSlab,
                              int boxWidth, int boxHeight,
                              float imageFontScaling, boolean atomBased) {
    g3d.fillRect(x, y, z, zSlab, boxWidth, boxHeight);
    g3d.setColix(colix);
    if (!atomBased)
      return;
    if (imageFontScaling >= 2) {
      g3d.drawRect(x + 3, y + 3, z - 1, zSlab, boxWidth - 6, boxHeight - 6);
      g3d.drawRect(x + 4, y + 4, z - 1, zSlab, boxWidth - 8, boxHeight - 8);
    } else {
      g3d.drawRect(x + 1, y + 1, z - 1, zSlab, boxWidth - 2, boxHeight - 2);
    }
  }

  public final static void renderSimpleLabel(JmolRendererInterface g3d, JmolFont font,
                                 String strLabel, short colix, short bgcolix,
                                 float[] boxXY, int z, int zSlab,
                                 int xOffset, int yOffset, float ascent,
                                 int descent, boolean doPointer,
                                 short pointerColix, boolean isExact) {

    // old static style -- quick, simple, no line breaks, odd alignment?
    // LabelsRenderer only

    float boxWidth = font.stringWidth(strLabel) + 8;
    float boxHeight = ascent + descent + 8;
    
    int x0 = (int) boxXY[0];
    int y0 = (int) boxXY[1];
    
    Text.setBoxXY(boxWidth, boxHeight, xOffset, yOffset, boxXY, isExact);

    float x = boxXY[0];
    float y = boxXY[1];
    if (bgcolix != 0 && g3d.setColix(bgcolix))
      showBox(g3d, colix, (int) x, (int) y, z, zSlab, (int) boxWidth,
          (int) boxHeight, 1, true);
    else
      g3d.setColix(colix);
    g3d.drawString(strLabel, font, (int) (x + 4),
        (int) (y + 4 + ascent), z - 1, zSlab);

    if (doPointer) {
      g3d.setColix(pointerColix);
      if (xOffset > 0)
        g3d.drawLine(x0, y0, zSlab, (int) x, (int) (y + boxHeight / 2), zSlab);
      else if (xOffset < 0)
        g3d.drawLine(x0, y0, zSlab, (int) (x + boxWidth),
            (int) (y + boxHeight / 2), zSlab);
    }
  }

}
