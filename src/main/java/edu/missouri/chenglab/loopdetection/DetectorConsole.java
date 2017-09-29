package edu.missouri.chenglab.loopdetection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.inference.TTest;

import edu.missouri.chenglab.loopdetection.utility.CommonFunctions;
import edu.missouri.chenglab.lordg.utility.Helper;

public class DetectorConsole {
	
	private static Helper helper = Helper.getHelperInstance();
	
	public static void main(String[] args) throws Exception {
		
		//peakDetect("C:/Users/Tuan/workspace/LoopDetectionAndAnalysis/python_script/chr1_10kb_gm12878_list_0mb_10mb_1483058775167.pdb");
		
//		String modelFolder = "/Users/Tuan/git/GenomeMDS/GenomeMDS/output/imr90/10kb/";
//		String all_regions_file = modelFolder + "/imr90.bed";
		
		String modelFolder = "C:/Users/Tuan/workspace/LoopDetectionAndAnalysis/output/gm12878/10kb/";
		//String modelFolder = "C:/Users/Tuan/git/GenomeMDS/GenomeMDS/output/gm12878/test/";
			
		String all_regions_file = modelFolder + "/gm12878.bed";
		
		for(int k = 1; k <= 23; k++) {
			
			String chrom = "";
			if (k < 23) chrom = "chr" + k;
			else chrom = "chrX";
			
			String inputFolder = modelFolder + chrom +"/";
			String outputFile = inputFolder + "/" + chrom + "_loop_region.bed" ;
			
			if (!CommonFunctions.isExist(inputFolder)) continue;
			
			//peakDetect(inputFile);
			
			int i = 0;
			String folderName;
			ArrayList<Point> lst = new ArrayList<Point>();
			while(true){
				if (i == 0) folderName = "0_10";
				else folderName = (i * 10 - 2) + "_" + (i + 1) * 10;
				
				File f = new File(inputFolder + "/" + folderName);
				if (!f.exists()){
					break;
				}		
				
				lst.addAll(getLoopRegions(inputFolder + "/" + folderName));
				
				i++;
				//if (i == 3) break;
			}
			
			PrintWriter pw = new PrintWriter(outputFile);
			for(Point p : lst){
				pw.printf("%s\t%s\t%s\n", chrom, p.labelX, p.labelY);				
			}
			pw.close();
			
		}
		
		PrintWriter pw = new PrintWriter(all_regions_file);
		for(int k = 1; k <= 23; k++) {
			
			String chrom = "";
			if (k < 23) chrom = "chr" + k;
			else {
				chrom = "chrX";
			}
			
			String inputFolder = modelFolder + "/" + chrom +"/";
			String outputFile = inputFolder + "/" + chrom + "_loop_region.bed" ;
			
			if (!CommonFunctions.isExist(inputFolder)) continue;
			
			BufferedReader br = new BufferedReader(new FileReader(outputFile));
			String ln;
			while((ln = br.readLine()) != null){
				pw.println(ln);
			}
			br.close();
		}
		
		pw.close();

		
				
	}
	
	public static ArrayList<Point> getLoopRegions(String folderName) throws Exception{
		File folder = new File(folderName);
		
		File[] files = folder.listFiles();
		ArrayList<Point> lst = new ArrayList<Point>();
		String coor_mapping_file = "";
		for(File f : files){
			if (f.getName().endsWith(".pdb")){
				lst = merge(peakDetect(f.getAbsolutePath()), lst);
				//lst = peakDetect(f.getAbsolutePath());
				//System.out.println(f.getName());
				
			}else if (f.getName().contains("coordinate_mapping")){
				coor_mapping_file = f.getAbsolutePath();
			}
			
		}
		if (lst.size() == 0) return lst;
		
		HashMap<Integer, Integer> coor_map = getCoorMapping(coor_mapping_file);
		
		PrintWriter pw = new PrintWriter(folderName + "/loop_coloring.txt");		
		
		for(Point p : lst){
			p.labelX = coor_map.get(p.x);
			p.labelY = coor_map.get(p.y);			
			pw.println("spectrum count, rainbow, id " + (p.x + 1) + "-" + (p.y + 1));
		}
		
		for(Point p : lst){
			p.labelX = coor_map.get(p.x);
			p.labelY = coor_map.get(p.y);			
			pw.print("spectrum count, rainbow, id " + (p.x + 1) + "-" + (p.y + 1) + ";");
		}
		
		pw.close();
		
		return lst;
	}
	
	
	public static HashMap<Integer, Integer> getCoorMapping(String fileName) throws Exception{
		
		BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
		String ln;
		String[] st;
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		while((ln = br.readLine()) != null){
			st = ln.split("\\t");			
			map.put(Integer.parseInt(st[1]), Integer.parseInt(st[0]));
		}
		br.close();
		
		return map;
	}
	
