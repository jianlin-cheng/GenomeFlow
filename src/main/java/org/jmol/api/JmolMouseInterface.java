package org.jmol.api;

public interface JmolMouseInterface {

  boolean handleOldJvm10Event(int id, int x, int y, int modifiers, long time);

  void clear();

  void dispose();

}
