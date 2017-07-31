package org.jmol.api;

public interface JmolFileInterface {

  String getAbsolutePath();

  String getName();

  long length();

  boolean isDirectory();

  JmolFileInterface getParentAsFile();

}
