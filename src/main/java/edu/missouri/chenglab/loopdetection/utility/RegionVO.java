package edu.missouri.chenglab.loopdetection.utility;

import java.util.ArrayList;

public class RegionVO implements Comparable<RegionVO>{
	String chrom;
	int chr_id;
	int start;
	int end;
	String name;
	
	ArrayList<RegionVO> genes = new ArrayList<RegionVO>();
	ArrayList<RegionVO> enhancers = new ArrayList<RegionVO>();
	
	public RegionVO(String chr, int st, int en){
		chrom = chr;
		start = st;
		end = en;
		if (chrom.equals("chrX")) chr_id = 23;
		else chr_id = Integer.parseInt(chrom.replace("chr", ""));
	}
	
	public int compareTo(RegionVO r){
		if (r.chr_id != this.chr_id) return Integer.compare(this.chr_id, r.chr_id);
		if (r.start != this.start) return Integer.compare(this.start, r.start);
		return Integer.compare(r.end,  this.end);
	}

	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<RegionVO> getGenes() {
		return genes;
	}

	public void setGenes(ArrayList<RegionVO> genes) {
		this.genes = genes;
	}

	public ArrayList<RegionVO> getEnhancers() {
		return enhancers;
	}

	public void setEnhancers(ArrayList<RegionVO> enhancers) {
		this.enhancers = enhancers;
	}

	public void add_gene(RegionVO g){
		genes.add(g);
	}
	
	public void add_enhancer(RegionVO e){
		enhancers.add(e);
	}
	
	public String getChrom() {
		return chrom;
	}

	public void setChrom(String chrom) {
		this.chrom = chrom;
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

	public int getChr_id() {
		return chr_id;
	}

	public void setChr_id(int chr_id) {
		this.chr_id = chr_id;
	}
	
	
	
}
