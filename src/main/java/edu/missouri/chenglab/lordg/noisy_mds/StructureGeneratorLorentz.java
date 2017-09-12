package edu.missouri.chenglab.lordg.noisy_mds;
/**
 * Dec-29-2016: search for alpha [0.7-1.5)
 * Jun-22-2017: remove adding/normalization of adjacent contacts
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import edu.missouri.chenglab.lordg.evaluation.CalRMSD;
import edu.missouri.chenglab.lordg.optimization.GradientAscent;
import edu.missouri.chenglab.lordg.optimization.OptimizedObject;
import edu.missouri.chenglab.lordg.utility.Helper;
import edu.missouri.chenglab.lordg.valueObject.Constants;
import edu.missouri.chenglab.lordg.valueObject.Constraint;
import edu.missouri.chenglab.lordg.valueObject.InputParameters;


public class StructureGeneratorLorentz implements OptimizedObject{

	private Helper helper = Helper.getHelperInstance();
	
	//number of structures will be generated
	//private int NUM;
	
	//list of constraints, each contains position i,j, IF, dist
	private List<Constraint> lstCons;
	
	//list to map 0..n to pos1...posn
	private List<Integer> lstPos;
	
	//factor to convert IF to physical distance
	//private double convertFactor = -1.0;
	
	//input file
	//private String INPUT_FILE = null;
	
	//output folder
	//private String OUTPUT_FOLDER = "";
	
	//structure
	private double[] str;
	
	//number of points
	private int n;
	
	//learning rate for optimization
	//private double LEARNING_RATE = Constants.INITIAL_LEARNING_RATE;
	
	//print intermediate result during optimization or not
	//private boolean VERBOSE = true;
	
	//maximum distance that will be scaled down to	
	//private double maxScale = Constants.SCALE_DISTANCE; 
	
	//list contains indices of sub constraints for parallel threads
	private ArrayList<Integer> lstSubDataSetId = new ArrayList<Integer>();

	//file prefix to name output file structure
	//private String FILE_PREFIX;
	
	//private int MAX_ITER = Constants.MAX_ITER;
	
	private String parameterFile;
	private InputParameters inputParameters;
	
	//private double avgIF;
	
	private double totalIF;
	
	private double maxIF;	
	
	private double AVG_DIST = Constants.AVG_DIST;
	private double WIDE_CURVE = Constants.WIDE_CURVE;

	//scale factor for the structure, it is average of direct coverted distances
	private double scale_factor = 1.0;
	
	//interval to ignore when calculating Spearman correlation, a[i,i + interval] = 0
	private int interval = 5;
	
	//private double contactThres;
	//private int[] chrLens = null;
	private HashMap<Integer,Integer> idToChr;// = new HashMap<Integer,Integer>(); //to map index to chromosome
	
	public StructureGeneratorLorentz(String parameterFile){
		this.parameterFile = parameterFile;
		this.inputParameters = new InputParameters();
	}
	
	public StructureGeneratorLorentz(InputParameters parameters){
		this.inputParameters = parameters;
	}
	
	/**
	 * read input contacts
	 * @throws Exception
	 */
	private void readInput() throws Exception{
		
		
		lstPos = new ArrayList<Integer>();
		
		lstCons = helper.readContactList(inputParameters, lstPos,inputParameters.getContact_thres());
		//lstCons = helper.readContactMatrixAsList(INPUT_FILE);
		//n = helper.determineNbrOfPoints(INPUT_FILE);
		
		n = lstPos.size();		
		System.out.println("Number of points: " + n);
		//map position to absolute id
		Map<Integer,Integer> mapPosToID = new HashMap<Integer,Integer>();
		for(int i = 0; i < lstPos.size(); i++){
			mapPosToID.put(lstPos.get(i), i);
		}
		//
		
		//output the mapping of coordinate to id in the output structure
		PrintWriter pw = new PrintWriter(inputParameters.getOutput_folder() + "/" + inputParameters.getFile_prefix() + "_coordinate_mapping.txt");
		for(int i = 0; i < lstPos.size(); i++){
			pw.println(lstPos.get(i) + "\t" + i);			
		}		
		pw.close();
		
		//calculate chromosome number for each index
		int[] chrLens = inputParameters.getChr_lens();
		
		idToChr = new HashMap<Integer,Integer>(); //to map index to chromosome
		if (chrLens != null){
			
			for(int i = 1; i <chrLens.length; i++){
				chrLens[i] = chrLens[i - 1] + chrLens[i];
			}
			for(int j = 0; j < chrLens[0]; j++){
				idToChr.put(j, 0);
			}
			for(int i = 1; i < chrLens.length; i++){
				for(int j = chrLens[i-1]; j < chrLens[i]; j++){
					idToChr.put(j, i);
				}
			}
			
		}else{
			//if idToChr is null, make the whole as one chromosome
			for(int i = 0; i < n; i++){				
				idToChr.put(i, 0);				
			}
		}				

		
		//correct lstCons to remove gap, if there is no gap, this doesn't change anything
		//avgIF = 0.0;
		for(Constraint con: lstCons){
			con.setPos1(mapPosToID.get(con.getPos1()));
			con.setPos2(mapPosToID.get(con.getPos2()));
		//	avgIF += con.getIF();
		}
		//avgIF /= lstCons.size();
		
		//avgIF = 1.0;		
		
		
		System.out.println("Done reading contact data!");
		
	}
	
	private void convertIF2Distance() throws Exception{
		
		System.out.println("Starting to convert IFs to distances");
		
		maxIF = 0.0;
		//double minIF = lstCons.get(0).getIF();
		//scale average distance to AVG_DIST
		scale_factor = 0.0;
		double avgAdjIF = 0.0;
		int avgAdjCount = 0;
		for(Constraint con : lstCons){
			if (Math.abs(con.getPos1() - con.getPos2()) == 1 && idToChr.get(con.getPos1()) == idToChr.get(con.getPos2())) {
				avgAdjCount++;
				avgAdjIF += con.getIF();
			}
		}		
		avgAdjIF /= avgAdjCount;
		
		
		System.out.println("Sorting contacts");		
		Collections.sort(lstCons);
		
		System.out.println("Adding adjacent contacts");
		

		addAdjacentContacts(avgAdjIF);		
		
		Collections.sort(lstCons);
		
		for(Constraint con : lstCons){
			//con.setIF(con.getIF()/avgIF); //normalize IF by avgIF
			scale_factor += (1.0 / Math.pow(con.getIF(),inputParameters.getConvert_factor()));
			
			totalIF += con.getIF();
			if (con.getIF() > maxIF){
				maxIF = con.getIF();
			}
		}
		scale_factor /= lstCons.size();
		
		
		//test new wide curve so that no scaling is needed
		//WIDE_CURVE = 500;
		
		System.out.println("Number of constraints: " + lstCons.size());
		double max = 0;
		//PrintWriter pw = new PrintWriter("distances.txt");
		for(Constraint con : lstCons){
			
			con.setDist(AVG_DIST / (Math.pow(con.getIF(),inputParameters.getConvert_factor()) * scale_factor ));
			
			//pw.println(AVG_DIST / (Math.pow(con.getIF(),inputParameters.getConvert_factor()) * scale_factor ));
			
			if (AVG_DIST / (Math.pow(con.getIF(),inputParameters.getConvert_factor()) * scale_factor ) > max){
				max = AVG_DIST / (Math.pow(con.getIF(),inputParameters.getConvert_factor()) * scale_factor) ;
			}
			
		}
		
		//pw.close();
		
		System.out.println("Max distance is: " + max);
		
	}
	
	//add adjacent contacts if not exist	
	private void addAdjacentContacts(double IF){
//		int id;
//		ArrayList<Constraint> ctList = new ArrayList<Constraint>();
//		Constraint ct;
//		for(int i = 0; i < n - 1; i++){
//			if (idToChr.get(i) == idToChr.get(i+ 1) ){	
//				
//				ct = new Constraint(i,i + 1,IF);				
//				id = Collections.binarySearch(lstCons, ct);
//				if (id < 0){
//					ctList.add(ct);
//				}else{					
//					if (lstCons.get(id).getIF() < IF){
//						lstCons.remove(id);
//						ctList.add(ct);
//					}
//					
//					
//				}
//			}			
//		}
//		
//		lstCons.addAll(ctList);
		
		boolean[] hasAdjacentContact = new boolean[n];// hasAdjacentContact[i] = true if contact (i, i + 1) exists
		System.out.println("Checking constraint...");
		
		for(Constraint c : lstCons){
			if (c.getPos2() - c.getPos1() == 1 && c.getIF() >= IF){
				hasAdjacentContact[c.getPos1()] = true; // this adjacent contact is good
				
			}else if (c.getPos2() - c.getPos1() == 1 && c.getIF() < IF){
				hasAdjacentContact[c.getPos1()] = true; // this adjacent contact is good
				c.setIF(IF);				
			}
		}
		
		Constraint ct;
		System.out.println("Adding adjacent constraint...");
		for(int i = 0; i < n - 1; i++){
			if (!hasAdjacentContact[i]){
				ct = new Constraint(i,i + 1,IF);
				lstCons.add(ct);
			}
		}
		
		
	}

	private void readParameters(String paraFile)throws Exception{
		File file = new File(paraFile);
		FileReader fr = null;
		BufferedReader br = null;
		String ln;
		String[] st;

		Pattern splitRegex = Pattern.compile("[=\\s#]+");
		
		try{
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			
			while((ln = br.readLine()) != null){
				if (ln.startsWith("#")){
					continue;
				}
				
				st = splitRegex.split(ln.trim());
				if (st.length == 0) continue;
				
				if (st[0].equalsIgnoreCase(Constants.NUM_KEY)){
					//NUM = Integer.parseInt(st[1]);
					inputParameters.setNum(Integer.parseInt(st[1]));
					
				}else if (st[0].equalsIgnoreCase(Constants.CONVERT_FACTOR_KEY)) {
					//convertFactor = Double.parseDouble(st[1]);
					inputParameters.setConvert_factor(Double.parseDouble(st[1]));
					
				}else if (st[0].equalsIgnoreCase(Constants.OUTPUT_FOLDER_KEY)){
					String output_folder = "";
					for (int i = 1; i < st.length; i++){
						output_folder += st[i];
					}
					
					inputParameters.setOutput_folder(output_folder);
					
				}else if (st[0].equalsIgnoreCase(Constants.INPUT_FILE_KEY)){
					String input_file = "";
					String file_prefix = "";
					for (int i = 1; i < st.length; i++){
						input_file += st[i];
					}
					String[] tmp = input_file.split("[\\/ \\. \\\\]");
					if (input_file.contains(".")){
						file_prefix = tmp[tmp.length - 2];
					}else{
						file_prefix = tmp[tmp.length - 1];
					}
				
					inputParameters.setInput_file(input_file);
					inputParameters.setFile_prefix(file_prefix);
					
				}else if (st[0].equalsIgnoreCase(Constants.VERBOSE_KEY)){
					//VERBOSE = Boolean.parseBoolean(st[1]);		
					inputParameters.setVerbose(Boolean.parseBoolean(st[1]));
				
				}else if (st[0].equalsIgnoreCase(Constants.LEARNING_RATE_KEY)){
					//LEARNING_RATE = Double.parseDouble(st[1]);
					inputParameters.setLearning_rate(Double.parseDouble(st[1]));
					
				}else if (st[0].equalsIgnoreCase(Constants.MAX_ITERATION_KEY)){
					//MAX_ITER = Integer.parseInt(st[1]);
					inputParameters.setMax_iteration(Integer.parseInt(st[1]));
				}else if (st[0].equalsIgnoreCase(Constants.CHR_LENGTH_KEY)) {
					String[] lens = st[1].trim().split(",");
					int[] chr_lens = new int[lens.length];
					for(int i = 0; i < lens.length; i++){
						chr_lens[i] = Integer.parseInt(lens[i]);
					}
					
					inputParameters.setChr_lens(chr_lens);
					
				}else if (st[0].equalsIgnoreCase(Constants.THRESHOLD_KEY)){
					//contactThres = Double.parseDouble(st[1]);
					inputParameters.setContact_thres(Double.parseDouble(st[1]));
					
				}else if (st[0].equalsIgnoreCase(Constants.KEEP_ORIGINAL_SCALE_KEY)){
					if (st[1].equalsIgnoreCase("true")){
						inputParameters.setKeepOriginalScale(true);
					}
				}
				
				
				
			}
			

		}catch(Exception e){
			
			e.printStackTrace();
			throw e;			
			
		}finally{
			if (br != null){
				br.close();
			}
			if (fr != null){
				fr.close();
			}
		}

	}
	
	/**
	 * Initialize variables before running optimization
	 */
	private void initialize(){
				
		
		
		//get the number of processor available
		int numOfcores = Runtime.getRuntime().availableProcessors();
		
		if (numOfcores == 0){
			numOfcores = 2;// default number when this parameter cannot be detected
		}
		//each core can take care of 2 threads		
		//limit number of threads to avoid excessive communication cost
		numOfcores = Math.min(numOfcores * 2 , Constants.MAX_NUM_THREAD);
		
//		String inEclipseStr = System.getProperty("runInEclipse");
//		if ("true".equalsIgnoreCase(inEclipseStr)){
//			numOfcores = 1;
//		}
		
		//August,18, test to see if adjacent distance is better
		numOfcores = 1;
		
		System.out.println("Number of processors:" + numOfcores);
		//divide the set of points into equal subsets, each will be processed by one processor (thread)

		helper.divideDataSet(lstCons.size(), numOfcores, lstSubDataSetId);
		
		
		File outputFolder = new File(inputParameters.getOutput_folder());
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}
		
