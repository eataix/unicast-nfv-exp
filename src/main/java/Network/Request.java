package Network;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import NetworkGenerator.NetworkValueSetter;
import Simulation.Parameters;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkState;

public class Request {
  @NotNull private final Server source;
  @NotNull private final Server destination;
  private double bandwidth;
  private double delayReq; //delayReq bound requirement
  @NotNull private final Parameters parameters;
  private int[] SC; //each number points to index of nfv in Parameters

  public Request(@NotNull Server source, @NotNull Server destination, @NotNull Parameters parameters) {
    this.source = source;
    this.destination = destination;
    this.parameters = parameters;
    this.bandwidth = NetworkValueSetter.getUniform(parameters.reqBWReqMin, parameters.reqBWReqMax);
    this.delayReq = NetworkValueSetter.getUniform(parameters.reqDelayReqMin, parameters.reqDelayReqMax);
    generateServiceChain();
  }

  public Request newRequest(@NotNull HashMap<Server, Server> serverMap) {
    checkState(serverMap.containsKey(this.getSource()) && serverMap.containsKey(this.getDestination()));
    Request newRequest = new Request(serverMap.get(this.getSource()), serverMap.get(this.getDestination()), this.parameters);
    newRequest.setDelayReq(this.getDelayReq());
    newRequest.setBandwidth(this.getBandwidth());
    newRequest.setServiceChain(Arrays.copyOf(this.getSC(), this.getSC().length));
    checkState(this.getSource().getId() == newRequest.getSource().getId() && this.getDestination().getId() == newRequest.getDestination().getId());
    return newRequest;
  }

  public void setBandwidth(double bandwidth) {
    this.bandwidth = bandwidth;
  }

  private void setDelayReq(double delayReq) {
    this.delayReq = delayReq;
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

  public void setServiceChain(int[] sc) {
    //make sure all elements are valid nfv ids, and there are no repeats.
    HashSet<Integer> nfvs = new HashSet<>();
    for (int aSc : sc) {
      if (aSc >= parameters.L || nfvs.contains(aSc)) {
        return;
      } else {
        nfvs.add(aSc);
      }
    }
    SC = sc;
  }

  public int[] getSC() {
    return SC;
  }

  public double getBandwidth() {
    return bandwidth;
  }

  @NotNull public Server getSource() {
    return source;
  }

  @NotNull public Server getDestination() {
    return destination;
  }

  public double getDelayReq() {
    return delayReq;
  }

  @NotNull public Parameters getParameters() {
    return parameters;
  }
}
