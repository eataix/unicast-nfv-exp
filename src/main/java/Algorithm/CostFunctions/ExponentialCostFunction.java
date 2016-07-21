package Algorithm.CostFunctions;

import Network.Link;
import Network.Server;
import Simulation.Parameters;

public class ExponentialCostFunction implements CostFunction {
  @Override
  public double getCost(Link link, double bandwidth, Parameters parameters) {
    return Math.pow(parameters.beta * (double) parameters.networkSize, (link.getAllocatedBandwidth() + bandwidth) / link.getBandwidthCapacity()) - 1d;
  }

  @Override
  public double getCost(Server server, int nfv, Parameters parameters) {
    return Math.pow(parameters.alpha * (double) parameters.networkSize,
                    (server.getComputingCapacity() - server.remainingCapacity() - parameters.nfvComputingReqs[nfv]) / server.getComputingCapacity()) - 1d;
  }
}
