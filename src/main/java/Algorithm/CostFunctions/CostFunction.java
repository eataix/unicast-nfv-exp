package Algorithm.CostFunctions;

import Network.Link;
import Network.Server;

public interface CostFunction {
  double getCost(Link l, int b);

  double getCost(Server s, int nfv);
}
