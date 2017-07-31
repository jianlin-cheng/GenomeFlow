/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2006-10-15 17:34:01 -0500 (Sun, 15 Oct 2006) $
 * $Revision: 5957 $
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

package org.jmol.adapter.readers.cifpdb;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import org.jmol.adapter.smarter.Atom;
import org.jmol.adapter.smarter.AtomSetCollectionReader;
import org.jmol.adapter.smarter.Structure;
import org.jmol.api.Interface;
import org.jmol.api.JmolAdapter;
import org.jmol.api.SymmetryInterface;
import org.jmol.constant.EnumStructure;
import org.jmol.util.Escape;
import org.jmol.util.Logger;
import org.jmol.util.Quadric;
import org.jmol.util.TextFormat;


/**
 * GPDB file reader.
 *
 *<p>
 * <a href='http://www.rcsb.org'>
 * http://www.rcsb.org
 * </a>
 *
 * @author Miguel, Egon, and Bob (hansonr@stolaf.edu)
 * 
 * pqr and gromacs pdb_wide_format added by Bob 
 * see http://repo.or.cz/w/gromacs.git/blob/HEAD:/src/gmxlib/pdbio.c line 244
 * see http://repo.or.cz/w/gromacs.git/blob/HEAD:/src/gmxlib/pdbio.c line 323
 * 
 * symmetry added by Bob Hanson:
 * 
 *  setFractionalCoordinates()
 *  setSpaceGroupName()
 *  setUnitCell()
 *  initializeCartesianToFractional();
 *  setUnitCellItem()
 *  setAtomCoord()
 *  applySymmetryAndSetTrajectory()
 *  
 */

public class GpdbReader extends AtomSetCollectionReader {

  private int lineLength;

  private StringBuffer pdbHeader;

  private boolean applySymmetry;
  private boolean getTlsGroups;
  
  private boolean isMultiModel;  // MODEL ...
  private boolean haveMappedSerials;
  private boolean isConnectStateBug;
  private boolean isLegacyModelType;

  private boolean gromacsWideFormat;
  protected boolean isPQR;
  
  private final Map<String, Map<String, Boolean>> htFormul = new Hashtable<String, Map<String, Boolean>>();
  private Map<String, String> htHetero;
  private Map<String, Map<String, Object>> htSites;
  private Map<String, Boolean> htElementsInCurrentGroup;
  private Map<String, Map<String, String>> htMolIds;
  
  private List<Map<String, String>> vCompnds;
  private List<Matrix4f> vBiomts;
  private List<Map<String, Object>> vBiomolecules;
  private List<Map<String, Object>> vTlsModels;
  private StringBuffer sbTlsErrors;

  private int[] chainAtomCounts;  
  
  private StringBuffer sbIgnored, sbSelected, sbConect, sb;

  private int atomCount;
  private int maxSerial;
  private int nUNK;
  private int nRes;
  
  private Map<String, String> currentCompnd;
  private String currentGroup3;
  private String currentKey;
  private int currentResno = Integer.MIN_VALUE;
  private int configurationPtr = Integer.MIN_VALUE;
  private boolean resetKey = true;
  private String compnd = null;
  private int conformationIndex;
  private int fileAtomIndex;
  private char lastAltLoc;
  private String lastAtomData;
  private int lastAtomIndex;
  private int lastGroup = Integer.MIN_VALUE;
  private char lastInsertion;
  private int lastSourceSerial = Integer.MIN_VALUE;
  private int lastTargetSerial = Integer.MIN_VALUE;
  private int tlsGroupID;

  
  
  
  final private static String lineOptions = 
   "ATOM    " + //0
   "HETATM  " + //1
   "MODEL   " + //2
   "CONECT  " + //3
   "HELIX   " + //4,5,6
   "SHEET   " +
   "TURN    " +
   "HET     " + //7
   "HETNAM  " + //8
   "ANISOU  " + //9
   "SITE    " + //10
   "CRYST1  " + //11
   "SCALE1  " + //12,13,14
   "SCALE2  " +
   "SCALE3  " +
   "EXPDTA  " + //15
   "FORMUL  " + //16
   "REMARK  " + //17
   "HEADER  " + //18
   "COMPND  " + //19
   "SOURCE  " + //20
   "TITLE   ";  //21


 @SuppressWarnings("unchecked")
@Override
 protected void initializeReader() throws Exception {
   setIsPDB();
   pdbHeader = (getHeader ? new StringBuffer() : null);
   applySymmetry = !checkFilter("NOSYMMETRY");
   getTlsGroups = checkFilter("TLS");
   if (htParams.containsKey("vTlsModels")) {
     // from   load files "tls.out" "xxxx.pdb"
     vTlsModels = (List<Map<String, Object>>) htParams.remove("vTlsModels");
   }
   if (checkFilter("CONF ")) {
     configurationPtr = parseInt(filter, filter.indexOf("CONF ") + 5);
     sbIgnored = new StringBuffer();
     sbSelected = new StringBuffer();
   }
   isLegacyModelType = (stateScriptVersionInt < 120000);
   isConnectStateBug = (stateScriptVersionInt >= 120151 && stateScriptVersionInt <= 120220
         || stateScriptVersionInt >= 120300 && stateScriptVersionInt <= 120320);
 }

 
 //for first load
  int loadChrNum = 0;
  @Override
  protected boolean checkLine() throws Exception {
    int ptOption = ((lineLength = line.length()) < 6 ? -1 : lineOptions
        .indexOf(line.substring(0, 6))) >> 3;

    boolean isAtom = (ptOption == 0 || ptOption == 1);
    int serial = (isAtom ? parseInt(line, 6, 11) : 0);

    if (isMultiModel && !doProcessLines)
      return true;
    if (isAtom) {
      getHeader = false;
      atom(serial);
      return true;
    }
    switch (ptOption) {
    case 3:
      conect();
      return true;
    case 4:
    case 5:
    case 6:
      // if (line.startsWith("HELIX ") || line.startsWith("SHEET ")
      // || line.startsWith("TURN  ")) {
      structure();
      return true;
    case 7:
      het();
      return true;
    case 8:
      hetnam();
      return true;
    case 9:
      anisou();
      return true;
    case 10:
      site();
      return true;
    case 11:
      cryst1();
      return true;
    case 12:
    case 13:
    case 14:
      // if (line.startsWith("SCALE1")) {
      // if (line.startsWith("SCALE2")) {
      // if (line.startsWith("SCALE3")) {
      scale(ptOption - 11);
      return true;
    case 15:
      expdta();
      return true;
    case 16:
      formul();
      return true;
    case 17:
      if (line.startsWith("REMARK 310")) {
    	  //remark310();
    	  //return true;
      }
      if (getTlsGroups) {
        if (line.indexOf("TLS DETAILS") > 0)
          return remarkTls();
      }
      if (line.startsWith("REMARK 310")) {
    	  loadChrNum = remark310();
    	  return false;
      }
      checkLineForScript();
      return true;
    case 18:
      header();
      return true;
    case 19:
    case 20:
      compnd(ptOption == 20);
      return true;
    case 21:
      title();
      return true;
    }
    return true;
  }

  //for first load
  // Gromacs pdb_wide_format:
  //%-6s%5u %-4.4s %3.3s %c%4d%c   %10.5f%10.5f%10.5f%8.4f%8.4f    %2s\n")
  //0         1         2         3         4         5         6         7
  //01234567890123456789012345678901234567890123456789012345678901234567890123456789
  //aaaaaauuuuu ssss sss cnnnnc   xxxxxxxxxxyyyyyyyyyyzzzzzzzzzzccccccccrrrrrrrr
 
