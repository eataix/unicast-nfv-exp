package Algorithm;

import Algorithm.CostFunctions.CostFunction;
import Algorithm.CostFunctions.ExponentialCostFunction;
import Algorithm.CostFunctions.OperationalCostFunction;
import Network.AuxiliaryNetwork;
import Network.Link;
import Network.Network;
import Network.Request;
import Network.Server;
import NetworkGenerator.NetworkPathFinder;
import Simulation.Parameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
    CostFunction costFunction = new OperationalCostFunction();
    createAuxiliaryNetwork(costFunction);
    if (auxiliaryNetwork == null) { //this means that some servers cannot be reached due to insufficient bandwidth
      return new Result.Builder().build(); //this generates a no-admittance result
    }
    auxiliaryNetwork.generateOfflineNetwork();
    ArrayList<Server> path = shortestPathInAuxiliaryNetwork(auxiliaryNetwork, request, parameters, costFunction);
    double finalPathCost = auxiliaryNetwork.calculatePathCost(path, costFunction);
    return new Result.Builder().path(path)
                               .pathCost(finalPathCost)
                               .admit(true)
                               .build();
  }

  public Result minOpCostWithDelay() {
    CostFunction costFunction = new ExponentialCostFunction();
    createAuxiliaryNetwork(costFunction);
    if (auxiliaryNetwork == null) { //this means that some servers cannot be reached due to insufficient bandwidth
      return new Result.Builder().build(); //this generates a no-admittance result
    }
    auxiliaryNetwork.generateOnlineNetwork();
    ArrayList<Server> path = LARAC(auxiliaryNetwork, request, parameters);
    double finalPathCost = auxiliaryNetwork.calculatePathCost(path, costFunction);
    return new Result.Builder().path(path)
                               .pathCost(finalPathCost)
                               .build();
  }

  public Result maxThroughputWithoutDelay() { //s is source, t is sink
    createAuxiliaryNetwork(parameters.costFunc);
    if (auxiliaryNetwork == null) { //this means that some servers cannot be reached due to insufficient bandwidth
      return new Result.Builder().build(); //this generates a no-admittance result
    }
    auxiliaryNetwork.generateOnlineNetwork();
    ArrayList<Server> path = shortestPathInAuxiliaryNetwork(auxiliaryNetwork, request, parameters, parameters.costFunc);
    double finalPathCost = auxiliaryNetwork.calculatePathCost(path, parameters.costFunc);
    boolean admit = admissionControl(finalPathCost);
    if (admit) {
      auxiliaryNetwork.admitRequestAndReserveResources(path);
    }
    return new Result.Builder().path(path)
                               .pathCost(finalPathCost)
                               .admit(admit)
                               .build();
  }

  public Result maxThroughputWithDelay() { //s is source, t is sink
    createAuxiliaryNetwork(parameters.costFunc);
    if (auxiliaryNetwork == null) { //this means that some servers cannot be reached due to insufficient bandwidth
      return new Result.Builder().build(); //this generates a no-admittance result
    }
    auxiliaryNetwork.generateOnlineNetwork();
    ArrayList<Server> path = LARAC(this.auxiliaryNetwork, this.request, this.parameters);
    double finalPathCost = auxiliaryNetwork.calculatePathCost(path, parameters.costFunc);
    boolean admit = admissionControl(finalPathCost);
    if (admit) {
      auxiliaryNetwork.admitRequestAndReserveResources(path);
    }
    return new Result.Builder().build();
  }

  private void createAuxiliaryNetwork(CostFunction cf) {
    auxiliaryNetwork = NetworkPathFinder.shortestPathsByCost(originalNetwork, request, cf, this.parameters);
  }

  private static ArrayList<Server> extractPath(Request request, HashMap<Server, Server> prevNode, Server destination) {
    ArrayList<Server> path = new ArrayList<>();
    Server curr = destination;
    int i = request.getSC().length;
    while (curr != null) {
      path.add(0, curr);
      curr = (i >= 0) ? prevNode.get(curr) : null;
      i--;
    }
    return path;
  }

  /**
   * @return A shortest path in @auxiliaryNetwork for @request with respect to @edgeWeightFunction
   */
  private static ArrayList<Server> shortestPathInAuxiliaryNetwork(AuxiliaryNetwork auxiliaryNetwork, Request request, Parameters parameters,
      CostFunction edgeWeightFunction) {
    HashMap<Server, Double> pathCost = new HashMap<>();
    HashMap<Server, Server> prevNode = new HashMap<>();
    HashSet<Server> prevLayer = new HashSet<>();
    Server src = auxiliaryNetwork.getSource();
    prevLayer.add(src);
    pathCost.put(auxiliaryNetwork.getSource(), 0.0);

    int l = request.getSC().length;
    for (int i = 0; i < l; i++) {
      int nfv = request.getSC()[i];
      HashSet<Server> currLayer = auxiliaryNetwork.getServiceLayer(i);
      for (Server curr : currLayer) {
        double minCost = Double.MAX_VALUE;
        Server minPrev = null;
        for (Server prev : prevLayer) {
          Link link = (prev.getId() == curr.getId()) ? new Link(curr) : prev.getLink(curr);
          double cost = link.getPathCost() + edgeWeightFunction.getCost(curr, nfv, parameters) + pathCost.get(prev);
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

    return extractPath(request, prevNode, dst);
  }

  /**
   * @param auxiliaryNetwork
   * @param request
   * @param parameters
   * @return
   */
  private static ArrayList<Server> LARAC(AuxiliaryNetwork auxiliaryNetwork, Request request, Parameters parameters) {

    CostFunction costOnly = parameters.costFunc;
    CostFunction delayOnly = new CostFunction() {
      @Override public double getCost(Server s, int nfv, Parameters parameters) {
        return 0; // TODO
      }

      @Override public double getCost(Link l, int b, Parameters parameters) {
        return l.getDelay();
      }
    };

    // PC is the shortest path on the original cost c
    ArrayList<Server> pathC = shortestPathInAuxiliaryNetwork(auxiliaryNetwork, request, parameters, costOnly);
    if (pathC == null) {
      return null;
    }

    double pathCCost = auxiliaryNetwork.calculatePathCost(pathC, costOnly);
    double pathCDelay = auxiliaryNetwork.calculatePathCost(pathC, delayOnly);
    if (pathCDelay <= request.getDelayReq()) {
      return pathC; // clearly no solution exists
    }

    ArrayList<Server> pathD = shortestPathInAuxiliaryNetwork(auxiliaryNetwork, request, parameters, delayOnly);
    if (pathD == null) {
      return null;
    }
    double pathDCost = auxiliaryNetwork.calculatePathCost(pathD, costOnly);
    double pathDDelay = auxiliaryNetwork.calculatePathCost(pathD, delayOnly);
    if (pathDDelay > request.getDelayReq()) {
      return null;
    }

    while (true) {
      final double lambda = (pathCCost - pathDCost) / (pathDDelay - pathCDelay);
      CostFunction modifiedCostFunction = new CostFunction() {
        @Override public double getCost(Link l, int b, Parameters parameters) {
          return costOnly.getCost(l, b, parameters) + lambda * delayOnly.getCost(l, b, parameters);
        }

        @Override public double getCost(Server s, int nfv, Parameters parameters) {
          return costOnly.getCost(s, nfv, parameters) + lambda * delayOnly.getCost(s, nfv, parameters);
        }
      };

      ArrayList<Server> pathR = shortestPathInAuxiliaryNetwork(auxiliaryNetwork, request, parameters, modifiedCostFunction);
      if (pathR == null) {
        return null;
      }

      double pathRCost = auxiliaryNetwork.calculatePathCost(pathR, costOnly);
      double pathRDelay = auxiliaryNetwork.calculatePathCost(pathR, delayOnly);

      if (pathRCost == pathCCost) {
        return pathD;
      }

      if (auxiliaryNetwork.calculatePathCost(pathR, modifiedCostFunction) == auxiliaryNetwork.calculatePathCost(pathC, modifiedCostFunction)) {
        pathD = pathR;
        pathDCost = pathRCost;
        pathDDelay = pathRDelay;
      } else {
        pathC = pathR;
        pathCCost = pathRCost;
        pathCDelay = pathRDelay;
      }
    }
  }

  private boolean admissionControl(double pathCost) {
    return pathCost < auxiliaryNetwork.size() * parameters.threshold - 1;
  }
}
