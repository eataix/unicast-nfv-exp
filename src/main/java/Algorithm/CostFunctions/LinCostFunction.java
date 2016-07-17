package Algorithm.CostFunctions;

import Network.Link;
import Network.Server;
import Simulation.Parameters;

public class LinCostFunction implements CostFunction {
  @Override
  public double getCost(Link l, int b, Parameters parameters) {
    return (l.getBandwidth() - l.getResidualBandwidth() + b) / l.getBandwidth();
  }

  @Override
  public double getCost(Server s, int nfv, Parameters parameters) {
    if (s.canReuseVM(nfv)) {
      return (s.getCapacity() - s.remainingCapacity()) / s.getCapacity();
    } else {
      return (s.getCapacity() - s.remainingCapacity() - parameters.nfvReqs[nfv]) / s.getCapacity();
    }
  }
}
