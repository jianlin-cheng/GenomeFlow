package edu.missouri.chenglab.hicdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;



public class Normalization {
	
	
	public static void normalize(String input_file, String output_file, int method) throws Exception{
		ArrayList<Integer> lstPos = new ArrayList<Integer>();
		HashMap<Integer,Integer> hmPos = new HashMap<Integer,Integer>();
		
		System.out.println("Reading input ...");
		double[][] a = readContactData(input_file, lstPos,hmPos);
		
		System.out.println("Performing normalization ...");
		if (method == 1){
			a = standardNorm(a);
		}else{
			a = ICENorm(a);
		}
		
		System.out.println("Writing out ...");
		writeOut(output_file,a,lstPos);
	}

	public static void writeOut(String fileOut, double[][] a, ArrayList<Integer> lstPos) throws Exception{
		PrintWriter pw = new PrintWriter(fileOut);
		for(int i = 0; i < a.length; i++){
			for(int j = i + 1; j < a.length; j++){
				if (a[i][j] > 0){
					pw.println(lstPos.get(i) + "\t" + lstPos.get(j) + "\t" + a[i][j]);
				}
			}			
		}
		pw.flush();
		pw.close();
		
	}	

	/**
	 * normalize using Iterative Correction 
	 * 
	 * https://liorpachter.wordpress.com/2013/11/17/imakaev_explained/
	 * 
    Calculate S_i=\sum_j W^k_{ij} and let \overline{S}_i = \frac{1}{n}\sum_{i=1}^n S_i.
    Set \Delta B^k_i = \frac{S_i}{\overline{S}_i}.
    Set W^{k+1}_{ij} = \frac{W^k_{ij}}{\Delta B_i \Delta B_j}.
    Set B^{k+1}_i = B^k_i \cdot \Delta B^k_i.


	 * @param a
	 * @return
	 */
	public static double[][] ICENorm(double[][] a){
		
		double total = 0.0;
		double[][] w = new double[a.length][a.length];
		
		for(int i = 0; i < a.length; i++){
			for(int j = 0; j < a.length; j++){
				w[i][j] = a[i][j];
			}
		}
		
		double[] s = new double[a.length];
		double[] b = new double[a.length];
		double mean;
		int count;
		
		for(int t = 0; t < 30; t++){
			
			for(int i = 0; i < w.length; i++){
				s[i] = sumVector(w[i]);
			}
			
			count = 0;
			for(int i = 0; i < w.length; i++){
				if (!Double.isNaN(s[i]) && s[i] > 0) {
					count++;
				}					
			}
			/*
			for(int i = 0; i < s.length; i++){
				System.out.printf("(%d, %.5f) , ", i,s[i]);
			}
			System.out.println();
			*/
			
			if (count == 0){
				System.out.println();
			}
			mean = sumVector(s) / count;
			//normalize by mean
			for(int i = 0; i < w.length; i ++){
				b[i] = s[i] / mean;
			}
			
			for(int i = 0; i < w.length; i++){
				for(int j = i+1; j < w.length; j++){
					
					if (b[i] <= 0 || b[j] <= 0) continue;
					
					w[i][j] = w[i][j] / (b[i] * b[j]);
					
					w[j][i] = w[i][j];
				}
			}			
		}
		
		//normalize elements so that sum of a row = 1
		//
		for(int i = 0; i < w.length; i++){
			s[i] = sumVector(w[i]);
		}
		
		
		for(int i = 0; i < w.length; i++){
			for(int j = i + 1; j < w.length; j++){
				if (s[i] <= 0) continue;
				w[i][j] /= s[i];
				w[j][i] = w[i][j];
			}
		}		
		for(int i = 0; i < a.length; i++){
			for(int j = i + 1; j < a.length; j++){
				if (!Double.isNaN(w[i][j])){
					total += w[i][j];		
				}
			}
		}		
		//scale normalized IF
		for(int i = 0; i < w.length; i++){
			for(int j = 0; j < w.length; j++){
				w[i][j] *= total;
			}
		}
		//
		
		
		return w;
	}


	
	/**
	 * 
	 * @param a: raw count
	 * @return normalized count matrix
	 */
	public static double[][] standardNorm(double[][] a){
		
		double[][] norm = new double[a.length][a.length];
		double[] s1 = new double[a.length];
		double total = 0;
		
		for(int i = 0; i < a.length; i++){
			s1[i] = sumVector(a[i]);
		}
		
		total = sumVector(s1) / 2;
		
		for(int i = 0; i < a.length; i++){
			for(int j = i + 1; j < a.length; j++){
				
				norm[i][j] = a[i][j] * total / (s1[i] * s1[j]);
				norm[j][i] = norm[i][j];
				
			}
		}
		
		return norm;
		
	}
	
	/**
	 * sum all elements of array
	 * @param a
	 * @return
	 */
	public static double sumVector(double[] a){
		double sum = 0.0;
		for(double d : a){
			if (!Double.isNaN(d)){
				sum = sum + d;
			}
		}
		return sum;
	}
	
