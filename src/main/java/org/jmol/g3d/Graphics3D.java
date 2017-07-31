/* $RCSfile$
 *  * $Author: hansonr $
 * $Date: 2012-09-03 20:41:34 -0500 (Mon, 03 Sep 2012) $
 * $Revision: 17503 $
 *
 * Copyright (C) 2003-2006  Miguel, Jmol Development, www.jmol.org
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

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;

import org.jmol.api.ApiPlatform;
import org.jmol.api.JmolRendererInterface;
import org.jmol.constant.EnumStereoMode;
import org.jmol.modelset.Atom;
import org.jmol.util.Colix;
import org.jmol.util.GData;
import org.jmol.util.JmolFont;
import org.jmol.util.MeshSurface;
import org.jmol.util.Normix;
import org.jmol.util.Rgb16;
import org.jmol.util.Shader;
import org.jmol.viewer.Viewer;

/**
 * Provides high-level graphics primitives for 3D visualization
 * for the software renderers. These methods should not have to 
 * be used with WebGL or OpenGL or other hardware accelerators.
 * 
 * This module is linked to via reflection from org.jmol.viewer.Viewer
 * 
 * Bob Hanson 9/2/2012
 * 
 * 
 *<p>
 * A pure software implementation of a 3D graphics engine.
 * No hardware required.
 * Depending upon what you are rendering ... some people say it
 * is <i>pretty fast</i>.
 *
 * @author Miguel, miguel@jmol.org
 * 
 * with additions by Bob Hanson hansonr@stolaf.edu
 * 
 * The above is an understatement to say the least.
 * 
 * This is a two-pass rendering system. In the first pass, all opaque
 * objects are rendered. In the second pass, all translucent objects
 * are rendered. 
 * 
 * If there are no translucent objects, then that is found in the 
 * first pass as follows: 
 * 
 * The renderers first try to set the color index of the object to be 
 * rendered using setColix(short colix), and that method returns false 
 * if we are in the wrong pass for that type of object. 
 * 
 * In addition, setColix records in the boolean haveTranslucentObjects 
 * whether a translucent object was seen in the first pass. 
 * 
 * The second pass is skipped if this flag is not set. This saves immensely 
 * on rendering time when there are no translucent objects.  
 * 
 * THUS, IT IS CRITICAL THAT ALL RENDERING OPTIONS CHECK THE COLIX USING
 * g3d.setColix(short colix) PRIOR TO RENDERING.
 * 
 * Translucency is rendered only approximately. We can't maintain a full
 * buffer of all translucent objects. Instead, we "cheat" by maintaining
 * one translucent z buffer. When a translucent pixel is to be written, its
 * z position is checked and...
 * 
 * ...if it is behind or at the z position of any pixel, it is ignored
 * ...if it is in front of a translucent pixel, it is added to the translucent buffer
 * ...if it is between an opaque and translucent pixel, the translucent pixel is
 *       turned opaque, and the new pixel is added to the translucent buffer
 * 
 * This guarantees accurate translucency when there are no more than two translucent
 * pixels between the user and an opaque pixel. It's a fudge, for sure. But it is 
 * pretty good, and certainly fine for "draft" work. 
 * 
 * Users needing more accurate translucencty are encouraged to use the POV-Ray export
 * facility for production-level work.
 * 
 * Antialiasing is accomplished as full scene antialiasing. This means that 
 * the width and height are doubled (both here and in TransformManager), the
 * scene is rendered, and then each set of four pixels is averaged (roughly)
 * as the final pixel in the width*height buffer. 
 * 
 * Antialiasing options allow for antialiasing of all objects:
 * 
 *    antialiasDisplay = true
 *    antialiasTranslucent = true
 * 
 * or just the opaque ones:
 * 
 *    antialiasDisplay = true
 *    antialiasTranslucent = false
 *    
 * or not at all:
 * 
 *    antialiasDisplay = false
 *
 * The difference will be speed and memory. Adding translucent objects
 * doubles the buffer requirement, and adding antialiasing quadruples
 * the buffer requirement. 
 * 
 * So we have:
 * 
 * Memory requirements are significant, in multiples of (width) * (height) 32-bit integers:
 *
 *                 antialias OFF       ON/opaque only   ON/all objects
 *
 *   no translucent     1p + 1z = 2      4p + 4z = 8      4p + 4z = 8
 *      objects
 *
 *   with translucent   2p + 2z = 4      5p + 5z = 10     8p + 8z = 16
 *      objects
 *
 * Note that no antialising at all is required for POV-Ray output. 
 * POV-Ray will do antialiasing on its own.
 * 
 * In principle we could save a bit in the case of antialiasing of 
 * just opaque objects and reuse the p and z buffers for the 
 * translucent buffer, but this hasn't been implemented because the 
 * savings isn't that great, and if you are going to the trouble of
 * having antialiasing, you probably what it all.
 * 
 * 
 */

final public class Graphics3D extends GData implements JmolRendererInterface {

  Platform3D platform;
  LineRenderer line3d;
  
  private CircleRenderer circle3d;
  private SphereRenderer sphere3d;
  private TriangleRenderer triangle3d;
  private CylinderRenderer cylinder3d;
  private HermiteRenderer hermite3d;
  private Normix normix3d;
  private boolean isFullSceneAntialiasingEnabled;
  private boolean antialias2; 

  @Override
  public void destroy() {
    releaseBuffers();
    platform = null;
    graphicsForMetrics = null;
  }

  /**
   * underlying GData object handles all general graphics setup. 
   * @return "this" in the case of Graphics3D and this.g3d in the case of Export3D
   * 
   */
  @Override
public GData getGData() {
    return this;
  }

  private byte[] anaglyphChannelBytes;
  
  private boolean twoPass = false;
  protected boolean isPass2;
  
  @Override
public boolean isPass2() {
    return isPass2;
  }
  
  private boolean addAllPixels;
  private boolean haveTranslucentObjects;
  protected boolean translucentCoverOnly = false;
  @Override
public void setTranslucentCoverOnly(boolean TF) {
    translucentCoverOnly = TF;
  }
  
  protected int[] pbuf;
  protected int[] pbufT;
  protected int[] zbuf;
  protected int[] zbufT;
  protected int translucencyMask;

  //int clipX;
  //int clipY;
  //int clipWidth;
  //int clipHeight;

  private short colixCurrent;
  private int[] shadesCurrent;
  private int argbCurrent;
  private int anaglyphLength;
  private boolean isScreened;
  private int argbNoisyUp, argbNoisyDn;

  private JmolFont currentFont;
  private Pixel pixel;

  protected int zMargin;
  
  void setZMargin(int dz) {
    zMargin = dz;
  }


  public Graphics3D() {
  }
  
  @Override
  public void initialize(ApiPlatform apiPlatform) {
    super.initialize(apiPlatform);
    platform = new Platform3D(apiPlatform);
    graphicsForMetrics = platform.getGraphicsForMetrics();
    
    this.line3d = new LineRenderer(this);
    this.circle3d = new CircleRenderer(this);
    this.sphere3d = new SphereRenderer(this);
    this.triangle3d = new TriangleRenderer(this);
    this.cylinder3d = new CylinderRenderer(this);
    this.hermite3d = new HermiteRenderer(this);
    this.normix3d = new Normix();
  }
  
  public boolean currentlyRendering() {
    return currentlyRendering;
  }
  
  @Override
  public void setWindowParameters(int width, int height, boolean antialias) {
    super.setWindowParameters(width, height, antialias);
    if (currentlyRendering)
      endRendering();
  }
  
  @Override
public boolean checkTranslucent(boolean isAlphaTranslucent) {
    if (isAlphaTranslucent)
      haveTranslucentObjects = true;
    return (!twoPass || twoPass && (isPass2 == isAlphaTranslucent));
  }
  
