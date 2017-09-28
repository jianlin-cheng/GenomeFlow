package edu.missouri.chenglab.swingutilities;

import java.util.List;

import javax.swing.SwingWorker;

import juicebox.data.Dataset;
import juicebox.data.HiCFileTools;

public class ReadHiCHeaderWorker extends SwingWorker<Dataset,Integer>{
	private List<String> files;
	//private JButton runButton;
	
	public ReadHiCHeaderWorker(List<String> files){
		this.files = files;
		//this.runButton = button;
		//runButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	@Override
	protected Dataset doInBackground() throws Exception {
		Dataset dataset = HiCFileTools.extractDatasetForCLT(files, false); 
		return dataset;
	}
}
