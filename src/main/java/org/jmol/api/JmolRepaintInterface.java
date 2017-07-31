package org.jmol.api;

import org.jmol.modelset.ModelSet;
import org.jmol.util.GData;
import org.jmol.viewer.ShapeManager;
import org.jmol.viewer.Viewer;

public interface JmolRepaintInterface {

  void set(Viewer viewer, ShapeManager shapeManager);

  boolean isRepaintPending();

  void popHoldRepaint(boolean andRepaint);

  boolean refresh();

  void pushHoldRepaint();

  void repaintDone();

  void requestRepaintAndWait();

  void clear(int iShape);

  void render(GData gdata, ModelSet modelSet, boolean isFirstPass, int[] minMax);

  String renderExport(String type, GData gdata, ModelSet modelSet,
                      String fileName);

}
