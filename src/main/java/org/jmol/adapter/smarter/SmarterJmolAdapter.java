/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-09-08 16:41:08 -0500 (Sat, 08 Sep 2012) $
 * $Revision: 17536 $
 *
 * Copyright (C) 2003-2005  Miguel, Jmol Development, www.jmol.org
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

package org.jmol.adapter.smarter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolAdapterAtomIterator;
import org.jmol.api.JmolAdapterBondIterator;
import org.jmol.api.JmolAdapterStructureIterator;
import org.jmol.api.JmolFilesReaderInterface;
import org.jmol.modelset.Atom;
import org.jmol.util.CompoundDocument;
import org.jmol.util.Logger;
import org.jmol.util.TextFormat;
import org.jmol.util.ZipUtil;

public class SmarterJmolAdapter extends JmolAdapter {

  public SmarterJmolAdapter() {
    super("SmarterJmolAdapter");
  }

  /**************************************************************
   * 
   * AtomSetCollectionReader.readData() will close any BufferedReader
   *  
   * **************************************************************/

  public final static String PATH_KEY = ".PATH";
  public final static String PATH_SEPARATOR =
    System.getProperty("path.separator", "/");

  /**
   * Just get the resolved file type; if a file, does NOT close the reader
   * 
   * @param atomSetCollectionOrReader 
   * @return a file type or null
   * 
   */
  @Override
  public String getFileTypeName(Object atomSetCollectionOrReader) {
    if (atomSetCollectionOrReader instanceof AtomSetCollection)
      return ((AtomSetCollection)atomSetCollectionOrReader).getFileTypeName();
    if (atomSetCollectionOrReader instanceof BufferedReader)
      return Resolver.getFileType((BufferedReader)atomSetCollectionOrReader);
    return null;
  }

  @Override
  public Object getAtomSetCollectionReader(String name, String type,
                                           BufferedReader bufferedReader, Map<String, Object> htParams) {
    return staticGetAtomSetCollectionReader(name, type, bufferedReader, htParams);
  }

    /**
   * The primary file or string reader -- returns just the reader now
   * 
   * @param name 
   * @param type 
   * @param bufferedReader 
   * @param htParams 
   * @return        an AtomSetCollectionReader or an error string
   * 
   */
  public static Object staticGetAtomSetCollectionReader(String name, String type,
                                   BufferedReader bufferedReader, Map<String, Object> htParams) {
    try {
      Object ret = Resolver.getAtomCollectionReader(name, type,
          bufferedReader, htParams, -1);
      if (ret instanceof String) {
        try {
          bufferedReader.close();
        } catch (Exception e) {
          //
        }
      } else {
        ((AtomSetCollectionReader) ret).setup(name, htParams, bufferedReader);
      }
      return ret;        
    } catch (Throwable e) {
      try {
        bufferedReader.close();
      } catch (Exception ex) {
        //
      }
      bufferedReader = null;
      Logger.error(null, e);
      return "" + e;
    }
  }

//added -hcf
  
  @Override
  public Object getAtomSetCollection(Object atomSetCollectionReader, int currentSelectedScale, int currentSelectedUnit, int[] selectedPath, String downOrUp, Atom[] currentUnits) {
    return staticGetAtomSetCollection((AtomSetCollectionReader) atomSetCollectionReader, currentSelectedScale, currentSelectedUnit, selectedPath, downOrUp, currentUnits);
  }  


  public static Object staticGetAtomSetCollection(AtomSetCollectionReader a, int currentSelectedScale, int currentSelectedUnit, int[] selectedPath, String downOrUp, Atom[] currentUnits) {
	    BufferedReader br = null;
	    try {
	      br = a.reader;
	      Object ret = a.readData(currentSelectedScale, currentSelectedUnit, selectedPath, downOrUp, currentUnits);
	      
	      if (!(ret instanceof AtomSetCollection))
	        return ret;
	      AtomSetCollection atomSetCollection = (AtomSetCollection) ret;
	      if (atomSetCollection.errorMessage != null)
	        return atomSetCollection.errorMessage;
	      return atomSetCollection;
	    } catch (Throwable e) {
	      try {
	        br.close();
	      } catch (Exception ex) {
	        //
	      }
	      br = null;
	      Logger.error(null, e);
	      return "" + e;
	    }
	  }
  
  
//added end -hcf
  
  
  
