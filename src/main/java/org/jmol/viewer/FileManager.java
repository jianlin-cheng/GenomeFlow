/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-09-12 13:11:19 -0500 (Wed, 12 Sep 2012) $
 * $Revision: 17558 $
 *
 * Copyright (C) 2003-2005  Miguel, Jmol Development Team
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
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jmol.viewer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.jmol.api.ApiPlatform;
import org.jmol.api.JmolFileAdapterInterface;
import org.jmol.api.JmolFileInterface;
import org.jmol.api.JmolFilesReaderInterface;
import org.jmol.api.JmolViewer;
import org.jmol.modelset.Atom;
import org.jmol.script.ScriptCompiler;
import org.jmol.util.ArrayUtil;
import org.jmol.util.Base64;
import org.jmol.util.BinaryDocument;
import org.jmol.util.CompoundDocument;
import org.jmol.util.Escape;
import org.jmol.util.Logger;
import org.jmol.util.Parser;
import org.jmol.util.TextFormat;
import org.jmol.util.ZipUtil;
import org.jmol.viewer.Viewer.ACCESS;

public class FileManager {

  protected Viewer viewer;

  FileManager(Viewer viewer) {
    this.viewer = viewer;
    clear();
  }

  void clear() {
    fullPathName = fileName = nameAsGiven = viewer.getZapName();
    spardirCache = null;
  }

  private void setLoadState(Map<String, Object> htParams) {
    if (viewer.getPreserveState()) {
      htParams.put("loadState", viewer.getLoadState(htParams));
    }
  }

  private String pathForAllFiles = "";
  
  String getPathForAllFiles() {
    return pathForAllFiles;
  }
  
  String setPathForAllFiles(String value) {
    if (value.length() > 0 && !value.endsWith("/") && !value.endsWith("|"))
        value += "/";
    return pathForAllFiles = value;
  }

  String nameAsGiven = "zapped";
  String fullPathName;
  String fileName;
  
  //added -hcf
  String localSeqPath;
  void setLocalSeqPath(String path) {
	  localSeqPath = path;
  }
  
  String getLocalSeqPath() {
	  return localSeqPath;
  }
  //added end -hcf
  
  void setFileInfo(String[] fileInfo) {
    // used by ScriptEvaluator dataFrame and load methods to temporarily save the state here
    fullPathName = fileInfo[0];
    fileName = fileInfo[1];
    nameAsGiven = fileInfo[2];
  }

  String[] getFileInfo() {
    // used by ScriptEvaluator dataFrame method
    return new String[] { fullPathName, fileName, nameAsGiven };
  }

  String getFullPathName() {
    return fullPathName != null ? fullPathName : nameAsGiven;
  }

  String getFileName() {
    return fileName != null ? fileName : nameAsGiven;
  }

  // for applet proxy
  private URL appletDocumentBaseURL = null;
  private String appletProxy;

  String getAppletDocumentBase() {
    return (appletDocumentBaseURL == null ? "" : appletDocumentBaseURL.toString());
  }

  void setAppletContext(String documentBase) {
    try {
      appletDocumentBaseURL = (documentBase.length() == 0 ? null : new URL(documentBase));
    } catch (MalformedURLException e) {
      // never mind
    }
  }

  void setAppletProxy(String appletProxy) {
    this.appletProxy = (appletProxy == null || appletProxy.length() == 0 ? null
        : appletProxy);
  }

  String getState(StringBuffer sfunc) {
    StringBuffer commands = new StringBuffer();
    if (sfunc != null) {
      sfunc.append("  _setFileState;\n");
      commands.append("function _setFileState() {\n\n");
    }
    if (commands.indexOf("append") < 0
        && viewer.getModelSetFileName().equals("zapped"))
      commands.append("  zap;\n");
    viewer.appendLoadStates(commands);
    if (sfunc != null)
      commands.append("\n}\n\n");
    return commands.toString();
  }

  String getFileTypeName(String fileName) {
    int pt = fileName.indexOf("::");
    if (pt >= 0)
      return fileName.substring(0, pt);
    if (fileName.startsWith("="))
      return "pdb";
    Object br = getUnzippedBufferedReaderOrErrorMessageFromName(fileName, null,
        true, false, true, true);
    if (br instanceof BufferedReader)
      return viewer.getModelAdapter().getFileTypeName(br);
    if (br instanceof ZipInputStream) {
      String zipDirectory = getZipDirectoryAsString(fileName);
      if (zipDirectory.indexOf("JmolManifest") >= 0)
        return "Jmol";
      return viewer.getModelAdapter().getFileTypeName(
          getBufferedReaderForString(zipDirectory));
    }
    if (br instanceof String[]) {
      return ((String[]) br)[0];
    }
    return null;
  }

  static BufferedInputStream getInputStreamForStringBuffer(Object t) {
    return new BufferedInputStream(new ByteArrayInputStream(((StringBuffer) t)
        .toString().getBytes()));
    }

  static BufferedReader getBufferedReaderForString(String string) {
    return new BufferedReader(new StringReader(string));
  }

  private String getZipDirectoryAsString(String fileName) {
  	Object t = getBufferedInputStreamOrErrorMessageFromName(
        fileName, fileName, false, false, null);
  	if (t instanceof StringBuffer)
  		t = getInputStreamForStringBuffer(t);
    return ZipUtil.getZipDirectoryAsStringAndClose((BufferedInputStream) t);
  }

  
  
  
 //added -hcf 
  
  Object createAtomSetCollectionFromFile(String name,
          Map<String, Object> htParams,
          boolean isAppend, int currentSelectedScale, int currentSelectedUnit, int[] selectedPath, String downOrUp, Atom[] currentUnits) {
		if (htParams.get("atomDataOnly") == null) {
			setLoadState(htParams);
		}
		name = viewer.resolveDatabaseFormat(name);
		int pt = name.indexOf("::");
		String nameAsGiven = (pt >= 0 ? name.substring(pt + 2) : name);
		String fileType = (pt >= 0 ? name.substring(0, pt) : null);
		Logger.info("\nFileManager.getAtomSetCollectionFromFile(" + nameAsGiven
				+ ")" + (name.equals(nameAsGiven) ? "" : " //" + name));
		String[] names = classifyName(nameAsGiven, true);
		if (names.length == 1)
			return names[0];
		String fullPathName = names[0];
		String fileName = names[1];
		htParams.put("fullPathName", (fileType == null ? "" : fileType + "::")
				+ fullPathName.replace('\\', '/'));
		if (viewer.getMessageStyleChime() && viewer.getDebugScript())
			viewer.scriptStatus("Requesting " + fullPathName);
		FileReader fileReader = new FileReader(fileName, fullPathName, nameAsGiven,
		  fileType, null, htParams, isAppend);
		fileReader.run(currentSelectedScale, currentSelectedUnit, selectedPath, downOrUp, currentUnits);
		return fileReader.atomSetCollection;	
}

  //added end -hcf
  
  

  
  
  /////////////// createAtomSetCollectionFromXXX methods /////////////////

  // where XXX = File, Files, String, Strings, ArrayData, DOM, Reader

  /*
   * note -- createAtomSetCollectionFromXXX methods
   * were "openXXX" before refactoring 11/29/2008 -- BH
   * 
   * The problem was that while they did open the file, they
   * (mostly) also closed them, and this was confusing.
   * 
   * The term "clientFile" was replaced by "atomSetCollection"
   * here because that's what it is --- an AtomSetCollection,
   * not a file. The file is closed at this point. What is
   * returned is the atomSetCollection object.
   * 
   * One could say this is just semantics, but there were
   * subtle bugs here, where readers were not always being 
   * closed explicitly. In the process of identifying Out of
   * Memory Errors, I felt it was necessary to clarify all this.
   * 
   * Apologies to those who feel the original clientFile notation
   * was more generalizable or understandable. 
   * 
   */
  Object createAtomSetCollectionFromFile(String name,
                                         Map<String, Object> htParams,
                                         boolean isAppend) {
    if (htParams.get("atomDataOnly") == null) {
      setLoadState(htParams);
    }
    name = viewer.resolveDatabaseFormat(name);
    int pt = name.indexOf("::");
    String nameAsGiven = (pt >= 0 ? name.substring(pt + 2) : name);
    String fileType = (pt >= 0 ? name.substring(0, pt) : null);
    Logger.info("\nFileManager.getAtomSetCollectionFromFile(" + nameAsGiven
        + ")" + (name.equals(nameAsGiven) ? "" : " //" + name));
    String[] names = classifyName(nameAsGiven, true);
    if (names.length == 1)
      return names[0];
    String fullPathName = names[0];
    String fileName = names[1];
    htParams.put("fullPathName", (fileType == null ? "" : fileType + "::")
        + fullPathName.replace('\\', '/'));
    if (viewer.getMessageStyleChime() && viewer.getDebugScript())
      viewer.scriptStatus("Requesting " + fullPathName);
    FileReader fileReader = new FileReader(fileName, fullPathName, nameAsGiven,
        fileType, null, htParams, isAppend);
    fileReader.run();
    return fileReader.atomSetCollection;
  }

  Object createAtomSetCollectionFromFiles(String[] fileNames,
                                          Map<String, Object> htParams,
                                          boolean isAppend) {
    setLoadState(htParams);
    String[] fullPathNames = new String[fileNames.length];
    String[] namesAsGiven = new String[fileNames.length];
    String[] fileTypes = new String[fileNames.length];
    for (int i = 0; i < fileNames.length; i++) {
      int pt = fileNames[i].indexOf("::");
      String nameAsGiven = (pt >= 0 ? fileNames[i].substring(pt + 2)
          : fileNames[i]);
      String fileType = (pt >= 0 ? fileNames[i].substring(0, pt) : null);
      String[] names = classifyName(nameAsGiven, true);
      if (names.length == 1)
        return names[0];
      fullPathNames[i] = names[0];
      fileNames[i] = names[0].replace('\\', '/');
      fileTypes[i] = fileType;
      namesAsGiven[i] = nameAsGiven;
    }
    htParams.put("fullPathNames", fullPathNames);
    htParams.put("fileTypes", fileTypes);
    FilesReader filesReader = new FilesReader(fullPathNames, namesAsGiven,
        fileTypes, null, htParams, isAppend);
    filesReader.run();
    return filesReader.atomSetCollection;
  }

