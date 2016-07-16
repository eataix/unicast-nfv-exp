package Simulation;

import Algorithm.Algorithm;
import Algorithm.CostFunctions.ExpCostFunction;
import Algorithm.CostFunctions.LinCostFunction;
import Algorithm.Result;
import Network.Network;
import Network.Request;
import NetworkGenerator.NetworkGenerator;
import NetworkGenerator.NetworkValueSetter;

public class Simulation {
  public static final int networkSize = 100;
  private static final int packets = 500; //number of requests to run through the network
  private static final int trials = 10;

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

    for (int j = 0; j < trials; j++) {
      System.out.print((j + 1) + "... ");
      Request[] requests = new Request[packets];
      for (int i = 0; i < packets; i++) {
        int bandwidth = (int) (Math.random() * 400) + 200;
        requests[i] = new Request(bandwidth, network.getRandomServer(), network.getRandomServer());
      }
      network.wipeLinks();
      Parameters.threshold = 1;
      for (int i = 0; i < packets; i++) {
        Algorithm alg = new Algorithm(network, requests[i]);
        results[i] = alg.maxThroughputWithoutDelay(new ExpCostFunction());
        if (results[i].admit) {
          accepted++;
        }
      }
      expSum += accepted;
      accepted = 0;
      network.wipeLinks();
      Parameters.threshold = 100000;
      for (int i = 0; i < packets; i++) {
        Algorithm alg = new Algorithm(network, requests[i]);
        results[i] = alg.maxThroughputWithoutDelay(new LinCostFunction());
        if (results[i].admit) {
          accepted++;
        }
      }
      linSum += accepted;
    }
    System.out.println("\n" + expSum);
    System.out.println(linSum);

		/* Sanity check: bandwidth in original network links are being updated.
    for(Link l : network.getLinks()){
			if(l.getBandwidth()!=l.getResidualBandwidth()){
				System.out.println("Link resource usage in original network: "+(l.getBandwidth()-l.getResidualBandwidth())+"/"+l.getBandwidth());
			}
		}*/
  }

  /**
   * We compare the performance of the proposed online algorithms while using different cost functions, i.e., a linear cost function and an exponential cost
   * function
   */
  public static void CompareCostFns() {

  }

  /**
   * We test the impact of beta on the performance of the proposed online algorithms by varying the value of beta from 2 to 6
   */
  public static void BetaEffect() {
    for (int beta = 2; beta <= 6; beta += 2) {

    }
  }

  /**
   * We test the impact of threshold on the performance of the proposed online algorithms by running the algorithms with and without the treshold
   */
  public static void ThresholdEffect() {
    for (int beta = 2; beta <= 6; beta += 2) {
    }
  }
}
