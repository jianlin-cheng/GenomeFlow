package edu.missouri.chenglab.struct3DMax;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.util.Arrays;
import java.util.Date;
import java.util.Collections;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import edu.missouri.chenglab.gmol.Constants;
import edu.missouri.chenglab.gmol.filemodification.ConvertPDB2GSS;
import java.util.HashMap;
import java.util.Map;
import edu.missouri.chenglab.lordg.utility.Helper;
import org.jmol.api.JmolViewer;

/** ------------------------------------------------------------------------------------------------------------------
 * start of Structure_3DMax class
 * ------------------------------------------------------------------------------------------------------------------  **/


public class Structure_3DMax {		
	
	/*global variables*/	
	static int max_iteration=2000;
	final static List<Double> list = new ArrayList<Double>();
	final static String out1 = "output_Spearman_Correlation.txt"; // stores the spearman correlation value for genome or chromosome
	final static String out2 = "output_report.log"; // reports the file containing max score	
	final static SpearmansCorrelation SC = new SpearmansCorrelation();
	static double initial_lrate = 0.01;
	static BufferedWriter log_outputWriter = null;
	static String outpath = null ; 
	static String outputname = null ;
	static int NN = 1; //number of structures per alpha
	static double min = 0.1,max = 2; // conversion factor								
	static String inputfile = null; // input file 
	static int Resolution = 1000;
	static String FileName = "";
	static String display_path = null;
	static int Factor = 15; // zoom the structure
	static String Header = "3D chromosome/genome modelling by 3DMax"; //header for pdb file
	static Outputwriter OutputStructure = new Outputwriter(); //class to output the .gss and .pdb structure 
	private static boolean global_isStopRunning = false;
	
	/*  end of global variables  */
	
