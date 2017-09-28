package edu.missouri.chenglab.swingutilities;

import javax.swing.SwingWorker;

import edu.missouri.chenglab.hicdata.ReadHiCData;

public class ExtractHiCWorker extends SwingWorker<Void,Void>{
	private ReadHiCData readHiCData;
	//private JButton runButton;
	
	public ExtractHiCWorker(ReadHiCData readHiC){
		this.readHiCData = readHiC;		
	}
	@Override
	protected Void doInBackground() throws Exception {
		
		ReadHiCData.dumpMatrix(readHiCData.getDataset(), readHiCData.getChrom1(), readHiCData.getChrom2(),
				readHiCData.getNorm(), readHiCData.getZoom(), readHiCData.getMatrixType(), readHiCData.getOutputFile());
		
		return null;
	}
}