  Object createAtomSetCollectionFromString(String strModel,
                                           StringBuffer loadScript,
                                           Map<String, Object> htParams,
                                           boolean isAppend,
                                           boolean isLoadVariable) {
    if (!isLoadVariable)
      DataManager.getInlineData(loadScript, strModel, isAppend, viewer
          .getDefaultLoadFilter());
    setLoadState(htParams);
    boolean isAddH = (strModel.indexOf(JmolConstants.ADD_HYDROGEN_TITLE) >= 0);
    String[] fnames = (isAddH ? getFileInfo() : null);
    FileReader fileReader = new FileReader("string", "string", "string", null,
        getBufferedReaderForString(strModel), htParams, isAppend);
    fileReader.run();
    if (fnames != null)
      setFileInfo(fnames);
    if (!isAppend && !(fileReader.atomSetCollection instanceof String)) {
      viewer.zap(false, true, false);
      fullPathName = fileName = (strModel == JmolConstants.MODELKIT_ZAP_STRING ? JmolConstants.MODELKIT_ZAP_TITLE
          : "string");
    }
    return fileReader.atomSetCollection;
  }

  Object createAtomSeCollectionFromStrings(String[] arrayModels,
                                           StringBuffer loadScript,
                                           Map<String, Object> htParams,
                                           boolean isAppend) {
    if (!htParams.containsKey("isData")) {
      String oldSep = "\"" + viewer.getDataSeparator() + "\"";
      String tag = "\"" + (isAppend ? "append" : "model") + " inline\"";
      StringBuffer sb = new StringBuffer(
          "set dataSeparator \"~~~next file~~~\";\ndata ");
      sb.append(tag);
      for (int i = 0; i < arrayModels.length; i++) {
        if (i > 0)
          sb.append("~~~next file~~~");
        sb.append(arrayModels[i]);
      }
      sb.append("end ").append(tag).append(";set dataSeparator ")
          .append(oldSep);
      loadScript.append(sb);
    }
    setLoadState(htParams);
    Logger.info("FileManager.getAtomSetCollectionFromStrings(string[])");
    String[] fullPathNames = new String[arrayModels.length];
    StringDataReader[] readers = new StringDataReader[arrayModels.length];
    for (int i = 0; i < arrayModels.length; i++) {
      fullPathNames[i] = "string[" + i + "]";
      readers[i] = new StringDataReader(arrayModels[i]);
    }
    FilesReader filesReader = new FilesReader(fullPathNames, fullPathNames,
        null, readers, htParams, isAppend);
    filesReader.run();
    return filesReader.atomSetCollection;
  }

  @SuppressWarnings("unchecked")
  Object createAtomSeCollectionFromArrayData(List<Object> arrayData,
                                             Map<String, Object> htParams,
                                             boolean isAppend) {
    // NO STATE SCRIPT -- HERE WE ARE TRYING TO CONSERVE SPACE
    Logger.info("FileManager.getAtomSetCollectionFromArrayData(Vector)");
    int nModels = arrayData.size();
    String[] fullPathNames = new String[nModels];
    DataReader[] readers = new DataReader[nModels];
    for (int i = 0; i < nModels; i++) {
      fullPathNames[i] = "String[" + i + "]";
      Object data = arrayData.get(i);
      if (data instanceof String)
        readers[i] = new StringDataReader((String) arrayData.get(i));
      else if (data instanceof String[])
        readers[i] = new ArrayDataReader((String[]) arrayData.get(i));
      else if (data instanceof List)
        readers[i] = new VectorDataReader((List<String>) arrayData.get(i));
    }
    FilesReader filesReader = new FilesReader(fullPathNames, fullPathNames,
        null, readers, htParams, isAppend);
    filesReader.run();
    return filesReader.atomSetCollection;
  }

  Object createAtomSetCollectionFromDOM(Object DOMNode,
                                        Map<String, Object> htParams) {
    DOMReader aDOMReader = new DOMReader(DOMNode, htParams);
    aDOMReader.run();
    return aDOMReader.atomSetCollection;
  }

  /**
   * not used in Jmol project -- will close reader
   * 
   * @param fullPathName
   * @param name
   * @param reader
   * @param htParams 
   * @return fileData
   */
  Object createAtomSetCollectionFromReader(String fullPathName, String name,
                                           Object reader,
                                           Map<String, Object> htParams) {
    FileReader fileReader = new FileReader(name, fullPathName, name, null,
        reader, htParams, false);
    fileReader.run();
    return fileReader.atomSetCollection;
  }

  /////////////// generally useful file I/O methods /////////////////

  // mostly internal to FileManager and its enclosed classes

  BufferedInputStream getBufferedInputStream(String fullPathName) {
    Object ret = getBufferedReaderOrErrorMessageFromName(fullPathName,
        new String[2], true, true);
    return (ret instanceof BufferedInputStream ? (BufferedInputStream) ret
        : null);
  }

  ZipUtil jmolZip;
  
  Object getBufferedInputStreamOrErrorMessageFromName(String name,
                                                      String fullName,
                                                      boolean showMsg,
                                                      boolean checkOnly,
                                                      byte[] outputBytes) {
    byte[] cacheBytes = (fullName == null ? null : getCachedPngjBytes(fullName));
    if (cacheBytes == null)
      cacheBytes = (byte[]) cacheGet(fullName, true);
    boolean isPngjBinaryPost = (name.indexOf("?POST?_PNGJBIN_") >= 0);
    boolean isPngjPost = (isPngjBinaryPost || name.indexOf("?POST?_PNGJ_") >= 0);
    if (name.indexOf("?POST?_PNG_") > 0 || isPngjPost) {
      Object o = viewer.getImageAs(isPngjPost ? "PNGJ" : "PNG", -1, 0, 0, null,
          null);
      if (!(o instanceof byte[]))
        return o;
      if (isPngjBinaryPost) {
        outputBytes = (byte[]) o;
        name = TextFormat.simpleReplace(name, "?_", "=_");
      } else {
        name = (new StringBuffer(name)).append("=").append(
            Base64.getBase64((byte[]) o)).toString();
      }
    }
    String errorMessage = null;
    int iurl = (cacheBytes == null ? urlPrefixes.length : -1);
    for (; --iurl >= 0;)
      if (name.startsWith(urlPrefixes[iurl]))
        break;
    boolean isURL = (iurl >= 0);
    String post = null;
    if (isURL && (iurl = name.indexOf("?POST?")) >= 0) {
      post = name.substring(iurl + 6);
      name = name.substring(0, iurl);
    }
    boolean isApplet = (appletDocumentBaseURL != null);
    BufferedInputStream bis = null;
    Object ret = null;
    // int length;
    try {
      JmolFileAdapterInterface fai = viewer.getFileAdapter();
      if (cacheBytes == null && (isApplet || isURL)) {
        if (isApplet && isURL && appletProxy != null)
          name = appletProxy + "?url=" + urlEncode(name);
        URL url = (isApplet ? new URL(appletDocumentBaseURL, name) : new URL(name));
        if (checkOnly)
        	return null;
        name = url.toString();
        if (showMsg && name.toLowerCase().indexOf("password") < 0)
          Logger.info("FileManager opening " + name);
        ret = fai.getBufferedURLInputStream(url, outputBytes, post);
        if (ret instanceof StringBuffer || ret instanceof String)
          return ret;
      } else if (cacheBytes == null
          && (cacheBytes = (byte[]) cacheGet(name, true)) == null) {
        if (showMsg)
          Logger.info("FileManager opening " + name);
        ret = fai.getBufferedFileInputStream(name);
      }
      if (ret instanceof String)
        return ret;
      if (cacheBytes == null)
        bis = (BufferedInputStream) ret;
      else
        bis = new BufferedInputStream(new ByteArrayInputStream(cacheBytes));
      if (checkOnly) {
        bis.close();
        bis = null;
      }
      return bis;
    } catch (Exception e) {
      try {
        if (bis != null)
          bis.close();
      } catch (IOException e1) {
      }
      errorMessage = "" + e;
    }
    return errorMessage;
  }
  
  private String urlEncode(String name) {
    try {
      return URLEncoder.encode(name, "utf-8");
    } catch (UnsupportedEncodingException e) {
      return name;
    }
  }

  /**
   * just check for a file as being readable. Do not go into a zip file
   * 
   * @param filename
   * @return String[2] where [0] is fullpathname and [1] is error message or null
   */
  String[] getFullPathNameOrError(String filename) {
    String[] names = classifyName(filename, true);
    if (names == null || names[0] == null || names.length < 2)
      return new String[] { null, "cannot read file name: " + filename };
    String name = names[0];
    String fullPath = names[0].replace('\\', '/');
    name = getZipRoot(name);
    Object errMsg = getBufferedInputStreamOrErrorMessageFromName(name, fullPath, false, true, null);
    return new String[] { fullPath,
        (errMsg instanceof String ? (String) errMsg : null) };
  }

  Object getBufferedReaderOrErrorMessageFromName(String name,
                                                 String[] fullPathNameReturn,
                                                 boolean isBinary,
                                                 boolean doSpecialLoad) {
    Object data = cacheGet(name, false);
    byte[] bytes = (data instanceof byte[] ? (byte[]) data : null);
    if (name.startsWith("cache://")) {
      if (data == null)
        return "cannot read " + name;
      if (data instanceof byte[]) {
        bytes = (byte[]) data;
      } else {
        return getBufferedReaderForString((String) data);
      }
    }
    String[] names = classifyName(name, true);
    if (names == null)
      return "cannot read file name: " + name;
    if (fullPathNameReturn != null)
      fullPathNameReturn[0] = names[0].replace('\\', '/');
    return getUnzippedBufferedReaderOrErrorMessageFromName(names[0], bytes,
        false, isBinary, false, doSpecialLoad);
  }

