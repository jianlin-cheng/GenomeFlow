package  edu.missouri.chenglab.Structure3DMax.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import shrec3D.Evaluate;
import edu.missouri.chenglab.Structure3DMax.utility.Helper;
import edu.missouri.chenglab.Structure3DMax.valueObject.Constraint;

/**
 * To calculate RMSD of a reconstructed structure and wish distances
 * 
 *
 */
public class CalRMSD {
	
	private static Helper helper = Helper.getHelperInstance();
	
	//calculate correlation between CNS's structures and input contact matrix
	public static void main(String[] args) throws Exception{
		/*//String strFile = "C:/Users/Tuan/workspace/GenomeMDS/output/chr1_25kb_list_1437410459558.pdb";
		//String strFile = "C:/Users/Tuan/workspace/3DGenome/output/lieber2014/chr1_500kb_list_1437430703807.pdb";
		String strFile = "/users/Tuan/workspace/CNS/chainDres5_Matrix_noise400/extended.pdb";
		String contactFile = "/users/Tuan/workspace/DataAndEvaluation/output/SyntheticYeast/pow1/dist30_50noise/chainDres5_Matrix_noise400.txt";
		
		double[][] str = helper.loadPDBStructure(strFile);
		List<Integer> lstPos = new ArrayList<Integer>();
		List<Constraint> lstCons = helper.readContactList(contactFile, lstPos);
		//List<Constraint> lstCons = helper.readContactMatrixAsList(contactFile);
		
		Map<Integer,Integer> mapPosToID = new HashMap<Integer,Integer>();
		for(int i = 0; i < lstPos.size(); i++){
			mapPosToID.put(lstPos.get(i), i);
		}
		

		for(Constraint con: lstCons){
			//System.out.println(con.getPos1() + "\t" + mapPosToID.get(con.getPos1()));
			//System.out.println(con.getPos2() + "\t" + mapPosToID.get(con.getPos2()));
			con.setPos1(mapPosToID.get(con.getPos1()));
			con.setPos2(mapPosToID.get(con.getPos2()));
			//System.out.println(con.getPos1() + "\t" + con.getPos2() + "\t" + con.getIF());		
		
		}
		
		int n = str.length;
		double[] str1 = new double[n * 3];
		for(int i = 0; i < n; i++){
			str1[i * 3] = str[i][0];
			str1[i * 3 + 1] = str[i][1];
			str1[i * 3 + 2] = str[i][2];
		}	

		System.out.println(correlationIFvsDist(str1, lstCons,0));
	*/
			
	}
	
	
	
	/**
	 * Calculate pairwise distance matrix from a structure
	 * @param str
	 * @return
	 */
	public static double[][] calculateDistMT(double[] str){		
		int n = str.length / 3;
		double[][] dist = new double[n][n];
		
		for(int i = 0; i < n; i++){
			for(int j = i + 1; j < n; j++){
				dist[i][j] = helper.calEuclidianDist(str[i * 3], str[i * 3 + 1], str[i * 3 + 2], str[j * 3], str[j * 3 + 1], str[j * 3 + 2]);
				dist[j][i] = dist[i][j];
			}
		}
		
		return dist;
		
	}
	public static double rmsd(double[][] dist, List<Constraint> lstCon){
		double sum = 0.0;
		int i,j,n = dist.length;
		for(Constraint con:lstCon){
			i = con.getPos1();
			j = con.getPos2();
			sum += (dist[i][j] - con.getDist()) * (dist[i][j] - con.getDist());
		}
		
		sum /= (n - 1) * n / 2;
		
		return sum;
	}

	public static double correlationIFvsDist(double[][] dist, List<Constraint> lstCon, int interval) throws Exception{
		double[][] conMT = new double[dist.length][dist.length];
		for(Constraint con:lstCon){
			conMT[con.getPos1()][con.getPos2()] = con.getIF();
			conMT[con.getPos2()][con.getPos1()] = conMT[con.getPos1()][con.getPos2()];
		}
		
		for(int i = 0; i < dist.length; i++){
			for(int k = 0; k < interval; k++){
				if (i + k < dist.length){
					conMT[i][i + k] = 0;
					conMT[i + k][i] = 0;
					
					dist[i][i + k] = 0;
					dist[i + k][i] = 0;
				}
			}
		}
		
		return Evaluate.calSpearmanCorrelation(dist, conMT);
	}
	
