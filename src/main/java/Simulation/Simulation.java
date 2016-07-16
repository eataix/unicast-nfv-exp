package Simulation;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import Algorithm.Algorithm;
import Algorithm.CostFunctions.ExpCostFunction;
import Algorithm.CostFunctions.LinCostFunction;
import Algorithm.Result;
import Network.Network;
import Network.Request;
import NetworkGenerator.NetworkGenerator;

@SuppressWarnings("Duplicates") public class Simulation {
  private static final int packets = 500; //number of requests to run through the network
  private static final int trials = 10;
  private static final Random random = new Random();
  private static final ExecutorService threadPool = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());

  public static void main(String[] args) {
    //Network network = new NetworkGenerator().barabasiAlbertNetwork(networkSize, 1);
    //NetworkValueSetter nvs = new NetworkValueSetter(network, parameters);
    //nvs.setConstantServerCapacity(10000, 0.2); //make it excessively large
    //nvs.setRandomLinkCapacity(1000, 10000);
    //nvs.placeNFVs(0.25);

    ArrayList<Runnable> listOfTasks = new ArrayList<>();
    listOfTasks.add(new Thread(() -> CompareCostFns()));
    listOfTasks.add(new Thread(() -> BetaEffect()));
    listOfTasks.add(new Thread(() -> ThresholdEffect()));
    listOfTasks.add(new Thread(() -> LEffect()));
    listOfTasks.forEach(threadPool::execute);

    threadPool.shutdown();
    try {
      threadPool.awaitTermination(1, TimeUnit.DAYS);
    } catch (InterruptedException ie) {
      ie.printStackTrace();
    }
  }

  /**
   * We compare the performance of the proposed online algorithms while using different cost functions, i.e., a linear cost function and an exponential cost
   * function
   */
  public static void CompareCostFns() {
    Parameters parametersWithExpCostFn = new Parameters.Builder().costFunc(new ExpCostFunction()).build();
    Parameters parametersWithLinearCostFn = new Parameters.Builder().costFunc(new LinCostFunction()).build();

    int accepted = 0; //number of accepted requests
    int expSum = 0; //sum of all exponential cost accepted requests
    int linSum = 0; //sum of all linear cost accepted requests
    Result[] results = new Result[parametersWithExpCostFn.numRequest];

    for (int networkSize : parametersWithExpCostFn.networkSizes) {
      for (int j = 0; j < parametersWithExpCostFn.numTrials; j++) {
        Network network = new NetworkGenerator().barabasiAlbertNetwork(networkSize, 1, parametersWithExpCostFn); // TODO
        Request[] requests = new Request[parametersWithExpCostFn.numRequest];
        for (int i = 0; i < parametersWithExpCostFn.numRequest; i++) {
          int bandwidth =
              random.nextInt((parametersWithExpCostFn.linkBWCapMax - parametersWithExpCostFn.linkBWCapMin) + 1) + parametersWithExpCostFn.linkBWCapMin;
          requests[i] = new Request(bandwidth, network.getRandomServer(), network.getRandomServer(), parametersWithExpCostFn);
        }
        network.wipeLinks();
        for (int i = 0; i < parametersWithExpCostFn.numRequest; i++) {
          Algorithm alg = new Algorithm(network, requests[i], parametersWithExpCostFn);
          results[i] = alg.maxThroughputWithoutDelay();
          if (results[i].admit) {
            accepted++;
          }
        }
        expSum += accepted;
        accepted = 0;
        network.wipeLinks();
        for (int i = 0; i < parametersWithExpCostFn.numRequest; i++) {
          Algorithm alg = new Algorithm(network, requests[i], parametersWithLinearCostFn);
          results[i] = alg.maxThroughputWithoutDelay();
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
      Result[] results = new Result[parameters.numRequest];

      for (int networkSize : parameters.networkSizes) {
        for (int j = 0; j < parameters.numTrials; j++) {
          Network network = new NetworkGenerator().barabasiAlbertNetwork(networkSize, 1, parameters); // TODO
          Request[] requests = new Request[parameters.numRequest];
          for (int i = 0; i < parameters.numRequest; i++) {
            int bandwidth = random.nextInt((parameters.linkBWCapMax - parameters.linkBWCapMin) + 1) + parameters.linkBWCapMin;
            requests[i] = new Request(bandwidth, network.getRandomServer(), network.getRandomServer(), parameters);
          }
          network.wipeLinks();
          for (int i = 0; i < parameters.numRequest; i++) {
            Algorithm alg = new Algorithm(network, requests[i], parameters);
            results[i] = alg.maxThroughputWithoutDelay();
            if (results[i].admit) {
              accepted++;
            }
          }
          expSum += accepted;
        }
      }
      System.out.println("\n" + expSum);
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

    for (int networkSize : parametersWithOutThreshold.networkSizes) {
      for (int j = 0; j < parametersWithThreshold.numTrials; j++) {
        Network network = new NetworkGenerator().barabasiAlbertNetwork(networkSize, 1, parametersWithOutThreshold); // TODO
        Request[] requests = new Request[parametersWithThreshold.numRequest];
        for (int i = 0; i < parametersWithThreshold.numRequest; i++) {
          int bandwidth =
              random.nextInt((parametersWithThreshold.linkBWCapMax - parametersWithThreshold.linkBWCapMin) + 1) + parametersWithThreshold.linkBWCapMin;
          requests[i] = new Request(bandwidth, network.getRandomServer(), network.getRandomServer(), parametersWithThreshold);
        }
        network.wipeLinks();
        for (int i = 0; i < parametersWithThreshold.numRequest; i++) {
          Algorithm alg = new Algorithm(network, requests[i], parametersWithThreshold);
          results[i] = alg.maxThroughputWithoutDelay();
          if (results[i].admit) {
            accepted++;
          }
        }
        expSum += accepted;
        accepted = 0;
        network.wipeLinks();
        for (int i = 0; i < parametersWithThreshold.numRequest; i++) {
          Algorithm alg = new Algorithm(network, requests[i], parametersWithOutThreshold);
          results[i] = alg.maxThroughputWithoutDelay();
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
  
  public static void CompareCostFnsRealTopology(String networkName) {
	  Parameters parametersWithExpCostFn = new Parameters.Builder().costFunc(new ExpCostFunction()).build();
	  Parameters parametersWithLinearCostFn = new Parameters.Builder().costFunc(new LinCostFunction()).build();

	  int accepted = 0; // number of accepted requests
	  int expSum = 0; // sum of all exponential cost accepted requests
	  int linSum = 0; // sum of all linear cost accepted requests
	  Result[] results = new Result[parametersWithExpCostFn.numRequest];

	  for (int j = 0; j < parametersWithExpCostFn.numTrials; j++) {
		Network network = new NetworkGenerator().generateRealNetworks(-1, networkName, parametersWithExpCostFn);
		Request[] requests = new Request[parametersWithExpCostFn.numRequest];
		for (int i = 0; i < parametersWithExpCostFn.numRequest; i++) {
			int bandwidth = random
				.nextInt((parametersWithExpCostFn.linkBWCapMax - parametersWithExpCostFn.linkBWCapMin) + 1)
				+ parametersWithExpCostFn.linkBWCapMin;
			requests[i] = new Request(bandwidth, network.getRandomServer(), network.getRandomServer(),
						parametersWithExpCostFn);
		}
		network.wipeLinks();
		for (int i = 0; i < parametersWithExpCostFn.numRequest; i++) {
			Algorithm alg = new Algorithm(network, requests[i], parametersWithExpCostFn);
			results[i] = alg.maxThroughputWithoutDelay();
			if (results[i].admit) {
				accepted++;
			}
		}
		expSum += accepted;
		accepted = 0;
		network.wipeLinks();
		for (int i = 0; i < parametersWithExpCostFn.numRequest; i++) {
			Algorithm alg = new Algorithm(network, requests[i], parametersWithLinearCostFn);
			results[i] = alg.maxThroughputWithoutDelay();
			if (results[i].admit) {
				accepted++;
			}
		}
		linSum += accepted;
	}
	System.out.println("\n" + expSum);
	System.out.println(linSum);
  }

  public static void BetaEffectRealTopology(String networkName) {
	  for (int beta = 2; beta <= 6; beta += 2) {
		  Parameters parameters = new Parameters.Builder().beta(beta).build();

	      int accepted = 0; //number of accepted requests
	      int expSum = 0; //sum of all exponential cost accepted requests
	      Result[] results = new Result[parameters.numRequest];

	      for (int j = 0; j < parameters.numTrials; j++) {
	        Network network = new NetworkGenerator().generateRealNetworks(-1, networkName, parameters); // TODO
	        Request[] requests = new Request[parameters.numRequest];
	        for (int i = 0; i < parameters.numRequest; i++) {
	          int bandwidth = random.nextInt((parameters.linkBWCapMax - parameters.linkBWCapMin) + 1) + parameters.linkBWCapMin;
	          requests[i] = new Request(bandwidth, network.getRandomServer(), network.getRandomServer(), parameters);
	        }
	        network.wipeLinks();
	        for (int i = 0; i < parameters.numRequest; i++) {
	          Algorithm alg = new Algorithm(network, requests[i], parameters);
	          results[i] = alg.maxThroughputWithoutDelay();
	          if (results[i].admit) {
	            accepted++;
	          }
	        }
	        expSum += accepted;
	      }
	      System.out.println("\n" + expSum);
	  }
  }

  /**
  * We test the impact of threshold on the performance of the proposed online algorithms by running the algorithms with and without the treshold
  */
  public static void ThresholdEffectRealToplolgy(String networkName) {
	  Parameters parametersWithThreshold = new Parameters.Builder().build();
	  Parameters parametersWithOutThreshold = new Parameters.Builder().threshold(Integer.MAX_VALUE).build();

	  int accepted = 0; //number of accepted requests
	  int expSum = 0; //sum of all exponential cost accepted requests
	  int linSum = 0; //sum of all linear cost accepted requests
	  Result[] results = new Result[parametersWithThreshold.numRequest];

	  for (int j = 0; j < parametersWithThreshold.numTrials; j++) {
	    Network network = new NetworkGenerator().generateRealNetworks(-1, networkName, parametersWithOutThreshold);
	    Request[] requests = new Request[parametersWithThreshold.numRequest];
	    for (int i = 0; i < parametersWithThreshold.numRequest; i++) {
	      int bandwidth =
	          random.nextInt((parametersWithThreshold.linkBWCapMax - parametersWithThreshold.linkBWCapMin) + 1) + parametersWithThreshold.linkBWCapMin;
	      requests[i] = new Request(bandwidth, network.getRandomServer(), network.getRandomServer(), parametersWithThreshold);
	    }
	    network.wipeLinks();
	    for (int i = 0; i < parametersWithThreshold.numRequest; i++) {
	      Algorithm alg = new Algorithm(network, requests[i], parametersWithThreshold);
	      results[i] = alg.maxThroughputWithoutDelay();
	      if (results[i].admit) {
	        accepted++;
	      }
	    }
	    expSum += accepted;
	    accepted = 0;
	    network.wipeLinks();
	    for (int i = 0; i < parametersWithThreshold.numRequest; i++) {
	      Algorithm alg = new Algorithm(network, requests[i], parametersWithOutThreshold);
	      results[i] = alg.maxThroughputWithoutDelay();
	      if (results[i].admit) {
	        accepted++;
	      }
	    }
	    linSum += accepted;
	  }
	  System.out.println("\n" + expSum);
	  System.out.println(linSum);
  }
  
  public static void LEffect() {
    for (int L = 2; L <= 6; L += 2) {
      Parameters parameters = new Parameters.Builder().beta(L).build();

      int accepted = 0; //number of accepted requests
      int expSum = 0; //sum of all exponential cost accepted requests
      Result[] results = new Result[parameters.numRequest];

      for (int networkSize : parameters.networkSizes) {
        for (int j = 0; j < parameters.numTrials; j++) {
          Network network = new NetworkGenerator().barabasiAlbertNetwork(networkSize, 1, parameters); // TODO
          Request[] requests = new Request[parameters.numRequest];
          for (int i = 0; i < parameters.numRequest; i++) {
            int bandwidth = random.nextInt((parameters.linkBWCapMax - parameters.linkBWCapMin) + 1) + parameters.linkBWCapMin;
            requests[i] = new Request(bandwidth, network.getRandomServer(), network.getRandomServer(), parameters);
          }
          network.wipeLinks();
          for (int i = 0; i < parameters.numRequest; i++) {
            Algorithm alg = new Algorithm(network, requests[i], parameters);
            results[i] = alg.minOpCostWithoutDelay();
            if (results[i].admit) {
              accepted++;
            }
          }
          expSum += accepted;
        }
      }
      System.out.println("\n" + expSum);
    }
  }
  

  public static void LEffectRealTopology(String networkName) {
	  
    for (int L = 2; L <= 6; L += 2) {
      Parameters parameters = new Parameters.Builder().beta(L).build();

      int accepted = 0; //number of accepted requests
      int expSum = 0; //sum of all exponential cost accepted requests
      Result[] results = new Result[parameters.numRequest];
      
      for (int j = 0; j < parameters.numTrials; j++) {
    	Network network = new NetworkGenerator().generateRealNetworks(-1, networkName, parameters); // TODO
        Request[] requests = new Request[parameters.numRequest];
        for (int i = 0; i < parameters.numRequest; i++) {
          int bandwidth = random.nextInt((parameters.linkBWCapMax - parameters.linkBWCapMin) + 1) + parameters.linkBWCapMin;
          requests[i] = new Request(bandwidth, network.getRandomServer(), network.getRandomServer(), parameters);
        }
        network.wipeLinks();
        for (int i = 0; i < parameters.numRequest; i++) {
          Algorithm alg = new Algorithm(network, requests[i], parameters);
          results[i] = alg.minOpCostWithoutDelay();
          if (results[i].admit) {
            accepted++;
          }
        }
        expSum += accepted;
      }
      System.out.println("\n" + expSum);
    }
  }
}
