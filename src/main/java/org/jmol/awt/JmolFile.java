package org.jmol.awt;

import java.io.File;

import org.jmol.api.JmolFileInterface;

/**
 * a subclass of File allowing extension to JavaScript
 * 
 * private to org.jmol.awt
 * 
 */

class JmolFile extends File implements JmolFileInterface {

  public JmolFile(String name) {
    super(name);
  }

  @Override
public JmolFileInterface getParentAsFile() {
    File file = getParentFile();
    return (file == null ? null : new JmolFile(file.getAbsolutePath()));
  }

}