  @Override
  public void beginRendering(Matrix3f rotationMatrix) {
    if (currentlyRendering)
      endRendering();
    if (windowWidth != newWindowWidth || windowHeight != newWindowHeight
        || newAntialiasing != isFullSceneAntialiasingEnabled) {
      windowWidth = newWindowWidth;
      windowHeight = newWindowHeight;
      isFullSceneAntialiasingEnabled = newAntialiasing;
      releaseBuffers();
    }
    normix3d.setRotationMatrix(rotationMatrix);
    antialiasEnabled = antialiasThisFrame = newAntialiasing;
    currentlyRendering = true;
    twoPass = true; //only for testing -- set false to disallow second pass
    isPass2 = false;
    colixCurrent = 0;
    haveTranslucentObjects = false;
    addAllPixels = true;
    if (pbuf == null) {
      platform.allocateBuffers(windowWidth, windowHeight,
                              antialiasThisFrame);
      pbuf = platform.pBuffer;
      zbuf = platform.zBuffer;
    }
    setWidthHeight(antialiasThisFrame);
    platform.obtainScreenBuffer();
   // if (backgroundImage != null)
   //   plotImage(Integer.MIN_VALUE, 0, Integer.MIN_VALUE, backgroundImage, null, (short) 0, 0, 0);
  }

  @Override
  public void setBackgroundTransparent(boolean TF) {
    if (platform != null)
    platform.setBackgroundTransparent(TF);
  }

  private void releaseBuffers() {
    pbuf = null;
    zbuf = null;
    pbufT = null;
    zbufT = null;
    platform.releaseBuffers();
  }
  
  @Override
  public boolean setPass2(boolean antialiasTranslucent) {
    if (!haveTranslucentObjects || !currentlyRendering)
      return false;
    isPass2 = true;
    colixCurrent = 0;
    addAllPixels = true;
    if (pbufT == null || antialias2 != antialiasTranslucent) {
      platform.allocateTBuffers(antialiasTranslucent);
      pbufT = platform.pBufferT;
      zbufT = platform.zBufferT;
    }    
    antialias2 = antialiasTranslucent;
    if (antialiasThisFrame && !antialias2)
      downsampleFullSceneAntialiasing(true);
    platform.clearTBuffer();
    return true;
  }
  
  
  @Override
  public void endRendering() {
    if (!currentlyRendering)
      return;
    if (pbuf != null) {
      if (isPass2)
        mergeOpaqueAndTranslucentBuffers();
      if (antialiasThisFrame)
        downsampleFullSceneAntialiasing(false);
    }
    platform.setBackgroundColor(bgcolor);
    platform.notifyEndOfRendering();
    //setWidthHeight(antialiasEnabled);
    currentlyRendering = false;
  }

  @Override
  public void applyAnaglygh(EnumStereoMode stereoMode, int[] stereoColors) {
    switch (stereoMode) {
    case REDCYAN:
      applyCyanAnaglyph();
      break;
    case CUSTOM:
      applyCustomAnaglyph(stereoColors);
      break;
    case REDBLUE:
      applyBlueAnaglyph();
      break;
    case REDGREEN:
      applyGreenAnaglyph();
      break;
    case DOUBLE:
      break;
    case NONE:
      break;
    }
  }
  
  @Override
  public void snapshotAnaglyphChannelBytes() {
    if (currentlyRendering)
      throw new NullPointerException();
    anaglyphLength = windowWidth * windowHeight;
    if (anaglyphChannelBytes == null ||
  anaglyphChannelBytes.length != anaglyphLength)
      anaglyphChannelBytes = new byte[anaglyphLength];
    for (int i = anaglyphLength; --i >= 0; )
      anaglyphChannelBytes[i] = (byte)pbuf[i];
  }

  public void applyCustomAnaglyph(int[] stereoColors) {
    //best if complementary, but they do not have to be0 
    int color1 = stereoColors[0];
    int color2 = stereoColors[1] & 0x00FFFFFF;
    for (int i = anaglyphLength; --i >= 0;) {
      int a = anaglyphChannelBytes[i] & 0x000000FF;
      a = (a | ((a | (a << 8)) << 8)) & color2;
      pbuf[i] = (pbuf[i] & color1) | a;
    }
  }

  public void applyGreenAnaglyph() {
    for (int i = anaglyphLength; --i >= 0; ) {
      int green = (anaglyphChannelBytes[i] & 0x000000FF) << 8;
      pbuf[i] = (pbuf[i] & 0xFFFF0000) | green;
    }
  }

  public void applyBlueAnaglyph() {
    for (int i = anaglyphLength; --i >= 0; ) {
      int blue = anaglyphChannelBytes[i] & 0x000000FF;
      pbuf[i] = (pbuf[i] & 0xFFFF0000) | blue;
    }
  }

  public void applyCyanAnaglyph() {
    for (int i = anaglyphLength; --i >= 0; ) {
      int blue = anaglyphChannelBytes[i] & 0x000000FF;
      int cyan = (blue << 8) | blue;
      pbuf[i] = pbuf[i] & 0xFFFF0000 | cyan;
    }
  }
  
  @Override
  public Object getScreenImage() {
    return platform.imagePixelBuffer;
  }

  @Override
  public void releaseScreenImage() {
    platform.clearScreenBufferThreaded();
  }

  @Override
public boolean haveTranslucentObjects() {
    return haveTranslucentObjects;
  }
  
  public void setTempZSlab(int zSlab) {
    this.zSlab = zSlab;
  }
  
  @Override
  public void setZShade(boolean zShade, int zSlab, int zDepth) {
    super.setZShade(zShade, zSlab, zDepth);
    if (zShade) {
      pixel = new ShadePixel();
    } else {
      pixel = new Pixel();
    }

  }
  private void downsampleFullSceneAntialiasing(boolean downsampleZBuffer) {
    int width4 = width;
    int offset1 = 0;
    int offset4 = 0;
    int bgcheck = bgcolor;
    // now is the time we have to put in the correct background color
    // this was a bug in 11.6.0-11.6.2. 
    
    // we must downsample the Z Buffer if there are translucent
    // objects left to draw and antialiasTranslucent is set false
    // in that case we must fudge the background color, because
    // otherwise a match of the background color with an object
    // will put it in the back -- the "blue tie on a blue screen"
    // television effect. We want to avoid that. Here we can do that
    // because the colors will be blurred anyway.
    
    if (downsampleZBuffer)
      bgcheck += ((bgcheck & 0xFF) == 0xFF ? -1 : 1);
    for (int i =0; i < pbuf.length; i++)
      if (pbuf[i] == 0)
        pbuf[i] = bgcheck;
    bgcheck &= 0xFFFFFF;
    for (int i = windowHeight; --i >= 0; offset4 += width4)
      for (int j = windowWidth; --j >= 0; ++offset1) {
        
        /* more precise, but of no benefit:

        int a = pbuf[offset4];
        int b = pbuf[offset4++ + width4];
        int c = pbuf[offset4];
        int d = pbuf[offset4++ + width4];
        int argb = ((((a & 0x0f0f0f) + (b & 0x0f0f0f)
           + (c & 0x0f0f0f) + (d & 0x0f0f0f)) >> 2) & 0x0f0f0f)
           + ( ((a & 0xF0F0F0) + (b & 0xF0F0F0) 
           +   (c & 0xF0F0F0) + (d & 0xF0F0F0)
                ) >> 2);
        */
        
        int argb = ((pbuf[offset4] >> 2) & 0x3F3F3F3F)
          + ((pbuf[offset4++ + width4] >> 2) & 0x3F3F3F3F)
          + ((pbuf[offset4] >> 2) & 0x3F3F3F3F)
          + ((pbuf[offset4++ + width4] >> 2) & 0x3F3F3F3F);
        argb += (argb & 0xC0C0C0C0) >> 6;
        pbuf[offset1] = argb & 0x00FFFFFF;
      }
    if (downsampleZBuffer) {
      //we will add the alpha mask later
      offset1 = offset4 = 0;
      for (int i = windowHeight; --i >= 0; offset4 += width4)
        for (int j = windowWidth; --j >= 0; ++offset1, ++offset4) {
          int z = Math.min(zbuf[offset4], zbuf[offset4 + width4]);
          z = Math.min(z, zbuf[++offset4]);
          z = Math.min(z, zbuf[offset4 + width4]);
          if (z != Integer.MAX_VALUE)
            z >>= 1;
          zbuf[offset1] = (pbuf[offset1] == bgcheck ? Integer.MAX_VALUE
              : z);
        }
      antialiasThisFrame = false;
      setWidthHeight(false);
    }    
  }