	public static double[][] readContactData(String inputFile, ArrayList<Integer> lstPos, HashMap<Integer,Integer> hmPos )	throws FileNotFoundException, Exception{
		
		//contact matrix will be returned
		double[][] a;
		
		int x,y,id1,id2;
		double f;
		int nbr = 3; // number of numbers in each line
		
		File file = new File(inputFile);

		FileReader fr=null;
		BufferedReader br = null;		
		Pattern splitRegex = Pattern.compile("[:\\s]+");
		
		HashSet<Integer> posSet = new HashSet<Integer>();
		StringBuilder sb = new StringBuilder();
		try{
				
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String ln;
			String[] st;
			int count = 1;
			ln = br.readLine();
			sb.append(ln).append(" ");
			nbr = splitRegex.split(ln.trim()).length;
			long progress = 0;
			while((ln = br.readLine()) != null){
				if (ln.trim().length() == 0 || !Character.isDigit(ln.charAt(0)) ){
					continue;
				}
				
				//read every 10 thousand lines and split at once
				sb.append(ln).append(" ");
				count++;
				
				if (count == 200000){
					count = 0;
					st = splitRegex.split(sb.toString());
					sb = new StringBuilder();
					
					if (st.length % nbr != 0){
						throw new Exception("There is a line that doesn't contain exactly 3 numbers");
					}
					//each line contains 'nbr' numbers
					for(int i = 0; i < st.length / nbr; i++){
						
						//only take first 3 numbers
						x = Integer.parseInt(st[i * nbr + 0]);						
						//position2
						y = Integer.parseInt(st[i * nbr + 1]);
						
						//interaction frequency
						//f = Double.parseDouble(st[i * nbr + 2]);
						
						//keeping absolute positions, so that later they can be recovered from indices
						
						posSet.add(x);
						
						posSet.add(y);
						 
					}
				}
				progress++;
				//System.out.println(progress * 200000 + " input lines have been read !");
			}
			br.close();
			fr.close();
			
			//if sb is not empty
			if (sb.toString().trim().length() > 0){
				st = splitRegex.split(sb.toString());
				sb = new StringBuilder();
				
				if (st.length % nbr != 0){
					throw new Exception("There is a line that doesn't contain exactly 3 numbers");
				}
				//each line contains 3 numbers
				for(int i = 0; i < st.length / nbr; i++){
					x = Integer.parseInt(st[i * nbr + 0]);
					
					//position2
					y = Integer.parseInt(st[i * nbr + 1]);
					
					//keeping absolute positions, so that later they can be recovered from indices
					
					posSet.add(x);
					
					posSet.add(y);
					 
				}
				
			}

			lstPos.addAll(posSet);
			
			//sort the lst of position ascendingly
			Collections.sort(lstPos);			
			
			//map postion into absolute index
			for(int i = 0; i < lstPos.size(); i++){
				hmPos.put(lstPos.get(i), i);
			}
			
			//initialize the matrix contact
			a = new double[lstPos.size()][lstPos.size()];			
			
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			count = 0;
			progress = 0;
			while((ln = br.readLine()) != null){
				
				if (ln.startsWith("#") || ln.trim().length() == 0){
					continue;
				}
				
				//read every a thoudsand lines and split at once
				sb.append(ln).append(" ");
				count++;
				
				if (count == 200000){
					count = 0;
					st = splitRegex.split(sb.toString());
					sb = new StringBuilder();
					
					if (st.length % nbr != 0){
						throw new Exception("There is a line that doesn't contain exactly 3 numbers");
					}
					//each line contains 3 numbers
					for(int i = 0; i < st.length / nbr; i++){
						x = Integer.parseInt(st[i * nbr + 0]);
						
						//position2
						y = Integer.parseInt(st[i * nbr + 1]);
						
						//interaction frequency
						f = Double.parseDouble(st[i * nbr + 2]);
						
						//keeping absolute positions, so that later they can be recovered from indices
						
						id1 = hmPos.get(x);
						id2 = hmPos.get(y);
												
						//if the frequency is less than thresholds, ignore the contact so that it is considered as non-contact
						

						a[id1][id2] = f;
						a[id2][id1] = f;
						
						 
					}
					
					progress++;
					//System.out.println(progress * 200000 + " input lines have been read - second time!");
				}	

			}

			//if sb is not empty
			if (sb.toString().trim().length() > 0){
				st = splitRegex.split(sb.toString());
				sb = new StringBuilder();
				
				if (st.length % nbr != 0){
					throw new Exception("There is a line that doesn't contain exactly 3 numbers");
				}
				//each line contains 3 numbers
				for(int i = 0; i < st.length / nbr; i++){
					x = Integer.parseInt(st[i * nbr + 0]);
					
					//position2
					y = Integer.parseInt(st[i * nbr + 1]);
					
					//interaction frequency
					f = Double.parseDouble(st[i * nbr + 2]);
					
					//keeping absolute positions, so that later they can be recovered from indices
					
					id1 = hmPos.get(x);
					id2 = hmPos.get(y);
					
					a[id1][id2] = f;
					a[id2][id1] = f;
					
					 
				}
				
			}

		
		}catch(FileNotFoundException ex){
			ex.printStackTrace();
			throw ex;
		}catch(IOException ex){
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


}
