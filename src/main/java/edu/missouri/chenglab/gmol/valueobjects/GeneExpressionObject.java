package edu.missouri.chenglab.gmol.valueobjects;

import java.util.HashMap;
import java.util.Map;

public class GeneExpressionObject extends Region {

	private Map<String, Double> expressionLevel = new HashMap<String, Double>();
	private String desc;
	
	public GeneExpressionObject(int chr, int start, int end, String name) {
		super(chr, start, end, name);		
	}
	
	

	public Map<String, Double> getExpressionLevel() {
		return expressionLevel;
	}


	public void setExpressionLevel(Map<String, Double> expressionLevel) {
		this.expressionLevel = expressionLevel;
	}




	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
}