  /**
   * Create the AtomSetCollection and return it
   * 
   * @param atomSetCollectionReader 
   * @return an AtomSetCollection or an error string
   * 
   */
  @Override
  public Object getAtomSetCollection(Object atomSetCollectionReader) {
    return staticGetAtomSetCollection((AtomSetCollectionReader) atomSetCollectionReader);
  }

  public static Object staticGetAtomSetCollection(AtomSetCollectionReader a) {
    BufferedReader br = null;
    try {
      br = a.reader;
      Object ret = a.readData();
      if (!(ret instanceof AtomSetCollection))
        return ret;
      AtomSetCollection atomSetCollection = (AtomSetCollection) ret;
      if (atomSetCollection.errorMessage != null)
        return atomSetCollection.errorMessage;
      return atomSetCollection;
    } catch (Throwable e) {
      try {
        br.close();
      } catch (Exception ex) {
        //
      }
      br = null;
      Logger.error(null, e);
      return "" + e;
    }
  }

  /**
   * primary for String[] or File[] reading -- two options
   * are implemented --- return a set of simultaneously open readers,
   * or return one single collection using a single reader
   * 
   * @param fileReader 
   * @param names 
   * @param types 
   * @param htParams 
   * @param getReadersOnly  TRUE for a set of readers; FALSE for one atomSetCollection
   * 
   * @return a set of AtomSetCollectionReaders, a single AtomSetCollection, or an error string
   * 
   */
  @Override
  public Object getAtomSetCollectionReaders(JmolFilesReaderInterface fileReader,
                                            String[] names, String[] types,
                                            Map<String, Object> htParams,
                                            boolean getReadersOnly) {
    //FilesOpenThread
    int size = names.length;
    AtomSetCollectionReader[] readers = (getReadersOnly ? new AtomSetCollectionReader[size]
        : null);
    AtomSetCollection[] atomsets = (getReadersOnly ? null
        : new AtomSetCollection[size]);
    for (int i = 0; i < size; i++) {
      try {
        Object reader = fileReader.getBufferedReaderOrBinaryDocument(i, false);
        if (!(reader instanceof BufferedReader))
          return reader;
        Object ret = Resolver.getAtomCollectionReader(names[i],
            (types == null ? null : types[i]), (BufferedReader) reader,
            htParams, i);
        if (!(ret instanceof AtomSetCollectionReader))
          return ret;
        AtomSetCollectionReader r = (AtomSetCollectionReader) ret;
        if (r.isBinary) {
          r.setup(names[i], htParams, fileReader.getBufferedReaderOrBinaryDocument(i, true));
        } else { 
          r.setup(names[i], htParams, reader);
        }
        if (getReadersOnly) {
          readers[i] = r;
        } else {
          ret = r.readData();
          if (!(ret instanceof AtomSetCollection))
            return ret;
          atomsets[i] = (AtomSetCollection) ret;
          if (atomsets[i].errorMessage != null)
            return atomsets[i].errorMessage;
        }
      } catch (Throwable e) {
        Logger.error(null, e);
        return "" + e;
      }
    }
    if (getReadersOnly)
      return readers;
    return getAtomSetCollectionFromSet(readers, atomsets, htParams);
  }
   