	/**
	 * 
	 * @param cor is a Nx3 matrix with each point is represented by 3 coordinates 
	 * @return
	 */
	public static ArrayList<Point> peakDetect(String fileName) throws Exception{
		
		double[][] cor = helper.loadPDBStructure(fileName);
		
		ArrayList<Point> lst = new ArrayList<Point>();
		
		int n = cor.length;
		double[][] distance = new double[n][n];
		
		if (n == 0) return lst;
		
		ArrayList<Double> lstDist = new ArrayList<Double>();
		
		for(int i = 0; i < n; i++){
			for(int j = i + 1; j < n; j++){
				distance[i][j] = Math.sqrt(helper.calEuclidianDist(cor[i][0], cor[i][1], cor[i][2], cor[j][0], cor[j][1], cor[j][2]));
				distance[j][i] = distance[i][j];
				
				lstDist.add(distance[i][j]);
			}
		}
		
		//avg_adjacent /= (n - 1);
		
		
		//distances along at gap = 1, 2, ... n - 1 (0 is ignore)
		double[][] gap_distance = new double[n][];
		double[] mean_gap_distance = new double[n];
		for(int gap = 1; gap < n; gap++){
			gap_distance[gap] = new double[n - gap];
			for(int i = 0; i < n - gap; i++){
				gap_distance[gap][i] = distance[i][i + gap];
			}
			
			mean_gap_distance[gap] =  StatUtils.mean(gap_distance[gap]);
		}
		
		
		
		Collections.sort(lstDist);
		double thres = lstDist.get((int)(lstDist.size() * 0.25));
		
		int end,gap = 5;//peaks are at least 5 points away from each other
		double d;
		int k = 3;
		
		TTest ttest = new TTest();
		for(int i = 0; i < n; i++){
			//for each i, pick only one end point with shortest distance to make loop
			end = -1;
			d = Double.MAX_VALUE; 
			
			for(int j = i + gap; j < n; j++){
				
				
				//if (distance[i][j] < gap_avg_distance[j - i] * 0.33 && distance[i][j] < thres ){
				if (distance[i][j] * k < thres 
						&& isLoop(distance, i, j) && distance[i][j] < d 
						&& distance[i][j] * k < mean_gap_distance[j-i]
						&& ttest.tTest(distance[i][j] * k, gap_distance[j - i]) < 0.0000001){

						//System.out.printf("spectrum count, rainbow, id %d-%d \n", i + 1, j + 1);
						if (distance[i][j] < d){
							d = distance[i][j];
							end = j;
						}
						
				}
			}
			
			if (end > -1){
				insert(lst, new Point(i,end), distance);
				//System.out.printf("spectrum count, rainbow, id %d-%d \n", i + 1, end + 1);
				//i = end + 1;
			}
		}		
		
		lst = filterLoop(lst, distance);
		
		for(int i = 0; i < lst.size(); i++){
			System.out.printf("spectrum count, rainbow, id %d-%d \n", lst.get(i).x + 1, lst.get(i).y + 1);
		}
		
		return lst;		
	}
	
	/**
	 * if two loops overlap, keep the one with smaller end-point distance
	 * @param loop
	 * @param d
	 * @return
	 */
	public static ArrayList<Point> filterLoop(ArrayList<Point> loop, double[][] d){
		
		if (loop.size() == 0) return loop;
		
		ArrayList<Point> rs = new ArrayList<Point>();
		rs.add(loop.get(0));
		for(int i = 1; i < loop.size(); i++){
			Point p1 = rs.get(rs.size() - 1);
			Point p2 = loop.get(i);
			if (Math.min(p1.y, p2.y) > Math.max(p1.x, p2.x)){
				if (d[p1.x][p1.y] > d[p2.x][p2.y]){
					rs.set(rs.size() - 1, p2);
				}
			}else{
				rs.add(p2);
			}			
		}
		
		return rs;		
	}
	
