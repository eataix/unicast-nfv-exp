package Algorithm;

import java.util.ArrayList;
import Network.Server;

public class Result {
	ArrayList<Server> path;
	public double pathCost;
	public boolean admit; 
	
	public Result(ArrayList<Server> p, double pc, boolean a){
		path = p;
		pathCost = pc;
		admit = a;
	}
	
	public Result(ArrayList<Server> p, double pc){
		path = p;
		pathCost = pc;
	}
	
	public Result(){
		admit = false;
		pathCost = Double.MAX_VALUE;
	}
	
	public ArrayList<Server> getPath(){
		return path;
	}
}
