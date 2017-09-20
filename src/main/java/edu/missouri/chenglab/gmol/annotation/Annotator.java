package edu.missouri.chenglab.gmol.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jmol.modelset.Atom;
import org.jmol.viewer.Viewer;

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
					else atom.labels.put(trackName, trackName);
					isLabel = true;
				}
			}
			if (isLabel){
				StringBuffer label = new StringBuffer();
				for(String k : atom.labels.keySet()){
					if (label.length() > 0) label.append(",");
					label.append(atom.labels.get(k));
				}
				StringBuffer script = new StringBuffer();
				script.append("select " + atom.index + ";wireframe " + radius + ";color " + color + ";");
				
				
				if (label.length() > 0) script.append("label " + label.toString() + ";select all;");
				
				viewer.script(script.toString());
			}
		}
		
	}
	
	private boolean isOverlap(Region reg, Atom atom){
		if (reg.getChrID() != atom.chrID) return false;
		
		return Integer.max(reg.getStart(), atom.fromPos) < Integer.min(reg.getEnd(), atom.endPos);
	}
	
	
	private List<Region> readRegions(String trackFile) throws Exception{
		
		List<Region> rs = new ArrayList<Region>();
		
		File f = new File(trackFile);
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String ln, name="", st[];		
		int chrID, start, end;
		
		while((ln = br.readLine()) != null){
			if (ln.startsWith("#")) continue;
			st = ln.split("\\s+");
			if (st.length < 3) continue;
			if (st[0].startsWith("chr")){
				chrID = Integer.parseInt(st[0].replace("chr", ""));
			}else{
				chrID = Integer.parseInt(st[0]);
			}
			start = Integer.parseInt(st[1]);
			end = Integer.parseInt(st[2]);
			name = "";
			if (st.length > 3){
				for(int i = 3; i < st.length; i++){
					name += st[i];
				}
			}
			
			rs.add(new Region(chrID, start, end, name));
		}
		br.close();
		
		Collections.sort(rs);
		return rs;
	}

}
