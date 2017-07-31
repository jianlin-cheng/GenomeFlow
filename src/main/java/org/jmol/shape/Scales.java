/*
 * added -hcf
 * this class is for displaying the current scale information
 */
package org.jmol.shape;

import org.jmol.util.JmolFont;

public class Scales extends FontShape {

  final static String defaultFontName = "SansSerif";
  final static String defaultFontStyle = "Bold";
  final static int defaultFontSize = 12;
  public final static int margin = 4;
  
  public String specString = "";
  public String scaleString = "";
  JmolFont currentMetricsFont3d;
  JmolFont baseFont3d;
  public int width;
  public int ascent;
  public int descent;
  int x, y, dx, dy;

  @Override
  public void initShape() {
    super.initShape();
    myType = "scales";
    baseFont3d = font3d = gdata.getFont3D(defaultFontName, defaultFontStyle, defaultFontSize);
    calcMetrics();
  }

  @Override
  public boolean wasClicked(int x, int y) {
    int width = viewer.getScreenWidth();
    int height = viewer.getScreenHeight();
    return (width > 0 && height > 0 
        && x > width - width - margin
        && y > height - ascent - margin);
  }


  void calcMetrics() {
    if (font3d == currentMetricsFont3d) 
      return;
    currentMetricsFont3d = font3d;
    width = font3d.stringWidth(scaleString);
    descent = font3d.getDescent();
    ascent = font3d.getAscent();
  }

  public void getFont(float imageFontScaling) {
    font3d = gdata.getFont3DScaled(baseFont3d, imageFontScaling);
    calcMetrics();
  }
}
