package edu.missouri.chenglab.Panelext;

import java.util.List;

import javax.swing.SwingWorker;
import edu.missouri.chenglab.ClusterTAD.ClusterTAD;

public class ExtractTAD extends SwingWorker<String,Void>{
	String[] Input = new String[6];
	
	public ExtractTAD(String[] input ){
		this.Input=input;
	}
	
	protected String doInBackground() throws Exception {
		String msg = null;
		try{
			
			ClusterTAD ctad = new ClusterTAD(Input);	
			msg = ctad.Perform_operation();
						
		}catch(Exception ex){
			ex.printStackTrace();
			return ex.getMessage();
		}
		
		return msg;
		
		
	}
}


