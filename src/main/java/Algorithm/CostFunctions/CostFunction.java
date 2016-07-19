package Algorithm.CostFunctions;

import Network.Link;
import Network.Server;
import Simulation.Parameters;

public interface CostFunction {
  double getCost(Link link, double bandwidth, Parameters parameters);

  double getCost(Server server, int nfv, Parameters parameters);
}
