package Network;

import java.util.HashSet;

import NetworkGenerator.NetworkValueSetter;
import Simulation.Parameters;

public class Request {
  private final int bandwidth;
  private final Server source;
  private final Server destination;
  private final Parameters parameters;
  private int[] SC; //each number points to index of nfv in Parameters
  private double delayReq; //delayReq bound requirement

  public Request(Server source, Server destination, Parameters parameters) {
    this.source = source;
    this.destination = destination;
    this.parameters = parameters;
    this.bandwidth = NetworkValueSetter.getUniform(parameters.reqBWReqMin, parameters.reqBWReqMax);
    this.delayReq = NetworkValueSetter.getUniform(parameters.reqDelayReqMin, parameters.reqDelayReqMax);
    generateServiceChain();
  }

  public boolean setServiceChain(int[] sc) {
    //make sure all elements are valid nfv ids, and there are no repeats.
    HashSet<Integer> nfvs = new HashSet<>();
    for (int aSc : sc) {
      if (aSc >= parameters.L || nfvs.contains(aSc)) {
        return false;
      } else {
        nfvs.add(aSc);
      }
    }
    SC = sc;
    return true;
  }

  private void generateServiceChain() {
    //create randomly ordered list of NFVs
    int[] nfvs = new int[parameters.L];
    for (int i = 0; i < parameters.L; i++) {
      nfvs[i] = i;
    }
    //fisher-yates shuffle
    for (int i = 0; i < parameters.L; i++) {
      int temp = nfvs[i];
      int index = (int) Math.floor(Math.random() * (parameters.L - i) + i);
      nfvs[i] = nfvs[index];
      nfvs[index] = temp;
    }

    int l = (int) Math.floor(Math.random() * (parameters.L - 1) + 1); //ensure there is at least one service in the service chain

    SC = new int[l];
    System.arraycopy(nfvs, 0, SC, 0, l);
  }

  public int[] getSC() {
    return SC;
  }

  public int getBandwidth() {
    return bandwidth;
  }

  public Server getSource() {
    return source;
  }

  public Server getDestination() {
    return destination;
  }

  public double getDelayReq() {
    return delayReq;
  }

  public Parameters getParameters() {
    return parameters;
  }
}
