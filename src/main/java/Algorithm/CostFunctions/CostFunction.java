package Algorithm.CostFunctions;

import Network.Link;
import Network.Server;

public interface CostFunction {
	public double getCost(Link l, int b);
	public double getCost(Server s, int nfv);
}