  void mergeOpaqueAndTranslucentBuffers() {
    if (pbufT == null)
      return;
    for (int offset = 0; offset < bufferSize; offset++)
      mergeBufferPixel(pbuf, pbufT[offset], offset, bgcolor);
  }
/*  
  static void averageBufferPixel(int[] pIn, int[] pOut, int pt, int dp) {
    int argbA = pIn[pt - dp];
    int argbB = pIn[pt + dp];
    if (argbA == 0 || argbB == 0)
      return;
    pOut[pt] = ((((argbA & 0xFF000000)>>1) + ((argbB & 0xFF000000)>>1))<< 1)
        | (((argbA & 0x00FF00FF) + (argbB & 0x00FF00FF)) >> 1) & 0x00FF00FF
        | (((argbA & 0x0000FF00) + (argbB & 0x0000FF00)) >> 1) & 0x0000FF00;
  }
*/  
  static void mergeBufferPixel(int[] pbuf, int argbB, int pt, int bgcolor) {
    if (argbB == 0)
      return;
    int argbA = pbuf[pt];
    if (argbA == argbB)
      return;
    if (argbA == 0)
      argbA = bgcolor;
    int rbA = (argbA & 0x00FF00FF);
    int gA = (argbA & 0x0000FF00);
    int rbB = (argbB & 0x00FF00FF);
    int gB = (argbB & 0x0000FF00);
    int logAlpha = (argbB >> 24) & 7;
    //just for now:
    //0 or 1=100% opacity, 2=87.5%, 3=75%, 4=50%, 5=50%, 6 = 25%, 7 = 12.5% opacity.
    //4 is reserved because it is the default-Jmol 10.2
    switch (logAlpha) {
    // 0.0 to 1.0 ==> MORE translucent   
    //                1/8  1/4 3/8 1/2 5/8 3/4 7/8
    //     t           32  64  96  128 160 192 224
    //     t >> 5       1   2   3   4   5   6   7

    case 1: // 7:1
      rbA = (((rbB << 2) + (rbB << 1) + rbB  + rbA) >> 3) & 0x00FF00FF;
      gA = (((gB << 2) + + (gB << 1) + gB + gA) >> 3) & 0x0000FF00;
      break;
    case 2: // 3:1
      rbA = (((rbB << 1) + rbB + rbA) >> 2) & 0x00FF00FF;
      gA = (((gB << 1) + gB + gA) >> 2) & 0x0000FF00;
      break;
    case 3: // 5:3
      rbA = (((rbB << 2) + rbB + (rbA << 1) + rbA) >> 3) & 0x00FF00FF;
      gA = (((gB << 2) + gB  + (gA << 1) + gA) >> 3) & 0x0000FF00;
      break;
    case 4: // 1:1
      rbA = ((rbA + rbB) >> 1) & 0x00FF00FF;
      gA = ((gA + gB) >> 1) & 0x0000FF00;
      break;
    case 5: // 3:5
      rbA = (((rbB << 1) + rbB + (rbA << 2) + rbA) >> 3) & 0x00FF00FF;
      gA = (((gB << 1) + gB  + (gA << 2) + gA) >> 3) & 0x0000FF00;
      break;
    case 6: // 1:3
      rbA = (((rbA << 1) + rbA + rbB) >> 2) & 0x00FF00FF;
      gA = (((gA << 1) + gA + gB) >> 2) & 0x0000FF00;
      break;
    case 7: // 1:7
      rbA = (((rbA << 2) + (rbA << 1) + rbA + rbB) >> 3) & 0x00FF00FF;
      gA = (((gA << 2) + (gA << 1) + gA + gB) >> 3) & 0x0000FF00;
      break;
    }
    pbuf[pt] = 0xFF000000 | rbA | gA;    
  }
  
  public boolean hasContent() {
    return platform.hasContent();
  }

  private int currentShadeIndex;
  private int lastRawColor;
  
  @Override
public void setColor(int argb) {
    argbCurrent = argbNoisyUp = argbNoisyDn = argb;
  }
  

  /**
   * sets current color from colix color index
   * @param colix the color index
   * @return true or false if this is the right pass
   */
  @Override
public boolean setColix(short colix) {
    boolean isLast = Colix.isColixLastAvailable(colix); 
    if (!isLast && colix == colixCurrent && currentShadeIndex == -1)
      return true;
    int mask = colix & Colix.TRANSLUCENT_MASK;
    if (mask == Colix.TRANSPARENT)
      return false;
    boolean isTranslucent = mask != 0;
    isScreened = isTranslucent && mask == Colix.TRANSLUCENT_SCREENED;
    if (!checkTranslucent(isTranslucent && !isScreened))
      return false;
    addAllPixels = isPass2 || !isTranslucent;
    if (isPass2) {
      translucencyMask = (mask << Colix.ALPHA_SHIFT) | 0xFFFFFF;
    }
    colixCurrent = colix;
    if (isLast) {
      if (argbCurrent != lastRawColor) {
        if (argbCurrent == 0)
          argbCurrent = 0xFFFFFFFF;
        lastRawColor = argbCurrent;
        Colix.allocateColix(argbCurrent);
        Colix.getShades(argbCurrent, inGreyscaleMode);
      }
    }
    shadesCurrent = getShades(colix);
    currentShadeIndex = -1;
    setColor(getColorArgbOrGray(colix));
    return true;
  }

  void addPixel(int offset, int z, int p) {
    pixel.addPixel(offset, z, p);
  }
  
  protected class Pixel {
    void addPixel(int offset, int z, int p) {
      if (!isPass2) {
        zbuf[offset] = z;
        pbuf[offset] = p;
        return;
      }
      int zT = zbufT[offset];
      if (z < zT) {
        // new in front -- merge old translucent with opaque
        // if (zT != Integer.MAX_VALUE)
        int argb = pbufT[offset];
        if (!translucentCoverOnly && argb != 0 && zT - z > zMargin)
          mergeBufferPixel(pbuf, argb, offset, bgcolor);
        zbufT[offset] = z;
        pbufT[offset] = p & translucencyMask;
      } else if (z == zT) {
      } else if (!translucentCoverOnly && z - zT > zMargin) {
          // oops-out of order
          mergeBufferPixel(pbuf, p & translucencyMask, offset, bgcolor);
      }
    }
  }
  
  protected class ShadePixel extends Pixel {
    @Override
    void addPixel(int offset, int z, int p) {
      if (z > zDepth)
        return;
      if (z <= zDepth && z >= zSlab) {
        int pR = p & 0xFF;
        int pG = (p & 0xFF00) >> 8;
        int pB = (p & 0xFF0000) >> 16;
        int pA = (p & 0xFF000000);
        float f = (float)(zDepth - z) / (zDepth - zSlab);
        if (Shader.zPower > 1) {
          for (int i = 0; i < Shader.zPower; i++)
            f *= f;
        }
        pR = zShadeR + (int) (f * (pR - zShadeR));
        pG = zShadeG + (int) (f * (pG - zShadeG));
        pB = zShadeB + (int) (f * (pB - zShadeB));        
        p = (pB << 16) | (pG << 8) | pR | pA;
      }
      super.addPixel(offset, z, p);
    }
  }

  @Override
public void drawFilledCircle(short colixRing, short colixFill, int diameter,
                               int x, int y, int z) {
    if (isClippedZ(z))
      return;
    int r = (diameter + 1) / 2;
    boolean isClipped = x < r || x + r >= width || y < r || y + r >= height;
    if (isClipped && isClippedXY(diameter, x, y))
      return;
    if (colixRing != 0 && setColix(colixRing)) {
      if (isClipped)
        circle3d.plotCircleCenteredClipped(x, y, z, diameter);
      else
        circle3d.plotCircleCenteredUnclipped(x, y, z, diameter);
    }
    if (colixFill != 0 && setColix(colixFill)) {
      if (isClipped)
        circle3d.plotFilledCircleCenteredClipped(x, y, z, diameter);
      else
        circle3d.plotFilledCircleCenteredUnclipped(x, y, z, diameter);
    }
  }

  @Override
public void volumeRender(int diameter, int x, int y, int z) {
    if (diameter == 1) {
      plotPixelClipped(x, y, z);
      return;
    }
    if (isClippedZ(z))
      return;
    int r = (diameter + 1) / 2;
    boolean isClipped = x < r || x + r >= width || y < r || y + r >= height;
    if (isClipped && isClippedXY(diameter, x, y))
      return;
    if (isClipped)
      circle3d.plotFilledCircleCenteredClipped(x, y, z, diameter);
    else
      circle3d.plotFilledCircleCenteredUnclipped(x, y, z, diameter);
  }
  
