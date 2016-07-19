package Algorithm.CostFunctions;

import Network.Link;
import Network.Server;
import Simulation.Parameters;

public class LinCostFunction implements CostFunction {
  @Override
  public double getCost(Link link, double bandwidth, Parameters parameters) {
    return (link.getBandwidth() - link.getResidualBandwidth() + bandwidth) / link.getBandwidth();
  }

  @Override
  public double getCost(Server server, int nfv, Parameters parameters) {
    if (server.canReuseVM(nfv)) {
      return ((double) server.getCapacity() - (double) server.remainingCapacity()) / (double) server.getCapacity();
    } else {
      return ((double) server.getCapacity() - (double) server.remainingCapacity() - (double) parameters.nfvReqs[nfv]) / (double) server.getCapacity();
    }
  }
}
