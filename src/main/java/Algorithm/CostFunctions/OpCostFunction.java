package Algorithm.CostFunctions;

import Network.Link;
import Network.Server;
import Simulation.Parameters;

public class OpCostFunction implements CostFunction {

  @Override
  public double getCost(Link l, int b, Parameters parameters) {
    return l.getOpCost();
  }

  @Override
  public double getCost(Server s, int nfv, Parameters parameters) {
    return s.getOpCost(nfv);
  }
}
