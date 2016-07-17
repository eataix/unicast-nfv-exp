package NetworkGenerator;

import java.util.ArrayList;
import java.util.HashSet;

import Network.Link;
import Network.Network;
import Network.Server;
import Simulation.Parameters;
import Simulation.Simulation;

public class NetworkValueSetter { //sets the parameters of a given network
  private final Network network;
  private final Parameters parameters;

  public NetworkValueSetter(Network network, Parameters parameters) {
    this.network = network;
    this.parameters = parameters;
  }

  public Network getNetwork() {
    return network;
  }

  //probability of any given nfv instance already deployed on a given server. Guarantees each nfv is on at least one server.
  public void placeNFVs(double nfvProb) {
    for (int nfv = 0; nfv < parameters.L; nfv++) {
      ArrayList<Server> servers = network.getUnusedServers(nfv);
      for (Server s : servers) {
        if (Math.random() < nfvProb) {
          s.addVM(nfv);
        }
      }
      HashSet<Server> layer = network.getReusableServers(nfv);
      if (layer.isEmpty()) { //ensure there is at least one server with nfv
        Server s = servers.get((int) Math.floor(Math.random() * servers.size()));
        s.addVM(nfv);
      }
    }
  }

  public void setConstantServerCapacity(int cap, double serverRatio) {
    for (Server s : network.getServers()) {
      if (Math.random() < serverRatio) {
        s.setCapacity(cap);
      } else {
        s.setCapacity(0);
      }
    }
    Server s = network.getRandomServer();
    s.setCapacity(cap); //ensure there is at least one server with capacity
  }

  public void setNormalServerCapacity(int mean, int weight, double serverRatio) {
    for (Server s : network.getServers()) {
      if (Math.random() < serverRatio) {
        s.setCapacity(getNormal(mean, weight));
      } else {
        s.setCapacity(0);
      }
    }
  }

  public void setRandomServerCapacity(int low, int high, double serverRatio) {
    for (Server s : network.getServers()) {
      if (Math.random() < serverRatio) {
        s.setCapacity(getUniform(low, high));
      } else {
        s.setCapacity(0);
      }
    }
  }

  public void setConstantLinkCapacity(int cap) {
    for (Link l : network.getLinks()) {
      l.setBandwidth(cap);
    }
  }

  public void setNormalLinkCapacity(int mean, int weight) {
    for (Link l : network.getLinks()) {
      l.setBandwidth(getNormal(mean, weight));
    }
  }

  public void setRandomLinkCapacity(int low, int high) {
    for (Link l : network.getLinks()) {
      l.setBandwidth(getUniform(low, high));
    }
  }

  public void setConstantLinkDelay(int cap) {
    for (Link l : network.getLinks()) {
      l.setDelay(cap);
    }
  }

  public void setNormalLinkDelay(int mean, int weight) {
    for (Link l : network.getLinks()) {
      l.setDelay(getNormal(mean, weight));
    }
  }

  public void setRandomLinkDelay(int low, int high) {
    for (Link l : network.getLinks()) {
      l.setDelay(getUniform(low, high));
    }
  }

  public void setConstantNFVRequirements(int cap) {
    for (int nfv = 0; nfv < parameters.L; nfv++) {
      parameters.nfvReqs[nfv] = cap;
    }
  }

  public void setNormalNFVRequirements(int mean, int weight) {
    for (int nfv = 0; nfv < parameters.L; nfv++) {
      parameters.nfvReqs[nfv] = getNormal(mean, weight);
    }
  }

  public void setRandomNFVRequirements(int low, int high) {
    for (int nfv = 0; nfv < parameters.L; nfv++) {
      parameters.nfvReqs[nfv] = getUniform(low, high);
    }
  }

  public void setConstantNFVServiceRate(int cap) {
    for (int nfv = 0; nfv < parameters.L; nfv++) {
      parameters.nfvRates[nfv] = cap;
    }
  }

  public void setNormalNFVServiceRate(int mean, int weight) {
    for (int nfv = 0; nfv < parameters.L; nfv++) {
      parameters.nfvRates[nfv] = getNormal(mean, weight);
    }
  }

  public void setRandomNFVServiceRate(int low, int high) {
    for (int nfv = 0; nfv < parameters.L; nfv++) {
      parameters.nfvRates[nfv] = getUniform(low, high);
    }
  }

  public static int getNormal(int mean, int weight) {
    int cap = -1;
    while (cap < 0) {
      cap = (int) Math.round(Simulation.random.nextGaussian() * weight + mean);
    }
    return cap;
  }

  public static int getUniform(int low, int high) {
    return (int) (low + Math.random() * (high - low));
  }
}
