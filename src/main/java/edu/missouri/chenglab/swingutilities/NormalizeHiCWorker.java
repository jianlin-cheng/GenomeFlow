package edu.missouri.chenglab.swingutilities;

import java.util.List;

import javax.swing.SwingWorker;

import edu.missouri.chenglab.hicdata.Normalization;
import juicebox.data.Dataset;
import juicebox.tools.utils.original.NormalizationVectorUpdater;

public class NormalizeHiCWorker extends SwingWorker<String,Integer>{
	
	String inputFile, outputFile;
	int minResolution  = 0;
	public NormalizeHiCWorker(String inputFile, String outputFile, int minRes){
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.minResolution = minRes;
	}
	
	@Override
	protected String doInBackground() throws Exception {
		
		if (inputFile.endsWith(".hic")){			
			if (minResolution > 0) NormalizationVectorUpdater.updateHicFile(inputFile, minResolution);
			else NormalizationVectorUpdater.updateHicFile(inputFile);
		}else{
			Normalization.normalize(inputFile, outputFile, 2);
		}
		
		
		return "Normalization done!";
	}
	
}