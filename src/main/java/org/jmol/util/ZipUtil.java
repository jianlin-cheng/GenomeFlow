/* $RCSfile$
 * $Author$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 2006  The Jmol Development Team
 *
 * Contact: jmol-developers@lists.sf.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 *  02110-1301, USA.
 */

package org.jmol.util;


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
//import java.net.URL;
//import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class ZipUtil {
  /*
    public static boolean isZipFile(String filePath) {
      try {
        URL url = new URL(filePath);
        URLConnection conn = url.openConnection();
        BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(), 8192);
        boolean isOK = isZipFile(bis);
        bis.close();
        return isOK;
      } catch (Exception e) {
        //
      }
      return false;
    }
  */
  public static boolean isZipFile(InputStream is) {
    byte[] abMagic = new byte[4];
    try {
      is.mark(5);
      is.read(abMagic, 0, 4);
      is.reset();
    } catch (Exception e) {
      // ignore;
    }
    return isZipFile(abMagic);
  }


  public static boolean isPngZipStream(InputStream is) {
    if (isZipFile(is))
      return false;
    try {
      is.mark(56);
      byte[] abMagic = getStreamBytes(is, 55);
      is.reset();
      return (abMagic[51] == 'P' && abMagic[52] == 'N' && abMagic[53] == 'G' && abMagic[54] == 'J');
    } catch (Exception e) {
    }
    return false;
  }

  /**
   * looks at byte 51 for "PNGJxxxxxxxxx+yyyyyyyyy"
   * where xxxxxxxxx is a byte offset to the JMOL data
   * and yyyyyyyyy is the length of the data.
   * 
   * @param bis
   * @return same stream or byte stream
   */
  public static BufferedInputStream checkPngZipStream(BufferedInputStream bis) {
    if (!isPngZipStream(bis))
      return bis;
    byte[] data = null;
    bis.mark(75);
    try {
      data = getStreamBytes(bis, 74);
      bis.reset();
      int pt = 0;
      for (int i = 64, f = 1; --i > 54; f *= 10)
        pt += (data[i] - '0') * f;
      int n = 0;
      for (int i = 74, f = 1; --i > 64; f *= 10)
        n += (data[i] - '0') * f;
      while (pt > 0)
        pt -= bis.skip(pt);
      data = getStreamBytes(bis, n);
      bis.close();
    } catch (Throwable e) {
      data = new byte[0];
    }
    return new BufferedInputStream(new ByteArrayInputStream(data));
  }

  public static boolean isZipFile(byte[] bytes) {
    return (bytes.length >= 4 
        && bytes[0] == 'P'  //PK<03><04> 
        && bytes[1] == 'K'
        && bytes[2] == '\3' 
        && bytes[3] == '\4');
  }

  public static ZipInputStream getStream(InputStream is) {
    return (is instanceof ZipInputStream? (ZipInputStream) is
        : is instanceof BufferedInputStream ? new ZipInputStream (is)
        : new ZipInputStream(new BufferedInputStream(is))); 
  }
  
  /**
   * reads a ZIP file and saves all data in a Hashtable
   * so that the files may be organized later in a different order. Also adds
   * a #Directory_Listing entry.
   * 
   * Files are bracketed by BEGIN Directory Entry and END Directory Entry lines, 
   * similar to CompoundDocument.getAllData.
   * 
   * @param is
   * @param subfileList
   * @param name0            prefix for entry listing 
   * @param binaryFileList   |-separated list of files that should be saved
   *                         as xx xx xx hex byte strings. The directory listing
   *                         is appended with ":asBinaryString"
   * @param fileData
   */
  public static void getAllData(InputStream is, String[] subfileList,
                                String name0, String binaryFileList,
                                Map<String, String> fileData) {
    ZipInputStream zis = getStream(is);
    ZipEntry ze;
    StringBuffer listing = new StringBuffer();
    binaryFileList = "|" + binaryFileList + "|";
    String prefix = TextFormat.join(subfileList, '/', 1);
    String prefixd = null;
    if (prefix != null) {
      prefixd = prefix.substring(0, prefix.indexOf("/") + 1);
      if (prefixd.length() == 0)
        prefixd = null;
    }
    try {
      while ((ze = zis.getNextEntry()) != null) {
        String name = ze.getName();
        if (prefix != null && prefixd != null
            && !(name.equals(prefix) || name.startsWith(prefixd)))
          continue;
        //System.out.println("ziputil: " + name);
        listing.append(name).append('\n');
        String sname = "|" + name.substring(name.lastIndexOf("/") + 1) + "|";
        boolean asBinaryString = (binaryFileList.indexOf(sname) >= 0);
        byte[] bytes = getStreamBytes(zis, ze.getSize());
        String str;
        if (asBinaryString) {
          str = getBinaryStringForBytes(bytes);
          name += ":asBinaryString";
        } else {
          str = new String(bytes);
        }
        str = "BEGIN Directory Entry " + name + "\n" + str
            + "\nEND Directory Entry " + name + "\n";
        fileData.put(name0 + "|" + name, str);
      }
    } catch (Exception e) {
    }
    fileData.put("#Directory_Listing", listing.toString());
  }

  public static String getBinaryStringForBytes(byte[] bytes) {
    StringBuffer ret = new StringBuffer();
    for (int i = 0; i < bytes.length; i++)
      ret.append(Integer.toHexString(bytes[i] & 0xFF))
          .append(' ');
    return ret.toString();
  }
  
  /**
   *  iteratively drills into zip files of zip files to extract file content
   *  or zip file directory. Also works with JAR files. 
   *  
   *  Does not return "__MACOS" paths
   * 
   * @param bis
   * @param list
   * @param listPtr
   * @param asBufferedInputStream  for Pmesh
   * @return  directory listing or subfile contents
   */
  static public Object getZipFileContents(BufferedInputStream bis, String[] list,
                                          int listPtr, boolean asBufferedInputStream) {
    StringBuffer ret;
    if (list == null || listPtr >= list.length)
      return getZipDirectoryAsStringAndClose(bis);
    String fileName = list[listPtr];
    ZipInputStream zis = new ZipInputStream(bis);
    ZipEntry ze;
    //System.out.println("fname=" + fileName);
    try {
      boolean isAll = (fileName.equals("."));
      if (isAll || fileName.lastIndexOf("/") == fileName.length() - 1) {
        ret = new StringBuffer();
        while ((ze = zis.getNextEntry()) != null) {
          String name = ze.getName();
          if (isAll || name.startsWith(fileName))
            ret.append(name).append('\n');
        }
        String str = ret.toString();
        if (asBufferedInputStream)
          return new BufferedInputStream(new ByteArrayInputStream(str.getBytes()));
        return str;
      }
      boolean asBinaryString = false;
      if (fileName.indexOf(":asBinaryString") > 0) {
        fileName = fileName.substring(0, fileName.indexOf(":asBinaryString"));
        asBinaryString = true;
      }
      while ((ze = zis.getNextEntry()) != null) {
        if (!fileName.equals(ze.getName()))
          continue;
        byte[] bytes = getStreamBytes(zis, ze.getSize());
        //System.out.println("ZipUtil::ZipEntry.name = " + ze.getName() + " " + bytes.length);
        if (isZipFile(bytes))
          return getZipFileContents(new BufferedInputStream(
              new ByteArrayInputStream(bytes)), list, ++listPtr, asBufferedInputStream);
        if (asBufferedInputStream)
          return new BufferedInputStream(new ByteArrayInputStream(bytes));
        if (asBinaryString) {
          ret = new StringBuffer();
          for (int i = 0; i < bytes.length; i++)
            ret.append(Integer.toHexString(bytes[i] & 0xFF)).append(' ');
          return ret.toString();
        }
      return new String(bytes);
      }
    } catch (Exception e) {
    }
    return "";
  }
  
  static public byte[] getZipFileContentsAsBytes(BufferedInputStream bis, String[] list,
                                          int listPtr) {
    byte[] ret = new byte[0];
    String fileName = list[listPtr];
    if (fileName.lastIndexOf("/") == fileName.length() - 1)
      return ret;
    try {
      bis = checkPngZipStream(bis);
      ZipInputStream zis = new ZipInputStream(bis);
      ZipEntry ze;
      while ((ze = zis.getNextEntry()) != null) {
        if (!fileName.equals(ze.getName()))
          continue;
        byte[] bytes = getStreamBytes(zis, ze.getSize());
        if (isZipFile(bytes) && ++listPtr < list.length)
          return getZipFileContentsAsBytes(new BufferedInputStream(
              new ByteArrayInputStream(bytes)), list, listPtr);
        return bytes;
      }
    } catch (Exception e) {
    }
    return ret;
  }
  
  static public String getZipDirectoryAsStringAndClose(BufferedInputStream bis) {
    StringBuffer sb = new StringBuffer();
    String[] s = new String[0];
    try {
      s = getZipDirectoryOrErrorAndClose(bis, false);
      bis.close();
    } catch (Exception e) { 
      Logger.error(e.getMessage());
    }
    for (int i = 0; i < s.length; i++)
      sb.append(s[i]).append('\n');
    return sb.toString();
  }
  
  static public String[] getZipDirectoryAndClose(BufferedInputStream bis, boolean addManifest) {
    String[] s = new String[0];
    try {
      s = getZipDirectoryOrErrorAndClose(bis, addManifest);
      bis.close();
    } catch (Exception e) { 
      Logger.error(e.getMessage());
    }
    return s;
  }

  public static boolean isJmolManifest(String thisEntry) {
    return thisEntry.startsWith("JmolManifest");
  }
  
  private static String[] getZipDirectoryOrErrorAndClose(BufferedInputStream bis, boolean addManifest) throws IOException {
    bis = checkPngZipStream(bis);
    List<String> v = new ArrayList<String>();
    ZipInputStream zis = new ZipInputStream(bis);
    ZipEntry ze;
    String manifest = null;
    while ((ze = zis.getNextEntry()) != null) {
      String fileName = ze.getName();
      if (addManifest && isJmolManifest(fileName))
        manifest = getZipEntryAsString(zis);
      else if (!fileName.startsWith("__MACOS")) // resource fork not nec.
        v.add(fileName);
    }
    zis.close();
    if (addManifest)
      v.add(0, manifest == null ? "" : manifest + "\n############\n");
    return v.toArray(new String[v.size()]);
  }


  private static String getZipEntryAsString(InputStream is) throws IOException {
    StringBuffer sb = new StringBuffer();
    byte[] buf = new byte[1024];
    int len;
    while (is.available() >= 1 && (len = is.read(buf)) > 0)
      sb.append(new String(buf, 0, len));
    return sb.toString();
  }
  
  public static byte[] getStreamBytes(InputStream is, long n) throws IOException {
    
    //Note: You cannot use InputStream.available() to reliably read
    //      zip data from the web. 
    
    byte[] buf = new byte[n >= 0 && n < 1024 ? (int) n : 1024];
    byte[] bytes = new byte[n < 0 ? 4096 : (int) n];
    int len = 0;
    int totalLen = 0;
    while ((n < 0 || totalLen < n) 
        && (len = is.read(buf)) > 0) {
      totalLen += len;
      if (totalLen > bytes.length)
        bytes = ArrayUtil.ensureLength(bytes, totalLen * 2);
      System.arraycopy(buf, 0, bytes, totalLen - len, len);
    }
    if (totalLen == bytes.length)
      return bytes;
    buf = new byte[totalLen];
    System.arraycopy(bytes, 0, buf, 0, totalLen);
    return buf;
  }

  public static boolean isGzip(byte[] bytes) {    
      return (bytes != null && bytes.length > 2 
          && bytes[0] == (byte) 0x1F && bytes[1] == (byte) 0x8B);
  }

  public static boolean isGzip(InputStream is) {
    byte[] abMagic = new byte[4];
    try {
      is.mark(5);
      is.read(abMagic, 0, 4);
      is.reset();
    } catch (IOException e) {
      // ignore
    }
    return isGzip(abMagic);
  }

  public static String getGzippedBytesAsString(byte[] bytes) {
    try {
      InputStream is = new ByteArrayInputStream(bytes);
      do {
        is = new BufferedInputStream(new GZIPInputStream(is));
      } while (isGzip(is));
      String s = getZipEntryAsString(is);
      is.close();
      return s;
    } catch (Exception e) {
      return "";
    }
  }

  public static InputStream getGzippedInputStream(byte[] bytes) {
    try {
      InputStream is = new ByteArrayInputStream(bytes);
      do {
        is = new BufferedInputStream(new GZIPInputStream(is));
      } while (isGzip(is));
      return is;
    } catch (Exception e) {
      return null;
    }
  }
  
  /**
   * check a JmolManifest for a reference to a script file (.spt)
   * 
   * @param manifest
   * @return  null, "", or a directory entry in the ZIP file
   */
  
  public static String getManifestScriptPath(String manifest) {
    if (manifest.indexOf("$SCRIPT_PATH$") >= 0)
      return "";
    char ch = (manifest.indexOf('\n') >= 0 ? '\n' : '\r');
    if (manifest.indexOf(".spt") >= 0) {
      String[] s = TextFormat.split(manifest, ch);
      for (int i = s.length; --i >= 0;)
        if (s[i].indexOf(".spt") >= 0)
          return "|" + TextFormat.trim(s[i], "\r\n \t");
    }
    return null;
  }


  /**
   * caches an entire pngj file's contents into a Map
   * 
   * @param bis
   * @param fileName 
   * @param cache 
   * @return  file listing, separated by \n
   */
  public static String cacheZipContents(BufferedInputStream bis, String fileName, Map<String, byte[]> cache) {
    ZipInputStream zis = getStream(bis);
    ZipEntry ze;
    StringBuffer listing = new StringBuffer();
    long n = 0;
    try {
      while ((ze = zis.getNextEntry()) != null) {
        String name = ze.getName();
        listing.append(name).append('\n');
        long nBytes = ze.getSize();
        byte[] bytes = getStreamBytes(zis, nBytes);
        n += bytes.length;
        cache.put(fileName + "|" + name, bytes );
      }
      zis.close();
    } catch (Exception e) {
      try {
        zis.close();
      } catch (IOException e1) {
      }
      return null;
    }
    Logger.info("ZipUtil cached " + n + " bytes from " + fileName);
    return listing.toString();
  }
  
}
