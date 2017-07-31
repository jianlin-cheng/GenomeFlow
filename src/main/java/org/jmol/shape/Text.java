/* $RCSfile$
 * $Author: egonw $
 * $Date: 2005-11-10 09:52:44 -0600 (Thu, 10 Nov 2005) $
 * $Revision: 4255 $
 *
 * Copyright (C) 2002-2005  The Jmol Development Team
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
package org.jmol.shape;

import org.jmol.util.Colix;
import org.jmol.util.Escape;
import org.jmol.util.GData;
import org.jmol.util.JmolFont;
import org.jmol.util.TextFormat;
import org.jmol.viewer.Viewer;

public class Text extends Object2d {

  @Override
  public void setScalePixelsPerMicron(float scalePixelsPerMicron) {    
    fontScale = 0;//fontScale * this.scalePixelsPerMicron / scalePixelsPerMicron;
    super.setScalePixelsPerMicron(scalePixelsPerMicron);    
  }
  
  public float fontScale;

  private String text, textUnformatted;
  public boolean doFormatText;

  public String[] lines;

  public JmolFont font;
  private byte fid;
  private int ascent;
  private int descent;
  private int lineHeight;

  private int textWidth;
  private int textHeight;

  private int[] widths;

  // for labels and hover
  public Text(GData gdata, JmolFont font, String text, short colix,
      short bgcolix, int x, int y, int z, int zSlab, int textAlign,
      float scalePixelsPerMicron) {
    this.scalePixelsPerMicron = scalePixelsPerMicron;
    this.viewer = null;
    this.gdata = gdata;
    isLabelOrHover = true;
    setText(text);
    this.colix = colix;
    this.bgcolix = bgcolix;
    setXYZs(x, y, z, zSlab);
    align = textAlign;
    setFont(font);
  }

  // for echo
  Text(Viewer viewer, GData gdata, JmolFont font, String target, short colix,
      int valign, int align, float scalePixelsPerMicron) {
    super(viewer, gdata, target, colix, valign, align, scalePixelsPerMicron);
    this.font = font;
    getFontMetrics();
  }

  private void getFontMetrics() {
    descent = font.getDescent();
    ascent = font.getAscent();
    lineHeight = ascent + descent;
  }

  public void setFid(byte fid) { //labels only
    if (this.fid == fid)
      return;
    fontScale = 0;
    setFont(JmolFont.getFont3D(fid));
  }

  public void setText(String text) {
    if (image != null)
      getFontMetrics();
    image = null;
    text = fixText(text);
    if (this.text != null && this.text.equals(text))
      return;
    this.text = text;
    textUnformatted = text;
    doFormatText = (viewer != null && text != null && (text.indexOf("%{") >= 0 || text
        .indexOf("@{") >= 0));
    if (!doFormatText)
      recalc();
  }

  public Object image;
  float imageScale = 1;
  public void setImage(Object image) {
    this.image = image;
    // this.text will be file name
    recalc();
  }

  public void setScale(float scale) {
    imageScale = scale;
    recalc();
  }
  
  void setFont(JmolFont f3d) {
    font = f3d;
    if (font == null)
      return;
    fid = font.fid;
    getFontMetrics();
    recalc();
  }

  public void setFontScale(float scale) {
    if (fontScale == scale)
      return;
    fontScale = scale;
    if (fontScale != 0)
      setFont(gdata.getFont3DScaled(font, scale));
  }

  String fixText(String text) {
    if (text == null || text.length() == 0)
      return null;
    int pt;
    while ((pt = text.indexOf("\n")) >= 0)
      text = text.substring(0, pt) + "|" + text.substring(pt + 1);
    return text;
  }

  @Override
  protected void recalc() {
    if (image != null) {
      textWidth = textHeight = 0;
      boxWidth = viewer.apiPlatform.getImageWidth(image) * fontScale * imageScale;
      boxHeight = viewer.apiPlatform.getImageHeight(image) * fontScale * imageScale;
      ascent = 0;
      return;
    }
    if (text == null) {
      text = null;
      lines = null;
      widths = null;
      return;
    }
    if (font == null)
      return;
    lines = TextFormat.split(text, '|');
    textWidth = 0;
    widths = new int[lines.length];
    for (int i = lines.length; --i >= 0;)
      textWidth = Math.max(textWidth, widths[i] = stringWidth(lines[i]));
    textHeight = lines.length * lineHeight;
    boxWidth = textWidth + (fontScale >= 2 ? 16 : 8);
    boxHeight = textHeight + (fontScale >= 2 ? 16 : 8);
  }

  public void formatText() {
    text = (viewer == null ? textUnformatted : viewer
        .formatText(textUnformatted));
    recalc();
  }


  public void setPosition(float scale) {
    float xLeft, xCenter, xRight;
    boolean is3dEcho = (xyz != null);
    if (valign == VALIGN_XY || valign == VALIGN_XYZ) {
      float x = (movableXPercent != Integer.MAX_VALUE ? movableXPercent
          * windowWidth / 100 : is3dEcho ? movableX : movableX * scale);
      float offsetX = this.offsetX * scale;
      xLeft = xRight = xCenter = x + offsetX;
    } else {
      xLeft = 5 * scale;
      xCenter = windowWidth / 2;
      xRight = windowWidth - xLeft;
    }

    // set box X from alignments

    boxXY[0] = xLeft;
    switch (align) {
    case ALIGN_CENTER:
      boxXY[0] = xCenter - boxWidth / 2;
      break;
    case ALIGN_RIGHT:
      boxXY[0] = xRight - boxWidth;
    }

    // set box Y from alignments

    boxXY[1] = 0;
    switch (valign) {
    case VALIGN_TOP:
      break;
    case VALIGN_MIDDLE:
      boxXY[1] = windowHeight / 2;
      break;
    case VALIGN_BOTTOM:
      boxXY[1] = windowHeight;
      break;
    default:
      float y = (movableYPercent != Integer.MAX_VALUE ? movableYPercent
          * windowHeight / 100 : is3dEcho ? movableY : movableY * scale);
      boxXY[1] = (is3dEcho ? y : (windowHeight - y)) + offsetY * scale;
    }

    if (align == ALIGN_CENTER)
      boxXY[1] -= (image != null ? boxHeight : xyz != null ? boxHeight 
          : ascent - boxHeight) / 2;
    else if (image != null)
      boxXY[1] -= 0;
    else if (xyz != null)
      boxXY[1] -= ascent / 2;
  }

  public static void setBoxXY(float boxWidth, float boxHeight, float xOffset,
                               float yOffset, float[] boxXY, boolean isExact) {
    float xBoxOffset, yBoxOffset;

    // these are based on a standard |_ grid, so y is reversed.
    if (xOffset > 0 || isExact) {
      xBoxOffset = xOffset;
    } else {
      xBoxOffset = -boxWidth;
      if (xOffset == 0)
        xBoxOffset /= 2;
      else
        xBoxOffset += xOffset;
    }

    if (isExact) {
      yBoxOffset = -boxHeight + yOffset;
    } else if (yOffset < 0) {
        yBoxOffset = -boxHeight + yOffset;
    } else if (yOffset == 0) {
      yBoxOffset = -boxHeight / 2; // - 2; removed in Jmol 11.7.45 06/24/2009
    } else {
      yBoxOffset = yOffset;
    }
    boxXY[0] += xBoxOffset;
    boxXY[1] += yBoxOffset;
    boxXY[2] = boxWidth;
    boxXY[3] = boxHeight;
  }
  
  public String getState() {
    StringBuffer s = new StringBuffer();
    if (text == null || isLabelOrHover || target.equals("error"))
      return "";
    //set echo top left
    //set echo myecho x y
    //echo .....
    boolean isImage = (image != null);
    //    if (isDefine) {
    String strOff = null;
    String echoCmd = "set echo ID " + Escape.escapeStr(target);
    switch (valign) {
    case VALIGN_XY:
      if (movableXPercent == Integer.MAX_VALUE
          || movableYPercent == Integer.MAX_VALUE) {
        strOff = (movableXPercent == Integer.MAX_VALUE ? movableX + " "
            : movableXPercent + "% ")
            + (movableYPercent == Integer.MAX_VALUE ? movableY + ""
                : movableYPercent + "%");
      } else {
        strOff = "[" + movableXPercent + " " + movableYPercent + "%]";
      }
      //$FALL-THROUGH$
    case VALIGN_XYZ:
      if (strOff == null)
        strOff = Escape.escapePt(xyz);
      s.append("  ").append(echoCmd).append(" ").append(strOff);
      if (align != ALIGN_LEFT)
        s.append(";  ").append(echoCmd).append(" ").append(hAlignNames[align]);
      break;
    default:
      s.append("  set echo ").append(vAlignNames[valign]).append(" ").append(
          hAlignNames[align]);
    }
    if (valign == VALIGN_XY && movableZPercent != Integer.MAX_VALUE)
      s.append(";  ").append(echoCmd).append(" depth ").append(movableZPercent);
    if (isImage)
      s.append("; ").append(echoCmd).append(" IMAGE /*file*/");
    else
      s.append("; echo ");
    s.append(Escape.escapeStr(text)); // was textUnformatted, but that is not really the STATE
    s.append(";\n");
    if (isImage && imageScale != 1)
      s.append("  ").append(echoCmd).append(" scale ").append(imageScale).append(";\n");
    if (script != null)
      s.append("  ").append(echoCmd).append(" script ").append(
          Escape.escapeStr(script)).append(";\n");
    if (modelIndex >= 0)
      s.append("  ").append(echoCmd).append(" model ").append(
          viewer.getModelNumberDotted(modelIndex)).append(";\n");
    //    }
    //isDefine and target==top: do all
    //isDefine and target!=top: just start
    //!isDefine and target==top: do nothing
    //!isDefine and target!=top: do just this
    //fluke because top is defined with default font
    //in initShape(), so we MUST include its font def here
    //    if (isDefine != target.equals("top"))
    //      return s.toString();
    // these may not change much:
    s.append("  " + Shape.getFontCommand("echo", font));
    if (scalePixelsPerMicron > 0)
      s.append(" " + (10000f / scalePixelsPerMicron)); // Angstroms per pixel
    s.append("; color echo");
    if (Colix.isColixTranslucent(colix))
      s.append(" translucent " + Colix.getColixTranslucencyFractional(colix));
    s.append(" ").append(Colix.getHexCode(colix));
    if (bgcolix != 0) {
      s.append("; color echo background");
      if (Colix.isColixTranslucent(bgcolix))
        s.append(" translucent "
            + Colix.getColixTranslucencyFractional(bgcolix));
      s.append(" ").append(Colix.getHexCode(bgcolix));
    }
    s.append(";\n");
    return s.toString();
  }

  private int stringWidth(String str) {
    int w = 0;
    int f = 1;
    int subscale = 1; //could be something less than that
    if (str == null)
      return 0;
    if (str.indexOf("<su") < 0)
      return font.stringWidth(str);
    int len = str.length();
    String s;
    for (int i = 0; i < len; i++) {
      if (str.charAt(i) == '<') {
        if (i + 4 < len
            && ((s = str.substring(i, i + 5)).equals("<sub>") || s
                .equals("<sup>"))) {
          i += 4;
          f = subscale;
          continue;
        }
        if (i + 5 < len
            && ((s = str.substring(i, i + 6)).equals("</sub>") || s
                .equals("</sup>"))) {
          i += 5;
          f = 1;
          continue;
        }
      }
      w += font.stringWidth(str.substring(i, i + 1)) * f;
    }
    return w;
  }

  public void setPosition(int width, int height, float scalePixelsPerMicron, float imageFontScaling,
                          boolean isExact, float[] boxXY) {
    if (boxXY == null)
      boxXY = this.boxXY;
    else
      this.boxXY = boxXY;
    setWindow(width, height, scalePixelsPerMicron);
    if (scalePixelsPerMicron != 0 && this.scalePixelsPerMicron != 0)
      setFontScale(scalePixelsPerMicron / this.scalePixelsPerMicron);
    else if (fontScale != imageFontScaling)
      setFontScale(imageFontScaling);
    if (doFormatText)
      formatText();

    if (isLabelOrHover) {
      boxXY[0] = movableX;
      boxXY[1] = movableY;
      setBoxXY(boxWidth, boxHeight, offsetX * imageFontScaling, offsetY
          * imageFontScaling, boxXY, isExact);
    } else {
      setPosition(fontScale);
    }
    boxX = boxXY[0];
    boxY = boxXY[1];

    // adjust positions if necessary

    if (adjustForWindow)
      setBoxOffsetsInWindow(/*image == null ? fontScale * 5 :*/0,
          isLabelOrHover ? 16 * fontScale + lineHeight : 0, boxY - textHeight);

  }

  public void setXY(float[] xy, int i) {
    if (i == 0) {
      int adj = (fontScale >= 2 ? 8 : 4);
      xy[2] = boxX;
      switch (align) {
      case ALIGN_CENTER:
        xy[2] += boxWidth / 2;
        break;
      case ALIGN_RIGHT:
        xy[2] += boxWidth - adj;
        break;
      default:
        xy[2] += adj;
      }
      xy[0] = xy[2];
      xy[1] = boxY + ascent - lineHeight + adj;
    } else {
      switch (align) {
      case ALIGN_CENTER:
        xy[0] = xy[2] - widths[i] / 2;
        break;
      case ALIGN_RIGHT:
        xy[0] = xy[2] - widths[i];
      }
    }
    xy[1] += lineHeight; 
  }

}
