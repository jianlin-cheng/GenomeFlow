package edu.missouri.chenglab.lordg.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import edu.missouri.chenglab.lordg.utility.Helper;

/**
 * Compute Spearman correlation between reconstructed structure vs. input matrix
 * @author Tuan
 *
 */
public class Evaluate {
	
	
	
	private static String contactFile = "/Users/Tuan/workspace/CNS/newdata/chr1_1mb_matrix.txt";
	private static String strFile = "/Users/Tuan/workspace/CNS/results_7_24_2015/models_a11/chr1_1mb_rank09_a11.pdb";
	
	private static String strFolder = "/Users/Tuan/workspace/Pastis/gm12878/structures_nan/";
	private static String resultFile = "/Users/Tuan/workspace/Pastis/gm12878/structures_nan/spearman.txt";
	
	public static void main(String[] args) throws Exception{
		
		/*
		String strFile = "/home/tuan/workspace/Hi-C_Data_Processing/output/3Dcoor_chr19.xyz";
		String contFile = "/home/tuan/workspace/Hi-C_Data_Processing/output/IFMatrix_Chr_19.txt";
		
		System.out.println(correlationStrIF(strFile,contFile));
		
		
		String strFile1 = "input/SyntheticYeast/chainABCDE.pdb";
		String strFile2 = "output/SyntheticYeast/pow1/3Dcoor.xyz";
		System.out.println(correlationStrStr(strFile1, strFile2));
		
		*/
		
//		double[] a = {56,75,45,71,61,64,58,80,76,61};
//		double[] b = {66,70,40,60,65,56,59,77,67,63};
//		System.out.println(new SpearmansCorrelation().correlation(a, b));

		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(resultFile, true)));
		
				
		String fileName;
		
		File dir = new File(strFolder);
		
		for (File f : dir.listFiles()){
			if (f.getName().endsWith(".pdb") && !f.getName().contains("reduced")){
				
				fileName = f.getName();		
				
				strFile = strFolder + "/" + fileName;		
				
				contactFile = strFolder + "/" + fileName.replace("pm2_","").replace(".pdb",".txt");
				
				//contactFile = strFolder + "/chr" + fileName.replace(".pdb","_matrix.txt"); 
				
				//contactFile = "/Users/Tuan/workspace/CNS/newdata" + "/" + fileName.replaceAll("_rank.*.pdb","_matrix.txt");
		
				double[][] b = readContactMatrix(contactFile);
				
				double[][] a = calPairwiseDist(Helper.getHelperInstance().loadPDBStructure(strFile));
				
			
				//ignore the first 9 atoms for Pastis structures, 0..8
//				double[][] a1 = calPairwiseDist(Helper.getHelperInstance().loadPDBStructure(strFile));
//				double[][] a = new double[a1.length - 9][a1.length - 9];
//				for(int i = 9; i < a1.length; i++){
//					for(int j = 9; j < a1.length; j++){
//						a[i - 9][j - 9] = a1[i][j];
//					}
//				}				
				//
				
				int gap = a.length/10;//remove low range contacts
				
				double cor = calSpearmanCorrelation(a, b, gap);
				
				System.out.println(cor);
				pw.printf("Filename: %s, correlation: %.5f \n", strFile, cor );		
				pw.flush();
				//break;
			}
		}
		
		pw.close();
	}
	
	/**
	 * correlation between reconstructed distances and true distances
	 * @param strFile1 : true yeast structure
	 * @param strFile2 : Shrec3D structure
	 * @return
	 */
