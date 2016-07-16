package NetworkGenerator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import Algorithm.CostFunctions.CostFunction;
import Network.AuxiliaryNetwork;
import Network.Link;
import Network.Network;
import Network.Request;
import Network.Server;

public class NetworkPathFinder { //gets shortest path from network to network

  /**
   * Use djikstra to get shortest paths by @costFn. New Link takes the minimum bandwidth in shortest path.
   */
  public AuxiliaryNetwork shortestPathsByCost(Network network, Request request, CostFunction costFn) {
    HashMap<Integer, HashMap<Integer, ArrayList<Link>>> allShortestPaths = new HashMap<>();
    final double[][] pathCosts = new double[network.size()][network.size()];
    double[][] pathDelays = new double[network.size()][network.size()];

    ArrayList<Server> auxServers = new ArrayList<Server>();
    for (Server s : network.getServers()) {
      auxServers.add(s.clone());
    }

    for (Server src : network.getServers()) { //find shortest path from s to every other server in the network
      final HashMap<Server, Double> pathCost = new HashMap<Server, Double>(); // cost of path from @src to other servers
      HashMap<Server, Server> prevNode = new HashMap<>();
      pathCost.put(src, 0.0);
      PriorityQueue<Server> queue = new PriorityQueue<>(network.getServers().size(), new Comparator<Server>() {
        @Override public int compare(Server s1, Server s2) {
          return pathCost.get(s1).compareTo(pathCost.get(s2));
        }
      }); //priority queue
      ArrayList<Server> searched = new ArrayList<>();
      queue.add(src);
      while (!queue.isEmpty()) {
        Server curr = queue.poll();
        for (Server neighbour : curr.getAllNeighbours()) {
          Link l = curr.getLink(neighbour);
          //searched nodes and links without enough bandwidth
          if (searched.contains(neighbour) || l.getResidualBandwidth() < request.bandwidth * request.SC.length) {
            continue;
          }
          Double cost = pathCost.get(neighbour);
          cost = (cost == null) ? Double.MAX_VALUE : cost;
          if (pathCost.get(curr) + costFn.getCost(l, request.bandwidth) < cost) {
            pathCost.put(neighbour, pathCost.get(curr) + costFn.getCost(l, request.bandwidth));
            prevNode.put(neighbour, curr);
          }
          //add to priority queue using insertion sort
          queue.add(neighbour);
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
        ArrayList<Link> shortestPath = new ArrayList<>();
        while (prevNode.get(curr) != null) {
          Link l = curr.getLink(prevNode.get(curr));
          shortestPath.add(0, l);
          delay += l.getDelay();
          curr = prevNode.get(curr);
        }
        //update all shortest paths map
        HashMap<Integer, ArrayList<Link>> srcMap =
            (allShortestPaths.containsKey(src.getId())) ? allShortestPaths.get(src.getId()) : new HashMap<Integer, ArrayList<Link>>();
        srcMap.put(dest.getId(), shortestPath);
        allShortestPaths.put(src.getId(), srcMap);
        pathCosts[src.getId()][dest.getId()] = pathCost.get(dest);
        pathDelays[src.getId()][dest.getId()] = delay;
      }
    }
    return new AuxiliaryNetwork(network.getServers(), network.getLinks(), pathCosts, pathDelays, allShortestPaths, request);
  }
}
