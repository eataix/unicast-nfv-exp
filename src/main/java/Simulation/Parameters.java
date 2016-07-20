package Simulation;

import java.util.Arrays;

import Algorithm.CostFunctions.CostFunction;
import Algorithm.CostFunctions.ExponentialCostFunction;

public class Parameters {
  public final int[] nfvReqs; //nfv vm resource requirements
  public final int[] nfvRates; //nfv vm service rate
  public final int[] nfvOperationalCosts; //operating cost of providing an vnf service
  public final int[] nfvInitCosts; //initialization cost of vnf service

  public final int[] nfvInitDelays; //initialization cost of vnf service
  public final int[] nfvProcessingDelays; //initialization cost of vnf service

  public final int[] networkSizes; //initialization cost of vnf service

  public final int alpha;
  public final int beta; // Multiply of beta, i.e., the value of beta would be "beta * |V|"
  public final int linkBWCapMin; // minimum link bandwidth capacity
  public final int linkBWCapMax; // maximum link bandwidth capacity
  public final int numRequest;
  public final int L;
  public final int reqBWReqMin; // minimum request bandwidth requirement
  public final int reqBWReqMax; // maximum request bandwidth requirement
  public final int reqDelayReqMin; // minimum request delay requirement
  public final int reqDelayReqMax; // maximum request delay requirement
  public final int numTrials;
  public final int linkDelayMin;
  public final int linkDelayMax;
  public final int threshold;
  public final int networkSize;
  public final double serverRatio;
  public final CostFunction costFunc;
  public final double nfvProb;
  public final boolean online;

  private Parameters(int[] nfvReqs, int[] nfvRates, int[] nfvOperationalCosts, int[] nfvInitCosts, int[] nfvInitDelays, int[] nfvProcessingDelays,
                     int[] networkSizes, int alpha, int beta, int linkBWCapMin, int linkBWCapMax, int numRequest, int L, int reqBWReqMin, int reqBWReqMax,
                     int reqDelayReqMin, int reqDelayReqMax, int numTrials, int linkDelayMin, int linkDelayMax, int threshold, int networkSize,
                     double serverRatio, CostFunction costFunc, double nfvProb, boolean online) {
    this.nfvInitDelays = nfvInitDelays;
    this.nfvProcessingDelays = nfvProcessingDelays;
    this.networkSizes = networkSizes;
    this.alpha = alpha;
    this.beta = beta;
    this.linkBWCapMin = linkBWCapMin;
    this.linkBWCapMax = linkBWCapMax;
    this.numRequest = numRequest;
    this.L = L;
    this.reqBWReqMin = reqBWReqMin;
    this.reqBWReqMax = reqBWReqMax;
    this.reqDelayReqMin = reqDelayReqMin;
    this.reqDelayReqMax = reqDelayReqMax;
    this.numTrials = numTrials;
    this.linkDelayMin = linkDelayMin;
    this.linkDelayMax = linkDelayMax;
    this.threshold = threshold;
    this.networkSize = networkSize;
    this.serverRatio = serverRatio;
    this.nfvReqs = nfvReqs;
    this.nfvRates = nfvRates;
    this.nfvOperationalCosts = nfvOperationalCosts;
    this.nfvInitCosts = nfvInitCosts;
    this.costFunc = costFunc;
    this.nfvProb = nfvProb;
    this.online = online;
  }

  @Override public String toString() {
    return "Parameters{" +
        "nfvReqs=" + Arrays.toString(nfvReqs) +
        ", nfvRates=" + Arrays.toString(nfvRates) +
        ", nfvOperationalCosts=" + Arrays.toString(nfvOperationalCosts) +
        ", nfvInitCosts=" + Arrays.toString(nfvInitCosts) +
        ", networkSizes=" + Arrays.toString(networkSizes) +
        ", alpha=" + alpha +
        ", beta=" + beta +
        ", linkBWCapMin=" + linkBWCapMin +
        ", linkBWCapMax=" + linkBWCapMax +
        ", numRequest=" + numRequest +
        ", L=" + L +
        ", reqBWReqMin=" + reqBWReqMin +
        ", reqBWReqMax=" + reqBWReqMax +
        ", reqDelayReqMin=" + reqDelayReqMin +
        ", reqDelayReqMax=" + reqDelayReqMax +
        ", numTrials=" + numTrials +
        ", linkDelayMin=" + linkDelayMin +
        ", linkDelayMax=" + linkDelayMax +
        ", threshold=" + threshold +
        ", networkSize=" + networkSize +
        ", serverRatio=" + serverRatio +
        ", costFunc=" + costFunc +
        ", nfvProb=" + nfvProb +
        ", online=" + online +
        '}';
  }

