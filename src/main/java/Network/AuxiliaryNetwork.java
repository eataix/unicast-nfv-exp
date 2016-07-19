package Network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;

import Algorithm.CostFunctions.CostFunction;
import Simulation.Parameters;

/**
 * A layered directed acyclic graph (DAG)
 * <p>
 * Each arc has a "weight" and a "delay"
 * <p>
 * This class supports many operations on an auxiliary network, including finding a (delay-aware) shortest path, etc.
 */
@SuppressWarnings("Duplicates") public class AuxiliaryNetwork extends Network {

  private final double[][] pathCosts;
  private final double[][] pathDelays;
  private final HashMap<Integer, HashMap<Integer, ArrayList<Link>>> allShortestPaths;
  private final CostFunction costFunction; // cost function for edges in the original graph

  private final Request request;
  private final Parameters parameters;

  private final Server source;
  private final Server destination;

  private final ArrayList<Server> auxServers = new ArrayList<>(); // TODO: Talk to Mike regarding what these variable does
  private final ArrayList<Link> auxLinks = new ArrayList<>();

  // The graph is organized as "layers", where Layer 0 contains source only, each of Layers 1, ..., L contains V_S, and Layer L+1 contains the destination
  public final ArrayList<HashSet<Server>> serviceLayers = new ArrayList<HashSet<Server>>();

  public AuxiliaryNetwork(ArrayList<Server> originalServers, ArrayList<Link> originalLinks, double[][] pathCosts, double[][] pathDelays,
                          HashMap<Integer, HashMap<Integer, ArrayList<Link>>> allShortestPaths, Request request, Parameters parameters, boolean offline,
                          CostFunction costFunction) {
    super(originalServers, originalLinks);
    this.pathCosts = pathCosts;
    this.pathDelays = pathDelays;
    this.allShortestPaths = allShortestPaths;
    this.request = request;
    this.parameters = parameters;
    this.source = request.getSource();
    this.destination = request.getDestination();
    this.costFunction = costFunction;

    generateNetwork(offline);
  }

  /**
   * Create network with auxServers and auxLinks
   */
  private void generateNetwork(boolean offline) {
    auxServers.add(this.source);
    auxServers.add(this.destination);

    // Layer 0, containing the source node only
    HashSet<Server> prevLayer = new HashSet<>();
    prevLayer.add(source);

    int[] SC = request.getSC();
    // Layers 1, ..., L, where each layer contains all servers that either have implemented a given NFV or can initialize a VM instance for a given NFV
    for (int nfv : SC) {
      HashSet<Server> origLayer = getReusableServers(nfv);
      if (offline) {
        origLayer.addAll(getUnusedServers(nfv));
      }
      HashSet<Server> currLayer = cloneServers(origLayer); // we do not want to make changes on the original network
      for (Server curr : currLayer) {
        for (Server prev : prevLayer) { // Connect each server in the previous layer to an server in the current layer
          Link l = new Link(prev, curr);
          l.setDelay(pathDelays[curr.getId()][prev.getId()]);
          l.setWeight(pathCosts[curr.getId()][prev.getId()]); // NOTE: We here set the weight of each edge as the cost of the path between two servers
          this.auxLinks.add(l);
        }
      }
      serviceLayers.add(currLayer);
      prevLayer = currLayer;
    }
    // Now we have added Layer 0 and Layers 1, ..., L, we now need to add the last layer containing the destination only, and link all servers in Layer L to the
    // the destination
    for (Server prev : prevLayer) { //link this up to destination
      Link l = new Link(prev, destination);
      l.setDelay(pathDelays[destination.getId()][prev.getId()]); // NOTE: We here set the weight of each edge as the cost of the path between two servers
      l.setWeight(pathCosts[destination.getId()][prev.getId()]);
      this.auxLinks.add(l);
    }
  }

  /**
   * This is what should be used, for most of the time
   * <p>
   * The set of links in this network are virtual links, each of which corresponds to a shortest path between its two endpoints. We have previously set the
   * weight of each edge to the cost of its corresponding shortest path in terms of the weighted sum of edges, which has already used the user-specified cost
   * function.
   *
   * @return a shortest path for the request, which was given to the constructor of this class.
   */
  public ArrayList<Server> findShortestPath() {
    return findShortestPath(l -> l.getWeight());
  }