  /**
   * fills a solid sphere
   *
   * @param diameter pixel count
   * @param x center x
   * @param y center y
   * @param z center z
   */
  @Override
public void fillSphere(int diameter, int x, int y, int z) {
	//  diameter=10;
    switch (diameter) {
    case 1:
      plotPixelClipped(argbCurrent, x, y, z);
      return;
    case 0:
      return;
    }
//int [] kk = {1,2,3);

    if (diameter <= (antialiasThisFrame ? SphereRenderer.maxSphereDiameter2
        : SphereRenderer.maxSphereDiameter))
      sphere3d.render(shadesCurrent, !addAllPixels, diameter, x, y, z, null,
          null, null, -1, null, addAllPixels);
    //System.out.println (shadesCurrent);
  }

  private int saveAmbient, saveDiffuse;

  @Override
public void volumeRender(boolean TF) {
    if (TF) {
      saveAmbient = Shader.ambientPercent;
      saveDiffuse = Shader.diffusePercent;
      GData.setAmbientPercent(100);
      GData.setDiffusePercent(0);
    } else {
      GData.setAmbientPercent(saveAmbient);
      GData.setDiffusePercent(saveDiffuse);
    }
  }
  /**
   * fills a solid sphere
   *
   * @param diameter pixel count
   * @param center javax.vecmath.Point3i defining the center
   */

  @Override
public void fillSphere(int diameter, Point3i center) {
    fillSphere(diameter, center.x, center.y, center.z);
  }

  /**
   * fills a solid sphere
   *
   * @param diameter pixel count
   * @param center a javax.vecmath.Point3f ... floats are casted to ints
   */
  @Override
public void fillSphere(int diameter, Point3f center) {
    fillSphere(diameter, (int)center.x, (int)center.y, (int)center.z);
  }

  @Override
public void fillEllipsoid(Point3f center, Point3f[] points, int x, int y,
                              int z, int diameter, Matrix3f mToEllipsoidal,
                              double[] coef, Matrix4f mDeriv,
                              int selectedOctant, Point3i[] octantPoints) {
    switch (diameter) {
    case 1:
      plotPixelClipped(argbCurrent, x, y, z);
      return;
    case 0:
      return;
    }
    if (diameter <= (antialiasThisFrame ? SphereRenderer.maxSphereDiameter2
        : SphereRenderer.maxSphereDiameter))
      sphere3d.render(shadesCurrent, !addAllPixels, diameter, x, y, z,
          mToEllipsoidal, coef, mDeriv, selectedOctant, octantPoints, addAllPixels);
  }

  /**
   * draws a rectangle
   *
   * @param x upper left x
   * @param y upper left y
   * @param z upper left z
   * @param zSlab z for slab check (for set labelsFront)
   * @param rWidth pixel count
   * @param rHeight pixel count
   */
  @Override
public void drawRect(int x, int y, int z, int zSlab, int rWidth, int rHeight) {
    // labels (and rubberband, not implemented) and navigation cursor
    if (zSlab != 0 && isClippedZ(zSlab))
      return;
    int w = rWidth - 1;
    int h = rHeight - 1;
    int xRight = x + w;
    int yBottom = y + h;
    if (y >= 0 && y < height)
      drawHLine(x, y, z, w);
    if (yBottom >= 0 && yBottom < height)
      drawHLine(x, yBottom, z, w);
    if (x >= 0 && x < width)
      drawVLine(x, y, z, h);
    if (xRight >= 0 && xRight < width)
      drawVLine(xRight, y, z, h);
  }

  private void drawHLine(int x, int y, int z, int w) {
    // hover, labels only
    if (w < 0) {
      x += w;
      w = -w;
    }
    if (x < 0) {
      w += x;
      x = 0;
    }
    if (x + w >= width)
      w = width - 1 - x;
    int offset = x + width * y;
    if (addAllPixels) {
      for (int i = 0; i <= w; i++) {
        if (z < zbuf[offset])
          addPixel(offset, z, argbCurrent);
        offset++;
      }
      return;
    }
    boolean flipflop = ((x ^ y) & 1) != 0;
    for (int i = 0; i <= w; i++) {
      if ((flipflop = !flipflop) && z < zbuf[offset])
        addPixel(offset, z, argbCurrent);
      offset++;
    }
  }

  private void drawVLine(int x, int y, int z, int h) {
    // hover, labels only
    if (h < 0) {
      y += h;
      h = -h;
    }
    if (y < 0) {
      h += y;
      y = 0;
    }
    if (y + h >= height) {
      h = height - 1 - y;
    }
    int offset = x + width * y;
    if (addAllPixels) {
      for (int i = 0; i <= h; i++) {
        if (z < zbuf[offset])
          addPixel(offset, z, argbCurrent);
        offset += width;
      }
      return;
    }
    boolean flipflop = ((x ^ y) & 1) != 0;
    for (int i = 0; i <= h; i++) {
      if ((flipflop = !flipflop) && z < zbuf[offset])
        addPixel(offset, z, argbCurrent);
      offset += width;
    }
  }


  /**
   * fills background rectangle for label
   *<p>
   *
   * @param x upper left x
   * @param y upper left y
   * @param z upper left z
   * @param zSlab  z value for slabbing
   * @param widthFill pixel count
   * @param heightFill pixel count
   */
  @Override
public void fillRect(int x, int y, int z, int zSlab, int widthFill, int heightFill) {
    // hover and labels only -- slab at atom or front -- simple Z/window clip
    if (isClippedZ(zSlab))
      return;
    if (x < 0) {
      widthFill += x;
      if (widthFill <= 0)
        return;
      x = 0;
    }
    if (x + widthFill > width) {
      widthFill = width - x;
      if (widthFill <= 0)
        return;
    }
    if (y < 0) {
      heightFill += y;
      if (heightFill <= 0)
        return;
      y = 0;
    }
    if (y + heightFill > height)
      heightFill = height - y;
    while (--heightFill >= 0)
      plotPixelsUnclipped(widthFill, x, y++, z);
  }
  
  /**
   * draws the specified string in the current font.
   * no line wrapping -- axis, labels, measures
   *
   * @param str the String
   * @param font3d the Font3D
   * @param xBaseline baseline x
   * @param yBaseline baseline y
   * @param z baseline z
   * @param zSlab z for slab calculation
   */
  
  @Override
public void drawString(String str, JmolFont font3d,
                         int xBaseline, int yBaseline, int z, int zSlab) {
    //axis, labels, measures    
    if (str == null)
      return;
    if (isClippedZ(zSlab))
      return;
    drawStringNoSlab(str, font3d, xBaseline, yBaseline, z); 
  }

  /**
   * draws the specified string in the current font.
   * no line wrapping -- echo, frank, hover, molecularOrbital, uccage
   *
   * @param str the String
   * @param font3d the Font3D
   * @param xBaseline baseline x
   * @param yBaseline baseline y
   * @param z baseline z
   */
  
  @Override
public void drawStringNoSlab(String str, JmolFont font3d, 
                               int xBaseline, int yBaseline,
                               int z) {
    // echo, frank, hover, molecularOrbital, uccage
    if (str == null)
      return;
    if(font3d != null)
      currentFont = font3d;
    plotText(xBaseline, yBaseline, z, argbCurrent, str, currentFont, null);
  }
  
  public void plotText(int x, int y, int z, int argb,
                String text, JmolFont font3d, JmolRendererInterface jmolRenderer) {
    TextRenderer.plot(x, y, z, argb, text, font3d, this, jmolRenderer, 
        antialiasThisFrame);    
  }
  
  @Override
public void drawImage(Object objImage, int x, int y, int z, int zSlab, 
                        short bgcolix, int width, int height) {
    if (objImage == null || width == 0 || height == 0)
      return;
    if (isClippedZ(zSlab))
      return;
    plotImage(x, y, z, objImage, null, bgcolix, width, height);
  }

  public void plotImage(int x, int y, int z, Object image, JmolRendererInterface jmolRenderer,
                        short bgcolix, int width, int height) {
    setColix(bgcolix);
    if (bgcolix == 0)
      argbCurrent = 0;
    ImageRenderer.plotImage(x, y, z, image, this, jmolRenderer, antialiasThisFrame, argbCurrent, 
        width, height);
  }

  @Override
public void setFont(byte fid) {
    currentFont = JmolFont.getFont3D(fid);
  }
  
  public void setFont(JmolFont font3d) {
    currentFont = font3d;
  }
  
