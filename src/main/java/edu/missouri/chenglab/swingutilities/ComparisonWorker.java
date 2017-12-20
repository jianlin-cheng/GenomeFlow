package edu.missouri.chenglab.swingutilities;

import javax.swing.SwingWorker;

import edu.missouri.chenglab.gmol.modelcomparison.Comparison;
import edu.missouri.chenglab.gmol.valueobjects.ComparisonObject;

public class ComparisonWorker extends SwingWorker<ComparisonObject,Integer>{
	private String inputFile1;
	private String inputFile2;
	
	public ComparisonWorker(String in1, String in2){
		this.inputFile1 = in1;
		this.inputFile2 = in2;
	}
	@Override
	protected ComparisonObject doInBackground() throws Exception {
		Comparison comparison = new Comparison();
		return comparison.compare(inputFile1, inputFile2);
	}
}
