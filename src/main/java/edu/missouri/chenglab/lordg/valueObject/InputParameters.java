package edu.missouri.chenglab.lordg.valueObject;

import java.util.List;

import org.jmol.api.JmolViewer;

public class InputParameters {
	private int num=1;
	private String output_folder;
	private String input_file;
	private String filtered_input_file;
	
	private String file_prefix;
	private boolean verbose=false;
	private double learning_rate=1.0;
	private int max_iteration=2000;
	private double convert_factor = -1;	
	private int[] chr_lens;	
	private double contact_thres=0.0;
	private boolean isKeepOriginalScale = false;
	private int number_threads = 0;
	private String tmpFolder;
	private boolean isPrintOutStr = true;
	
	private String distanceFile = null;
	
	private boolean isAddInequalityConstraint = false;
	
	//initial model for optimization
	private String initial_str_file = null;
	
	//to visualize model during reconstruction
	private JmolViewer viewer = null;
	
	//to map ID to genomic ID in 3D model
	private List<Integer> lstPos = null;
	
	private boolean isStopRunning = false;
	
	private boolean isSearchingConversionFactor = false;
	
	private String chrom = "1";
	private String genomeID = "hg19";
	
	public InputParameters copy(){
		InputParameters inputParameter = new InputParameters();
		inputParameter.setNum(num);
		inputParameter.setOutput_folder(output_folder);
		inputParameter.setInput_file(input_file);
		inputParameter.setFile_prefix(file_prefix);
		inputParameter.setVerbose(verbose);
		inputParameter.setLearning_rate(learning_rate);
		inputParameter.setMax_iteration(max_iteration);
		inputParameter.setConvert_factor(convert_factor);
		inputParameter.setChr_lens(chr_lens);
		inputParameter.setContact_thres(contact_thres);
		inputParameter.setKeepOriginalScale(isKeepOriginalScale);
		inputParameter.setNumber_threads(number_threads);
		inputParameter.setTmpFolder(tmpFolder);
		inputParameter.setPrintOutStr(isPrintOutStr);
		inputParameter.setAddInequalityConstraint(isAddInequalityConstraint);
		inputParameter.setDistanceFile(distanceFile);
		inputParameter.setFiltered_input_file(filtered_input_file);
		
		inputParameter.setViewer(viewer);
		inputParameter.setLstPos(lstPos);
		
		inputParameter.setStopRunning(isStopRunning);
		
		inputParameter.setSearchingConversionFactor(isSearchingConversionFactor);
		
		inputParameter.setChrom(chrom);
		inputParameter.setGenomeID(genomeID);
		
		return inputParameter;
	}
	
	
	public String getChrom() {
		return chrom;
	}


	public void setChrom(String chrom) {
		this.chrom = chrom;
	}


	public String getGenomeID() {
		return genomeID;
	}


	public void setGenomeID(String genomeId) {
		this.genomeID = genomeId;
	}


	public boolean isSearchingConversionFactor() {
		return isSearchingConversionFactor;
	}

	public void setSearchingConversionFactor(boolean isSearchingConversionFactor) {
		this.isSearchingConversionFactor = isSearchingConversionFactor;
	}

	public boolean isStopRunning() {
		return isStopRunning;
	}

	public void setStopRunning(boolean isStopRunning) {
		this.isStopRunning = isStopRunning;
	}



	public List<Integer> getLstPos() {
		return lstPos;
	}

	public void setLstPos(List<Integer> lstPos) {
		this.lstPos = lstPos;
	}



	public JmolViewer getViewer() {
		return viewer;
	}

	public void setViewer(JmolViewer viewer) {
		this.viewer = viewer;
	}

	public String getFiltered_input_file() {
		return filtered_input_file;
	}


	public void setFiltered_input_file(String filtered_input_file) {
		this.filtered_input_file = filtered_input_file;
	}


	public String getDistanceFile() {
		return distanceFile;
	}

	public void setDistanceFile(String distanceFile) {
		this.distanceFile = distanceFile;
	}



	public boolean isAddInequalityConstraint() {
		return isAddInequalityConstraint;
	}

	public void setAddInequalityConstraint(boolean isAddInequalityConstraint) {
		this.isAddInequalityConstraint = isAddInequalityConstraint;
	}

	public boolean isPrintOutStr() {
		return isPrintOutStr;
	}

	public void setPrintOutStr(boolean isPrintOutStr) {
		this.isPrintOutStr = isPrintOutStr;
	}

	public String getTmpFolder() {
		return tmpFolder;
	}
	public void setTmpFolder(String tmpFolder) {
		this.tmpFolder = tmpFolder;
	}
			
	public int getNumber_threads() {
		return number_threads;
	}
	public void setNumber_threads(int number_threads) {
		this.number_threads = number_threads;
	}
	public boolean isKeepOriginalScale() {
		return isKeepOriginalScale;
	}
	public void setKeepOriginalScale(boolean isKeepOriginalScale) {
		this.isKeepOriginalScale = isKeepOriginalScale;
	}
	public String getInitial_str_file() {
		return initial_str_file;
	}
	public void setInitial_str_file(String initial_str_file) {
		this.initial_str_file = initial_str_file;
	}
	public double getContact_thres() {
		return contact_thres;
	}
	public void setContact_thres(double contact_thres) {
		this.contact_thres = contact_thres;
	}
	public int[] getChr_lens() {
		return chr_lens;
	}
	public void setChr_lens(int[] chr_lens) {
		this.chr_lens = chr_lens;
	}
	public String getFile_prefix() {
		return file_prefix;
	}
	public void setFile_prefix(String file_prefix) {
		this.file_prefix = file_prefix;
	}
	public double getConvert_factor() {
		return convert_factor;
	}
	public void setConvert_factor(double convert_factor) {
		this.convert_factor = convert_factor;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public String getOutput_folder() {
		return output_folder;
	}
	public void setOutput_folder(String output_folder) {
		this.output_folder = output_folder;
	}
	public String getInput_file() {
		return input_file;
	}
	public void setInput_file(String input_file) {
		this.input_file = input_file;
	}
	public boolean isVerbose() {
		return verbose;
	}
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	public double getLearning_rate() {
		return learning_rate;
	}
	public void setLearning_rate(double learning_rate) {
		this.learning_rate = learning_rate;
	}
	public int getMax_iteration() {
		return max_iteration;
	}
	public void setMax_iteration(int max_iteration) {
		this.max_iteration = max_iteration;
	}
	
	
	
}
