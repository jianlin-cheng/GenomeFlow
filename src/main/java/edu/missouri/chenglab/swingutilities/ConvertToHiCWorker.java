package edu.missouri.chenglab.swingutilities;

import javax.swing.SwingWorker;

import edu.missouri.chenglab.hicdata.PreProcessingHiC;
import juicebox.tools.utils.original.NormalizationVectorUpdater;

public class ConvertToHiCWorker extends SwingWorker<String,Void> {
	
	private PreProcessingHiC preprocess;
	
	
	public ConvertToHiCWorker(PreProcessingHiC preprocess){
		this.preprocess = preprocess;		
	}
	@Override
	protected String doInBackground() throws Exception {
		
		try {
			preprocess.getPreprocessor().preprocess(preprocess.getInputFile());
        
			NormalizationVectorUpdater.updateHicFile(preprocess.getOutputFile()); 
		}catch(Exception ex) {
			return "Error: please check file format! " + ex.getMessage();
		}
        
		return "Convert to HiC done!";
	}
}
