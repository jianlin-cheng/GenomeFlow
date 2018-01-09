package edu.missouri.chenglab.Structure3DMax.valueObject;

public class Constraint implements Comparable<Constraint>{
	private int pos1;
	private int pos2;
	private double dist; //converted distance from IF
	private double strDist;//distance computed from a structure
	private double IF;
	
	public Constraint(int i, int j,double iF) {
		super();
		
		if (i > j){
			this.pos2 = i;
			this.pos1 = j;
		}else{		
			this.pos1 = i;
			this.pos2 = j;
		}
		IF = iF;
	}
	
	public Constraint(int i, int j,double iF, double dist) {
		super();
		
		if (i > j){
			this.pos2 = i;
			this.pos1 = j;
		}else{		
			this.pos1 = i;
			this.pos2 = j;
		}

		this.IF = iF;
		this.dist = dist;
	}
		
	public int getPos1() {
		return pos1;
	}



	public void setPos1(int pos1) {
		this.pos1 = pos1;
	}



	public int getPos2() {
		return pos2;
	}



	public void setPos2(int pos2) {
		this.pos2 = pos2;
	}



	public double getDist() {
		return dist;
	}
	public void setDist(double dist) {
		this.dist = dist;
	}
	public double getIF() {
		return IF;
	}
	public void setIF(double iF) {
		IF = iF;
	}

	public double getStrDist() {
		return strDist;
	}

	public void setStrDist(double strDist) {
		this.strDist = strDist;
	}

	@Override
	public int compareTo(Constraint ct) {
		if ((pos1 < ct.getPos1()) 
				|| (pos1 == ct.getPos1() && pos2 < ct.getPos2()) ){
		
			return -1;
		
		}else if (pos1 == ct.getPos1() && pos2 == ct.getPos2()){
			return 0;
		
		}else{
			return 1;
		}	

	}
	
}
