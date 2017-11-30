package edu.missouri.chenglab.gmol.valueobjects;

import org.jmol.adapter.smarter.Atom;

public class Region implements Comparable<Region> {
	private int chrID;
	private int start;
	private int end;
	private String name;
	private Atom atom;
	
	public Region(int chr, int start, int end, String name){
		this.chrID = chr;
		this.start = start;
		this.end = end;
		this.name = name;
	}
	
	public int getChrID() {
		return chrID;
	}
	public void setChrID(int chrID) {
		this.chrID = chrID;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}	
	public Atom getAtom() {
		return atom;
	}

	public void setAtom(Atom atom) {
		this.atom = atom;
	}

	@Override
	public int compareTo(Region o) {
		if (chrID != o.chrID) return Integer.compare(chrID, o.chrID);
		if(start != o.start) return Integer.compare(start, o.start);		
		return Integer.compare(end, o.end);
	}
	
	@Override
	public boolean equals(Object o){
		if (! (o instanceof Region)) {
			return false;			
		};
		
		Region tmp = (Region) o;		
		return tmp.chrID == this.chrID && tmp.start ==  this.start && tmp.end == this.end;		
	}
	
	@Override
	public int hashCode(){
		return chrID + start + end;
	}
	
	
}
