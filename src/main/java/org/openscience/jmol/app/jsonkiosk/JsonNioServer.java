package org.openscience.jmol.app.jsonkiosk;

import java.io.IOException;

import org.jmol.api.JmolViewer;

public interface JsonNioServer {

  public abstract void close();

  public abstract int getPort();

  /**
   * from StatusManager
   * 
   * @param msg
   */
  public abstract void scriptCallback(String msg);

  /**
   * from JmolPanel and SYNC command
   * 
   * @param port
   * @param msg
   */
  public abstract void send(int port, String msg);

  public abstract void startService(int port, JsonNioClient client,
                                    JmolViewer jmolViewer, String name, int version)
      throws IOException;

}
