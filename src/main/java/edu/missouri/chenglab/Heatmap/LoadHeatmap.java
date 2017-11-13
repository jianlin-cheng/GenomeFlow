package edu.missouri.chenglab.Heatmap;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;


public class LoadHeatmap {

	public static int Startlocation;
	public static int Endlocation;
	static String sep = "\\s+";
    static HashMap<Integer, Integer> map =  new HashMap<Integer, Integer>();
    static HashMap<Integer, Integer> mapthekey =  new HashMap<Integer, Integer>(); 
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
	 * Create a mapping Hashmap from Input file
	 * @param Filename
	 * @return
	 */
	
	public static Map<Integer, Integer> Createmap(String Filename) {
		List<Integer> list = new ArrayList<Integer>();
		
		
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
							map.put(key, count); // Genome location and index
						    count =  count + 1;
																			
						}							
					}
					}
				}			
				input.close();
		 	} catch (FileNotFoundException e) {
	         e.printStackTrace();
		 	}	
		System.out.println(String.format("The total number of unique element = %d", count));
		
		// Copy the key into a list and order.
		list = new ArrayList<>(map.keySet());
		Collections.sort(list);
		// copy the key into a map
		map.clear();
		for (int i = 0;i<list.size();i++) {
			int value = list.get(i);
			map.put(i,value);
			mapthekey.put(value, i);
		}
		
		Startlocation = list.get(0);	
		Endlocation = list.get(list.size()-1);
			
		return map;
	}
	/**
	 * Given a mapping, detect the resolution, it is the gap repeated the most in the map
	 * @param map
	 * @return
	 */
	private static int detectResolution(Map<Integer, Integer> map){
		int max = 0, res = 0, g, n = map.size();
		Map<Integer, Integer> count = new HashMap<Integer, Integer>();
		for(int i = 1; i < n; i++){
			g = map.get(i) - map.get(i - 1);
			if (!count.containsKey(g)) count.put(g,  0);
			count.put(g,  count.get(g) + 1);
			if (count.get(g) > max){
				max = count.get(g);
				res = g;
			}
		}
		
		return res;	
		
	}
	
	
	/**
	 * Find the index position given the genome location
	 * @param min_x
	 * @param max_x
	 * @param min_y
	 * @param max_y
	 * @param loc
	 * @return
	 */
	public static int [] FindIndex( double min_x, double max_x,double min_y, double max_y, int loc) {
		int min_row = (int) (min_x * loc);
		int max_row = (int) (max_x * loc);
		int min_col = (int) (min_y * loc);
		int max_col = (int) (max_y * loc);
		
		int [] values  = new int [4];
		values[0] = mapthekey.get(min_row );
		values[1] = mapthekey.get(max_row );
		values[2] = mapthekey.get(min_col );
		values[3] = mapthekey.get(max_col );
		
		return values;
		
	}
	
	
	
	
	/**
	 * Returns the Resolution
	 * @param Filename
	 * @return
	 * @throws Exception 
	 */
	public static int Resolution(String Filename) throws Exception {
		
		Map<Integer, Integer> map =  Createmap(Filename);
		int res = detectResolution(map);
		return res;
	}
	

	
	
	/**
	 *  Read matrix from file		
	 * @param Filename
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static double [][] readFile(String Filename, String sep) throws FileNotFoundException{			
		// pre-read in the number of rows/columns
		int rows = 0;
		int cols = 0;		
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
		 // read in the data
		 double [][] a = new double[rows][cols];
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
	 * Read TAD File 
	 * @param Filename
	 * @param sep
	 * @return
	 * @throws FileNotFoundException
	 */
	public static  int [][] readTADFile (String Filename, String sep) throws FileNotFoundException{			
		// pre-read in the number of rows/columns
				int rows = 0;
				int cols = 0;		
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
				 // read in the data
				 int [][] a = new int[rows][cols];
				 try {
					 int linesCounter = 0;
					 Scanner input = new Scanner (new File( Filename));
				 
						while(input.hasNextLine())
						{
							String [] line = null;
							String rowdata = input.nextLine();
							line = rowdata.split(sep);						
							 for (int k = 0; k < cols; k++) {							  
				                	a[linesCounter][k] = Integer.parseInt(line[k]);
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
	 * Performs the Pearson correlation, and gets the result
	 * @param data
	 * @return
	 * @throws FileNotFoundException
	 */
	public static double [][] Pearson (double [][] data) throws FileNotFoundException{
		double [][] Data = null;
		PearsonsCorrelation pearson = new PearsonsCorrelation();
		Data = pearson.computeCorrelationMatrix(data).getData();		
		return Data;	
	
	}
	
	/**
	 * Performs the Spearman correlation, and gets the result
	 * @param data
	 * @return
	 * @throws FileNotFoundException
	 */
	public static double [][] Spearman (double [][] data) throws FileNotFoundException{
		double [][] Data = null;
		SpearmansCorrelation spear = new SpearmansCorrelation();
		Data = spear.computeCorrelationMatrix(data).getData();		
		return Data;	
	
	}
	
	
	
	/**
	 * Performs the Tanh, and gets the result
	 * @param data
	 * @return
	 * @throws FileNotFoundException
	 */
	public static double [][] Tanh (double [][] data) throws FileNotFoundException{
		int row = data.length;
		int col = data[0].length;
		double [][] Data = new double[row][col];
		double x;
		for (int i=0;i <row; i++) {
			for  (int j=0;j <col; j++) {
				// convert them in radians
				x = Math.toRadians(data[i][j]);
				//get the hyperbolic tangent of these doubles
				Data[i][j] = Math.tanh(x);
			}
		}
				
		return Data;	
	
	}
	
	/**
	 * @author Tosin
	 * Return the size of the matrix
	 * @param data 
	 * @return
	 */
	public static int [] MatSize(double [][] data) {
		
		int row = data.length-1;
		int col = data[0].length-1;
		int [] mat =  {row,col};
		return mat;
		
	}
	
	/**
	 * Return a Submatrix based on the specified data range
	 * @param data
	 * @param min_x
	 * @param max_x
	 * @param min_y
	 * @param max_y
	 * @return
	 * @throws FileNotFoundException
	 */
	public static double[][] MatrixReform(double [][] data, double min_x, double max_x,double min_y, double max_y)  throws FileNotFoundException{
		int min_row = (int) min_x;
		int max_row = (int) max_x;
		int min_col = (int) min_y;
		int max_col = (int) max_y;
		
		int x_size = max_row - min_row + 1;
		int y_size = max_col - min_col + 1;
		double [][] Data = new double[x_size][y_size];
		
		for (int i=min_row;i <= max_row; i++) {
			for  (int j=min_col;j <= max_col; j++) {
				//get the data to display
				int row_ind = i-min_row;
				int col_ind = j-min_col;
				Data[row_ind][col_ind] = data[i][j];
			}
		}
		
		
		return Data;
	}

	/**
	 * Returns a transformed verion of new dataset
	 * @param indata
	 * @return
	 * @throws FileNotFoundException 
	 */
	
	public static double[][] TransformData(double [][] indata, int transformint) throws FileNotFoundException{
		double [][] outdata = null;
    	switch ( transformint) {
    		case 1:
    			outdata = Pearson(indata);
    			break;
    		case 2:
    			outdata = Spearman(indata);
    			break;
    		case 3:
    			outdata = Tanh(indata);
    			break;
    		default:
    			outdata = indata;
    			break;
    	}
    	
    	return  outdata;
    }
 
	
	
	
	
}