  @Override
public JmolFont getFont3DCurrent() {
    return currentFont;
  }

  boolean currentlyRendering;

  /*
  private void setRectClip(int x, int y, int width, int height) {
    // not implemented
    if (x < 0)
      x = 0;
    if (y < 0)
      y = 0;
    if (x + width > windowWidth)
      width = windowWidth - x;
    if (y + height > windowHeight)
      height = windowHeight - y;
    clipX = x;
    clipY = y;
    clipWidth = width;
    clipHeight = height;
    if (antialiasThisFrame) {
      clipX *= 2;
      clipY *= 2;
      clipWidth *= 2;
      clipHeight *= 2;
    }
  }
  */

  //mostly public drawing methods -- add "public" if you need to

  /* ***************************************************************
   * points
   * ***************************************************************/

  
  @Override
public void drawPixel(int x, int y, int z) {
    // measures - render angle
    plotPixelClipped(x, y, z);
  }

  @Override
public void drawPoints(int count, int[] coordinates, int scale) {
    // for dots only
    if (scale > 1) {
      float s2 = scale * scale * 0.8f;
      for (int i = -scale; i < scale; i++) {
        for (int j = -scale; j < scale; j++) {
          if (i * i + j * j > s2)
            continue;
          plotPoints(count, coordinates, i, j);
          plotPoints(count, coordinates, i, j);
        }
      }
    } else {
      plotPoints(count, coordinates, 0, 0);
    }
  }

  /* ***************************************************************
   * lines and cylinders
   * ***************************************************************/

  @Override
public void drawDashedLine(int run, int rise, Point3i pointA, Point3i pointB) {
    // measures only
    line3d.plotDashedLine(argbCurrent, !addAllPixels, run, rise, 
        pointA.x, pointA.y, pointA.z,
        pointB.x, pointB.y, pointB.z, true);
  }

  @Override
public void drawDottedLine(Point3i pointA, Point3i pointB) {
     //axes, bbcage only
    line3d.plotDashedLine(argbCurrent, !addAllPixels, 2, 1,
                          pointA.x, pointA.y, pointA.z,
                          pointB.x, pointB.y, pointB.z, true);
  }

  @Override
public void drawLine(int x1, int y1, int z1, int x2, int y2, int z2) {
    // stars
    line3d.plotLine(argbCurrent, !addAllPixels, argbCurrent, !addAllPixels,
                    x1, y1, z1, x2, y2, z2, true);
  }

  @Override
public void drawLine(short colixA, short colixB,
                       int x1, int y1, int z1, int x2, int y2, int z2) {
    // backbone and sticks
    if (!setColix(colixA))
      colixA = 0;
    boolean isScreenedA = !addAllPixels;
    int argbA = argbCurrent;
    if (!setColix(colixB))
      colixB = 0;
    if (colixA == 0 && colixB == 0)
      return;
    line3d.plotLine(argbA, isScreenedA, argbCurrent, !addAllPixels,
                    x1, y1, z1, x2, y2, z2, true);
  }
  
  @Override
public void drawLine(Point3i pointA, Point3i pointB) {
    // draw quadrilateral and hermite
    line3d.plotLine(argbCurrent, !addAllPixels, argbCurrent, !addAllPixels,
                    pointA.x, pointA.y, pointA.z,
                    pointB.x, pointB.y, pointB.z, true);
  }
  
  @Override
public void fillCylinder(short colixA, short colixB, byte endcaps,
                           int diameter,
                           int xA, int yA, int zA, int xB, int yB, int zB) {
    //Backbone, Mps, Sticks
    if (!setColix(colixA))
      colixA = 0;
    boolean isScreenedA = !addAllPixels;
    if (!setColix(colixB))
      colixB = 0;
    if (colixA == 0 && colixB == 0)
      return;
    cylinder3d.render(colixA, colixB, isScreenedA, !addAllPixels, endcaps, diameter,
                      xA, yA, zA, xB, yB, zB);
  }

  @Override
public void fillCylinderScreen(byte endcaps,
                           int diameter,
                           int xA, int yA, int zA, int xB, int yB, int zB) {
    //measures, vectors, polyhedra
    cylinder3d.render(colixCurrent, colixCurrent, !addAllPixels, !addAllPixels, endcaps, diameter,
                      xA, yA, zA, xB, yB, zB);
  }

  @Override
public void fillCylinderScreen(byte endcaps, int diameter,
                           Point3i screenA, Point3i screenB) {
    //draw
    cylinder3d.render(colixCurrent, colixCurrent, !addAllPixels, !addAllPixels, endcaps, diameter,
                      screenA.x, screenA.y, screenA.z,
                      screenB.x, screenB.y, screenB.z);
  }

  @Override
public void fillCylinder(byte endcaps, int diameter,
                           Point3i screenA, Point3i screenB) {
    //axes, bbcage, uccage, cartoon, dipoles, mesh
    cylinder3d.render(colixCurrent, colixCurrent, !addAllPixels, !addAllPixels, endcaps, diameter,
                      screenA.x, screenA.y, screenA.z,
                      screenB.x, screenB.y, screenB.z);
  }

  @Override
public void fillCylinderBits(byte endcaps, int diameter,
                               Point3f screenA, Point3f screenB) {
   // dipole cross, cartoonRockets, draw line
   cylinder3d.renderBits(colixCurrent, colixCurrent, !addAllPixels, !addAllPixels, endcaps, diameter,
       screenA.x, screenA.y, screenA.z,
       screenB.x, screenB.y, screenB.z);
 }

  @Override
public void fillConeScreen(byte endcap, int screenDiameter,
                       Point3i screenBase, Point3i screenTip, boolean isBarb) {
    // dipoles, mesh, vectors
    cylinder3d.renderCone(colixCurrent, !addAllPixels, endcap, screenDiameter,
                          screenBase.x, screenBase.y, screenBase.z,
                          screenTip.x, screenTip.y, screenTip.z, false, isBarb);
  }

  @Override
public void fillConeSceen(byte endcap, int screenDiameter,
                       Point3f screenBase, Point3f screenTip) {
    // cartoons, rockets
    cylinder3d.renderCone(colixCurrent, !addAllPixels, endcap, screenDiameter,
                          screenBase.x, screenBase.y, screenBase.z,
                          screenTip.x, screenTip.y, screenTip.z, true, false);
  }

  @Override
public void drawHermite(int tension,
                          Point3i s0, Point3i s1, Point3i s2, Point3i s3) {
    hermite3d.renderHermiteRope(false, tension, 0, 0, 0, s0, s1, s2, s3);
  }

  @Override
public void drawHermite(boolean fill, boolean border,
                          int tension, Point3i s0, Point3i s1, Point3i s2,
                          Point3i s3, Point3i s4, Point3i s5, Point3i s6,
                          Point3i s7, int aspectRatio) {
    hermite3d.renderHermiteRibbon(fill, border, tension, s0, s1, s2, s3, s4, s5, s6,
        s7, aspectRatio);
  }

  @Override
public void fillHermite(int tension, int diameterBeg,
                          int diameterMid, int diameterEnd,
                          Point3i s0, Point3i s1, Point3i s2, Point3i s3) {
    hermite3d.renderHermiteRope(true, tension,
                     diameterBeg, diameterMid, diameterEnd,
                     s0, s1, s2, s3);
  }
  
  @Override
public void drawTriangle(Point3i screenA, short colixA, Point3i screenB,
                           short colixB, Point3i screenC, short colixC,
                           int check) {
    // primary method for mapped Mesh
    if ((check & 1) == 1)
      drawLine(colixA, colixB, screenA.x, screenA.y, screenA.z, screenB.x,
          screenB.y, screenB.z);
    if ((check & 2) == 2)
      drawLine(colixB, colixC, screenB.x, screenB.y, screenB.z, screenC.x,
          screenC.y, screenC.z);
    if ((check & 4) == 4)
      drawLine(colixA, colixC, screenA.x, screenA.y, screenA.z, screenC.x,
          screenC.y, screenC.z);
  }

