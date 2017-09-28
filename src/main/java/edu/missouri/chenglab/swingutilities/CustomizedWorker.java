package edu.missouri.chenglab.swingutilities;

import java.awt.Cursor;
import java.util.List;

import javax.swing.JButton;
import javax.swing.SwingWorker;

import juicebox.data.HiCFileTools;
import juicebox.data.Dataset;

public class CustomizedWorker extends SwingWorker<Dataset,Integer>{
	private List<String> files;
	//private JButton runButton;
	
	public CustomizedWorker(List<String> files){
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
