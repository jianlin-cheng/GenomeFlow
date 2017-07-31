package org.jmol.util;


import javax.vecmath.Matrix3f;
import javax.vecmath.Point3f;

import org.jmol.api.ApiPlatform;
import org.jmol.api.JmolGraphicsInterface;
import org.jmol.constant.EnumStereoMode;

public class GData implements JmolGraphicsInterface {

  private static void flushCaches() {
    Colix.flushShades();
    Shader.flushSphereCache();
  }

  public static int getAmbientPercent() {
    return Shader.ambientPercent;
  }
  public static int getDiffusePercent() {
    return Shader.diffusePercent;
  }
  // {"Plain", "Bold", "Italic", "BoldItalic"};
  public static int getFontStyleID(String fontStyle) {
    return JmolFont.getFontStyleID(fontStyle);
  }
  public static Point3f getLightSource() {
    return new Point3f(Shader.xLight, Shader.yLight, Shader.zLight);
  }

  public static int getPhongExponent() {
    return Shader.phongExponent;
  }

  public static boolean getSpecular() {
    return Shader.specularOn;
  }
  
  public static int getSpecularExponent() {
    return Shader.specularExponent;
  }
  
  public static int getSpecularPercent() {
    return Shader.specularPercent;
  }
  public static int getSpecularPower() {
    return Shader.specularPower;
  }

  public static int getZShadePower() {
    return Shader.zPower;
  }  
  /**
   * fractional distance from black for ambient color
   * 
   * @param val
   */
  public synchronized static void setAmbientPercent(int val) {
    if (Shader.ambientPercent == val)
      return;
    Shader.ambientPercent = val;
    Shader.ambientFraction = val / 100f;
    flushCaches();
  }
  /**
   * df in I = df * (N dot L) + sf * (R dot V)^p
   * 
   * @param val
   */
  public synchronized static void setDiffusePercent(int val) {
    if (Shader.diffusePercent == val)
      return;
    Shader.diffusePercent = val;
    Shader.diffuseFactor = val / 100f;
    flushCaches();
  }
  /**
   * p in I = df * (N dot L) + sf * (R dot V)^p
   * 
   * @param val
   */
  public synchronized static void setPhongExponent(int val) {
    if (Shader.phongExponent == val && Shader.usePhongExponent)
      return;
    Shader.phongExponent = val;
    float x = (float) (Math.log(val) / Math.log(2));
    Shader.usePhongExponent = (x != (int) x);
    if (!Shader.usePhongExponent)
      Shader.specularExponent = (int) x;
    flushCaches();
  }
  public synchronized static void setSpecular(boolean val) {
    if (Shader.specularOn == val)
      return;
    Shader.specularOn = val;
    flushCaches();
  }


  /**
   * log_2(p) in I = df * (N dot L) + sf * (R dot V)^p for faster calculation of
   * shades
   * 
   * @param val
   */
  public synchronized static void setSpecularExponent(int val) {
    if (Shader.specularExponent == val)
      return;
    Shader.specularExponent = val;
    Shader.phongExponent = (int) Math.pow(2, val);
    Shader.usePhongExponent = false;
    flushCaches();
  }

  /**
   * sf in I = df * (N dot L) + sf * (R dot V)^p not a percent of anything,
   * really
   * 
   * @param val
   */
  public synchronized static void setSpecularPercent(int val) {
    if (Shader.specularPercent == val)
      return;
    Shader.specularPercent = val;
    Shader.specularFactor = val / 100f;
    flushCaches();
  }
  /**
   * fractional distance to white for specular dot
   * 
   * @param val
   */
  public synchronized static void setSpecularPower(int val) {
    if (val < 0) {
      setSpecularExponent(-val);
      return;
    }
    if (Shader.specularPower == val)
      return;
    Shader.specularPower = val;
    Shader.intenseFraction = val / 100f;
    flushCaches();
  }
  /**
   * fractional distance from black for ambient color
   * 
   * @param val
   */
  public synchronized static void setZShadePower(int val) {
    Shader.zPower = val;
  }
  public ApiPlatform apiPlatform;
  protected int windowWidth, windowHeight;
  
  protected int displayMinX, displayMaxX, displayMinY, displayMaxY;

  protected boolean antialiasThisFrame;
  
  protected boolean antialiasEnabled;

  protected boolean inGreyscaleMode;

  protected short[] changeableColixMap = new short[16];

  protected Object backgroundImage;

  protected int newWindowWidth, newWindowHeight;

  protected boolean newAntialiasing;

  public int bgcolor;

  public int xLast, yLast;

  public int slab, depth;

  public int width, height;


  public int zSlab, zDepth;

  public int bufferSize;

  public final static byte ENDCAPS_NONE = 0;

  public final static byte ENDCAPS_OPEN = 1;

  public final static byte ENDCAPS_FLAT = 2;

  public final static byte ENDCAPS_SPHERICAL = 3;

  public final static byte ENDCAPS_OPENEND = 4;

