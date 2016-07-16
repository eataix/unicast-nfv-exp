package Algorithm.CostFunctions;

import Network.Link;
import Network.Server;
import Simulation.Parameters;

public interface CostFunction {
  double getCost(Link l, int b, Parameters parameters);

  double getCost(Server s, int nfv, Parameters parameters);
}
