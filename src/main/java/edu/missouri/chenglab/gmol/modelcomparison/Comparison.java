package edu.missouri.chenglab.gmol.modelcomparison;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.jmol.adapter.smarter.Atom;
import org.jmol.viewer.Viewer;

import edu.missouri.chenglab.gmol.Constants;
import edu.missouri.chenglab.loopdetection.utility.CommonFunctions;
import edu.missouri.chenglab.lordg.utility.Helper;

/**
 * This class is to calculate RMSE and Spearman's correlation for 2 models in GSS files
 * Only loci in both models are considered
 * Models are scaled and transform to minimize their RMSE
 * @author Tuan
 *
 */
public class Comparison {
	
	public static void main(String[] args) throws Exception{
		String inputFile1 = "C:/Users/Tuan/workspace/Gmol/output/chr11_10kb_gm12878_list_125mb_135mb_1512086718696.gss";
		//String inputFile2 = "C:/Users/Tuan/workspace/Gmol/output/chr11_10kb_gm12878_list_125mb_135mb_1512086718696.gss";
		String inputFile2 = "C:/Users/Tuan/workspace/Gmol/output/chr11_10kb_gm12878_list_125mb_135mb_1512068595242.gss";
		
		Comparison comp = new Comparison();
		
		comp.compare(null, inputFile1, inputFile2);
	}
	
