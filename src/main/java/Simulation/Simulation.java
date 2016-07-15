package Simulation;
import NetworkGenerator.*;
import Network.*;
import Algorithm.Algorithm;
import Algorithm.Result;
import Algorithm.CostFunctions.ExpCostFunction;
import Algorithm.CostFunctions.LinCostFunction;

public class Simulation {
	public static int networkSize = 100;
	public static int packets = 500; //number of requests to run through the network
	public static int trials = 10;

	public static void main(String[] args) {
		Parameters.importParameters("sim1.txt");
		Network network = new NetworkGenerator().barabasiAlbertNetwork(networkSize, 1);
		NetworkValueSetter nvs = new NetworkValueSetter(network);
		nvs.setConstantServerCapacity(10000, Parameters.serverRatio); //make it excessively large
		nvs.setRandomLinkCapacity(1000, 10000);
		nvs.placeNFVs(0.25);
		
		int accepted = 0; //number of accepted requests
		int expSum = 0; //sum of all exponential cost accepted requests
		int linSum = 0; //sum of all linear cost accepted requests
		Result[] results = new Result[packets];
		
		for(int j = 0; j < trials; j++){
			System.out.print((j+1)+"... ");
			Request[] requests = new Request[packets];
			for(int i = 0; i < packets; i++){
				int bandwidth = (int) Math.random()*400 + 200;
				requests[i] = new Request(bandwidth, network.getRandomServer(), network.getRandomServer());
			}
			network.wipeLinks();
			Parameters.threshold = 1;
			for(int i = 0; i < packets; i++){
				Algorithm alg = new Algorithm(network, requests[i]);
				results[i] = alg.maxThroughputWithoutDelay(new ExpCostFunction());
				if(results[i].admit){
					accepted++;
				}
			}
			expSum+= accepted; 
			accepted = 0;
			network.wipeLinks();
			Parameters.threshold = 100000;
			for(int i = 0; i < packets; i++){
				Algorithm alg = new Algorithm(network, requests[i]);
				results[i] = alg.maxThroughputWithoutDelay(new LinCostFunction());
				if(results[i].admit){
					accepted++;
				}
			}
			linSum += accepted;
		}
		System.out.println("\n"+expSum);
		System.out.println(linSum);
		
		/* Sanity check: bandwidth in original network links are being updated. 
		for(Link l : network.getLinks()){
			if(l.getBandwidth()!=l.getResidualBandwidth()){
				System.out.println("Link resource usage in original network: "+(l.getBandwidth()-l.getResidualBandwidth())+"/"+l.getBandwidth());
			}
		}*/
	}
}