  private void atom(int serial) {
    Atom atom = new Atom();
    atom.atomName = line.substring(12, 16).trim();
    char ch = line.charAt(16);
    if (ch != ' ')
      atom.alternateLocationID = ch;
    atom.group3 = parseToken(line, 17, 20);
    ch = line.charAt(21);
    if (chainAtomCounts != null)
      chainAtomCounts[ch]++;
    atom.chainID = ch;
    atom.sequenceNumber = parseInt(line, 22, 26);
    atom.insertionCode = JmolAdapter.canonizeInsertionCode(line.charAt(26));
    atom.isHetero = line.startsWith("HETATM");
    atom.elementSymbol = deduceElementSymbol(atom.isHetero);
    if (!filterAtom(atom, fileAtomIndex++))
      return;
    atom.atomSerial = serial;
    if (serial > maxSerial)
      maxSerial = serial;
    if (atom.group3 == null) {
      if (currentGroup3 != null) {
        currentGroup3 = null;
        currentResno = Integer.MIN_VALUE;
        htElementsInCurrentGroup = null;
      }
    } else if (!atom.group3.equals(currentGroup3)
        || atom.sequenceNumber != currentResno) {
      currentGroup3 = atom.group3;
      currentResno = atom.sequenceNumber;
      htElementsInCurrentGroup = htFormul.get(atom.group3);
      nRes++;
      if (atom.group3.equals("UNK"))
        nUNK++;
    }

    if (gromacsWideFormat) {
      setAtomCoord(atom, parseFloat(line, 30, 40), parseFloat(line, 40, 50),
          parseFloat(line, 50, 60));
    } else {
      //calculate the charge from cols 79 & 80 (1-based): 2+, 3-, etc
      int charge = 0;
      if (lineLength >= 80) {
        char chMagnitude = line.charAt(78);
        char chSign = line.charAt(79);
        if (chSign >= '0' && chSign <= '7') {
          char chT = chSign;
          chSign = chMagnitude;
          chMagnitude = chT;
        }
        if ((chSign == '+' || chSign == '-' || chSign == ' ')
            && chMagnitude >= '0' && chMagnitude <= '7') {
          charge = chMagnitude - '0';
          if (chSign == '-')
            charge = -charge;
        }
      }
      atom.formalCharge = charge;
      setAtomCoord(atom, parseFloat(line, 30, 38), parseFloat(line, 38, 46),
          parseFloat(line, 46, 54));
    }
    setAdditionalAtomParameters(atom);
    if (haveMappedSerials)
      atomSetCollection.addAtomWithMappedSerialNumber(atom);
    else {
        //atomSetCollection.addAtom(atom);
        //for first load, still need to record the scales - chromosome, loci, fiber, nucleo
	  Pattern patternGss = Pattern.compile(".gs.pdb");
      Pattern patternCrs = Pattern.compile(".cs.pdb");
	  Pattern patternLcs = Pattern.compile(".ls.pdb");
	  Pattern patternFbs = Pattern.compile(".fs.pdb");
	  Pattern patternNus = Pattern.compile(".ns.pdb");
	  
	  Matcher gssMatcher = patternGss.matcher(fileName);
	  Matcher crsMatcher = patternCrs.matcher(fileName);
	  Matcher lcsMatcher = patternLcs.matcher(fileName);
	  Matcher fbsMatcher = patternFbs.matcher(fileName);	
	  Matcher nusMatcher = patternNus.matcher(fileName);
	  
	  if (crsMatcher.find()){
		  atom.chrScaleNumber = loadChrNum;
		  atom.lociScaleNumber = atom.sequenceNumber;		  
	  }
	  else if (lcsMatcher.find()){
		  atom.chrScaleNumber = loadChrNum;
		  atom.lociScaleNumber = atom.sequenceNumber;
		  atom.fiberScaleNumber = parseInt(line, 6, 11);
	  }
	  else if (fbsMatcher.find()){
		  atom.chrScaleNumber = loadChrNum;
		  atom.lociScaleNumber = atom.sequenceNumber;
		  //right now - do not record the fiber scale information for this scale
	  }
	  else if (nusMatcher.find()) {
	  }
	  atomSetCollection.addAtom(atom, true);
    }
    
    if (atomCount++ == 0)
      atomSetCollection.setAtomSetAuxiliaryInfo("isPDB", Boolean.TRUE);
    // note that values are +1 in this serial map
    if (atom.isHetero) {
      if (htHetero != null) {
        atomSetCollection.setAtomSetAuxiliaryInfo("hetNames", htHetero);
        htHetero = null;
      }
    }
  }


  private int[] selectedUnitInfo = new int[5];
  @Override
  //for scaledown and scaleup
  
  protected boolean checkLine(int currentSelectedScale, int currentSelectedUnit, int[] selectedPath, String downOrUp) throws Exception {
	int ptOption = ((lineLength = line.length()) < 6 ? -1 : lineOptions
	            .indexOf(line.substring(0, 6))) >> 3;
    switch (ptOption) {
    case 0:
    case 1:
    	atom(1, currentSelectedScale, currentSelectedUnit, selectedPath, downOrUp);
    return true;
    case 17:
      if (line.startsWith("REMARK 310")) {
    	  selectedUnitInfo = remark310(selectedPath, currentSelectedScale, downOrUp);
    	  return false;
      }
    }
    return true;
  }
  

  @Override
  protected void finalizeReader() throws Exception {
    checkNotPDB();
    atomSetCollection.connectAll(maxSerial, isConnectStateBug);
    if (vBiomolecules != null && vBiomolecules.size() > 0
        && atomSetCollection.getAtomCount() > 0) {
      atomSetCollection.setAtomSetAuxiliaryInfo("biomolecules", vBiomolecules);
      setBiomoleculeAtomCounts();
      if (vBiomts != null && applySymmetry) {
        atomSetCollection.applySymmetry(vBiomts, notionalUnitCell, applySymmetryToBonds, filter);
        vTlsModels = null; // for now, no TLS groups for biomolecules
      }
    }
    if (vTlsModels != null) {
      SymmetryInterface symmetry = (SymmetryInterface) Interface.getOptionInterface("symmetry.Symmetry");
      int n = atomSetCollection.getAtomSetCount();
      if (n == vTlsModels.size()) {
        for (int i = n; --i >= 0;)
          setTlsGroups(i, i, symmetry);
      } else {
        Logger.info(n + " models but " + vTlsModels.size() + " TLS descriptions");
        if (vTlsModels.size() == 1) {
          Logger.info(" -- assuming all models have the same TLS description -- check REMARK 3 for details.");
          for (int i = n; --i >= 0;)
            setTlsGroups(0, i, symmetry);
        }
      }
      checkForResidualBFactors(symmetry);
    }
    if (sbTlsErrors != null) {
      atomSetCollection.setAtomSetCollectionAuxiliaryInfo("tlsErrors", sbTlsErrors.toString());
      appendLoadNote(sbTlsErrors.toString());
    }
    
    super.finalizeReader();
    if (vCompnds != null)
      atomSetCollection.setAtomSetCollectionAuxiliaryInfo("compoundSource", vCompnds);
    if (htSites != null) { // && atomSetCollection.getAtomSetCount() == 1)
      addSites(htSites);
    }
    if (pdbHeader != null)
      atomSetCollection.setAtomSetCollectionAuxiliaryInfo("fileHeader",
          pdbHeader.toString());
    if (configurationPtr > 0) {
      Logger.info(sbSelected.toString());
      Logger.info(sbIgnored.toString());
    }
  }
  
  private void checkForResidualBFactors(SymmetryInterface symmetry) {
    Atom[] atoms = atomSetCollection.getAtoms();
    boolean isResidual = false;
     for (int i = atomSetCollection.getAtomCount(); --i >= 0;) {
      float[] anisou = tlsU.get(atoms[i]);
      if (anisou == null)
        continue;
      float resid = anisou[7] - (anisou[0] + anisou[1] + anisou[2])/3f;
      if (resid < 0 || Float.isNaN(resid)) {
        isResidual = true; // can't be total
        break;
      }
     }
     
     Logger.info("TLS analysis suggests Bfactors are " + (isResidual ? "" : "NOT") + " residuals");

     for (Map.Entry<Atom, float[]> entry : tlsU.entrySet()) {
       float[] anisou = entry.getValue();
       float resid = anisou[7];
       if (resid == 0)
         continue;
       if (!isResidual)
         resid -= (anisou[0] + anisou[1] + anisou[2])/3f;         
       anisou[0] += resid;
       anisou[1] += resid;
       anisou[2] += resid;
       entry.getKey().ellipsoid[1] = symmetry.getEllipsoid(anisou);
       
       // check for equal: 
       
       System.out.println("TLS-U:  " + Escape.escape(anisou));
       anisou = (entry.getKey().anisoBorU);
       if (anisou != null)
         System.out.println("ANISOU: " + Escape.escape(anisou));       
     }
     tlsU = null;
  }

  private void header() {
    if (lineLength < 8)
      return;
    appendLoadNote(line.substring(7).trim());
    if (lineLength >= 66)
      atomSetCollection.setCollectionName(line.substring(62, 66));
    if (lineLength > 50)
      line = line.substring(0, 50);
    atomSetCollection.setAtomSetCollectionAuxiliaryInfo("CLASSIFICATION", line.substring(7).trim());
  }

  private void title() {
    if (lineLength < 10)
      return;
    appendLoadNote(line.substring(10).trim());
  }
  
