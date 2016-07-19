package NetworkGenerator;

import java.util.ArrayList;
import java.util.HashMap;

import Algorithm.CostFunctions.CostFunction;
import Network.AuxiliaryNetwork;
import Network.Link;
import Network.Network;
import Network.Request;
import Network.Server;
import Simulation.Parameters;

/**
 * Get shortest path from network to network
 */
public class NetworkPathFinder {

  /**
   * Use Dijkstra to get all-pair shortest paths with respect to cost function @costFn. New Link takes the minimum bandwidth in shortest path.
   */
  public static AuxiliaryNetwork shortestPathsByCost(Network network, Request request, CostFunction costFn, Parameters parameters) {
    HashMap<Integer, HashMap<Integer, ArrayList<Link>>> allPairShortestPaths = new HashMap<Integer, HashMap<Integer, ArrayList<Link>>>();
    double[][] pathCosts = new double[network.size()][network.size()];
    double[][] pathDelays = new double[network.size()][network.size()];
    ArrayList<Server> auxServers = new ArrayList<Server>();
    for (Server s : network.getServers()) {
      auxServers.add(new Server(s));
    }

    for (Server src : network.getServers()) { //find shortest path from @src to every other server in the network
      HashMap<Server, Double> pathCost = new HashMap<Server, Double>();
      HashMap<Server, Server> prevNode = new HashMap<Server, Server>();
      pathCost.put(src, 0d);
      ArrayList<Server> queue = new ArrayList<Server>();
      ArrayList<Server> searched = new ArrayList<Server>();
      queue.add(src);
      while (!queue.isEmpty()) {
        //priority queue
        Server curr = queue.remove(0);
        for (Server neighbour : curr.getAllNeighbours()) {
          Link l = curr.getLink(neighbour);
          //searched nodes and links without enough bandwidth
          if (searched.contains(neighbour) || l.getResidualBandwidth() < request.getBandwidth() * request.getSC().length) {
            continue;
          }
          Double cost = pathCost.getOrDefault(neighbour, Double.MAX_VALUE);
          double altCost = pathCost.get(curr) + costFn.getCost(l, request.getBandwidth(), parameters);
          if (altCost < cost) {
            pathCost.put(neighbour, altCost);
            prevNode.put(neighbour, curr);
          }
          //add to priority queue using insertion sort
          insertSort(queue, neighbour, pathCost);
        }
        searched.add(curr);
      }

      //update shortest paths
      for (Server dest : network.getServers()) {
        if (dest == src) {
          continue;
        }
        if (pathCost.get(dest) == null) {//Auxiliary graph could not be constructed (some destinations are not reachable with current residual bandwidth)
          return null;
        }
        Server curr = dest;
        double delay = 0;
        ArrayList<Link> shortestPath = new ArrayList<Link>();
        while (prevNode.get(curr) != null) {
          Link l = curr.getLink(prevNode.get(curr));
          shortestPath.add(0, l);
          delay += l.getDelay();
          curr = prevNode.get(curr);
        }
        HashMap<Integer, ArrayList<Link>> srcMap = allPairShortestPaths.getOrDefault(src.getId(), new HashMap<>());
        srcMap.put(dest.getId(), shortestPath);
        allPairShortestPaths.put(src.getId(), srcMap);
        pathCosts[src.getId()][dest.getId()] = pathCost.get(dest);
        pathDelays[src.getId()][dest.getId()] = delay;

/*				HashMap<Integer, ArrayList<Link>> destMap = (allPairShortestPaths.containsKey(dest.getId())) ?
            allPairShortestPaths.get(dest.getId()) : new HashMap<Integer, ArrayList<Link>>();
				destMap.put(src.getId(), shortestPath);
				allPairShortestPaths.put(dest.getId(), destMap);
				pathCosts[dest.getId()][src.getId()] = pathCost.get(dest);*/
      }
    }
    AuxiliaryNetwork auxnet = new AuxiliaryNetwork(network.getServers(), network.getLinks(), pathCosts, pathDelays, allPairShortestPaths, request, parameters);
    return auxnet;
  }

  private static void insertSort(ArrayList<Server> queue, Server s, HashMap<Server, Double> pathCost) {
    if (queue.isEmpty()) {
      queue.add(s);
    }
    for (int i = 0; i < queue.size(); i++) {
      Server q = queue.get(i);
      if (pathCost.get(s) < pathCost.get(q)) {
        queue.add(i, s);
        break;
      } else if (i == queue.size() - 1) {
        queue.add(s);
        break;
      }
    }
  }
}