  /**
   * @return A shortest path in this network with respect to @edgeWeightFunction
   *
   * Notice: The auxiliary network is a DAG
   */
  private ArrayList<Server> findShortestPath(Function<Link, Double> edgeWeightFunction) {
    HashMap<Server, Double> pathCost = new HashMap<>();
    HashMap<Server, Server> prevNode = new HashMap<>();
    HashSet<Server> prevLayer = new HashSet<>();
    Server src = this.getSource();
    prevLayer.add(src);
    pathCost.put(this.getSource(), 0.0);

    int L = request.getSC().length;
    for (int i = 0; i < L; i++) {
      HashSet<Server> currLayer = this.getServiceLayer(i);
      for (Server curr : currLayer) {
        double minCost = Double.MAX_VALUE;
        Server minPrev = null;
        for (Server prev : prevLayer) {
          Link link = prev.getLink(curr);
          if (link != null) {
            double cost = edgeWeightFunction.apply(link) + pathCost.get(prev);
            if (cost < minCost) {
              minCost = cost;
              minPrev = prev;
            }
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
    Server dest = this.getDestination();
    for (Server prev : prevLayer) {
      Link link = prev.getLink(dest);
      if (link != null) {
        double cost = edgeWeightFunction.apply(link) + pathCost.get(prev);
        if (cost < minCost) {
          minCost = cost;
          minPrev = prev;
        }
      }
    }
    prevNode.put(dest, minPrev);

    return extractPath(request, prevNode, dest);
  }

  public ArrayList<Server> findDelayAwareShortestPath() {
    CostFunction costOnly = new CostFunction() {
      @Override public double getCost(Server server, int nfv, Parameters parameters) {
        return costFunction.getCost(server, nfv, parameters);
      }

      @Override public double getCost(Link l, double b, Parameters parameters) {
        return costFunction.getCost(l, b, parameters);
      }
    };

    CostFunction delayOnly = new CostFunction() {
      @Override public double getCost(Server server, int nfv, Parameters parameters) {
        return 0;
      }

      @Override public double getCost(Link l, double b, Parameters parameters) {
        return l.getDelay();
      }
    };

    // PC is the shortest path on the original cost c
    ArrayList<Server> pathC = findShortestPath(l -> l.getWeight());
    if (pathC == null) {
      return null;
    }

    double pathCCost = this.calculatePathCost(pathC, costOnly);
    double pathCDelay = this.calculatePathCost(pathC, delayOnly);
    if (pathCDelay <= request.getDelayReq()) {
      return pathC; // clearly no solution exists
    }

    ArrayList<Server> pathD = findShortestPath(l -> l.getDelay());
    if (pathD == null) {
      return null;
    }
    double pathDCost = this.calculatePathCost(pathD, costOnly);
    double pathDDelay = this.calculatePathCost(pathD, delayOnly);
    if (pathDDelay > request.getDelayReq()) {
      return null;
    }

    while (true) {
      final double lambda = (pathCCost - pathDCost) / (pathDDelay - pathCDelay);
      CostFunction modifiedCostFunction = new CostFunction() {
        @Override public double getCost(Link l, double b, Parameters parameters) {
          return costFunction.getCost(l, b, parameters) + lambda * l.getDelay();
        }

        @Override public double getCost(Server server, int nfv, Parameters parameters) {
          return costFunction.getCost(server, nfv, parameters);
        }
      };

      ArrayList<Server> pathR = findShortestPath(l -> l.getWeight() + lambda * l.getDelay());
      if (pathR == null) {
        return null;
      }

      double pathRCost = this.calculatePathCost(pathR, costOnly);
      double pathRDelay = this.calculatePathCost(pathR, delayOnly);

      if (pathRCost == pathCCost) {
        return pathD;
      }

      if (this.calculatePathCost(pathR, modifiedCostFunction) == this.calculatePathCost(pathC, modifiedCostFunction)) {
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

  private HashSet<Server> getServiceLayer(int index) {
    return serviceLayers.get(index);
  }

  public Server getSource() {
    return source;
  }

  /**
   * TODO: I don't know why we even need to re-calculate the cost of each edge and server. Haven't we done this during the construction of auxiliary graph?
   *
   * @param serversOnPath the list of servers on the path
   * @param costFunction cost function
   * @return the cost of path @serversOnPath with respect to a given cost function @costFunction
   */
  public double calculatePathCost(ArrayList<Server> serversOnPath, CostFunction costFunction) {
    if (serversOnPath.size() != request.getSC().length + 2) { //No path was found
      return Double.MAX_VALUE;
    }
    HashMap<Link, Link> clonedLinks = new HashMap<>();
    HashMap<Integer, Server> clonedServers = new HashMap<>();
    for (Link l : links) {
      clonedLinks.put(l, new Link(l));
    }
    for (Server s : this.servers) {
      clonedServers.put(s.getId(), new Server(s));
    }

    double cost = 0;

    //get server costs
    for (int i = 1; i < serversOnPath.size() - 1; i++) {
      Server cs = clonedServers.get(serversOnPath.get(i).getId());
      int nfv = request.getSC()[i - 1];
      cost += costFunction.getCost(cs, nfv, this.parameters);
      if (!cs.canCreateVM(nfv)) {
        return Double.MAX_VALUE;
      }
      cs.addVM(nfv);
    }

    //get link costs
    for (int i = 0; i < serversOnPath.size() - 1; i++) {
      Server s1 = serversOnPath.get(i);
      Server s2 = serversOnPath.get(i + 1);
      for (Link l : getLinkPath(s1, s2)) {
        Link cl = clonedLinks.get(l);
        cost += costFunction.getCost(cl, request.getBandwidth(), this.parameters);
        if (!cl.canSupportBandwidth(request.getBandwidth())) {//obviously a rejection
          return Double.MAX_VALUE;
        }
        cl.allocateBandwidth(request.getBandwidth());
      }
    }
    return cost;
  }

  public Server getDestination() {
    return destination;
  }

  private HashSet<Server> cloneServers(Collection<Server> svrs) {
    HashSet<Server> clones = new HashSet<Server>();
    for (Server s : svrs) {
      clones.add(new Server(s));
    }
    return clones;
  }

  public ArrayList<Link> getLinkPath(Server s1, Server s2) {
    if (s1.getId() == s2.getId()) {
      return new ArrayList<Link>();
    }
    return allShortestPaths.get(s1.getId()).get(s2.getId());
  }

  public void admitRequestAndReserveResources(ArrayList<Server> path) {//assign network resources for request
    //update servers
    for (int i = 0; i < request.getSC().length; i++) { //the first and last server are the source and destination node of the request.
      int nfv = request.getSC()[i];
      Server s = path.get(i + 1);
      useNFV(s.getId(), nfv);
    }

    //update links
    for (int i = 0; i < path.size() - 1; i++) {
      Server s1 = path.get(i);
      Server s2 = path.get(i + 1);
      //The links in the allShortestPaths mapping are from the original network.
      allocateBandwidthOnPath(allShortestPaths.get(s1.getId()).get(s2.getId()), request.getBandwidth());
    }
  }
}
