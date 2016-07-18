package Network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import Algorithm.CostFunctions.CostFunction;
import Simulation.Parameters;

public class AuxiliaryNetwork extends Network {
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

  private void generateNetwork(boolean offline) { //create network with auxServers and auxLinks
    source = new Server(request.getSource());
    destination = new Server(request.getDestination());
    auxServers.add(source);
    auxServers.add(destination);
    HashSet<Server> prevLayer = new HashSet<>();
    prevLayer.add(source);
    int[] SC = request.getSC();
    //create service layers
    for (int nfv : SC) {
      HashSet<Server> origLayer = getReusableServers(nfv);
      if (offline) {
        origLayer.addAll(getUnusedServers(nfv));
      }
      HashSet<Server> currLayer = cloneServers(origLayer);
      for (Server curr : currLayer) {
        for (Server prev : prevLayer) {
          Link l = new Link(prev, curr);
          l.setDelay(pathDelays[curr.getId()][prev.getId()]);
          l.setPathCost(pathCosts[curr.getId()][prev.getId()]);
        }
      }
      serviceLayers.add(currLayer);
      prevLayer = currLayer;
    }
    for (Server prev : prevLayer) { //link this up to destination
      Link l = new Link(prev, destination);
      l.setDelay(pathDelays[destination.getId()][prev.getId()]);
      l.setPathCost(pathCosts[destination.getId()][prev.getId()]);
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

  public double calculatePathCost(ArrayList<Server> servers, CostFunction costFunction) {
    if (servers.size() != request.getSC().length + 2) { //No path was found
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
    for (int i = 1; i < servers.size() - 1; i++) {
      Server cs = clonedServers.get(servers.get(i).getId());
      int nfv = request.getSC()[i - 1];
      cost += costFunction.getCost(cs, nfv, this.parameters);
      if (!cs.canCreateVM(nfv)) {
        return Double.MAX_VALUE;
      }
      cs.addVM(nfv);
    }

    //get link costs
    for (int i = 0; i < servers.size() - 1; i++) {
      Server s1 = servers.get(i);
      Server s2 = servers.get(i + 1);
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
