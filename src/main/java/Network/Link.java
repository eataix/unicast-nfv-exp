package Network;

import Simulation.Parameters;
import Simulation.Simulation;

public class Link {
	private Server s1;
	private Server s2;
	private int B; //bandwidth capacity
	private int b; //bandwidth being used
	private int opCost; //operation cost
	private double d; //delay
	private double pathUtCost; //utilization cost of a link representing a shortest path.  
	
	public Link (Server svr1, Server svr2){
		s1 = svr1;
		s2 = svr2;
		addLinkToServers();
	}
	
	private void addLinkToServers(){
		s1.addLink(this);
		s2.addLink(this);
	}
	
	private Link(Server svr1, Server svr2, int BW, int bw, double delay, int opcost){
		s1 = svr1;
		s2 = svr2;
		B = BW;
		b = bw;
		d = delay;
		opCost = opcost;
	}
	
	public Link clone(){
		return new Link(s1, s2, B, b, d, opCost);
	}
	
	public Link(Server s){ //use this constructor to create a self link
		B = 20; //default value
		s1 = s;
		s2 = s;
	}
	
	public void wipe(){
		b = 0;
	}
	
	public int getResidualBandwidth(){
		return B-b;
	}
	
	public int getBandwidth(){
		return B;
	}
	
	public int getAllocatedBandwidth(){
		return b;
	}
	
	public void setDelay(double D){
		d = D;
	}
	
	public double getDelay(){
		return d;
	}
	
	public void setBandwidth(int bw){
		B = bw;
	}
	
	public boolean canSupportBandwidth(double d){
		return b+d<B;
	}
	
	public boolean selfLink(){
		return s1==s2;
	}
	
	public void allocateBandwidth(double d){
		if(b+d < B) b += d;
	}
	
	public double getUtCost(double bandwidth){ //TODO
		if(selfLink())
			return 0;
		return Math.pow(Parameters.b*Simulation.networkSize, (double) (b+bandwidth)/B)-1;
	}
	
	public double getPathCost(){ //to be used in the algorithm section
		return pathUtCost;
	}
	
	public void setPathCost(double pathcost){
		pathUtCost = pathcost;
	}
	
	public double getOpCost(){
		if(s1 == s2)
			return 0;
		return opCost;
	}
	
	public void setOpCost(int cost){
		opCost = cost;
	}
	
	public Server getLinkedServer(Server s){
		if(s==s1)
			return s2;
		if(s==s2)
			return s1;
		else 
			return null;
	}
}
