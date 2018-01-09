package edu.missouri.chenglab.Structure3DMax.valueObject;

public class Constants {


	
	public static final String NUM_KEY = "NUM";
		
	public static final String OUTPUT_FOLDER_KEY = "OUTPUT_FOLDER";
	public static final String INPUT_FILE_KEY = "INPUT_FILE";
	
	public static final String CONVERT_FACTOR_KEY = "CONVERT_FACTOR";
	
	public static final String VERBOSE_KEY = "VERBOSE";
	
	public static final String CHR_UPPER_BOUND_ID_FILE_KEY = "CHR_UPPER_BOUND_ID_FILE";
	
	public static final String LEARNING_RATE_KEY = "LEARNING_RATE";
	
	public static final String MAX_ITERATION_KEY = "MAX_ITERATION";
	
	public static final String CHR_LENGTH_KEY = "CHROMOSOME_LENGTH";
	
	public static final String THRESHOLD_KEY = "CONTACT_THRESHOLD";
	
	public static final String KEEP_ORIGINAL_SCALE_KEY = "KEEP_ORIGINAL_SCALE";
	
	public static final String INITIAL_MODEL = "INITIAL_MODEL";
	
	public static final String DISTANCE_FILE = "DISTANCE_FILE";
	
	public static String HEADER_STR_FILE = "3D chromosome/genome modeling by 3DMax";
	
	//maximum number of threads should be used 
	public static final int MAX_NUM_THREAD = 120;
	
	//the starting learning rate for the line search
	public static double INITIAL_LEARNING_RATE = 0.001;		
	
	//maximum number of iterations
	public static int MAX_ITER = 3000;
	
	//this constant is used to check if the norm of the gradient is near zero
	public static final double NEAR_ZERO = 10e-6;

	//if the distance is larger than LARGE_DISTANCE_FOR_TANH, it will be scale down to this value
	public static final double SCALE_DISTANCE = 15.0;
	
	//average distance will be scaled down to this value
	public static final double AVG_DIST = 10.0;//test Sep 01, 2016
	
	public static final double WIDE_CURVE = 25;
	public static final int TEMP_NUM = 5; //number of models generated for each convert_factor when searching for convert_factor
	public static final double START_CONVERT_FACTOR = 0.1;
	public static final double END_CONVERT_FACTOR = 3.0;
	public static final double DEFAULT_CONVERT_FACTOR = 1.0; 
	
	public static final long MAX_CONTACT_LENGTH = Long.MAX_VALUE;
	public static final long MIN_CONTACT_LENGTH = 0;

}
