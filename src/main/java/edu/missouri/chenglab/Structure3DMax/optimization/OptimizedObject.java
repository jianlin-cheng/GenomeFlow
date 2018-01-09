package edu.missouri.chenglab.Structure3DMax.optimization;

/**
 * 
 * 
 *
 *Every class that wants to use gradient ascent should implement this interface
 *so that GradientAscent can callback the function to calculate derivative and objective
 *
 */
public interface OptimizedObject {
	/**
	 * 
	 * @param x : array of variables, each 3 consecutive elements represent a point in 3D 
	 * @param der: array of derivative of corresponding variables
	 * @param obj: current objective function
	 * @return objective function corresponding to the new variables
	 */
	public double calGradientAndObjective(double[] x, double[] der) throws InterruptedException;
	
	
	/**
	 * 
	 * @param x : array of variables
	 * @param obj: objective function
	 * @return objective function corresponding to the new variables
	 */
	public double calObjective(double[] x) throws InterruptedException;


}
