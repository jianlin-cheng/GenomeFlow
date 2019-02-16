package edu.missouri.chenglab.ClusterTAD;

import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.jmol.api.JmolViewer;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.CSVLoader;
import edu.missouri.chenglab.ClusterTAD.Parameter;

public class ClusterTAD{
	
	public static int nRegion; // number of region
	public static List<Double> list ;
	public static String name = "30x30_data";//nij19" ;//"30x30_data";//nij.chr20
	
	public static String inputfile = null; // input file 
	public static String outpath = null ; 
	public static String outputname = null ;
	public static int ismatrix = 1;
	public static int startloc = 0;
	public static String chromosome  = null; //  specify the Chromosome number
	
	JmolViewer viewer;
	
	public static String Outputpath;
	public static String Clusterpath = "Clusters/";
	public static String ClusterFolder;  
	public static String TADFolder;
	static BufferedWriter log_outputWriter = null;
	public static ArrayList <Integer> ZeroRows= null;
	static int min_TAD_size = 120000; //120kb
	static int max_TAD_size = 800000 ; //800KB
	public static int [][] Best_TAD = null;
	public static double Best_Qscore = -Integer.MAX_VALUE;
	public static  int Best_K = 0;
	public static String Bestname = null;;
	public static int Resolution = 40000; // 1000 = 1KB
	public static double [][] Feat = new double[nRegion][];
	public static int status = 0;
	public static double [][] RealData = null;
	public static String reclustername = null;
	public static String  TADfile = null;
	public static boolean global_isStopRunning = false;

	 // creates progress bar
    final static JProgressBar pb = new JProgressBar();
    final static int MAX = 150;
    final static JFrame frame = new JFrame("Processing data..... Please wait");
    static int current;
    static int limit;
    
