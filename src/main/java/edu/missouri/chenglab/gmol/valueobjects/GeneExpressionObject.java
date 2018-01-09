package edu.missouri.chenglab.gmol.valueobjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class GeneExpressionObject extends Region {

	private Map<String, Double> expressionLevel = new HashMap<String, Double>();
	private String desc;
	private DescriptiveStatistics descStat = new DescriptiveStatistics(); 
	
	public GeneExpressionObject(int chr, int start, int end, String name) {
		super(chr, start, end, name);		
	}
	
	

//	public Map<String, Double> getExpressionLevel() {
//		return expressionLevel;
//	}


//	public void setExpressionLevel(Map<String, Double> expressionLevel) {
//		this.expressionLevel = expressionLevel;
//	}
	
	
	
	public void putExpressionLevel(String name, double level){
		expressionLevel.put(name, level);
		descStat.addValue(level);
	}
	
	public DescriptiveStatistics getDescStat() {
		return descStat;
	}

	public double getExpressionLevel(String name){
		return expressionLevel.get(name);
	}
	
	public List<String> getSampleNames(){
		return new ArrayList<String>(expressionLevel.keySet());
	}
	
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
}
