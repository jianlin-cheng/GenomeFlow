package edu.missouri.chenglab.loopdetection;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TTest;
import org.jmol.modelset.Atom;
import org.jmol.viewer.Viewer;

import edu.missouri.chenglab.gmol.Constants;
import edu.missouri.chenglab.lordg.utility.Helper;

public class Detector {
	
	private Helper helper = Helper.getHelperInstance();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
	}
	
	public void identifyLoop(Atom[] atoms, Viewer viewer){
		
		List<Loop> resultLoop = new ArrayList<Loop>();
		
		List<Atom> atomList = new ArrayList<Atom>();
		atomList.add(atoms[0]);
		for(int i = 1; i < atoms.length; i++){			
			if (atoms[i].chrID == atoms[i - 1].chrID) atomList.add(atoms[i]);
			else{//new chromosome
				Atom[] atomChrom = convertListToArray(atomList);
				resultLoop.addAll(identifyLoopInAChromosome(atomChrom));
				
				atomList = new ArrayList<Atom>();
				atomList.add(atoms[i]);
			}			
		}
		
		if (atomList.size() > 0){
			Atom[] atomChrom = convertListToArray(atomList);
			resultLoop.addAll(identifyLoopInAChromosome(atomChrom));
		}
		
		//String inputModelFile = viewer.getFullPathName();
		//viewer.loadNewModel(inputModelFile, String.format("%d loop%s identified", resultLoop.size(), resultLoop.size() > 1 ? "s":""));
		
		viewer.displayMessage(new String[]{String.format("%d loop%s identified", resultLoop.size(), resultLoop.size() > 1 ? "s":"")});
		
		//viewer.evalString("restrict bonds not selected;select not selected;wireframe 5;color structure");
		
		StringBuilder sb = new StringBuilder();
		for (Loop loop : resultLoop){
		    //cmd.spectrum(expression="count", palette="rainbow", selection="id " + str(loop.beg + 1) + "-" + str(loop.end + 1))
			//viewer.evalString(String.format("select atomno >= %d and atomno <= %d; wireframe 10; color group;", loop.atomBeg.index + 1, loop.atomEnd.index + 1));
			sb.append(String.format("select atomno >= %d and atomno <= %d; wireframe 10; color group;", loop.atomBeg.index + 1, loop.atomEnd.index + 1));			
		}
		viewer.evalString(sb.toString());
		
		
		viewer.evalString(String.format("select all;"));
		String outputFile = (String) viewer.getParameter(Constants.OUTPUTLOOPFILE);	
		if (outputFile != null && outputFile.length() > 0){
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(outputFile);
				for(Loop loop : resultLoop){
					pw.println("chr" + loop.atomBeg.chrID + "\t" + loop.atomBeg.fromPos + "\t" + loop.atomEnd.endPos);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}finally{			
				if (pw != null) pw.close();
			}
		}
		
	}
	
	private Atom[] convertListToArray(List<Atom> lst){
		Atom[] a = new Atom[lst.size()];
		for(int i = 0; i < lst.size(); i++){
			a[i] = lst.get(i);
		}
		return a;
	}
	
	public List<Loop> identifyLoopInAChromosome(Atom[] atoms){
		
		int length = atoms.length;
		

		double[][] coor = new double[length][3];
		for(int i = 0; i < length; i++){
			coor[i][0] = atoms[i].x;
			coor[i][1] = atoms[i].y;
			coor[i][2] = atoms[i].z;
		}
		

		DescriptiveStatistics ds = new DescriptiveStatistics();
		
		double[][] d = new double[length][length];
		double[] avg = new double[length];
		double[][] gap = new double[length][];
				
		for(int g = 0; g < length; g++){
			gap[g] = new double[length - g];
			int count=0;
			for(int i = 0; i < length - g; i++){
				int j = i + g;
				d[i][j] = distance(coor[i], coor[j]);
				d[j][i] = d[i][j];
				
				ds.addValue(d[i][j]);
				
				avg[g] += d[i][j];
				
				gap[g][count++] = d[i][j];
				
			}
			avg[g] /= (length - g);
		}
				

		double dthres = ds.getPercentile(25);
		    
		List<Loop> loops = new ArrayList<Loop>();
		
		TTest ttest = new TTest();
		
		int g = 5, k = 3;
		for(int i = 0; i < length; i++){
		    
		    double dmin = Double.MAX_VALUE;
		    int end = -1;
		    for (int j = i + g; j < length; j++){	        
		        if (d[i][j] < dmin && d[i][j] * k < avg[j-i] && d[i][j] * k < dthres && isloop(d, i, j) && ttest.tTest(d[i][j] * k, gap[j-i]) < 0.0000001){
		        	dmin = d[i][j];	        
		            end = j;
		        }
		    }
		                    
		    
		    if (end > -1){
		        Loop lp = new Loop(i, end,atoms[i], atoms[end], d[i][end]);
		        
		        loops.add(lp);
		    }
		}

		List<Loop> filteredLoops = filterLoop(loops, d);
		
		return filteredLoops;
 	
	}

	/**
	 * if 2 loops overlap, keep one with smallest end point distance    
	 * @return
	 */
	private List<Loop>  filterLoop(List<Loop> lstLoop, double[][] d){    
	    List<Loop> rs = new ArrayList<Loop>();
	    if (lstLoop.size() == 0) return rs;
	    
	    rs.add(lstLoop.get(0));
	    
	    for (int i = 1; i < lstLoop.size(); i++){
	        if (isOverlap(rs.get(rs.size() - 1), lstLoop.get(i))){
	        	
	        	if (d[rs.get(rs.size() - 1).beg][rs.get(rs.size() - 1).end] > d[lstLoop.get(i).beg][lstLoop.get(i).end])
	        	    rs.set(rs.size() - 1, lstLoop.get(i));
	        }else{
	            rs.add(lstLoop.get(i));
	        }
	    }
	    
	    return rs;
	}

	/**
	 * 
	 * @return
	 */
	private boolean isloop(double[][] dist, int p1,int p2){
	
	    int mid1 = (int)(p1 + (p2 - p1) * 0.25);
	    int mid2 = (int)(p1 + (p2 - p1) * 0.75);
	    int out1 = Math.max(0, p1 - 3);
	    int out2 = Math.min(dist.length - 1, p2 + 3);
			
	    if (mid1 - p1 <= 1) return false;
	    
	    if (out2 - p2 <= 1)	return false;	    		
        if (p1 - out1 <= 1) return false;
        if (p2 - mid2 <= 1) return false;
        
	    for (int i = out1;i <= mid1;i++){ 			
	       for(int j = mid2; j <= out2; j++)
	           if (dist[i][j] < dist[p1][p2]) return false;
	    }
	   
	    
//	    for(int i = out1; i < p1; i++){ 
//	        for(int j = mid2; j <= out2; j++)			
//	             if (dist[i][j] < dist[p1][p2]) return false;
//	            
//	    }
	    
	    return true;
	}
	    


	/**
	 * check if two loops overlap    
	 * @return
	 */
	private boolean isOverlap(Loop loop1, Loop loop2){    
	    return Math.min(loop1.end, loop2.end) > Math.max(loop1.beg, loop2.beg);
	}
	
	private double distance(double[] x, double[] y){
	    return Math.sqrt(helper.calEuclidianDist(x[0], x[1], x[2], y[0], y[1], y[2]));
	}
	    		
	class Loop{
		int beg = -1;//id of atomBeg in the region (or chromosome)
		int end = -1;
		Atom atomBeg;
		Atom atomEnd;
		double dist = 0.0;
		public Loop(int b, int e, Atom ab, Atom ae, double d){
			beg = b;
			end = e;
			atomBeg = ab;
			atomEnd = ae;
			dist = d;
		}
	}
}
