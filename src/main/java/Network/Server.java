package Network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import Simulation.Simulation;

import static com.google.common.base.Preconditions.checkArgument;

public class Server {
  private final int id;
  private double computingCapacity; //some servers have 0 computingCapacity as they are switches
  private final ArrayList<Link> links;
  private HashMap<Integer, VM> NFVs;

  public Server(Server server) {
    this(server.getId(), server.getComputingCapacity(), new ArrayList<>(), copyVMs(server.NFVs));
  }

  private Server(int Id, double computingCapacity, ArrayList<Link> links, HashMap<Integer, VM> NFVs) {
    this.id = Id;
    this.computingCapacity = computingCapacity;
    this.links = links;
    this.NFVs = NFVs;
  }

  public Server(int Id) {
    id = Id;
    NFVs = new HashMap<Integer, VM>();
    links = new ArrayList<Link>();
  }

  private static HashMap<Integer, VM> copyVMs(HashMap<Integer, VM> NFVs) {
    HashMap<Integer, VM> newNFVs = new HashMap<>();
    for (Integer nfv : NFVs.keySet()) {
      newNFVs.put(nfv, new VM(nfv));
    }
    return newNFVs;
  }

  public int getId() {
    return id;
  }

  public double getComputingCapacity() {
    return computingCapacity;
  }

  public void setComputingCapacity(double computingCapacity) {
    this.computingCapacity = computingCapacity;
  }

  public void wipe() {
    NFVs = new HashMap<Integer, VM>();
  }

  public int getDegree() { //just in case there are duplicate links. There shouldn't be, but just in case...
    HashSet<Server> neighbours = links.stream().map(l -> l.getLinkedServer(this)).collect(Collectors.toCollection(HashSet::new));
    return neighbours.size();
  }

  public double getOperationalCost(int nfv) { //operational cost
    if (NFVs.get(nfv) == null) {
      return Simulation.baseParameters.nfvOperationalCosts[nfv] + Simulation.baseParameters.nfvInitCosts[nfv];
    }
    return Simulation.baseParameters.nfvOperationalCosts[nfv];
  }

  public Link getLink(Server s) {//returns link that connects "this" to Server s
    for (Link l : links) {
      if (l.getLinkedServer(this) == s) {
        return l;
      }
    }
    return null;
  }

  public void removeLink(Server s) {//removes link to server s if there exists one
    for (int i = 0; i < links.size(); i++) {
      Link l = links.get(i);
      if (l.getLinkedServer(this) == s) {
        links.remove(i);
        return;
      }
    }
  }

  public ArrayList<Server> getAllNeighbours() {
    ArrayList<Server> neighbours = links.stream().map(l -> l.getLinkedServer(this)).collect(Collectors.toCollection(ArrayList::new));
    return neighbours;
  }

  public ArrayList<Server> getReachableNeighbours(int b) {
    //get neighbours where link can carry additional bandwidth b
    ArrayList<Server> neighbours =
        links.stream().filter(l -> l.canSupportBandwidth(b)).map(l -> l.getLinkedServer(this)).collect(Collectors.toCollection(ArrayList::new));
    return neighbours;
  }

  public double remainingCapacity() {
    double sum = 0;
    for (VM vm : NFVs.values()) {
      sum += vm.resourceAllocated();
    }
    return computingCapacity - sum;
  }

  public boolean canReuseVM(int nfv) { //will need to check whether service rate of VM exceeds arrival rate of packets
    return NFVs.containsKey(nfv);
  }

  public boolean canCreateVM(int nfv) { //has spare computingCapacity to create enough VMs to handle rate
    double serviceReq = Simulation.baseParameters.nfvComputingReqs[nfv];
    return serviceReq < remainingCapacity();
  }

  public boolean addVM(int nfv) { //add VM for nfv to server. Server can contain multiple VMs with same NFV to serve higher demand.
    checkArgument(computingCapacity > 0 || canCreateVM(nfv));
    if (computingCapacity == 0 || !canCreateVM(nfv)) {
      return false;
    }
    if (!NFVs.containsKey(nfv)) {
      NFVs.put(nfv, new VM(nfv));
    }
    return true;
  }

  void addLink(Link l) {
    Server other = l.getLinkedServer(this);
    if (!getAllNeighbours().contains(other)) {
      links.add(l);
    }
  }

  @Override
  public String toString() {
    return "Server: " + this.id + " Capacity: " + this.computingCapacity;
  }

  private static class VM {
    final int serviceType;

    VM(int nfv) {
      serviceType = nfv;
    }

    double resourceAllocated() {
      return Simulation.baseParameters.nfvComputingReqs[serviceType];
    }
  }
}
