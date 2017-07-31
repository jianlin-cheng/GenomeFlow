package org.jmol.api;

import java.net.URL;

public interface JmolFileAdapterInterface {

  public Object getBufferedURLInputStream(URL url, byte[] outputBytes,
                                          String post);

  public Object getBufferedFileInputStream(String name);

}
