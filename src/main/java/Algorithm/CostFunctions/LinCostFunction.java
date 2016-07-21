package Algorithm.CostFunctions;

import Network.Link;
import Network.Server;
import Simulation.Parameters;

public class LinCostFunction implements CostFunction {
  @Override
  public double getCost(Link link, double bandwidth, Parameters parameters) {
    return (link.getBandwidthCapacity() - link.getResidualBandwidth() + bandwidth) / link.getBandwidthCapacity();
  }

  @Override
  public double getCost(Server server, int nfv, Parameters parameters) {
    if (server.canReuseVM(nfv)) {
      return (server.getComputingCapacity() - server.remainingCapacity()) / server.getComputingCapacity();
    } else {
      return (server.getComputingCapacity() - server.remainingCapacity() - parameters.nfvComputingReqs[nfv]) / server.getComputingCapacity();
    }
  }
}