  private void compnd(boolean isSource) {
    if (!isSource) {
      if (compnd == null)
        compnd = "";
      else
        compnd += " ";
      String s = line;
      if (lineLength > 62)
        s = s.substring(0, 62);
      compnd += s.substring(10).trim();
      atomSetCollection.setAtomSetCollectionAuxiliaryInfo("COMPND", compnd);
    }
    if (vCompnds == null) {
      if (isSource)
        return;
      vCompnds = new ArrayList<Map<String,String>>();
      htMolIds = new Hashtable<String, Map<String,String>>();
      currentCompnd = new Hashtable<String, String>();
      currentCompnd.put("select", "(*)");
      currentKey = "MOLECULE";
      htMolIds.put("", currentCompnd);
    }
    if (isSource && resetKey) {
      resetKey = false;
      currentKey = "SOURCE";
      currentCompnd = htMolIds.get("");
    }
    line = line.substring(10, Math.min(lineLength, 72)).trim();
    int pt = line.indexOf(":");
    if (pt < 0 || pt > 0 && line.charAt(pt - 1) == '\\')
      pt = line.length();
    String key = line.substring(0, pt).trim();
    String value = (pt < line.length() ? line.substring(pt + 1) : null);
    if (key.equals("MOL_ID")) {
      if (value == null)
        return;
      if (isSource) {
        currentCompnd = htMolIds.remove(value);
        return;
      }
      currentCompnd = new Hashtable<String, String>();
      vCompnds.add(currentCompnd);
      htMolIds.put(value, currentCompnd);
    }
    if (currentCompnd == null)
      return;
    if (value == null) {
      value = currentCompnd.get(currentKey);
      if (value == null)
        value = "";
      value += key;
      if (vCompnds.size() == 0)
        vCompnds.add(currentCompnd);
    } else {
      currentKey = key;
    }
    if (value.endsWith(";"))
      value = value.substring(0, value.length() - 1);
    currentCompnd.put(currentKey, value);
    if (currentKey.equals("CHAIN"))
      currentCompnd.put("select", "(:"
          + TextFormat.simpleReplace(TextFormat
              .simpleReplace(value, ", ", ",:"), " ", "") + ")");
  }

  @SuppressWarnings("unchecked")
  private void setBiomoleculeAtomCounts() {
    for (int i = vBiomolecules.size(); --i >= 0;) {
      Map<String, Object> biomolecule = vBiomolecules.get(i);
      String chain = (String) biomolecule.get("chains");
      int nTransforms = ((List<Matrix4f>) biomolecule.get("biomts")).size();
      int nAtoms = 0;
      for (int j = chain.length() - 1; --j >= 0;)
        if (chain.charAt(j) == ':')
          nAtoms += chainAtomCounts[chain.charAt(j + 1)];
      biomolecule.put("atomCount", Integer.valueOf(nAtoms * nTransforms));
    }
  }

  //added -hcf
  //REMARK 310 - contains the structure information (scales transform)
  //for first load
  private int remark310() throws Exception {
	  int chrNum = 0;
	  while (true) {
	     if (line == null || !line.startsWith("REMARK 310"))
	         break; 
	     //for reading chromosome number
	     Pattern forChrNumPattern = Pattern.compile("(REMARK 310  CHR:)(\\d+)");
	     Matcher forChrNumMatcher = forChrNumPattern.matcher(line);
	     if (forChrNumMatcher.matches()) {
	    	 chrNum = Integer.parseInt(forChrNumMatcher.group(2));
	     }
	     readLine();
	  }
	  return chrNum;
  }
  
  
  //for scaleup/scaledown
  private int[] remark310(int[] selectedPath, int currentSelectedScale, String downOrUp) throws Exception {
	  int chrNum = 0;
	  
	  while (true) {
	     if (line == null || !line.startsWith("REMARK 310"))
	         break; 
	     //for reading chromosome number
	     Pattern forChrNumPattern = Pattern.compile("(REMARK 310  CHR:)(\\d+)");
	     Matcher forChrNumMatcher = forChrNumPattern.matcher(line);
	     if (forChrNumMatcher.matches()) {
	    	 chrNum = Integer.parseInt(forChrNumMatcher.group(2));
	    	 selectedUnitInfo[1] = chrNum;

	     }
	     //for reading structure information - only for fiber scale and nucleotide scale
	     //transform selectedPath into selectedUnitInfo
	     if(currentSelectedScale == 3 && downOrUp.equals("down")) {
	    	 int selectedChr = selectedPath[1];
	    	 int selectedLoci = selectedPath[2];
	    	 int selectedFiber = selectedPath[3];
	    	 
	    	 String patternLine = "(REMARK 310  FB\\()(" + selectedFiber +")(\\):)(" + selectedChr + ")(\\s)(" + selectedLoci + ")(\\s)(" + "\\d+" + ")(\\s)(" + "\\d+" + ")";
	    	 Pattern forSelectedUnitPattern = Pattern.compile(patternLine);
	    	 Matcher forSelectedUnitMatcher = forSelectedUnitPattern.matcher(line);
	    	 
	    	 if(selectedFiber != 0) {
	    		 if (forSelectedUnitMatcher.matches()) {
		    		 selectedUnitInfo[0] = selectedFiber;
		    		 selectedUnitInfo[1] = selectedChr;
		    		 selectedUnitInfo[2] = selectedLoci;
		    		 selectedUnitInfo[3] = Integer.parseInt(forSelectedUnitMatcher.group(8));
		    		 selectedUnitInfo[4] = Integer.parseInt(forSelectedUnitMatcher.group(10));
	    		 }
	    	 }
	    	 else if(selectedLoci != 0) {
	    		 selectedUnitInfo[0] = 0;
	    		 selectedUnitInfo[1] = selectedChr;
	    		 selectedUnitInfo[2] = selectedLoci;
	    		 selectedUnitInfo[3] = 0;
	    		 selectedUnitInfo[4] = 0;
	    	 }
	    	 else if (selectedChr != 0) {
	    		 selectedUnitInfo[0] = 0;
	    		 selectedUnitInfo[1] = selectedChr;
	    		 selectedUnitInfo[2] = 0;
	    		 selectedUnitInfo[3] = 0;
	    		 selectedUnitInfo[4] = 0;
	    	 }
	    	 else if (selectedChr == 0) {
	    		 selectedUnitInfo[0] = 0;
	    		 selectedUnitInfo[1] = 0;
	    		 selectedUnitInfo[2] = 0;
	    		 selectedUnitInfo[3] = 0;
	    		 selectedUnitInfo[4] = 0;
	    	 }
	     }
	     else if (currentSelectedScale == 5 && downOrUp.equals("up")) {
	    	 
	     }
	     
	     readLine();
	  }
	  
	  return selectedUnitInfo;
	  
  }
  
  
  //added end -hcf
  


  // Gromacs pdb_wide_format:
  //%-6s%5u %-4.4s %3.3s %c%4d%c   %10.5f%10.5f%10.5f%8.4f%8.4f    %2s\n")
  //0         1         2         3         4         5         6         7
  //01234567890123456789012345678901234567890123456789012345678901234567890123456789
  //aaaaaauuuuu ssss sss cnnnnc   xxxxxxxxxxyyyyyyyyyyzzzzzzzzzzccccccccrrrrrrrr
 
