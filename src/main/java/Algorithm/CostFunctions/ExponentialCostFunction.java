package Algorithm.CostFunctions;

import Network.Link;
import Network.Server;
import Simulation.Parameters;

public class ExponentialCostFunction implements CostFunction {
  @Override
  public double getCost(Link l, int b, Parameters parameters) {
    return Math.pow(parameters.beta * parameters.networkSize, (l.getAllocatedBandwidth() + b) / l.getBandwidth()) - 1;
  }

  @Override
  public double getCost(Server s, int nfv, Parameters parameters) {
    return Math.pow(parameters.alpha * parameters.networkSize, (s.getCapacity() - s.remainingCapacity() - parameters.nfvReqs[nfv]) / s.getCapacity()) - 1;
  }
}
