package Simulation;

import java.util.Arrays;

import Algorithm.CostFunctions.CostFunction;
import Algorithm.CostFunctions.ExponentialCostFunction;

@SuppressWarnings("WeakerAccess") public class Parameters {
  public final int networkSize;
  public final int[] networkSizes;
  public final boolean offline;
  public final int numTrials;

  public final double alpha;
  public final double beta; // Multiply of beta, i.e., the value of beta would be "beta * |V|"
  public final double threshold;
  public final CostFunction costFunc;

  public final double serverRatio; // Server to Node Ratio
  public final double linkBWCapMin; // minimum link bandwidth capacity
  public final double linkBWCapMax; // maximum link bandwidth capacity
  public final double linkDelayMin;
  public final double linkDelayMax;
  public final double linkCostMax;
  public final double linkCostMin;

  public final int numRequests;
  public final int L;
  public final double reqBWReqMin; // minimum request bandwidth requirement
  public final double reqBWReqMax; // maximum request bandwidth requirement
  public final double reqDelayReqMin; // minimum request delay requirement
  public final double reqDelayReqMax; // maximum request delay requirement

  public final double nfvProb;
  public final double[] nfvComputingReqs; //nfv vm resource requirements
  public final double[] nfvRates; //nfv vm service rate
  public final double[] nfvOperationalCosts; //operating cost of providing an vnf service
  public final double[] nfvInitCosts; //initialization cost of vnf service
  public final double[] nfvInitDelays; //initialization cost of vnf service
  public final double[] nfvProcessingDelays; //initialization cost of vnf service

  private Parameters(int networkSize, int[] networkSizes, boolean offline, int numTrials, double alpha, double beta, double threshold, CostFunction costFunc,
                     double serverRatio, double linkBWCapMin, double linkBWCapMax, double linkDelayMin, double linkDelayMax, double linkCostMax, double linkCostMin, int numRequests, int L,
                     double reqBWReqMin, double reqBWReqMax, double reqDelayReqMin, double reqDelayReqMax, double nfvProb, double[] nfvComputingReqs,
                     double[] nfvRates, double[] nfvOperationalCosts, double[] nfvInitCosts, double[] nfvInitDelays, double[] nfvProcessingDelays) {

    this.nfvInitDelays = nfvInitDelays;
    this.nfvProcessingDelays = nfvProcessingDelays;
    this.networkSize = networkSize;
    this.networkSizes = networkSizes;
    this.alpha = alpha;
    this.beta = beta;
    this.linkBWCapMin = linkBWCapMin;
    this.linkBWCapMax = linkBWCapMax;
    this.numRequests = numRequests;
    this.L = L;
    this.reqBWReqMin = reqBWReqMin;
    this.reqBWReqMax = reqBWReqMax;
    this.reqDelayReqMin = reqDelayReqMin;
    this.reqDelayReqMax = reqDelayReqMax;
    this.numTrials = numTrials;
    this.linkDelayMin = linkDelayMin;
    this.linkDelayMax = linkDelayMax;
    this.linkCostMax = linkCostMax;
    this.linkCostMin = linkCostMin;
    this.threshold = threshold;
    this.serverRatio = serverRatio;
    this.nfvComputingReqs = nfvComputingReqs;
    this.nfvRates = nfvRates;
    this.nfvOperationalCosts = nfvOperationalCosts;
    this.nfvInitCosts = nfvInitCosts;
    this.costFunc = costFunc;
    this.nfvProb = nfvProb;
    this.offline = offline;
  }

