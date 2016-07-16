package Simulation;

import java.util.Random;

import Algorithm.Algorithm;
import Algorithm.CostFunctions.ExpCostFunction;
import Algorithm.CostFunctions.LinCostFunction;
import Algorithm.Result;
import Network.Network;
import Network.Request;
import NetworkGenerator.NetworkGenerator;
import NetworkGenerator.NetworkValueSetter;

@SuppressWarnings("Duplicates") public class Simulation {
  public static final int networkSize = 100;
  private static final int packets = 500; //number of requests to run through the network
  private static final int trials = 10;
  private static final Random random = new Random();

  public static void main(String[] args) {
    Network network = new NetworkGenerator().barabasiAlbertNetwork(networkSize, 1);
    NetworkValueSetter nvs = new NetworkValueSetter(network);
    nvs.setConstantServerCapacity(10000, 0.2); //make it excessively large
    nvs.setRandomLinkCapacity(1000, 10000);
    nvs.placeNFVs(0.25);
  }

  /**
   * We compare the performance of the proposed online algorithms while using different cost functions, i.e., a linear cost function and an exponential cost
   * function
   */
  public static void CompareCostFns() {
    Parameters parametersWithThreshold = new Parameters.Builder().build();
    Parameters parametersWithOutThreshold = new Parameters.Builder().threshold(Integer.MAX_VALUE).build();

    int accepted = 0; //number of accepted requests
    int expSum = 0; //sum of all exponential cost accepted requests
    int linSum = 0; //sum of all linear cost accepted requests
    Result[] results = new Result[parametersWithThreshold.numRequest];
    int[] networkSizes = {50, 100};

    for (int networkSize : networkSizes) {
      for (int j = 0; j < parametersWithThreshold.numTrials; j++) {
        Network network = new NetworkGenerator().barabasiAlbertNetwork(networkSize, 1);
        Request[] requests = new Request[parametersWithThreshold.numRequest];
        for (int i = 0; i < parametersWithThreshold.numRequest; i++) {
          int bandwidth =
              random.nextInt((parametersWithThreshold.linkBWCapMax - parametersWithThreshold.linkBWCapMin) + 1) + parametersWithThreshold.linkBWCapMin;
          requests[i] = new Request(bandwidth, network.getRandomServer(), network.getRandomServer());
        }
        network.wipeLinks();
        for (int i = 0; i < parametersWithThreshold.numRequest; i++) {
          Algorithm alg = new Algorithm(network, requests[i]);
          results[i] = alg.maxThroughputWithoutDelay(new ExpCostFunction());
          if (results[i].admit) {
            accepted++;
          }
        }
        expSum += accepted;
        accepted = 0;
        network.wipeLinks();
        for (int i = 0; i < parametersWithThreshold.numRequest; i++) {
          Algorithm alg = new Algorithm(network, requests[i]);
          results[i] = alg.maxThroughputWithoutDelay(new LinCostFunction());
          if (results[i].admit) {
            accepted++;
          }
        }
        linSum += accepted;
      }
    }
    System.out.println("\n" + expSum);
    System.out.println(linSum);
  }

  public static void BetaEffect() {
    for (int beta = 2; beta <= 6; beta += 2) {
      Parameters parameters = new Parameters.Builder().beta(beta).build();

      int accepted = 0; //number of accepted requests
      int expSum = 0; //sum of all exponential cost accepted requests
      int linSum = 0; //sum of all linear cost accepted requests
      Result[] results = new Result[parameters.numRequest];
      int[] networkSizes = {50, 100};

      for (int networkSize : networkSizes) {
        for (int j = 0; j < parameters.numTrials; j++) {
          Network network = new NetworkGenerator().barabasiAlbertNetwork(networkSize, 1);
          Request[] requests = new Request[packets];
          for (int i = 0; i < parameters.numRequest; i++) {
            int bandwidth = random.nextInt((parameters.linkBWCapMax - parameters.linkBWCapMin) + 1) + parameters.linkBWCapMin;
            requests[i] = new Request(bandwidth, network.getRandomServer(), network.getRandomServer());
          }
          network.wipeLinks();
          for (int i = 0; i < parameters.numRequest; i++) {
            Algorithm alg = new Algorithm(network, requests[i]);
            results[i] = alg.maxThroughputWithoutDelay(new ExpCostFunction());
            if (results[i].admit) {
              accepted++;
            }
          }
          expSum += accepted;
          accepted = 0;
          network.wipeLinks();
          for (int i = 0; i < parameters.numRequest; i++) {
            Algorithm alg = new Algorithm(network, requests[i]);
            results[i] = alg.maxThroughputWithoutDelay(new LinCostFunction());
            if (results[i].admit) {
              accepted++;
            }
          }
          linSum += accepted;
        }
      }
      System.out.println("\n" + expSum);
      System.out.println(linSum);
    }
  }

  /**
   * We test the impact of threshold on the performance of the proposed online algorithms by running the algorithms with and without the treshold
   */
  public static void ThresholdEffect() {
    Parameters parametersWithThreshold = new Parameters.Builder().build();
    Parameters parametersWithOutThreshold = new Parameters.Builder().threshold(Integer.MAX_VALUE).build();

    int accepted = 0; //number of accepted requests
    int expSum = 0; //sum of all exponential cost accepted requests
    int linSum = 0; //sum of all linear cost accepted requests
    Result[] results = new Result[parametersWithThreshold.numRequest];
    int[] networkSizes = {50, 100};

    for (int networkSize : networkSizes) {
      for (int j = 0; j < parametersWithThreshold.numTrials; j++) {
        Network network = new NetworkGenerator().barabasiAlbertNetwork(networkSize, 1);
        Request[] requests = new Request[parametersWithThreshold.numRequest];
        for (int i = 0; i < parametersWithThreshold.numRequest; i++) {
          int bandwidth =
              random.nextInt((parametersWithThreshold.linkBWCapMax - parametersWithThreshold.linkBWCapMin) + 1) + parametersWithThreshold.linkBWCapMin;
          requests[i] = new Request(bandwidth, network.getRandomServer(), network.getRandomServer());
        }
        network.wipeLinks();
        for (int i = 0; i < parametersWithThreshold.numRequest; i++) {
          Algorithm alg = new Algorithm(network, requests[i]);
          results[i] = alg.maxThroughputWithoutDelay(new ExpCostFunction());
          if (results[i].admit) {
            accepted++;
          }
        }
        expSum += accepted;
        accepted = 0;
        network.wipeLinks();
        for (int i = 0; i < parametersWithThreshold.numRequest; i++) {
          Algorithm alg = new Algorithm(network, requests[i]);
          results[i] = alg.maxThroughputWithoutDelay(new LinCostFunction());
          if (results[i].admit) {
            accepted++;
          }
        }
        linSum += accepted;
      }
    }
    System.out.println("\n" + expSum);
    System.out.println(linSum);
  }
}