	public static double correlationIFvsDist(double[] str, List<Constraint> lstCon, int interval) throws Exception{		
		
		int n = str.length / 3;// number of points
		int len = n * (n - 1) / 2;
		
		
		double[] dist = new double[len];
		double[] IFs = new double[len];
				
		int i,j,count = 0;
		double d;
		for(Constraint con:lstCon){
			i = con.getPos1();
			j = con.getPos2();
			if (i != j && Math.abs(i - j) >= interval && con.getIF() > 0 && count<=len-1){				
				d = helper.calEuclidianDist(str[i * 3], str[i * 3 + 1], str[i * 3 + 2], str[j * 3], str[j * 3 + 1], str[j * 3 + 2]);
				dist[count] = d;
				IFs[count] = con.getIF();
				count++;
			}
		}		
		
		return Evaluate.calSpearmanCorrelation(dist, IFs);
		//return correlationIFvsDist(calculateDistMT(str), lstCon, interval);
	}
	public static double correlationWishDistvsDist(double[] str, List<Constraint> lstCon, int interval) throws Exception{		
		
		int n = str.length / 3;// number of points
		int len = n * (n - 1) / 2;
		double[] dist = new double[len];
		double[] wishDist = new double[len];
		int i,j,count = 0;
		double d;
		for(Constraint con:lstCon){
			i = con.getPos1();
			j = con.getPos2();
			if (i != j && Math.abs(i - j) >= interval && con.getIF() > 0 && count<=len-1){				
				d = helper.calEuclidianDist(str[i * 3], str[i * 3 + 1], str[i * 3 + 2], str[j * 3], str[j * 3 + 1], str[j * 3 + 2]);
				dist[count] = d;
				wishDist[count] = con.getDist();
				count++;
			}
		}		
		
		return Evaluate.calSpearmanCorrelation(dist, wishDist);
		//return correlationIFvsDist(calculateDistMT(str), lstCon, interval);
	}

	public static double PearsoncorrelationWishDistvsDist(double[] str, List<Constraint> lstCon, int interval) throws Exception{		
			
			int n = str.length / 3;// number of points
			int len = n * (n - 1) / 2;
			double[] dist = new double[len];
			double[] wishDist = new double[len];
			int i,j,count = 0;
			double d;
			for(Constraint con:lstCon){
				i = con.getPos1();
				j = con.getPos2();
				if (i != j && Math.abs(i - j) >= interval && con.getIF() > 0 && count<=len-1){				
					d = helper.calEuclidianDist(str[i * 3], str[i * 3 + 1], str[i * 3 + 2], str[j * 3], str[j * 3 + 1], str[j * 3 + 2]);
					dist[count] = d;
					wishDist[count] = con.getDist();
					count++;
				}
			}		
			
			return Evaluate.calPearsonCorrelation(dist, wishDist);
			//return correlationIFvsDist(calculateDistMT(str), lstCon, interval);
		}
	
	
	
	public static double rmse(double[] str, List<Constraint> lstCon){
		double d,sum = 0.0;
		int i,j;
		for(Constraint con:lstCon){
			i = con.getPos1();
			j = con.getPos2();
			d = helper.calEuclidianDist(str[i * 3], str[i * 3 + 1], str[i * 3 + 2], str[j * 3], str[j * 3 + 1], str[j * 3 + 2]);
			sum += (d - con.getDist()) * (d - con.getDist());
		}
		
		sum /= lstCon.size();
		
		return Math.sqrt(sum);

		//return rmsd(calculateDistMT(str), lstCon);
	}
	
	public static double rmse(double[] dist1, double[] dist2){
		double s1 = 0, s2 = 0;
		for(int i = 0; i < dist1.length; i++){
			s1 += dist1[i];
			s2 += dist2[i];
		}
		
		double ratio = s1/s2;
		double sum = 0;
		for(int i = 0; i < dist1.length; i++){
			sum += (dist1[i] - dist2[i] * ratio) * (dist1[i] - dist2[i] * ratio);
		}
		sum /= dist1.length;
		
		return Math.sqrt(sum);
		
	}
	
}