  @Override public String toString() {
    return "Parameters{" +
        "networkSize=" + networkSize +
        ", networkSizes=" + Arrays.toString(networkSizes) +
        ", offline=" + offline +
        ", numTrials=" + numTrials +
        ", alpha=" + alpha +
        ", beta=" + beta +
        ", threshold=" + threshold +
        ", costFunc=" + costFunc +
        ", serverRatio=" + serverRatio +
        ", linkBWCapMin=" + linkBWCapMin +
        ", linkBWCapMax=" + linkBWCapMax +
        ", linkDelayMin=" + linkDelayMin +
        ", linkDelayMax=" + linkDelayMax +
        ", numRequests=" + numRequests +
        ", L=" + L +
        ", reqBWReqMin=" + reqBWReqMin +
        ", reqBWReqMax=" + reqBWReqMax +
        ", reqDelayReqMin=" + reqDelayReqMin +
        ", reqDelayReqMax=" + reqDelayReqMax +
        ", nfvProb=" + nfvProb +
        ", nfvComputingReqs=" + Arrays.toString(nfvComputingReqs) +
        ", nfvRates=" + Arrays.toString(nfvRates) +
        ", nfvOperationalCosts=" + Arrays.toString(nfvOperationalCosts) +
        ", nfvInitCosts=" + Arrays.toString(nfvInitCosts) +
        ", nfvInitDelays=" + Arrays.toString(nfvInitDelays) +
        ", nfvProcessingDelays=" + Arrays.toString(nfvProcessingDelays) +
        '}';
  }

  @SuppressWarnings("unused") public static class Builder {
    /**
     * Experiment parameters
     */
    private int networkSize = 50;
    private int[] networkSizes = new int[] {50, 100, 150, 200, 250};
    //private int[] networkSizes = new int[] {50, 100, 150, 200, 300, 400, 500, 600, 800, 1000};
    private boolean offline = true;
    private int numTrials = 2;

    /**
     * Algorithm related parameters
     */
    private double alpha = 2d; // * |V|, this should have no impact on the algorithm
    private double beta = 2d; // Multiply of beta, i.e., the value of beta would be "beta * |V|"
    private double threshold = 1d; // threshold = "threshold" * |V| - 1
    private CostFunction costFunc = new ExponentialCostFunction();

    /**
     * Network related parameters
     */

    // The following three settings are adopted from Zichuan's multicast paper
    private double serverRatio = 0.1; // Server to Node Ratio
    private double linkBWCapMin = 1000; // 1,000 Mbps, minimum link bandwidth capacity
    private double linkBWCapMax = 10000; // 10,000 Mbps maximum link bandwidth capacity

    // The following two settings are from our paper on consolidated middleboxes
    private double linkDelayReqMin = 2; // minimum link delay
    private double linkDelayReqMax = 5; // maximum link delay
    
    private double linkCostMax = 0.001;
    private double linkCostMin = 0.005;

    /**
     * Request related parameters
     */
    private int numRequests = 500; // number of requests
    private int L = 4; // the maximum service chain length

    // These following four are from our paper on consolidated middleboxes
    private double reqBWReqMin = 10; // minimum request bandwidth requirement
    private double reqBWReqMax = 120; // maximum request bandwidth requirement
    private double reqDelayMin = 40; // minimum request delay requirement
    private double reqDelayMax = 200; // maximum request delay requirement

    /**
     * NFV parameters
     */
    private double nfvProb = 0.1;   //probability of any given nfv instance already deployed on a given server. Guarantees each nfv is on at least one server.

    // TODO find values for the following four
    // Firewall, Proxy, NAT, IDS, LB, Gateway
    private double[] nfvOperationalCosts = new double[] {3, 1, 1, 5, 2, 4};//operating cost of providing an vnf service
    private double[] nfvInitCosts = new double[] {30, 10, 10, 50, 20, 40}; //initialization cost of vnf service
    private double[] nfvInitDelays = new double[] {60, 60, 60, 90, 60, 40}; //initialization cost of vnf service
    private double[] nfvProcessingDelays = new double[] {6, 6, 6, 9, 6, 4,}; //initialization cost of vnf service

    // The following two are insignificant
    // Computing resource requirement is irrelevant
    private double[] nfvComputingReqs = new double[] {2, 3, 5, 2, 6, 4}; //nfv vm resource requirements
    // I don't think rate is considered in this paper
    private double[] nfvRates = new double[] {3, 5, 6, 7, 8, 5};  //nfv vm service rate

