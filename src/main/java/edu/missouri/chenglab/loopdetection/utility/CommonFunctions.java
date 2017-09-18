package edu.missouri.chenglab.loopdetection.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jmol.modelset.Atom;
import org.jmol.modelset.ModelSet;

public class CommonFunctions {

	public static int countChain(ModelSet modelSet){
		if (modelSet == null) return 0;
		Atom[] atoms = modelSet.atoms;
		Set<Character> set = new HashSet<Character>();
		for(int i = 0; i < atoms.length; i++){
			set.add(atoms[i].getChainID());
		}
		return set.size();
	}
	
	public static boolean isFile(String fileName){
		File f = new File(fileName);
		return f.exists() && f.isFile();
	}
	public static boolean isFolder(String folderName){
		File f = new File(folderName);
		return f.exists() && f.isDirectory();
	}
	public static void make_folder(String folder) throws Exception{
		File file = new File(folder);
		file.mkdirs();
	}
	
	public static boolean isExist(String fileName){
		File f = new File(fileName);
		return f.exists();
	}
	
	public static void delete_file(String file_name) throws Exception{
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
	 * user_set and test_set are sorted
	 * @param user_set
	 * @param test_set
	 * @return
	 */
	public static int count_overlap_regions(ArrayList<RegionVO> user_set, ArrayList<RegionVO> test_set){
		int i = 0, j = 0, count = 0;
		
		while(i < user_set.size() && j < test_set.size()){
			
			if (user_set.get(i).getChr_id() == test_set.get(j).getChr_id() 
					&& Math.max(user_set.get(i).getStart(), test_set.get(j).getStart()) 
						<= Math.min(user_set.get(i).getEnd(), test_set.get(j).getEnd())){
				
				int overlap = Math.min(user_set.get(i).getEnd(), test_set.get(j).getEnd()) - Math.max(user_set.get(i).getStart(), test_set.get(j).getStart());
				
				//if (overlap > (user_set.get(i).end - user_set.get(i).start) / 2) {
				if (overlap > 0) {						
					count++;
				}
				
				i++;
				j++;
			}else if (user_set.get(i).compareTo(test_set.get(j)) > 0) j++;
			else{ 
				i++;
				System.out.println("Length: " + (user_set.get(i).getEnd() - user_set.get(i).getStart())/10000);
				System.out.printf("chrom: %s, start: %d, end: %d\n", user_set.get(i).getChrom(), user_set.get(i).getStart(), user_set.get(i).getEnd());
			}
			
		}
		
		return count;
	}

	/**
	 * count region in user_set but not in test_set
	 * user_set and test_set are sorted
	 * @param user_set
	 * @param test_set
	 * @return
	 */
	public static int count_exclusive_regions(ArrayList<RegionVO> user_set, ArrayList<RegionVO> test_set){
		int i = 0, j = 0, count = 0;
		
		while(i < user_set.size() && j < test_set.size()){
			
			if (user_set.get(i).compareTo(test_set.get(j)) < 0){
				i++;
				count++;
			}else if (user_set.get(i).getChr_id() == test_set.get(j).getChr_id() 
					&& Math.max(user_set.get(i).getStart(), test_set.get(j).getStart()) 
						<= Math.min(user_set.get(i).getEnd(), test_set.get(j).getEnd())){
				
				
				i++;				
				
			}else if (user_set.get(i).compareTo(test_set.get(j)) > 0) j++;
		}
		
//		while(i < user_set.size()){
//			count++;
//			i++;
//		}
		
		return count;
	}
	
	public static ArrayList<RegionVO> get_region(String bedfile) throws Exception{
		
		ArrayList<RegionVO> lst = new ArrayList<RegionVO>();
		BufferedReader br = new BufferedReader(new FileReader(new File(bedfile)));
		String ln;
		String[] st;
		RegionVO r;
		while((ln = br.readLine()) != null){
			if (!ln.startsWith("chr")) continue;
			
			st = ln.split("\\s+");
			if (!st[0].contains("chr")) st[0] = "chr" + st[0];
			
			if (st[0].contains("Y") || st[0].contains("M")) continue;
			
			r = new RegionVO(st[0], Integer.parseInt(st[1]), Integer.parseInt(st[2]));
			lst.add(r);
			
			if (st.length > 3) r.setName(st[3]); 
		}
		br.close();
		
		Collections.sort(lst);
		
		return lst;
	}
}