	public static List<Double> Quality = new ArrayList<Double>();;
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
		 
		
		 nRegion = rows;
		
		 
		return a;
	}
	
	/**
	 * Return the data for a row
	 * @param mat
	 * @param row
	 * @param lencol
	 * @return
	 */
	public static void rowdata(double [][]mat, int row, int lencol ){		    
			for (int j = 0; j < nRegion ; j++){
				list.add( mat[row][j]);
			}			
	}
	/**
	 * Return the data for a col
	 * @param mat
	 * @param col
	 * @param lencol
	 */
	public static void coldata(double [][]mat, int col, int lencol){		 
			for (int j = 0; j < nRegion ; j++){
				list.add(mat[j][col]);
			}			
	}
	
	/**
	 * Create New dataset to use for Clustering
	 * @param mat
	 * @return
	 */
	public static double [][] CreateNewData(double [][]mat){
		FindZeroRows(mat); //Add zero rows to list
		System.out.println(String.format("The number of Zero Rows = %d ", ZeroRows.size()));
		//find the size of ZeroRows
		int size_Zero = ZeroRows.size();
		int nrows = nRegion-size_Zero;
		int ncols = nRegion + mat[0].length; // not necessarily 2*nRegion
		list = new ArrayList<Double>();
		int index = 0;
		double [][] Feature = new double[nrows][ncols];
		for (int diag = 0; diag < nRegion; diag++){		
			//check if arraylist contains index, if yes continue, otherwise create dataset
			if (ZeroRows.contains(diag)) {
				continue;
			}else {
				ClusterTAD.rowdata(mat, diag,ncols);
				ClusterTAD.coldata(mat, diag,ncols);
				for (int col_d = 0; col_d< ncols; col_d++){
					Feature[index][col_d] = list.get(col_d);
				}
				index++;
				list.clear();
			}
			
		}
			
	   
		return Feature; 
	}
	
	/**
	 * Elbow method determine the number of clusters
	 * @param len
	 * @return
	 * @throws IOException 
	 */	
	public static int RoughEstimate(int len) throws IOException{	
		
		double K = Math.sqrt(len/2);		
		int k_opt = (int) Math.round(K);
		System.out.println(String.format("Recommended Number of Cluster (K) = %d \n",k_opt));	    
	    log_outputWriter.write(String.format("Recommended Number of Cluster (K) = %d\n",k_opt));
	    	   
	    return k_opt;
	    
	   
	}
	
	/**
	 * Perform kmeans clustering using weka-kmeans algorithm
	 * @param data
	 * @return
	 * @throws Exception
	 */	
	public static int [] ClusteringAlgorithm(double[][]data, int ncluster) throws Exception {
		
	     int numDimensions = data.length;
	     int numInstances = data[0].length;
	     ArrayList<Attribute> atts = new ArrayList<Attribute>();
	     List<Instance> instances = new ArrayList<Instance>();
	     for(int dim = 0; dim < numDimensions; dim++)
	     {
	         Attribute current = new Attribute("Attribute" + dim, dim);
	         if(dim == 0)
	         {
	             for(int obj = 0; obj < numInstances; obj++)
	             {
	                 instances.add(new SparseInstance(numDimensions));
	             }
	         }

	         for(int obj = 0; obj < numInstances; obj++)
	         {
	             instances.get(obj).setValue(current, data[dim][obj]);
	         }

	         atts.add(current);
		     if (Parameter.stoprunning) { 
		    	
		    	 break; 
		     }
	     }
	     

	     
	     Instances newDataset = new Instances("Dataset", atts, instances.size());

	     for(Instance inst : instances)
	         newDataset.add(inst);

		SimpleKMeans kmeans = new SimpleKMeans();
		 
		kmeans.setSeed(10);

		//important parameter to set: preserver order, number of cluster.
		kmeans.setPreserveInstancesOrder(true);
		try {
			kmeans.setNumClusters(ncluster);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		kmeans.buildClusterer(newDataset);
		
		// This array returns the cluster number (starting with 0) for each instance
		// The array has as many elements as the number of instances
		int[] assignments = kmeans.getAssignments();
		int i=0;
		/*for(int clusterNum : assignments) {
		    System.out.printf("Instance %d -> Cluster %d \n", i, clusterNum);
		    i++;
		}*/
		
		return assignments;
		
	}
	
	/**
	 * Get SumRow to Know if it contains Data or not
	 * 
	 * @param matrix
	 * @param row
	 * @return
	 */
	public static double sumRow(double[][] matrix, int row)
	{
	   double sum = 0;

	    int colSize = matrix[row].length;
	    for(int j = 0; j < colSize; j++){
	        sum += matrix[row][j];
	    }  
	    return sum;
	}
	
	/**
	 * Find the Zero Rows from the Data
	 * @param data
	 */
	public static void FindZeroRows(double [][]data) {
	 ZeroRows = new ArrayList<Integer>();
	 int numDimensions = data.length;   
     int count;
     for(int dim = 0; dim < numDimensions; dim++)
     {
        
       double sum = sumRow(data, dim);
        if (sum == 0) {
        	ZeroRows.add(dim);
        }
     }
	}
	

	/**
	 * perform clustering on dataset range
	 * @param data
	 * @param minK
	 * @param maxK
	 * @return
	 * @throws Exception
	 */
	public static int[][] performclustering(double [][]data, int minK, int maxK, int row) throws Exception {		
	    //Determine the best K in the range	
	   
	    int len = maxK - minK + 1;
	    int ncluster;
	    //Create a multidimensional array to store clusters
	    
	    int [][] Clusters = new int[row][len];
	    int [] Cluster_assign = null;
	    ncluster = minK;
	    for (int i=0;i < len;i++) {	
	    	    	
	    	 System.out.println(String.format("kmeans_Clustering :: K = %d .......", ncluster));  	
	    	//========Perform Kmeans Clustering using Current K====================
		     if (Parameter.stoprunning) { 
		    	 break; }
	    	Cluster_assign = ClusteringAlgorithm(data,ncluster);
	    	int h= 0; int k= 0;
	    	while (h<row)
	    	 {
	    		//check if arraylist contains index, if yes value equals zero, otherwise assign cluster value
				if (ZeroRows.contains(h)) {
					Clusters[h][i] =0;
				}else {
					Clusters[h][i] = Cluster_assign[k];	  
					k++;
				}
				h++;
	    	}
	    	ncluster  = ncluster + 1;
		 }
	    
	    return Clusters;
	}
	
	/**
	 * EXtract the TAD from Clusters defined for the dataset
	 * @param Cluster
	 * @return
	 * @throws IOException
	 */	
	public static int [][] ExtractTAD(int [] Cluster) throws IOException {
		int count = 1;
		int start = 0;
		int len = Cluster.length;
		ArrayList <Integer>Border = new ArrayList<Integer>();
		System.out.println(String.format("Identify the TADs"));
		for (int i=1; i<len;i++) {
			 if (Cluster[i]!=Cluster[start]) {
				 Border.add(start);
				 Border.add(i-1);
				 start = i;
				 count = count + 1;
			 }
			 
	    }	
		 Border.add(start);	 Border.add(len-1);
		int nTAD = Border.size()/2;
		//convert array list to array
		Object[] objects = Border.toArray();
		Integer [] border = new Integer[objects.length];
		System.arraycopy(objects,0, border, 0, objects.length);
		Border.clear();
		//==========================================================
		// Select the TADs with size > 3 from those classified above
		//===========================================================
		for (int i=0; i<border.length ;i+=2) {
			int current =border[i];
			int next = border[i+1];
			int size = next - current + 1;
			size = size * Resolution;
			if (size > min_TAD_size) {
				Border.add(current);   //TAD start
				Border.add(next);	//TAD end
								
			}			 
	    }
		
		
		// Copy TAD to a 2D array
		nTAD = Border.size()/2;
		//convert array list to array
		objects = Border.toArray();
	
		ArrayList <Integer>TADlist = new ArrayList<Integer>();
		for (int i=0; i<Border.size() ;i+=2) {
			int identify_TAD =  (int) objects[i+1];			
			//=========================================================
			//Find the gaps classified as domain
			//=========================================================			
			if (!ZeroRows.contains(identify_TAD)) {				
				TADlist.add((int) objects[i]);
				TADlist.add((int) objects[i+1]);				
			}
							
			
		}
		
		nTAD = TADlist.size()/2;
		//convert array list to array
		objects = TADlist.toArray();
		int [][] TAD = new int[nTAD][2]; // [start end identifier]
		int ind= 0;
		for (int i=0; i<TADlist.size() ;i+=2) {
			TAD[ind][0] = (int) objects[i] ;    
			TAD[ind][1] = (int) objects[i+1] ;
			
			int out1 = TAD[ind][0]* Resolution;    //multiply by Resolution here
			int out2 = TAD[ind][1] * Resolution;
			System.out.println((String.format("%d	%d	",out1 ,out2))); //Recognized as domain = 1
			log_outputWriter.write((String.format("%d	%d	\n",out1 ,out2))); //Recognized as domain = 1
			ind++;
		}
		return TAD;
		
	}
	
	/**
	 *  Returns the Intra Quality of Domains
	 * @param dS - domain start
	 * @param dE - domain end
	 * @param Data - Input Data
	 * @return
	 */
	public static double IntraQuality(int dS,int dE, double[][] Data) {
		double intra = 0;
		double sum = 0;
		int count = 0;
		 for (int i=dS; i<dE+1;i++) {
			 for (int j=i+1; j<dE+1;j++) {
				 count = count +1;
				 sum = sum + Data[i][j];
			 }
		 }
		 // compute intra
		 if (sum >  0 && count > 0) {
			 intra = sum/count;
		 }
		 else {
			 intra = 0;
		 }
		return intra;
	}
	
	/**
	 * Returns the Inter Quality of Domains
	 * @param dS1
	 * @param dE1
	 * @param dS2
	 * @param dE2
	 * @param Data
	 * @return
	 */
	public static double [] InterQuality(int dS1,int dE1, int dS2,int dE2,double[][] Data) {
		double [] output = new double[2];
		double sum = 0;
		int count = 0;
		int incr = 0;
		for (int i=dS1; i<=dS2-1;i++) {
			incr = incr + 1;
			int c= 0;
			 for (int j=dE1+1; j<=dE2;j++) {
				 c = c + 1;
				 count = count +1;
				 sum = sum + Data[i][j];
				 if(c==incr) {
				        break;
				 }
			 }
		 }
				
		output[0] = sum; output[1] = count; 
		return output;
		
		
	}
	
	
	
	/**
	 * Determines the Quality of TAD set for one K value 
	 * @param TAD
	 * @param Data
	 * @return
	 */
	public static double TAD_QA(int [][]TAD, double [][] Data) {
		int TAD_len = TAD.length;
    	int domain1_start; int domain1_end,domain2_start; int domain2_end;
    	double [] outinter1 = new double[2];
    	double [] outinter2 = new double[2];
    	double avg = 0;
    	double Sum = 0;
    	
    	for (int j=0; j<TAD_len;j++) {
    		 /**
    		  * ===============================================================
    		  * For each Domain find intra-TD and inter-TD
    		  * Intra(i) denote the average of contact frequencies between bins within the same TD i
    		  * Inter(i, j) denote the average of contact frequencies between a bin in TD i and a bin in adjacent TD j, where |i�j | = 1
    		  * ===============================================================
    		  */
    		 // Intra
    		 domain1_start = TAD[j][0];
    	     domain1_end =  TAD[j][1];
    	     double intra = IntraQuality(domain1_start,domain1_end, Data) ;
    	     double inter = 0;
    		 // Inter
    	     if (j==0) {
    	    	 // Adjacent td is just the next one
    	    	 domain2_start = TAD[j+1][0];
    	    	 domain2_end = TAD[j+1][1];
    	    	 outinter1 =  InterQuality( domain1_start,domain1_end,domain2_start,domain2_end,Data) ;
    	    	 inter = outinter1[0]/outinter1[1];
    	     }
    	     else if(j > 0 && j < TAD_len - 1)  {
    	    	 //Adjacent td includes prev and next
    	    	 domain2_start = TAD[j-1][0];
    	    	 domain2_end = TAD[j-1][1];
    	    	 outinter1 =  InterQuality( domain2_start,domain2_end,domain1_start,domain1_end,Data) ;
    	    	 domain2_start = TAD[j+1][0];
    	    	 domain2_end = TAD[j+1][1];
    	    	 outinter2 =  InterQuality( domain1_start,domain1_end,domain2_start,domain2_end,Data) ;
    	    	 inter = (outinter1[0] + outinter2[0]) /  (outinter1[1] + outinter2[1]);
    	     }
    	     else if (j==TAD_len - 1 ) {
    	    	 // Adjacent td id just the  prev one  
    	    	 domain2_start = TAD[j-1][0];
    	    	 domain2_end = TAD[j-1][1];
    	    	 outinter1 =  InterQuality(domain2_start,domain2_end, domain1_start,domain1_end,Data) ;
    	    	 inter = outinter1[0]/outinter1[1];
    	     }
    	     
    	     Sum += (intra - inter);
    	         		 
    	 }
    	 avg = Sum/TAD_len;
    	 System.out.println(String.format("The Quality Score = %f", avg));
    	 
    	 
		return avg;
	}
	
	
	/**
	 * Extract the TAD, Find the Quality and Send to File
	 * @param Clusters
	 * @param minK
	 * @param maxK
	 * @throws Exception
	 */
	public static void ClusterTAD_algorithm(int[][] Clusters, int minK, int maxK, double[][] Data) throws Exception {		
		int dim = Clusters.length;
	    int len = maxK - minK + 1; //Number of clusters 
	    int [][] TAD =null;
	    double Qscore;	   
	    int [] Cluster = new int[dim];
	    int K = minK;
	    String namest= "";
	    
	    TADwriter wt = new TADwriter();	
	    TADFolder = Outputpath + "TADs/";
	    wt.make_folder(TADFolder ); //make folder
	    
	    System.out.println("================================================");
	    System.out.println(String.format("TAD Identification and Quality Assessment"));
	    System.out.println("================================================");
	    
	    for (int i=0; i<len;i++) {
	    		    	 
	    	System.out.print(String.format("k = %d ->", K));
	    	log_outputWriter.write(String.format("k = %d ->", K));
	    	for (int h=0; h<dim ;h++) {
	    		Cluster[h] = Clusters[h][i];	    	
	    	}
	    	// Order Clustering numbering based on index
	    	Cluster = Order_ClusterNum(Cluster); // return a ordered label
	    	//-----------------------------------------------------------
	    	// Extract TAD based on the Order defined   		
	    	log_outputWriter.write("TAD for Clustering Algorithm\n");
	    	TAD = ExtractTAD(Cluster);
	    	System.out.println(String.format("The Number of TAD = %d", TAD.length));	
	    	//-----------------------------------------------------------
	    	// Write TAD identified to file
	    	String TADfile="";
	    	if (status==1) {
	    		namest = reclustername ;
	    	}
	    	TADfile =TADFolder  + "TAD_" + name + "_K="+ namest+String.valueOf(K) +".bed"; //file to hold TAD written to file	   
		    wt.delete_file(TADfile);
		    wt.writeTAD(TADfile,TAD,chromosome); //write matrix to file
		  //-----------------------------------------------------------
	    	//Find the TAD Quality
	    	
	    	Qscore = TAD_QA(TAD,Data);
	    	Quality.add(Qscore);
	    	if ( Qscore > Best_Qscore) {
	    		Best_Qscore = Qscore;	// current best Qscore
	    		 //Note the TADs identifies by Current
	    		Best_TAD = twoDimensionalArrayClone(TAD);
	    		
	    		Best_K = K;
	    		
	    		Bestname="TAD_" + name + "_K="+ namest+String.valueOf(K) +".bed";
	    		
	    	}
	    	
	    	System.out.println("-----------------------------------------------------");
	    	K++;
	    }
	  
	   
	}
	
	/**
	 * create SubData from Big Dataset
	 * @param array
	 * @return
	 */
	public static double [][] createSubData(int [] array){
		int nrow = array.length;
		int ncols = 2*nRegion;
		double [][] Data = new double[nrow][ncols];
		for (int i=0; i<nrow;i++) {
			int ind = array[i];
			Data[i] = Feat[ind];			
		}
	
		return Data;
	}
	
	
	/**
	 * First Step for re-Clustering operation, check Data set dimension
	 * @param Cluster
	 * @param len
	 * @param sz
	 * @param label
	 * @throws Exception
	 */
	public static void Recluster(int []Cluster, int len, int []sz, int []label) throws Exception {
		// Select 1/2 
		int limit_1 = (int)(0.1*len);
		
		
		for (int i = 0; i < limit_1; i++) {
			int size =sz[label[i]] * Resolution;
			
			if(size < max_TAD_size) {
				System.out.println("\t RECLUSTERING not Necessary...");
				System.out.println("--------------------------------------------");
				break;
			}
			
			System.out.println("--------------------------------------------");
			System.out.println("\t RECLUSTERING...");
			System.out.println("--------------------------------------------");
			System.out.println(String.format("Clustering %d ........\n",i));
			//find the index of the element and 
			int [] array = new int[sz[label[i]]];
			int ind = 0;
		
			for (int j=0; j<Cluster.length;j++) {
				 if (Cluster[j]==label[i]) {
					 array[ind] = j;
					 ind++;
				 }
				
		    }
						
			//Find row in big data and create a new Dataset
			double [][] Data = null;
			Data = createSubData(array);			
			// Re-cluster Data
			Reclustering( Cluster,  Data, array);
			
		}
	}
	
	/**
	 * Order the Cluster Assignment based on index
	 * @param Cluster
	 * @return
	 */	
	public static int [] Order_ClusterNum(int []Cluster) {
		int len = Cluster.length;
		int [] New_Cluster = new int[len];
		int i = 0;
		int count = 0;
		New_Cluster[i] =count;
		 for (i=1; i<len;i++) {
			 if (Cluster[i-1]==Cluster[i]) {
				 count = count + 0;}
			 else {
				 count = count + 1;
			 }
			 New_Cluster[i] = count;
		 }
		return New_Cluster;		
	}

	/**
	 * Perform Re-clustering of Cluster Subdata
	 * @param Cluster
	 * @param Data
	 * @param array
	 * @throws Exception
	 */
	
	public static void Reclustering(int [] Cluster,  double[][]Data, int [] array) throws Exception {
		 status = 1; //means Reclustering done
		//=========Determine an estimate of the Number of Cluster(K)==========
	    int size_Data = Data.length;//get the size of new data created
	    int K  = RoughEstimate(size_Data);		    
	    //=====Create a Window for Flexibility around K, Hence K = upperLimit = K-10 and Lower Limit =  K + 10
	    int Kmin = K-1; int Kmax=K+1;
	    if (Kmin < 0) { 	   // less than zero
	    	K = 2;
	    }
	    if (Kmax > size_Data ) { //Greater than size of the data 
	    	Kmax = size_Data - 1;   }
	    //============Perform Clustering and Save Cluster assignment========
	    TADwriter wt = new TADwriter();		
	    ClusterFolder = Outputpath + Clusterpath;
	    wt.make_folder(ClusterFolder); //make folder
	    String Clusterfile = ClusterFolder + "file_" + name + "_subcluster.txt"; //file to hold clusters
	    //transpose
	    Data = transpose(Data);
	    int row = Data[0].length;
	    int[][] labels =  performclustering(Data,Kmin,Kmax,row);
	    wt.delete_file(Clusterfile);
	    wt.writeClusterMatrix(Clusterfile, labels); //write matrix to file
	    row = Cluster.length;
	    int col =labels[0].length;
	    int [][]Clusters = new int[row][col];
	    
	    // Copy Cluster to the Length of the number of columns in Clusters
	    for ( int p = 0; p<col; p++) {
	    	 for (int i=0; i<row;i++) {
	    		 Clusters[i][p] = Cluster[i];
	    	 }
	    }
	    	    
	    // Replace the rows of the reclustered with new defined cluster
	    int nrow = array.length;	   
	    for (int j=0; j<col;j++) {
	    	 int index = 0;
		    for (int i=0; i<nrow;i++) {
		    	int ind = array[i];
		    	Clusters[ind][j] = labels[index][j];
		    	index++;
		    }
	 }
	    //======================== Quality Assessment==================
	    ClusterTAD_algorithm(Clusters,Kmin,Kmax,RealData);
	    //=============================================================
	
		
}
	
	/**
	 * Transpose to genearate Dataset that can be clustered
	 * @param matrix
	 * @return
	 */
	public static double[][]transpose(double [][] matrix){
		int m = matrix.length;
		int n = matrix[0].length;

		double[][] trasposedMatrix = new double[n][m];

		    for(int x = 0; x < n; x++)
		    {
		        for(int y = 0; y < m; y++)
		        {
		            trasposedMatrix[x][y] = matrix[y][x];
		        }
		    }

		    return trasposedMatrix;
	}
	
	
	/**
	 * Perform cluster TAD Iteration- Limit is Iteration 2
	 * @param Clusters
	 * @param minK
	 * @param maxK
	 * @throws Exception 
	 */
	
	public static void ClusterTAD_Iteration(int[][] Clusters,int minK, int maxK) throws Exception {
		
		//Find the TAD from the result
		int dim = Clusters.length;
		int size_Zero = ZeroRows.size();
		int nrows = nRegion-size_Zero;
		int [] Cluster = new int[nrows];
	    int len = maxK - minK + 1; //Number of clusters 
	    ArrayList <Integer>cluster_size = new ArrayList<Integer>();
	    //--------------------------------------------------
	    // Update Labels - Remove the zerosRows
	    for (int i=0; i<len;i++) {
	    	reclustername = String.valueOf(minK+i) + "_RECLUSTER_K=";
	    	int index = 0;
			for (int ind = 0; ind < nrows; ind++){		
				//check if arraylist contains index, if yes continue, otherwise create dataset				
					Cluster[ind] = Clusters[ind][i];	
			}
			
		//---------------------------------------------------
		// Order Clustering numbering based on index
	    Cluster = Order_ClusterNum(Cluster); // return a ordered label
	    List<Integer> list = new ArrayList<Integer>();
	    Collections.addAll(list, Arrays.stream(Cluster).boxed().toArray(Integer[]::new));
	   	    
	   // System.out.println(String.format("list size = %d",list.size()));
		//Get the size of each cluster	
	    int max = Arrays.stream(Cluster).max().getAsInt();	
	    int min=   Arrays.stream(Cluster).min().getAsInt(); 
	   // System.out.println(String.format("The Minimum label = %d and The Maximum label = %d ", min,max));
	    int sz = max-min + 1;
	    int [] freq = new int[sz];
	    int ind = 0;
	    for (int k=min;k<=max;k++) {	    	
	    	int occurence = Collections.frequency(list, k);
	    	//System.out.println(String.format("%d    %d",k,occurence));
	    	freq[ind]= occurence;
	    	ind++;
	    }
	  
	    //---------------------------------------------------
	    // Rank the clusters based on the size, get index 
	    int[] sortedlabel = IntStream.range(0, freq.length)
                .boxed().sorted((x, y) -> freq[x] - freq[y])
                .mapToInt(ele -> ele).toArray();
	    // Reverse the content, to get in descending order
	    for (int k = 0; k < sortedlabel.length/2; k++) {
            int temp = sortedlabel[k];
            sortedlabel[k] = sortedlabel[sortedlabel.length-(1+k)];
            sortedlabel[sortedlabel.length-(1+k)] = temp;
          }
	    
	    	    
	   // System.out.println(String.format("sorted list size %d", sortedlabel.length));
	   
	   //------------------------------------------------------
	    Recluster(Cluster, sortedlabel.length, freq, sortedlabel) ;
	    
	    
	    }
	    
	    
	 
	}
	
	
		  
	
	
	
	/**
	 * Clone 2-Dimensional Array
	 * @param a
	 * @return
	 */	
	  public static int[][] twoDimensionalArrayClone(int [][] a) {
	    int[][] b = new int[a.length][];
	    for (int i = 0; i < a.length; i++) {
	      b[i] = a[i].clone();
	    }
	    return b;
	  }
	
	
	  
	  
	  
	  
	  /**
	   * Constructor for clusterTAD
	   * @param args
	   * @param Viewer
	   * @throws Exception
	   */
	  
	  public ClusterTAD(String[] args ) throws Exception{
		// ==========PARAMETERS==================		 
			inputfile = args[0];
			outpath = args[1];		
			Resolution = Integer.parseInt(args[2]);
			ismatrix = Integer.parseInt(args[3]);
		    startloc = Integer.parseInt(args[4]);
		    chromosome = args[5];	  
		    
		  
		   
	  }
	
	/**
	 * 
	 * @param args
	 * @return 
	 */
	public String Perform_operation(){	
		String msg = null;
		System.out.println("Welcome to TAD identification from contact Matrix.");
	     System.out.println("INSTRUCTION:\n (1)Input is a Normalized Matrix. \n"
	     		+ " (2) Specify the Data sperator for input data");
	   
		String[] tmp = inputfile.split("[\\/ \\. \\\\]");
		 if (inputfile.contains(".")){
			name = tmp[tmp.length - 2];
		   }
		  else{
				
			name = tmp[tmp.length - 1];
		}
	     
	     String namest = "";
	     String path = "data/";
	   	 
	     JProgressBar progress = new JProgressBar(0, 100);
	     // Overlay a string showing the percentage done
	     progress.setStringPainted(true);
	     
	     //==========================================================================
	    // Split process into stages and monitor with progress bar
	    // ============================================================================
	     
	    //STAGE 1: data loading 
	     try {
	    	 System.out.println("Reading Data from File......");
		     String  seperator = "\\s+";  // specify the data seperator for dataset
		    		     		     
		     System.out.println(String.format("Input = %s", inputfile));
		    try {	
		    	RealData= Parameter.readFile(inputfile,seperator,ismatrix);
			    System.out.println("File read successfully!!!\n");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//===================================================================
		
		if (RealData!=null) {
		//===================================================================
		    
		//STAGE 2: New data creation	   
		    System.out.println("File read successfully!!!\n");
		    //==========Create new Feature Data================
		    nRegion = RealData.length;
		    Feat = ClusterTAD.CreateNewData(RealData);
		    System.out.println("Feature Dataset Created Successfully!!!\n");
		    String filename = "Readme.txt";
		    Outputpath = outpath +   "/Output/" ;		    
		
		    TADwriter wt = new TADwriter();
		    wt.make_folder(Outputpath);
		    log_outputWriter = new BufferedWriter(new FileWriter( Outputpath + filename));
		 //===================================================================
		
		//===================================================================   		  
		    
		//STAGE 3: Estimate number of Clusters
		    //=========Determine an estimate of the Number of Cluster(K)==========
		    int size_Feat = Feat.length;//get the size of new data created
		    int K  = RoughEstimate(size_Feat);		    
		    //=====Create a Window for Flexibility around K, Hence K = upperLimit = K-10 and Lower Limit =  K + 10
		    int Kmin = K-1; int Kmax=K+1;
		    if (Kmin < 0) { 	   // less than zero
		    	Kmin = 2;  }
		    if (Kmax > size_Feat ) { //Greater than size of the data 
		    	Kmax = size_Feat - 1;   }
		    
		//===================================================================
		  
		//===================================================================  
		    //STAGE 4: Perform Clustering and Save
		    //============Perform Clustering and Save Cluster assignment========
		    ClusterFolder = Outputpath + Clusterpath;	    
		    wt.make_folder(ClusterFolder); //make folder
		    String Clusterfile = ClusterFolder + "file_" + name + "_cluster.txt"; //file to hold clusters
		    int row = Feat.length;
		    int[][] labels =  performclustering(Feat,Kmin,Kmax,row);
		   	    
		    wt.delete_file(Clusterfile);
		    wt.writeClusterMatrix(Clusterfile, labels); //write matrix to file
		   
		   
		   
		    //============================================================
		    ClusterTAD_algorithm(labels,Kmin,Kmax,RealData);   //Return this TAD, and find out if there are more interesting TADs
		    
		
		    //=============================================================    
		    // Perform Re-clustering
		    //----------------------------------------------------------------------		    
		    //STAGE 5: Extract  Re-clustering and Extract
		  
		    
		     ClusterTAD_Iteration(labels,Kmin,Kmax); //Only enabled for 
		     
		    		     
		    //======================Quality Assessment=======================   
		    
		    System.out.println(String.format("Best Quality Score for TAD identified at K = %d ",Best_K));
		    System.out.println(String.format("Quality score of best TAD identified = %f ",Best_Qscore));
		    System.out.println("TAD identified written to file");
		  
		    		    
		   
		    TADfile =TADFolder  + "Best" + Bestname ;  //file to hold TAD written to file	 	   
		    wt.delete_file(TADfile);
		    wt.writeTAD(TADfile,Best_TAD,chromosome); //write matrix to file
		    
		    System.out.println("=========== Quality Assessment Completed =========");
		    
		    
		    
		    //write Quality score to file
		    String Qscorefile =TADFolder + name + "_TAD_QualityScore_List.txt"; //file to hold Quality score of TAD written to file		   
			wt.delete_file( Qscorefile);
		    wt.writeList(Qscorefile, Quality); 
		    
		    
		    JOptionPane.showMessageDialog(null, "<html><b>Successfully Completed</b>. Check the output directory for output files. <br /> The best TAD identified is saved in the <i>TADs</i> directory with Prefix =  <b>BestTAD </b> </html>","Information",JOptionPane.INFORMATION_MESSAGE);
		    
		    msg = "TAD extraction completed";
		}
		
		    
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			msg = "An error occured. File Not Found ";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			msg = "TAD extraction failed";
		}
	        try {
				log_outputWriter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				msg = "An error occured. Failed to write file to output directory";
				e.printStackTrace();
			}  
			try {
				log_outputWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				msg = "An error occured. Failed to write file to output directory";
				e.printStackTrace();
			}
			
			return msg;
	}
}

	