//		double[][] interMap = computeInterMap(lstCons, idToChr,chrLens.length);
//		//interMap = MakeInputLabData.standardNorm(interMap);
//		try{
//			PrintWriter pw = new PrintWriter("interMap.txt");
//			for(int i = 0; i < interMap.length; i++){
//				for(int j = 0; j < interMap.length - 1; j++){
//					pw.print(interMap[i][j]+",");
//				}
//				pw.print(interMap[i][interMap.length - 1]);
//				if (i < interMap.length - 1){
//					pw.println();
//				}
//			}
//			pw.close();
//		}catch(Exception ex){
//			
//		}
	}
	
//	private double[][] computeInterMap(List<Constraint> lst, Map<Integer,Integer> map, int n){
//		
//		double[][] a = new double[n][n];
//		int totalInter = 0, countInter = 0, totalIntra = 0, countIntra = 0;
//		double thres = 1.0;
//		
//		//SummaryStatistics interStat = new SummaryStatistics();
//		//SummaryStatistics intraStat = new SummaryStatistics();
//		for(Constraint ct : lst){
//			
//			if ( map.get(ct.getPos1()) == map.get(ct.getPos2()) ){
//				//intraStat.addValue(ct.getIF());
//				
//				totalIntra++;
//				if (ct.getIF() > thres){
//					countIntra++;
//				}
//			}else{
//				//interStat.addValue(ct.getIF());
//				
//				totalInter++;
//				if (ct.getIF() < thres){
//					countInter++;
//				}
//			}
//			
//			//a[map.get(ct.getPos1())][map.get(ct.getPos2())] += ct.getIF();
//			//a[map.get(ct.getPos2())][map.get(ct.getPos1())] += ct.getIF();
//		}
//		
//		System.out.printf("Inter less than 0.65: %.3f, \n Intra more than 0.65: %.3f", countInter*100.0/totalInter, countIntra*100.0/totalIntra);
//		/*
//		System.out.printf("Inter, mean: %.2f, min: %.2f, max: %.2f, sd: %.2f", interStat.getMean(), interStat.getMin(), interStat.getMax(), interStat.getStandardDeviation());
//		System.out.println();
//		System.out.printf("Intra, mean: %.2f, min: %.2f, max: %.2f, sd: %.2f", intraStat.getMean(), intraStat.getMin(), intraStat.getMax(), intraStat.getStandardDeviation());
//		*/
//		
//		return a;
//	}
	
	
	/**
	 * Initialize the genome structure, adjacent points are initialized to be closer together than the others
	 */
	private void initializeStructure() throws Exception{
		
		str = new double[n * 3];
		
		if (inputParameters != null && inputParameters.getInitial_str_file() != null){
			double[] tmp = helper.makeMatrixForPDBOutput(helper.loadPDBStructure(inputParameters.getInitial_str_file()));
			if (tmp.length >= n * 3){
				
				for(int i = 0; i < n * 3; i++){
					str[i] = tmp[i];
				}
				
			}else{
				System.err.println("Initial structure is not in the same size");
				System.exit(1);
			}
			return;
		}
		
		double chrX=0,chrY=0,chrZ=0,size = 1.0;
		
		for(int i = 0; i < n; i++){
			
//			//reset starting point for every chromosome
//			if (i == 0 || idToChr.get(i) != idToChr.get(i - 1)){			
				chrX = Math.random() * size;
				chrY = Math.random() * size;
				chrZ = Math.random() * size;
				
//			}else {
//				//extend in X,Y,Z coordinate
//				chrX += size * (Math.random() - 0.5);
//				chrY += size * (Math.random() - 0.5);
//				chrZ += size * (Math.random() - 0.5);
//			}

			str[i * 3] = chrX;
			str[i * 3 + 1] = chrY;
			str[i * 3 + 2] = chrZ;		

		}


	}

	public double generateStructure() throws Exception {
		
		if (parameterFile != null) readParameters(parameterFile);
		
		double convertFactor = inputParameters.getConvert_factor();
		
		File outputFolder = new File(inputParameters.getOutput_folder());
		if (!outputFolder.exists()){
			outputFolder.mkdirs();
		}
		
		//read contact data
		//readInput();
		
		if (convertFactor == -1){
			convertFactor = Constants.START_CONVERT_FACTOR;
			double cor, minCor = 1.0; //correlation between IFs and distances
			double bestConvertFactor = Constants.DEFAULT_CONVERT_FACTOR;//default
			
			/*
			for(; convertFactor <= Constants.END_CONVERT_FACTOR; convertFactor += 0.1){
				try {
					inputParameters.setConvert_factor(convertFactor);
					cor = run(true); //the value is not being use, but to 
				} catch (Exception e) {					
					e.printStackTrace();
					continue;
				}
				
//				if (minCor > cor){
//					minCor = cor;
//					bestConvertFactor = convertFactor;
//				}
				
				if (minCor - cor > 0.001 || (Math.abs(cor - minCor) < 0.001 && bestConvertFactor > convertFactor) ){					
					minCor = cor;
					bestConvertFactor = convertFactor;
				}

			}*/
			
			
			//new
			int numOfcores = Runtime.getRuntime().availableProcessors();
			numOfcores = Math.min(numOfcores, Constants.MAX_NUM_THREAD);
			
			ExecutorService executor = Executors.newFixedThreadPool(Math.min((int)Constants.END_CONVERT_FACTOR * 10, numOfcores));			
			
			HashMap<Future<Double>, Double> result = new HashMap<Future<Double>, Double>();
			for(; convertFactor <= Constants.END_CONVERT_FACTOR; convertFactor += 0.1){
				inputParameters.setConvert_factor(convertFactor);
				inputParameters.setVerbose(false);
				
				InputParameters ip = inputParameters.copy();
				
				ip.setNumber_threads(1);
				ip.setNum(5);
				ip.setPrintOutStr(false);
				
				CallableWorker worker = new CallableWorker(ip);
				Future<Double> future_rs = executor.submit(worker);
				result.put(future_rs, convertFactor);			
			}
			
			
			for(Entry<Future<Double>, Double> entry : result.entrySet()){
				
				cor = entry.getKey().get();
				System.out.println("Convert factor: " + entry.getValue() + "\t" + cor);
				
				if (minCor - cor > 0.001 || (Math.abs(cor - minCor) < 0.001 && bestConvertFactor > entry.getValue()) ){
					
					minCor = cor;
					bestConvertFactor = entry.getValue();
				}
			}			
			executor.shutdown();
			/////
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
				
			PrintWriter pw = new PrintWriter(inputParameters.getOutput_folder() + "/" + "best_alpha_log.txt");
			pw.printf("\n\nBest convert factor: %.2f, with avg. correlation: %.2f, pick models generated using this convert factor as your final models \n", bestConvertFactor, minCor);
			System.out.printf("\n\nBest convert factor: %.2f, pick models generated using this convert factor as your final models \n", bestConvertFactor);
			pw.close();
			
			//generate models after searching for best factor
			convertFactor = bestConvertFactor;
			inputParameters.setConvert_factor(convertFactor);
			return run(false);
			
			
		}else{
			return run(false);
		}
	}
	
	public double run(boolean isLookupConvertFactor) throws Exception{
		String fileName;
		
		readInput();
		
		convertIF2Distance();
		
		initialize();
		
		double str_scale = scale_factor; //scale structure when writing it out, it is scaled by AVG/scale_factor when converting IF to distance
		
		if (!inputParameters.isKeepOriginalScale()) str_scale = 1;
		
		String logFileName = "";		
		PrintWriter logPW = null;
		double rmsd,cor;//,corDist;
		double avgRMSD = 0,avgCor = 0;//, avgCorDist = 0;
		boolean isOutput = false;
		
		int run_nbr = inputParameters.getNum();
		//if search for best alpha, just run 3 times for each alpha candidate
		if (isLookupConvertFactor){
			run_nbr = Constants.TEMP_NUM;
		}
		
		for(int i = 0; i < run_nbr; i++) {		
			initializeStructure();
			
			GradientAscent gradientAscent = new GradientAscent(this, str, inputParameters.isVerbose(), inputParameters.getOutput_folder() + "/tmp/", idToChr);
			if (inputParameters.getLearning_rate() != 0){
				gradientAscent.setInitialLearingRate(inputParameters.getLearning_rate());
			}
			
			gradientAscent.performGradientAscent(inputParameters);
			
			String currentTimeMillis = System.currentTimeMillis() + "";
			
			if (isLookupConvertFactor){
				fileName = inputParameters.getFile_prefix() + "_" + currentTimeMillis + "_" + inputParameters.getConvert_factor() + ".pdb" ;
				isOutput = false;
			}else{
				fileName = inputParameters.getFile_prefix() + "_" + currentTimeMillis + ".pdb" ;
				
				isOutput = true;
			}
			
			//print out log			
			if (isOutput){
				
				
				
				helper.writeStructure(inputParameters.getOutput_folder() + "/" + fileName,helper.zoomStructure(str, str_scale), idToChr, Constants.HEADER_STR_FILE);
				
				logFileName =  inputParameters.getFile_prefix() + "_log_" + currentTimeMillis + ".txt";
				logPW = new PrintWriter(inputParameters.getOutput_folder() + "/" + logFileName);
				logPW.println("Input file: " + inputParameters.getInput_file());
				logPW.println("Convert factor: " + inputParameters.getConvert_factor());
				logPW.println("Learning rate: " + inputParameters.getLearning_rate());
				if (inputParameters.getChr_lens() != null){
					logPW.print("Chromosome lengths:");
					for(int k = 0; k < inputParameters.getChr_lens().length; k++){
						logPW.print(inputParameters.getChr_lens()[k] + " ");
					}
					logPW.println();
				}
	
				logPW.flush();
			
			}
			
			rmsd = CalRMSD.rmse(str, lstCons);
			interval = 0;
			try{
				cor = CalRMSD.correlationIFvsDist(str, lstCons, interval);
			}catch(Exception ex){
				//continue;
				cor = 1.0;// convertFactor fails the reconstruction so make cor = maximum (1)
			}
			//corDist = CalRMSD.correlationWishDistvsDist(str, lstCons, interval);
			
			avgRMSD += rmsd;
			avgCor += cor;
			//avgCorDist += corDist;
			
			if (isOutput){
				logPW.println("RMSE: " + rmsd);
				logPW.println("Spearman correlation IFs vs. Reconstructed Dist: " + cor);
				//logPW.println("Spearman correlation WishDist vs. Reconstructed Dist: " + corDist);
				logPW.flush();
				logPW.close();
				
			}
			
//			System.out.println("RMSE: " + rmsd);
//			System.out.println("Spearman correlation IFs vs. Reconstructed Dist: " + cor);
//			System.out.println("Spearman correlation WishDist vs. Reconstructed Dist: " + corDist);
		}
				
		avgRMSD /= run_nbr;
		avgCor /= run_nbr;
		//avgCorDist /= run_nbr;
		
		PrintWriter pw = new PrintWriter(inputParameters.getOutput_folder() + "/" + inputParameters.getFile_prefix() + "_log.txt");
		pw.println("Input file: " + inputParameters.getInput_file());
		pw.println("Convert factor: " + inputParameters.getConvert_factor());
		pw.println("Learning rate: " + inputParameters.getLearning_rate());
		if (inputParameters.getChr_lens() != null){
			pw.print("Chromosome lengths:");
			for(int k = 0; k < inputParameters.getChr_lens().length; k++){
				pw.print(inputParameters.getChr_lens()[k] + " ");
			}
			pw.println();
		}

		pw.println("AVG RMSE: " + avgRMSD);
		pw.println("AVG Spearman correlation IFs vs. Reconstructed Dist: " + avgCor);
		//pw.println("AVG Spearman correlation WishDist vs. Reconstructed Dist: " + avgCorDist);
		pw.close();
		
		System.out.println("AVG RMSE: " + avgRMSD);
		System.out.println("AVG Spearman correlation IFs vs. Reconstructed Dist: " + avgCor);
		//System.out.println("AVG Spearman correlation WishDist vs. Reconstructed Dist: " + avgCorDist);

		return avgCor;
	}
	
	
	
	/**
	 * Calculate objective function and gradient
	 */
	@Override
	public double calGradientAndObjective(double[] x, double[] der)
			throws InterruptedException {
		
		double cost = 0.0;		
		
		GradientCaculator[] gradCalculator = new GradientCaculator[lstSubDataSetId.size()];
		
		gradCalculator[0] = new GradientCaculator(x, 0 , lstSubDataSetId.get(0), der != null);
		for(int i = 1; i < gradCalculator.length; i++){
			gradCalculator[i] = new GradientCaculator(x, lstSubDataSetId.get(i - 1) + 1, lstSubDataSetId.get(i), der != null);
		}
		//System.out.println("Starting threads ..., number of threads: " + gradCalculator.length);
		
		//if there are more than one threads
		if (gradCalculator.length > 1){
		
			//start threads
			for(int i = 0; i < gradCalculator.length; i++){
				gradCalculator[i].start();
			}
			
			//wait for all threads to finish
			try {
				for(int i=0; i< gradCalculator.length; i++){
					gradCalculator[i].join();
				}			
			} catch (InterruptedException e) {			
				e.printStackTrace();
				throw e;
			}		
		}else{//otherwise, use the current thread
			
			gradCalculator[0].run();
			
		}

		//aggregate the cost
		for(int i = 0; i < gradCalculator.length; i++){
			cost += gradCalculator[i].getCost();
		}
	
		if (der != null){
			//aggregate the gradient
			for(int i = 0; i < der.length; i++){
				der[i] = 0;
				for(int k = 0; k < gradCalculator.length; k++){
					der[i] += gradCalculator[k].getChange()[i];
				} 				
			}			
		}		
		
		return cost;
		
	}

	/**
	 * calculate objective function only
	 */
	@Override
	public double calObjective(double[] x) throws InterruptedException {
		
		return calGradientAndObjective(x,null);
	}
	
	
	/**
	 * This class is used to calculate the gradient for a subset of datapoints
	 * in a single threaded program, all data points are i = 1 .. n and j = i+1 ... n
	 * to calculate the gradient in parallel, one thread will be in charged of calculating for i = begin ... end
	 * 
	 * for any modification of the objective function, this function will need to be modified accordingly
	 * @author Tuan
	 *
	 */
	class GradientCaculator extends Thread{
		//the first index to calculate the gradient
		private int beg;
		//the last index to calculate the gradient
		private int end;
		//gradient to be returned
		double[] change;
		//objective function
		double cost=0;
		//indicate if calculation for gradient is needed
		boolean isGradientNeeded;
		
		double[] structure;
		
		GradientCaculator(double[] str, int b, int e, boolean isGradient){
			this.beg = b;
			this.end = e;		
			this.structure = str;
			this.isGradientNeeded = isGradient;
			
			if (isGradientNeeded){
				this.change = new double[n * 3];
			}
		}
		
		public void run(){
			double dist,x,ez,tmp,z,ifr,tanh;
			int i,j;
			Constraint con;
			for(int k = beg; k <= end; k ++){
				
				con = lstCons.get(k);
				
				i = con.getPos1();
				j = con.getPos2();
				dist = con.getDist();
				
				
				ifr = con.getIF();
				
				if (ifr <= 0) continue;
					
//				ifr = ifr * ifr;
				
				//if (Math.abs(lstPos.get(i) - lstPos.get(j)) == 1 && idToChr.get(i) == idToChr.get(j)){
				if (Math.abs(i - j) == 1 && idToChr.get(i) == idToChr.get(j)){
					ifr = 1.0 * maxIF;
				}				
//				else {
//					ifr = 1.0;
//				}
				
				x = Math.sqrt(helper.calEuclidianDist(structure[i * 3], structure[i * 3 + 1], 
						structure[i * 3 + 2], structure[j * 3], structure[j * 3 + 1], structure[j * 3 + 2]));
				
				//WIDE_CURVE = dist;
				int pow = 2;
				z = Math.pow(x - dist,pow);		
								
				tmp = - ifr * WIDE_CURVE * pow * Math.pow(x - dist,pow - 1) / ((z + WIDE_CURVE)*(z + WIDE_CURVE) * x); 


				cost += ifr * WIDE_CURVE / (z + WIDE_CURVE);
				
				if (Double.isNaN(tmp)){
					System.err.println("tmp become NaN");
					System.err.println("z: " + z + "\tx: " + x + "\tdist: " + dist);
					System.err.println(i + "\t" + j);
					System.err.println(structure[i * 3] + "\t" + structure[i * 3 + 1] + "\t" + structure[i * 3 + 2]);
					System.err.println(structure[j * 3] + "\t" + structure[j * 3 + 1] + "\t" + structure[j * 3 + 2]);
					System.exit(1);
				}
				
				if ( isGradientNeeded){					 
					
					change[i * 3] += tmp * (structure[i * 3] - structure[j * 3]);
					change[i * 3 + 1] += tmp * (structure[i * 3 + 1] - structure[j * 3 + 1]);
					change[i * 3 + 2] += tmp * (structure[i * 3 + 2] - structure[j * 3 + 2]);
					
					change[j * 3] += tmp * (structure[j * 3] - structure[i * 3]);
					change[j * 3 + 1] += tmp * (structure[j * 3 + 1] - structure[i * 3 + 1]);
					change[j * 3 + 2] += tmp * (structure[j * 3 + 2] - structure[i * 3 + 2]);
					
				}

				
			}			
		}

		public double[] getChange() {
			return change;
		}
		
		public double getCost() {
			return cost;
		}
		
	}

	
	public static void main(String[] args) throws Exception{
		
		//run_for_loop(args, "k562");
		
//		String[] inputs = new String[2];
//		inputs[0] = "E:/GM12878/1kb_resolution_intrachromosomal/chr1/MAPQGE30/tmp";
//		inputs[1] = "C:/Users/Tuan/workspace/GenomeMDS/output/hierarchical_modeling/chr1";
//		run_for_folder(inputs);
		
		//run_for_folder(args);
		
		//args[0] = "parameters_shao.txt";
		//String[] ar = new String[]{"parameters_shao.txt"};
		
		run_for_one_input(args);
		
		//run_for_loop(args);
		
	}
	
	private static void run_for_one_input(String[] args) throws Exception{
		String paraFile = "parameters.txt";
		if (args.length > 0) paraFile = args[0];
		
		//check if the parameter file exists in the current directory
		File file = new File(paraFile);
		if (!file.exists()){
			throw new Exception("The parameter file " + paraFile + "cannot be found in the current directory");
		}
		
		StructureGeneratorLorentz generator = new StructureGeneratorLorentz(paraFile);
		generator.generateStructure();
	}
	
	//generate models of fragments to detect loops
	private static void run_for_loop(String[] args) throws Exception{
		
		String chrom = "chr2";
		
		if (args != null && args.length >= 1) chrom = args[0];
		
		String celltype = args[1];
		
		int limit = 250;
		int start,end = 10;
		
		while (end < limit + 10){
		    start = Math.max(0, end - 12);		    
		    
		    
		    
			String paraFile = "parameters_" + celltype + "_" + chrom + "_" + start + "_10kb.txt";
			PrintWriter pw = new PrintWriter(paraFile);
			pw.println("NUM = 10");
			pw.println("OUTPUT_FOLDER = output/" + celltype + "/10kb/" + chrom + "/" + start + "_" + end);
			//String inputFile =  "E:/" + celltype + "/10kb_resolution_intrachromosomal/" + chrom + "/MAPQGE30/" + chrom + "_10kb_" + celltype + "_list_" + start + "mb_" + end + "mb.txt";
			String inputFile =  "input/" + celltype + "/10kb_resolution_intrachromosomal/" + chrom + "/MAPQGE30/" + chrom + "_10kb_" + celltype + "_list_" + start + "mb_" + end + "mb.txt";
					
			pw.println("INPUT_FILE = " + inputFile);
			pw.println("CONVERT_FACTOR = 0.8");
			pw.println("VERBOSE = true");
			pw.println("LEARNING_RATE = 1.0");
			pw.println("MAX_ITERATION = 10000");			
			pw.close();
			
//			if (args != null && args.length >= 1){
//				paraFile = args[0];
//			}
			
			String output_folder = "output/" + celltype + "/10kb/" + chrom + "/" + start + "_" + end;			
			File f = new File(output_folder);
			if (!f.exists()) f.mkdirs();
			
			//check if the parameter file exists in the current directory
			File file = new File(inputFile);
			if (!file.exists()){
				//throw new Exception("The parameter file " + paraFile + "cannot be found in the current directory");
				end = end + 10;
				continue;
			}
			
			StructureGeneratorLorentz generator = new StructureGeneratorLorentz(paraFile);
			generator.generateStructure();
			
			f = new File(paraFile);
			f.delete();
			
			end = end + 10;			
		
		}
		
	}
	//if the input is a folder of input files each containing an input contact file
	private static void run_for_folder(String[] args) throws Exception{
		
		int number_of_threads = 120;
		
		if (args.length == 0){
			args = new String[2];
			
			//args[0] = "E:/GM12878/1kb_resolution_intrachromosomal/chr1/MAPQGE30/tmp";
			//args[1] = "C:/Users/Tuan/workspace/GenomeMDS/output/hierarchical_modeling/chr1";
			
			args[0] = "C:/Users/Tuan/workspace/Hierarchical3DGenome/output/server/domains_input/";//"input/GM12878/1kb_resolution_intrachromosomal/chr1/MAPQGE30/tmp";
			args[1] = "output/chr1";
		}
		String input_folder = args[0];
		String output_folder = args[1];
		
		double convert_factor = 1.0;
		if (args.length == 3){
			convert_factor = Double.parseDouble(args[2]);
		}
		
		File input = new File(input_folder);
		InputParameters input_parameter;
		
		ArrayList<String> file_names = new ArrayList<String>();
		//int i = 0;
		for(File f : input.listFiles()){
			if (!f.isFile() || !f.getName().endsWith(".txt")) continue;
			
			//file_names.add(Integer.parseInt(f.getName().replace("region_", "").replace(".txt", "")));
			file_names.add(f.getName());
		}
		Collections.sort(file_names);
		
		ExecutorService executor = Executors.newFixedThreadPool(number_of_threads);
		
		for(String file_id : file_names){
								
			//String input_full_name = input_folder + "/region_" + file_id + ".txt";
			String input_full_name = input_folder + "/" + file_id;
			
			//check if the parameter file exists in the current directory
			File file = new File(input_full_name);
			if (!file.exists() || file.length() < 1){								
				continue;
			}
			
			
			input_parameter = new InputParameters();
			
			String input_name = file_id.replace(".txt", "");
			
			input_parameter.setNum(1);
			
			
			input_parameter.setOutput_folder(output_folder + "/" + input_name);
			
			input_parameter.setInput_file(input_full_name);
			
			input_parameter.setFile_prefix(input_name);
			
			input_parameter.setVerbose(false);
			//input_parameter.setLearning_rate(1.0);
			input_parameter.setMax_iteration(10000);
			
			input_parameter.setKeepOriginalScale(true);
			
			input_parameter.setConvert_factor(convert_factor);
			
			
			File fout = new File(input_parameter.getOutput_folder());
			if (!fout.exists()) {
				if (!fout.mkdirs()){
					System.err.println("Cannot create output folder:" + input_parameter.getOutput_folder());
					System.exit(1);
				}
			}
			
			executor.submit(new WorkerThread(input_parameter));			
		}
		
		executor.shutdown();
		
	}

	static class WorkerThread implements Runnable {
		
		InputParameters input_parameter;
		public WorkerThread(InputParameters input_parameter){
			this.input_parameter = input_parameter;
		}
		@Override
		public void run() {
			StructureGeneratorLorentz generator = new StructureGeneratorLorentz(input_parameter);
			try {
				generator.generateStructure();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
	}

	static class CallableWorker implements Callable<Double> {
		
		InputParameters input_parameter;
		public CallableWorker(InputParameters input_parameter){
			this.input_parameter = input_parameter;
		}
		@Override
		public Double call() throws Exception{
			StructureGeneratorLorentz generator = new StructureGeneratorLorentz(input_parameter);
			try {
				return generator.generateStructure();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			return 0.0;
		}
		
	}

	

}