  @Override
public void drawTriangle(Point3i screenA, Point3i screenB, Point3i screenC,
                           int check) {
    // primary method for unmapped monochromatic Mesh
    if ((check & 1) == 1)
      line3d.plotLine(argbCurrent, !addAllPixels, argbCurrent, !addAllPixels,
          screenA.x, screenA.y, screenA.z, screenB.x, screenB.y, screenB.z,
          true);
    if ((check & 2) == 2)
      line3d.plotLine(argbCurrent, !addAllPixels, argbCurrent, !addAllPixels,
          screenB.x, screenB.y, screenB.z, screenC.x, screenC.y, screenC.z,
          true);
    if ((check & 4) == 4)
      line3d.plotLine(argbCurrent, !addAllPixels, argbCurrent, !addAllPixels,
          screenA.x, screenA.y, screenA.z, screenC.x, screenC.y, screenC.z,
          true);
  }

  /*
  public void drawfillTriangle(int xA, int yA, int zA, int xB,
                               int yB, int zB, int xC, int yC, int zC) {
    // sticks -- sterochemical wedge notation -- not implemented?
    line3d.plotLine(argbCurrent, !addAllPixels, argbCurrent, !addAllPixels, xA,
        yA, zA, xB, yB, zB, true);
    line3d.plotLine(argbCurrent, !addAllPixels, argbCurrent, !addAllPixels, xA,
        yA, zA, xC, yC, zC, true);
    line3d.plotLine(argbCurrent, !addAllPixels, argbCurrent, !addAllPixels, xB,
        yB, zB, xC, yC, zC, true);
    triangle3d.fillTriangle(xA, yA, zA, xB, yB, zB, xC, yC, zC, false);
  }
  */
  @Override
public void fillTriangleTwoSided(short normix,
                           int xScreenA, int yScreenA, int zScreenA,
                           int xScreenB, int yScreenB, int zScreenB,
                           int xScreenC, int yScreenC, int zScreenC) {
    // polyhedra
    setColorNoisy(normix3d.getShadeIndex(normix));
    triangle3d.fillTriangle( xScreenA, yScreenA, zScreenA,
        xScreenB, yScreenB, zScreenB,
        xScreenC, yScreenC, zScreenC, false);
  }

  @Override
public void fillTriangle(Point3f screenA, Point3f screenB, Point3f screenC) {
    // rockets
    setColorNoisy(getShadeIndex(screenA, screenB, screenC));
    triangle3d.fillTriangle(screenA, screenB, screenC, false);
  }

  @Override
public void fillTriangle(Point3i screenA, Point3i screenB, Point3i screenC) {
    // cartoon DNA plates
    triangle3d.fillTriangle(screenA, screenB, screenC, false);
  }

  @Override
public void fillTriangle(Point3i screenA, short colixA,
                                   short normixA, Point3i screenB,
                                   short colixB, short normixB,
                                   Point3i screenC, short colixC,
                                   short normixC, float factor) {
    // isosurface test showing triangles
    boolean useGouraud;
    if (!isPass2 && normixA == normixB && normixA == normixC && colixA == colixB
        && colixA == colixC) {
      setTriangleColixAndShadeIndex(colixA, normix3d.getShadeIndex(normixA));
      useGouraud = false;
    } else {
      if (!setTriangleTranslucency(colixA, colixB, colixC))
        return;
      triangle3d.setGouraud(getShades(colixA)[normix3d.getShadeIndex(normixA)],
          getShades(colixB)[normix3d.getShadeIndex(normixB)],
          getShades(colixC)[normix3d.getShadeIndex(normixC)]);
      useGouraud = true;
    }
    triangle3d.fillTriangle(screenA, screenB, screenC, factor,
        useGouraud);
  }

  @Override
public void fillTriangle(Point3i screenA, short colixA, short normixA,
                           Point3i screenB, short colixB, short normixB,
                           Point3i screenC, short colixC, short normixC) {
    // mesh, isosurface
    boolean useGouraud;
    if (!isPass2 && normixA == normixB && normixA == normixC &&
        colixA == colixB && colixA == colixC) {
      setTriangleColixAndShadeIndex(colixA, normix3d.getShadeIndex(normixA));
      useGouraud = false;
    } else {
      if (!setTriangleTranslucency(colixA, colixB, colixC))
        return;
      triangle3d.setGouraud(getShades(colixA)[normix3d.getShadeIndex(normixA)],
                            getShades(colixB)[normix3d.getShadeIndex(normixB)],
                            getShades(colixC)[normix3d.getShadeIndex(normixC)]);
      useGouraud = true;
    }
    triangle3d.fillTriangle(screenA, screenB, screenC, useGouraud);
  }

  private void setTriangleColixAndShadeIndex(short colix, int shadeIndex) {
    if (colix == colixCurrent && currentShadeIndex == shadeIndex)
      return;
    currentShadeIndex = -1;
    setColix(colix);
    setColorNoisy(shadeIndex);
  }

  private boolean setTriangleTranslucency(short colixA, short colixB, short colixC) {
    if (!isPass2)
      return true;
    int maskA = colixA & Colix.TRANSLUCENT_MASK;
    int maskB = colixB & Colix.TRANSLUCENT_MASK;
    int maskC = colixC & Colix.TRANSLUCENT_MASK;
    maskA &= ~Colix.TRANSPARENT;
    maskB &= ~Colix.TRANSPARENT;
    maskC &= ~Colix.TRANSPARENT;
    //if (maskA == 0 && maskB == 0 && maskC == 0)
      //return false;
    int mask = ((maskA + maskB + maskC) / 3) & Colix.TRANSLUCENT_MASK;
   // System.out.println(mask >> TRANSLUCENT_SHIFT);
    translucencyMask = (mask << Colix.ALPHA_SHIFT) | 0xFFFFFF;
    return true;
  }

  /* ***************************************************************
   * quadrilaterals
   * ***************************************************************/
  
  @Override
public void drawQuadrilateral(short colix, Point3i screenA, Point3i screenB,
                                Point3i screenC, Point3i screenD) {
    //mesh only -- translucency has been checked
    setColix(colix);
    drawLine(screenA, screenB);
    drawLine(screenB, screenC);
    drawLine(screenC, screenD);
    drawLine(screenD, screenA);
  }

  @Override
public void fillQuadrilateral(Point3f screenA, Point3f screenB,
                                Point3f screenC, Point3f screenD) {
    // hermite, rockets, cartoons
    setColorNoisy(getShadeIndex(screenA, screenB, screenC));
    triangle3d.fillTriangle(screenA, screenB, screenC, false);
    triangle3d.fillTriangle(screenA, screenC, screenD, false);
  }

  @Override
public void fillQuadrilateral(Point3i screenA, short colixA, short normixA,
                                Point3i screenB, short colixB, short normixB,
                                Point3i screenC, short colixC, short normixC,
                                Point3i screenD, short colixD, short normixD) {
    // mesh
    fillTriangle(screenA, colixA, normixA,
                 screenB, colixB, normixB,
                 screenC, colixC, normixC);
    fillTriangle(screenA, colixA, normixA,
                 screenC, colixC, normixC,
                 screenD, colixD, normixD);
  }

  @Override
public void drawSurface(MeshSurface meshSurface, short colix) {
    // Export3D only
  }
  
  void plotPixelClipped(int x, int y, int z) {
    //circle3D, drawPixel, plotPixelClipped(point3)
    if (isClipped(x, y, z))
      return;
    int offset = y * width + x;
    if (z < zbuf[offset])
      addPixel(offset, z, argbCurrent);
  }

  @Override
public void plotPixelClipped(Point3i screen) {
    // hermite only
    plotPixelClipped(screen.x, screen.y, screen.z);
  }

  void plotPixelClipped(int argb, int x, int y, int z) {
    // cylinder3d plotRaster
    if (isClipped(x, y, z))
      return;
    int offset = y * width + x;
    if (z < zbuf[offset])
      addPixel(offset, z, argb);
  }

  @Override
public void plotPixelClippedNoSlab(int argb, int x, int y, int z) {
    // drawString via text3d.plotClipped
    if (isClipped(x, y))
      return;
    int offset = y * width + x;
    if (z < zbuf[offset])
      addPixel(offset, z, argb);
  }

  void plotPixelClipped(int argb, boolean isScreened, int x, int y, int z) {
    if (isClipped(x, y, z))
      return;
    if (isScreened && ((x ^ y) & 1) != 0)
      return;
    int offset = y * width + x;
    if (z < zbuf[offset])
      addPixel(offset, z, argb);
  }

  void plotPixelUnclipped(int x, int y, int z) {
    // circle (halo)
    int offset = y * width + x;
    if (z < zbuf[offset])
      addPixel(offset, z, argbCurrent);
  }
  
