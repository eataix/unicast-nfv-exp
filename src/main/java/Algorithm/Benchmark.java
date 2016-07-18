package Algorithm;

import Network.Network;
import Network.Request;
import Simulation.Parameters;

public class Benchmark {

	private final Network originalNetwork;
	private final Request request;
	private final Parameters parameters;

	public Benchmark(Network originalNetwork, Request request, Parameters parameters) {
		this.originalNetwork = originalNetwork;
		this.request = request;
		this.parameters = parameters;
	}

	public Result benchmarkNFVUnicast() {
		;
	}
	
	public Result benchmarkNFVUnicastDelay() {
		;
	}
	
	public Result benchmarkOnlineUnicast() {
		;
	}
	
	public Result benchmarkOnlineUnicastDelay(){
		;
	}
	
	public Request getRequest() {
		return request;
	}

	public Network getOriginalNetwork() {
		return originalNetwork;
	}

	public Parameters getParameters() {
		return parameters;
	}
}
