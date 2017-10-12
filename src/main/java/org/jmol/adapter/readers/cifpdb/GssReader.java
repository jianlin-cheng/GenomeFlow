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

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.vecmath.Matrix4f;

import org.jmol.adapter.smarter.Atom;
import org.jmol.adapter.smarter.AtomSetCollectionReader;
import org.jmol.api.Interface;
import org.jmol.api.SymmetryInterface;
import org.jmol.util.Escape;
import org.jmol.util.Logger;

/**
 * PDB file reader.
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

public class GssReader extends AtomSetCollectionReader {

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
  
  private StringBuffer sbIgnored, sbSelected;

  private int atomCount;
  private int maxSerial;
  private int nUNK;
  private int nRes;

  private String currentGroup3;
  private int currentResno = Integer.MIN_VALUE;
  private int configurationPtr = Integer.MIN_VALUE;
  private int conformationIndex;
  private int fileAtomIndex;
  private char lastAltLoc;
  private int lastGroup = Integer.MIN_VALUE;
  private char lastInsertion;
  private int tlsGroupID;

  
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

  @Override
  //for scaledown and scaleup
  protected boolean checkLine(int currentSelectedScale, int currentSelectedUnit, int[] selectedPath, String downOrUp) throws Exception {
    atom(1, currentSelectedScale, currentSelectedUnit, selectedPath, downOrUp);
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

  //modified -hcf
  private int globScaleNum = 0;
  private int fiberScaleNum = 0;
  private int lociScaleNum = 0;
  private int chrScaleNum = 0;
  private int nucleoScaleNum = 0;
  private int sequenceLength;
  private int sequenceNum = 0; private float unitX; private float unitY; private float unitZ; private float unitRadius = 0; 
  private int countLoci = 0;
  private int chrID = 0;
  private int fromPos = 0;
  private int endPos = 0;
  private String spName = "";
  private String ensChr = "";
  private String lcChr = "";

  private void atom(int serial, int currentSelectedScale, int currentSelectedUnit, int[] selectedPath, String downOrUp) {
	  Atom atom = new Atom();
	  atom.atomName = "C";
	  
	  if (!gromacsWideFormat) {
	    if (downOrUp.equals("none")) {
	    	  Pattern patternGlobScaleLine = Pattern.compile("(^<gs>)(\\d+)");
		      Pattern patternFiberScaleLine = Pattern.compile("(^<fs>)(\\d+)");
		      Pattern patternLociScaleLine = Pattern.compile("(^<ls>)(\\d+)");
		      Pattern patternChrScaleLine = Pattern.compile("(^<cs>)(\\d+)");
		      Pattern patternLengthLine = Pattern.compile("(^<lt>)(\\d+)(</lt>)");
		      Pattern patternNucleoLineSimp = Pattern.compile("(^<un\\s)(\\d+)(>)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(</un>)");
		      Pattern patternNucleoLineWseq = Pattern.compile("(^<un\\s)(\\d+)(>)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(</un><seq>)([\\d]+)(\\s)([\\d]+)(</seq>)");
		      Pattern patternGlobalChrLineWseq = Pattern.compile("(^<un\\s)(\\d+)(>)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(</un><seq>)([\\d]+)(\\s)([\\d]+)(</seq><ens-chr>)([A-Za-z0-9]+)(</ens-chr>)(<lc-seq>)(.*)(</lc-seq>)");
		      Pattern patternSpLine = Pattern.compile("(<sp>)(.*)(</sp>)");
		      Pattern patternEnsLine = Pattern.compile("(<ens-chr>)([A-Za-z0-9]+)(</ens-chr>)");
		      Pattern patternLcLine = Pattern.compile("(<lc-seq>)(.*)(</lc-seq>)");
		      
		      Matcher matcherGls = patternGlobScaleLine.matcher(line);
		      Matcher matcherFbs = patternFiberScaleLine.matcher(line);
		      Matcher matcherLcs = patternLociScaleLine.matcher(line);
		      Matcher matcherChs = patternChrScaleLine.matcher(line);
		      Matcher matcherLt = patternLengthLine.matcher(line);
		      Matcher matcherNulSimp = patternNucleoLineSimp.matcher(line);
		      Matcher matcherNulWseq = patternNucleoLineWseq.matcher(line);
		      Matcher MatcherGlobalChrLineWseq = patternGlobalChrLineWseq.matcher(line);
		      Matcher matcherSpLine = patternSpLine.matcher(line);
		      Matcher matcherEnsLine = patternEnsLine.matcher(line);
		      Matcher matcherLcLine = patternLcLine.matcher(line);
		      
		      if (matcherGls.find()){
				    globScaleNum = Integer.parseInt(matcherGls.group(2));
			  }
		      else if (matcherFbs.find()){
				    fiberScaleNum = Integer.parseInt(matcherFbs.group(2));  
			  }
			  else if (matcherLcs.find()){
					lociScaleNum = Integer.parseInt(matcherLcs.group(2));
			  }
			  else if (matcherChs.find()) {
				    chrScaleNum = Integer.parseInt(matcherChs.group(2));
			  }
			  else if (matcherLt.matches()){
					sequenceLength = Integer.parseInt(matcherLt.group(2));
			  }
			  else if (matcherNulSimp.matches()){
					sequenceNum = Integer.parseInt(matcherNulSimp.group(2));
					unitX = Float.parseFloat(matcherNulSimp.group(4));
					unitY = Float.parseFloat(matcherNulSimp.group(6));
					unitZ = Float.parseFloat(matcherNulSimp.group(8));
					unitRadius = Float.parseFloat(matcherNulSimp.group(10));
			  }
			  else if (matcherNulWseq.matches()) {
					sequenceNum = Integer.parseInt(matcherNulWseq.group(2));
					unitX = Float.parseFloat(matcherNulWseq.group(4));
					unitY = Float.parseFloat(matcherNulWseq.group(6));
					unitZ = Float.parseFloat(matcherNulWseq.group(8));
					unitRadius = Float.parseFloat(matcherNulWseq.group(10));
					fromPos = Integer.parseInt(matcherNulWseq.group(12));
					endPos = Integer.parseInt(matcherNulWseq.group(14));
			  }
			  else if (MatcherGlobalChrLineWseq.matches()) {
					sequenceNum = Integer.parseInt(MatcherGlobalChrLineWseq.group(2));
					unitX = Float.parseFloat(MatcherGlobalChrLineWseq.group(4));
					unitY = Float.parseFloat(MatcherGlobalChrLineWseq.group(6));
					unitZ = Float.parseFloat(MatcherGlobalChrLineWseq.group(8));
					unitRadius = Float.parseFloat(MatcherGlobalChrLineWseq.group(10));
					fromPos = Integer.parseInt(MatcherGlobalChrLineWseq.group(12));
					endPos = Integer.parseInt(MatcherGlobalChrLineWseq.group(14));
					ensChr = MatcherGlobalChrLineWseq.group(16);
					lcChr = MatcherGlobalChrLineWseq.group(19);
			  }
			  else if (matcherSpLine.matches()) {
				    spName = matcherSpLine.group(2);
			  }
			  else if (matcherEnsLine.matches()) {
				    ensChr = matcherEnsLine.group(2);
				    
				    //Tuan added
				    if (ensChr.equalsIgnoreCase("X")){
				    	chrID = 23;
				    }else if (ensChr.equalsIgnoreCase("Y")){
				    	chrID = 24;
				    }else{
				    	chrID = Integer.parseInt(ensChr);
				    }
				    //End
				    
			  }
			  else if (matcherLcLine.matches()) {
				    lcChr = matcherLcLine.group(2);
			  }
		  
		      if (unitRadius != 0){
		    	  setAtomCoord(atom, unitX, unitY, unitZ);
		    	  atom.globScaleNumber = globScaleNum;
		    	  atom.fiberScaleNumber = fiberScaleNum;
		    	  atom.nucleoScaleNumber = nucleoScaleNum;
		    	  atom.lociScaleNumber = lociScaleNum;
		    	  atom.chrScaleNumber = chrScaleNum;
		    	  atom.sequenceLength = sequenceLength;
		    	  atom.chrID = chrID;
		    	  //Tuan added
		    	  atom.chainID = (char)(chrID - 1 + 'A');
		    	  //End
		    	  atom.fromPos = fromPos;
		    	  atom.endPos = endPos;
		    	  atom.spName = spName;
		    	  atom.ensChr = ensChr;
		    	  atom.lcChr = lcChr;
	  	    	  //sequenceNum => chr/loci/fiber/nucleoNumber
	  	    	  switch (currentSelectedScale) {
	  	    	    case 1:
	  	    		  atom.chrScaleNumber = sequenceNum;
	  	    		break;
	  	    	    case 2:
	  	    	      atom.lociScaleNumber = sequenceNum;
	  	    	    break;
	  	    	    case 3:
	  	    	      atom.fiberScaleNumber = sequenceNum;
	  	    	    break;
	  	    	    case 4:
	  	    	      atom.nucleoScaleNumber = sequenceNum;
	  	    	    break;
	  	    	  }

		    	  setAdditionalAtomParameters(atom);
		    	  if (haveMappedSerials) {
		    	      atomSetCollection.addAtomWithMappedSerialNumber(atom);
		    	  }
		    	  else {
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
		    	    //initial X Y Z & Radius 
		    	  unitX = 0;
		    	  unitY = 0;
		    	  unitZ = 0;
		    	  unitRadius = 0;
		    	  //chrID = 0;//Tuan change
		    	  fromPos = 0;
		    	  endPos = 0;
		      }

	    }
	    else if (downOrUp.equals("down")){
      	  Pattern patternGlobScaleLine = Pattern.compile("(^<gs>)(\\d+)");
      	  Pattern patternChrScaleLine = Pattern.compile("(^<cs>)(\\d+)");
  	      Pattern patternLociScaleLine = Pattern.compile("^(<ls>)(\\d+)");
  	      Pattern patternFiberScaleLine = Pattern.compile("(^<fs>)(\\d+)"); 
  	      Pattern patternLengthLine = Pattern.compile("(^<lt>)(\\d+)(</lt>)");
	      Pattern patternNucleoLineSimp = Pattern.compile("(^<un\\s)(\\d+)(>)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(</un>)");
	      Pattern patternNucleoLineWseq = Pattern.compile("(^<un\\s)(\\d+)(>)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(</un><seq>)([\\d]+)(\\s)([\\d]+)(</seq>)");
	      Pattern patternGlobalChrLineWseq = Pattern.compile("(^<un\\s)(\\d+)(>)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(</un><seq>)([\\d]+)(\\s)([\\d]+)(</seq><ens-chr>)([A-Za-z0-9]+)(</ens-chr>)(<lc-seq>)(.*)(</lc-seq>)");
	      Pattern patternSpLine = Pattern.compile("(<sp>)(.*)(</sp>)");
	      Pattern patternEnsLine = Pattern.compile("(<ens-chr>)([A-Za-z0-9]+)(</ens-chr>)");
	      Pattern patternLcLine = Pattern.compile("(<lc-seq>)(.*)(</lc-seq>)");
	      
  	      Matcher matcherGls = patternGlobScaleLine.matcher(line);
  	      Matcher matcherChs = patternChrScaleLine.matcher(line);
  	      Matcher matcherLcs = patternLociScaleLine.matcher(line);
  	      Matcher matcherFbs = patternFiberScaleLine.matcher(line);
  	      Matcher matcherLt = patternLengthLine.matcher(line);
	      Matcher matcherNulSimp = patternNucleoLineSimp.matcher(line);
	      Matcher matcherNulWseq = patternNucleoLineWseq.matcher(line);
	      Matcher MatcherGlobalChrLineWseq = patternGlobalChrLineWseq.matcher(line);
	      Matcher matcherSpLine = patternSpLine.matcher(line);
	      Matcher matcherEnsLine = patternEnsLine.matcher(line);
	      Matcher matcherLcLine = patternLcLine.matcher(line);
  	      
  	      
  	      if (matcherGls.find()){
  			    globScaleNum = Integer.parseInt(matcherGls.group(2));
  		  }
  	      else if (matcherChs.find()) {
  			    chrScaleNum = Integer.parseInt(matcherChs.group(2));
  		  }
  	      else if (matcherLcs.find()){
  				lociScaleNum = Integer.parseInt(matcherLcs.group(2));
  				int lociSelected = selectedPath[2];
  				//from loci scale, start to change
  		  		if (lociSelected == lociScaleNum && currentSelectedScale == 3) {
  		  			countLoci++;
  		  		}
  		  }
  	      else if (matcherFbs.find()){
  			    fiberScaleNum = Integer.parseInt(matcherFbs.group(2));
  		  }
  		  else if (matcherLt.matches()){
  				sequenceLength = Integer.parseInt(matcherLt.group(2));
  		  }
		  else if (matcherNulSimp.matches()){
				sequenceNum = Integer.parseInt(matcherNulSimp.group(2));
				unitX = Float.parseFloat(matcherNulSimp.group(4));
				unitY = Float.parseFloat(matcherNulSimp.group(6));
				unitZ = Float.parseFloat(matcherNulSimp.group(8));
				unitRadius = Float.parseFloat(matcherNulSimp.group(10));
		  }
		  else if (matcherNulWseq.matches()) {
				sequenceNum = Integer.parseInt(matcherNulWseq.group(2));
				unitX = Float.parseFloat(matcherNulWseq.group(4));
				unitY = Float.parseFloat(matcherNulWseq.group(6));
				unitZ = Float.parseFloat(matcherNulWseq.group(8));
				unitRadius = Float.parseFloat(matcherNulWseq.group(10));
				fromPos = Integer.parseInt(matcherNulWseq.group(12));
				endPos = Integer.parseInt(matcherNulWseq.group(14));
		  }
		  else if (MatcherGlobalChrLineWseq.matches()) {
				sequenceNum = Integer.parseInt(MatcherGlobalChrLineWseq.group(2));
				unitX = Float.parseFloat(MatcherGlobalChrLineWseq.group(4));
				unitY = Float.parseFloat(MatcherGlobalChrLineWseq.group(6));
				unitZ = Float.parseFloat(MatcherGlobalChrLineWseq.group(8));
				unitRadius = Float.parseFloat(MatcherGlobalChrLineWseq.group(10));
				fromPos = Integer.parseInt(MatcherGlobalChrLineWseq.group(12));
				endPos = Integer.parseInt(MatcherGlobalChrLineWseq.group(14));
				ensChr = MatcherGlobalChrLineWseq.group(16);
				lcChr = MatcherGlobalChrLineWseq.group(19);
		  }
		  else if (matcherSpLine.matches()) {
			    spName = matcherSpLine.group(2);
		  }
		  else if (matcherEnsLine.matches()) {
			    ensChr = matcherEnsLine.group(2);
			    
			    //Tuan add
			    if (ensChr.equalsIgnoreCase("X")){
			    	chrID = 23;
			    }else if (ensChr.equalsIgnoreCase("Y")){
			    	chrID = 24;
			    }else{
			    	chrID = Integer.parseInt(ensChr);
			    }
			    //End
		  }
		  else if (matcherLcLine.matches()) {
			    lcChr = matcherLcLine.group(2);
		  }
  	  
  	    //  unitRadius = 4.5f;
  	      if (unitRadius != 0){
  	    	  setAtomCoord(atom, unitX, unitY, unitZ);
  	    	  atom.globScaleNumber = globScaleNum;
  	    	  atom.fiberScaleNumber = fiberScaleNum;
  	       	  atom.nucleoScaleNumber = nucleoScaleNum;
  	    	  atom.lociScaleNumber = lociScaleNum;
  	    	  atom.chrScaleNumber = chrScaleNum;
  	    	  atom.sequenceLength = sequenceLength;
	    	  atom.chrID = chrID;
	    	  
	    	  //Tuan added
	    	  atom.chainID = (char)(chrID - 1 + 'A');
	    	  //End
	    	  
	    	  atom.fromPos = fromPos;
	    	  atom.endPos = endPos;
	    	  atom.spName = spName;
	    	  atom.ensChr = ensChr;
	    	  atom.lcChr = lcChr;
	    	  
  	    	  //sequenceNum => chr/loci/fiber/nucleoNumber
  	    	  switch (currentSelectedScale) {
  	    	    case 1:
  	    		  atom.lociScaleNumber = sequenceNum;
  	    		break;
  	    	    case 2:
  	    	      atom.fiberScaleNumber = sequenceNum;
  	    	    break;
  	    	    case 3:
  	    	      atom.nucleoScaleNumber = sequenceNum;
  	    	    break;
  	    	  }
  	    	  
  	    	  //setAtomCoord(atom, 1, 2, 3);
  	    	  setAdditionalAtomParameters(atom);
  	    	  if (haveMappedSerials) {
  	    	      atomSetCollection.addAtomWithMappedSerialNumber(atom);
  	    	  }
  	    	  else {
  	    		  //determine what units to load into atomSetCollection-hcf-important
  	    		  switch (currentSelectedScale) {
  	    		  	case 1:
  	    			  if (currentSelectedUnit == 0 || chrScaleNum == currentSelectedUnit) {
  	    				  atomSetCollection.addAtom(atom, true);
  	    			  }
  	    			break;
  	    		  	case 2:
  	    		  	  if(selectedPath[2] != 0) {
	  	    		  		if (chrScaleNum == selectedPath[1] && lociScaleNum == selectedPath[2]) {
	  	    		  			atomSetCollection.addAtom(atom, true);
	  	    		  		}
  	    		  	  }
  	    		  	  else if (selectedPath[1] != 0) {
	  	    		  		if (chrScaleNum == selectedPath[1]) {
	  	    		  			atomSetCollection.addAtom(atom, true);
	  	    		  		}
  	    		  	  }
  	    		  	  else if (selectedPath[1] == 0) {
	  	    		  		atomSetCollection.addAtom(atom, true);
  	    		  	  }
  	    		  	break;
  	    		  	case 3:
	  	    		  	if (selectedPath[3] != 0) {
	  	    		  		if (chrScaleNum == selectedPath[1] && lociScaleNum == selectedPath[2] && fiberScaleNum == selectedPath[3]) {
	  	    		  			atomSetCollection.addAtom(atom, true);
	  	    		  		}
	  	    		  	}
	  	    		  	else if(selectedPath[2] != 0) {
	  	    		  		if (chrScaleNum == selectedPath[1] && lociScaleNum == selectedPath[2]) {
	  	    		  			atomSetCollection.addAtom(atom, true);
	  	    		  		}
	  	    		  	}
	  	    		  	else if (selectedPath[1] != 0) {
	  	    		  		if (chrScaleNum == selectedPath[1]) {
	  	    		  			atomSetCollection.addAtom(atom, true);
	  	    		  		}
	  	    		  		
	  	    		  	}
	  	    		  	else if (selectedPath[1] == 0) {
	  	    		  		atomSetCollection.addAtom(atom, true);
	  	    		  	}
  	    		  	break;
  	    		  	case 4:
  	    		  		atomSetCollection.addAtom(atom, true);
  	    		    break;
  	    		  }
  	    	      
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
  	    	    //initial X Y Z & Radius 
  	    	  unitX = 0;
  	    	  unitY = 0;
  	    	  unitZ = 0;
  	    	  unitRadius = 0;
  	    	//chrID = 0;//Tuan change
	    	  fromPos = 0;
	    	  endPos = 0;  	    	  
  	      }
    	}
    	else if (downOrUp.equals("up")) {
      	  Pattern patternGlobScaleLine = Pattern.compile("(^<gs>)(\\d+)");
      	  Pattern patternChrScaleLine = Pattern.compile("(^<cs>)(\\d+)");
  	      Pattern patternLociScaleLine = Pattern.compile("^(<ls>)(\\d+)");
  	      Pattern patternFiberScaleLine = Pattern.compile("(^<fs>)(\\d+)"); 
  	      Pattern patternLengthLine = Pattern.compile("(^<lt>)(\\d+)(</lt>)");
	      Pattern patternNucleoLineSimp = Pattern.compile("(^<un\\s)(\\d+)(>)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(</un>)");
	      Pattern patternNucleoLineWseq = Pattern.compile("(^<un\\s)(\\d+)(>)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(</un><seq>)([\\d]+)(\\s)([\\d]+)(</seq>)");
	      Pattern patternGlobalChrLineWseq = Pattern.compile("(^<un\\s)(\\d+)(>)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(</un><seq>)([\\d]+)(\\s)([\\d]+)(</seq><ens-chr>)([A-Za-z0-9]+)(</ens-chr>)(<lc-seq>)(.*)(</lc-seq>)");
	      Pattern patternSpLine = Pattern.compile("(<sp>)(.*)(</sp>)");
	      Pattern patternEnsLine = Pattern.compile("(<ens-chr>)([A-Za-z0-9]+)(</ens-chr>)");
	      Pattern patternLcLine = Pattern.compile("(<lc-seq>)(.*)(</lc-seq>)");
	      
  	      Matcher matcherGls = patternGlobScaleLine.matcher(line);
  	      Matcher matcherChs = patternChrScaleLine.matcher(line);
  	      Matcher matcherLcs = patternLociScaleLine.matcher(line);
  	      Matcher matcherFbs = patternFiberScaleLine.matcher(line);
  	      Matcher matcherLt = patternLengthLine.matcher(line);
	      Matcher matcherNulSimp = patternNucleoLineSimp.matcher(line);
	      Matcher matcherNulWseq = patternNucleoLineWseq.matcher(line);
	      Matcher MatcherGlobalChrLineWseq = patternGlobalChrLineWseq.matcher(line);
	      Matcher matcherSpLine = patternSpLine.matcher(line);
	      Matcher matcherEnsLine = patternEnsLine.matcher(line);
	      Matcher matcherLcLine = patternLcLine.matcher(line);
  	      
  	      
  	      if (matcherGls.find()){
  			    globScaleNum = Integer.parseInt(matcherGls.group(2));
  		  }
  	      else if (matcherChs.find()) {
  			    chrScaleNum = Integer.parseInt(matcherChs.group(2));
  		  }
  	      else if (matcherLcs.find()){
  				lociScaleNum = Integer.parseInt(matcherLcs.group(2));
  				int lociSelected = selectedPath[2];
  				//from loci scale, start to change
  		  		if (lociSelected == lociScaleNum && currentSelectedScale == 3) {
  		  			countLoci++;
  		  		}
  		  }
  	      else if (matcherFbs.find()){
  			    fiberScaleNum = Integer.parseInt(matcherFbs.group(2));
  		  }
  		  else if (matcherLt.matches()){
  				sequenceLength = Integer.parseInt(matcherLt.group(2));
  		  }
		  else if (matcherNulSimp.matches()){
				sequenceNum = Integer.parseInt(matcherNulSimp.group(2));
				unitX = Float.parseFloat(matcherNulSimp.group(4));
				unitY = Float.parseFloat(matcherNulSimp.group(6));
				unitZ = Float.parseFloat(matcherNulSimp.group(8));
				unitRadius = Float.parseFloat(matcherNulSimp.group(10));
		  }
		  else if (matcherNulWseq.matches()) {
				sequenceNum = Integer.parseInt(matcherNulWseq.group(2));
				unitX = Float.parseFloat(matcherNulWseq.group(4));
				unitY = Float.parseFloat(matcherNulWseq.group(6));
				unitZ = Float.parseFloat(matcherNulWseq.group(8));
				unitRadius = Float.parseFloat(matcherNulWseq.group(10));
				fromPos = Integer.parseInt(matcherNulWseq.group(12));
				endPos = Integer.parseInt(matcherNulWseq.group(14));
		  }
		  else if (MatcherGlobalChrLineWseq.matches()) {
				sequenceNum = Integer.parseInt(MatcherGlobalChrLineWseq.group(2));
				unitX = Float.parseFloat(MatcherGlobalChrLineWseq.group(4));
				unitY = Float.parseFloat(MatcherGlobalChrLineWseq.group(6));
				unitZ = Float.parseFloat(MatcherGlobalChrLineWseq.group(8));
				unitRadius = Float.parseFloat(MatcherGlobalChrLineWseq.group(10));
				fromPos = Integer.parseInt(MatcherGlobalChrLineWseq.group(12));
				endPos = Integer.parseInt(MatcherGlobalChrLineWseq.group(14));
				ensChr = MatcherGlobalChrLineWseq.group(16);
				lcChr = MatcherGlobalChrLineWseq.group(19);
		  }
		  else if (matcherSpLine.matches()) {
			    spName = matcherSpLine.group(2);
		  }
		  else if (matcherEnsLine.matches()) {
			    ensChr = matcherEnsLine.group(2);
			    
			    //Tuan add
			    if (ensChr.equalsIgnoreCase("X")){
			    	chrID = 23;
			    }else if (ensChr.equalsIgnoreCase("Y")){
			    	chrID = 24;
			    }else{
			    	chrID = Integer.parseInt(ensChr);
			    }
			    //End
		  }
		  else if (matcherLcLine.matches()) {
			    lcChr = matcherLcLine.group(2);
		  }
  	  
  	    //  unitRadius = 4.5f;
  	      if (unitRadius != 0){
  	    	  setAtomCoord(atom, unitX, unitY, unitZ);
  	    	  atom.globScaleNumber = globScaleNum;
  	    	  atom.fiberScaleNumber = fiberScaleNum;
  	    	  atom.nucleoScaleNumber = nucleoScaleNum;
  	    	  atom.lociScaleNumber = lociScaleNum;
  	    	  atom.chrScaleNumber = chrScaleNum;
  	    	  atom.sequenceLength = sequenceLength;
	    	  atom.chrID = chrID;
	    	  
	    	  //Tuan added
	    	  atom.chainID = (char)(chrID - 1 + 'A');
	    	  //End
	    	  
	    	  atom.fromPos = fromPos;
	    	  atom.endPos = endPos;
	    	  atom.spName = spName;
	    	  atom.ensChr = ensChr;
	    	  atom.lcChr = lcChr;
	    	  
  	    	  //sequenceNum => chr/loci/fiber/nucleoNumber
  	    	  switch (currentSelectedScale) {
	    	    case 5:
	    		  atom.nucleoScaleNumber = sequenceNum;
	    		break;  	    	  	
  	    	    case 4:
  	    		  atom.fiberScaleNumber = sequenceNum;
  	    		break;
  	    	    case 3:
  	    	      atom.lociScaleNumber = sequenceNum;
  	    	    break;
  	    	    case 2:
  	    	      atom.chrScaleNumber = sequenceNum;
  	    	    break;
  	    	  }
 
  	    	  //setAtomCoord(atom, 1, 2, 3);
  	    	  setAdditionalAtomParameters(atom);
  	    	  if (haveMappedSerials) {
  	    	      atomSetCollection.addAtomWithMappedSerialNumber(atom);
  	    	  }
  	    	  else {
  	    		  //determine what units to load into atomSetCollection-hcf-important
  	    		  switch (currentSelectedScale) {
  	    		    case 2:
  	    		    	atomSetCollection.addAtom(atom, true);
  	  	    		  	break;
  	    		  	case 3:
  	    		  		atomSetCollection.addAtom(atom, true);
  	    		  	    break;
  	    		  	case 4:
	  	    		  	if(selectedPath[2] != 0) {
	  	    		  		if (chrScaleNum == selectedPath[1] && lociScaleNum == selectedPath[2]) {
	  	    		  		atomSetCollection.addAtom(atom, true);
	  	    		  		}
	  	    		  	}
	  	    		  	else if (selectedPath[1] != 0) {
	  	    		  		if (chrScaleNum == selectedPath[1]) {
	  	    		  			atomSetCollection.addAtom(atom, true);
	  	    		  		}
	  	    		  	}
	  	    		  	else if (selectedPath[1] == 0) {
	  	    		  		atomSetCollection.addAtom(atom, true);
	  	    		  	}
  	    		  	    break;
  	    		  	case 5:
	  	    		  	if(selectedPath[2] != 0 && selectedPath[3] != 0) {
	  	    		  		if (chrScaleNum == selectedPath[1] && lociScaleNum == selectedPath[2] 
	  	    		  				&& fiberScaleNum == selectedPath[3]) {
	  	    		  		atomSetCollection.addAtom(atom, true);
	  	    		  		}
	  	    		  	}
	  	    		  	else if (selectedPath[1] != 0) {
	  	    		  		if (chrScaleNum == selectedPath[1]) {
	  	    		  			atomSetCollection.addAtom(atom, true);
	  	    		  		}
	  	    		  	}
	  	    		  	else if (selectedPath[1] == 0) {
	  	    		  		atomSetCollection.addAtom(atom, true);
	  	    		  	}
  	    		    break;
  	    		  }
  	    	      
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
  	    	    //initial X Y Z & Radius 
  	    	  unitX = 0;
  	    	  unitY = 0;
  	    	  unitZ = 0;
  	    	  unitRadius = 0;
  	    	  //chrID = 0;//Tuan change
	    	  fromPos = 0;
	    	  endPos = 0;  	    	  
  	      }
    	}
	    
	    //Tuan added
	    if (atom.atomSerial > maxSerial) maxSerial = atom.atomSerial;
	    //End

    }

  }
 

  //modify end -hcf

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
    if (isTrue) {
      Logger.warn("PdbReader findAtom chain=" + chain + " resno=" + resno + " not found");
      tlsAddError("atom not found: chain=" + chain + " resno=" + resno);
    }
    return (isTrue ? -1 : atom2);
  }

  private Map<Atom, float[]>tlsU;

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