  public String getEmbeddedFileState(String fileName) {
    String[] dir = null;
    dir = getZipDirectory(fileName, false);
    if (dir.length == 0) {
      String state = viewer.getFileAsString(fileName, Integer.MAX_VALUE, false, true);
      return (state.indexOf(JmolConstants.EMBEDDED_SCRIPT_TAG) < 0 ? ""
          : ScriptCompiler.getEmbeddedScript(state));
    }
    for (int i = 0; i < dir.length; i++)
      if (dir[i].indexOf(".spt") >= 0) {
        String[] data = new String[] { fileName + "|" + dir[i], null };
        getFileDataOrErrorAsString(data, Integer.MAX_VALUE, false, false);
        return data[1];
      }
    return "";
  }

  Object getUnzippedBufferedReaderOrErrorMessageFromName(
                                                         String name,
                                                         byte[] bytes,
                                                         boolean allowZipStream,
                                                         boolean asInputStream,
                                                         boolean isTypeCheckOnly, boolean doSpecialLoad) {
    String[] subFileList = null;
    String[] info = (bytes == null && doSpecialLoad ? viewer.getModelAdapter().specialLoad(name,
        "filesNeeded?") : null);
    String name00 = name;
    if (info != null) {
      if (isTypeCheckOnly)
        return info;
      if (info[2] != null) {
        String header = info[1];
        Map<String, String> fileData = new Hashtable<String, String>();
        if (info.length == 3) {
          // we need information from the output file, info[2]
          String name0 = getObjectAsSections(info[2], header, fileData);
          fileData.put("OUTPUT", name0);
          info = viewer.getModelAdapter()
              .specialLoad(name, fileData.get(name0));
          if (info.length == 3) {
            // might have a second option
            name0 = getObjectAsSections(info[2], header, fileData);
            fileData.put("OUTPUT", name0);
            info = viewer.getModelAdapter().specialLoad(info[1],
                fileData.get(name0));
          }
        }
        // load each file individually, but return files IN ORDER
        StringBuffer sb = new StringBuffer();
        if (fileData.get("OUTPUT") != null)
          sb.append(fileData.get(fileData.get("OUTPUT")));
        String s;
        for (int i = 2; i < info.length; i++) {
          name = info[i];
          name = getObjectAsSections(name, header, fileData);
          Logger.info("reading " + name);
          s = fileData.get(name);
          sb.append(s);
        }
        s = sb.toString();
        if (spardirCache == null)
          spardirCache = new Hashtable<String, byte[]>();
        spardirCache.put(name00.replace('\\','/'), s.getBytes());
        return getBufferedReaderForString(s);
      }
      // continuing...
      // here, for example, for an SPT file load that is not just a type check
      // (type check is only for application file opening and drag-drop to
      // determine if
      // script or load command should be used)
    }

    if (bytes == null) 
      bytes = getCachedPngjBytes(name);
    String fullName = name;
    if (name.indexOf("|") >= 0) {
      subFileList = TextFormat.split(name, "|");
      if (bytes == null)
        Logger.info("FileManager opening " + name);
      name = subFileList[0];
    }
    Object t = (bytes == null ? 
        getBufferedInputStreamOrErrorMessageFromName(name, fullName, true, false, null)
        : new BufferedInputStream(new ByteArrayInputStream(bytes)));
    if (t instanceof String)
      return t;
    if (t instanceof StringBuffer)
      return getInputStreamForStringBuffer(t);
    try {
      BufferedInputStream bis = (BufferedInputStream) t;
      if (ZipUtil.isGzip(bis)) {
        do {
          bis = new BufferedInputStream(new GZIPInputStream(bis));
        } while (ZipUtil.isGzip(bis));
      } else if (CompoundDocument.isCompoundDocument(bis)) {
        CompoundDocument doc = new CompoundDocument(bis);
        return getBufferedReaderForString(doc.getAllData("Molecule", "Input")
            .toString());
      } else {
        bis = ZipUtil.checkPngZipStream(bis);
        if (ZipUtil.isZipFile(bis)) {
          if (allowZipStream)
            return new ZipInputStream(bis);
          if (asInputStream)
            return ZipUtil.getZipFileContents(bis, subFileList, 1, true);
          // danger -- converting bytes to String here.
          // we lose 128-156 or so.
          String s = (String) ZipUtil.getZipFileContents(bis, subFileList, 1,
              false);
          bis.close();
          return getBufferedReaderForString(s);
        }
      }
      
      if (asInputStream)
        return bis;
      return new BufferedReader(new InputStreamReader(bis));
    } catch (Exception ioe) {
      return ioe.getMessage();
    }
  }

  /**
   * 
   * @param fileName
   * @param addManifest
   * @return [] if not a zip file;
   */
  String[] getZipDirectory(String fileName, boolean addManifest) {
    Object t = getBufferedInputStreamOrErrorMessageFromName(fileName, fileName,
        false, false, null);
    if (t instanceof StringBuffer)
      t = getInputStreamForStringBuffer(t);
    return ZipUtil.getZipDirectoryAndClose((BufferedInputStream) t, addManifest);
  }

  /**
   * delivers file contents and directory listing for a ZIP/JAR file into sb
   * 
   * 
   * @param name
   * @param header
   * @param fileData
   * @return name of entry
   */
  private String getObjectAsSections(String name, String header,
                                     Map<String, String> fileData) {
    if (name == null)
      return null;
    String[] subFileList = null;
    boolean asBinaryString = false;
    String name0 = name.replace('\\', '/');
    if (name.indexOf(":asBinaryString") >= 0) {
      asBinaryString = true;
      name = name.substring(0, name.indexOf(":asBinaryString"));
    }
    StringBuffer sb = null;
    if (fileData.containsKey(name0))
      return name0;
    if (name.indexOf("#JMOL_MODEL ") >= 0) {
      fileData.put(name0, name0 + "\n");
      return name0;
    }
    String fullName = name;
    if (name.indexOf("|") >= 0) {
      subFileList = TextFormat.split(name, "|");
      name = subFileList[0];
    }
    BufferedInputStream bis = null;
    try {
      Object t = getBufferedInputStreamOrErrorMessageFromName(name, fullName, false, false, null);
      if (t instanceof String) {
        fileData.put(name0, (String) t + "\n");
        return name0;
      }
      if (t instanceof StringBuffer)
        t = getInputStreamForStringBuffer(t);
      bis = (BufferedInputStream) t;
      if (CompoundDocument.isCompoundDocument(bis)) {
        CompoundDocument doc = new CompoundDocument(bis);
        doc.getAllData(name.replace('\\', '/'), "Molecule", fileData);
      } else if (ZipUtil.isZipFile(bis)) {
        ZipUtil.getAllData(bis, subFileList, name.replace('\\', '/'),
            "Molecule", fileData);
      } else if (asBinaryString) {
        // used for Spartan binary file reading
        BinaryDocument bd = new BinaryDocument();
        bd.setStream(bis, false);
        sb = new StringBuffer();
        //note -- these headers must match those in ZipUtil.getAllData and CompoundDocument.getAllData
        if (header != null)
          sb.append("BEGIN Directory Entry " + name0 + "\n");
        try {
          while (true)
            sb.append(Integer.toHexString((bd.readByte()) & 0xFF)).append(' ');
        } catch (Exception e1) {
          sb.append('\n');
        }
        if (header != null)
          sb.append("\nEND Directory Entry " + name0 + "\n");
        fileData.put(name0, sb.toString());
      } else {
        BufferedReader br = new BufferedReader(new InputStreamReader(ZipUtil
            .isGzip(bis) ? new GZIPInputStream(bis) : bis));
        String line;
        sb = new StringBuffer();
        if (header != null)
          sb.append("BEGIN Directory Entry " + name0 + "\n");
        while ((line = br.readLine()) != null) {
          sb.append(line);
          sb.append('\n');
        }
        br.close();
        if (header != null)
          sb.append("\nEND Directory Entry " + name0 + "\n");
        fileData.put(name0, sb.toString());
      }
    } catch (Exception ioe) {
      fileData.put(name0, ioe.getMessage());
    }
    if (bis != null)
      try {
        bis.close();
      } catch (Exception e) {
        //
      }
    if (!fileData.containsKey(name0))
      fileData.put(name0, "FILE NOT FOUND: " + name0 + "\n");
    return name0;
  }

  Object getFileAsBytes(String name, OutputStream os, boolean  allowZip) {
    // ?? used by eval of "WRITE FILE"
    // will be full path name
    if (name == null)
      return null;
    String fullName = name;
    String[] subFileList = null;
    if (name.indexOf("|") >= 0) {
      subFileList = TextFormat.split(name, "|");
      name = subFileList[0];
      allowZip = true;
    }
    Object t = getBufferedInputStreamOrErrorMessageFromName(name, fullName, false, false,
        null);
    if (t instanceof String)
      return "Error:" + t;
    if (t instanceof StringBuffer)
      t = getInputStreamForStringBuffer(t);
    try {
      BufferedInputStream bis = (BufferedInputStream) t;
      Object bytes = (os != null || subFileList == null || subFileList.length <= 1
            || !allowZip || !ZipUtil.isZipFile(bis) && !ZipUtil.isPngZipStream(bis) ? getStreamAsBytes(
            bis, os)
            : ZipUtil.getZipFileContentsAsBytes(bis, subFileList, 1));
      bis.close();
      return bytes;
    } catch (Exception ioe) {
      return ioe.getMessage();
    }
  }

  private static Object getStreamAsBytes(BufferedInputStream bis,
                                         OutputStream os) throws IOException {
    byte[] buf = new byte[1024];
    byte[] bytes = (os == null ? new byte[4096] : null);
    int len = 0;
    int totalLen = 0;
    while ((len = bis.read(buf)) > 0) {
      totalLen += len;
      if (os == null) {
        if (totalLen >= bytes.length)
          bytes = ArrayUtil.ensureLength(bytes, totalLen * 2);
        System.arraycopy(buf, 0, bytes, totalLen - len, len);
      } else {
        os.write(buf, 0, len);
      }
    }
    bis.close();
    if (os == null) {
      buf = new byte[totalLen];
      System.arraycopy(bytes, 0, buf, 0, totalLen);
      return buf;
    }
    return totalLen + " bytes";
  }

