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

  private static double getNormal(double mean, double weight) {
    int cap = -1;
    while (cap < 0) {
      cap = (int) Math.round(Simulation.random.nextGaussian() * weight + mean);
    }
    return (double) cap;
  }

  public static double getUniform(double low, double high) {
    return low + Math.random() * (high - low);
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
        Server s = servers.get((int) Math.floor(Math.random() * (double) servers.size()));
        s.addVM(nfv);
      }
    }
  }

  public void setConstantServerCapacity(double cap, double serverRatio) {
    for (Server s : network.getServers()) {
      if (Math.random() < serverRatio) {
        s.setComputingCapacity(cap);
      } else {
        s.setComputingCapacity(0d);
      }
    }
    Server s = network.getRandomServer();
    s.setComputingCapacity(cap); //ensure there is at least one server with capacity
  }

  public void setNormalServerCapacity(double mean, double weight, double serverRatio) {
    for (Server s : network.getServers()) {
      if (Math.random() < serverRatio) {
        s.setComputingCapacity(getNormal(mean, weight));
      } else {
        s.setComputingCapacity(0d);
      }
    }
  }

  public void setRandomServerCapacity(double low, double high, double serverRatio) {
    for (Server s : network.getServers()) {
      if (Math.random() < serverRatio) {
        s.setComputingCapacity(getUniform(low, high));
      } else {
        s.setComputingCapacity(0d);
      }
    }
  }

  public void setConstantLinkCapacity(double cap) {
    for (Link l : network.getLinks()) {
      l.setBandwidth(cap);
    }
  }

  public void setNormalLinkCapacity(double mean, double weight) {
    for (Link l : network.getLinks()) {
      l.setBandwidth(getNormal(mean, weight));
    }
  }

  public void setRandomLinkCapacity(double low, double high) {
    for (Link l : network.getLinks()) {
      l.setBandwidth(getUniform(low, high));
    }
  }

  public void setConstantLinkDelay(double cap) {
    for (Link l : network.getLinks()) {
      l.setDelay(cap);
    }
  }

  public void setNormalLinkDelay(double mean, double weight) {
    for (Link l : network.getLinks()) {
      l.setDelay(getNormal(mean, weight));
    }
  }

  public void setRandomLinkDelay(double low, double high) {
    for (Link l : network.getLinks()) {
      l.setDelay(getUniform(low, high));
    }
  }

  public void setConstantNFVRequirements(double cap) {
    for (int nfv = 0; nfv < parameters.L; nfv++) {
      parameters.nfvComputingReqs[nfv] = cap;
    }
  }

  public void setNormalNFVRequirements(double mean, double weight) {
    for (int nfv = 0; nfv < parameters.L; nfv++) {
      parameters.nfvComputingReqs[nfv] = getNormal(mean, weight);
    }
  }

  public void setRandomNFVRequirements(double low, double high) {
    for (int nfv = 0; nfv < parameters.L; nfv++) {
      parameters.nfvComputingReqs[nfv] = getUniform(low, high);
    }
  }

  public void setConstantNFVServiceRate(double cap) {
    for (int nfv = 0; nfv < parameters.L; nfv++) {
      parameters.nfvRates[nfv] = cap;
    }
  }

  public void setNormalNFVServiceRate(double mean, double weight) {
    for (int nfv = 0; nfv < parameters.L; nfv++) {
      parameters.nfvRates[nfv] = getNormal(mean, weight);
    }
  }

  public void setRandomNFVServiceRate(double low, double high) {
    for (int nfv = 0; nfv < parameters.L; nfv++) {
      parameters.nfvRates[nfv] = getUniform(low, high);
    }
  }
}
