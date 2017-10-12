package edu.missouri.chenglab.lordg.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import edu.missouri.chenglab.lordg.valueObject.Constraint;
import edu.missouri.chenglab.lordg.valueObject.InputParameters;

public class Helper {	
	private DecimalFormat df2 = new DecimalFormat("0.00");
	
	private static Helper helper = new Helper();
	
	private Helper(){		
	}
	
	public static Helper getHelperInstance(){
		if (helper == null){
			synchronized(Helper.class){
				if (helper == null){
					helper = new Helper();
				}
			}
		}
		
		return helper;
	}
	


	/**
	 * Read contact matrix file
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public List<Constraint> readContactMatrixAsList(String fileName) throws Exception{
		File file = new File(fileName);
		FileReader fr = null;
		BufferedReader br = null;
		ArrayList<Constraint> lst = new ArrayList<Constraint>();
		
		try{
			Pattern splitRegex = Pattern.compile("\\s+");
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			
			String ln;
			String[] st;			
			int count = 0;		
			double d;
			while((ln = br.readLine()) != null){
				
				st = splitRegex.split(ln.trim());
								
				for(int i = count + 1; i < st.length; i++){
					d = Double.parseDouble(st[i]);
					if (!Double.isInfinite(d) && !Double.isNaN(d) && d > 0.0){
						lst.add(new Constraint(count,i,d));
					}
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
		return lst;
	}

	/**
	 * Read contact list file, each line is a contact of the form: pos1 pos1 IF
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public List<Constraint> readContactList(InputParameters inputPara, List<Integer> lstPos,double...thres) throws Exception{
		
		String fileName = inputPara.getInput_file();
		
		File file = new File(fileName);
		
		long totalLength = file.length();
		double lengthPerPercent = 100.0/totalLength;
		long readLength = 0;
		int readPercent; 
		
		
		FileReader fr = null;
		BufferedReader br = null;
		ArrayList<Constraint> lst = new ArrayList<Constraint>();
		Set<Integer> setPos = new HashSet<Integer>();
		double thr = thres.length == 0 ? 0.0 : thres[0];
		
		System.out.println("Reading input data ......");
		
		String inEclipseStr = System.getProperty("runInEclipse");
		double random = -1;
		if ("true".equalsIgnoreCase(inEclipseStr)){
			//random = 0.85;
		}

		
		try{
			Pattern splitRegex = Pattern.compile("[:\\s]+");
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			
			String ln;
			String[] st;			

			StringBuilder sb = new StringBuilder();
			int count = 0;
			int x,y,nbr = -1; // number of elements in one line
			double f;
			int prevProgress = 0;
			while((ln = br.readLine()) != null){
				
				readLength += ln.length();
				readPercent = (int) Math.round(lengthPerPercent * readLength);
				
				if (inputPara.getViewer() != null && readPercent > prevProgress && readPercent % 5 == 0){					
					inputPara.getViewer().displayMessage(new String[]{"Reading input data ... " + readPercent + " %" });
					prevProgress = readPercent;
				}
				
				if (ln.startsWith("#") || ln.trim().length() == 0 || ln.startsWith("x")){
					continue;
				}
				if (nbr == -1){
					nbr = splitRegex.split(ln).length;
				}
				
				//read every a thoudsand lines and split at once
				sb.append(ln).append(" ");
				count++;
				
				if (count == 500000){
					
					
					
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
						
						if (x != y && !Double.isNaN(f) && f > thr 
								/*&& Math.abs(x-y) < Constants.MAX_CONTACT_LENGTH
									&& Math.abs(x-y) > Constants.MIN_CONTACT_LENGTH*/){
							
							if (Math.random() > random)
								lst.add(new Constraint(x, y, f)) ;
							setPos.add(x);
							setPos.add(y);
						}
						 
					}
					//progress++;
					if (random < 0)
						System.out.print(".");
				}		
			}
			
			//if sb is not empty
			if (sb.toString().trim().length() > 0){
				st = splitRegex.split(sb.toString());
				//sb = new StringBuilder();
				
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
					
					if (x != y && !Double.isNaN(f) && f > thr 
							/*&& Math.abs(x-y) < Constants.MAX_CONTACT_LENGTH
								&& Math.abs(x-y) > Constants.MIN_CONTACT_LENGTH*/){
						
						if (Math.random() > random)
							lst.add(new Constraint(x, y, f));
						setPos.add(x);
						setPos.add(y);
					}
				}
				
			}
			
			System.out.println("\nDone reading input data. ");
			
			lstPos.addAll(setPos);
			
				
			Collections.sort(lstPos);
			
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
		return lst;
	}

	/**
	 * Read contact matrix file
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public int determineNbrOfPoints(String fileName) throws Exception{
		File file = new File(fileName);
		Scanner scan = null;
		
		try{
			Pattern splitRegex = Pattern.compile("\\s+");
			scan = new Scanner(file);
			
			String ln;
			String[] st;			
			
			ln = scan.nextLine();			
			st = splitRegex.split(ln.trim());
			return st.length;
			
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			if (scan != null){
				scan.close();
			}
		}		
		
	}

	/**
	 * Make distance matrix from contact matrix
	 * @param contMT
	 * @return
	 */
	public double[][] makeDistanceMatrix(double[][] contMT, double factor){
		double[][] distMT = new double[contMT.length][contMT.length];
		for (int i = 0; i < distMT.length; i++){
			for (int j = 0; j < distMT.length; j++){
				if (i != j){
					distMT[i][j] = 1.0 / Math.pow(contMT[i][j],factor);
				}
				
			}
		}		
		return distMT;
	}
	

	
	/**
	 * 
	 * @param total: number of constraints
	 * @param k: number of processors
	 * @return an arraylist contains ending index for each processor
	 */
	public void divideDataSet(int total, int k, ArrayList<Integer> lstSubDataSetId){
		
		int size = total / k;		
		for(int i = 1; i < k; i++){
			lstSubDataSetId.add(i * size);
		}
		if (total % k != 0 || k == 1){
			lstSubDataSetId.add(total - 1);
		}
	}
	
	
	/**
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return square euclidean distance of (x1,y1) and (x2,y2) 
	 */
	public double calEuclidianDist(double x1, double y1, double z1, double x2, double y2, double z2){
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2);
	}
	
	/**
	 * log function to take care of the case x <= 0	
	 * @param x
	 * @return
	 */
	public double log(double x){
		if (x <= 0){
			return Integer.MIN_VALUE;
		}else{
			return Math.log(x);
		}
	}
	
	/**
	 * 
	 * @param 
	 * @return hyperbolic function tanh(x)
	 */
	public double tanh(double x){		
		
		//this is an approximation for the tanh function
		if (x > 15){
			return 1.0;
		}else if (x < -15){
			return -1;
		}
		double ex = Math.pow(Math.E,x);
		double emx = 1/ex;
		double y = (ex - emx)/(ex + emx) ;		
		
		return y;

	}
	
	
	/**
	 * Zoom the structure (str) by the factor
	 * @param str
	 * @param fator
	 */
	public double[] zoomStructure(double[] str, double factor){
		
		if (str == null){
			return null;
		}
		double[] tmp = new double[str.length];
		for(int i = 0; i < str.length; i++){
			tmp[i] = str[i] * factor;
		}
		return tmp;
	}

	/**
	 * Zoom the structure (str) by the factor
	 * @param str
	 * @param fator
	 */
	public double[][] zoomStructure(double[][] str, double factor){
		
		if (str == null){
			return null;
		}
		double[][] tmp = new double[str.length][3];
		for(int i = 0; i < str.length; i++){
			tmp[i][0] = str[i][0] * factor;
			tmp[i][1] = str[i][1] * factor;
			tmp[i][2] = str[i][2] * factor;
		}
		return tmp;
	}
	
	
	/**
	 * Read a mapping between indices and coordinates
	 * @param fileName
	 * @return map<Index, Coordinate>
	 */
	public HashMap<Integer,Integer> loadCoordinateMapping(String fileName) throws Exception{
		
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
		String ln, st[];
		int x,y;
		while((ln = br.readLine()) != null){
			st = ln.split("\\s+");
			x = Integer.parseInt(st[0]);
			y = Integer.parseInt(st[1]);
			map.put(y,x);
		}
		
		br.close();
		
		return map;
	}
	
	/**
	 * Read a PDB file and return a coordinate matrix, general one
	 * @param fileName
	 * @return
	 */
	public double[][] loadPDBStructure(String fileName) throws Exception{
		File file = new File(fileName);
		FileReader fr = null;
		BufferedReader br = null;
		double[][] coor;
		ArrayList<Double> lstX = new ArrayList<Double>();
		ArrayList<Double> lstY = new ArrayList<Double>();
		ArrayList<Double> lstZ = new ArrayList<Double>();
		try{
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			

			String ln;
			//String[] st;
			//Pattern splitRegex = Pattern.compile("[\\s]+");
			double x, y, z;
			while((ln = br.readLine()) != null){
				if (! ln.startsWith("ATOM")){
					continue;
				}
//				st = splitRegex.split(ln);				
//				if (st.length > 7){
//					lstX.add(Double.parseDouble(st[5]));
//					lstY.add(Double.parseDouble(st[6]));
//					lstZ.add(Double.parseDouble(st[7]));
//				}
				
				x = Double.parseDouble(ln.substring(30, 38));
				y = Double.parseDouble(ln.substring(38, 46));
				z = Double.parseDouble(ln.substring(46, 54));
				
				if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)){
					System.err.println("NaN Error in: " + ln);
					System.exit(1);
				}
				
				lstX.add(x);
				lstY.add(y);
				lstZ.add(z);
				
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
		
		coor = new double[lstX.size()][3];
		for(int i = 0; i < lstX.size(); i++){
			coor[i][0] = lstX.get(i);
			coor[i][1] = lstY.get(i);
			coor[i][2] = lstZ.get(i);
		}
		
		return coor;
	}

	/**
	 * Convert a coordinate matrix into a matrix ready for output to write out PDB file
	 * @param a
	 * @return
	 */
	public double[] makeMatrixForPDBOutput(double[][] a){
		double[] str = new double[a.length * 3];
		for(int i = 0; i < a.length; i++){
			str[i * 3 + 0] = a[i][0];
			str[i * 3 + 1] = a[i][1];
			str[i * 3 + 2] = a[i][2];
		}
		
		return str;
	}

	public double[][] convertArrayVariableToStr(double[] a){
		int n = a.length / 3;
		double[][] str = new double[n][3];
		for(int i = 0; i < n; i++){
			str[i][0] = a[i * 3];
			str[i][1] = a[i * 3 + 1];
			str[i][2] = a[i * 3 + 2];
		}
		
		return str;
	}
	
	/**
	 * To extract one chromosome from the whole genome structure
	 * @param a: coordinates of all points in the genome
	 * @param start: starting index
	 * @param end: ending index (inclusive)
	 * @return coordinates of points of the chromosome
	 */
	public double[][] extractChr(double[][] a, int start, int end){
		
		int n = end - start + 1;
		
		//System.out.println("Length a: " + a.length + "\t n:" + n);
				
		double[][] b = new double[n][3];
		for(int i = 0; i < n; i++){
			b[i][0] = a[i + start][0];
			b[i][1] = a[i + start][1];
			b[i][2] = a[i + start][2];
		}
		
		return b;		
	}

	/**
	 * Please refer to the pdb format file for detail of each column
	 * @param pathFilename: output file name
	 * @param structure: every 3 consecutive points is one point in 3D
	 * @param idToChr: to identify if 2 fragments belong to the same chromosome
	 * @param header for the pdb file
	 */
	public void writeStructureXYZ(String pathFilename, double[] structure, boolean... isTranslate) throws IOException{

		//number of fragments
		int n = structure.length / 3;
				
		
		if (isTranslate != null && isTranslate.length > 0 && isTranslate[0]) {
			//  make the minimum x-, y-, and z-coordinates of the structure equal to one (1)
			double translationX = Double.MAX_VALUE;
			double translationY = Double.MAX_VALUE;
			double translationZ = Double.MAX_VALUE;
			
			//  find the minimum x,y,z coordinate values
			for(int i = 0; i < n; i++){
				if(structure[i * 3] < translationX){
					translationX = structure[i];
				}
				if(structure[i * 3 + 1] < translationY){
					translationY = structure[i + 1];
				}
				if(structure[i * 3 + 2] < translationZ){
					translationZ = structure[i + 2];
				}
			}
			
			//  subtract one (1.0) to each of the translations to leave the minimum point at coordinate one (1.0) after the translation
			translationX -= 1;
			translationY -= 1;
			translationZ -= 1;
			
			//  translate all of the points in the structure
			for(int i = 0; i < n; i++){
				structure[i * 3] -= translationX;
				structure[i * 3 + 1] -= translationY;
				structure[i * 3 + 2] -= translationZ;
			}
			
		}
		

		//  write the structure to file
		try{						
			PrintWriter pw = new PrintWriter(pathFilename);
			
			
			//  write the header block
			pw.println(n);


			String line;
			for(int i = 0; i < n; i++){
				
				line = "C ";
				
				line += String.format("%10.5f",structure[i * 3]) + " ";
				line += String.format("%10.5f",structure[i * 3 + 1]) + " ";
				line += String.format("%10.5f",structure[i * 3 + 2]);
				
				pw.println(line);				
				pw.flush();				
			}
			
			pw.println("END");			
			pw.flush();					
			pw.close();
		}catch (IOException e) {
			System.err.println("Error: could not output file: " + pathFilename);
			e.printStackTrace();
			throw e;
		}
	}

	
	/**
	 * Please refer to the pdb format file for detail of each column
	 * @param pathFilename: output file name
	 * @param structure: every 3 consecutive points is one point in 3D
	 * @param idToChr: to identify if 2 fragments belong to the same chromosome
	 * @param header for the pdb file
	 */
	public void writeStructure(String pathFilename, double[] structure, HashMap<Integer,Integer> idToChr, String header, boolean... isTranslate) throws IOException{

		//number of fragments
		int n = structure.length / 3;
		
		if (n >= 10000){
			writeStructureXYZ(pathFilename.replace(".pdb", ".xyz"), structure, isTranslate);
		}
		
		if (idToChr == null){
			idToChr = new HashMap<Integer,Integer>();
			//if idToChr is null, make the whole as one chromosome
			for(int i = 0; i < n; i++){				
				idToChr.put(i, 0);				
			}
		}
		/////////
		
		if (isTranslate != null && isTranslate.length > 0 && isTranslate[0]) {
			//  make the minimum x-, y-, and z-coordinates of the structure equal to one (1)
			double translationX = Double.MAX_VALUE;
			double translationY = Double.MAX_VALUE;
			double translationZ = Double.MAX_VALUE;
			
			//  find the minimum x,y,z coordinate values
			for(int i = 0; i < n; i++){
				if(structure[i * 3] < translationX){
					translationX = structure[i];
				}
				if(structure[i * 3 + 1] < translationY){
					translationY = structure[i + 1];
				}
				if(structure[i * 3 + 2] < translationZ){
					translationZ = structure[i + 2];
				}
			}
			
			//  subtract one (1.0) to each of the translations to leave the minimum point at coordinate one (1.0) after the translation
			translationX -= 1;
			translationY -= 1;
			translationZ -= 1;
			
			//  translate all of the points in the structure
			for(int i = 0; i < n; i++){
				structure[i * 3] -= translationX;
				structure[i * 3 + 1] -= translationY;
				structure[i * 3 + 2] -= translationZ;
			}
			
		}
		

		//  write the structure to file
		try{						
			PrintWriter pw = new PrintWriter(pathFilename);
			
			
			//  write the header block
			pw.println(header.toUpperCase());

			int atomSerial = 1;
			int resName = 1;
			
			String line;
			for(int i = 0; i < n; i++){
				line = "";
				line += getAtomString("ATOM");
				line += getSerialString(atomSerial + "") + " ";//12th space
				atomSerial++; // increase atom id
				
				line += getNameString("CA");
				line += getAltLocString(" ");
				//line += getResNameString(resName + "") + " ";//21st space
				line += getResNameString("MET" + "") + " ";//21st space

				line += getChainIDString((char)(idToChr.get(i) + 'A' ) + "");
				
				line += getResSeqString(resName + "");
				//line += getResSeqString("MET" + "");
				
				//if (atomSerial % 10 == 0){ // 10 atom in the same residue name
					resName++;
				//}
				
				line += " "; //27th space (iCode)
				line += "   "; //28,29,30th spaces
				
				line += getXString(structure[i * 3]);
				line += getXString(structure[i * 3 + 1]);
				line += getXString(structure[i * 3 + 2]);
				
				line += getOccupancyString(0.2);
				line += getTempFactorString(10.0);
				
				pw.println(line);				
				pw.flush();				
			}
			
			for(int i=1; i<atomSerial-1; i++){
				if(idToChr.get(i-1) == idToChr.get(i)){
					line = "";
				}else{
					line = "#";
				}
				
				line += getConnectString("CONECT");
				line += getSerial1String(i+"");
				line += getSerial2String((i+1) + "");
				pw.println(line);
				
				pw.flush();					
			}
			
			pw.println("END");			
			pw.flush();					
			pw.close();
		}catch (IOException e) {
			System.err.println("Error: could not output file: " + pathFilename);
			e.printStackTrace();
			throw e;
		}
	}
	
	public void make_folder(String folder) throws Exception{
		File file = new File(folder);
		file.mkdirs();
	}
	
	public boolean isExist(String fileName){
		File f = new File(fileName);
		return f.exists();
	}
	
	public void delete_file(String file_name) throws Exception{
		File file = new File(file_name);
		if (file.exists() && file.isFile()) file.delete();
		else if(file.isDirectory()){
			for(File f : file.listFiles()){
				delete_file(f.getAbsolutePath());
			}
			file.delete();
		}
	}


	/**
	 * Please refer to the GSS format file for detail
	 * @param pathFilename: output file name
	 * @param structure: every 3 consecutive points is one point in 3D
	 * @param
	 */
	public void writeStructureGSS(String pathFilename, double[] structure, List<Integer> lstPos, HashMap<Integer,Integer> idToChr, String chrom, String genomeID) throws IOException{

		PrintWriter pw = new PrintWriter(pathFilename);
		pw.println(String.format("<sp>%s</sp>", genomeID));
		
		
		int id, n = 0, start=0, end=0, chrID = 1;
		double radius = 1.0;
		
		//for each chromosome
		pw.println(String.format("<ens-chr>%s</ens-chr>", chrom));
		pw.println("<lc-seq>unknown</lc-seq>");
		pw.println("<cs>" + chrom);
		
		for(int i = 3; i < structure.length; i += 3){
			id = i / 3;
			
			if (idToChr.get(id) == idToChr.get(id - 1)) continue;
			
			end = id  - 1;
			n = end - start + 1;
			pw.printf("<lt>%d</lt>\n", n);
			
			writeStructureGSS(pw, structure, lstPos, radius, start * 3, end * 3);
			
			pw.println("</cs>");
			
			start = id;
			
			if (i < structure.length){
				chrID = idToChr.get(id) + 1;
				pw.println("<ens-chr>" + chrID + "</ens-chr>");
				pw.println("<lc-seq>unknown</lc-seq>");
				pw.println("<cs>" + chrID);
			}			
		}
		
		end = structure.length / 3 - 1;
		n = end - start + 1;
		if (end > start){
			pw.printf("<lt>%d</lt>\n", n);		
			writeStructureGSS(pw, structure, lstPos, radius, start * 3, end * 3);		
			pw.println("</cs>");
		}
		
		
		pw.close();			
	}
	
	public void writeStructureGSS(PrintWriter pw, double[] a,List<Integer> lstPos, double radius, int start, int end){ // start, end are included
		int id;
		for(int i = start; i <= end; i += 3){
			id = i / 3;
			pw.printf("<un %d>%.3f %.3f %.3f %.1f</un><seq>%d %d</seq>\n", 
					id+1, a[i], a[i + 1], a[i + 2], radius, lstPos.get(id), (id + 1 < lstPos.size() ? lstPos.get(id + 1) - 1 : lstPos.get(id) + 1));
			
		}
	}
	

	
	//to format the string to follow the format of pdb file format
	private String getAtomString(String st){
		//1-6
		int length = 6;
		int currentLength = st.length();
		if (currentLength > length){
			//System.err.println("Error in atom name, length exceeds " +  length);
			return st.substring(0, length);
		}
		for(int i=0; i < length - currentLength; i++){
			st = st + " ";
		}
		
		return st;
	}
	
	//to format the string to follow the format of pdb file format
	private String getSerialString(String st){
		//7-11
		int length = 5;
		int currentLength = st.length();
		if (currentLength > length){
			//System.err.println("Error in serial, length exceeds " + length);
			return st.substring(0, length);
		}
		for(int i=0; i < length - currentLength; i++){
			st = " " + st;
		}
		
		return st;
	}
	
	//to format the string to follow the format of pdb file format
	private String getNameString(String st){
		//13-16
		int length = 4;
		int currentLength = st.length();
		if (currentLength > length){
			//System.err.println("Error in name, length exceeds " + length);
			return st.substring(0, length);
		}
		for(int i=0; i < length - currentLength; i++){
			st = " " +st;
		}
		
		return st;
	}

	//to format the string to follow the format of pdb file format
	private String getAltLocString(String st){
		//17
		int length = 1;
		int currentLength = st.length();
		if (currentLength > length){
			//System.err.println("Error in alt loc, length exceeds " + length);
			return st.substring(0, length);
		}
		for(int i=0; i < length - currentLength; i++){
			st = st + " ";
		}
		
		return st;
	}
	
	//to format the string to follow the format of pdb file format
	private String getResNameString(String st){
		//18-20
		int length = 3;
		int currentLength = st.length();
		if (currentLength > length){
			//System.err.println("Error in res name, length exceeds " + length);
			return st.substring(0, length);
		}
		for(int i=0; i < length - currentLength; i++){
			st = " " + st;
		}
		
		return st;
	}
	
	//to format the string to follow the format of pdb file format
	private String getChainIDString(String st){
		//22
		int length = 1;
		int currentLength = st.length();
		if (currentLength > length){
			//System.err.println("Error in chain id, length exceeds " + length);
			return st.substring(0, length);
		}
		for(int i=0; i < length - currentLength; i++){
			st = st + " ";
		}
		
		return st;
	}
	
	//to format the string to follow the format of pdb file format
	private String getResSeqString(String st){
		//23-26
		int length = 4;
		int currentLength = st.length();
		if (currentLength > length){
			//System.err.println("Error in res seq, length exceeds " + length);
			return st.substring(0, length);
		}
		for(int i=0; i < length - currentLength; i++){
			st = st + " ";
		}
		
		return st;
	}
	
	//to format the string to follow the format of pdb file format
	private String getXString(double x){
		//31-38
		int length = 8;
		StringBuffer sb = new StringBuffer();
		
		sb.append(String.format("%8.3f",x));

		while (sb.length() > length && sb.toString().contains(".")){
			sb.delete(sb.length() - 1, sb.length());
		}
				
		return sb.toString();
	}

