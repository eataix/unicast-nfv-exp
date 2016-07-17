package Algorithm.CostFunctions;

import Network.Link;
import Network.Server;
import Simulation.Parameters;

public class OperationalCostFunction implements CostFunction {

  @Override
  public double getCost(Link l, int b, Parameters parameters) {
    return l.getOperationalCost();
  }

  @Override
  public double getCost(Server s, int nfv, Parameters parameters) {
    return s.getOperationalCost(nfv);
  }
}
