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
  private Server src;
  private Server dst;
  private final ArrayList<Server> auxServers;
  private final ArrayList<Link> auxLinks;
  private final Parameters parameters;

  public AuxiliaryNetwork(ArrayList<Server> originalServers, ArrayList<Link> originalLinks, double[][] pathcosts, double[][] pathdelays,
                          HashMap<Integer, HashMap<Integer, ArrayList<Link>>> asp, Request r, Parameters parameters) {
    super(originalServers, originalLinks);
    pathCosts = pathcosts;
    pathDelays = pathdelays;
    allShortestPaths = asp;
    request = r;
    auxServers = new ArrayList<Server>();
    auxLinks = new ArrayList<Link>();
    serviceLayers = new ArrayList<HashSet<Server>>();
    this.parameters = parameters;
  }

  public void generateNetwork(boolean offline) { //create network with auxServers and auxLinks
    src = request.src.clone();
    dst = request.dst.clone();
    auxServers.add(src);
    auxServers.add(dst);
    HashSet<Server> prevLayer = new HashSet<Server>();
    prevLayer.add(src);
    int[] SC = request.SC;
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
    for (Server prev : prevLayer) { //link this up to dst
      Link l = new Link(prev, dst);
      l.setDelay(pathDelays[dst.getId()][prev.getId()]);
      l.setPathCost(pathCosts[dst.getId()][prev.getId()]);
    }
  }

  public HashSet<Server> getServiceLayer(int index) {
    return serviceLayers.get(index);
  }

  public Server getSource() {
    return src;
  }

  public Server getDestination() {
    return dst;
  }

  private HashSet<Server> cloneServers(Collection<Server> svrs) {
    HashSet<Server> clones = new HashSet<Server>();
    for (Server s : svrs) {
      clones.add(s.clone());
    }
    return clones;
  }

  public ArrayList<Link> getLinkPath(Server s1, Server s2) {
    if (s1.getId() == s2.getId()) {
      return new ArrayList<Link>();
    }
    return allShortestPaths.get(s1.getId()).get(s2.getId());
  }

  public double calculatePathCost(ArrayList<Server> path, CostFunction cf) {
    if (path.size() != request.SC.length + 2) { //No path was found
      return Double.MAX_VALUE;
    }
    HashMap<Link, Link> clonedLinks = new HashMap<Link, Link>();
    HashMap<Integer, Server> clonedServers = new HashMap<Integer, Server>();
    for (Link l : links) {
      clonedLinks.put(l, l.clone());
    }
    for (Server s : servers) {
      clonedServers.put(s.getId(), s.clone());
    }

    double cost = 0;

    //get server costs
    for (int i = 1; i < path.size() - 1; i++) {
      Server cs = clonedServers.get(path.get(i).getId());
      int nfv = request.SC[i - 1];
      cost += cf.getCost(cs, nfv, this.parameters);
      if (!cs.canCreateVM(nfv)) {
        return Double.MAX_VALUE;
      }
      cs.addVM(nfv);
    }

    //get link costs
    for (int i = 0; i < path.size() - 1; i++) {
      Server s1 = path.get(i);
      Server s2 = path.get(i + 1);
      for (Link l : getLinkPath(s1, s2)) {
        Link cl = clonedLinks.get(l);
        cost += cf.getCost(cl, request.bandwidth, this.parameters);
        if (!cl.canSupportBandwidth(request.bandwidth)) {//obviously a rejection
          return Double.MAX_VALUE;
        }
        cl.allocateBandwidth(request.bandwidth);
      }
    }
    return cost;
  }

  public void admitRequest(ArrayList<Server> path) {//assign network resources for request
    //update servers
    for (int i = 0; i < request.SC.length; i++) { //the first and last server are the src and destination node of the request.
      int nfv = request.SC[i];
      Server s = path.get(i + 1);
      useNFV(s.getId(), nfv);
    }

    //update links
    for (int i = 0; i < path.size() - 1; i++) {
      Server s1 = path.get(i);
      Server s2 = path.get(i + 1);
      //The links in the allShortestPaths mapping are from the original network.
      allocateBandwidthOnPath(allShortestPaths.get(s1.getId()).get(s2.getId()), request.bandwidth);
    }
  }
}
