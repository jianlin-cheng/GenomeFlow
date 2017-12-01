package edu.missouri.chenglab.gmol.modelcomparison;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
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
import org.jmol.adapter.smarter.Atom;

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
		String inputFile2 = "C:/Users/Tuan/workspace/Gmol/output/chr11_10kb_gm12878_list_125mb_135mb_1512068595242.gss";
		
		Comparison comp = new Comparison();
		
		comp.compare(inputFile1, inputFile2);
	}
	
	public void compare(String inputFile1, String inputFile2) throws Exception{
		
		List<AtomRegion> atomList1 = readModel(inputFile1);
		List<AtomRegion> atomList2 = readModel(inputFile2);
		
		Set<AtomRegion> set1 = new HashSet<AtomRegion>(atomList2);
		Set<AtomRegion> set2 = new HashSet<AtomRegion>(atomList2);
		
		atomList1 = atomList1.stream().filter(a -> set2.contains(a)).collect(Collectors.toList());
		atomList2 = atomList2.stream().filter(a -> set1.contains(a)).collect(Collectors.toList());
			
		center(atomList1);
		center(atomList2);
		
		
		
		
		RealMatrix matrix1 = getCoordinateMatrix(atomList1);
		RealMatrix matrix2 = getCoordinateMatrix(atomList2);
		
		RealMatrix mt = matrix1.transpose().multiply(matrix2).scalarMultiply(1.0/matrix1.getRowDimension());
		
		SingularValueDecomposition svd = new SingularValueDecomposition(mt);
		
		RealMatrix u = svd.getU();
		RealMatrix v = svd.getV();
		RealMatrix s = svd.getS();
		
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
		
		System.out.println(t);
		
		
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
