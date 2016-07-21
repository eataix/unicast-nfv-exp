package Algorithm.CostFunctions;

import Network.Link;
import Network.Server;
import Simulation.Parameters;

public class ExponentialCostFunction implements CostFunction {
  @Override
  public double getCost(Link link, double bandwidth, Parameters parameters) {
    return Math.pow(parameters.beta * parameters.networkSize, (link.getAllocatedBandwidth() + bandwidth) / link.getBandwidth()) - 1;
  }

  @Override
  public double getCost(Server server, int nfv, Parameters parameters) {
    return Math.pow(parameters.alpha * parameters.networkSize,
                    ((double) server.getCapacity() - (double) server.remainingCapacity() - (double) parameters.nfvComputingReq[nfv]) / (double) server.getCapacity())
        - 1;
  }
}