  /**
   * 
   * needed to consolidate a set of models into one model; could start
   * with AtomSetCollectionReader[] or with AtomSetCollection[]
   * 
   * @param readerSet 
   * @param atomsets 
   * @param htParams 
   * @return a single AtomSetCollection or an error string
   * 
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object getAtomSetCollectionFromSet(Object readerSet, Object atomsets,
                                            Map<String, Object> htParams) {
    AtomSetCollectionReader[] readers = (AtomSetCollectionReader[]) readerSet;
    AtomSetCollection[] asc = (atomsets == null ? new AtomSetCollection[readers.length]
        : (AtomSetCollection[]) atomsets);
    if (atomsets == null) {
      for (int i = 0; i < readers.length; i++) {
        try {
          Object ret = readers[i].readData();
          if (!(ret instanceof AtomSetCollection))
            return ret;
          asc[i] = (AtomSetCollection) ret;
          if (asc[i].errorMessage != null)
            return asc[i].errorMessage;
        } catch (Throwable e) {
          Logger.error(null, e);
          return "" + e;
        }
      }
    }
    AtomSetCollection result;
    if (htParams.containsKey("trajectorySteps")) {
      // this is one model with a set of coordinates from a 
      // molecular dynamics calculation
      // all the htParams[] entries point to the same Hashtable
      result = asc[0];
      try {
        result.finalizeTrajectory(
            (List<Point3f[]>) htParams.get("trajectorySteps"),
            (List<Vector3f[]>) htParams.get("vibrationSteps"));
      } catch (Exception e) {
        if (result.errorMessage == null)
          result.errorMessage = e.getMessage();
      }
    } else { 
      result = new AtomSetCollection(asc);
    }
    return (result.errorMessage == null ? result : result.errorMessage);
  }

  /**
   * A rather complicated means of reading a ZIP file, which could be a 
   * single file, or it could be a manifest-organized file, or it could be
   * a Spartan directory.
   * 
   * @param is 
   * @param fileName 
   * @param zipDirectory 
   * @param htParams 
   * @param asBufferedReader 
   * @return a single atomSetCollection
   * 
   */
  @Override
  public Object getAtomSetCollectionOrBufferedReaderFromZip(InputStream is, String fileName, String[] zipDirectory,
                             Map<String, Object> htParams, boolean asBufferedReader, boolean asBufferedInputStream) {
    return staticGetAtomSetCollectionOrBufferedReaderFromZip(is, fileName, zipDirectory, htParams, 1, asBufferedReader, asBufferedInputStream);
  }

