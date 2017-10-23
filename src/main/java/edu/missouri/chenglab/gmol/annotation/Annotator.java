package edu.missouri.chenglab.gmol.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jmol.modelset.Atom;
import org.jmol.viewer.Viewer;

import edu.missouri.chenglab.gmol.Constants;
import edu.missouri.chenglab.gmol.valueobjects.Region;

public class Annotator {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public void annotate(String trackName, String trackFile, String color, int radius, Viewer viewer) throws Exception{
		
		List<Region> regions = readRegions(trackFile);
		Atom[] atoms = viewer.getModelSet().atoms;
				
		StringBuilder commandSB = new StringBuilder();
		for(Atom atom : atoms){
			boolean isLabel = false;
			for(Region reg : regions){
				if (isOverlap(reg, atom)){
					if (reg.getName() != null && reg.getName().length() > 0) atom.labels.put(trackName, reg.getName());					
					isLabel = true;
				}
			}
			if (isLabel){
				StringBuffer label = new StringBuffer();
				for(String k : atom.labels.keySet()){
					if (label.length() > 0) label.append(",");
					label.append(atom.labels.get(k).replace(";", ","));
				}
				//if (label.length() == 0) label.append(trackName);
				
				StringBuffer script = new StringBuffer();
				script.append("select " + atom.index + ";wireframe " + radius + ";color " + color + ";");
				
				atom.currentColor = color;
				
				if (label.length() > 0) {
					script.append("label " + label.toString() + ";");
				}
				
				//viewer.script(script.toString());
				commandSB.append(script.toString());
			}
		}
		commandSB.append("select all;");
		viewer.evalString(commandSB.toString());		
	}
	
	
	public void annotateDomain(String trackName, String trackFile, Viewer viewer) throws Exception{
		
		List<Region> regions = readRegions(trackFile);
		Atom[] atoms = viewer.getModelSet().atoms;
				
		StringBuilder commandSB = new StringBuilder();
		
		int ireg = 0, iatom = 0;
		Region reg;
		Atom atom, begAtom=null, endAtom=null;		
		
		int smallSize = 2, bigSize = 8;
		int count = 0;
		while(ireg < regions.size() && iatom < atoms.length){
			reg = regions.get(ireg);
			atom = atoms[iatom];
			
			if (isOverlap(reg, atom)){
				
				if (atom.fromPos <= reg.getStart() && reg.getEnd() <= atom.endPos){
					begAtom = atom;
					endAtom = atom;					
				}else{
					if (begAtom == null){ 
						begAtom = atom;
					}else{
						endAtom = atom;						
					}
					
				}
				iatom++;				
				
			}else if (begAtom != null && endAtom != null){
				
				commandSB.append(String.format("select atomno >= %d and atomno <= %d; wireframe %d; color %s;", 
						begAtom.index + 1, endAtom.index + 1, count % 2 == 0 ? smallSize : bigSize, count % 2 == 0 ? "red" : "green"));
				count++;
				
				begAtom = null;
				endAtom = null;
				
				ireg++;
			}else{
				
				
				int k = compare(reg, atom);
				if (k > 0) iatom++;
				else{
					ireg++;
				}
			}
		}
		
		viewer.evalString(commandSB.toString());
		viewer.evalString(String.format("select all;"));
		

		
	}

	
	
	/**
	 * Clear all labels
	 * @param viewer
	 * @throws Exception
	 */
	public void deannotate(Viewer viewer) throws Exception{

		StringBuffer script = new StringBuffer();
		script.append("select all;label \"\";wireframe " + Constants.DEFAULTWIREFRAME  + ";color structure;");
		viewer.script(script.toString());
	}

	
	//positive if reg > atom
	//negative if reg < atom
	private int compare(Region reg, Atom atom){
		if (reg.getChrID() != atom.chrID) return reg.getChrID() - atom.chrID;		
		return reg.getStart() - atom.fromPos;
	}

	
	private boolean isOverlap(Region reg, Atom atom){
		if (reg.getChrID() != atom.chrID) return false;
		
		return Integer.max(reg.getStart(), atom.fromPos) < Integer.min(reg.getEnd(), atom.endPos);
	}
	
	
	private List<Region> readRegions(String trackFile) throws Exception{
		
		Pattern genePattern = Pattern.compile("^(.+?)(\\s+)chr(\\d+)(\\s+)([+-])(\\s+)(\\d+)(\\s+)(\\d+)(\\s+)(.*)");
		Pattern bedPattern = Pattern.compile("^chr(\\d+|[XY])(\\s+)(\\d+)(\\s+)(\\d+)(\\s+)(\\S+)(.*)");
		Pattern bedPatternNoName = Pattern.compile("^chr(\\d+|[XY])(\\s+)(\\d+)(\\s+)(\\d+)(.*)");
		
		
		List<Region> rs = new ArrayList<Region>();
		
		File f = new File(trackFile);
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String ln, name="";		
		int chrID, start, end;
		
		while((ln = br.readLine()) != null){
			if (ln.startsWith("#")) continue;
			
			Matcher geneMatcher = genePattern.matcher(ln);
			Matcher bedMatcher = bedPattern.matcher(ln);
			Matcher bedMatcherNoName = bedPatternNoName.matcher(ln);
			
			if (geneMatcher.find()){
				chrID = Integer.parseInt(geneMatcher.group(3));
				start = Integer.parseInt(geneMatcher.group(7));
				end = Integer.parseInt(geneMatcher.group(9));
				name = geneMatcher.group(1);
			}else if (bedMatcher.find()){
				if (bedMatcher.group(1).equalsIgnoreCase("x")){
					chrID = 23;
					
				}else if (bedMatcher.group(1).equalsIgnoreCase("y")){
					chrID = 24;				
				}else{
					chrID = Integer.parseInt(bedMatcher.group(1));
				}
				start = Integer.parseInt(bedMatcher.group(3));
				end = Integer.parseInt(bedMatcher.group(5));
				name = bedMatcher.group(7);
			}else if (bedMatcherNoName.find()){
				if (bedMatcherNoName.group(1).equalsIgnoreCase("x")){
					chrID = 23;
					
				}else if (bedMatcherNoName.group(1).equalsIgnoreCase("y")){
					chrID = 24;				
				}else{
					chrID = Integer.parseInt(bedMatcherNoName.group(1));
				}
				start = Integer.parseInt(bedMatcherNoName.group(3));
				end = Integer.parseInt(bedMatcherNoName.group(5));
				name = "";
			}else{
				continue;
			}
			
			rs.add(new Region(chrID, start, end, name));
		}
		br.close();
		
		Collections.sort(rs);
		return rs;
	}

}
