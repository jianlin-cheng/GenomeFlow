package edu.missouri.chenglab.ClusterTAD;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

import org.jmol.api.JmolViewer;

public class Parameter {
	public static String inputtype_Matrix=  "1"; 
	public static String inputtype_Tuple=  "0"; 
	public static boolean stoprunning = false;
	
	

	   /**
	    *  Read the matrix input from file
	    * @param rows
	    * @param cols
	    * @param Filename
	    * @param sep
	    * @return
	    */
		public static double[][] readMatrix(int rows, int cols, String Filename, String sep){
		// read in the data
		double [][] a  = new double[rows][cols];
		 try {
			 int linesCounter = 0;
			 Scanner input = new Scanner (new File( Filename));
		 
				while(input.hasNextLine())
				{
					String [] line = null;
					String rowdata = input.nextLine();
					line = rowdata.split(sep);						
					 for (int k = 0; k < cols; k++) {							  
		                	a[linesCounter][k] = Double.parseDouble(line[k]);
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
	 * 
	 * read tuple input 
	 * @param Filename
	 * @param sep
	 * @return
	 * @throws FileNotFoundException
	 *  */
		
	
	public static double [][] readTupleFile(String Filename, String sep) throws FileNotFoundException{	
		
		//create Hashmap
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		//store index on hashmap: 
		int max = 0;	
		int count = 0;
		try {
			 Scanner input = new Scanner (new File( Filename));			 
				while(input.hasNextLine())
				{	String [] line = null;
					String rowdata = input.nextLine();
					if(!rowdata.isEmpty()) {
					line = rowdata.split(sep);
					for (int i= 0; i<2;i++) {
						int key = Integer.parseInt(line[i]);
						if (map.containsKey(key)) {
							//key exists
							continue;
						} else {
							Object value = map.get(key);
							if (value == null) {
							    value = max++;		
								map.put(key, (Integer) value);
								count =  count + 1;
							}														
						}							
					}
					}
				}			
				input.close();
		 	} catch (FileNotFoundException e) {
	         e.printStackTrace();
		 	}	
		System.out.println(String.format("The total number of unique element = %d", count));
		// Once the number of element is obtained, get the total number of elements, create a matrix with them	
		 double [][] a = new double[count][count];
		 for (int i = 0; i<max;i++) {
				for (int j = 0; j<max;j++) {
					a[i][j] = 0;
			}
		}
		
		// For each key get value
		 try {				
			 Scanner input = new Scanner (new File( Filename));
		 
				while(input.hasNextLine())
				{
					String [] line = null;
					String rowdata = input.nextLine();
					if(!rowdata.isEmpty()) {
						line = rowdata.split(sep);						
						// Find the key value from map
						int indexkey0 = Integer.parseInt(line[0]); int indexvalue0 = map.get(indexkey0);
						int indexkey1 = Integer.parseInt(line[1]); int indexvalue1 = map.get(indexkey1);
						
						System.out.println(String.format("%d %d %f",  indexkey0, indexkey1 ,Double.parseDouble(line[2]) ));
						
						if (Double.isNaN( Double.parseDouble(line[2]))) {
							a[indexvalue0][indexvalue1] = 0;
							a[indexvalue1][indexvalue0] = 0;
						}
						else {
							a[indexvalue0][indexvalue1] = Double.parseDouble(line[2]) ;					 
							a[indexvalue1][indexvalue0] = Double.parseDouble(line[2]) ;
						}
					}
				
	             }			
			
				input.close();
		 	} catch (FileNotFoundException e) {
		 		System.out.println("There is a line with a wrong Format");
	         e.printStackTrace();
		 	}
		 
							
		return a;
		
	}
	
	
	
	/**
	 *  Read matrix from file		
	 * @param Filename
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static double [][] readFile(String Filename, String sep, int ismatrix) throws FileNotFoundException{	
		//#######################################################################
		// Detect To determine if it is a Matrix or Tuple. Greater than 3 cols
		//#######################################################################
		
		// pre-read in the number of rows/columns
		int rows = 0;
		int cols = 0;	
		double [][] matrix= null;			
		 
		 System.out.println(String.format("Number of rows = %d", rows));
		 //===================================
		 // Read in Matrix input directly
		 //===================================		 
		 if (ismatrix == 1) {
			 try {
				 Scanner input = new Scanner (new File( Filename));
			 
					while(input.hasNextLine())
					{	String [] line = null;
						String rowdata = input.nextLine();					
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
			 System.out.println(String.format("Number of row/col = %d", rows));
			 matrix = readMatrix( rows, cols, Filename,  sep);
		 }				
		 //===================================
		 // Convert Tuple to a Matrix
		 //===================================	
		 else {
			
			 matrix =  readTupleFile(Filename,sep);
		 }
		 
		 
		 return matrix;
	}
}