  void plotPixelUnclipped(int argb, int x, int y, int z) {
    // cylinder plotRaster
    int offset = y * width + x;
    if (z < zbuf[offset])
      addPixel(offset, z, argb);
  }
  
  void plotPixelsClipped(int count, int x, int y, int z) {
    // for circle only; i.e. halo 
    // simple Z/window clip
    if (y < 0 || y >= height || x >= width)
      return;
    if (x < 0) {
      count += x; // x is negative, so this is subtracting -x
      x = 0;
    }
    if (count + x > width)
      count = width - x;
    if (count <= 0)
      return;
    int offsetPbuf = y * width + x;
    int offsetMax = offsetPbuf + count;
    int step = 1;
    if (!addAllPixels) {
      step = 2;
      if (((x ^ y) & 1) != 0)
        ++offsetPbuf;
    }
    while (offsetPbuf < offsetMax) {
      if (z < zbuf[offsetPbuf])
        addPixel(offsetPbuf, z, argbCurrent);
      offsetPbuf += step;
    }
  }

  void plotPixelsClipped(int count, int x, int y, int zAtLeft, int zPastRight,
                         Rgb16 rgb16Left, Rgb16 rgb16Right) {
    // cylinder3d.renderFlatEndcap, triangle3d.fillRaster
    if (count <= 0 || y < 0 || y >= height || x >= width
        || (zAtLeft < slab && zPastRight < slab)
        || (zAtLeft > depth && zPastRight > depth))
      return;
    int seed = (x << 16) + (y << 1) ^ 0x33333333;
    // scale the z coordinates;
    int zScaled = (zAtLeft << 10) + (1 << 9);
    int dz = zPastRight - zAtLeft;
    int roundFactor = count / 2;
    int zIncrementScaled = ((dz << 10) + (dz >= 0 ? roundFactor : -roundFactor))
        / count;
    if (x < 0) {
      x = -x;
      zScaled += zIncrementScaled * x;
      count -= x;
      if (count <= 0)
        return;
      x = 0;
    }
    if (count + x > width)
      count = width - x;
    // when screening 0,0 should be turned ON
    // the first time through this will get flipped to true
    boolean flipflop = ((x ^ y) & 1) != 0;
    int offsetPbuf = y * width + x;
    if (rgb16Left == null) {
      while (--count >= 0) {
        if (addAllPixels || (flipflop = !flipflop) == true) {
          int z = zScaled >> 10;
          if (z >= slab && z <= depth && z < zbuf[offsetPbuf]) {
            seed = ((seed << 16) + (seed << 1) + seed) & 0x7FFFFFFF;
            int bits = (seed >> 16) & 0x07;
            addPixel(offsetPbuf, z, bits == 0 ? argbNoisyDn
                : (bits == 1 ? argbNoisyUp : argbCurrent));
          }
        }
        ++offsetPbuf;
        zScaled += zIncrementScaled;
      }
    } else {
      int rScaled = rgb16Left.rScaled << 8;
      int rIncrement = ((rgb16Right.rScaled - rgb16Left.rScaled) << 8) / count;
      int gScaled = rgb16Left.gScaled;
      int gIncrement = (rgb16Right.gScaled - gScaled) / count;
      int bScaled = rgb16Left.bScaled;
      int bIncrement = (rgb16Right.bScaled - bScaled) / count;
      while (--count >= 0) {
        if (addAllPixels || (flipflop = !flipflop)) {
          int z = zScaled >> 10;
          if (z >= slab && z <= depth && z < zbuf[offsetPbuf])
            addPixel(offsetPbuf, z, 0xFF000000 | (rScaled & 0xFF0000)
                | (gScaled & 0xFF00) | ((bScaled >> 8) & 0xFF));
        }
        ++offsetPbuf;
        zScaled += zIncrementScaled;
        rScaled += rIncrement;
        gScaled += gIncrement;
        bScaled += bIncrement;
      }
    }
  }

  /*
   final static boolean ENABLE_GOURAUD_STATS = false;
   static int totalGouraud;
   static int shortCircuitGouraud;

   void plotPixelsUnclipped(int count, int x, int y, int zAtLeft,
   int zPastRight, Rgb16 rgb16Left, Rgb16 rgb16Right) {
   // for Triangle3D.fillRaster
   if (count <= 0)
   return;
   int seed = (x << 16) + (y << 1) ^ 0x33333333;
   // scale the z coordinates;
   int zScaled = (zAtLeft << 10) + (1 << 9);
   int dz = zPastRight - zAtLeft;
   int roundFactor = count / 2;
   int zIncrementScaled = ((dz << 10) + (dz >= 0 ? roundFactor : -roundFactor))
   / count;
   int offsetPbuf = y * width + x;
   if (rgb16Left == null) {
   if (!isTranslucent) {
   while (--count >= 0) {
   int z = zScaled >> 10;
   if (z < zbuf[offsetPbuf]) {
   zbuf[offsetPbuf] = z;
   seed = ((seed << 16) + (seed << 1) + seed) & 0x7FFFFFFF;
   int bits = (seed >> 16) & 0x07;
   pbuf[offsetPbuf] = (bits == 0 ? argbNoisyDn
   : (bits == 1 ? argbNoisyUp : argbCurrent));
   }
   ++offsetPbuf;
   zScaled += zIncrementScaled;
   }
   } else {
   boolean flipflop = ((x ^ y) & 1) != 0;
   while (--count >= 0) {
   flipflop = !flipflop;
   if (flipflop) {
   int z = zScaled >> 10;
   if (z < zbuf[offsetPbuf]) {
   zbuf[offsetPbuf] = z;
   seed = ((seed << 16) + (seed << 1) + seed) & 0x7FFFFFFF;
   int bits = (seed >> 16) & 0x07;
   pbuf[offsetPbuf] = (bits == 0 ? argbNoisyDn
   : (bits == 1 ? argbNoisyUp : argbCurrent));
   }
   }
   ++offsetPbuf;
   zScaled += zIncrementScaled;
   }
   }
   } else {
   boolean flipflop = ((x ^ y) & 1) != 0;
   if (ENABLE_GOURAUD_STATS) {
   ++totalGouraud;
   int i = count;
   int j = offsetPbuf;
   int zMin = zAtLeft < zPastRight ? zAtLeft : zPastRight;

   if (!isTranslucent) {
   for (; zbuf[j] < zMin; ++j)
   if (--i == 0) {
   if ((++shortCircuitGouraud % 100000) == 0)
   Logger.debug("totalGouraud=" + totalGouraud
   + " shortCircuitGouraud=" + shortCircuitGouraud + " %="
   + (100.0 * shortCircuitGouraud / totalGouraud));
   return;
   }
   } else {
   if (flipflop) {
   ++j;
   if (--i == 0)
   return;
   }
   for (; zbuf[j] < zMin; j += 2) {
   i -= 2;
   if (i <= 0) {
   if ((++shortCircuitGouraud % 100000) == 0)
   Logger.debug("totalGouraud=" + totalGouraud
   + " shortCircuitGouraud=" + shortCircuitGouraud + " %="
   + (100.0 * shortCircuitGouraud / totalGouraud));
   return;
   }
   }
   }
   }

   int rScaled = rgb16Left.rScaled << 8;
   int rIncrement = ((rgb16Right.rScaled - rgb16Left.rScaled) << 8) / count;
   int gScaled = rgb16Left.gScaled;
   int gIncrement = (rgb16Right.gScaled - gScaled) / count;
   int bScaled = rgb16Left.bScaled;
   int bIncrement = (rgb16Right.bScaled - bScaled) / count;
   while (--count >= 0) {
   if (!isTranslucent || (flipflop = !flipflop)) {
   int z = zScaled >> 10;
   if (z < zbuf[offsetPbuf]) {
   zbuf[offsetPbuf] = z;
   pbuf[offsetPbuf] = (0xFF000000 | (rScaled & 0xFF0000)
   | (gScaled & 0xFF00) | ((bScaled >> 8) & 0xFF));
   }
   }
   ++offsetPbuf;
   zScaled += zIncrementScaled;
   rScaled += rIncrement;
   gScaled += gIncrement;
   bScaled += bIncrement;
   }
   }
   }
   */
  ///////////////////////////////////
  void plotPixelsUnclipped(int count, int x, int y, int zAtLeft,
                           int zPastRight, Rgb16 rgb16Left, Rgb16 rgb16Right) {
    // for isosurface Triangle3D.fillRaster
    if (count <= 0)
      return;
    int seed = (x << 16) + (y << 1) ^ 0x33333333;
    boolean flipflop = ((x ^ y) & 1) != 0;
    // scale the z coordinates;
    int zScaled = (zAtLeft << 10) + (1 << 9);
    int dz = zPastRight - zAtLeft;
    int roundFactor = count / 2;
    int zIncrementScaled = ((dz << 10) + (dz >= 0 ? roundFactor : -roundFactor))
        / count;
    int offsetPbuf = y * width + x;
    if (rgb16Left == null) {
      while (--count >= 0) {
        if (addAllPixels || (flipflop = !flipflop)) {
          int z = zScaled >> 10;
          if (z < zbuf[offsetPbuf]) {
            seed = ((seed << 16) + (seed << 1) + seed) & 0x7FFFFFFF;
            int bits = (seed >> 16) & 0x07;
            addPixel(offsetPbuf, z, bits == 0 ? argbNoisyDn
                : (bits == 1 ? argbNoisyUp : argbCurrent));
          }
        }
        ++offsetPbuf;
        zScaled += zIncrementScaled;
      }
    } else {
      int rScaled = rgb16Left.rScaled << 8;
      int rIncrement = ((rgb16Right.rScaled - rgb16Left.rScaled) << 8) / count;
      int gScaled = rgb16Left.gScaled;
      int gIncrement = (rgb16Right.gScaled - gScaled) / count;
      int bScaled = rgb16Left.bScaled;
      int bIncrement = (rgb16Right.bScaled - bScaled) / count;
      while (--count >= 0) {
        if (addAllPixels || (flipflop = !flipflop)) {
          int z = zScaled >> 10;
          if (z < zbuf[offsetPbuf])
            addPixel(offsetPbuf, z, 0xFF000000 | (rScaled & 0xFF0000)
                | (gScaled & 0xFF00) | ((bScaled >> 8) & 0xFF));
        }
        ++offsetPbuf;
        zScaled += zIncrementScaled;
        rScaled += rIncrement;
        gScaled += gIncrement;
        bScaled += bIncrement;
      }
    }
  }

