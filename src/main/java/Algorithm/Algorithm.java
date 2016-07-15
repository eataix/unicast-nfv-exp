package Algorithm;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import Simulation.Parameters;
import Algorithm.CostFunctions.CostFunction;
import Algorithm.CostFunctions.ExpCostFunction;
import Network.*;
import NetworkGenerator.NetworkPathFinder;

public class Algorithm {
	Network originalNetwork;
	AuxiliaryNetwork network;
	Request request;
	
	public Algorithm(Network n, Request r){
		originalNetwork = n;
		request = r;
	}
	
	public Result minOpCostWithoutDelay(){
		CostFunction cf = new ExpCostFunction();
		createAuxiliaryNetwork(cf);
		if(network == null){ //this means that some servers cannot be reached due to insufficient bandwidth
			return new Result(); //this generates a no-admittance result
		}
		network.generateNetwork(true);
		ArrayList<Server> path = shortestPathInAuxiliaryNetwork(cf);
		double finalPathCost = network.calculatePathCost(path, cf);
		return new Result(path, finalPathCost);
	}
	
	public Result maxThroughputWithoutDelay(CostFunction cf){ //s is source, t is sink
		createAuxiliaryNetwork(cf);
		if(network == null){ //this means that some servers cannot be reached due to insufficient bandwidth
			return new Result(); //this generates a no-admittance result
		}
		network.generateNetwork(false);
		ArrayList<Server> path = shortestPathInAuxiliaryNetwork(cf);
		double finalPathCost = network.calculatePathCost(path, cf);
		return new Result(path, finalPathCost, admitRequest(finalPathCost));
	}
	
	private void createAuxiliaryNetwork(CostFunction cf){
		network = new NetworkPathFinder().shortestPathsByCost(originalNetwork, request, cf);
	}
	
	private ArrayList<Server> extractPath(HashMap<Server, Server> prevNode, Server dst){
		ArrayList<Server> path = new ArrayList<Server>();
		Server curr = dst;
		int i = request.SC.length;
		while(curr != null){
			path.add(0, curr);
			curr = (i >= 0) ? prevNode.get(curr) : null;
			i--;
		}
		return path;
	}
	
	private ArrayList<Server> shortestPathInAuxiliaryNetwork(CostFunction cf){
		int l = request.SC.length;
		HashMap<Server, Double> pathCost = new HashMap<Server, Double>();
		HashMap<Server, Server> prevNode = new HashMap<Server, Server>();
		ArrayList<Server> queue = new ArrayList<Server>();
		HashSet<Server> prevLayer = new HashSet<Server>(); 
		Server src = network.getSource();
		prevLayer.add(src);
		queue.add(network.getSource());
		pathCost.put(network.getSource(), 0.0);
		for(int i = 0; i < l; i++){ 
			int nfv = request.SC[i];
			HashSet<Server> currLayer = network.getServiceLayer(i);
			for(Server curr : currLayer){
//				System.out.println("layer "+i+" : Server id:"+curr.getId());
				double minCost = Double.MAX_VALUE;
				Server minPrev = null;
				for(Server prev : prevLayer){
					Link link = (prev.getId() == curr.getId()) ? new Link(curr) : prev.getLink(curr);
					double cost = link.getPathCost() + cf.getCost(curr, nfv) + pathCost.get(prev);
					if(cost < minCost){
						minCost = cost;
						minPrev = prev;
					}
				}
				pathCost.put(curr, minCost);
				prevNode.put(curr, minPrev);
			}
			prevLayer = currLayer;
		}
		//final layer to sink 
		double minCost = Double.MAX_VALUE;
		Server minPrev = null;
		Server dst = network.getDestination();
		for(Server prev : prevLayer){
			Link link = (prev.getId() == dst.getId()) ? new Link(dst) : prev.getLink(dst);
			double tempCost = link.getPathCost() + pathCost.get(prev);
			if(tempCost < minCost){
				minCost = tempCost;
				minPrev = prev;
			}
		}
		prevNode.put(dst, minPrev);
		
		return extractPath(prevNode, dst);
	}
	
	private boolean admitRequest(double pathCost){
		return pathCost<network.size()*Parameters.threshold+1;
	}
}
