package org.jmol.awt;

import java.awt.GraphicsEnvironment;

import javax.vecmath.Point3f;

import netscape.javascript.JSObject;

import org.jmol.api.ApiPlatform;
import org.jmol.api.Interface;
import org.jmol.api.JmolFileAdapterInterface;
import org.jmol.api.JmolFileInterface;
import org.jmol.api.JmolMouseInterface;
import org.jmol.api.JmolPopupInterface;
import org.jmol.api.JmolViewer;
import org.jmol.util.JmolFont;
import org.jmol.viewer.ActionManager;
import org.jmol.viewer.Viewer;

public class Platform implements ApiPlatform {

  @Override
public void setViewer(JmolViewer viewer, Object display) {
    // ignored
  }
  
  ///// Display 

  @Override
public void convertPointFromScreen(Object display, Point3f ptTemp) {
    Display.convertPointFromScreen(display, ptTemp);
  }

  @Override
public void getFullScreenDimensions(Object display, int[] widthHeight) {
    Display.getFullScreenDimensions(display, widthHeight);        
  }
  
  @Override
public JmolPopupInterface getMenuPopup(Viewer viewer, String menuStructure, char type) {
    JmolPopupInterface jmolpopup = (JmolPopupInterface) Interface.getOptionInterface(
        type == 'j' ? "popup.JmolPopup" : "modelkit.ModelKitPopup");
    if (jmolpopup != null)
      jmolpopup.initialize(viewer, menuStructure);
    return jmolpopup;
  }

  @Override
public boolean hasFocus(Object display) {
    return Display.hasFocus(display);
  }

  @Override
public String prompt(String label, String data, String[] list,
                       boolean asButtons) {
    return Display.prompt(label, data, list, asButtons);
  }

  /**
   * legacy apps will use this
   * 
   * @param viewer
   * @param g
   * @param size
   */
  @Override
public void renderScreenImage(JmolViewer viewer, Object g, Object size) {
    Display.renderScreenImage(viewer, g, size);
  }

  @Override
public void requestFocusInWindow(Object display) {
    Display.requestFocusInWindow(display);
  }

  @Override
public void repaint(Object display) {
    Display.repaint(display);
  }

  @Override
public void setTransparentCursor(Object display) {
    Display.setTransparentCursor(display);
  }

  @Override
public void setCursor(int c, Object display) {
    Display.setCursor(c, display);
  }

  @Override
public JmolFileAdapterInterface getFileAdapter() {
    return new JmolFileAdapter();
  }

  ////// Mouse

  @Override
public JmolMouseInterface getMouseManager(Viewer viewer, ActionManager actionManager) {
    return new Mouse(viewer, actionManager);
  }

  ////// Image 

  @Override
public Object allocateRgbImage(int windowWidth, int windowHeight,
                                 int[] pBuffer, int windowSize,
                                 boolean backgroundTransparent) {
    return Image.allocateRgbImage(windowWidth, windowHeight, pBuffer, windowSize, backgroundTransparent);
  }

  @Override
public Object createImage(Object data) {
    return Image.createImage(data);
  }

  @Override
public void disposeGraphics(Object gOffscreen) {
    Image.disposeGraphics(gOffscreen);
  }

  @Override
public void drawImage(Object g, Object img, int x, int y, int width, int height) {
    Image.drawImage(g, img, x, y, width, height);
  }

  @Override
public int[] grabPixels(Object imageobj, int width, int height) {
    return Image.grabPixels(imageobj, width, height); 
  }

  @Override
public int[] drawImageToBuffer(Object gOffscreen, Object imageOffscreen,
                                 Object imageobj, int width, int height, int bgcolor) {
    return Image.drawImageToBuffer(gOffscreen, imageOffscreen, imageobj, width, height, bgcolor);
  }

  @Override
public int[] getTextPixels(String text, JmolFont font3d, Object gObj,
                             Object image, int width, int height, int ascent) {
    return Image.getTextPixels(text, font3d, gObj, image, width, height, ascent);
  }

  @Override
public void flushImage(Object imagePixelBuffer) {
    Image.flush(imagePixelBuffer);
  }

  @Override
public Object getGraphics(Object image) {
    return Image.getGraphics(image);
  }

  @Override
public int getImageHeight(Object image) {
    return Image.getHeight(image);
  }

  @Override
public int getImageWidth(Object image) {
    return Image.getWidth(image);
  }

  @Override
public Object getJpgImage(Viewer viewer, int quality, String comment) {
    return Image.getJpgImage(this, viewer, quality, comment);
  }

  @Override
public Object getStaticGraphics(Object image, boolean backgroundTransparent) {
    return Image.getStaticGraphics(image, backgroundTransparent);
  }

  @Override
public Object newBufferedImage(Object image, int w, int h) {
    return Image.newBufferedImage(image, w, h);
  }

  @Override
public Object newBufferedRgbImage(int w, int h) {
    return Image.newBufferedImage(w, h);
  }

  @Override
public boolean waitForDisplay(Object display, Object image) throws InterruptedException {
    Image.waitForDisplay(display, image);
    return true;
  }

  
  ///// FONT
  
  @Override
public int fontStringWidth(Object fontMetrics, String text) {
    return Font.stringWidth(fontMetrics, text);
  }

  @Override
public int getFontAscent(Object fontMetrics) {
    return Font.getAscent(fontMetrics);
  }

  @Override
public int getFontDescent(Object fontMetrics) {
    return Font.getDescent(fontMetrics);
  }

  @Override
public Object getFontMetrics(Object graphics, Object font) {
    return Font.getFontMetrics(graphics, font);
  }

  @Override
public Object newFont(String fontFace, boolean isBold, boolean isItalic, float fontSize) {
    return Font.newFont(fontFace, isBold, isItalic, fontSize);
  }

  /// misc

  @Override
public Object getJsObjectInfo(Object jsObject, String method, Object[] args) {
    JSObject DOMNode = (JSObject) jsObject;
    if (method == null) {
      String namespaceURI = (String) DOMNode.getMember("namespaceURI");
      String localName = (String) DOMNode.getMember("localName");
      return "namespaceURI=\"" + namespaceURI + "\" localName=\"" + localName + "\"";
    }
    return (args == null ? DOMNode.getMember(method) : DOMNode.call(method, args));
  }

  @Override
public boolean isHeadless() {
    return GraphicsEnvironment.isHeadless();
  }

  @Override
public JmolFileInterface newFile(String name) {
    return JmolFileAdapter.newFile(name);
  }

}