  /**
   * 
   * @param data
   *        [0] initially path name, but returned as full path name; [1]file
   *        contents (directory listing for a ZIP/JAR file) or error string
   * @param nBytesMax
   * @param doSpecialLoad
   * @param allowBinary 
   * @return true if successful; false on error
   */

  boolean getFileDataOrErrorAsString(String[] data, int nBytesMax,
                                     boolean doSpecialLoad, boolean allowBinary) {
    data[1] = "";
    String name = data[0];
    if (name == null)
      return false;
    Object t = getBufferedReaderOrErrorMessageFromName(name, data, false,
        doSpecialLoad);
    if (t instanceof String) {
      data[1] = (String) t;
      return false;
    }
    try {
      BufferedReader br = (BufferedReader) t;
      StringBuffer sb = new StringBuffer(8192);
      String line;
      if (nBytesMax == Integer.MAX_VALUE) {
        line = br.readLine();
        if (allowBinary || line != null && line.indexOf('\0') < 0
            && (line.length() != 4 || line.charAt(0) != 65533
            || line.indexOf("PNG") != 1)) {
          sb.append(line).append('\n');
          while ((line = br.readLine()) != null)
            sb.append(line).append('\n');
        }
      } else {
        int n = 0;
        int len;
        while (n < nBytesMax && (line = br.readLine()) != null) {
          if (nBytesMax - n < (len = line.length()) + 1)
            line = line.substring(0, nBytesMax - n - 1);
          sb.append(line).append('\n');
          n += len + 1;
        }
      }
      br.close();
      data[1] = sb.toString();
      fixUnicode(data, 1);
      return true;
    } catch (Exception ioe) {
      data[1] = ioe.getMessage();
      return false;
    }
  }

  private enum Encoding {
    NONE, UTF8, UTF_16BE, UTF_16LE, UTF_32BE, UTF_32LE
  }
  private static void fixUnicode(String[] data, int i) {
    String s = data[i];
    Encoding encoding = Encoding.NONE;
    //    try {
    //      System.out.println("16 " + new String(s.getBytes(), "UTF-16"));
    //      System.out.println("16LE " + new String(s.getBytes(), "UTF-16LE"));
    //      System.out.println("16BE " + new String(s.getBytes(), "UTF-16BE"));
    //    } catch (UnsupportedEncodingException e1) {
    //      // ignore
    //    }

    
    if (s.indexOf("\357\273\277") == 0) //EF BB BF
      encoding = Encoding.UTF8;
    else if (s.indexOf("\0\0\376\377") == 0) // 0 0 FE FF
      encoding = Encoding.UTF_32BE;
    else if (s.indexOf("\377\376\0\0") == 0)  // FF FE 0 0
      encoding = Encoding.UTF_32LE;
    else if (s.indexOf("\377\376") == 0)  // FF FE
      encoding = Encoding.UTF_16LE;
    else if (s.indexOf("\376\377") == 0) // FE FF
      encoding = Encoding.UTF_16BE;
    if (encoding == Encoding.NONE)
      return;
    Logger.info("FileManager found encoding " + encoding.name());
    try {
      s = new String(s.getBytes(), encoding.name().replace('_', '-'));
      switch (encoding) {
      case UTF8:
      case UTF_16BE:
        // extra byte at beginning removed
        s = s.substring(1);
        break;
      case UTF_16LE:
        // extra bytes at beginning and end removed
        s = s.substring(1, s.length() - 1);
        break;
      default:
        break;        
      }
    } catch (UnsupportedEncodingException e) {
      System.out.println(e);
    }
    data[i] = s;
//    System.out.println(s);
//    for (int ii = 0; ii < s.length(); ii++)
//      System.out.println(ii + " '" + s.charAt(ii) + "' "
//          + Character.codePointAt(s, ii));
  }

  Object getFileAsImage(String name, String[] retFileNameOrError) {
    if (name == null) {
      retFileNameOrError[0] = "";
      return null;
    }
    String[] names = classifyName(name, true);
    if (names == null) {
      retFileNameOrError[0] = "cannot read file name: " + name;
      return null;
    }
    Object image = null;
    ApiPlatform apiPlatform = viewer.apiPlatform;
    String fullPathName = names[0].replace('\\', '/');
    if (fullPathName.indexOf("|") > 0) {
      Object ret = getFileAsBytes(fullPathName, null, true);
      if (!(ret instanceof byte[])) {
        retFileNameOrError[0] = "" + ret;
        return null;
      }
      image = apiPlatform.createImage(ret);
    } else if (urlTypeIndex(fullPathName) >= 0) {
      try {
        image = apiPlatform.createImage(new URL(fullPathName));
      } catch (Exception e) {
        retFileNameOrError[0] = "bad URL: " + fullPathName;
        return null;
      }
    } else {
      image = apiPlatform.createImage(fullPathName);
    }
    if (image == null)
      return null;
    try {
      if (!apiPlatform.waitForDisplay(viewer.getDisplay(), image)) {
        return null;
      }
      /* SUN but here for malformed URL - can't trap
       Uncaught error fetching image:
       java.lang.NullPointerException
       at sun.net.www.ParseUtil.toURI(Unknown Source)
       at sun.net.www.protocol.http.HttpURLConnection.plainConnect(Unknown Source)
       at sun.net.www.protocol.http.HttpURLConnection.connect(Unknown Source)
       at sun.net.www.protocol.http.HttpURLConnection.getInputStream(Unknown Source)
       at sun.awt.image.URLImageSource.getDecoder(Unknown Source)
       at sun.awt.image.InputStreamImageSource.doFetch(Unknown Source)
       at sun.awt.image.ImageFetcher.fetchloop(Unknown Source)
       at sun.awt.image.ImageFetcher.run(Unknown Source)
       */
    } catch (Exception e) {
      e.printStackTrace();
      retFileNameOrError[0] = e.getMessage() + " opening " + fullPathName;
      return null;
    }
    if (apiPlatform.getImageWidth(image) < 1) {
      retFileNameOrError[0] = "invalid or missing image " + fullPathName;
      return null;
    }
    retFileNameOrError[0] = fullPathName;
    return image;
  }

  private final static int URL_LOCAL = 3;
  private final static String[] urlPrefixes = { "http:", "https:", "ftp:",
      "file:" };

