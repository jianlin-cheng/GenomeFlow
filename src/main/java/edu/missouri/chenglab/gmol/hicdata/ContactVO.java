package edu.missouri.chenglab.gmol.hicdata;

/**
 * A contact between 2 fragments
 * @author Tuan
 *
 */
public class ContactVO implements Comparable<ContactVO> {

	
	private int pos1Start;//starting locus of fragment 1
	private int pos1End;//ending locus of fragment 2
	private int pos2Start;//starting locus of fragment 2
	private int pos2End;//ending locus of fragment 2

	private int pos1;// relative ID of the fragment1
	private int pos2;// relative ID of the fragment2 

	private double IF;//interaction frequency
	private int chr1;
	private int chr2;
		
	public ContactVO(int i, int ch1, int j, int ch2, double iF) {
		super();
		//make sure that j > i, which is upper triangle
		//(1,2) (1,3) (1,4) (1,5)....
		if (i > j){
			this.pos2 = i;
			this.chr2 = ch1;
			
			this.pos1 = j;
			this.chr1 = ch2;
			
		}else{		
			this.pos1 = i;
			this.chr1 = ch1;
			
			this.pos2 = j;
			this.chr2 = ch2;			
		}
		
		
		IF = iF;
	}
	
	public ContactVO(int i, int j, double iF) {
		super();
		//make sure that j > i, which is upper triangle
		//(1,2) (1,3) (1,4) (1,5)....
		if (i > j){
			this.pos2 = i;						
			this.pos1 = j;		
			
		}else{		
			this.pos1 = i;						
			this.pos2 = j;			
		}
				
		IF = iF;
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
	public double getIF() {
		return IF;
	}
	public void setIF(double iF) {
		IF = iF;
	}
		
	public int getChr1() {
		return this.chr1;
	}
	public void setChr1(int chr1) {
		this.chr1 = chr1;
	}
	public int getChr2() {
		return this.chr2;
	}
	public void setChr2(int chr2) {
		this.chr2 = chr2;
	}
	
	public int getPos1Start() {
		return pos1Start;
	}

	public void setPos1Start(int pos1Start) {
		this.pos1Start = pos1Start;
	}

	public int getPos1End() {
		return pos1End;
	}

	public void setPos1End(int pos1End) {
		this.pos1End = pos1End;
	}

	public int getPos2Start() {
		return pos2Start;
	}

	public void setPos2Start(int pos2Start) {
		this.pos2Start = pos2Start;
	}

	public int getPos2End() {
		return pos2End;
	}

	public void setPos2End(int pos2End) {
		this.pos2End = pos2End;
	}

	/**
	 * Compare 2 contacts by pos1, chr1 and pos2, chr2
	 */
	@Override	
	public int compareTo(ContactVO ct) {		
		if (chr1 != ct.getChr1()) return Integer.compare(chr1, ct.getChr1());
		if (chr2 != ct.getChr2()) return Integer.compare(chr2, ct.getChr2());
		
		if (pos1 != ct.getPos1()) return Integer.compare(pos1, ct.getPos1());
		if (pos2 != ct.getPos2()) return Integer.compare(pos2, ct.getPos2());
		
		return 0;			
	}
	
	@Override
	public boolean equals(Object o){
		if (! (o instanceof ContactVO)){
			return false;
		}
		
		ContactVO ct = (ContactVO) o;
		if (chr1 == ct.getChr1() && chr2 == ct.getChr2() && 
				ct.getPos1() == this.pos1 && ct.getPos2() == this.pos2){			
			return true;
		}
		
		return false;
	}
	
}