    Builder alpha(double alpha) {
      this.alpha = alpha;
      return this;
    }

    Builder beta(double beta) {
      this.beta = beta;
      return this;
    }

    public Builder linkBWCapMin(double linkBWCapMin) {
      this.linkBWCapMin = linkBWCapMin;
      return this;
    }

    public Builder linkBWCapMax(double linkBWCapMax) {
      this.linkBWCapMax = linkBWCapMax;
      return this;
    }

    public Builder linkDelayReqMin(double linkDelayReqMin) {
      this.linkDelayReqMin = linkDelayReqMin;
      return this;
    }

    public Builder linkDelayReqMax(double linkDelayReqMax) {
      this.linkDelayReqMax = linkDelayReqMax;
      return this;
    }

    public Builder numRequests(int numRequests) {
      this.numRequests = numRequests;
      return this;
    }

    public Builder L(int L) {
      this.L = L;
      return this;
    }

    public Builder reqBWReqMin(double reqBWReqMin) {
      this.reqBWReqMin = reqBWReqMin;
      return this;
    }

    public Builder reqBWReqMax(double reqBWReqMax) {
      this.reqBWReqMax = reqBWReqMax;
      return this;
    }

    public Builder reqDelayMin(double reqDelayMin) {
      this.reqDelayMin = reqDelayMin;
      return this;
    }

    public Builder reqDelayMax(double reqDelayMax) {
      this.reqDelayMax = reqDelayMax;
      return this;
    }

    public Builder numTrials(int numTrials) {
      this.numTrials = numTrials;
      return this;
    }

    Builder threshold(double threshold) {
      this.threshold = threshold;
      return this;
    }

    public Builder serverRatio(double serverRatio) {
      this.serverRatio = serverRatio;
      return this;
    }

    public Builder nfvComputingReqs(double[] nfvComputingReqs) {
      this.nfvComputingReqs = nfvComputingReqs;
      return this;
    }

    public Builder nfvRates(double[] nfvRates) {
      this.nfvRates = nfvRates;
      return this;
    }

    public Builder nfvOperationalCosts(double[] nfvOperationalCosts) {
      this.nfvOperationalCosts = nfvOperationalCosts;
      return this;
    }

    public Builder nfvInitCosts(double[] nfvInitCosts) {
      this.nfvInitCosts = nfvInitCosts;
      return this;
    }

    Builder networkSize(int networkSize) {
      this.networkSize = networkSize;
      return this;
    }

    public Builder networkSizes(int[] networkSizes) {
      this.networkSizes = networkSizes;
      return this;
    }

    Builder costFunc(CostFunction costFunc) {
      this.costFunc = costFunc;
      return this;
    }

    public Builder nfvProb(double nfvProb) {
      this.nfvProb = nfvProb;
      return this;
    }

    public Builder offline(boolean offline) {
      this.offline = offline;
      return this;
    }

    public Builder nfvInitDelays(double[] nfvInitDelays) {
      this.nfvInitDelays = nfvInitDelays;
      return this;
    }

    public Builder nfvProcessingDelays(double[] nfvProcessingDelays) {
      this.nfvProcessingDelays = nfvProcessingDelays;
      return this;
    }

    public Parameters build() {
      return new Parameters(networkSize, networkSizes, offline, numTrials, alpha, beta, threshold, costFunc, serverRatio, linkBWCapMin, linkBWCapMax,
                            linkDelayReqMin, linkDelayReqMax, linkCostMax, linkCostMin, numRequests, L, reqBWReqMin, reqBWReqMax, reqDelayMin, reqDelayMax, nfvProb, nfvComputingReqs,
                            nfvRates, nfvOperationalCosts, nfvInitCosts, nfvInitDelays, nfvProcessingDelays
      );
    }
  }
}
