package org.jmol.api;

import org.jmol.viewer.Viewer;

public interface JmolPopupInterface {

  public String getMenuAsString(String string);

  public Object getMenuAsObject();

  public void initialize(Viewer viewer, String menu);

  public void show(int x, int y);

  public void updateComputedMenus();

  public void dispose();
 
}
