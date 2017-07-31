package org.jmol.api;

import org.jmol.util.JmolFont;

public interface JmolGraphicsInterface {

  public abstract boolean isAntialiased();

  public abstract int getRenderHeight();

  public abstract int getRenderWidth();

  public abstract int getSlab();

  public abstract void setSlab(int slabValue);

  public abstract int getColorArgbOrGray(short colix);

  public abstract int getDepth();

  public abstract void setDepth(int depthValue);

  public abstract JmolFont getFont3DScaled(JmolFont font3d, float imageFontScaling);

  public abstract byte getFontFid(float fontSize);

  public abstract boolean isClippedZ(int z);

  public abstract boolean isClippedXY(int i, int screenX, int screenY);

  public abstract boolean isInDisplayRange(int x, int y);

}
