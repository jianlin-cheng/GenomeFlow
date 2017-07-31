package org.jmol.awt;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.jmol.api.JmolFileAdapterInterface;
import org.jmol.api.JmolFileInterface;

public class JmolFileAdapter implements JmolFileAdapterInterface {

  @Override
public Object getBufferedURLInputStream(URL url, byte[] outputBytes,
                                          String post) {
    try {
      URLConnection conn = url.openConnection();
      String type = null;
      if (outputBytes != null) {
        type = "application/octet-stream;";
      } else if (post != null) {
        type = "application/x-www-form-urlencoded";
      }
      if (type != null) {
        conn.setRequestProperty("Content-Type", type);
        conn.setDoOutput(true);
        if (outputBytes == null)
          outputString(conn, post);
        else
          outputBytes(conn, outputBytes);
      }
      return new BufferedInputStream(conn.getInputStream());
    } catch (IOException e) {
      return e.getMessage();
    }
  }

  private void outputBytes(URLConnection conn, byte[] bytes) throws IOException {
    conn.getOutputStream().write(bytes);
    conn.getOutputStream().flush();
    //??conn.getOutputStream().close();
  }

  private void outputString(URLConnection conn, String post) throws IOException {
    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
    wr.write(post);
    wr.flush();
    //??wr.close();
  }

  @Override
public Object getBufferedFileInputStream(String name) {
    File file = new File(name);
    try {
      return new BufferedInputStream(new FileInputStream(file));
    } catch (IOException e) {
      return e.getMessage();
    }
  }

  public static JmolFileInterface newFile(String name) {
    return new JmolFile(name);
  }

}
