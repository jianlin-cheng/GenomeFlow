package edu.missouri.chenglab.gmol.valueobjects;

public class ComparisonObject {
	private String msg;
	private double correlationScore;
	private double rmse;	
	private String model;
	private String colorCommand;
	
	public ComparisonObject(String msg, double correlationScore, double rmse, String model) {
		super();
		this.msg = msg;
		this.correlationScore = correlationScore;
		this.rmse = rmse;
		this.model = model;
	}
	
	
	
	public String getColorCommand() {
		return colorCommand;
	}



	public void setColorCommand(String colorCommand) {
		this.colorCommand = colorCommand;
	}



	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public double getCorrelationScore() {
		return correlationScore;
	}
	public void setCorrelationScore(double correlationScore) {
		this.correlationScore = correlationScore;
	}
	public double getRmse() {
		return rmse;
	}
	public void setRmse(double rmse) {
		this.rmse = rmse;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	
	
	
	
	
	
}