  private void atom(int serial, int currentSelectedScale, int currentSelectedUnit, int[] selectedPath, String downOrUp) {
    Atom atom = new Atom();
    atom.atomName = "C";
    
    char ch = line.charAt(16);
    if (ch != ' ')
      atom.alternateLocationID = ch;
    atom.group3 = parseToken(line, 17, 20);
    ch = line.charAt(21);
    if (chainAtomCounts != null)
      chainAtomCounts[ch]++;
    atom.chainID = ch;
    
    atom.sequenceNumber = parseInt(line, 22, 26);
    atom.insertionCode = JmolAdapter.canonizeInsertionCode(line.charAt(26));
    
    atom.atomSerial = serial;
    if (serial > maxSerial)
      maxSerial = serial;
    if (atom.group3 == null) {
      if (currentGroup3 != null) {
        currentGroup3 = null;
        currentResno = Integer.MIN_VALUE;
        htElementsInCurrentGroup = null;
      }
    } else if (!atom.group3.equals(currentGroup3)
        || atom.sequenceNumber != currentResno) {
      currentGroup3 = atom.group3;
      currentResno = atom.sequenceNumber;
      htElementsInCurrentGroup = htFormul.get(atom.group3);
      nRes++;
      if (atom.group3.equals("UNK"))
        nUNK++;
    }

    Float unitX = parseFloat(line, 30, 38); //x
    Float unitY = parseFloat(line, 38, 46); //y
    Float unitZ = parseFloat(line, 46, 54); //z

    
    if (!gromacsWideFormat) {
      if(downOrUp.equals("down")){
    	  setAtomCoord(atom, unitX, unitY, unitZ);
          setAdditionalAtomParameters(atom);
          if (haveMappedSerials)
              atomSetCollection.addAtomWithMappedSerialNumber(atom);
          else {
        	   //to record chromomsom, loci, fiber scale information, in the meantime, determine what units to load into atomSetCollection
        	    switch (currentSelectedScale) {
        	      case 1:
        	    	atom.chrScaleNumber = currentSelectedUnit;
        	    	atom.lociScaleNumber = atom.sequenceNumber;
      			    if (currentSelectedUnit == 0 || atom.chrScaleNumber == currentSelectedUnit) {
      			    	atomSetCollection.addAtom(atom, true);
    			    }        	    	
        	      break;
        	      case 2:
        	    	atom.chrScaleNumber = selectedUnitInfo[1];
        	    	atom.lociScaleNumber = atom.sequenceNumber;
        	    	atom.fiberScaleNumber = parseInt(line, 6, 11);
      		  	    if(selectedPath[2] != 0) {
      		  	  	  if (atom.chrScaleNumber == selectedPath[1] && atom.lociScaleNumber == selectedPath[2]) {
      		  	  		  atomSetCollection.addAtom(atom, true);
      		  		  }
      		  	    }
      		  	    else if (selectedPath[1] != 0) {
      		  		  if (atom.chrScaleNumber == selectedPath[1]) {
      		  			atomSetCollection.addAtom(atom, true);
      		  		  }
      		  	    }
      		  	    else if (selectedPath[1] == 0) {
      		  	    atomSetCollection.addAtom(atom, true);
      		  	    }        	    	
        	      break;
        	      case 3:
        	    	atom.chrScaleNumber = selectedUnitInfo[1];
        	    	atom.lociScaleNumber = atom.sequenceNumber;
        	    	//for fiberScaleNumber - need to be calculated
        	    	atom.nucleoScaleNumber = parseInt(line, 6, 11);
    		  	    int fromNucleo = selectedUnitInfo[3];
    		  	    int endNucleo = selectedUnitInfo[4];
    		  	    atom.fiberScaleNumber = selectedPath[3];
    		  	    
    		  	    if (selectedPath[3] != 0) {
  	    		  		if (atom.chrScaleNumber == selectedPath[1] && atom.lociScaleNumber == selectedPath[2] && (atom.nucleoScaleNumber >= fromNucleo && atom.nucleoScaleNumber <= endNucleo)) {
  	    		  		atomSetCollection.addAtom(atom, true);
  	    		  		}
    		  	    }
    		  	    else if (selectedPath[2] != 0) {
  	    		  		if (atom.chrScaleNumber == selectedPath[1] && atom.lociScaleNumber == selectedPath[2]) {
  	    		  		atomSetCollection.addAtom(atom, true);
  	    		  		}
    		  	    }
    		  	    else if (selectedPath[1] != 0) {
  	    		  		if (atom.chrScaleNumber == selectedPath[1]) {
  	    		  		atomSetCollection.addAtom(atom, true);
  	    		  		}
    		  	    }
    		  	    else if (selectedPath[0] == 0) {
    		  	    	atomSetCollection.addAtom(atom, true);
    		  	    }        	    	
        	      break;
        	      case 4:
        	    	atom.chrScaleNumber = selectedUnitInfo[1];
        	    	//TODO
        	      break;
        	      case 5:
        	       // atom.chrScaleNumber = chrNum;
        	       // TODO - hcf
        	       break;    	
        	    }
          }    
      }
      else if (downOrUp.equals("up")) {
    	  setAtomCoord(atom, unitX, unitY, unitZ);
          setAdditionalAtomParameters(atom);
          if (haveMappedSerials)
              atomSetCollection.addAtomWithMappedSerialNumber(atom);
          else {
         	//to record chromomsom, loci, fiber scale information, in the meantime, determine what units to load into atomSetCollection
      	    switch (currentSelectedScale) {
    	      case 5:
         	       // atom.chrScaleNumber = chrNum;
         	       // TODO - hcf
         	  break;        	    
    	      case 4:
    	    	atom.chrScaleNumber = selectedUnitInfo[1];
    	    	atom.lociScaleNumber = atom.sequenceNumber;
    	    	atom.fiberScaleNumber = parseInt(line, 6, 11);
				if (selectedPath[2] != 0) {
	    		  		if (atom.chrScaleNumber == selectedPath[1] && atom.lociScaleNumber == selectedPath[2]) {
	    		  			atomSetCollection.addAtom(atom, true);
	    		  		}
				}
    		  	else if (selectedPath[1] != 0) {
    		  		if (atom.chrScaleNumber == selectedPath[1]) {
    		  			atomSetCollection.addAtom(atom, true);
    		  		}
    		  	}
    		  	else if (selectedPath[1] == 0) {
    		  		atomSetCollection.addAtom(atom, true);
    		  	}    	    	
    	      break;	
      	      case 3:
      	    	atom.chrScaleNumber = selectedUnitInfo[1];
      	    	atom.lociScaleNumber = atom.sequenceNumber;
      	    	atomSetCollection.addAtom(atom, true);
      	      break;    	      
      	      case 2:
      	    	atom.chrScaleNumber = selectedUnitInfo[1];
      	    	atomSetCollection.addAtom(atom, true);
      	      break;
      	    }
	      }    
      }

    }
    
  }
  

  @Override
  protected boolean filterAtom(Atom atom, int iAtom) {
    if (!super.filterAtom(atom, iAtom))
      return false;
    if (configurationPtr > 0) {
      if (atom.sequenceNumber != lastGroup || atom.insertionCode != lastInsertion) {
        conformationIndex = configurationPtr - 1;
        lastGroup = atom.sequenceNumber;
        lastInsertion = atom.insertionCode;
        lastAltLoc = '\0';
      }
      // ignore atoms that have no designation
      if (atom.alternateLocationID != '\0') {
        // count down until we get the desired index into the list
        String msg = " atom [" + atom.group3 + "]"
                           + atom.sequenceNumber 
                           + (atom.insertionCode == '\0' ? "" : "^" + atom.insertionCode)
                           + (atom.chainID == '\0' ? "" : ":" + atom.chainID)
                           + "." + atom.atomName
                           + "%" + atom.alternateLocationID + "\n";
        if (conformationIndex >= 0 && atom.alternateLocationID != lastAltLoc) {
          lastAltLoc = atom.alternateLocationID;
          conformationIndex--;
        }
        if (conformationIndex < 0 && atom.alternateLocationID != lastAltLoc) {
          sbIgnored.append("ignoring").append(msg);
          return false;
        }
        sbSelected.append("loading").append(msg);
      }
    }
    return true;
  }

  /**
   * adaptable via subclassing
   * 
   * @param atom
   */
  protected void setAdditionalAtomParameters(Atom atom) {
    if (isPQR) {
      if (gromacsWideFormat) {
        atom.partialCharge = parseFloat(line.substring(60, 68));
        atom.radius = fixRadius(parseFloat(line.substring(68, 76)));
      } else {
        String[] tokens = getTokens();
        int pt = tokens.length - 2 - (line.length() > 75 ? 1 : 0);
        atom.partialCharge = parseFloat(tokens[pt++]);
        atom.radius = fixRadius(parseFloat(tokens[pt]));
      }
      return;
    }
    
    float floatOccupancy;
    
    if (gromacsWideFormat) {
      floatOccupancy = parseFloat(line.substring(60, 68));
      atom.bfactor = fixRadius(parseFloat(line.substring(68, 76)));
    } else {
      /****************************************************************
       * read the occupancy from cols 55-60 (1-based) 
       * --should be in the range 0.00 - 1.00
       ****************************************************************/
    
      floatOccupancy = parseFloat(line, 54, 60);

      /****************************************************************
       * read the bfactor from cols 61-66 (1-based)
       ****************************************************************/
        atom.bfactor = parseFloat(line, 60, 66);
    }
    
    atom.occupancy = (Float.isNaN(floatOccupancy) ? 100
        : (int) (floatOccupancy * 100));
    
  }

