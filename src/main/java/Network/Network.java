package Network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public class Network {
  @NotNull private final ArrayList<Server> servers;
  @NotNull private final HashMap<Integer, Server> serversById;
  @NotNull private ArrayList<Link> links;
  
  // used in offline networks
  private double[][] pathCosts;
  private double[][] pathDelays;
  private HashMap<Integer, HashMap<Integer, ArrayList<Link>>> allPairShortestPaths;

  public Network(@NotNull ArrayList<Server> servers, @NotNull ArrayList<Link> links) {
    this.servers = servers;
    this.links = links;

    serversById = new HashMap<>(); // this is a safer way of getting a server by Id than relying on the ordering of the arraylist.
    for (Server s : servers) {
      serversById.put(s.getId(), s);
    }
  }

  public Network newNetwork(@NotNull HashMap<Server, Server> serverMap) {
    ArrayList<Server> servers = new ArrayList<>();

    for (Server oldServer : this.getServers()) {
      Server newServer = new Server(oldServer);
      servers.add(newServer);
      serverMap.put(oldServer, newServer);
    }

    ArrayList<Link> links = new ArrayList<>();
    for (Link oldLink : this.getLinks()) {
      Server newS1 = serverMap.get(oldLink.getS1());
      Server newS2 = serverMap.get(oldLink.getS2());
      Link link = new Link(newS1, newS2, oldLink.getBandwidthCapacity(), oldLink.getAllocatedBandwidth(), oldLink.getDelay(), oldLink.getOperationalCost());
      links.add(link);
    }

    Network newNetwork = new Network(servers, links);
    checkState(newNetwork.getLinks().size() == this.getLinks().size() && newNetwork.getServers().size() == this.getServers().size());

    return newNetwork;
  }

  public int size() {
    return servers.size();
  }

  @NotNull public ArrayList<Server> getServers() {
    return servers;
  }

  @NotNull public ArrayList<Link> getLinks() {
    return links;
  }

  public void setLinks(@NotNull ArrayList<Link> newLinks) {
    links = newLinks;
  }

  public Server getRandomServer() {
    checkState(!servers.isEmpty());
    int i = (int) (Math.random() * (double) servers.size());
    return servers.get(i);
  }

  public HashSet<Server> getReusableServers(int nfv) {
    return servers.stream().filter(s -> s.canReuseVM(nfv)).collect(Collectors.toCollection(HashSet::new));
  }

  public ArrayList<Server> getUnusedServers(int nfv) { //returns an arraylist in case you want to select random server
    return servers.stream().filter(s -> s.canCreateVM(nfv)).collect(Collectors.toCollection(ArrayList::new));
  }

  public boolean isConnected() {
    HashSet<Server> searched = new HashSet<>();
    ArrayList<Server> queue = new ArrayList<>();
    queue.add(servers.get(0));
    while (!queue.isEmpty()) {
      Server curr = queue.remove(0);
      queue.addAll(curr.getAllNeighbours().stream().filter(s -> !searched.contains(s)).collect(Collectors.toList()));
      searched.add(curr);
    }
    return searched.size() == servers.size();
  }

  public void wipeLinks() {//this returns all servers and bandwidth to 0% utilization
    for (Link l : links) {
      l.wipe();
    }
  }

  void allocateBandwidthOnPath(@NotNull ArrayList<Link> path, double bandwidth) {
    checkArgument(bandwidth >= 0d);
    for (Link l : path) {
      l.allocateBandwidth(bandwidth);
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

public double[][] getPathCosts() {
	return pathCosts;
}

public void setPathCosts(double[][] pathCosts) {
	this.pathCosts = pathCosts;
}

public double[][] getPathDelays() {
	return pathDelays;
}

public void setPathDelays(double[][] pathDelays) {
	this.pathDelays = pathDelays;
}

public HashMap<Integer, HashMap<Integer, ArrayList<Link>>> getAllPairShortestPaths() {
	return allPairShortestPaths;
}

public void setAllPairShortestPaths(HashMap<Integer, HashMap<Integer, ArrayList<Link>>> allPairShortestPaths) {
	this.allPairShortestPaths = allPairShortestPaths;
}
}

