package Network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import Simulation.Simulation;

public class Server {
  private final int id;
  private int capacity; //some servers have 0 capacity as they are switches
  private final ArrayList<Link> links;
  private HashMap<Integer, VM> NFVs;

  public Server clone() {
    HashMap<Integer, VM> newNFVs = new HashMap<>();
    for (Integer nfv : NFVs.keySet()) {
      newNFVs.put(nfv, new VM(nfv));
    }
    return new Server(id, capacity, new ArrayList<>(), newNFVs);
  }

  private Server(int Id, int c, ArrayList<Link> l, HashMap<Integer, VM> nfvs) {
    this.id = Id;
    this.capacity = c;
    this.links = l;
    this.NFVs = nfvs;
  }

  public Server(int Id) {
    id = Id;
    NFVs = new HashMap<Integer, VM>();
    links = new ArrayList<Link>();
  }

  public int getId() {
    return id;
  }

  public int getCapacity() {
    return capacity;
  }

  public void setCapacity(int c) {
    capacity = c;
  }

  public void wipe() {
    NFVs = new HashMap<Integer, VM>();
  }

  public int getDegree() { //just in case there are duplicate links. There shouldn't be, but just in case...
    HashSet<Server> neighbours = new HashSet<>();
    for (Link l : links) {
      neighbours.add(l.getLinkedServer(this));
    }
    return neighbours.size();
  }

  public double getOpCost(int nfv) { //operational cost
    if (NFVs.get(nfv) == null) {
      return Simulation.defaultParameters.NFVOpCost[nfv] + Simulation.defaultParameters.NFVInitCost[nfv];
    }
    return Simulation.defaultParameters.NFVOpCost[nfv];
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
    ArrayList<Server> neighbours = new ArrayList<>();
    for (Link l : links) {
      neighbours.add(l.getLinkedServer(this));
    }
    return neighbours;
  }

  public ArrayList<Server> getReachableNeighbours(int b) {
    //get neighbours where link can carry additional bandwidth b
    ArrayList<Server> neighbours = new ArrayList<>();
    for (Link l : links) {
      if (l.canSupportBandwidth(b)) {
        neighbours.add(l.getLinkedServer(this));
      }
    }
    return neighbours;
  }

  public int remainingCapacity() {
    int sum = 0;
    for (VM vm : NFVs.values()) {
      sum += vm.resourceAllocated();
    }
    return capacity - sum;
  }

  public boolean canReuseVM(int nfv) { //will need to check whether service rate of VM exceeds arrival rate of packets
    return NFVs.containsKey(nfv);
  }

  public boolean canCreateVM(int nfv) { //has spare capacity to create enough VMs to handle rate
    int serviceReq = Simulation.defaultParameters.NFVreq[nfv];
    return serviceReq < remainingCapacity();
  }

  public boolean addVM(int nfv) { //add VM for nfv to server. Server can contain multiple VMs with same NFV to serve higher demand.
    if (capacity == 0 || !canCreateVM(nfv)) {
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

  private class VM {
    final int serviceType;

    VM(int nfv) {
      serviceType = nfv;
    }

    int resourceAllocated() {
      return Simulation.defaultParameters.NFVreq[serviceType];
    }
  }

  @Override
  public String toString() {
    return "Server: " + this.id + " Capacity: " + this.capacity;
  }

/*
  private class VM { //service rate must exceed arrival rate.
		int arrivalRate;
		int serviceType;
		int instances; //you can have multiple VM instances that run the same service to meet higher arrival rate of packets.

		public VM(int nfv, int arr){
			serviceType = nfv;
			arrivalRate = arr;
			instances = ceilingIntDivision(arr, parameters.NFVrate[serviceType]);
		}

		public void createMoreInstances(int arr){ //create enough instances to meet demand of additional arrival rate arr.
			instances += additionalInstancesRequired(arr);
			arrivalRate += arr;
		}

		public int additionalInstancesRequired(int arr){ //number of additional VMs required to service arrival rate arr.
			int serviceRate = instances*parameters.NFVrate[serviceType];
			int diff = arrivalRate + arr - serviceRate;
			if(diff<0) return 0;
			else	   return ceilingIntDivision(diff, serviceRate);
		}

		public int resourceAllocated(){
			return instances*parameters.NFVreq[serviceType];
		}

		public int ceilingIntDivision(int num, int divisor) {
		    return (num + divisor - 1) / divisor;
		}

	}*/
}
