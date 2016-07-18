package NetworkGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import Algorithm.CostFunctions.CostFunction;
import Network.AuxiliaryNetwork;
import Network.Link;
import Network.Network;
import Network.Request;
import Network.Server;
import Simulation.Parameters;

public class NetworkPathFinder { //gets shortest path from network to network

  /**
   * Use djikstra to get shortest paths by @costFn. New Link takes the minimum bandwidth in shortest path.
   */
  public static AuxiliaryNetwork shortestPathsByCost(Network network, Request request, CostFunction costFn, Parameters parameters) {
    HashMap<Integer, HashMap<Integer, ArrayList<Link>>> allShortestPaths = new HashMap<>();
    final double[][] pathCosts = new double[network.size()][network.size()];
    double[][] pathDelays = new double[network.size()][network.size()];

    ArrayList<Server> auxServers = network.getServers().stream().map(s -> new Server(s)).collect(Collectors.toCollection(ArrayList::new));

    for (Server server : network.getServers()) { //find shortest path from server to every other server in the network
      final HashMap<Server, Double> pathCost = new HashMap<>(); // cost of path from @server to other servers
      HashMap<Server, Server> prevNode = new HashMap<>();
      pathCost.put(server, 0d);
      PriorityQueue<Server> queue = new PriorityQueue<>(network.getServers().size(),
          (s1, s2) -> pathCost.getOrDefault(s1, Double.POSITIVE_INFINITY).compareTo(pathCost.getOrDefault(s2, Double.POSITIVE_INFINITY))); //priority queue

      ArrayList<Server> searched = new ArrayList<>();
      queue.add(server);
      while (!queue.isEmpty()) {
        Server curr = queue.poll();
        for (Server neighbour : curr.getAllNeighbours()) {
          Link l = curr.getLink(neighbour);
          //searched nodes and links without enough bandwidth
          if (searched.contains(neighbour) || l.getResidualBandwidth() < request.getBandwidth() * request.getSC().length) {
            continue;
          }
          Double cost = pathCost.getOrDefault(neighbour, Double.POSITIVE_INFINITY);
          if (pathCost.get(curr) + costFn.getCost(l, request.getBandwidth(), parameters) < cost) {
            pathCost.put(neighbour, pathCost.get(curr) + costFn.getCost(l, request.getBandwidth(), parameters));
            prevNode.put(neighbour, curr);
          }
          //add to priority queue using insertion sort
          queue.add(neighbour);
        }
        searched.add(curr);
      }

      //update shortest paths
      for (Server dest : network.getServers()) {
        if (dest == server) {
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
            (allShortestPaths.containsKey(server.getId())) ? allShortestPaths.get(server.getId()) : new HashMap<>();
        srcMap.put(dest.getId(), shortestPath);
        allShortestPaths.put(server.getId(), srcMap);
        pathCosts[server.getId()][dest.getId()] = pathCost.get(dest);
        pathDelays[server.getId()][dest.getId()] = delay;
      }
    }
    return new AuxiliaryNetwork(network.getServers(), network.getLinks(), pathCosts, pathDelays, allShortestPaths, request, parameters);
  }
}