	/**
	 * 
	 * @param dist: NxN matrix of distances 
	 * @param p1 < p2
	 * @param p2
	 * @return
	 */
	public static boolean isLoop(double[][] dist, int p1, int p2){
		
//		if (p1 == 828 && p2 == 858){
//			System.out.println();
//			
//			double curveDist = 0;
//			for(int i = p1; i < p2; i++){
//				curveDist += dist[i][i + 1];
//			}
//			
//			System.out.println(curveDist + "\t" + dist[p1][p2]);
//		}
		
		int mid1 = p1 + (p2 - p1) * 1 / 4;
		int mid2 = p1 + (p2 - p1) * 3 / 4;
		int out1 = Math.max(0, p1 - 3);
		int out2 = Math.min(dist.length - 1, p2 + 3);
		
		double buffer = 0.2;
		
		//p1-mid1 < mid2-p2
		//p1-mid1 < p2-out2
		if (mid1 - p1 <= 1) return false;
		for(int i = p1; i <= mid1; i++){
			//p1-mid1 < mid2-p2
			if (p2 - mid2 <= 1) return false;
			int count = 0;
			for(int j = mid2; j <= p2; j++){				
				if (dist[i][j] < dist[p1][p2]){
					count++;
					//return false;
				}				
			}
			if (count > (p2 - mid2 + 1) * buffer) return false; //allow 10% smaller than dist[p1][p2] 
			
			//p1-mid1 < p2-out2
			if (out2 - p2 <= 1) return false;
			count = 0;
			for(int j = p2; j <= out2; j++){
				if (dist[i][j] < dist[p1][p2]){
					count++;
					//return false;
				}
			}	
			if (count > (out2 - p2 + 1) * buffer) return false;
		}
		
		//out1-p1 < mid2-p2
		//out1-p1 < p2-out2
		if (p1 - out1 <= 1) return false;
		for(int i = out1; i < p1; i++){
			//out1-p1 < mid2-p2		
			int count = 0;
			for(int j = mid2; j <= p2; j++){
				if (dist[i][j] < dist[p1][p2]){
					count++;
					//return false;
				}
			}
			if (count > (p2 - mid2 + 1) * buffer) return false;
			
			//out1-p1 < p2-out2
			count = 0;
			for(int j = p2; j <= out2; j++){
				if (dist[i][j] < dist[p1][p2]) {
					count++;
					//return false;
				}				
			}	
			if (count > (out2 - p2 + 1) * buffer) return false;
		}
				
		return true;
	}
	
	
	//filter regions that include smaller ones
	//only keep smallest regions
	public static void filter(ArrayList<Point> lst){
		ArrayList<Point> removedPoint = new ArrayList<Point>();
		
		for(int i = 0; i < lst.size(); i++){
			boolean isContaining = false;
			for(int j = 0; j < lst.size(); j++){
				//if i contains j
				if (i != j && lst.get(i).x <= lst.get(j).x && lst.get(j).y <= lst.get(i).y){
					isContaining = true;
					break;
				}
			}
			
			if (isContaining){
				//lst.remove(i);
				//i--;
				removedPoint.add(lst.get(i));
			}			
		}
		
		for(Point p : removedPoint){
			lst.remove(p);
		}
		
	}
	
	//merge 2 lists into one, two overlap regions will be merged into one bigger region
	public static ArrayList<Point> merge(ArrayList<Point> lst1, ArrayList<Point> lst2){
		
		if (lst2.size() < lst1.size()) return merge(lst2, lst1);
		
		ArrayList<Point> lst = new ArrayList<Point>();
		int i = 0, j = 0;
		//System.out.println("List_length:" + lst1.size() + "\t" + lst2.size());
		while (i < lst1.size() && j < lst2.size() ){
			if (lst1.get(i).y < lst2.get(j).x){
				lst.add(lst1.get(i));
				i++;
			}else if (lst2.get(j).y < lst1.get(i).x){
				lst.add(lst2.get(j));
				j++;
			}else{
				if (overlap(lst1.get(i), lst2.get(j)) > 50){ // if overlap is more than 50%, merge them into one					
					lst.add(new Point(Math.min(lst1.get(i).x, lst2.get(j).x), Math.max(lst1.get(i).y, lst2.get(j).y)) );
				}else{
					//if a region is within the other, include the smaller one only
					if (isContaining(lst1.get(i), lst2.get(j))){
						lst.add(lst2.get(j));
					}else if (isContaining(lst2.get(j), lst1.get(i))){
						lst.add(lst1.get(i));
					}else{//ambiguous loops, ignore boths 
						lst.add(lst1.get(i));
						lst.add(lst2.get(j));
					}
				}
				
				i++;
				j++;
			}			
		}
		
		while (j < lst2.size()){
			lst.add(lst2.get(j));
			j++;
		}
		
		while (i < lst1.size()){
			lst.add(lst1.get(i));
			i++;
		}
		
		return lst;
	}
	
	//if p1 is containing p2
	public static boolean isContaining(Point p1, Point p2){
		return (p1.x <= p2.x && p2.y <= p1.y);
	}
	
	//insert a peak point into the list, if this peak point overlap more than 50% with a previous one, then, keep one with smaller distance
	public static void insert(ArrayList<Point> lst, Point peak, double[][] distance){		
		
		int n = lst.size();
		
		if (n == 0) lst.add(peak);
		
		else if (overlap(lst.get(n - 1), peak) >= 5.0){
			
			if (distance[peak.x][peak.y] < distance[lst.get(n - 1).x][lst.get(n - 1).y]) lst.set(n - 1,  peak);
			//else discard this new loop because it is not better than previous one
			
		}else{
			lst.add(peak);
		}		
	}
	
	public static double overlap(Point p1, Point p2){
		//overlap length
		int len = Math.min(p1.y, p2.y) - Math.max(p1.x, p2.x);
		
		if (len <= 0) return 0.0;
		
		//return the percentage of overlap 
		return Math.max( len * 100.0 / (p1.y - p1.x), len * 100.0 / (p2.y - p2.x));
	}
	
	
	
	static class Point implements Comparable<Point>{
		int x,y; //indices
		int labelX, labelY;//labels
		public Point(int xx, int yy){
			this.x = xx;
			this.y = yy;
			
			//so when sorting using label, x,y can be use if labels are not supplied
			this.labelX = xx;
			this.labelY = yy;
		}
		@Override
		public int compareTo(Point p) {
			
			if (this.x == p.x) return this.y - p.y;
			
			return (this.x - p.x);
		}
	}

}
