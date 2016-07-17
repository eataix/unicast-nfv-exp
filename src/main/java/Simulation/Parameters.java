package Simulation;

import Algorithm.CostFunctions.CostFunction;
import Algorithm.CostFunctions.ExponentialCostFunction;

public class Parameters {
  public final int[] nfvReqs; //nfv vm resource requirements
  public final int[] nfvRates; //nfv vm service rate
  public final int[] nfvOperationalCosts; //operating cost of providing an vnf service
  public final int[] nfvInitCosts; //initialization cost of vnf service

  public final int[] networkSizes; //initialization cost of vnf service

  public final int alpha;
  public final int beta;
  public final int linkBWCapMin;
  public final int linkBWCapMax;
  public final int numRequest;
  public final int L;
  public final int reqNetworkReqMin;
  public final int reqNetworkReqMax;
  public final int reqDelayReqMin;
  public final int reqDelayReqMax;
  public final int numTrials;
  public final int linkDelayReqMin;
  public final int linkDelayReqMax;
  public final int threshold;
  public final int networkSize;
  public final double serverRatio;
  public final CostFunction costFunc;
  public final double nfvProb;
  public final boolean online;

  Parameters(int[] nfvReqs, int[] nfvRates, int[] nfvOperationalCosts, int[] nfvInitCosts, int[] networkSizes, int alpha, int beta, int linkBWCapMin,
             int linkBWCapMax, int numRequest, int L, int reqNetworkReqMin, int reqNetworkReqMax, int reqDelayReqMin, int reqDelayReqMax, int numTrials,
             int linkDelayReqMin, int linkDelayReqMax, int threshold, int networkSize, double serverRatio, CostFunction costFunc, double nfvProb,
             boolean online) {
    this.networkSizes = networkSizes;
    this.alpha = alpha;
    this.beta = beta;
    this.linkBWCapMin = linkBWCapMin;
    this.linkBWCapMax = linkBWCapMax;
    this.numRequest = numRequest;
    this.L = L;
    this.reqNetworkReqMin = reqNetworkReqMin;
    this.reqNetworkReqMax = reqNetworkReqMax;
    this.reqDelayReqMin = reqDelayReqMin;
    this.reqDelayReqMax = reqDelayReqMax;
    this.numTrials = numTrials;
    this.linkDelayReqMin = linkDelayReqMin;
    this.linkDelayReqMax = linkDelayReqMax;
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

  public static class Builder {
    private int alpha = 2;
    private int beta = 2;
    private int linkBWCapMin = 1000;
    private int linkBWCapMax = 10000;
    private int linkDelayReqMin = 10;
    private int linkDelayReqMax = 100;
    private int numRequest = 500;
    private int L = 4;
    private int reqNetworkReqMin = 10;
    private int reqNetworkReqMax = 100;
    private int reqDelayReqMin = 10;
    private int reqDelayReqMax = 100;
    private int numTrials = 10;
    private int threshold;
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

    Builder alpha(int alpha) {
      this.beta = beta;
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

    public Builder reqNetworkReqMin(int reqNetworkReqMin) {
      this.reqNetworkReqMin = reqNetworkReqMin;
      return this;
    }

    public Builder reqNetworkReqMax(int reqNetworkReqMax) {
      this.reqNetworkReqMax = reqNetworkReqMax;
      return this;
    }

    public Builder reqDelayReqMin(int reqDelayReqMin) {
      this.reqDelayReqMin = reqDelayReqMin;
      return this;
    }

    public Builder reqDelayReqMax(int reqDelayReqMax) {
      this.reqDelayReqMax = reqDelayReqMax;
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

    public Parameters build() {
      return new Parameters(nfvReqs, nfvRates, nfvOperationalCosts, nfvInitCosts, networkSizes, alpha, beta, linkBWCapMin, linkBWCapMax, numRequest, L,
                            reqNetworkReqMin,
                            reqNetworkReqMax,
                            reqDelayReqMin, reqDelayReqMax, numTrials, linkDelayReqMin, linkDelayReqMax, threshold, networkSize, serverRatio, costFunc,
                            nfvProb, online);
    }
  }
}
