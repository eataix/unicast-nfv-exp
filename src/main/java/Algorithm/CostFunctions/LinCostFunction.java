package Algorithm.CostFunctions;

import Network.Link;
import Network.Server;
import Simulation.Parameters;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;

/**
 * In a linear cost model, the cost of using a link is proportional to the bandwidth requirement, while the cost of using a server node is proportional to the
 * operational cost of nfv and possibly initialization cost
 */
public class LinCostFunction implements CostFunction {
  @Override
  public double getCost(Link link, double bandwidth, Parameters parameters) {
    checkArgument(bandwidth >= 0);
    return bandwidth;
  }

  @Override
  public double getCost(Server server, int nfv, Parameters parameters) {
    checkElementIndex(nfv, parameters.nfvOperationalCosts.length);
    double cost = parameters.nfvOperationalCosts[nfv];
    if (!server.canReuseVM(nfv)) {
      cost += parameters.nfvInitCosts[nfv];
    }
    return cost;
  }
}
