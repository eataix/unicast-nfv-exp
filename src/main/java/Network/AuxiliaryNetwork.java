package Network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import Algorithm.CostFunctions.CostFunction;
import Simulation.Parameters;

@SuppressWarnings("Duplicates") public class AuxiliaryNetwork extends Network {
  private final HashMap<Integer, HashMap<Integer, ArrayList<Link>>> allShortestPaths;
  private final double[][] pathCosts;
  private final double[][] pathDelays;
  public final ArrayList<HashSet<Server>> serviceLayers;
  private final Request request;

  private Server source;
  private Server destination;
  private final ArrayList<Server> auxServers;
  private final ArrayList<Link> auxLinks;
  private final Parameters parameters;

  public AuxiliaryNetwork(ArrayList<Server> originalServers, ArrayList<Link> originalLinks, double[][] pathCosts, double[][] pathDelays,
                          HashMap<Integer, HashMap<Integer, ArrayList<Link>>> allShortestPaths, Request request, Parameters parameters) {
    super(originalServers, originalLinks);
    this.pathCosts = pathCosts;
    this.pathDelays = pathDelays;
    this.allShortestPaths = allShortestPaths;
    this.request = request;
    this.parameters = parameters;

    auxServers = new ArrayList<Server>();
    auxLinks = new ArrayList<Link>();
    serviceLayers = new ArrayList<HashSet<Server>>();
  }

  public void generateOnlineNetwork() {
    generateNetwork(false);
  }

  public void generateOfflineNetwork() {
    generateNetwork(true);
  }

  /**
   * Create network with auxServers and auxLinks
   */
  private void generateNetwork(boolean offline) {
    source = new Server(request.getSource());
    destination = new Server(request.getDestination());
    auxServers.add(source);
    auxServers.add(destination);

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
          l.setWeight(pathCosts[curr.getId()][prev.getId()]);
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
      l.setDelay(pathDelays[destination.getId()][prev.getId()]);
      l.setWeight(pathCosts[destination.getId()][prev.getId()]);
      this.auxLinks.add(l);
    }
  }

  public HashSet<Server> getServiceLayer(int index) {
    return serviceLayers.get(index);
  }

  public Server getSource() {
    return source;
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

  /**
   * @param serversOnPath the list of servers on the path
   * @param costFunction cost function
   * @return * the cost of path @serversOnPath with respect to a given cost function @costFunction
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