  /**
   * The problem here stems from the fact that developers have not fully 
   * understood the PDB specifications -- and that those have changed. 
   * The actual rules are as follows (using 1-based numbering:
   * 
   * 1) Chemical symbols may be in columns 77 and 78 for total disambiguity.
   * 2) Only valid chemical symbols should be in columns 13 and 14
   *    These are the first two characters of a four-character field.
   * 3) Four-character atom names for hydrogen necessarily start in 
   *    column 13, so when that is the case, if the four-letter 
   *    name starts with "H" then it is hydrogen regardless of what
   *    letter comes next. For example, "HG3 " is mercury (and should
   *    be in a HETATM record, not an ATOM record, anyway), but "HG33"
   *    is hydrogen, presumably.
   *    
   *    This leave open the ambiguity of a four-letter H name in a 
   *    heteroatom set where the symbol is really H, not Hg or Ha, or Ho or Hf, etc.
   *     
   * 
   * @param isHetero
   * @return           an atom symbol
   */
  protected String deduceElementSymbol(boolean isHetero) {
    if (lineLength >= 78) {
      char ch76 = line.charAt(76);
      char ch77 = line.charAt(77);
      if (ch76 == ' ' && Atom.isValidElementSymbol(ch77))
        return "" + ch77;
      if (Atom.isValidElementSymbolNoCaseSecondChar(ch76, ch77))
        return "" + ch76 + ch77;
    }
    char ch12 = line.charAt(12);
    char ch13 = line.charAt(13);
    // PDB atom symbols are supposed to be in these two characters
    // But they could be right-aligned or left-aligned
    if ((htElementsInCurrentGroup == null ||
         htElementsInCurrentGroup.get(line.substring(12, 14)) != null) &&
        Atom.isValidElementSymbolNoCaseSecondChar(ch12, ch13))
      return (isHetero || ch12 != 'H' ? "" + ch12 + ch13 : "H");
    // not a known two-letter code
    if (ch12 == 'H') // added check for PQR files "HD22" for example
      return "H";
    // check for " NZ" for example
    if ((htElementsInCurrentGroup == null ||
         htElementsInCurrentGroup.get("" + ch13) != null) &&
        Atom.isValidElementSymbol(ch13))
      return "" + ch13;
    // check for misplaced "O   " for example
    if (ch12 != ' ' && (htElementsInCurrentGroup == null ||
         htElementsInCurrentGroup.get("" + ch12) != null) &&
        Atom.isValidElementSymbol(ch12))
      return "" + ch12;
    // could be GLX or ASX;
    // probably a bad file. But we will make ONE MORE ATTEMPT
    // and read columns 14/15 instead of 12/13. What the heck!
    char ch14 = line.charAt(14);
    if (ch12 == ' ' && ch13 != 'X' && (htElementsInCurrentGroup == null ||
        htElementsInCurrentGroup.get(line.substring(13, 15)) != null) &&
        Atom.isValidElementSymbolNoCaseSecondChar(ch13, ch14))
     return  "" + ch13 + ch14;
    return "Xx";
  }
  
  private boolean haveDoubleBonds;
  
  private void conect() {
    // adapted for improper non-crossreferenced files such as 1W7R
    if (sbConect == null) {
      sbConect = new StringBuffer();
      sb = new StringBuffer();
    } else {
      sb.setLength(0);
    }
    int sourceSerial = -1;
    sourceSerial = parseInt(line, 6, 11);
    if (sourceSerial < 0)
      return;
    for (int i = 0; i < 9; i += (i == 5 ? 2 : 1)) {
      int offset = i * 5 + 11;
      int offsetEnd = offset + 5;
      int targetSerial = (offsetEnd <= lineLength ? parseInt(line, offset,
          offsetEnd) : -1);
      if (targetSerial < 0)
        continue;
      boolean isDoubleBond = (sourceSerial == lastSourceSerial && targetSerial == lastTargetSerial);
      if (isDoubleBond)
        haveDoubleBonds = true;
      lastSourceSerial = sourceSerial;
      lastTargetSerial = targetSerial;
      boolean isSwapped = (targetSerial < sourceSerial);
      int i1;
      if (isSwapped) {
        i1 = targetSerial;
        targetSerial = sourceSerial;
      } else {
        i1 = sourceSerial;
      }
      String st = ";" + i1 + " " + targetSerial + ";";
      if (sbConect.indexOf(st) >= 0 && !isDoubleBond)
        continue;
      // check for previous double
      if (haveDoubleBonds) {
        String st1 = "--" + st;
        if (sbConect.indexOf(st1) >= 0)
          continue;
        sbConect.append(st);
        sb.append(st1);
      } else {
        sbConect.append(st);
      }
      atomSetCollection.addConnection(new int[] { i1, targetSerial,
          i < 4 ? 1 : JmolAdapter.ORDER_HBOND });
    }
    sbConect.append(sb);
  }

  /*
          1         2         3
  0123456789012345678901234567890123456
  HELIX    1  H1 ILE      7  LEU     18
  HELIX    2  H2 PRO     19  PRO     19
  HELIX    3  H3 GLU     23  TYR     29
  HELIX    4  H4 THR     30  THR     30
  SHEET    1  S1 2 THR     2  CYS     4
  SHEET    2  S2 2 CYS    32  ILE    35
  SHEET    3  S3 2 THR    39  PRO    41
  TURN     1  T1 GLY    42  TYR    44

  HELIX     1 H1 ILE A    7  PRO A   19
  HELIX     2 H2 GLU A   23  THR A   30
  SHEET     1 S1 0 CYS A   3  CYS A   4
  SHEET     2 S2 0 CYS A  32  ILE A  35

  HELIX  113 113 ASN H  307  ARG H  327  1                                  21    
  SHEET    1   A 6 ASP A  77  HIS A  80  0                                        
  SHEET    2   A 6 GLU A  47  ILE A  51  1  N  ILE A  48   O  ASP A  77           
  SHEET    3   A 6 ARG A  22  ILE A  26  1  N  VAL A  23   O  GLU A  47           


TYPE OF HELIX CLASS NUMBER (COLUMNS 39 - 40)
--------------------------------------------------------------
Right-handed alpha (default) 1
Right-handed omega 2
Right-handed pi 3
Right-handed gamma 4
Right-handed 310 5
Left-handed alpha 6
Left-handed omega 7
Left-handed gamma 8
27 ribbon/helix 9
Polyproline 10

   */
  
  private void structure() {
    EnumStructure structureType = EnumStructure.NONE;
    EnumStructure substructureType = EnumStructure.NONE;
    int startChainIDIndex;
    int startIndex;
    int endChainIDIndex;
    int endIndex;
    int strandCount = 0;
    if (line.startsWith("HELIX ")) {
      structureType = EnumStructure.HELIX;
      startChainIDIndex = 19;
      startIndex = 21;
      endChainIDIndex = 31;
      endIndex = 33;
      if (line.length() >= 40)
      substructureType = Structure.getHelixType(parseInt(line.substring(38, 40)));
    } else if (line.startsWith("SHEET ")) {
      structureType = EnumStructure.SHEET;
      startChainIDIndex = 21;
      startIndex = 22;
      endChainIDIndex = 32;
      endIndex = 33;
      strandCount = parseInt(line.substring(14, 16));
    } else if (line.startsWith("TURN  ")) {
      structureType = EnumStructure.TURN;
      startChainIDIndex = 19;
      startIndex = 20;
      endChainIDIndex = 30;
      endIndex = 31;
    } else
      return;

    if (lineLength < endIndex + 4)
      return;

    String structureID = line.substring(11, 15).trim();
    int serialID = parseInt(line.substring(7, 10));
    char startChainID = line.charAt(startChainIDIndex);
    int startSequenceNumber = parseInt(line, startIndex, startIndex + 4);
    char startInsertionCode = line.charAt(startIndex + 4);
    char endChainID = line.charAt(endChainIDIndex);
    int endSequenceNumber = parseInt(line, endIndex, endIndex + 4);
    // some files are chopped to remove trailing whitespace
    char endInsertionCode = ' ';
    if (lineLength > endIndex + 4)
      endInsertionCode = line.charAt(endIndex + 4);

    // this should probably call Structure.validateAndAllocate
    // in order to check validity of parameters
    // model number set to -1 here to indicate ALL MODELS
    if (substructureType == EnumStructure.NONE)
      substructureType = structureType;
    Structure structure = new Structure(-1, structureType, substructureType,
        structureID, serialID, strandCount, startChainID, startSequenceNumber,
        startInsertionCode, endChainID, endSequenceNumber, endInsertionCode);
    atomSetCollection.addStructure(structure);
  }

  private int getModelNumber() {
    int startModelColumn = 6; // should be 10 0-based
    int endModelColumn = 14;
    if (endModelColumn > lineLength)
      endModelColumn = lineLength;
    int iModel = parseInt(line, startModelColumn, endModelColumn);
    return (iModel == Integer.MIN_VALUE ? 0 : iModel);
  }
  
  private void model(int modelNumber) {
    /****************************************************************
     * mth 2004 02 28
     * note that the pdb spec says:
     * COLUMNS       DATA TYPE      FIELD         DEFINITION
     * ----------------------------------------------------------------------
     *  1 -  6       Record name    "MODEL "
     * 11 - 14       Integer        serial        Model serial number.
     *
     * but I received a file with the serial
     * number right after the word MODEL :-(
     ****************************************************************/
    checkNotPDB();
    haveMappedSerials = false;
    sbConect = null;
    atomSetCollection.newAtomSet();
    atomSetCollection.setAtomSetAuxiliaryInfo("isPDB", Boolean.TRUE);
    atomSetCollection.setAtomSetNumber(modelNumber);
  }

