package edu.missouri.chenglab.lordg.valueObject;

public class GenomicLocation {

	private int id;
	private int originalID;

	private String chr;


	private int start;
	private int end;
	
	

	public GenomicLocation(int id, String chr, int start, int end) {


		this.id = id;
		this.chr = chr;
		this.start = start;
		this.end = end;
				
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getOriginalID() {
		return originalID;
	}

	public void setOriginalID(int originalID) {
		this.originalID = originalID;
	}


	public String getChr() {
		return chr;
	}

	public void setChr(String chr) {
		this.chr = chr;
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
	
	
}