  public static class Builder {
    private int alpha = 2;
    private int beta = 2;
    private int linkBWCapMin = 1000;
    private int linkBWCapMax = 10000;
    private int linkDelayReqMin = 10;
    private int linkDelayReqMax = 100;
    private int numRequest = 500;
    private int L = 4;
    private int reqBWReqMin = 10;
    private int reqBWReqMax = 100;
    private int reqDelayMin = 500;
    private int reqDelayMax = 1000;
    private int numTrials = 3;
    private int threshold = 2;
    private double serverRatio = 0.2;
    private int networkSize = 50;

    private int[] nfvReqs = new int[] {2, 3, 5, 2, 6, 4};
    private int[] nfvRates = new int[] {3, 5, 6, 7, 8, 5};
    private int[] nfvOperationalCosts = new int[] {2, 3, 5, 2, 6, 4};
    private int[] nfvInitCosts = new int[] {5, 6, 7, 4, 8, 5};

    private int[] networkSizes = new int[] {50, 100, 200, 300, 400, 500, 600, 800, 1000};
    private CostFunction costFunc = new ExponentialCostFunction();
    private double nfvProb = 0.1;
    private boolean online = false;
    private int[] nfvInitDelays = new int[] {5, 6, 7, 4, 8, 5}; // TODO I just copy the code from nfvInitCosts
    private int[] nfvProcessingDelays = new int[] {3, 5, 6, 7, 8, 5}; // TODO I just copy the code from nfvRates

    Builder alpha(int alpha) {
      this.alpha = alpha;
      return this;
    }

    Builder beta(int beta) {
      this.beta = beta;
      return this;
    }

    public Builder linkBWCapMin(int linkBWCapMin) {
      this.linkBWCapMin = linkBWCapMin;
      return this;
    }

    public Builder linkBWCapMax(int linkBWCapMax) {
      this.linkBWCapMax = linkBWCapMax;
      return this;
    }

    public Builder linkDelayReqMin(int linkDelayReqMin) {
      this.linkDelayReqMin = linkDelayReqMin;
      return this;
    }

    public Builder linkDelayReqMax(int linkDelayReqMax) {
      this.linkDelayReqMax = linkDelayReqMax;
      return this;
    }

    public Builder numRequest(int numRequest) {
      this.numRequest = numRequest;
      return this;
    }

    public Builder L(int L) {
      this.L = L;
      return this;
    }

    public Builder reqBWReqMin(int reqBWReqMin) {
      this.reqBWReqMin = reqBWReqMin;
      return this;
    }

    public Builder reqBWReqMax(int reqBWReqMax) {
      this.reqBWReqMax = reqBWReqMax;
      return this;
    }

    public Builder reqDelayMin(int reqDelayMin) {
      this.reqDelayMin = reqDelayMin;
      return this;
    }

    public Builder reqDelayMax(int reqDelayMax) {
      this.reqDelayMax = reqDelayMax;
      return this;
    }

    public Builder numTrials(int numTrials) {
      this.numTrials = numTrials;
      return this;
    }

    Builder threshold(int threshold) {
      this.threshold = threshold;
      return this;
    }

    public Builder serverRatio(double serverRatio) {
      this.serverRatio = serverRatio;
      return this;
    }

    public Builder NFVreq(int[] NFVreq) {
      this.nfvReqs = NFVreq;
      return this;
    }

    public Builder NFVrate(int[] NFVrate) {
      this.nfvRates = NFVrate;
      return this;
    }

    public Builder NFVOpCost(int[] NFVOpCost) {
      this.nfvOperationalCosts = NFVOpCost;
      return this;
    }

    public Builder NFVInitCost(int[] NFVInitCost) {
      this.nfvInitCosts = NFVInitCost;
      return this;
    }

    public Builder networkSizes(int[] networkSizes) {
      this.networkSizes = networkSizes;
      return this;
    }

    public Builder costFunc(CostFunction costFunc) {
      this.costFunc = costFunc;
      return this;
    }

    public Builder nfvProb(double nfvProb) {
      this.nfvProb = nfvProb;
      return this;
    }

    public Builder online(boolean online) {
      this.online = online;
      return this;
    }

    public Builder nfvInitDelays(int[] nfvInitDelays) {
      this.nfvInitDelays = nfvInitDelays;
      return this;
    }

    public Builder nfvProcessingDelays(int[] nfvProcessingDelays) {
      this.nfvProcessingDelays = nfvProcessingDelays;
      return this;
    }

    public Parameters build() {
      return new Parameters(nfvReqs, nfvRates, nfvOperationalCosts, nfvInitCosts, nfvInitDelays, nfvProcessingDelays, networkSizes, alpha, beta, linkBWCapMin,
                            linkBWCapMax, numRequest, L, reqBWReqMin, reqBWReqMax, reqDelayMin, reqDelayMax, numTrials, linkDelayReqMin, linkDelayReqMax,
                            threshold, networkSize, serverRatio, costFunc, nfvProb, online);
    }
  }
}
