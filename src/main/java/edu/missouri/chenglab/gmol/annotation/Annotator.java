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
					label.append(atom.labels.get(k));
				}
				//if (label.length() == 0) label.append(trackName);
				
				StringBuffer script = new StringBuffer();
				script.append("select " + atom.index + ";wireframe " + radius + ";color " + color + ";");
				
				atom.currentColor = color;
				
				if (label.length() > 0) script.append("label " + label.toString() + ";select all;");
				
				viewer.script(script.toString());
			}
		}
		
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

	
	
	private boolean isOverlap(Region reg, Atom atom){
		if (reg.getChrID() != atom.chrID) return false;
		
		return Integer.max(reg.getStart(), atom.fromPos) < Integer.min(reg.getEnd(), atom.endPos);
	}
	
	
	private List<Region> readRegions(String trackFile) throws Exception{
		
		Pattern genePattern = Pattern.compile("^(.+?)(\\s+)chr(\\d+)(\\s+)([+-])(\\s+)(\\d+)(\\s+)(\\d+)(\\s+)(.*)");
		Pattern bedPattern = Pattern.compile("^chr(\\d+)(\\s+)(\\d+)(\\s+)(\\d+)(\\s+)(.+?)(\\s+)(.*)");
		
		
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
			
			if (geneMatcher.find()){
				chrID = Integer.parseInt(geneMatcher.group(3));
				start = Integer.parseInt(geneMatcher.group(7));
				end = Integer.parseInt(geneMatcher.group(9));
				name = geneMatcher.group(1);
			}else if (bedMatcher.find()){
				chrID = Integer.parseInt(bedMatcher.group(1));
				start = Integer.parseInt(bedMatcher.group(3));
				end = Integer.parseInt(bedMatcher.group(5));
				name = bedMatcher.group(7);
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