  public static final short NORMIX_NULL = Normix.NORMIX_NULL;

  public int zShadeR, zShadeG, zShadeB;

  protected Object graphicsForMetrics;

  final public static int yGT = 1;

  final public static int yLT = 2;

  final public static int xGT = 4;

  final public static int xLT = 8;

  final public static int zGT = 16;

  final public static int zLT = 32;

  public GData() {
    
  }

  /**
   * @param stereoMode  
   * @param stereoColors 
   */
  public void applyAnaglygh(EnumStereoMode stereoMode, int[] stereoColors) {
  }

  /**
   * @param stereoRotationMatrix  
   */
  public void beginRendering(Matrix3f stereoRotationMatrix) {
  }

  public void changeColixArgb(short id, int argb) {
    if (id < changeableColixMap.length && changeableColixMap[id] != 0)
      changeableColixMap[id] = Colix.getColix(argb);
  }

  public void clearFontCache() {
  }

  public int clipCode(int z) {
    int code = 0;
    if (z < slab)
      code |= zLT;
    else if (z > depth) // note that this is .GT., not .GE.
      code |= zGT;
    return code;
  }

  public int clipCode(int x, int y, int z) {
    int code = 0;
    if (x < 0)
      code |= xLT;
    else if (x >= width)
      code |= xGT;
    if (y < 0)
      code |= yLT;
    else if (y >= height)
      code |= yGT;
    if (z < slab)
      code |= zLT;
    else if (z > depth) // note that this is .GT., not .GE.
      code |= zGT;

    return code;
  }

  public void destroy() {
  }

  public void endRendering() {
  }

  public short[] getBgColixes(short[] bgcolixes) {
    return bgcolixes;
  }

  public short getChangeableColix(short id, int argb) {
    if (id >= changeableColixMap.length) {
      short[] t = new short[id + 16];
      System.arraycopy(changeableColixMap, 0, t, 0, changeableColixMap.length);
      changeableColixMap = t;
    }
    if (changeableColixMap[id] == 0)
      changeableColixMap[id] = Colix.getColix(argb);
    return (short) (id | Colix.CHANGEABLE_MASK);
  }

  @Override
public int getColorArgbOrGray(short colix) {
    if (colix < 0)
      colix = changeableColixMap[colix & Colix.UNMASK_CHANGEABLE_TRANSLUCENT];
    return (inGreyscaleMode ? Colix.getArgbGreyscale(colix) : Colix.getArgb(colix));
  }

  /**
   * gets g3d depth
   * 
   * @return depth
   */
  @Override
public int getDepth() {
    return depth;
  }

  public JmolFont getFont3D(float fontSize) {
    return JmolFont.getFont3D(JmolFont.FONT_FACE_SANS, JmolFont.FONT_STYLE_PLAIN,
        fontSize, fontSize, apiPlatform, graphicsForMetrics);
  }

  public JmolFont getFont3D(String fontFace, float fontSize) {
    return JmolFont.getFont3D(JmolFont.getFontFaceID(fontFace),
        JmolFont.FONT_STYLE_PLAIN, fontSize, fontSize, apiPlatform, graphicsForMetrics);
  }

  public JmolFont getFont3D(String fontFace, String fontStyle, float fontSize) {
    int iStyle = JmolFont.getFontStyleID(fontStyle);
    if (iStyle < 0)
      iStyle = 0;
    return JmolFont.getFont3D(JmolFont.getFontFaceID(fontFace), iStyle, fontSize,
        fontSize, apiPlatform, graphicsForMetrics);
  }
  @Override
public JmolFont getFont3DScaled(JmolFont font, float scale) {
    // TODO: problem here is that we are assigning a bold font, then not DEassigning it
    float newScale = font.fontSizeNominal * scale;
    return (newScale == font.fontSize ? font : JmolFont.getFont3D(
        font.idFontFace, (antialiasThisFrame ? font.idFontStyle | 1
            : font.idFontStyle), newScale, font.fontSizeNominal, apiPlatform, graphicsForMetrics));
  }
  @Override
public byte getFontFid(float fontSize) {
    return getFont3D(fontSize).fid;
  }
  public byte getFontFid(String fontFace, float fontSize) {
    return getFont3D(fontFace, fontSize).fid;
  }
  /**
   * gets g3d height
   * 
   * @return height pixel count
   */
  @Override
public int getRenderHeight() {
    return height;
  }
  /**
   * gets g3d width
   * 
   * @return width pixel count;
   */
  @Override
public int getRenderWidth() {
    return width;
  }

  public Object getScreenImage() {
    return null;
  }

  public int[] getShades(short colix) {
    if (colix < 0)
      colix = changeableColixMap[colix & Colix.UNMASK_CHANGEABLE_TRANSLUCENT];
    return (inGreyscaleMode ? Colix.getShadesGreyscale(colix) : Colix
        .getShades(colix));
  }

  /* ***************************************************************
   * fontID stuff
   * a fontID is a byte that contains the size + the face + the style
   * ***************************************************************/

