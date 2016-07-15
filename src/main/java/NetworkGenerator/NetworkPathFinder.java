package NetworkGenerator;
import java.util.ArrayList;
import java.util.HashMap;

import Algorithm.CostFunctions.CostFunction;
import Network.*;

public class NetworkPathFinder { //gets shortest path from network to network
	HashMap <Server, Double> pathCost;
	HashMap <Server, Server> prevNode;
	
	public AuxiliaryNetwork shortestPathsByCost(Network n, Request r, CostFunction fun){//use djikstra to get shortest paths by utilization cost. New Link takes the minimum bandwidth in shortest path.
		HashMap<Integer, HashMap<Integer, ArrayList<Link>>> allShortestPaths = new HashMap<Integer, HashMap<Integer, ArrayList<Link>>>();
		double[][]pathCosts = new double[n.size()][n.size()];
		double[][]pathDelays = new double[n.size()][n.size()];
		ArrayList<Server> auxServers = new ArrayList<Server>();
		for(Server s : n.getServers()){
			auxServers.add(s.clone());
		}
		for(Server src : n.getServers()){ //find shortest path from s to every other server in the network
			pathCost = new HashMap <Server, Double>();
			prevNode = new HashMap <Server, Server>();
			pathCost.put(src, 0.0);
			ArrayList<Server> queue = new ArrayList<Server>();
			ArrayList<Server> searched = new ArrayList<Server>();
			queue.add(src);
			while(!queue.isEmpty()){
				//priority queue
				Server curr = queue.remove(0);
				for(Server neighbour : curr.getAllNeighbours()){
					Link l = curr.getLink(neighbour);
					//searched nodes and links without enough bandwidth
					if(searched.contains(neighbour)||l.getResidualBandwidth()<r.bandwidth*r.SC.length/2){ 
						continue;
					}
					Double cost = pathCost.get(neighbour);
					cost = (cost==null) ? Double.MAX_VALUE : cost;
					if(pathCost.get(curr) + fun.getCost(l, r.bandwidth)<cost){
						pathCost.put(neighbour, pathCost.get(curr) + fun.getCost(l, r.bandwidth));
						prevNode.put(neighbour, curr);
					}
					//add to priority queue using insertion sort
					insertSort(queue, neighbour);
				}
				searched.add(curr);
			}
			
			//update shortest paths
			for(Server dest : n.getServers()){
				if(dest==src)continue;
				if(pathCost.get(dest)==null){//Auxiliary graph could not be constructed (some destinations are not reachable with current residual bandwidth)
					return null;
				}
				Server curr = dest;
				double delay = 0;
				ArrayList<Link> shortestPath = new ArrayList<Link>();
				while(prevNode.get(curr) != null){
					Link l = curr.getLink(prevNode.get(curr)); 
					shortestPath.add(0,l);
					delay += l.getDelay();
					curr = prevNode.get(curr);
				}
				//update all shortest paths map
				HashMap<Integer, ArrayList<Link>> srcMap = (allShortestPaths.containsKey(src.getId())) ? 
						allShortestPaths.get(src.getId()) : new HashMap<Integer, ArrayList<Link>>();
				srcMap.put(dest.getId(), shortestPath);		
				allShortestPaths.put(src.getId(), srcMap);
				pathCosts[src.getId()][dest.getId()] = pathCost.get(dest);	
				pathDelays[src.getId()][dest.getId()] = delay;
				
/*				HashMap<Integer, ArrayList<Link>> destMap = (allShortestPaths.containsKey(dest.getId())) ? 
						allShortestPaths.get(dest.getId()) : new HashMap<Integer, ArrayList<Link>>();
				destMap.put(src.getId(), shortestPath);
				allShortestPaths.put(dest.getId(), destMap);
				pathCosts[dest.getId()][src.getId()] = pathCost.get(dest);*/
			}
		}
		AuxiliaryNetwork auxnet = new AuxiliaryNetwork(n.getServers(), n.getLinks(), pathCosts, pathDelays, allShortestPaths, r);
		return auxnet;
	}
	
	public void insertSort(ArrayList<Server> queue, Server s){
		if(queue.isEmpty()){
			queue.add(s);
		}
		for(int i = 0; i < queue.size(); i++){
			Server q = queue.get(i);
			if(pathCost.get(s)<pathCost.get(q)){
				queue.add(i, s);
				break;
			}
			else if(i==queue.size()-1){
				queue.add(s);
				break;
			}		
		}
	}
}