  private void checkNotPDB() {
    boolean isPDB = (nRes == 0 || nUNK != nRes);
    // This speeds up calculation, because no crosschecking
    // No special-position atoms in mmCIF files, because there will
    // be no center of symmetry, no rotation-inversions, 
    // no atom-centered rotation axes, and no mirror or glide planes. 
    atomSetCollection.setCheckSpecial(!isPDB);
    atomSetCollection.setAtomSetAuxiliaryInfo("isPDB", isPDB ? Boolean.TRUE : Boolean.FALSE);
    nUNK = nRes = 0;
    currentGroup3 = null;
  }

  private void cryst1() throws Exception {
    float a = getFloat(6, 9);
    if (a == 1)
      a = Float.NaN; // 1 for a means no unit cell
    setUnitCell(a, getFloat(15, 9), getFloat(24, 9), getFloat(33,
        7), getFloat(40, 7), getFloat(47, 7));
    setSpaceGroupName(parseTrimmed(line, 55, 66));
  }

  private float getFloat(int ich, int cch) throws Exception {
    return parseFloat(line, ich, ich+cch);
  }

  private void scale(int n) throws Exception {
    int pt = n * 4 + 2;
    setUnitCellItem(pt++,getFloat(10, 10));
    setUnitCellItem(pt++,getFloat(20, 10));
    setUnitCellItem(pt++,getFloat(30, 10));
    setUnitCellItem(pt++,getFloat(45, 10));
  }

  private void expdta() {
    if (line.toUpperCase().indexOf("NMR") >= 0)
      atomSetCollection.setAtomSetCollectionAuxiliaryInfo("isNMRdata", "true");
  }

  private void formul() {
    String groupName = parseToken(line, 12, 15);
    String formula = parseTrimmed(line, 19, 70);
    int ichLeftParen = formula.indexOf('(');
    if (ichLeftParen >= 0) {
      int ichRightParen = formula.indexOf(')');
      if (ichRightParen < 0 || ichLeftParen >= ichRightParen ||
          ichLeftParen + 1 == ichRightParen ) // pick up () case in 1SOM.pdb
        return; // invalid formula;
      formula = parseTrimmed(formula, ichLeftParen + 1, ichRightParen);
    }
    Map<String, Boolean> htElementsInGroup = htFormul.get(groupName);
    if (htElementsInGroup == null)
      htFormul.put(groupName, htElementsInGroup = new Hashtable<String, Boolean>());
    // now, look for atom names in the formula
    next[0] = 0;
    String elementWithCount;
    while ((elementWithCount = parseTokenNext(formula)) != null) {
      if (elementWithCount.length() < 2)
        continue;
      char chFirst = elementWithCount.charAt(0);
      char chSecond = elementWithCount.charAt(1);
      if (Atom.isValidElementSymbolNoCaseSecondChar(chFirst, chSecond))
        htElementsInGroup.put("" + chFirst + chSecond, Boolean.TRUE);
      else if (Atom.isValidElementSymbol(chFirst))
        htElementsInGroup.put("" + chFirst, Boolean.TRUE);
    }
  }
  
  private void het() {
    if (line.length() < 30) {
      return;
    }
    if (htHetero == null) {
      htHetero = new Hashtable<String, String>();
    }
    String groupName = parseToken(line, 7, 10);
    if (htHetero.containsKey(groupName)) {
      return;
    }
    String hetName = parseTrimmed(line, 30, 70);
    htHetero.put(groupName, hetName);
  }
  
  private void hetnam() {
    if (htHetero == null) {
      htHetero = new Hashtable<String, String>();
    }
    String groupName = parseToken(line, 11, 14);
    String hetName = parseTrimmed(line, 15, 70);
    if (groupName == null) {
      Logger.error("ERROR: HETNAM record does not contain a group name: " + line);
      return;
    }
    String htName = htHetero.get(groupName);
    if (htName != null) {
      hetName = htName + hetName;
    }
    htHetero.put(groupName, hetName);
    //Logger.debug("hetero: "+groupName+" "+hetName);
  }
  
  /*
 The ANISOU records present the anisotropic temperature factors.

Record Format

COLUMNS        DATA TYPE       FIELD         DEFINITION                  
----------------------------------------------------------------------
 1 -  6        Record name     "ANISOU"                                  

 7 - 11        Integer         serial        Atom serial number.         

13 - 16        Atom            name          Atom name.                  

17             Character       altLoc        Alternate location indicator.                  

18 - 20        Residue name    resName       Residue name.               

22             Character       chainID       Chain identifier.           

23 - 26        Integer         resSeq        Residue sequence number.    

27             AChar           iCode         Insertion code.             

29 - 35        Integer         u[0][0]       U(1,1)                

36 - 42        Integer         u[1][1]       U(2,2)                

43 - 49        Integer         u[2][2]       U(3,3)                

50 - 56        Integer         u[0][1]       U(1,2)                

57 - 63        Integer         u[0][2]       U(1,3)                

64 - 70        Integer         u[1][2]       U(2,3)                

73 - 76        LString(4)      segID         Segment identifier, left-justified.

77 - 78        LString(2)      element       Element symbol, right-justified.

79 - 80        LString(2)      charge        Charge on the atom.       

Details

* Columns 7 - 27 and 73 - 80 are identical to the corresponding ATOM/HETATM record.

* The anisotropic temperature factors (columns 29 - 70) are scaled by a factor of 10**4 (Angstroms**2) and are presented as integers.

* The anisotropic temperature factors are stored in the same coordinate frame as the atomic coordinate records. 
   */
  
  private void anisou() {
    float[] data = new float[8];
    data[6] = 1; //U not B
    int serial = parseInt(line, 6, 11);
    int index;
    if (line.substring(6, 26).equals(lastAtomData)) {
      index = lastAtomIndex;
    } else {
      if (!haveMappedSerials)
        atomSetCollection.createAtomSerialMap();
      index = atomSetCollection.getAtomIndexFromSerial(serial);
      haveMappedSerials = true;
    }
    if (index < 0) {
      //normal when filtering
      //System.out.println("ERROR: ANISOU record does not correspond to known atom");
      return;
    }
    Atom atom = atomSetCollection.getAtom(index);
    for (int i = 28, pt = 0; i < 70; i += 7, pt++)
      data[pt] = parseFloat(line, i, i + 7);
    for (int i = 0; i < 6; i++) {
      if (Float.isNaN(data[i])) {
          Logger.error("Bad ANISOU record: " + line);
          return;
      }
      data[i] /= 10000f;
    }
    atomSetCollection.setAnisoBorU(atom, data, 12); // was 8 12.3.16
    // new type 12 - cartesian already
    // Ortep Type 0: D = 1, C = 2, Cartesian
    // Ortep Type 8: D = 2pi^2, C = 2, a*b*
    // Ortep Type 10: D = 2pi^2, C = 2, Cartesian
  }
  /*
   * http://www.wwpdb.org/documentation/format23/sect7.html
   * 
 Record Format

COLUMNS       DATA TYPE         FIELD            DEFINITION
------------------------------------------------------------------------
 1 -  6       Record name       "SITE    "
 8 - 10       Integer           seqNum      Sequence number.
12 - 14       LString(3)        siteID      Site name.
16 - 17       Integer           numRes      Number of residues comprising 
                                            site.

19 - 21       Residue name      resName1    Residue name for first residue
                                            comprising site.
23            Character         chainID1    Chain identifier for first residue
                                            comprising site.
24 - 27       Integer           seq1        Residue sequence number for first
                                            residue comprising site.
28            AChar             iCode1      Insertion code for first residue
                                            comprising site.
30 - 32       Residue name      resName2    Residue name for second residue
...
41 - 43       Residue name      resName3    Residue name for third residue
...
52 - 54       Residue name      resName4    Residue name for fourth residue
 
   */
  
  private void site() {
    if (htSites == null) {
      htSites = new Hashtable<String, Map<String, Object>>();
    }
    //int seqNum = parseInt(line, 7, 10);
    int nResidues = parseInt(line, 15, 17);
    String siteID = parseTrimmed(line, 11, 14);
    Map<String, Object> htSite = htSites.get(siteID);
    if (htSite == null) {
      htSite = new Hashtable<String, Object>();
      //htSite.put("seqNum", "site_" + seqNum);
      htSite.put("nResidues", Integer.valueOf(nResidues));
      htSite.put("groups", "");
      htSites.put(siteID, htSite);
    }
    String groups = (String)htSite.get("groups");
    for (int i = 0; i < 4; i++) {
      int pt = 18 + i * 11;
      String resName = parseTrimmed(line, pt, pt + 3);
      if (resName.length() == 0)
        break;
      String chainID = parseTrimmed(line, pt + 4, pt + 5);
      String seq = parseTrimmed(line, pt + 5, pt + 9);
      String iCode = parseTrimmed(line, pt + 9, pt + 10);
      groups += (groups.length() == 0 ? "" : ",") + "[" + resName + "]" + seq;
      if (iCode.length() > 0)
        groups += "^" + iCode;
      if (chainID.length() > 0)
        groups += ":" + chainID;
      htSite.put("groups", groups);
    }
  }