  /**
   * gets g3d slab
   * 
   * @return slab
   */
  @Override
public int getSlab() {
    return slab;
  }

  public void initialize(ApiPlatform apiPlatform) {
    this.apiPlatform = apiPlatform;
  }

  /**
   * is full scene / oversampling antialiasing in effect
   * 
   * @return the answer
   */
  @Override
public boolean isAntialiased() {
    return antialiasThisFrame;
  }

  public boolean isClipped(int x, int y) {
    return (x < 0 || x >= width || y < 0 || y >= height);
  }

  public boolean isClipped(int x, int y, int z) {
    // this is the one that could be augmented with slabPlane
    return (x < 0 || x >= width || y < 0 || y >= height || z < slab || z > depth);
  }

  @Override
public boolean isClippedXY(int diameter, int x, int y) {
    int r = (diameter + 1) >> 1;
    return (x < -r || x >= width + r || y < -r || y >= height + r);
  }

  @Override
public boolean isClippedZ(int z) {
    return (z != Integer.MIN_VALUE && (z < slab || z > depth));
  }

  /**
   * is full scene / oversampling antialiasing GENERALLY in effect
   *
   * @return the answer
   */
  public boolean isDisplayAntialiased() {
    return antialiasEnabled;
  }

  @Override
public boolean isInDisplayRange(int x, int y) {
    return (x >= displayMinX && x < displayMaxX && y >= displayMinY && y < displayMaxY);
  }

  public void releaseScreenImage() {
  }

  /**
   * sets background color to the specified argb value
   *
   * @param argb an argb value with alpha channel
   */
  public void setBackgroundArgb(int argb) {
    bgcolor = argb;
    // background of Jmol transparent in front of certain applications (VLC Player)
    // when background [0,0,1]. 
  }

  public void setBackgroundImage(Object image) {
    backgroundImage = image;
  }

  /**
   * @param TF  
   */
  public void setBackgroundTransparent(boolean TF) {
  }

  /**
   * clipping from the front and the back
   *<p>
   * the plane is defined as a percentage from the back of the image to the
   * front
   *<p>
   * for depth values:
   * <ul>
   * <li>0 means 100% is shown
   * <li>25 means the back 25% is <i>not</i> shown
   * <li>50 means the back half is <i>not</i> shown
   * <li>100 means that nothing is shown
   * </ul>
   *<p>
   * 
   * @param depthValue
   *        rear clipping percentage [0,100]
   */
  @Override
public void setDepth(int depthValue) {
    depth = depthValue < 0 ? 0 : depthValue;
  }

  /**
   * controls greyscale rendering
   * 
   * @param greyscaleMode
   *        Flag for greyscale rendering
   */
  public void setGreyscaleMode(boolean greyscaleMode) {
    this.inGreyscaleMode = greyscaleMode;
  }

  public void setNewWindowParametersForExport() {
    windowWidth = newWindowWidth;
    windowHeight = newWindowHeight;
    setWidthHeight(false);
  }

  /**
   * @param antialias  
   * @return true if need a second (translucent) pass
   */
  public boolean setPass2(boolean antialias) {
    return false;
  }

  /**
   * clipping from the front and the back
   *<p>
   * the plane is defined as a percentage from the back of the image to the
   * front
   *<p>
   * For slab values:
   * <ul>
   * <li>100 means 100% is shown
   * <li>75 means the back 75% is shown
   * <li>50 means the back half is shown
   * <li>0 means that nothing is shown
   * </ul>
   *<p>
   * 
   * @param slabValue
   *        front clipping percentage [0,100]
   */
  @Override
public void setSlab(int slabValue) {
    slab = slabValue < 0 ? 0 : slabValue;
  }

  protected void setWidthHeight(boolean isAntialiased) {
    width = windowWidth;
    height = windowHeight;
    if (isAntialiased) {
      width <<= 1;
      height <<= 1;
    }
    xLast = width - 1;
    yLast = height - 1;
    displayMinX = -(width >> 1);
    displayMaxX = width - displayMinX;
    displayMinY = -(height >> 1);
    displayMaxY = height - displayMinY;
    bufferSize = width * height;
  }

  public void setWindowParameters(int width, int height, boolean antialias) {
    newWindowWidth = width;
    newWindowHeight = height;
    newAntialiasing = antialias;    
  }

  /**
   * @param zShade
   *        whether to shade along z front to back
   * @param zSlab
   *        for zShade
   * @param zDepth
   *        for zShade
   */
  public void setZShade(boolean zShade, int zSlab, int zDepth) {
    if (zShade) {
      zShadeR = bgcolor & 0xFF;
      zShadeG = (bgcolor & 0xFF00) >> 8;
      zShadeB = (bgcolor & 0xFF0000) >> 16;
      this.zSlab = zSlab < 0 ? 0 : zSlab;
      this.zDepth = zDepth < 0 ? 0 : zDepth;
    }
  }

  public void snapshotAnaglyphChannelBytes() {
  }

}
