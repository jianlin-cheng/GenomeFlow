package  edu.missouri.chenglab.compareTAD;

import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import edu.missouri.chenglab.ClusterTAD.Parameter;
import javax.swing.JOptionPane;

public class TADComparison {


	public static int [][] Method1= null;
	public static int [][] Method2 =null;
	public static int Resolution = 40000;
	public static String sep = "\\s+";
	static BufferedWriter log_outputWriter = null;
	static String Outputpath= null;
	static String filename = null; // output filename
	static ArrayList Method1_B1 = new ArrayList();  // search for TAD in an arraylist
	static ArrayList Method1_B2 = new ArrayList();
	static ArrayList Method2_B1 = new ArrayList();
	static ArrayList Method2_B2 = new ArrayList();
	
	/**
	 * Read TAD File 
	 * @param Filename
	 * @param sep
	 * @return
	 * @throws FileNotFoundException
	 */
	public static  int [][] readTADFile (String Filename, int num) throws FileNotFoundException{			
		// pre-read in the number of rows/columns
				int rows = 0;
				int cols = 0;		
				 try {
					 Scanner input = new Scanner (new File( Filename));
				 
						while(input.hasNextLine())
						{	String [] line = null;
							String rowdata = input.nextLine();	
							rowdata = rowdata.trim();
							//System.out.println(rowdata);
							line = rowdata.split(sep);						
							//System.out.println(String.format("col = %d", line.length));
							++rows;		
							cols = line.length;
						}			
						input.close();
				 	} catch (FileNotFoundException e) {
			         e.printStackTrace();
				 	}
				
			
				 // read in the data
				 int [][] a = new int[rows][2];
				 try {
					 int linesCounter = 0;
					 Scanner input = new Scanner (new File( Filename));
				 
						while(input.hasNextLine())
						{
							String [] line = null;
							String rowdata = input.nextLine();
							rowdata = rowdata.trim();
							line = rowdata.split(sep);						
												  
				            a[linesCounter][0] = Integer.parseInt(line[1])/Resolution ;
				            a[linesCounter][1] = Integer.parseInt(line[2])/Resolution ; 
				            if (num ==1) {
				            	Method1_B1 .add( a[linesCounter][0]) ;
				            	Method1_B2 .add( a[linesCounter][1]) ;
				            }else {
				            	
				            	Method2_B1 .add( a[linesCounter][0]) ;
				            	Method2_B2 .add( a[linesCounter][1]) ;
				            }
							 linesCounter++;    						    
						}			
						input.close();
				 	} catch (FileNotFoundException e) {
			         e.printStackTrace();
				 	}
				 
				
				return a;
	}
	
	
	/**
	 * Check if a file exist
	 * @param fileName
	 * @return
	 */
	
	public static boolean isExist(String fileName){
		File f = new File(fileName);
		return f.exists();
	}
	/**
	 * Delete a created file
	 * @param file_name
	 * @throws Exception
	 */		
	public static void delete_file(String file_name) throws Exception{
		File file = new File(file_name);
		if (file.exists() && file.isFile()) file.delete();
		else if(file.isDirectory()){
			for(File f : file.listFiles()){
				delete_file(f.getAbsolutePath());
			}
			file.delete();
		}
	}
	
	/**
	 * return the filename without extention
	 * @param file
	 * @return
	 */
	