//	//to format the string to follow the format of pdb file format
//	private String getYString(double x){
//		//39-46
//		int length = 8;
//		String st = "";
//		if (x > 10000 || x < -1000){
//			st = String.format("%8.2f",x);
//		}else{
//			st = String.format("%8.3f",x);
//		}
//		int currentLength = st.length();
//		if (currentLength > length){
//			System.err.println("Error in Y, length exceeds " + length);
//			return st.substring(0, length);
//		}
//		for(int i=0; i < length - currentLength; i++){
//			st = " "+st;
//		}
//		
//		return st;
//	}
//	//to format the string to follow the format of pdb file format
//	private String getZString(double x){
//		//47-54
//		int length = 8;
//		String st = "";
//		if (x > 10000 || x < -1000){
//			st = String.format("%8.2f",x);
//		}else{
//			st = String.format("%8.3f",x);
//		}
//		int currentLength = st.length();
//		if (currentLength > length){
//			System.err.println("Error in Z, length exceeds " + length);
//			return st.substring(0, length);
//		}
//		for(int i=0; i < length - currentLength; i++){
//			st = " "+st;
//		}
//		
//		return st;
//	}
	//to format the string to follow the format of pdb file format
	private String getOccupancyString(double x){
		//55-60
		int length = 6;
		String st = df2.format(x);
		int currentLength = st.length();
		if (currentLength > length){
			System.err.println("Error in occupancy, length exceeds " + length);
			return st.substring(0, length);
		}
		for(int i=0; i < length - currentLength; i++){
			st = " "+st;
		}
		
		return st;
	}
	//to format the string to follow the format of pdb file format
	private String getTempFactorString(double x){
		//61-66
		int length = 6;
		String st = df2.format(x);
		int currentLength = st.length();
		if (currentLength > length){
			//System.err.println("Error in tempFactor, length exceeds " + length);
			return st.substring(0, length);
		}
		for(int i=0; i < length - currentLength; i++){
			st = " "+st;
		}		
		return st;
	}
	//to format the string to follow the format of pdb file format
	private String getConnectString(String st){
		//1-6
		int length = 6;
		int currentLength = st.length();
		if (currentLength > length){
			//System.err.println("Error in connect, length exceeds " + length);
			return st.substring(0, length);
		}
		for(int i=0; i < length - currentLength; i++){
			st = st + " ";
		}
		
		return st;
	}
	//to format the string to follow the format of pdb file format
	private String getSerial1String(String st){
		//7-11
		int length = 5;
		int currentLength = st.length();
		if (currentLength > length){
			//System.err.println("Error in serial 1, length exceeds " + length);
			return st.substring(0, length);
		}
		for(int i=0; i < length - currentLength; i++){
			st = " " + st;
		}		
		return st;
	}
	//to format the string to follow the format of pdb file format	
	private String getSerial2String(String st){
		//12-16
		int length = 5;
		int currentLength = st.length();
		if (currentLength > length){
			//System.err.println("Error in serial 2, length exceeds " + length);
			return st.substring(0, length);
		}
		for(int i=0; i < length - currentLength; i++){
			st = " " + st;
		}		
		return st;
	}


	/**
	* Read contact matrix file
	* @param fileName
	* @return
	* @throws Exception
	*/
	public double[][] readContactMatrix(String fileName) throws Exception{
		File file = new File(fileName);
		Scanner scan = null;
		double[][] a = null;
		try{
			Pattern splitRegex = Pattern.compile("\\s+");
			scan = new Scanner(file);
			String ln = scan.nextLine();
			String[] st;
			st = splitRegex.split(ln.trim());
			int n = st.length;
			a = new double[n][n];
			for(int i = 0; i < n; i++){
				a[0][i] = Double.parseDouble(st[i]);
			}
			int count = 1;
			
			while(scan.hasNextLine()){
				ln = scan.nextLine();			
				st = splitRegex.split(ln.trim());
				
				for(int i = 0; i < n; i++){
					a[count][i] = Double.parseDouble(st[i]);
				}
				
				count++;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			if (scan != null){
				scan.close();
			}
		}
		
		return a;
	}

}