  ///////////////////////////////
  void plotPixelsUnclipped(int count, int x, int y, int z) {
    
    // for Cirle3D.plot8Filled and fillRect
    
    int offsetPbuf = y * width + x;
    if (addAllPixels) {
      while (--count >= 0) {
        if (z < zbuf[offsetPbuf])
          addPixel(offsetPbuf, z, argbCurrent);
        ++offsetPbuf;
      }
    } else {
      int offsetMax = offsetPbuf + count;
      if (((x ^ y) & 1) != 0)
        if (++offsetPbuf == offsetMax)
          return;
      do {
        if (z < zbuf[offsetPbuf])
          addPixel(offsetPbuf, z, argbCurrent);
        offsetPbuf += 2;
      } while (offsetPbuf < offsetMax);
    }
  }

  private void plotPoints(int count, int[] coordinates, int xOffset, int yOffset) {
    for (int i = count * 3; i > 0; ) {
      int z = coordinates[--i];
      int y = coordinates[--i] + yOffset;
      int x = coordinates[--i] + xOffset;
      if (isClipped(x, y, z))
        continue;
      int offset = y * width + x++;
      if (z < zbuf[offset])
        addPixel(offset, z, argbCurrent);
      if (antialiasThisFrame) {
        offset = y * width + x;
        if (!isClipped(x, y, z) && z < zbuf[offset])
          addPixel(offset, z, argbCurrent);
        offset = (++y)* width + x;
        if (!isClipped(x, y, z) && z < zbuf[offset])
          addPixel(offset, z, argbCurrent);
        offset = y * width + (--x);
        if (!isClipped(x, y, z) && z < zbuf[offset])
          addPixel(offset, z, argbCurrent);
      }

    }
  }

  
  private final Vector3f vectorAB = new Vector3f();
  private final Vector3f vectorAC = new Vector3f();
  private final Vector3f vectorNormal = new Vector3f();

  void setColorNoisy(int shadeIndex) {
    currentShadeIndex = shadeIndex;
    argbCurrent = shadesCurrent[shadeIndex];
    argbNoisyUp = shadesCurrent[shadeIndex < Shader.shadeIndexLast ? shadeIndex + 1
        : Shader.shadeIndexLast];
    argbNoisyDn = shadesCurrent[shadeIndex > 0 ? shadeIndex - 1 : 0];
  }

  /**
   *  used by CartoonRenderer (DNA surface) and GeoSurfaceRenderer (face) to
   *  assign a noisy shade to the surface it will render
   * @param screenA 
   * @param screenB 
   * @param screenC 
   */
  @Override
public void setNoisySurfaceShade(Point3i screenA, Point3i screenB, Point3i screenC) {
    vectorAB.set(screenB.x - screenA.x, screenB.y - screenA.y, screenB.z
        - screenA.z);
    int shadeIndex;
    if (screenC == null) {
      shadeIndex = Shader.getShadeIndex(-vectorAB.x, -vectorAB.y, vectorAB.z);
    } else {
      vectorAC.set(screenC.x - screenA.x, screenC.y - screenA.y, screenC.z
          - screenA.z);
      vectorAB.cross(vectorAB, vectorAC);
      shadeIndex = vectorAB.z >= 0 ? Shader.getShadeIndex(-vectorAB.x,
          -vectorAB.y, vectorAB.z) : Shader.getShadeIndex(vectorAB.x,
          vectorAB.y, -vectorAB.z);
    }
    if (shadeIndex > Shader.shadeIndexNoisyLimit)
      shadeIndex = Shader.shadeIndexNoisyLimit;
    setColorNoisy(shadeIndex);
  }

  private int getShadeIndex(Point3f screenA,
                                 Point3f screenB, Point3f screenC) {
    // for fillTriangle and fillQuad.
    vectorAB.sub(screenB, screenA);
    vectorAC.sub(screenC, screenA);
    vectorNormal.cross(vectorAB, vectorAC);
    return
      (vectorNormal.z >= 0
            ? Shader.getShadeIndex(-vectorNormal.x, -vectorNormal.y,
                                    vectorNormal.z)
            : Shader.getShadeIndex(vectorNormal.x, vectorNormal.y,
                                    -vectorNormal.z));
  }
    
  @Override
public boolean isDirectedTowardsCamera(short normix) {
    //polyhedra renderer
    return normix3d.isDirectedTowardsCamera(normix);
  }

  @Override
public Vector3f[] getTransformedVertexVectors() {
    // mesh renderer
    return normix3d.getTransformedVectors();
  }

  //////////////////////////////////////////////////////////
  
  @Override
public void renderBackground(JmolRendererInterface jmolRenderer) {
    if (backgroundImage != null)
      plotImage(Integer.MIN_VALUE, 0, Integer.MIN_VALUE, backgroundImage,
          jmolRenderer, (short) 0, 0, 0);
  }

  @Override
public void drawAtom(Atom atom) {
    fillSphere(atom.screenDiameter,
        atom.screenX, atom.screenY, atom.screenZ);
	//  fillSphere(200,100,100,100);
    
  }

  // implemented only for Export3D:

  public final static int EXPORT_NOT = 0;
  public final static int EXPORT_CARTESIAN = 1;
  public final static int EXPORT_RAYTRACER = 2;
  
  @Override
public int getExportType() {
    return EXPORT_NOT;
  }

  @Override
public String getExportName() {
    return null;
  }

  public boolean canDoTriangles() {
    return true;
  }
  
  public boolean isCartesianExport() {
    return false;
  }

  @Override
public boolean initializeExporter(String type, Viewer viewer, double privateKey, GData g3d,
                                    Object output) {
    return false;
  }

  @Override
public String finalizeOutput() {
    return null;
  }

  @Override
public void drawBond(Atom atomA, Atom atomB, short colixA, short colixB,
                           byte endcaps, short mad) {
  }

  @Override
public boolean drawEllipse(Point3f ptAtom, Point3f ptX, Point3f ptY,
                           boolean fillArc, boolean wireframeOnly) {
    return false;
  }

  public double getPrivateKey() {
    // exporter only
    return 0;
  }

  @Override
  public void clearFontCache() {
    TextRenderer.clearFontCache();
  }


}