	public void compare(Viewer viewer, String inputFile1, String inputFile2) throws Exception{
		
		List<AtomRegion> atomList1 = readModel(inputFile1);
		List<AtomRegion> atomList2 = readModel(inputFile2);
		
		Set<AtomRegion> set1 = new HashSet<AtomRegion>(atomList2);
		Set<AtomRegion> set2 = new HashSet<AtomRegion>(atomList2);
		
		atomList1 = atomList1.stream().filter(a -> set2.contains(a)).collect(Collectors.toList());
		atomList2 = atomList2.stream().filter(a -> set1.contains(a)).collect(Collectors.toList());
		
		if (atomList1.size() == 0){
			viewer.displayMessage(new String[]{"Check your models!!!, they have no common regions!"});
			return;
		}
		
		normalize(atomList2);
		
		center(atomList1);
		center(atomList2);
		
		
		RealMatrix matrix1 = getCoordinateMatrix(atomList1);
		RealMatrix matrix2 = getCoordinateMatrix(atomList2);
		
		/*
		double[][] a = matrix1.getData();
		PrintWriter pw = new PrintWriter("matrix1.txt");
		for(int i = 0; i < a.length; i++){
			for(int j = 0;j < a[0].length; j++){
				pw.printf("%.5f ", a[i][j]);
			}
			pw.println();
		}
		pw.close();
		
		a = matrix2.getData();
		pw = new PrintWriter("matrix2.txt");
		for(int i = 0; i < a.length; i++){
			for(int j = 0;j < a[0].length; j++){
				pw.printf("%.5f ", a[i][j]);
			}
			pw.println();
		}
		pw.close();
		*/
		
		RealMatrix mt = ((matrix1.transpose()).multiply(matrix2)).scalarMultiply(1.0/matrix1.getRowDimension());
		
		SingularValueDecomposition svd = new SingularValueDecomposition(mt);
		
		RealMatrix u = svd.getU();
		RealMatrix v = svd.getVT();
		RealMatrix s = svd.getS();
		
		//RealMatrix test = u.multiply(s).multiply(v).subtract(mt);
		
		LUDecomposition decompositionU = new LUDecomposition(u);
		LUDecomposition decompositionV = new LUDecomposition(v);
		double uDeterminant = decompositionU.getDeterminant();
		double vDeterminant = decompositionV.getDeterminant();
		
		if (uDeterminant * vDeterminant < 0){
			s.multiplyEntry(2, 2, -1);
			
			u.multiplyEntry(0, 2, -1);
			u.multiplyEntry(1, 2, -1);
			u.multiplyEntry(2, 2, -1);
		}
		
		RealMatrix r = u.multiply(v);
		
		double varSum = StatUtils.variance(matrix1.getColumn(0)) + StatUtils.variance(matrix1.getColumn(1)) + StatUtils.variance(matrix1.getColumn(2));
		
		double scale = 1.0 / varSum * (s.getEntry(0, 0) + s.getEntry(1, 1) + s.getEntry(2, 2));
		
		RealMatrix t = MatrixUtils.createRowRealMatrix(new double[]{StatUtils.mean(matrix1.getColumn(0)), StatUtils.mean(matrix1.getColumn(1)), StatUtils.mean(matrix1.getColumn(2))}).subtract(
				MatrixUtils.createRowRealMatrix(new double[]{StatUtils.mean(matrix2.getColumn(0)), StatUtils.mean(matrix2.getColumn(1)), StatUtils.mean(matrix2.getColumn(2))}).multiply(r.scalarMultiply(scale)));
		
		
		RealMatrix matrix1Converted = matrix1.multiply(r.scalarMultiply(scale));
		
		//translate
		for(int i = 0; i < matrix1Converted.getRowDimension(); i++){
			RealMatrix tmp = matrix1Converted.getRowMatrix(i).subtract(t);		
			matrix1Converted.setRowMatrix(i, tmp);
		}
		
		
//		RealMatrix errorMatrix = matrix1Converted.subtract(matrix2);
//		double totalError1 = 0;
//		for(int i = 0; i < errorMatrix.getRowDimension(); i++){
//			totalError1 += StatUtils.sumSq(errorMatrix.getRow(i));
//		}		
//		System.out.println("Total error :" + totalError1);
		
		
		Helper helper = Helper.getHelperInstance();
		
		double[] str = new double[matrix2.getRowDimension() * 2 * 3];
		
		List<Integer> lstPos = new ArrayList<Integer>();
		HashMap<Integer, Integer> idToChr = new HashMap<Integer,Integer>();
		String chrom = "1";
		String genomeID = "-";
		
		
		int k = 0;
		for(int i = 0; i < matrix2.getRowDimension(); i ++){
			
			idToChr.put(k/3, 1); 
			
			str[k++] = matrix2.getRow(i)[0];
			str[k++] = matrix2.getRow(i)[1];
			str[k++] = matrix2.getRow(i)[2];	
			
			lstPos.add(atomList1.get(i).fromPos);
		}
		
		for(int i = 0; i < matrix1Converted.getRowDimension(); i ++){
			
			idToChr.put(k/3, 2);
			
			str[k++] = matrix1Converted.getRow(i)[0];
			str[k++] = matrix1Converted.getRow(i)[1];
			str[k++] = matrix1Converted.getRow(i)[2];
			
			lstPos.add(atomList2.get(i).fromPos);
		}
		  
		String outputFileGSS = "superimpose_" + CommonFunctions.getFileNameFromPath(inputFile1).replace(".gss", "") 
				+ CommonFunctions.getFileNameFromPath(inputFile2).replace(".gss", "") + ".gss";
		helper.writeStructureGSS(outputFileGSS, str, lstPos, idToChr, chrom, genomeID);
		
		int n = matrix1Converted.getRowDimension();
		double[] dist1 = new double[n * (n - 1) / 2];
		double[] dist2 = new double[n * (n - 1) / 2];
		
		SpearmansCorrelation spearmanCor = new SpearmansCorrelation();
		
		
		k = 0;
		double totalError = 0.0;
		for(int i = 0; i < n; i++){
			for(int j = i + 1; j < n; j++){
				dist1[k] = Math.sqrt(helper.calEuclidianDist(matrix1Converted.getRow(i)[0],  matrix1Converted.getRow(i)[1], matrix1Converted.getRow(i)[2], 
						matrix1Converted.getRow(j)[0],  matrix1Converted.getRow(j)[1], matrix1Converted.getRow(j)[2]));
				
				
				dist2[k] = Math.sqrt(helper.calEuclidianDist(matrix2.getRow(i)[0], matrix2.getRow(i)[1], matrix2.getRow(i)[2], 
						matrix2.getRow(j)[0], matrix2.getRow(j)[1], matrix2.getRow(j)[2]));
				
				totalError += (dist1[k] - dist2[k]) * (dist1[k] - dist2[k]);
				k++;
			}
		}
		
		totalError = Math.sqrt(totalError) / dist1.length;
		
		//totalError /= Constants.AVG_DIST_MODEL;
		
		double cor = spearmanCor.correlation(dist1, dist2);
		
		viewer.loadNewModel(outputFileGSS, new String[]{String.format("RMSE: %.8f",totalError), String.format("Spearman correlation: %.4f",cor)});
		
		viewer.evalString(String.format("select atomno >= %d and atomno <= %d; wireframe 5; color group;select atomno >= %d and atomno <= %d;wireframe 15; color group",1,n, n+1, n + n));
		
		//CommonFunctions.delete_file(outputFileGSS);
		
	}
	
	private RealMatrix getCoordinateMatrix(List<AtomRegion> atomList){
		double[][] a = new double[atomList.size()][3];
		for(int i = 0; i < atomList.size(); i++){
			a[i][0] = atomList.get(i).x;
			a[i][1] = atomList.get(i).y;
			a[i][2] = atomList.get(i).z;
		}
		
		return MatrixUtils.createRealMatrix(a);
	}