  public static int urlTypeIndex(String name) {
    for (int i = 0; i < urlPrefixes.length; ++i) {
      if (name.startsWith(urlPrefixes[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * 
   * @param name
   * @param isFullLoad
   * @return [0] full path name, [1] file name without path, [2] full URL
   */
  private String[] classifyName(String name, boolean isFullLoad) {
    if (name == null)
      return new String[] { null };
    boolean doSetPathForAllFiles = (pathForAllFiles.length() > 0);
    if (name.startsWith("?")) {
       if ((name = viewer.dialogAsk("load", name.substring(1))) == null)
         return new String[] { isFullLoad ? "#CANCELED#" : null };
       doSetPathForAllFiles = false;
    }
    JmolFileInterface file = null;
    URL url = null;
    String[] names = null;
    if (name.startsWith("cache://")) {
      names = new String[3];
      names[0] = names[2] = name;
      names[1] = stripPath(names[0]);
      return names;
    }
    name = viewer.resolveDatabaseFormat(name);
    if (name.indexOf(":") < 0 && name.indexOf("/") != 0)
      name = addDirectory(viewer.getDefaultDirectory(), name);
    if (appletDocumentBaseURL != null) {
      // This code is only for the applet
      try {
        if (name.indexOf(":\\") == 1 || name.indexOf(":/") == 1)
          name = "file:/" + name;
        //        else if (name.indexOf("/") == 0 && viewer.isSignedApplet())
        //        name = "file:" + name;
        url = new URL(appletDocumentBaseURL, name);
      } catch (MalformedURLException e) {
        return new String[] { isFullLoad ? e.getMessage() : null };
      }
    } else {
      // This code is for the app -- no local file reading for headless
      if (urlTypeIndex(name) >= 0 
          || viewer.isRestricted(ACCESS.NONE) 
          || viewer.isRestricted(ACCESS.READSPT) 
              && !name.endsWith(".spt") && !name.endsWith("/")) {
        try {
          url = new URL(name);
        } catch (MalformedURLException e) {
          return new String[] { isFullLoad ? e.getMessage() : null };
        }
      } else {
        file = viewer.apiPlatform.newFile(name);
        names = new String[] { file.getAbsolutePath(), file.getName(),
            "file:/" + file.getAbsolutePath().replace('\\', '/') };
      }
    }
    if (url != null) {
      names = new String[3];
      names[0] = names[2] = url.toString();
      names[1] = stripPath(names[0]);
    }
    if (doSetPathForAllFiles) {
      String name0 = names[0];
      names[0] = pathForAllFiles + names[1];
      Logger.info("FileManager substituting " + name0 + " --> " + names[0]);
    }
    if (isFullLoad && (file != null || urlTypeIndex(names[0]) == URL_LOCAL)) {
      String path = (file == null ? TextFormat.trim(names[0].substring(5), "/")
          : names[0]);
      int pt = path.length() - names[1].length() - 1;
      if (pt > 0) {
        path = path.substring(0, pt);
        setLocalPath(viewer, path, true);
      }
    }
    return names;
  }

  private static String addDirectory(String defaultDirectory, String name) {
    if (defaultDirectory.length() == 0)
      return name;
    char ch = (name.length() > 0 ? name.charAt(0) : ' ');
    String s = defaultDirectory.toLowerCase();
    if ((s.endsWith(".zip") || s.endsWith(".tar")) && ch != '|' && ch != '/')
      defaultDirectory += "|";
    return defaultDirectory
        + (ch == '/'
            || ch == '/'
            || (ch = defaultDirectory.charAt(defaultDirectory.length() - 1)) == '|'
            || ch == '/' ? "" : "/") + name;
  }

  String getDefaultDirectory(String name) {
    String[] names = classifyName(name, true);
    if (names == null)
      return "";
    name = fixPath(names[0]);
    return (name == null ? "" : name.substring(0, name.lastIndexOf("/")));
  }

  private static String fixPath(String path) {
    path = path.replace('\\', '/');
    path = TextFormat.simpleReplace(path, "/./", "/");
    int pt = path.lastIndexOf("//") + 1;
    if (pt < 1)
      pt = path.indexOf(":/") + 1;
    if (pt < 1)
      pt = path.indexOf("/");
    if (pt < 0)
      return null;
    String protocol = path.substring(0, pt);
    path = path.substring(pt);

    while ((pt = path.lastIndexOf("/../")) >= 0) {
      int pt0 = path.substring(0, pt).lastIndexOf("/");
      if (pt0 < 0)
        return TextFormat.simpleReplace(protocol + path, "/../", "/");
      path = path.substring(0, pt0) + path.substring(pt + 3);
    }
    if (path.length() == 0)
      path = "/";
    return protocol + path;
  }

  public String getFilePath(String name, boolean addUrlPrefix,
                            boolean asShortName) {
    String[] names = classifyName(name, false);
    return (names == null || names.length == 1 ? "" : asShortName ? names[1]
        : addUrlPrefix ? names[2] 
        : names[0] == null ? "" 
        : names[0].replace('\\', '/'));
  }

  private final static String[] urlPrefixPairs = { "http:", "http://", "www.",
      "http://www.", "https:", "https://", "ftp:", "ftp://", "file:",
      "file:///" };

  public static String getLocalUrl(JmolFileInterface file) {
    // entering a url on a file input box will be accepted,
    // but cause an error later. We can fix that...
    // return null if there is no problem, the real url if there is
    if (file.getName().startsWith("="))
      return file.getName();
    String path = file.getAbsolutePath().replace('\\', '/');
    for (int i = 0; i < urlPrefixPairs.length; i++)
      if (path.indexOf(urlPrefixPairs[i]) == 0)
        return null;
    // looking for /xxx/xxxx/file://...
    for (int i = 0; i < urlPrefixPairs.length; i += 2)
      if (path.indexOf(urlPrefixPairs[i]) > 0)
        return urlPrefixPairs[i + 1]
            + TextFormat.trim(path.substring(path.indexOf(urlPrefixPairs[i])
                + urlPrefixPairs[i].length()), "/");
    return null;
  }

  public static JmolFileInterface getLocalDirectory(JmolViewer viewer, boolean forDialog) {
    String localDir = (String) viewer
        .getParameter(forDialog ? "currentLocalPath" : "defaultDirectoryLocal");
    if (forDialog && localDir.length() == 0)
      localDir = (String) viewer.getParameter("defaultDirectoryLocal");
    if (localDir.length() == 0)
      return (viewer.isApplet() ? null : viewer.apiPlatform.newFile(System
          .getProperty("user.dir", ".")));
    if (viewer.isApplet() && localDir.indexOf("file:/") == 0)
      localDir = localDir.substring(6);
    JmolFileInterface f = viewer.apiPlatform.newFile(localDir);
    return f.isDirectory() ? f : f.getParentAsFile();
  }

  /**
   * called by getImageFileNameFromDialog 
   * called by getOpenFileNameFromDialog
   * called by getSaveFileNameFromDialog
   * 
   * called by classifyName for any full file load
   * called from the CD command
   * 
   * currentLocalPath is set in all cases
   *   and is used specifically for dialogs as a first try
   * defaultDirectoryLocal is set only when not from a dialog
   *   and is used only in getLocalPathForWritingFile or
   *   from an open/save dialog.
   * In this way, saving a file from a dialog doesn't change
   *   the "CD" directory. 
   * Neither of these is saved in the state, but 
   * 
   * 
   * @param viewer
   * @param path
   * @param forDialog
   */
  public static void setLocalPath(JmolViewer viewer, String path,
                                  boolean forDialog) {
    while (path.endsWith("/") || path.endsWith("\\"))
      path = path.substring(0, path.length() - 1);
    viewer.setStringProperty("currentLocalPath", path);
    if (!forDialog)
      viewer.setStringProperty("defaultDirectoryLocal", path);
  }

  public static String getLocalPathForWritingFile(JmolViewer viewer, String file) {
    if (file.indexOf("file:/") == 0)
      return file.substring(6);
    if (file.indexOf("/") == 0 || file.indexOf(":") >= 0)
      return file;
    JmolFileInterface dir = getLocalDirectory(viewer, false);
    return (dir == null ? file : fixPath(dir.toString() + "/" + file));
  }

  public static String setScriptFileReferences(String script, String localPath,
                                               String remotePath,
                                               String scriptPath) {
    if (localPath != null)
      script = setScriptFileReferences(script, localPath, true);
    if (remotePath != null)
      script = setScriptFileReferences(script, remotePath, false);
    script = TextFormat.simpleReplace(script, "\1\"", "\"");
    if (scriptPath != null) {
      while (scriptPath.endsWith("/"))
        scriptPath = scriptPath.substring(0, scriptPath.length() - 1);
      for (int ipt = 0; ipt < scriptFilePrefixes.length; ipt++) {
        String tag = scriptFilePrefixes[ipt];
        script = TextFormat.simpleReplace(script, tag + ".", tag + scriptPath);
      }
    }
    return script;
  }

  /**
   * Sets all local file references in a script file to point to files within
   * dataPath. If a file reference contains dataPath, then the file reference is
   * left with that RELATIVE path. Otherwise, it is changed to a relative file
   * name within that dataPath. 
   * 
   * Only file references starting with "file://" are changed.
   * 
   * @param script
   * @param dataPath
   * @param isLocal 
   * @return revised script
   */
  private static String setScriptFileReferences(String script, String dataPath,
                                                boolean isLocal) {
    if (dataPath == null)
      return script;
    boolean noPath = (dataPath.length() == 0);
    List<String> fileNames = new ArrayList<String>();
    getFileReferences(script, fileNames);
    List<String> oldFileNames = new ArrayList<String>();
    List<String> newFileNames = new ArrayList<String>();
    int nFiles = fileNames.size();
    for (int iFile = 0; iFile < nFiles; iFile++) {
      String name0 = fileNames.get(iFile);
      String name = name0;
      int itype = urlTypeIndex(name);
      if (isLocal == (itype < 0 || itype == URL_LOCAL)) {
        int pt = (noPath ? -1 : name.indexOf("/" + dataPath + "/"));
        if (pt >= 0) {
          name = name.substring(pt + 1);
        } else {
          pt = name.lastIndexOf("/");
          if (pt < 0 && !noPath)
            name = "/" + name;
          if (pt < 0 || noPath)
            pt++;
          name = dataPath + name.substring(pt);
        }
      }
      Logger.info("FileManager substituting " + name0 + " --> " + name);
      oldFileNames.add("\"" + name0 + "\"");
      newFileNames.add("\1\"" + name + "\"");
    }
    return TextFormat.replaceStrings(script, oldFileNames, newFileNames);
  }

  private static String[] scriptFilePrefixes = new String[] { "/*file*/\"",
      "FILE0=\"", "FILE1=\"" };

  public static void getFileReferences(String script, List<String> fileList) {
    for (int ipt = 0; ipt < scriptFilePrefixes.length; ipt++) {
      String tag = scriptFilePrefixes[ipt];
      int i = -1;
      while ((i = script.indexOf(tag, i + 1)) >= 0) {
        String s = Parser.getNextQuotedString(script, i);
        if (s.indexOf("::") >= 0)
          s = TextFormat.split(s, "::")[1];
        fileList.add(s);
      }
    }
  }

  Object createZipSet(String fileName, String script, String[] scripts, boolean includeRemoteFiles) {
    List<Object> v = new ArrayList<Object>();
    List<String> fileNames = new ArrayList<String>();
    Long crcValue;
    Hashtable<Object,String> crcMap = new Hashtable<Object,String>();
    boolean haveSceneScript = (scripts != null && scripts.length == 3 
        && scripts[1].startsWith(SCENE_TAG));
    boolean sceneScriptOnly = (haveSceneScript && scripts[2].equals("min"));
    if (!sceneScriptOnly) {
      getFileReferences(script, fileNames);
      if (haveSceneScript)
        getFileReferences(scripts[1], fileNames);
    }
    boolean haveScripts = (!haveSceneScript && scripts != null && scripts.length > 0);
    if (haveScripts) {
      script = wrapPathForAllFiles("script " + Escape.escapeStr(scripts[0]), "");
      for (int i = 0; i < scripts.length; i++)
        fileNames.add(scripts[i]);
    }
    int nFiles = fileNames.size();
    if (fileName != null)
      fileName = fileName.replace('\\', '/');
    String fileRoot = fileName;
    if (fileRoot != null) {
      fileRoot = fileName.substring(fileName.lastIndexOf("/") + 1);
      if (fileRoot.indexOf(".") >= 0)
        fileRoot = fileRoot.substring(0, fileRoot.indexOf("."));
    }
    List<String> newFileNames = new ArrayList<String>();
    for (int iFile = 0; iFile < nFiles; iFile++) {
      String name = fileNames.get(iFile);
      int itype = urlTypeIndex(name);
      boolean isLocal = (itype < 0 || itype == URL_LOCAL);
      String newName = name;
      // also check that somehow we don't have a local file with the same name as
      // a fixed remote file name (because someone extracted the files and then used them)
      if (isLocal || includeRemoteFiles) {
        int ptSlash = name.lastIndexOf("/");
        newName = (name.indexOf("?") > 0 && name.indexOf("|") < 0 ?
            TextFormat.replaceAllCharacters(name, "/:?\"'=&", "_") : stripPath(name));
        newName = TextFormat.replaceAllCharacters(newName, "[]", "_");
        boolean isSparDir = (spardirCache != null && spardirCache.containsKey(name)); 
        if (isLocal && name.indexOf("|") < 0 
            && !isSparDir) {
          v.add(name);
          v.add(newName);
          v.add(null); // data will be gotten from disk
        } else {
          // all remote files, and any file that was opened from a ZIP collection
          Object ret = (isSparDir ? spardirCache.get(name) : getFileAsBytes(name, null, true));
          if (!(ret instanceof byte[]))
            return ret;
          CRC32 crc = new CRC32();
          crc.update((byte[])ret);
          crcValue = Long.valueOf(crc.getValue());
          // only add to the data list v when the data in the file is new
          if(crcMap.containsKey(crcValue)){
            // let newName point to the already added data
            newName = crcMap.get(crcValue); 
          }else{
            if (isSparDir)
              newName = newName.replace('.', '_');
            if (crcMap.containsKey(newName)) {
              // now we have a conflict. To different files with the same name
              // append "[iFile]" to the new file name to ensure it's unique
              int pt = newName.lastIndexOf(".");
              if (pt > ptSlash) // is a file extension, probably
                newName = newName.substring(0, pt) + "[" + iFile + "]" + newName.substring(pt);
              else
                newName = newName + "[" + iFile + "]";
            }
            v.add(name);
            v.add(newName);
            v.add(ret);
            crcMap.put(crcValue,newName);
          }
        }
        name = "$SCRIPT_PATH$" + newName;
      }
      crcMap.put(newName, newName);
      newFileNames.add(name);
    }
    if (!sceneScriptOnly) {
      script = TextFormat.replaceQuotedStrings(script, fileNames, newFileNames);
      v.add("state.spt");
      v.add(null);
      v.add(script.getBytes());
    }
    if (haveSceneScript) {
      if (scripts[0] != null) {
        v.add("animate.spt");
        v.add(null);
        v.add(scripts[0].getBytes());
      }
      v.add("scene.spt");
      v.add(null);
      script = TextFormat.replaceQuotedStrings(scripts[1], fileNames, newFileNames);
      v.add(script.getBytes());
    }
    String sname = (haveSceneScript ? "scene.spt" : "state.spt");
    v.add("JmolManifest.txt");
    v.add(null);
    String sinfo = "# Jmol Manifest Zip Format 1.1\n" + "# Created "
        + DateFormat.getDateInstance().format(new Date()) + "\n"
        + "# JmolVersion " + Viewer.getJmolVersion() + "\n" + sname;
    v.add(sinfo.getBytes());
    v.add("Jmol_version_" + Viewer.getJmolVersion().replace(' ','_').replace(':','.'));
    v.add(null);
    v.add(new byte[0]);
    if (fileRoot != null) {
      Object bytes = viewer.getImageAs("PNG", -1, -1, -1, null, null, null,
          JmolConstants.embedScript(script));
      if (bytes instanceof byte[]) {
        v.add("preview.png");
        v.add(null);
        v.add(bytes);
      }
    }
    return writeZipFile(fileName, v, "OK JMOL");
  }

  static String wrapPathForAllFiles(String cmd, String strCatch) {
    String vname = "v__" + ("" + Math.random()).substring(3);
    return "# Jmol script\n{\n\tVar " + vname + " = pathForAllFiles\n\tpathForAllFiles=\"$SCRIPT_PATH$\"\n\ttry{\n\t\t" + cmd + "\n\t}catch(e){" + strCatch + "}\n\tpathForAllFiles = " + vname + "\n}\n";
  }

  private static String stripPath(String name) {
    int pt = Math.max(name.lastIndexOf("|"), name.lastIndexOf("/"));
    return name.substring(pt + 1);
  }

  /**
   * generic method to create a zip file based on
   * http://www.exampledepot.com/egs/java.util.zip/CreateZip.html
   * 
   * @param outFileName
   *        or null to return byte[]
   * @param fileNamesAndByteArrays
   *        Vector of [filename1, bytes|null, filename2, bytes|null, ...]
   * @param msg
   * @return msg bytes filename or errorMessage or byte[]
   */
  private Object writeZipFile(String outFileName,
                              List<Object> fileNamesAndByteArrays,
                              String msg) {
    byte[] buf = new byte[1024];
    long nBytesOut = 0;
    long nBytes = 0;
    Logger.info("creating zip file " + (outFileName == null ? "" : outFileName) + "...");
    String fullFilePath = null;
    String fileList = "";
    try {
      ByteArrayOutputStream bos = (outFileName == null
          || outFileName.startsWith("http://") ? new ByteArrayOutputStream()
          : null);
      ZipOutputStream os = new ZipOutputStream(
          bos == null ? (OutputStream) new FileOutputStream(outFileName) : bos);
      for (int i = 0; i < fileNamesAndByteArrays.size(); i += 3) {
        String fname = (String) fileNamesAndByteArrays.get(i);
        byte[] bytes = null;
        if (fname.indexOf("file:/") == 0) {
          fname = fname.substring(5);
          if (fname.length() > 2 && fname.charAt(2) == ':') // "/C:..." DOS/Windows
            fname = fname.substring(1);
        } else if (fname.indexOf("cache://") == 0) {
          Object data = cacheGet(fname, false); 
          fname = fname.substring(8);
          bytes = (data instanceof byte[] ? (byte[]) data : ((String) data).getBytes());
        }
        String fnameShort = (String) fileNamesAndByteArrays.get(i + 1);
        if (fnameShort == null)
          fnameShort = fname;
        if (bytes == null)
          bytes = (byte[]) fileNamesAndByteArrays.get(i + 2);
        String key = ";" + fnameShort + ";";
        if (fileList.indexOf(key) >= 0) {
          Logger.info("duplicate entry");
          continue;
        }
        fileList += key;
        os.putNextEntry(new ZipEntry(fnameShort));
        int nOut = 0;
        if (bytes == null) {
          // get data from disk
          FileInputStream in = new FileInputStream(fname);
          int len;
          while ((len = in.read(buf)) > 0) {
            os.write(buf, 0, len);
            nOut += len;
          }
          in.close();
        } else {
          // data are already in byte form
          os.write(bytes, 0, bytes.length);
          nOut += bytes.length;
        }
        nBytesOut += nOut;
        os.closeEntry();
        Logger.info("...added " + fname + " (" + nOut + " bytes)");
      }
      os.close();
      Logger.info(nBytesOut + " bytes prior to compression");
      if (bos != null) {
        byte[] bytes = bos.toByteArray();
        if (outFileName == null)
          return bytes;
        fullFilePath = outFileName;
        nBytes = bytes.length;
        String ret = postByteArray(outFileName, bytes);
        if (ret.indexOf("Exception") >= 0)
          return ret;
        msg += " " + ret;
      } else {
        JmolFileInterface f = viewer.apiPlatform.newFile(outFileName);
        fullFilePath = f.getAbsolutePath().replace('\\', '/');
        nBytes = f.length();
      }
    } catch (IOException e) {
      Logger.info(e.getMessage());
      return e.getMessage();
    }
    return msg + " " + nBytes + " " + fullFilePath;
  }

  private String postByteArray(String outFileName, byte[] bytes) {
    Object ret = getBufferedInputStreamOrErrorMessageFromName(outFileName, null, false,
        false, bytes);
    if (ret instanceof String)
      return (String) ret;
    if (ret instanceof StringBuffer)
    	ret = getInputStreamForStringBuffer(ret);
    try {
      ret = getStreamAsBytes((BufferedInputStream) ret, null);
    } catch (IOException e) {
      try {
        ((BufferedInputStream) ret).close();
      } catch (IOException e1) {
        // ignore
      }
    }
    return new String((byte[]) ret);
  }

  ////////////////// reader classes -- DOM, File, and Files /////////////

  private class DOMReader {
    private Object aDOMNode;
    Object atomSetCollection;
    Map<String, Object> htParams;

    DOMReader(Object DOMNode, Map<String, Object> htParams) {
      this.aDOMNode = DOMNode;
      this.htParams = htParams;
    }

    void run() {
      htParams.put("nameSpaceInfo", viewer.apiPlatform.getJsObjectInfo(aDOMNode, null, null));
      atomSetCollection = viewer.getModelAdapter().getAtomSetCollectionFromDOM(
          aDOMNode, htParams);
      if (atomSetCollection instanceof String)
        return;
      viewer.zap(false, true, false);
      fullPathName = fileName = nameAsGiven = "JSNode";
    }
  }

  private class FileReader {
    private String fileNameIn;
    private String fullPathNameIn;
    private String nameAsGivenIn;
    private String fileTypeIn;
    Object atomSetCollection;
    private BufferedReader reader;
    private Map<String, Object> htParams;
    private boolean isAppend;
    private byte[] bytes;

    FileReader(String fileName, String fullPathName, String nameAsGiven,
        String type, Object reader, Map<String, Object> htParams,
        boolean isAppend) {
      fileNameIn = fileName;
      fullPathNameIn = fullPathName;
      nameAsGivenIn = nameAsGiven;
      fileTypeIn = type;
      this.reader = (reader instanceof BufferedReader ? (BufferedReader) reader : reader instanceof Reader ? new BufferedReader((Reader) reader) : null);
      this.bytes = (reader instanceof byte[] ? (byte[]) reader : null);
      this.htParams = htParams;
      this.isAppend = isAppend;
    }

    
//added -hcf

    void run(int currentSelectedScale, int currentSelectedUnit, int[] selectedPath, String downOrUp, Atom[] currentUnits) {

      if (!isAppend && viewer.displayLoadErrors)
        viewer.zap(false, true, false);

      String errorMessage = null;
      Object t = null;
      if (reader == null) {
        t = getUnzippedBufferedReaderOrErrorMessageFromName(fullPathNameIn,
            bytes, true, false, false, true);
        if (t == null || t instanceof String) {
          errorMessage = (t == null ? "error opening:" + nameAsGivenIn
              : (String) t);
          if (!errorMessage.startsWith("NOTE:"))
            Logger.error("file ERROR: " + fullPathNameIn + "\n" + errorMessage);
          atomSetCollection = errorMessage;
          return;
        }
      }

      if (reader == null) {
        if (t instanceof BufferedReader) {
          reader = (BufferedReader) t;
        } else if (t instanceof ZipInputStream) {
          String name = fullPathNameIn;
          String[] subFileList = null;
          if (name.indexOf("|") >= 0 && !name.endsWith(".zip")) {
            subFileList = TextFormat.split(name, "|");
            name = subFileList[0];
          }
          if (subFileList != null)
            htParams.put("subFileList", subFileList);
          ZipInputStream zis = (ZipInputStream) t;
          String[] zipDirectory = getZipDirectory(name, true);
          atomSetCollection = viewer.getModelAdapter()
              .getAtomSetCollectionOrBufferedReaderFromZip(zis, name,
                  zipDirectory, htParams, false, false);
          try {
            zis.close();
          } catch (Exception e) {
            //
          }
        }
      }

      if (reader != null) {
        atomSetCollection = viewer.getModelAdapter()
            .getAtomSetCollectionReader(fullPathNameIn, fileTypeIn, reader,
                htParams);// this step is for getting READER TYPE -hcf       
        if (!(atomSetCollection instanceof String)) {
        	 //here, judge whether the reader is "GSS" or "GPDB" or else for the next step data reading, else remain unchanged - hcf
        	if (htParams.get("readerName").equals("Gpdb") || htParams.get("readerName").equals("Gss")) {
                atomSetCollection = viewer.getModelAdapter().getAtomSetCollection(
                        atomSetCollection, currentSelectedScale, currentSelectedUnit, selectedPath, downOrUp, currentUnits);
        	}
        	else {
        		atomSetCollection = viewer.getModelAdapter().getAtomSetCollection(
                        atomSetCollection);
        	}
        }
      }

      if (reader != null)
        try {
          reader.close();
        } catch (IOException e) {
          // ignore
        }

      if (atomSetCollection instanceof String)
        return;

      if (!isAppend && !viewer.displayLoadErrors)
        viewer.zap(false, true, false);

      fullPathName = fullPathNameIn;
      nameAsGiven = nameAsGivenIn;
      fileName = fileNameIn;

    }
  
 
    
//added end -hcf
    

    
    
    void run() {

      if (!isAppend && viewer.displayLoadErrors)
        viewer.zap(false, true, false);

      String errorMessage = null;
      Object t = null;
      if (reader == null) {
        t = getUnzippedBufferedReaderOrErrorMessageFromName(fullPathNameIn,
            bytes, true, false, false, true);
        if (t == null || t instanceof String) {
          errorMessage = (t == null ? "error opening:" + nameAsGivenIn
              : (String) t);
          if (!errorMessage.startsWith("NOTE:"))
            Logger.error("file ERROR: " + fullPathNameIn + "\n" + errorMessage);
          atomSetCollection = errorMessage;
          return;
        }
      }

      if (reader == null) {
        if (t instanceof BufferedReader) {
          reader = (BufferedReader) t;
        } else if (t instanceof ZipInputStream) {
          String name = fullPathNameIn;
          String[] subFileList = null;
          if (name.indexOf("|") >= 0 && !name.endsWith(".zip")) {
            subFileList = TextFormat.split(name, "|");
            name = subFileList[0];
          }
          if (subFileList != null)
            htParams.put("subFileList", subFileList);
          ZipInputStream zis = (ZipInputStream) t;
          String[] zipDirectory = getZipDirectory(name, true);
          atomSetCollection = viewer.getModelAdapter()
              .getAtomSetCollectionOrBufferedReaderFromZip(zis, name,
                  zipDirectory, htParams, false, false);
          try {
            zis.close();
          } catch (Exception e) {
            //
          }
        }
      }

      if (reader != null) {
        atomSetCollection = viewer.getModelAdapter()
            .getAtomSetCollectionReader(fullPathNameIn, fileTypeIn, reader,
                htParams);
        if (!(atomSetCollection instanceof String))
          atomSetCollection = viewer.getModelAdapter().getAtomSetCollection(
              atomSetCollection);
      }

      if (reader != null)
        try {
          reader.close();
        } catch (IOException e) {
          // ignore
        }

      if (atomSetCollection instanceof String)
        return;

      if (!isAppend && !viewer.displayLoadErrors)
        viewer.zap(false, true, false);

      fullPathName = fullPathNameIn;
      nameAsGiven = nameAsGivenIn;
      fileName = fileNameIn;

    }
  }

  /**
   * open a set of models residing in different files
   * 
   */
  private class FilesReader implements JmolFilesReaderInterface {
    private String[] fullPathNamesIn;
    private String[] namesAsGivenIn;
    private String[] fileTypesIn;
    Object atomSetCollection;
    private DataReader[] stringReaders;
    private Map<String, Object> htParams;
    private boolean isAppend;

    FilesReader(String[] name, String[] nameAsGiven, String[] types,
        DataReader[] readers, Map<String, Object> htParams, boolean isAppend) {
      fullPathNamesIn = name;
      namesAsGivenIn = nameAsGiven;
      fileTypesIn = types;
      stringReaders = readers;
      this.htParams = htParams;
      this.isAppend = isAppend;
    }

    void run() {

      if (!isAppend && viewer.displayLoadErrors)
        viewer.zap(false, true, false);

      boolean getReadersOnly = !viewer.displayLoadErrors;
      atomSetCollection = viewer.getModelAdapter().getAtomSetCollectionReaders(
          this, fullPathNamesIn, fileTypesIn, htParams, getReadersOnly);
      stringReaders = null;
      if (getReadersOnly && !(atomSetCollection instanceof String)) {
        atomSetCollection = viewer.getModelAdapter()
            .getAtomSetCollectionFromSet(atomSetCollection, null, htParams);
      }
      if (atomSetCollection instanceof String) {
        Logger.error("file ERROR: " + atomSetCollection);
        return;
      }
      if (!isAppend && !viewer.displayLoadErrors)
        viewer.zap(false, true, false);

      fullPathName = fileName = nameAsGiven = (stringReaders == null ? "file[]"
          : "String[]");
    }

    /**
     * called by SmartJmolAdapter to request another buffered reader or binary document,
     * rather than opening all the readers at once.
     * 
     * @param i   the reader index
     * @param isBinary 
     * @return    a BufferedReader or null in the case of an error
     * 
     */
    @Override
	public Object getBufferedReaderOrBinaryDocument(int i, boolean isBinary) {
      if (stringReaders != null)
        return (isBinary ? null : stringReaders[i].getBufferedReader()); // no binary strings
      String name = fullPathNamesIn[i];
      String[] subFileList = null;
      htParams.remove("subFileList");
      if (name.indexOf("|") >= 0) {
        subFileList = TextFormat.split(name, "|");
        name = subFileList[0];
      }
      Object t = getUnzippedBufferedReaderOrErrorMessageFromName(name, null,
          true, isBinary, false, true);
      if (t instanceof ZipInputStream) {
        if (subFileList != null)
          htParams.put("subFileList", subFileList);
        String[] zipDirectory = getZipDirectory(name, true);
        t = getBufferedInputStreamOrErrorMessageFromName(name, fullPathNamesIn[i], 
            false, false, null);
        BufferedInputStream bis = (t instanceof StringBuffer 
        		? getInputStreamForStringBuffer(t)
        		: (BufferedInputStream) t);
        t = viewer.getModelAdapter()
            .getAtomSetCollectionOrBufferedReaderFromZip(bis, name,
                zipDirectory, htParams, true, isBinary);
      }
      if (t instanceof BufferedInputStream)
        return new BinaryDocument((BufferedInputStream) t);
      if (t instanceof BufferedReader || t instanceof BinaryDocument) {
        return t;
      }
      return (t == null ? "error opening:" + namesAsGivenIn[i] : (String) t);
    }

  }

  /**
   * Just a simple abstract class to join a String reader and a String[]
   * reader under the same BufferedReader umbrella.
   * 
   * Subclassed as StringDataReader, ArrayDataReader, and VectorDataReader
   * 
   */

  abstract class DataReader extends BufferedReader {

    DataReader(Reader in) {
      super(in);
    }

    BufferedReader getBufferedReader() {
      return this;
    }

    protected int readBuf(char[] buf) throws IOException {
      // not used by StringDataReader
      int nRead = 0;
      String line = readLine();
      if (line == null)
        return 0;
      int linept = 0;
      int linelen = line.length();
      for (int i = 0; i < buf.length && linelen >= 0; i++) {
        if (linept >= linelen) {
          linept = 0;
          buf[i] = '\n';
          line = readLine();
          linelen = (line == null ? -1 : line.length());
        } else {
          buf[i] = line.charAt(linept++);
        }
        nRead++;
      }
      return nRead;
    }
  }

  /**
   * 
   * ArrayDataReader subclasses BufferedReader and overrides its
   * read, readLine, mark, and reset methods so that JmolAdapter 
   * works with String[] arrays without any further adaptation. 
   * 
   */

  class ArrayDataReader extends DataReader {
    private String[] data;
    private int pt;
    private int len;

    ArrayDataReader(String[] data) {
      super(new StringReader(""));
      this.data = data;
      len = data.length;
    }

    @Override
    public int read(char[] buf) throws IOException {
      return readBuf(buf);
    }

    @Override
    public String readLine() {
      return (pt < len ? data[pt++] : null);
    }

    int ptMark;

    /**
     * 
     * @param ptr
     */
    public void mark(long ptr) {
      //ignore ptr.
      ptMark = pt;
    }

    @Override
    public void reset() {
      pt = ptMark;
    }
  }

  class StringDataReader extends DataReader {

    StringDataReader(String data) {
      super(new StringReader(data));
    }
  }

  /**
   * 
   * VectorDataReader subclasses BufferedReader and overrides its
   * read, readLine, mark, and reset methods so that JmolAdapter 
   * works with Vector<String> arrays without any further adaptation. 
   * 
   */

  class VectorDataReader extends DataReader {
    private List<String> data;
    private int pt;
    private int len;

    VectorDataReader(List<String> data) {
      super(new StringReader(""));
      this.data = data;
      len = data.size();
    }

    @Override
    public int read(char[] buf) throws IOException {
      return readBuf(buf);
    }

    @Override
    public String readLine() {
      return (pt < len ? data.get(pt++) : null);
    }

    int ptMark;

    /**
     * 
     * @param ptr
     */
    public void mark(long ptr) {
      //ignore ptr.
      ptMark = pt;
    }

    @Override
    public void reset() {
      pt = ptMark;
    }
  }

  public static String fixFileNameVariables(String format, String fname) {
    String str = TextFormat.simpleReplace(format, "%FILE", fname);
    if (str.indexOf("%LC") < 0)
      return str;
    fname = fname.toLowerCase();
    str = TextFormat.simpleReplace(str, "%LCFILE", fname);
    if (fname.length() == 4)
      str = TextFormat.simpleReplace(str, "%LC13", fname.substring(1, 3));
    return str;
  }

  /*
  private class MonitorInputStream extends FilterInputStream {
    private long length;
    private int position;
    private int markPosition;

    MonitorInputStream(InputStream in, long length) {
      super(in);
      this.length = length;
    }

    public int read() throws IOException {
      int nextByte = super.read();
      if (nextByte >= 0)
        ++position;
      return nextByte;
    }

    public int read(byte[] b) throws IOException {
      int cb = super.read(b);
      if (cb > 0)
        position += cb;
      return cb;
    }

    public int read(byte[] b, int off, int len) throws IOException {
      int cb = super.read(b, off, len);
      if (cb > 0)
        position += cb;
      return cb;
    }

    public long skip(long n) throws IOException {
      long cb = super.skip(n);
      // this will only work in relatively small files ... 2Gb
      position += cb;
      return cb;
    }

    public void mark(int readlimit) {
      super.mark(readlimit);
      markPosition = position;
    }

    public void reset() throws IOException {
      position = markPosition;
      super.reset();
    }

    int getPosition() {
      return position;
    }

    long getLength() {
      return length;
    }

    int getPercentageRead() {
      return (int) (position * 100 / length;
    }
  }
  */
  
  private Map<String, byte[]> pngjCache;
  private Map<String, byte[]> spardirCache;
  
  void clearPngjCache(String fileName) {
    if (fileName == null || pngjCache != null && pngjCache.containsKey(getCanonicalName(getZipRoot(fileName))))
      pngjCache = null;
  }


  private byte[] getCachedPngjBytes(String pathName) {
    if (pathName.indexOf(".png") < 0)
      return null;
    Logger.info("FileManager checking PNGJ cache for " + pathName);
    String shortName = shortSceneFilename(pathName);

    if (pngjCache == null && !cachePngjFile(new String[] { pathName, null }))
      return null;
    boolean isMin = (pathName.indexOf(".min.") >= 0);
    if (!isMin) {
      String cName = getCanonicalName(getZipRoot(pathName));
      if (!pngjCache.containsKey(cName)
          && !cachePngjFile(new String[] { pathName, null }))
        return null;
      if (pathName.indexOf("|") < 0)
        shortName = cName;
    }
    if (pngjCache.containsKey(shortName)) {
      Logger.info("FileManager using memory cache " + shortName);
      return pngjCache.get(shortName);
    }
    for (String key : pngjCache.keySet())
      System.out.println(key);
    System.out.println("FileManager memory cache (" + pngjCache.size()
        + ") did not find " + pathName + " as " + shortName);
    if (!isMin || !cachePngjFile(new String[] { pathName, null }))
      return null;
    Logger.info("FileManager using memory cache " + shortName);
    return pngjCache.get(shortName);
  }

  boolean cachePngjFile(String[] data) {
    pngjCache = new Hashtable<String, byte[]>();
    data[1] = null;
    if (data[0] == null)
      return false;
    data[0] = getZipRoot(data[0]);
    String shortName = shortSceneFilename(data[0]);
    try {
      data[1] = ZipUtil.cacheZipContents(
          ZipUtil.checkPngZipStream((BufferedInputStream) getBufferedInputStreamOrErrorMessageFromName(
              data[0], null, false, false, null)), shortName, pngjCache);
    } catch (Exception e) {
      return false;
    }
    if (data[1] == null)
      return false;
    byte[] bytes = data[1].getBytes();
    pngjCache.put(getCanonicalName(data[0]), bytes); // marker in case the .all. file is changed
    if (shortName.indexOf("_scene_") >= 0) {
      pngjCache.put(shortSceneFilename(data[0]), bytes); // good for all .min. files of this scene set
      bytes = pngjCache.remove(shortName + "|state.spt");
      if (bytes != null) 
        pngjCache.put(shortSceneFilename(data[0] + "|state.spt"), bytes);
    }
    for (String key: pngjCache.keySet())
      System.out.println(key);
    return true;
  }
  
  private static String getZipRoot(String fileName) {
    int pt = fileName.indexOf("|");
    return (pt < 0 ? fileName : fileName.substring(0, pt));
  }

  private String getCanonicalName(String pathName) {
    String[] names = classifyName(pathName, true);
    return (names == null ? pathName : names[2]);
  }
  
  private String shortSceneFilename(String pathName) {
    pathName = getCanonicalName(pathName);
    int pt = pathName.indexOf("_scene_") + 7;
    if (pt < 7)
      return pathName;
    String s = "";
    if (pathName.endsWith("|state.spt")) {
      int pt1 = pathName.indexOf('.', pt);
      if (pt1 < 0)
        return pathName;
      s = pathName.substring(pt, pt1);
    }
    int pt2 = pathName.lastIndexOf("|");
    return pathName.substring(0, pt) + s
        + (pt2 > 0 ? pathName.substring(pt2) : "");
  }

  private final static String SCENE_TAG = "###scene.spt###";
  public static String getSceneScript(String[] scenes, Map<String, String> htScenes, List<Integer> list) {
    // no ".spt.png" -- that's for the sceneScript-only version
    // that we will create here.
    // extract scenes based on "pause scene ..." commands
    int iSceneLast = 0;
    int iScene = 0;
    StringBuffer sceneScript = new StringBuffer(SCENE_TAG + " Jmol " + JmolViewer.getJmolVersion() + "\n{\nsceneScripts={");
    for (int i = 1; i < scenes.length; i++) {
      scenes[i - 1] = TextFormat.trim(scenes[i - 1], "\t\n\r ");
      int[] pt = new int[1];
      iScene = Parser.parseInt(scenes[i], pt);
      if (iScene == Integer.MIN_VALUE)
        return "bad scene ID: " + iScene;
      scenes[i] = scenes[i].substring(pt[0]);
      list.add(Integer.valueOf(iScene));
      String key = iSceneLast + "-" + iScene;
      htScenes.put(key, scenes[i - 1]);
      if (i > 1)
        sceneScript.append(",");
      sceneScript.append('\n')
        .append(Escape.escapeStr(key))
        .append(": ")
        .append(Escape.escapeStr(scenes[i - 1]));
      iSceneLast = iScene;
    }
    sceneScript.append("\n}\n");
    if (list.size() == 0) 
      return "no lines 'pause scene n'";
    sceneScript
      .append("\nthisSceneRoot = '$SCRIPT_PATH$'.split('_scene_')[1];\n")
      .append("thisSceneID = 0 + ('$SCRIPT_PATH$'.split('_scene_')[2]).split('.')[1];\n")
      .append("var thisSceneState = '$SCRIPT_PATH$'.replace('.min.png','.all.png') + 'state.spt';\n")
      .append("var spath = ''+currentSceneID+'-'+thisSceneID;\n")
      .append("print thisSceneRoot + ' ' + spath;\n")
      .append("var sscript = sceneScripts[spath];\n")
      .append("var isOK = true;\n")
      .append("try{\n")
      .append("if (thisSceneRoot != currentSceneRoot){\n")
      .append(" isOK = false;\n")
      .append("} else if (sscript != '') {\n")
      .append(" isOK = true;\n")
      .append("} else if (thisSceneID <= currentSceneID){\n")
      .append(" isOK = false;\n")
      .append("} else {\n")
      .append(" sscript = '';\n")
      .append(" for (var i = currentSceneID; i < thisSceneID; i++){\n")
      .append("  var key = ''+i+'-'+(i + 1); var script = sceneScripts[key];\n")
      .append("  if (script = '') {isOK = false;break;}\n")
      .append("  sscript += ';'+script;\n")
      .append(" }\n")
      .append("}\n}catch(e){print e;isOK = false}\n")
      .append("if (isOK) {" 
          + FileManager.wrapPathForAllFiles("script inline @sscript", "print e;isOK = false") 
          + "}\n")
      .append("if (!isOK){script @thisSceneState}\n")
      .append("currentSceneRoot = thisSceneRoot; currentSceneID = thisSceneID;\n}\n");
    return sceneScript.toString();
  }

  private Map<String, Object> cache = new Hashtable<String, Object>();
  void cachePut(String key, Object data) {
    key = key.replace('\\', '/');
    if (Logger.debugging)
      Logger.info("cachePut " + key);
    if (data == null || data.equals(""))
      cache.remove(key);
    else
      cache.put(key, data);
  }
  
  Object cacheGet(String key, boolean bytesOnly) {
    key = key.replace('\\', '/');
    if (Logger.debugging && cache.containsKey(key))
      Logger.info("cacheGet " + key);
    Object data = cache.get(key);
    return (bytesOnly && (data instanceof String) ? null : data);
  }

  void cacheClear() {
    cache.clear();
  }

  public int cacheFileByName(String fileName, boolean isAdd) {
    if (fileName == null || !isAdd && fileName.equalsIgnoreCase("")) {
      cacheClear();
      return -1;
    }
    Object data;
    if (isAdd) {
      fileName = viewer.resolveDatabaseFormat(fileName);
      data = getFileAsBytes(fileName, null, true);
      if (data instanceof String)
        return 0;
      cachePut(fileName, data);
    } else {
      data = cache.remove(fileName.replace('\\', '/'));
    }
    return (data == null ? 0 : data instanceof String ? ((String) data).length()
        : ((byte[]) data).length);
  }

  Map<String, Integer> cacheList() {
    Map<String, Integer> map = new Hashtable<String, Integer>();
    for (Map.Entry<String, Object> entry : cache.entrySet())
      map.put(entry.getKey(), Integer
          .valueOf(entry.getValue() instanceof byte[] ? ((byte[]) entry
              .getValue()).length : entry.getValue().toString().length()));
    return map;
  }

}
