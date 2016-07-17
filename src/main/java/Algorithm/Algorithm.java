package Algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import Algorithm.CostFunctions.CostFunction;
import Algorithm.CostFunctions.ExpCostFunction;
import Algorithm.CostFunctions.OpCostFunction;
import Network.AuxiliaryNetwork;
import Network.Link;
import Network.Network;
import Network.Request;
import Network.Server;
import NetworkGenerator.NetworkPathFinder;
import NetworkGenerator.Utils;
import Simulation.Parameters;

public class Algorithm {
  private final Network originalNetwork;
  private final Request request;
  private final Parameters parameters;

  private AuxiliaryNetwork auxiliaryNetwork;

  public Algorithm(Network originalNetwork, Request request, Parameters parameters) {
    this.originalNetwork = originalNetwork;
    this.request = request;
    this.parameters = parameters;
  }

  public Result minOpCostWithoutDelay() {
    CostFunction cf = new OpCostFunction();
    createAuxiliaryNetwork(cf);
    if (auxiliaryNetwork == null) { //this means that some servers cannot be reached due to insufficient bandwidth
      return new Result(); //this generates a no-admittance result
    }
    auxiliaryNetwork.generateNetwork(true);
    ArrayList<Server> path = shortestPathInAuxiliaryNetwork();
    double finalPathCost = auxiliaryNetwork.calculatePathCost(path, cf);
    return new Result(path, finalPathCost);
  }

  public Result maxThroughputWithoutDelay() { //s is source, t is sink
    createAuxiliaryNetwork(parameters.costFunc);
    if (auxiliaryNetwork == null) { //this means that some servers cannot be reached due to insufficient bandwidth
      return new Result(); //this generates a no-admittance result
    }
    auxiliaryNetwork.generateNetwork(false);
    ArrayList<Server> path = shortestPathInAuxiliaryNetwork();
    double finalPathCost = auxiliaryNetwork.calculatePathCost(path, parameters.costFunc);
    return new Result(path, finalPathCost, admitRequest(finalPathCost));
  }

  public Result minOpCostWithDelay(double delay) {
    CostFunction cf = new ExpCostFunction();
    createAuxiliaryNetwork(cf);
    if (auxiliaryNetwork == null) { //this means that some servers cannot be reached due to insufficient bandwidth
      return new Result(); //this generates a no-admittance result
    }
    auxiliaryNetwork.generateNetwork(true);
    ArrayList<Link> path = Utils.LARAC(auxiliaryNetwork, auxiliaryNetwork.getSource(), auxiliaryNetwork.getDestination(), delay);
    return new Result();
  }

  public Result maxThroughputWithDelay(CostFunction cf, double delay) { //s is source, t is sink
    createAuxiliaryNetwork(cf);
    if (auxiliaryNetwork == null) { //this means that some servers cannot be reached due to insufficient bandwidth
      return new Result(); //this generates a no-admittance result
    }
    auxiliaryNetwork.generateNetwork(false);
    ArrayList<Link> path = Utils.LARAC(auxiliaryNetwork, auxiliaryNetwork.getSource(), auxiliaryNetwork.getDestination(), delay);
    return new Result();
  }

  private void createAuxiliaryNetwork(CostFunction cf) {
    auxiliaryNetwork = new NetworkPathFinder().shortestPathsByCost(originalNetwork, request, cf, this.parameters);
  }

  private ArrayList<Server> extractPath(HashMap<Server, Server> prevNode, Server dst) {
    ArrayList<Server> path = new ArrayList<>();
    Server curr = dst;
    int i = request.SC.length;
    while (curr != null) {
      path.add(0, curr);
      curr = (i >= 0) ? prevNode.get(curr) : null;
      i--;
    }
    return path;
  }

  private ArrayList<Server> shortestPathInAuxiliaryNetwork() {
    HashMap<Server, Double> pathCost = new HashMap<>();
    HashMap<Server, Server> prevNode = new HashMap<>();
    HashSet<Server> prevLayer = new HashSet<>();
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
          double cost = link.getPathCost() + parameters.costFunc.getCost(curr, nfv, parameters) + pathCost.get(prev);
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
    return pathCost < auxiliaryNetwork.size() * parameters.threshold - 1;
  }
}
