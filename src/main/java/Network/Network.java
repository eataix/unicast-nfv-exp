package Network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class Network {
  private final ArrayList<Server> servers;
  private final HashMap<Integer, Server> serversById;
  private ArrayList<Link> links;

  public Network(ArrayList<Server> servers, ArrayList<Link> links) {
    this.servers = servers;
    this.links = links;

    serversById = new HashMap<>(); // this is a safer way of getting a server by Id than relying on the ordering of the arraylist.
    for (Server s : servers) {
      serversById.put(s.getId(), s);
    }
  }

  public static Network newNetwork(Network oNetwork) {
    ArrayList<Server> servers = new ArrayList<>();
    HashMap<Server, Server> serverMap = new HashMap<>();

    for (Server oldServer : oNetwork.getServers()) {
      Server newServer = new Server(oldServer);
      servers.add(newServer);
      serverMap.put(oldServer, newServer);
    }

    ArrayList<Link> links = new ArrayList<>();
    for (Link oldLink : oNetwork.getLinks()) {
      Server newS1 = serverMap.get(oldLink.getS1());
      Server newS2 = serverMap.get(oldLink.getS2());
      Link link = new Link(newS1, newS2, oldLink.getBandwidthCapacity(), oldLink.getAllocatedBandwidth(), oldLink.getDelay(), oldLink.getOperationalCost());
      links.add(link);
    }

    Network newNetwork = new Network(servers, links);
    checkState(newNetwork.getLinks().size() == oNetwork.getLinks().size() && newNetwork.getServers().size() == oNetwork.getServers().size());
    return newNetwork;
  }

  public int size() {
    return servers.size();
  }

  public ArrayList<Server> getServers() {
    return servers;
  }

  public ArrayList<Link> getLinks() {
    return links;
  }

  public void setLinks(ArrayList<Link> newLinks) {
    links = newLinks;
  }

  public Server getRandomServer() {
    int i = (int) (Math.random() * (double) servers.size());
    return servers.get(i);
  }

  public HashSet<Server> getReusableServers(int nfv) {
    HashSet<Server> serviceLayer = new HashSet<Server>();
    for (Server s : servers) {
      if (s.canReuseVM(nfv)) {
        serviceLayer.add(s);
      }
    }
    return serviceLayer;
  }

  public ArrayList<Server> getUnusedServers(int nfv) { //returns an arraylist in case you want to select random server
    ArrayList<Server> usableServers = new ArrayList<Server>();
    for (Server s : servers) {
      if (s.canCreateVM(nfv)) {
        usableServers.add(s);
      }
    }
    return usableServers;
  }

  public boolean isConnected() {
    HashSet<Server> searched = new HashSet<Server>();
    ArrayList<Server> queue = new ArrayList<Server>();
    queue.add(servers.get(0));
    while (!queue.isEmpty()) {
      Server curr = queue.remove(0);
      for (Server s : curr.getAllNeighbours()) {
        if (!searched.contains(s)) {
          queue.add(s);
        }
      }
      searched.add(curr);
    }
    return searched.size() == servers.size();
  }

  public void wipeLinks() {//this returns all servers and bandwidth to 0% utilization
    for (Link l : links) {
      l.wipe();
    }
  }

  void allocateBandwidthOnPath(ArrayList<Link> path, double b) {
    checkNotNull(path);
    for (Link l : path) {
      l.allocateBandwidth(b);
    }
  }

  void useNFV(int serverId, int nfv) {
    Server s = serversById.get(serverId);
    if (!s.canReuseVM(nfv)) {
      s.addVM(nfv);
    }
  }

  @Override public String toString() {
    return String.format("Network{servers=%s, links=%s}", servers, links);
  }
}