  /*
  REMARK   3  TLS DETAILS                                                         
  REMARK   3   NUMBER OF TLS GROUPS  : NULL
   or 
  REMARK   3  TLS DETAILS                                                         
  REMARK   3   NUMBER OF TLS GROUPS  : 20                                         
  REMARK   3                                                                      
  REMARK   3   TLS GROUP : 1                                                      
  REMARK   3    NUMBER OF COMPONENTS GROUP : 1                                    
  REMARK   3    COMPONENTS        C SSSEQI   TO  C SSSEQI                         
  REMARK   3    RESIDUE RANGE :   A     2        A     8                          
  REMARK   3    ORIGIN FOR THE GROUP (A):  17.3300  62.7550  29.2560              
  REMARK   3    T TENSOR                                                          
  REMARK   3      T11:   0.0798 T22:   0.0357                                     
  REMARK   3      T33:   0.0678 T12:   0.0530                                     
  REMARK   3      T13:  -0.0070 T23:   0.0011                                     
  REMARK   3    L TENSOR                                                          
  REMARK   3      L11:  13.1074 L22:   7.9735                                     
  REMARK   3      L33:   2.5703 L12:  -6.5507                                     
  REMARK   3      L13:  -1.5297 L23:   4.1172                                     
  REMARK   3    S TENSOR                                                          
  REMARK   3      S11:  -0.4246 S12:  -0.4216 S13:   0.1672                       
  REMARK   3      S21:   0.5307 S22:   0.3071 S23:   0.0385                       
  REMARK   3      S31:   0.0200 S32:  -0.2454 S33:   0.1174                       
  REMARK   3                                                                      
  REMARK   3   TLS GROUP : 2
   ...                                                      
   or (1zy8)
  REMARK   7                                                                      
  REMARK   7 TLS DEFINITIONS USED IN A FEW FINAL ROUNDS                           
  REMARK   7 OF REFINEMENT:                                                       
  REMARK   7 TLS DETAILS                                                          

   */
  private boolean remarkTls() throws Exception {
    int nGroups = 0;
    int iGroup = 0;
    String components = null;
    List<Map<String, Object>> tlsGroups = null;
    Map<String, Object> tlsGroup = null;
    List<Map<String, Object>> ranges = null;
    Map<String, Object> range = null;
    String remark = line.substring(0, 11);
    while (readLine() != null && line.startsWith(remark)) {
      try {
        String[] tokens = getTokens(line.substring(10).replace(':', ' '));
        if (tokens.length < 2)
          continue;
        Logger.info(line);
        if (tokens[1].equalsIgnoreCase("GROUP")) {
          tlsGroup = new Hashtable<String, Object>();
          ranges = new ArrayList<Map<String, Object>>();
          tlsGroup.put("ranges", ranges);
          tlsGroups.add(tlsGroup);
          tlsGroupID = parseInt(tokens[tokens.length - 1]);
          tlsGroup.put("id", Integer.valueOf(tlsGroupID));
        } else if (tokens[0].equalsIgnoreCase("NUMBER")) {
          if (tokens[2].equalsIgnoreCase("COMPONENTS")) {
            // ignore
          } else {
            nGroups = parseInt(tokens[tokens.length - 1]);
            if (nGroups < 1)
              break;
            if (vTlsModels == null)
              vTlsModels = new ArrayList<Map<String, Object>>();
            tlsGroups = new ArrayList<Map<String, Object>>();
            appendLoadNote(line.substring(11).trim());
          }
        } else if (tokens[0].equalsIgnoreCase("COMPONENTS")) {
          components = line;
        } else if (tokens[0].equalsIgnoreCase("RESIDUE")) {
          /*
          REMARK   3    RESIDUE RANGE :   A     2        A     8
          token 0  1      2       3   4   5     6        7     8
          */
          range = new Hashtable<String, Object>();
          char chain1, chain2;
          int res1, res2;
          if (tokens.length == 6) {
            chain1 = tokens[2].charAt(0);
            chain2 = tokens[4].charAt(0);
            res1 = parseInt(tokens[3]);
            res2 = parseInt(tokens[5]);
          } else {
            int toC = components.indexOf(" C ");
            int fromC = components.indexOf(" C ", toC + 4);
            chain1 = line.charAt(fromC);
            chain2 = line.charAt(toC);
            res1 = parseInt(line.substring(fromC + 1, toC));
            res2 = parseInt(line.substring(toC + 1));
          }
          if (chain1 == chain2) {
            range.put("chains", "" + chain1 + chain2);
            if (res1 <= res2) {
              range.put("residues", new int[] { res1, res2 });
              ranges.add(range);
            } else {
              tlsAddError(" TLS group residues are not in order (range ignored)");            
            }
          } else {
            tlsAddError(" TLS group chains are different (range ignored)");            
          }
        } else if (tokens[0].equalsIgnoreCase("SELECTION")) {
          /*
           * REMARK   3    SELECTION: RESID 513:544 OR RESID 568:634 OR RESID
           * 
           * REMARK   3    SELECTION: (CHAIN A AND RESID 343:667)                            
           */
          char chain = '\0';
          for (int i = 1; i < tokens.length; i++) {
            if (tokens[i].toUpperCase().indexOf("CHAIN") >= 0) {
              chain = tokens[++i].charAt(0);
              continue;
            }
            int resno = parseInt(tokens[i]);
            if (resno == Integer.MIN_VALUE)
              continue;
            range = new Hashtable<String, Object>();
            range.put("residues", new int[] { resno, parseInt(tokens[++i]) });
            if (chain != '\0')
              range.put("chains", "" + chain + chain);
            ranges.add(range);
          }
        } else if (tokens[0].equalsIgnoreCase("ORIGIN")) {
          /*
          REMARK   3    ORIGIN FOR THE GROUP (A):  17.3300  62.7550  29.2560              
          */
          /* 
           * Parse tightly packed numbers e.g. -999.1234-999.1234-999.1234
           * assuming there are 4 places to the right of each decimal point
           */
          Point3f origin = new Point3f();
          tlsGroup.put("origin", origin);
          if (tokens.length == 8) {
            origin.set(parseFloat(tokens[5]), parseFloat(tokens[6]),
                parseFloat(tokens[7]));
          } else {
            int n = line.length();
            origin.set(parseFloat(line.substring(n - 27, n - 18)),
                parseFloat(line.substring(n - 18, n - 9)), parseFloat(line
                    .substring(n - 9, n)));
          }
          if (Float.isNaN(origin.x) || Float.isNaN(origin.y) || Float.isNaN(origin.z)) {
            origin.set(Float.NaN, Float.NaN, Float.NaN);
            tlsAddError("invalid origin: " + line);
          }
        } else if (tokens[1].equalsIgnoreCase("TENSOR")) {
          /*
           * REMARK   3    T TENSOR                                                          
           * REMARK   3      T11:   0.0798 T22:   0.0357                                     
           * REMARK   3      T33:   0.0678 T12:   0.0530                                     
           * REMARK   3      T13:  -0.0070 T23:   0.0011                                     
           */
          char tensorType = tokens[0].charAt(0);
          String s = (readLine().substring(10)
              + readLine().substring(10) + readLine().substring(10)).replace(
                  tensorType, ' ').replace(':', ' ');
          //System.out.println("Tensor data = " + s);
          tokens = getTokens(s);
          float[][] tensor = new float[3][3];
          tlsGroup.put("t" + tensorType, tensor);
          for (int i = 0; i < tokens.length; i++) {
            int ti = tokens[i].charAt(0) - '1';
            int tj = tokens[i].charAt(1) - '1';
            tensor[ti][tj] = parseFloat(tokens[++i]);
            if (ti < tj)
              tensor[tj][ti] = tensor[ti][tj];
          }
          for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
              if (Float.isNaN(tensor[i][j])) {
                tlsAddError("invalid tensor: " + Escape.escapeArray(tensor));

              }
          //System.out.println("Tensor t" + tensorType + " = " + Escape.escape(tensor));
          if (tensorType == 'S' && ++iGroup == nGroups) {
            Logger.info(nGroups + " TLS groups read");
            readLine();
            break;
          }
        }
      } catch (Exception e) {
        Logger.error(line + "\nError in TLS parser: ");
        e.printStackTrace();
        tlsGroups = null;
        break;
      }
    }
    if (tlsGroups != null) {
      Hashtable<String, Object> groups = new Hashtable<String, Object>();
      groups.put("groupCount", Integer.valueOf(nGroups));
      groups.put("groups", tlsGroups);
      vTlsModels.add(groups);
    }
    return (nGroups < 1);
  }

