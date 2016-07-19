package Algorithm.CostFunctions;

import Network.Link;
import Network.Server;
import Simulation.Parameters;

public class OperationalCostFunction implements CostFunction {

  @Override public double getCost(Link link, double bandwidth, Parameters parameters) {
    return link.getOperationalCost();
  }

  @Override
  public double getCost(Server server, int nfv, Parameters parameters) {
    return server.getOperationalCost(nfv);
  }
}
