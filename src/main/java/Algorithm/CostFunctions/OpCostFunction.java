package Algorithm.CostFunctions;

import Network.Link;
import Network.Server;

public class OpCostFunction implements CostFunction{

	@Override
	public double getCost(Link l, int b) {
		return l.getOpCost();
	}

	@Override
	public double getCost(Server s, int nfv) {
		return s.getOpCost(nfv);
	}
}