  /**
   * for now, we just ignore TLS details if user has selected a specific model
   */
  private void handleTlsMissingModels() {
    vTlsModels = null;
  }

  /**
   * sets the atom property property_tlsGroup based on TLS group ranges
   * 
   * @param iGroup
   * @param iModel
   * @param symmetry 
   */
  @SuppressWarnings("unchecked")
  private void setTlsGroups(int iGroup, int iModel, SymmetryInterface symmetry) {
    Logger.info("TLS model " + (iModel + 1) + " set " + (iGroup + 1));
    Map<String, Object> tlsGroupInfo = vTlsModels.get(iGroup);
    List<Map<String, Object>> groups = (List<Map<String, Object>>) tlsGroupInfo
        .get("groups");
    int index0 = atomSetCollection.getAtomSetAtomIndex(iModel);
    int[] data = new int[atomSetCollection.getAtomSetAtomCount(iModel)];
    int indexMax = index0 + data.length;
    Atom[] atoms = atomSetCollection.getAtoms();
    int nGroups = groups.size();
    for (int i = 0; i < nGroups; i++) {
      Map<String, Object> group = groups.get(i);
      List<Map<String, Object>> ranges = (List<Map<String, Object>>) group
          .get("ranges");
      tlsGroupID = ((Integer) group.get("id")).intValue();
      for (int j = ranges.size(); --j >= 0;) {
        String chains = (String) ranges.get(j).get("chains");
        int[] residues = (int[]) ranges.get(j).get("residues");
        char chain0 = chains.charAt(0);
        char chain1 = chains.charAt(1);
        int res0 = residues[0];
        int res1 = residues[1];
        int index1 = findAtomForRange(index0, indexMax, chain0, res0, false);
        int index2 = (index1 >= 0 ? findAtomForRange(index1, indexMax, chain1,
            res1, false) : -1);
        if (index2 < 0) {
          Logger.info("TLS processing terminated");
          return;
        }
        Logger.info("TLS ID=" + tlsGroupID + " model atom index range "
            + index1 + "-" + index2);
        boolean isSameChain = (chain0 == chain1);  // will be true
        // could demand a contiguous section here for each range.
        for (int iAtom = index0; iAtom < indexMax; iAtom++) {
          Atom atom = atoms[iAtom];
          if (isSameChain ? atom.sequenceNumber >= res0 && atom.sequenceNumber <= res1
            : atom.chainID > chain0 && atom.chainID < chain1 
              || atom.chainID == chain0 && atom.sequenceNumber >= res0
              || atom.chainID == chain1 && atom.sequenceNumber <= res1
          ) {
              data[iAtom - index0] = tlsGroupID;
              setTlsEllipsoid(atom, group, symmetry);
            }
        }
      }
    }
    StringBuffer sdata = new StringBuffer();
    for (int i = 0; i < data.length; i++)
      sdata.append(data[i]).append('\n');
    atomSetCollection.setAtomSetAtomProperty("tlsGroup", sdata.toString(),
        iModel);
    atomSetCollection.setAtomSetAuxiliaryInfo("TLS", tlsGroupInfo, iModel);
    atomSetCollection.setEllipsoids();
  }

  private int findAtomForRange(int atom1, int atom2, char chain, int resno,
                          boolean isLast) {
    int iAtom = findAtom(atom1, atom2, chain, resno, true);
    return (isLast && iAtom >= 0 ? findAtom(iAtom, atom2, chain, resno, false) : iAtom);
  }

  private int findAtom(int atom1, int atom2, char chain, int resno, boolean isTrue) {
    Atom[] atoms = atomSetCollection.getAtoms();
    for (int i = atom1; i < atom2; i++) {
     Atom atom = atoms[i];
     if ((atom.chainID == chain && atom.sequenceNumber == resno) == isTrue)
       return i;
    }
    if (isTrue) {
      Logger.warn("PdbReader findAtom chain=" + chain + " resno=" + resno + " not found");
      tlsAddError("atom not found: chain=" + chain + " resno=" + resno);
    }
    return (isTrue ? -1 : atom2);
  }

  private final float[] dataT = new float[8];

  private static final float RAD_PER_DEG = (float) (Math.PI / 180);
  private static final float _8PI2_ = (float) (8 * Math.PI * Math.PI);
  private Map<Atom, float[]>tlsU;
  
  private void setTlsEllipsoid(Atom atom, Map<String, Object> group, SymmetryInterface symmetry) {
    Point3f origin = (Point3f) group.get("origin");
    if (Float.isNaN(origin.x))
      return;
    
    float[][] T = (float[][]) group.get("tT");
    float[][] L = (float[][]) group.get("tL");
    float[][] S = (float[][]) group.get("tS");

    if (T == null || L == null || S == null)
      return;

    // just factor degrees-to-radians into x, y, and z rather
    // than create all new matrices

    float x = (atom.x - origin.x) * RAD_PER_DEG;
    float y = (atom.y - origin.y) * RAD_PER_DEG;
    float z = (atom.z - origin.z) * RAD_PER_DEG;

    float xx = x * x;
    float yy = y * y;
    float zz = z * z;
    float xy = x * y;
    float xz = x * z;
    float yz = y * z;

    /*
     * 
     * from pymmlib-1.2.0.tar|mmLib/TLS.py
     * 
     */

    dataT[0] = T[0][0];
    dataT[1] = T[1][1];
    dataT[2] = T[2][2];
    dataT[3] = T[0][1];
    dataT[4] = T[0][2];
    dataT[5] = T[1][2];
    dataT[6] = 12; // (non)ORTEP type 12 -- macromolecular Cartesian

    float[] anisou = new float[8];

    float bresidual = (Float.isNaN(atom.bfactor) ? 0 : atom.bfactor / _8PI2_);

    anisou[0] /*u11*/= dataT[0] + L[1][1] * zz + L[2][2] * yy - 2 * L[1][2]
        * yz + 2 * S[1][0] * z - 2 * S[2][0] * y;
    anisou[1] /*u22*/= dataT[1] + L[0][0] * zz + L[2][2] * xx - 2 * L[2][0]
        * xz - 2 * S[0][1] * z + 2 * S[2][1] * x;
    anisou[2] /*u33*/= dataT[2] + L[0][0] * yy + L[1][1] * xx - 2 * L[0][1]
        * xy - 2 * S[1][2] * x + 2 * S[0][2] * y;
    anisou[3] /*u12*/= dataT[3] - L[2][2] * xy + L[1][2] * xz + L[2][0] * yz
        - L[0][1] * zz - S[0][0] * z + S[1][1] * z + S[2][0] * x - S[2][1] * y;
    anisou[4] /*u13*/= dataT[4] - L[1][1] * xz + L[1][2] * xy - L[2][0] * yy
        + L[0][1] * yz + S[0][0] * y - S[2][2] * y + S[1][2] * z - S[1][0] * x;
    anisou[5] /*u23*/= dataT[5] - L[0][0] * yz - L[1][2] * xx + L[2][0] * xy
        + L[0][1] * xz - S[1][1] * x + S[2][2] * x + S[0][1] * y - S[0][2] * z;
    anisou[6] = 12; // macromolecular Cartesian
    anisou[7] = bresidual;
    if (Float.isNaN(bresidual))
      System.out.println("hmm");
    
    if (tlsU == null)
      tlsU = new Hashtable<Atom, float[]>();
     tlsU.put(atom, anisou);

    // symmetry is set to [1 1 1 90 90 90] -- Cartesians, not actual unit cell

    atom.ellipsoid = new Quadric[] { null, null, symmetry.getEllipsoid(dataT) };
    //if (atom.atomIndex == 0)
      //System.out.println("pdbreader ellip 0 = " + atom.ellipsoid[1]); 
  }

  private void tlsAddError(String error) {
    if (sbTlsErrors == null)
      sbTlsErrors = new StringBuffer();
    sbTlsErrors.append(fileName).append('\t').append("TLS group ").append(
        tlsGroupID).append('\t').append(error).append('\n');
  }

  protected static float fixRadius(float r) {    
    return (r < 0.9f ? 1 : r);
    // based on parameters in http://pdb2pqr.svn.sourceforge.net/viewvc/pdb2pqr/trunk/pdb2pqr/dat/
    // AMBER forcefield, H atoms may be given 0 (on O) or 0.6 (on N) for radius
    // PARSE forcefield, lots of H atoms may be given 0 radius
    // CHARMM forcefield, HN atoms may be given 0.2245 radius
    // PEOEPB forcefield, no atoms given 0 radius
    // SWANSON forcefield, HW (on water) will be given 0 radius, and H on oxygen given 0.9170
  }

}