		 /**
		  * calculate the Euclidean distance	
		  * @param x1
		  * @param y1
		  * @param z1
		  * @param x2
		  * @param y2
		  * @param z2
		  * @return
		  */
 		public static double calEuclidianDist(double x1, double y1, double z1, double x2, double y2, double z2){
			double diff = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2);
			return Math.sqrt(diff);
		}
	
		
		/**
		 * calculate the distance from XYZ coordinates	
		 * @param n
		 * @param xyz
		 * @return
		 */
		public static double[][] distmatrix (int n,double [][]xyz){
			double [][] Dist =new double[n][n];
			for (int i = 0; i<n;i++) {
				for (int j = 0; j<n;j++) {
					Dist[i][j] = Structure_3DMax.calEuclidianDist(xyz[i][0], xyz[i][1], xyz[i][2], xyz[j][0],
							xyz[j][1], xyz[j][2]); //calculate the distance between regions i and j	
				}
			}
				
			return Dist;
			
		}
		
	
		
		/**
		 * Objective function with no sigma dependence
		 * @param n
		 * @param dist
		 * @param xyz
		 * @return
		 */
		public static double obj_function(int n, double [][] dist, double [][]xyz) {
			double func_val = 0;
			for (int i = 0; i<n;i++) {
				for (int j = i+1; j<n;j++) {
					if(dist[i][j]!=0) {
						double euc_dist = Structure_3DMax .calEuclidianDist(xyz[i][0], xyz[i][1], xyz[i][2], xyz[j][0],
								xyz[j][1], xyz[j][2]); //calculate the distance between regions i and j	
						double diff = euc_dist - dist[i][j];
						func_val = func_val + 	Math.pow(diff, 2);
					}
				}
			}
			
			func_val = -n/2 - (n*Math.log(Math.sqrt(func_val/n))); // Update value of objective function
			return func_val;
		}
		
		/**
		 * determine if program should stop running
		 * @return
		 */
		public boolean isStopRunning() {
			return global_isStopRunning;
		}
		/**
		 * Set the program Status
		 * @param isStopRunning
		 */
		public static void setStopRunning(boolean isStopRunning) {
			global_isStopRunning = isStopRunning;
		}
		
		/**
		 * 3DMax algorithm	
		 * @param n
		 * @param alpha
		 * @param contact
		 * @param dist
		 * @return
		 */
		public static double[][] Algorithm_3DMax( int n,double alpha,double[][] contact,double [][] dist, JmolViewer viewer, double found_alpha, double found_corr )
		{
			/*====================================================================			
			 *  Variables declaration
			 *  ====================================================================			
			 *  n = The length of the contact matrix
			 *  alpha = conversion factor
			 *  contact = input Normalized contact matrix			 * 
			 *  sigma = variance
			 *  epsilon = To signify the boundary of convergence
			 *  step = Step of gradient descent
			 *  count = number of iterations for optimization
			 *  it_EM = number of iterations for EM Algorithm
			 *  gradient_x, double gradient_y , double gradient_z = Gradient for X,Y,Z respectively
			 *  XYZ, temp_XYZ = XYZ coordinates for regions
			 *  obj_val_old,obj_val_new = objective function value old and new respectively
			 *  return::: The optimized X,Y,Z coordinates
			 */
						
			
			
			double [][] XYZ = new double[n][3];
			double sigma = Math.random();
			double epsilon =0.00001; 
		    double step= 0.0001; 			
			int it_EM = 1; 
			double obj_val_old = 0,obj_val_new = 0;			
			double gradient_x, gradient_y , gradient_z;
			
			/*====================================================================			
			* INITIALIZATION
			* ====================================================================	*/
		
			
			// Randomly initialize in the range -0.5 to 0.5			
			double min = -0.5;
			double max = 0.5;			
			for (int i = 0; i<n;i++) {
				for (int j = 0; j<3;j++) {
					double rand = new Random().nextDouble(); 
					XYZ[i][j] = min + (rand * (max-min));
					XYZ[i][j] = XYZ[i][j] ;
			 }
			}
			
			// Calculate the objective function value
			obj_val_new = Structure_3DMax .obj_function(n, dist, XYZ);
			//System.out.println(String.format("Objective function: %10.10f",obj_val_old));
			
			int epoch = 0;
			while(it_EM < 20) {				
				//========= decreasing learning rate=======						
				step = LearningRate(epoch);  //comment out to use constant learning rate
				
				System.out.print(String.format("epoch: %d step : %1.10f \t ",it_EM,step));			   
				
				int count=0; 				
				while(count < max_iteration) {
					obj_val_old = obj_val_new; // update the objective function
					double v = 0;
					for (int i = 0; i < n ;i++) {						
						for (int j = 0; j < n ;j++) {
							if(i!=j && contact[i][j]!=0) {
								double euc_dist = Structure_3DMax .calEuclidianDist(XYZ[i][0], XYZ[i][1], XYZ[i][2], XYZ[j][0],
										XYZ[j][1], XYZ[j][2]); //calculate the distance between regions i and j		
								double diff = euc_dist - dist[i][j];
								double var = Math.pow(diff,2);
								v = v + var;
							}
						}
					}
					
					// calculate w
						double w = Math.sqrt(v/n);
					//Find dl_dw
						double dl_dw = -n/w;
					//Find dw_dv
						double dw_dv = 1/(2*Math.sqrt(n*v));
					
					for (int i = 0; i < n ;i++) {
						gradient_x = 0; gradient_y = 0; gradient_z = 0;						
						// calculate gradient
						for (int j = 0; j < n ;j++) {
							if(i!=j && contact[i][j]!=0) {
								double euc_dist = Structure_3DMax .calEuclidianDist(XYZ[i][0], XYZ[i][1], XYZ[i][2], XYZ[j][0],
										XYZ[j][1], XYZ[j][2]); //calculate the distance between regions i and j		
								double diff = euc_dist - dist[i][j];
								double dv_dx  =  ((XYZ[i][0]-XYZ[j][0]) * diff)/euc_dist ; // for dv/dx
								double dv_dy   = ((XYZ[i][1]-XYZ[j][1]) * diff)/euc_dist ; // for dv/dy
								double dv_dz   = ((XYZ[i][2]-XYZ[j][2]) * diff)/euc_dist ; // for dv/dz
								gradient_x= gradient_x + (2*dl_dw*dw_dv*dv_dx);
								gradient_y= gradient_y + (2*dl_dw*dw_dv*dv_dy);
								gradient_z= gradient_z + (2*dl_dw*dw_dv*dv_dz);
							}
						}
						
						// Gradient ascent
						double [] gradient = new double [3];
						gradient[0] = gradient_x; gradient[1] = gradient_y; gradient[2] = gradient_z;
					   
						
											
						for (int k = 0; k < 3; k ++) {
							XYZ[i][k] = XYZ[i][k] + (step * gradient[k]);
						}						
					} // end of for loop
					
					// Update the objective function score using new coordinates but old variance(sigma)
					obj_val_new = Structure_3DMax .obj_function(n, dist, XYZ);
					//System.out.println(String.format("New Objective function: %f",obj_val_new));
					
				    //===== Test for Algorithm convergence ======
					if (Math.abs(obj_val_new  - obj_val_old) < epsilon) {
						//System.out.println(String.format("Converged !!!"));
						break;
					}
					
					
					count++; 
					epoch++; // The number of epoch
					/*========================================================
					 * 				Visualization 
					 * =======================================================					 * 
					 */
					 String outputFilePDB =display_path + "Iteration_" + Integer.toString(count) +".pdb";
					//write file to directory
					 try {					
						 
						OutputStructure.writeStructurePDB(outputFilePDB, XYZ, Header) ; // Output pdb structure   
						viewer.loadNewModel(outputFilePDB, new String[]{"Searching for best Conversion factor..... ","Conversion Factor: " + String.format("%.2f", alpha), 
								"Objective Function: " + String.format("%.2f",obj_val_new), "Best alpha found: " + String.format("%.2f",found_alpha),"Correlation of Best alpha found: " + String.format("%.2f",found_corr) });	
													
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
					// delete created file
					try {
						OutputStructure.delete_file(outputFilePDB);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					
					
					
				} 
			    
				if (Math.abs(obj_val_new  - obj_val_old) < epsilon) {
					//System.out.println(String.format("Converged !!!"));
					//System.out.println(String.format("number of iterations::%d !!!",count));
					break;
				} 
				
				
				it_EM++;
			}
			
			/*================================================================
			 * Return the  optimizzed XYZ coordinates
			 =================================================================*/
			//Find Nan in the XYZ coordinate
			
			for (int i = 0; i<n;i++) {
				for (int j = 0; j<3;j++) {
					if(Double.isNaN(XYZ[i][j])){
						System.out.println(String.format("Nan found, function called again !!!"));
						XYZ = Structure_3DMax .Algorithm_3DMax(n,alpha, contact,dist,viewer,found_alpha,found_corr);	
					}					
			 }
			}
			
			return XYZ;
			
		}
		
		
		/**
		 * Convert 2D array to 1D array	
		 * @param arr
		 * @return
		 */
		public static double[] toDoubleArray(double[][] arr){
			 List<Double> list = new ArrayList<Double>();
			    for (int i = 0; i < arr.length; i++) {
			       
			        for (int j = 0; j < arr[i].length; j++) { 
			            //  actually store the values
			            list.add(arr[i][j]); 
			        }
			    }
			          // new 1D dimension
			    double[] vector = new  double[list.size()];
			    for (int i = 0; i < vector.length; i++) {
			        vector[i] = list.get(i);
			    }
			    return vector;
		}

		/**
		 *  Write to File
		 * @param filename
		 * @param x
		 * @throws IOException
		 */
		public static void write (String filename, double[][]x) throws IOException{
			  BufferedWriter outputWriter = null;
			  outputWriter = new BufferedWriter(new FileWriter(filename));
			  int row = x.length;  int col = x[0].length;
			  for (int i = 0; i < row; i++) {
				  for (int j = 0; j < col; j++) {
					outputWriter.write(x[i][j] +"\t");
				  }
				  outputWriter.write("\n");
			  }
			  outputWriter.flush();  
			  outputWriter.close();  
			}
		
		/**
		 * Learning decreasing learning rate implementation
		 * @param epoch
		 * @return
		 */
		public static double  LearningRate(int epoch) {			
			double drop = 0.5;
			double epochs_drop = 70.0;
			double fract = Math.floor((1+epoch)/epochs_drop);
			double step = initial_lrate *Math.pow(drop, fract);
			if (step < 0.0000001) {
				step = 0.0000001;
			}
			return step;
		}
		
		/**
		 * source is the array to copy
		 * destination is the new array created
		 * @param aSource
		 * @param aDestination
		 */		
		 public static void arrayCopy(double[][] aSource, double[][] aDestination) {
			    for (int i = 0; i < aSource.length; i++) {
			        System.arraycopy(aSource[i], 0, aDestination[i], 0, aSource[i].length);
			    }
			}
		
		
		/**
		 * This controls the 3DMax Algorithm
		 * @param name
		 * @param contact
		 */
		public static void Control_3DMax(double[][] contact,double min, double max, int N,JmolViewer viewer) {
			
			/*
			 *  Input: contact = The Normalized contact matrix
			 *  dist = distance matrix
			 *  Conversion factor (alpha) is from range min to max
			 *  N = number of structures per alpha
			 *  min and max = minimum and maximum conversion factors
			 */
			
			
			String name = outpath + "/" + outputname  ;		     
			String struct_name = null;	
			int n = contact.length; // Length of the contact Matrix [rows]			
			double [][] D_Matrix = new double[n][n];
			double [][] Gen_D_Matrix = new double[n][n];
			double [][] XYZ = new double[n][3];	// Returned coordinate using alpha value	
			double [][] Max_XYZ = new double[n][3];	// Returned coordinate using alpha value	
			ArrayList<Double> Corr_List = new ArrayList<Double>(); // List of correlation score between target and expected distance structure
			double Max_score =  -1; // Maximum correlation score
			String Max_name = "";
			double sel_alpha = min;
			String sel_name = "";
			
			
			for (double alpha = min; alpha<=max; alpha+=0.1) {
				System.out.println(String.format("Conversion Factor = %f",alpha));
						
				// Convert contact matrix to distance matrix using current alpha
				for (int i = 0; i<n;i++) {
					for (int j = 0; j<n;j++) {
						if (contact[i][j]!=0) {
							D_Matrix[i][j] = 1/Math.pow(contact[i][j], alpha);
						}
						else {
							D_Matrix[i][j] = 0.0;
						}
					}								
				}
				 
				
				
				double[] xArray = toDoubleArray(D_Matrix); //=====Convert 2D to 1D array====
				/*================================================================
				 * For each alpha generate N structures for each alpha
				 *===============================================================*/
				for (int j = 0; j< N; j++) {	
					System.out.println(String.format("Creating structure at alpha = %f and structure = %d.......",alpha,j));
					// ====== Call the EM Algorithm functn =======	
				
					XYZ = Structure_3DMax .Algorithm_3DMax(n,alpha, contact,D_Matrix,viewer,sel_alpha,Max_score);
					// ===Find the distance matrix from XYZ coordinates =====
					Gen_D_Matrix = Structure_3DMax .distmatrix (n,XYZ);
					struct_name = name + "_alpha="+ Double.toString(alpha) ;
					//=====Convert 2D to 1D array====					 
					 double[] yArray = toDoubleArray(Gen_D_Matrix);
					//====== Scoring of generated structure {spearman correlation}=====					 
					double corr =   SC.correlation(xArray,yArray); // return the spearman Correlation	
				
					System.out.println(String.format("Finished :::: Spearman corr = %f", corr));
					if(corr > Max_score) {
						Max_score = corr;
						Max_name = struct_name;
						sel_alpha = alpha;
						sel_name = outputname + "_alpha="+ Double.toString(alpha) ;
						arrayCopy(XYZ,Max_XYZ);
					}
					// ====== output the display structure, display and delete======
					String outputFilePDB =display_path + outputname + "_alpha="+ Double.toString(alpha) +".pdb";
					try {
						if (viewer!=null) {
						OutputStructure.writeStructurePDB(outputFilePDB, XYZ, Header) ; // Output pdb structure   
						viewer.loadNewModel(outputFilePDB, new String[]{"Conversion Factor: " + String.format("%.2f",alpha), 
								"Correlation: " + String.format("%.2f", corr)});
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Corr_List.add(corr);
					
					// delete created file
					try {
						OutputStructure.delete_file(outputFilePDB);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					
					
				} // end of N loop
				
			}// end of alpha loop
			
			/*================================================================
			 * Select the Representative model for chromosome
			 *===============================================================*/
			String msg = "The representative structure is " + Max_name + " with spearman Correlation = %f";
			System.out.println(String.format(msg, Max_score));
			list.add(Max_score);
			
			// ====== output the structure coordinate======
			try {
				String  fname = Max_name + ".xyz";							
				Structure_3DMax.write(fname,Max_XYZ); // Output XYZ Coordinate  
				// Generate the .gss and .pdb file here
				HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
				//create a mapping coordinate
				for (int value =0; value<n;value++) {
					Integer key = Resolution + (value * Resolution);			
					map.put(key, value);
				}
				
				// Output the .gss file
				String outputFile = Max_name + ".gss";
				try {
					OutputStructure.writeStructureGSS(Max_XYZ, map, outputFile,Resolution);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
				//output the .pdb file
				outputFile = Max_name + ".pdb";
				OutputStructure.writeStructurePDB(outputFile, XYZ, Header) ; // Output pdb structure   				
				
				// display the final model
				if (viewer!=null) {					
					viewer.loadNewModel(outputFile, new String[]{"Search Completed!","Selected Model: " + String.format("%s",sel_name), 
							"Correlation: " + String.format("%.2f", Max_score), "Alpha: " + String.format("%.2f", sel_alpha)});
					}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {								
			
				log_outputWriter.write(String.format("Reprsentative Structure : %s\n",Max_name));
				log_outputWriter.write(String.format("Spearman Correlation : %f \n\n", Max_score));				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	  
		
		
		/**
		 *  Read matrix from file		
		 * @param Filename
		 * @return
		 * @throws FileNotFoundException 
		 */
		public static double [][] readFile(String Filename, String sep, JmolViewer viewer) throws FileNotFoundException{	
			
			//Accept Input Data				
			viewer.displayMessage(new String[]{"Reading input data ..."});
			//#######################################################################
			// Detect To determine if it is a Matrix or Tuple. Greater than 3 cols
			//#######################################################################
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
		 * 
		 * @param filename
		 * @throws IOException
		 */
		
		public static void writescores (String filename) throws IOException{
			  BufferedWriter outputWriter = null;
			  outputWriter = new BufferedWriter(new FileWriter(filename));		  
			  
				  for (int j = 0; j < list.size(); j++) {
					String Sval = String.valueOf(list.get(j));
					outputWriter.write( Sval);
					outputWriter.write("\n");
				  }
				 
			  
			  outputWriter.flush();  
			  outputWriter.close();  
			}
		
				
		
		
		/**
		 * accept Input
		 * @param parameters
		 */

		public Structure_3DMax(String[] args, JmolViewer viewer){
			
			long startTime = System.nanoTime();
		    double [][] Data = null;
		    
			// ==========PARAMETERS==================		 
			inputfile = args[0];
			outpath = args[1];
			min = Double.parseDouble(args[2]);
			max = Double.parseDouble(args[3]);
		    initial_lrate = Double.parseDouble(args[4]);
		    max_iteration = Integer.parseInt(args[5]);
			Resolution = Integer.parseInt(args[6]);
			
			
			 String[] tmp = inputfile.split("[\\/ \\. \\\\]");
			 if (inputfile.contains(".")){
				 FileName = tmp[tmp.length - 2];
			   }
			  else{
					
				FileName = tmp[tmp.length - 1];
			}
			//====== Write Result log to file ========
		    
		     
		     String logfile = outpath  + "/" +  FileName  + "_" + out2 ;
			try {
				
				log_outputWriter = new BufferedWriter(new FileWriter( logfile ));
				log_outputWriter.write(String.format("NOTE: alpha in the output Representative structure name is the Conversion factor used to convert the input IF to its distance equivalent\n\n"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
		    }					     
		    		
			System.out.println(String.format("Input = %s", inputfile));
		      try {
		    	 System.out.println("Reading Data from File......");
		    	 String seperator = ","; // specify the data seperator for dataset
				Data = readFile(inputfile,seperator,viewer);
			    System.out.println("File read successfully!!!\n");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	      // Call the control Algorithm
		     System.out.println("Structure Generation Started ........");
		     
		     try {	
					log_outputWriter.write(String.format("Input : %s\n", inputfile));
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		     
		     outputname = "Structure_"+ FileName + "_" + new Date().getTime();
		     
		     // Create a folder
			display_path = outpath + "/tmp_3DMax/";
			File dir = new File(display_path);
			dir.mkdir();
				
		    // Run 3DMax Control Algorithm
		    Structure_3DMax.Control_3DMax(Data,min,max,NN,viewer);	 
			 
		    JOptionPane.showMessageDialog(null, "Successfully Completed!, Check output directory for output files");
			 
			 
			 System.out.println("3DMax Completed Successfully........");
		     long stopTime = System.nanoTime();
		     long elapsedTime = stopTime - startTime;
		     //convert to seconds 
		     double seconds = (double)elapsedTime / 1000000000.0	;     
		     System.out.println(String.format("Time elapsed = %f seconds", seconds) ); 	
		     
		     try {
		    	 
					//writescores( outpathname + inputfile + "_"+ out1);
					log_outputWriter.flush();  
					log_outputWriter.close(); 
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}



} /*  end of 3DMax class  */