//	private static double correlationStrStr(String strFile1, String strFile2) throws Exception{
//		double[][] a = calPairwiseDist(MakeSyntheticInput.loadPDBYeastStructure(strFile1));
//		double[][] b = calPairwiseDist(readShrec3DCoordinates(strFile2));
//		
//		return calCorrelation(a, b);
//	}
	
	/**
	 * Calculate correlation between reconstructed distances and input IFs
	 * @param strFile
	 * @param contFile
	 * @return
	 * @throws Exception
	 */
	private static double correlationStrIF(String strFile, String contFile) throws Exception{
		double[][] a = calPairwiseDist(readShrec3DCoordinates(strFile));
		double[][] b = readContactMatrix(contFile);
		
		return calCorrelation(a, b);
	}
	
	/**
	 * correlation between 2 symmetric matrices, Pearson correlation
	 * @param a
	 * @param b
	 * @return
	 */
	public static double calCorrelation(double[][] a, double[][] b) throws Exception{
		
		if (a.length != b.length){			
			throw new Exception("Matrices don't have the same dimension: " + a.length + "\t vs. \t" + b.length);			
		}
		
		//double[] x = flatArray(a);
		//double[] y = flatArray(b);
		double[] x,y;
		
		ArrayList<Double> lst1 = new ArrayList<Double>();
		ArrayList<Double> lst2 = new ArrayList<Double>();
		for(int i = 0; i < a.length; i++){
			for(int j = i + 1; j < a.length; j++){
				if(!Double.isNaN(a[i][j]) ){
					lst1.add(a[i][j]);
					lst2.add(b[i][j]);
				}
				
			}
		}	
		x = new double[lst1.size()];
		y = new double[lst1.size()];
		for(int i = 0; i < lst1.size(); i++){
			x[i] = lst1.get(i);
			y[i] = lst2.get(i);
		}
		center(x);
		center(y); 
		double cor = product(x, y) / (norm2(x) * norm2(y));
		
		return cor;

	}
	
	/**
	 * convert symmetric 2D array into 1D array
	 * @param a
	 * @return
	 */
	public static double[] flatArray(double[][] a){
		double[] x = new double[a.length * (a.length - 1) / 2];
				
		int count = 0;
		for(int i = 0; i < a.length; i++){
			for(int j = i + 1; j < a.length; j++){
				x[count] = a[i][j];		
				count++;
			}
		}		
		return x;
	}
	
	/**
	 * correlation between 2 symmetric matrices
	 * @param a
	 * @param b
	 * @return
	 */
	public static double calSpearmanCorrelation(double[][] a, double[][] b, int... gap) throws Exception{

		int g=0;
		if (gap != null && gap.length > 0){
			g = gap[0];
		}
		
		int count = 0;
		for(int i = 0; i < a.length; i++){
			for(int j = i + 1 + g; j < a.length; j++){
				if (!Double.isNaN(a[i][j]) && !Double.isNaN(b[i][j]) && a[i][j] > 0 && b[i][j] > 0){		
					count++;
				}
			}
		}		
		
		double[] x = new double[count];
		double[] y = new double[count];
		
		count = 0;
		for(int i = 0; i < a.length; i++){
			for(int j = i + 1 + g; j < a.length; j++){
				if (!Double.isNaN(a[i][j]) && !Double.isNaN(b[i][j]) && a[i][j] > 0 && b[i][j] > 0){
					x[count] = a[i][j];
					y[count] = b[i][j];
					count++;
				}
				
			}
		}	
		
		//System.out.printf("%d, %d\n", a.length, count);
		
		double cor = calSpearmanCorrelation(x, y);
		//double mycor = mySpearmanCorrelation(x, y);
		
		//System.out.printf("\nMy cor: %.2f, the other: %.2f \n", mycor, cor);
		return cor;
	}

	public static double calSpearmanCorrelation(double[] a, double[] b) throws Exception{
		
		//remove pairs of (0,0) as they inflate correlation number
		int count = 0;		
		double avga = 0, avgb = 0;
		for(int i = 0; i < a.length; i++){
			if (Math.abs(a[i]) < 0.000001 && Math.abs(b[i]) < 0.000001) continue;
			avga += a[i];
			avgb += b[i];
			count++;
		}
		
		avga /= count;
		avgb /= count;
		
		double[] x = new double[count];
		double[] y = new double[count];
		
		int t = 0;
		for(int i = 0; i < a.length; i++){
			if (Math.abs(a[i]) < 0.000001 && Math.abs(b[i]) < 0.000001) continue;
			x[t] = (int)(a[i] * 100 / avga); // round up to 1 decimal before comparing
			y[t] = (int)(b[i] * 100 / avgb);
			t++;
		}
		////
		
		return new SpearmansCorrelation().correlation(x, y);
	}
	
	/**
	 * Read contact matrix
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	private static double[][] readContactMatrix(String fileName) throws Exception{
		File file = new File(fileName);
		FileReader fr = null;
		BufferedReader br = null;
		String ln;
		String[] st;
		Pattern splitRegex = Pattern.compile("[\\s]+");
		double[][] a = null;
		int count = 0;
		try{
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			
			ln = br.readLine();
			st = splitRegex.split(ln.trim());
			a = new double[st.length][st.length];
			
			for(int i = 0; i < st.length; i++){
				a[count][i] = Double.parseDouble(st[i]);
			}
			count++;
			
			while((ln = br.readLine()) != null){
				st = splitRegex.split(ln.trim());
				
				for(int i = 0; i < st.length; i++){
					a[count][i] = Double.parseDouble(st[i]);
				}
				count++;
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			if (br != null){
				br.close();				
			}
			if (fr != null){
				fr.close();
			}
		}
				
		
		return a;
		
	}
	
	/**
	 * calculate pairwise distances between points from a coordinate matrix
	 * @param co
	 * @return
	 */
	public static double[][] calPairwiseDist(double[][] co, ArrayList<Integer> ... omit){
		double[][] d = new double[co.length][co.length];
		
		for(int i = 0; i < co.length; i++){
			if (omit.length == 0 || !omit[0].contains(i)){
				for(int j = i + 1; j < co.length; j++){
					if (omit.length == 0 || !omit[0].contains(j)){
						d[i][j] = distance(co[i][0],co[i][1],co[i][2],co[j][0],co[j][1],co[j][2]);
						d[j][i] = d[i][j];
					}
				}
			}
		}
		
		return d;
	}
	
	/**
	 * Read coordinates of structures from file
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static double[][] readShrec3DCoordinates(String fileName) throws Exception{
		File file = new File(fileName);	
		FileReader fr = null;
		BufferedReader br = null;
		String ln;
		String[] st;
		Pattern splitRegex = Pattern.compile("[\\s]+");
		ArrayList<Double> lstX = new ArrayList<Double>();
		ArrayList<Double> lstY = new ArrayList<Double>();
		ArrayList<Double> lstZ = new ArrayList<Double>();
		
		try{
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			
			while((ln = br.readLine()) != null){
				st = splitRegex.split(ln);
				lstX.add(Double.parseDouble(st[0]));
				lstY.add(Double.parseDouble(st[1]));
				lstZ.add(Double.parseDouble(st[2]));				
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			if (br != null){
				br.close();
			}
			if (fr != null){
				fr.close();
			}
		}
		
		double[][] coor = new double[lstX.size()][3];
		for(int i = 0; i < lstX.size(); i++){
			coor[i][0] = lstX.get(i);
			coor[i][1] = lstY.get(i);
			coor[i][2] = lstZ.get(i);
		}
		
		return coor;
	}
	
	private static double distance(double x1, double y1, double z1, double x2, double y2, double z2){
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));
	}
	/**
	 * center the array at 0
	 * @param a
	 */
	public static void center(double[] a){
		double avg = 0;
		for(double d : a){
			avg += d;
		}
		avg /= a.length;
		
		for(int i = 0; i < a.length; i++){
			a[i] -= avg;
		}		
	}

	/**
	 * calculate 2-norm
	 * @param a
	 * @return
	 */
	public static double norm2(double[] a){
		double sum = 0;
		for(double d : a){
			sum += d * d;
		}
		return Math.sqrt(sum);
	}
	
	/**
	 * calculate product of 2 vectors
	 * @param x
	 * @param y
	 * @return
	 */
	public static double product(double[] x, double[] y){
		double p = 0;
		for(int i = 0; i < x.length; i++){
			p += x[i] * y[i];
		}
		return p;
	}
	
	/**
	 * Calculate standard deviation of matrix a
	 * @param a
	 * @return
	 */
	public static double standardDeviation(double[] a){
		double sd = 0.0,avg = 0.0;
		for(double d : a){
			avg += d;
		}
		avg /= a.length;

		for(double d : a){
			sd += (d - avg) * (d - avg);
		}

		return Math.sqrt(sd/a.length);
		
	}
	
	
	public static double mySpearmanCorrelation(double[] a, double[] b){
		List<Element> lst = new ArrayList<Element>();
		for(int i = 0; i < a.length; i++){
			lst.add(new Element(a[i],i));
		}
		//Collections.sort(lst, (e1,e2) -> e1.value.compareTo(e2.value) );
		
		double[] ranka = new double[a.length];
		for(int i = 0; i < lst.size(); i++){
			ranka[lst.get(i).id] = i;
		}
		
		lst.clear();
		for(int i = 0; i < b.length; i++){
			lst.add(new Element(b[i],i));
		}
		//Collections.sort(lst, (e1,e2) -> e1.value.compareTo(e2.value) );
		
		double[] rankb = new double[b.length];
		for(int i = 0; i < lst.size(); i++){
			rankb[lst.get(i).id] = i;
		}
		
		return new PearsonsCorrelation().correlation(ranka, rankb);
		
//		int n = a.length;
//		double total = 0.0;
//		double coef = 6.0 / ((double)(n) * n * n - n);
//		for(int i = 0; i < n; i++){
//			total += (ranka[i] - rankb[i]) * (ranka[i] - rankb[i]) ; 
//		}
//		total *= coef;
//		
//		//System.out.printf("\n %.2f \n", 1.0 - total);
//		
//		return 1.0 - total;
	}
	
	static class Element{
		Double value;
		int id;
		
		public Element(Double v, int i){
			this.value = v;
			this.id = i;
		}
		
		
	}
	

	
}
