package Algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import Algorithm.CostFunctions.CostFunction;
import Algorithm.CostFunctions.ExpCostFunction;
import Network.AuxiliaryNetwork;
import Network.Link;
import Network.Network;
import Network.Request;
import Network.Server;
import NetworkGenerator.NetworkPathFinder;
import Simulation.Parameters;

public class Algorithm {
  private final Network originalNetwork;
  private AuxiliaryNetwork auxiliaryNetwork;
  private final Request request;

  public Algorithm(Network n, Request r) {
    originalNetwork = n;
    request = r;
  }

  public Result minOpCostWithoutDelay() {
    CostFunction cf = new ExpCostFunction();
    createAuxiliaryNetwork(cf);
    if (auxiliaryNetwork == null) { //this means that some servers cannot be reached due to insufficient bandwidth
      return new Result(); //this generates a no-admittance result
    }
    auxiliaryNetwork.generateNetwork(true);
    ArrayList<Server> path = shortestPathInAuxiliaryNetwork(cf);
    double finalPathCost = auxiliaryNetwork.calculatePathCost(path, cf);
    return new Result(path, finalPathCost);
  }

  public Result maxThroughputWithoutDelay(CostFunction cf) { //s is source, t is sink
    createAuxiliaryNetwork(cf);
    if (auxiliaryNetwork == null) { //this means that some servers cannot be reached due to insufficient bandwidth
      return new Result(); //this generates a no-admittance result
    }
    auxiliaryNetwork.generateNetwork(false);
    ArrayList<Server> path = shortestPathInAuxiliaryNetwork(cf);
    double finalPathCost = auxiliaryNetwork.calculatePathCost(path, cf);
    return new Result(path, finalPathCost, admitRequest(finalPathCost));
  }

  private void createAuxiliaryNetwork(CostFunction cf) {
    auxiliaryNetwork = new NetworkPathFinder().shortestPathsByCost(originalNetwork, request, cf);
  }

  private ArrayList<Server> extractPath(HashMap<Server, Server> prevNode, Server dst) {
    ArrayList<Server> path = new ArrayList<Server>();
    Server curr = dst;
    int i = request.SC.length;
    while (curr != null) {
      path.add(0, curr);
      curr = (i >= 0) ? prevNode.get(curr) : null;
      i--;
    }
    return path;
  }

  private ArrayList<Server> shortestPathInAuxiliaryNetwork(CostFunction cf) {
    HashMap<Server, Double> pathCost = new HashMap<Server, Double>();
    HashMap<Server, Server> prevNode = new HashMap<Server, Server>();
    HashSet<Server> prevLayer = new HashSet<Server>();
    Server src = auxiliaryNetwork.getSource();
    prevLayer.add(src);
    pathCost.put(auxiliaryNetwork.getSource(), 0.0);

    int l = request.SC.length;
    for (int i = 0; i < l; i++) {
      int nfv = request.SC[i];
      HashSet<Server> currLayer = auxiliaryNetwork.getServiceLayer(i);
      for (Server curr : currLayer) {
        //				System.out.println("layer "+i+" : Server id:"+curr.getId());
        double minCost = Double.MAX_VALUE;
        Server minPrev = null;
        for (Server prev : prevLayer) {
          Link link = (prev.getId() == curr.getId()) ? new Link(curr) : prev.getLink(curr);
          double cost = link.getPathCost() + cf.getCost(curr, nfv) + pathCost.get(prev);
          if (cost < minCost) {
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
    Server dst = auxiliaryNetwork.getDestination();
    for (Server prev : prevLayer) {
      Link link = (prev.getId() == dst.getId()) ? new Link(dst) : prev.getLink(dst);
      double tempCost = link.getPathCost() + pathCost.get(prev);
      if (tempCost < minCost) {
        minCost = tempCost;
        minPrev = prev;
      }
    }
    prevNode.put(dst, minPrev);

    return extractPath(prevNode, dst);
  }

  private boolean admitRequest(double pathCost) {
    return pathCost < auxiliaryNetwork.size() * Parameters.threshold + 1;
  }
}
