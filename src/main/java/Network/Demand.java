package Network;

public class Demand {
	public static int length; // number of nfvs
	public int rate;
	public static int delay;
	public static int [] NFVreq; //nfv vm resource requirements
	public static int [] NFVrate; //nfv vm service rate
	
	public Demand(int r, int d){
		rate = r;
		delay = d;
	}
	
	public Demand(int [] req, int[] svcr, int r, int d){
		NFVreq = req;
		NFVrate = svcr;
		rate = r;
		delay = d;
	}
	
	public static void setNumberOfNFVs(int len){
		length = len;
		NFVreq = new int [len];
		NFVrate = new int [len];
	}
	
	public static void setNFVResourceRequirements(int [] req){
		NFVreq = req;
	}
	
	public static void setNFVServiceRate(int [] rate){
		NFVrate = rate;
	}
}