  private static Object staticGetAtomSetCollectionOrBufferedReaderFromZip(
                                    InputStream is, String fileName,
                                    String[] zipDirectory, Map<String, Object> htParams,
                                    int subFilePtr, boolean asBufferedReader, boolean asBufferedInputStream) {

    // we're here because user is using | in a load file name
    // or we are opening a zip file.

    boolean doCombine = (subFilePtr == 1);
    htParams.put("zipSet", fileName);
    String[] subFileList = (String[]) htParams.get("subFileList");
    if (subFileList == null)
      subFileList = Resolver.checkSpecialInZip(zipDirectory);
    String subFileName = (subFileList == null
        || subFilePtr >= subFileList.length ? null : subFileList[subFilePtr]);
    if (subFileName != null
        && (subFileName.startsWith("/") || subFileName.startsWith("\\")))
      subFileName = subFileName.substring(1);
    int selectedFile = 0;
    if (subFileName == null && htParams.containsKey("modelNumber")) {
      selectedFile = ((Integer) htParams.get("modelNumber")).intValue();
      if (selectedFile > 0 && doCombine)
        htParams.remove("modelNumber");
    }

    // zipDirectory[0] is the manifest if present
    String manifest = (String) htParams.get("manifest");
    boolean useFileManifest = (manifest == null);
    if (useFileManifest)
      manifest = (zipDirectory.length > 0 ? zipDirectory[0] : "");
    boolean haveManifest = (manifest.length() > 0);
    if (haveManifest) {
      if (Logger.debugging)
        Logger.info("manifest for  " + fileName + ":\n" + manifest);
    }
    boolean ignoreErrors = (manifest.indexOf("IGNORE_ERRORS") >= 0);
    boolean selectAll = (manifest.indexOf("IGNORE_MANIFEST") >= 0);
    boolean exceptFiles = (manifest.indexOf("EXCEPT_FILES") >= 0);
    if (selectAll || subFileName != null)
      haveManifest = false;
    if (useFileManifest && haveManifest) {
      String path = ZipUtil.getManifestScriptPath(manifest);
      if (path != null)
        return NOTE_SCRIPT_FILE + fileName + path + "\n";
    }
    List<Object> vCollections = new ArrayList<Object>();
    Map<String, Object> htCollections = (haveManifest ? new Hashtable<String, Object>() : null);
    int nFiles = 0;
    // 0 entry is manifest

    // check for a Spartan directory. This is not entirely satisfying,
    // because we aren't reading the file in the proper sequence.
    // this code is a hack that should be replaced with the sort of code
    // running in FileManager now.

    Object ret = Resolver.checkSpecialData(is, zipDirectory);
    if (ret instanceof String)
      return ret;
    StringBuffer data = (StringBuffer) ret;
    try {
      if (data != null) {
        BufferedReader reader = new BufferedReader(new StringReader(data.toString()));
        if (asBufferedReader) {
          return reader;
        }
        ret = Resolver.getAtomCollectionReader(fileName, null, reader,
            htParams, -1);
        if (!(ret instanceof AtomSetCollectionReader))
          return ret;
        ((AtomSetCollectionReader) ret).setup(fileName, htParams, reader);
        ret = ((AtomSetCollectionReader) ret).readData();
        if (ret instanceof AtomSetCollection) {
          AtomSetCollection atomSetCollection = (AtomSetCollection) ret;
          if (atomSetCollection.errorMessage != null) {
            if (ignoreErrors)
              return null;
            return atomSetCollection.errorMessage;
          }
          return atomSetCollection;
        }
        if (ignoreErrors)
          return null;
        return "unknown reader error";
      }
      if (is instanceof BufferedInputStream)
        is = ZipUtil.checkPngZipStream((BufferedInputStream) is);
      ZipInputStream zis = ZipUtil.getStream(is);
      ZipEntry ze;
      if (haveManifest)
        manifest = '|' + manifest.replace('\r', '|').replace('\n', '|') + '|';
      while ((ze = zis.getNextEntry()) != null
          && (selectedFile <= 0 || vCollections.size() < selectedFile)) {
        if (ze.isDirectory())
          continue;
        String thisEntry = ze.getName();
        if (subFileName != null && !thisEntry.equals(subFileName))
          continue;
        if (subFileName != null)
          htParams.put("subFileName", subFileName);
        if (ZipUtil.isJmolManifest(thisEntry) || haveManifest
            && exceptFiles == manifest.indexOf("|" + thisEntry + "|") >= 0)
          continue;
        byte[] bytes = ZipUtil.getStreamBytes(zis, ze.getSize());
        if (ZipUtil.isZipFile(bytes)) {
          BufferedInputStream bis = new BufferedInputStream(
              new ByteArrayInputStream(bytes));
          String[] zipDir2 = ZipUtil.getZipDirectoryAndClose(bis, true);
          bis = new BufferedInputStream(new ByteArrayInputStream(bytes));
          Object atomSetCollections = staticGetAtomSetCollectionOrBufferedReaderFromZip(
              bis, fileName + "|" + thisEntry, zipDir2, htParams, ++subFilePtr,
              asBufferedReader, asBufferedInputStream);
          if (atomSetCollections instanceof String) {
            if (ignoreErrors)
              continue;
            return atomSetCollections;
          } else if (atomSetCollections instanceof AtomSetCollection
              || atomSetCollections instanceof List<?>) {
            if (haveManifest && !exceptFiles)
              htCollections.put(thisEntry, atomSetCollections);
            else
              vCollections.add(atomSetCollections);
          } else if (atomSetCollections instanceof BufferedReader) {
            if (doCombine)
              zis.close();
            return atomSetCollections; // FileReader has requested a zip file
            // BufferedReader
          } else {
            if (ignoreErrors)
              continue;
            zis.close();
            return "unknown zip reader error";
          }
        } else if (asBufferedInputStream){ 
          if (ZipUtil.isGzip(bytes))
            return ZipUtil.getGzippedInputStream(bytes);
          BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(bytes));
            if (doCombine)
              zis.close();
            return bis;          
        } else {
          String sData = (CompoundDocument.isCompoundDocument(bytes) ? (new CompoundDocument(
              new BufferedInputStream(new ByteArrayInputStream(bytes))))
              .getAllData("Molecule", "Input").toString()
              : ZipUtil.isGzip(bytes) ? ZipUtil.getGzippedBytesAsString(bytes)
                  : new String(bytes));
          BufferedReader reader = new BufferedReader(new StringReader(sData));
          if (asBufferedReader) {
            if (doCombine)
              zis.close();
            return reader;
          }
          String fname = fileName + "|" + ze.getName();
          ret = Resolver.getAtomCollectionReader(fname,
              null, reader, htParams, -1);
          if (ret instanceof AtomSetCollectionReader) {
            ((AtomSetCollectionReader) ret).setup(fname, htParams, reader);
            ret = ((AtomSetCollectionReader) ret).readData();
          }
          if (!(ret instanceof AtomSetCollection)) {
            if (ignoreErrors)
              continue;
            zis.close();
            return "" + ret;
          }
          if (haveManifest && !exceptFiles)
            htCollections.put(thisEntry, ret);
          else
            vCollections.add(ret);
          AtomSetCollection a = (AtomSetCollection) ret;
          if (a.errorMessage != null) {
            if (ignoreErrors)
              continue;
            zis.close();
            return a.errorMessage;
          }
        }
      }
      if (doCombine)
        zis.close();

      // if a manifest exists, it sets the files and file order

      if (haveManifest && !exceptFiles) {
        String[] list = TextFormat.split(manifest, '|');
        for (int i = 0; i < list.length; i++) {
          String file = list[i];
          if (file.length() == 0 || file.indexOf("#") == 0)
            continue;
          if (htCollections.containsKey(file))
            vCollections.add(htCollections.get(file));
          else if (Logger.debugging)
            Logger.info("manifested file " + file + " was not found in "
                + fileName);
        }
      }
      if (!doCombine)
        return vCollections;
      AtomSetCollection result = new AtomSetCollection(vCollections);
      if (result.errorMessage != null) {
        if (ignoreErrors)
          return null;
        return result.errorMessage;
      }
      if (nFiles == 1)
        selectedFile = 1;
      if (selectedFile > 0 && selectedFile <= vCollections.size())
        return vCollections.get(selectedFile - 1);
      return result;

    } catch (Exception e) {
      if (ignoreErrors)
        return null;
      Logger.error(null, e);
      return "" + e;
    } catch (Error er) {
      Logger.error(null, er);
      return "" + er;
    }
  }

  /**
   * Direct DOM HTML4 page reading; Egon was interested in this at one point.
   * 
   * @param DOMNode 
   * @param htParams 
   * @return a single AtomSetCollection or an error string
   * 
   */
  @Override
  public Object getAtomSetCollectionFromDOM(Object DOMNode, Map<String, Object> htParams) {
    try {
      Object ret = Resolver.DOMResolve(DOMNode, htParams);
      if (!(ret instanceof AtomSetCollectionReader))
        return ret;
      AtomSetCollectionReader a = (AtomSetCollectionReader) ret;
      a.setup("DOM node", htParams, null);
      ret = a.readData(DOMNode);
      if (!(ret instanceof AtomSetCollection))
        return ret;
      AtomSetCollection asc = (AtomSetCollection) ret;
      if (asc.errorMessage != null)
        return asc.errorMessage;
      return asc;
    } catch (Throwable e) {
      Logger.error(null, e);
      return "" + e;
    }
  }

  @Override
  public String[] specialLoad(String name, String type) {
    return Resolver.specialLoad(name, type);  
  }
  
  @Override
  public void finish(Object atomSetCollection) {
    ((AtomSetCollection)atomSetCollection).finish();
  }

  ////////////////////////// post processing ////////////////////////////
  
  @Override
  public String getAtomSetCollectionName(Object atomSetCollection) {
    return ((AtomSetCollection)atomSetCollection).getCollectionName();
  }
  
  @Override
  public Map<String, Object> getAtomSetCollectionAuxiliaryInfo(Object atomSetCollection) {
    return ((AtomSetCollection)atomSetCollection).getAtomSetCollectionAuxiliaryInfo();
  }

  @Override
  public int getAtomSetCount(Object atomSetCollection) {
    return ((AtomSetCollection)atomSetCollection).getAtomSetCount();
  }

  @Override
  public int getAtomSetNumber(Object atomSetCollection, int atomSetIndex) {
    return ((AtomSetCollection)atomSetCollection).getAtomSetNumber(atomSetIndex);
  }

  @Override
  public String getAtomSetName(Object atomSetCollection, int atomSetIndex) {
    return ((AtomSetCollection)atomSetCollection).getAtomSetName(atomSetIndex);
  }
  
  @Override
  public Map<String, Object> getAtomSetAuxiliaryInfo(Object atomSetCollection, int atomSetIndex) {
    return ((AtomSetCollection) atomSetCollection)
        .getAtomSetAuxiliaryInfo(atomSetIndex);
  }

  /* **************************************************************
   * The frame related methods
   * **************************************************************/

  @Override
  public int getHydrogenAtomCount(Object atomSetCollection) {
    return ((AtomSetCollection)atomSetCollection).getHydrogenAtomCount();
  }
  
  @Override
  public String[][] getBondList(Object atomSetCollection) {
    return ((AtomSetCollection)atomSetCollection).getBondList();
  }


  @Override
  public int getAtomCount(Object atomSetCollection) {
    AtomSetCollection a = (AtomSetCollection)atomSetCollection; 
    return (a.bsAtoms == null ? a.getAtomCount() : a.bsAtoms.cardinality());
  }

  @Override
  public boolean coordinatesAreFractional(Object atomSetCollection) {
    return ((AtomSetCollection)atomSetCollection).coordinatesAreFractional;
  }

  @Override
  public float[] getNotionalUnitcell(Object atomSetCollection) {
    return ((AtomSetCollection)atomSetCollection).notionalUnitCell;
  }

  @Override
  public float[] getPdbScaleMatrix(Object atomSetCollection) {
    float[] a = ((AtomSetCollection)atomSetCollection).notionalUnitCell;
    if (a.length < 22)
      return null;
    float[] b = new float[16];
    for (int i = 0; i < 16; i++)
      b[i] = a[6 + i];
    return b;
  }

  @Override
  public float[] getPdbScaleTranslate(Object atomSetCollection) {
    float[] a = ((AtomSetCollection)atomSetCollection).notionalUnitCell;
    if (a.length < 22)
      return null;
    float[] b = new float[3];
    b[0] = a[6 + 4*0 + 3];
    b[1] = a[6 + 4*1 + 3];
    b[2] = a[6 + 4*2 + 3];
    return b;
  }
  
  ////////////////////////////////////////////////////////////////

  @Override
  public JmolAdapterAtomIterator
    getAtomIterator(Object atomSetCollection) {
    return new AtomIterator((AtomSetCollection)atomSetCollection);
  }

  @Override
  public JmolAdapterBondIterator
    getBondIterator(Object atomSetCollection) {
    return new BondIterator((AtomSetCollection)atomSetCollection);
  }

  @Override
  public JmolAdapterStructureIterator
    getStructureIterator(Object atomSetCollection) {
    return ((AtomSetCollection)atomSetCollection).getStructureCount() == 0 ? 
        null : new StructureIterator((AtomSetCollection)atomSetCollection);
  }

}
