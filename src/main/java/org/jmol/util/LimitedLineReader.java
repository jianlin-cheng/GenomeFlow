package org.jmol.util;

import java.io.BufferedReader;

public class LimitedLineReader {
  private char[] buf;
  private int cchBuf;
  private int ichCurrent;

  public LimitedLineReader(BufferedReader bufferedReader, int readLimit)
    throws Exception {  
    bufferedReader.mark(readLimit);
    buf = new char[readLimit];
    cchBuf = Math.max(bufferedReader.read(buf), 0);
    ichCurrent = 0;
    bufferedReader.reset();
  }

  public String getHeader(int n) {
    return (n == 0 ? new String(buf) : new String(buf, 0, Math.min(cchBuf, n)));
  }
  
  public String readLineWithNewline() {
    while (ichCurrent < cchBuf) {
      int ichBeginningOfLine = ichCurrent;
      char ch = 0;
      while (ichCurrent < cchBuf &&
             (ch = buf[ichCurrent++]) != '\r' && ch != '\n') {
      }
      if (ch == '\r' && ichCurrent < cchBuf && buf[ichCurrent] == '\n')
        ++ichCurrent;
      int cchLine = ichCurrent - ichBeginningOfLine;
      if (buf[ichBeginningOfLine] == '#')
        continue; // flush comment lines;
      StringBuffer sb = new StringBuffer(cchLine);
      sb.append(buf, ichBeginningOfLine, cchLine);
      return sb.toString();
    }
    return "";
  }
}