	 private static String getFileNameWithoutExtension(File file) {
	        String fileName = "";
	 
	        try {
	            if (file != null && file.exists()) {
	                String name = file.getName();
	                fileName = name.replaceFirst("[.][^.]+$", "");
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            fileName = "";
	        }
	 
	        return fileName;
	 
	    }
	 
	 
	 public static void compare_TAD() throws Exception {
		 
		 
		  /* %--------------------------------------------------------------------------
		    % Case 1:
		    % Finds the Number of exact TD between ClusterTD and Method-2
		    % For each TD in method A find ones that have same start and end border
		    %--------------------------------------------------------------------------
		  */
		 System.out.println(String.format("The Total number of TADs in Method-1 = %d \n", Method1_B1.size()));
		 log_outputWriter.write(String.format("The Total number of TADs in Method-1 = %d \n", Method1_B1.size()));
		 
		 int eqcount = 0;
		 int subcount = 0; 
		 int confcount = 0;
		 int newcount = 0;
		 int len = Method1_B1.size();
		 for (int i = 0; i < Method1_B1.size(); i++) {
			 //CASE 1
			 // Get the index where Start border (border 1) exist 
			 Object b1 = Method1_B1.get(i); //column 1 is the start border
			 boolean cont = Method2_B1.contains(b1);
			 // if found
			 if (cont) {
				 int cont_ind = Method2_B1.indexOf(b1);
				 // find if the border 2 is the same
				 Object b2 = Method1_B2.get(i); //column 1 is the start border
				 Object val=  Method2_B2.get(cont_ind );
				 if ( b2.equals(val)) { // if the same increase count
					 eqcount++;
			 }
				 else
					 continue;
			 }
			 
			 if (Parameter.stoprunning) { 
			    	
		    	 break; 
		     }
			
		 }
		 
		 	System.out.println(String.format("The Number of equal TD = %d \n",eqcount));
		 	 log_outputWriter.write(String.format("Case 1 = %d \n", eqcount));	 
		 
		 	
		 	 //CASE 2   //CASE 3
			for (int i = 0; i < Method1_B1.size(); i++) {
				int index = 0;
				 Object M1_b1 = Method1_B1.get(i); // column 1 is the start border
			 	 Object  M1_b2 = Method1_B2.get(i); // column 2 is the end border 
			 	 
			 	ArrayList findB1 = new ArrayList();
				ArrayList findB2 = new ArrayList();
				 for (int j = 0; j < Method2_B1.size(); j++) {
					if ( (int) M1_b1 >= (int) Method2_B1.get(j)) {
						findB1.add(j);
					}
					if ( (int) M1_b2 <= (int) Method2_B2.get(j)) {
						findB2.add(j);
					}				 		
			     }
				 
				 int begin_boundary = 0, close_boundary = 1;
				 if (!findB1.isEmpty() && !findB2.isEmpty()) {
					 begin_boundary = (int) findB1.get(findB1.size()-1);
					 close_boundary = (int) findB2.get(0);
				 }else
				 {
					 continue ;
				 }
				 
				 findB1.clear(); findB2.clear();
				 if( begin_boundary==close_boundary) {
			            // this means its the same TD
					 index = 1;
				 }
				 else
				 {
			            confcount = confcount + 1; //if it starts in a TD and ends in another TD
				 }
				 
				 // Find the TAD to check out if it inside a other TD
			      
			        if(index==1) {
			        	
			            subcount = subcount + 1;
			        }
			        
			        if (Parameter.stoprunning) { 
				    	
				    	 break; 
				     }
				 
		 	 }
			
			 // Actual No of SubTD = count - eqcount:: because count includes those from exact same TD
			
			int  no_SubTD = subcount - eqcount;
			System.out.println(String.format("The Number of TDs with SubTD = %d\n",no_SubTD));
			
			System.out.println(String.format("The No of TDs with Conflicting TD btw methods = %d\n",confcount));
			
			 log_outputWriter.write(String.format("CASE 2 =  %d\n",no_SubTD));
			
			log_outputWriter.write(String.format("CASE 3  = %d\n",confcount));
			
			
			//CASE 4
			
			
			 newcount = len-eqcount-no_SubTD-confcount;
			 
			 System.out.println(String.format("The No of Unique TDs = %d\n",newcount));
			
			log_outputWriter.write(String.format("CASE 4 =  %d\n",newcount));
			
			//Calculate the Total Recall percentage
			int sumfound = eqcount + no_SubTD;
			
			double per1 = (sumfound/(double) len) ;
					per1 = per1	*  100.0;
			
			 System.out.println(String.format(" Total Recall  =  %f%%\n",per1));
			log_outputWriter.write(String.format("Total Recall  =  %f%%\n",per1));
			
	 }
	
	 /**
	  * Function to format report 
	  * @param fileA
	  * @param fileB
	 * @throws Exception 
	  */
	public  TADComparison (String[] args) throws Exception {
		
		String fileA  = args[0];
		String fileB  = args[1];		
		Resolution = Integer.parseInt(args[2]);
		Outputpath =  args[3];	
	    
		
		
		Method1 = readTADFile(fileA,1);
		Method2= readTADFile(fileB,2);
		String fileNameWithOutExt1 = getFileNameWithoutExtension(new File(fileA));
		String fileNameWithOutExt2 = getFileNameWithoutExtension(new File(fileB));
		filename = "TAD_Comprison_"   + fileNameWithOutExt1 + "_Report.txt";
		
		if (isExist(filename)) {
			delete_file(filename);
		}
		log_outputWriter = new BufferedWriter(new FileWriter( Outputpath + "/" + filename)); 
		String title=  "COMPARISON REPORT\n\n";
		log_outputWriter.write(title);
		
		log_outputWriter.write("----------------------------------------------------------------------\n");
		log_outputWriter.write("Objective: Check the consistency of TAD from Method-1 with Method-2\n");
		log_outputWriter.write("======================================================================\n");
	    log_outputWriter.write(String.format("Method-1: %s\n",fileA));
	    log_outputWriter.write(String.format("Method-1: %s\n\n",fileB));
	    log_outputWriter.write("Case 1: The number of Exact TADs found in Method-1 and Method-2 \n"
	    		+ "Case 2: The number of Sub-TADs that exist between Method-1 and Method-2 \n"
	    		+ "Case 3: The number of Conflicting TADs.\n"
	    		+ "Case 4: The number of New TADs not found in Method-2 \n"
	    		+ "=======================================================================\n");
	    
	    
	    compare_TAD(); 		
	    
	    log_outputWriter.flush();
		log_outputWriter.close();
		 Method1_B1.clear();
		 Method1_B2.clear();
		 Method2_B1 .clear();
		 Method2_B2.clear();
		
	   
	}


	
	
	
}
