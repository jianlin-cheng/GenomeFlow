package edu.missouri.chenglab.Panelext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

public class taskexecution implements Runnable {
	private String Output;
	private String Task;
	
	public taskexecution(String output, String task){
		Output = output;
		Task = task;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		/**
		   * Execute Command in background
		   * @param Output
		   */
		  
		
			  String[] cmdScript = new String[]{"/bin/bash", Output}; 
				Map<Integer, String> map = new HashMap<>();					 		
			    map = execCommand(cmdScript);	
			    String status = null;
			    if (map.get(1).toString().isEmpty()) {
			    	status = "Process Completed successfully";
			    }else {
			    	status = "A process error or warning message was thrown by the script. Please address the message if it affects the expected output generation.";							    			
			    }
				JOptionPane.showMessageDialog(null, "command report:\n" + map.get(1).toString() + "\n script status: " + status, Task + " Completed", JOptionPane.INFORMATION_MESSAGE);
		 
		  
	}
	
	/**
	 * Execute shells script
	 * @param str
	 * @return
	 */
	public static Map execCommand(String... str) {
	    Map<Integer, String> map = new HashMap<>();
	    ProcessBuilder pb = new ProcessBuilder(str);
	    pb.redirectErrorStream(true);
	    Process process = null;
	    try {
	        process = pb.start();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    BufferedReader reader = null;
	    if (process != null) {
	        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	    }

	    String line;
	    StringBuilder stringBuilder = new StringBuilder();
	    try {
	        if (reader != null) {
	            while ((line = reader.readLine()) != null) {
	                stringBuilder.append(line).append("\n");
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    try {
	        if (process != null) {
	            process.waitFor();
	        }
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }

	    if (process != null) {
	        map.put(0, String.valueOf(process.exitValue()));
	    }

	    try {
	        map.put(1, stringBuilder.toString());
	    } catch (StringIndexOutOfBoundsException e) {
	        if (stringBuilder.toString().length() == 0) {
	            return map;
	        }
	    }
	    return map;
	}
  

}
