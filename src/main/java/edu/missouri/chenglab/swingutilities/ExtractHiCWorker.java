package edu.missouri.chenglab.swingutilities;

import javax.swing.SwingWorker;

import org.broad.igv.Globals;

import edu.missouri.chenglab.hicdata.ReadHiCData;
import juicebox.windowui.MatrixType;

public class ExtractHiCWorker extends SwingWorker<String,Void>{
	private ReadHiCData readHiCData;
	//private JButton runButton;
	
	public ExtractHiCWorker(ReadHiCData readHiC){
		this.readHiCData = readHiC;		
	}
	@Override
	protected String doInBackground() throws Exception {
		
		try{
			 if ((readHiCData.getMatrixType() == MatrixType.OBSERVED || readHiCData.getMatrixType() == MatrixType.NORM)
		                && readHiCData.getChrom1().getName().equals(Globals.CHR_ALL)
		                && readHiCData.getChrom2().getName().equals(Globals.CHR_ALL)) {			 
			 
				boolean includeIntra = true;
				ReadHiCData.dumpGenomeWideData(readHiCData.getDataset(), readHiCData.getDataset().getChromosomes() , includeIntra, 
						readHiCData.getZoom(), readHiCData.getNorm(), readHiCData.getMatrixType(), readHiCData.getZoom().getBinSize(), readHiCData.getOutputFile());
			 }else{
			
				 ReadHiCData.dumpMatrix(readHiCData.getDataset(), readHiCData.getChrom1(), readHiCData.getChrom2(),
						 readHiCData.getNorm(), readHiCData.getZoom(), readHiCData.getMatrixType(), readHiCData.getOutputFile());
			 }
		 
		}catch(Exception ex){
			ex.printStackTrace();
			return ex.getMessage();
		}
		
		return "Data is extracted!";
	}
}