	/**
	 * Normalize models so that RMSE is comparable for different cases
	 * @param atomList
	 */
	private void normalize(List<AtomRegion> atomList){
		
		
		Helper helper = Helper.getHelperInstance();
		//make average adjacent distance = 10
		double avgDist = 0;
		int count = 0;
		for(int i = 1; i < atomList.size(); i++){
			if (atomList.get(i).chrID == atomList.get(i - 1).chrID){
				avgDist += helper.calEuclidianDist(atomList.get(i).x, atomList.get(i).y, atomList.get(i).z, 
						atomList.get(i-1).x, atomList.get(i-1).y, atomList.get(i-1).z);
				count++;
			}
		}
		
		avgDist /= count;
		
		double ratio = Constants.AVG_DIST_MODEL/avgDist;
		
		for(AtomRegion atom: atomList){
			atom.x *= ratio;
			atom.y *= ratio;
			atom.z *= ratio;
		}
	}
	
	private void center(List<AtomRegion> atomList){
		double[] center = new double[3];
		
		for(AtomRegion atom: atomList){
			center[0] += atom.x;
			center[1] += atom.y;
			center[2] += atom.z;
		}
		center[0] /= atomList.size();
		center[1] /= atomList.size();
		center[2] /= atomList.size();
		
		
		for(AtomRegion atom: atomList){
			atom.x -= center[0];
			atom.y -= center[1];
			atom.z -= center[2];
		}
	}
	

	/**
	 * 
	 * @param inputFile: a .gss file contains a 3D model
	 * @return list of atoms, each represents a point in the 3D model
	 */
	public List<AtomRegion> readModel(String inputFile) throws Exception{
		
		List<AtomRegion> atomsList = new ArrayList<AtomRegion>();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(inputFile)));
		String line;
		int chrID=1, start, end;
		float x, y, z;
		while((line = br.readLine()) != null){
			
	    	  
		      Pattern patternChrScaleLine = Pattern.compile("(^<cs>)(\\d+)");
		      //Pattern patternLengthLine = Pattern.compile("(^<lt>)(\\d+)(</lt>)");		      
		      Pattern patternNucleoLineWseq = Pattern.compile("(^<un\\s)(\\d+)(>)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(\\s)([\\d.-]+)(</un><seq>)([\\d]+)(\\s)([\\d]+)(</seq>)");		      
		      //Pattern patternSpLine = Pattern.compile("(<sp>)(.*)(</sp>)");
		      //Pattern patternEnsLine = Pattern.compile("(<ens-chr>)([A-Za-z0-9]+)(</ens-chr>)");
		      
		      
		      Matcher matcherChs = patternChrScaleLine.matcher(line);
		      //Matcher matcherLt = patternLengthLine.matcher(line);		      
		      Matcher matcherNulWseq = patternNucleoLineWseq.matcher(line);
		      
		      //Matcher matcherSpLine = patternSpLine.matcher(line);
		      //Matcher matcherEnsLine = patternEnsLine.matcher(line);
		      
		      
		      if (matcherChs.find()) {
				    chrID = Integer.parseInt(matcherChs.group(2));			  	
			  }else if (matcherNulWseq.matches()) {
					//sequenceNum = Integer.parseInt(matcherNulWseq.group(2));
					x = Float.parseFloat(matcherNulWseq.group(4));
					y = Float.parseFloat(matcherNulWseq.group(6));
					z = Float.parseFloat(matcherNulWseq.group(8));
					//unitRadius = Float.parseFloat(matcherNulWseq.group(10));
					start = Integer.parseInt(matcherNulWseq.group(12));
					end = Integer.parseInt(matcherNulWseq.group(14));
					
					AtomRegion atom = new AtomRegion();
					atom.chrID = chrID;
					atom.x = x;
					atom.y = y;
					atom.z = z;
					atom.fromPos = start;
					atom.endPos = end;
					
					atomsList.add(atom);
			  }
			
		}
		
		br.close();		
		
		return atomsList;
	}
	
	
	class AtomRegion extends Atom{
		@Override
		public boolean equals(Object o) {
			
			if (!(o instanceof AtomRegion)) return false;				
			
			AtomRegion atom = (AtomRegion) o;			
			
			return atom.chrID == this.chrID && atom.fromPos == this.fromPos && atom.endPos == this.endPos;
		}
		
		@Override
		public int hashCode(){
			return this.chrID + this.fromPos + this.endPos;
		}
		
		
	}
}
