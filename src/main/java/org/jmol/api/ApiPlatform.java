package org.jmol.api;

import javax.vecmath.Point3f;

import org.jmol.util.JmolFont;
import org.jmol.viewer.ActionManager;
import org.jmol.viewer.Viewer;

public interface ApiPlatform {

  void setViewer(JmolViewer viewer, Object display);
  
  /////// Display

  boolean isHeadless();
  
  void convertPointFromScreen(Object display, Point3f ptTemp);

  void getFullScreenDimensions(Object display, int[] widthHeight);
  
  boolean hasFocus(Object display);

  String prompt(String label, String data, String[] list, boolean asButtons);

  void repaint(Object display);

  void requestFocusInWindow(Object display);

  void setCursor(int i, Object display);

  void setTransparentCursor(Object display);

  ////  Mouse 

  JmolMouseInterface getMouseManager(Viewer viewer, ActionManager actionManager);

  ///// Font
  
  int fontStringWidth(Object fontMetrics, String text);

  int getFontAscent(Object fontMetrics);

  int getFontDescent(Object fontMetrics);

  Object getFontMetrics(Object graphics, Object font);

  Object newFont(String fontFace, boolean isBold, boolean isItalic, float fontSize);

  ///// core Image handling
  
  Object allocateRgbImage(int windowWidth, int windowHeight, int[] pBuffer,
                          int windowSize, boolean backgroundTransparent);

  void disposeGraphics(Object graphicForText);

  void drawImage(Object g, Object img, int x, int y, int width, int height);

  int[] drawImageToBuffer(Object gObj, Object imageOffscreen,
                          Object image, int width, int height, int bgcolor);

  void flushImage(Object imagePixelBuffer);

  Object getStaticGraphics(Object image, boolean backgroundTransparent);

  Object getGraphics(Object image1);

  int getImageWidth(Object image);

  int getImageHeight(Object image);

  Object newBufferedImage(Object image, int i, int height);

  Object newBufferedRgbImage(int w, int h);
  
  void renderScreenImage(JmolViewer jmolViewer, Object g, Object currentSize);

  int[] getTextPixels(String text, JmolFont font3d, Object gObj,
                      Object image, int mapWidth, int height,
                      int ascent);

  ///// Image creation for export (optional for any platform)

  /**
   * can be ignored (return null) if platform cannot save images
   * 
   * @param ret
   * @return     null only if this platform cannot save images
   */
  Object createImage(Object ret);

  /**
   * used for JPG writing only; can be ignored
   * 
   * @param viewer
   * @param quality
   * @param comment
   * @return    null only if this platform cannot save images
   */
  Object getJpgImage(Viewer viewer, int quality, String comment);

  /**
   * 
   * @param image
   * @param width
   * @param height
   * @return         pixels
   */
  int[] grabPixels(Object image, int width, int height);

  /**
   * can be ignored (return false) if platform cannot save images
   * 
   * @param display
   * @param image
   * @return        false only if this platform cannot save images
   * @throws InterruptedException
   */
  boolean waitForDisplay(Object display, Object image) throws InterruptedException;

  JmolPopupInterface getMenuPopup(Viewer viewer, String menuStructure, char type);

  Object getJsObjectInfo(Object jsObject, String method, Object[] args);

  JmolFileAdapterInterface getFileAdapter();

  JmolFileInterface newFile(String name);

}
