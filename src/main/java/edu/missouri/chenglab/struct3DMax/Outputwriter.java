package edu.missouri.chenglab.struct3DMax;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class Outputwriter {
	
	private DecimalFormat df2 = new DecimalFormat("0.00");
	
	/**
	 *  Create a new folder		
	 * @param folder
	 * @throws Exception
	 */
	public void make_folder(String folder) throws Exception{
		File file = new File(folder);
		file.mkdirs();
	}
	/**
	 * Check if a file exist
	 * @param fileName
	 * @return
	 */
	
	public boolean isExist(String fileName){
		File f = new File(fileName);
		return f.exists();
	}
	/**
	 * Delete a created file
	 * @param file_name
	 * @throws Exception
	 */		
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
	 * 
	 * @param pathFilename: output file name
	 * @param structure: X,Y,Z cordinates of a Pdb
	 * @param header for the pdb file
	 */
	public void writeStructurePDB(String pathFilename, double[][]XYZ, String header) throws IOException{

		//number of fragments
		int n = XYZ.length;
				

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

				line += getChainIDString((char)( 'A' ) + "");
				
				line += getResSeqString(resName + "");
				
				resName++;
				
				
				line += " "; //27th space (iCode)
				line += "   "; //28,29,30th spaces
				
				line += getXString(XYZ[i][0]);
				line += getXString(XYZ[i][1]);
				line += getXString(XYZ[i][2]);
				
				line += getOccupancyString(0.2);
				line += getTempFactorString(10.0);
				
				pw.println(line);				
				pw.flush();				
			}
			
			for(int i=1; i<atomSerial-1; i++){
				
				line = "";
				
				
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
	
	
	
	
	
	
	
	/**
	 * output .gss file
	 * @param coords: coordinates of points
	 * @param outputFile
	 */	
	public void writeStructureGSS(double[][] coords, Map<Integer, Integer> map, String outputFile, int res) throws Exception{
		PrintWriter pw = new PrintWriter(outputFile);
		pw.println("<sp>some_species</sp>");
		pw.println("<ens-chr>1</ens-chr>");
		pw.println("<lc-seq>unknown</lc-seq>");
		pw.println("<cs>1");
		
		pw.printf("<lt>%d</lt>\n", coords.length);
		double radius = 1.0;
		
		for(int i = 0; i < coords.length; i++){
			pw.printf("<un %d>%.3f %.3f %.3f %.1f</un><seq>%d %d</seq>\n", 
					i+1, coords[i][0], coords[i][1], coords[i][2], radius, i*res + 1, i*res + res);
		}
		
		pw.println("</cs>");
		pw.close();
	}


}
