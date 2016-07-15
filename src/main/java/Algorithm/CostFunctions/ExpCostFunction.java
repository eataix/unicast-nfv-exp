package Algorithm.CostFunctions;

import Network.Link;
import Network.Server;

public class ExpCostFunction implements CostFunction {
	@Override
	public double getCost(Link l, int b) {
		return l.getUtCost(b);
	}
	
	@Override
	public double getCost(Server s, int nfv){
		return s.getUtCost(nfv);
	}
}
 