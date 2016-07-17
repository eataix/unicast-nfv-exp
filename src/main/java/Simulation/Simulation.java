package Simulation;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Algorithm.Algorithm;
import Algorithm.CostFunctions.ExponentialCostFunction;
import Algorithm.CostFunctions.LinCostFunction;
import Algorithm.Result;
import Network.Network;
import Network.Request;
import NetworkGenerator.NetworkGenerator;
import NetworkGenerator.NetworkValueSetter;

@SuppressWarnings("Duplicates") public class Simulation {
  public static final Random random = new Random();
  private static final ExecutorService threadPool = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());
  public static final Parameters defaultParameters = new Parameters.Builder().build();
  private static final ArrayList<TopologyFile> topologyFiles = new ArrayList<>();

  public static void main(String[] args) {
    for (int networkSize : defaultParameters.networkSizes) {
      for (int trial = 0; trial < defaultParameters.numTrials; ++trial) {
        topologyFiles.add(new TopologyFile(networkSize, String.valueOf(trial)));
      }
    }
    topologyFiles.add(new TopologyFile("GEANT"));
    topologyFiles.add(new TopologyFile("AS1755"));
    topologyFiles.add(new TopologyFile("AS4755"));
    topologyFiles.forEach(System.out::println);

    //ArrayList<Runnable> listOfTasks = new ArrayList<>();
    //listOfTasks.add(new Thread(() -> CompareCostFnsWithoutDelays()));
    //listOfTasks.add(new Thread(() -> CompareCostFnsWithDelays()));
    //listOfTasks.add(new Thread(() -> betaImpactWithoutDelays()));
    //listOfTasks.add(new Thread(() -> betaImpactWithDelays()));
    //listOfTasks.add(new Thread(() -> ThresholdEffectWithoutDelays()));
    //listOfTasks.add(new Thread(() -> ThresholdEffectWithDelays()));
    //listOfTasks.add(new Thread(() -> LEffectWithoutDelays()));
    //listOfTasks.add(new Thread(() -> LEffectWithDelays()));
    //listOfTasks.forEach(threadPool::execute);
    //
    //threadPool.shutdown();
    //try {
    //  threadPool.awaitTermination(1, TimeUnit.DAYS);
    //} catch (InterruptedException ie) {
    //  ie.printStackTrace();
    //}
  }

  /**
   * We compare the performance of the proposed online algorithms while using different cost functions, i.e., a linear cost function and an exponential cost
   * function
   */
  private static void CompareCostFnsWithoutDelays() {
    Parameters parametersWithExpCostFn = new Parameters.Builder().costFunc(new ExponentialCostFunction()).build();
    Parameters parametersWithLinearCostFn = new Parameters.Builder().costFunc(new LinCostFunction()).build();

    int expSum = 0;
    int linSum = 0;

    for (TopologyFile topologyFile : topologyFiles) {
      int accepted = 0;

      Network network = generateAndInitializeNetwork(topologyFile, parametersWithExpCostFn);
      network.wipeLinks();
      ArrayList<Request> requests = generateRequests(parametersWithExpCostFn, network, parametersWithExpCostFn.numRequest);

      Result[] expResults = new Result[parametersWithExpCostFn.numRequest];
      for (int i = 0; i < parametersWithExpCostFn.numRequest; ++i) {
        Algorithm alg = new Algorithm(network, requests.get(i), parametersWithExpCostFn);
        expResults[i] = alg.maxThroughputWithoutDelay();
        if (expResults[i].isAdmitted()) {
          ++accepted;
        }
      }
      expSum += accepted;

      Result[] linearResults = new Result[parametersWithExpCostFn.numRequest];
      accepted = 0;
      network.wipeLinks();
      for (int i = 0; i < parametersWithExpCostFn.numRequest; ++i) {
        Algorithm alg = new Algorithm(network, requests.get(i), parametersWithLinearCostFn);
        linearResults[i] = alg.maxThroughputWithoutDelay();
        if (linearResults[i].isAdmitted()) {
          ++accepted;
        }
      }
      linSum += accepted;
    }
  }

  private static void CompareCostFnsWithDelays() {
    Parameters parametersWithExpCostFn = new Parameters.Builder().costFunc(new ExponentialCostFunction()).build();
    Parameters parametersWithLinearCostFn = new Parameters.Builder().costFunc(new LinCostFunction()).build();

    int expSum = 0;
    int linSum = 0;

    for (TopologyFile topologyFile : topologyFiles) {
      int accepted = 0;

      Network network = generateAndInitializeNetwork(topologyFile, parametersWithExpCostFn);
      network.wipeLinks();
      ArrayList<Request> requests = generateRequests(parametersWithExpCostFn, network, parametersWithExpCostFn.numRequest);

      Result[] expResults = new Result[parametersWithExpCostFn.numRequest];
      for (int i = 0; i < parametersWithExpCostFn.numRequest; ++i) {
        Algorithm alg = new Algorithm(network, requests.get(i), parametersWithExpCostFn);
        expResults[i] = alg.maxThroughputWithDelay();
        if (expResults[i].isAdmitted()) {
          ++accepted;
        }
      }
      expSum += accepted;

      Result[] linearResults = new Result[parametersWithExpCostFn.numRequest];
      accepted = 0;
      network.wipeLinks();
      for (int i = 0; i < parametersWithExpCostFn.numRequest; ++i) {
        Algorithm alg = new Algorithm(network, requests.get(i), parametersWithLinearCostFn);
        linearResults[i] = alg.maxThroughputWithDelay();
        if (linearResults[i].isAdmitted()) {
          ++accepted;
        }
      }
      linSum += accepted;
    }
  }

  private static ArrayList<Request> generateRequests(Parameters parameters, Network network, int numRequests) {
    ArrayList<Request> requests = new ArrayList<>();
    for (int i = 0; i < numRequests; ++i) {
      requests.add(new Request(network.getRandomServer(), network.getRandomServer(), parameters));
    }
    return requests;
  }

  private static void betaImpactWithoutDelays() {
    for (int beta = 2; beta <= 6; beta += 2) {
      Parameters parameters = new Parameters.Builder().beta(beta).build();

      int accepted = 0; //number of accepted requests
      int expSum = 0; //sum of all exponential cost accepted requests
      Result[] results = new Result[parameters.numRequest];

      for (TopologyFile topologyFile : topologyFiles) {
        Network network = generateAndInitializeNetwork(topologyFile, parameters);
        ArrayList<Request> requests = generateRequests(parameters, network, parameters.numRequest);
        network.wipeLinks();

        for (int i = 0; i < parameters.numRequest; i++) {
          Algorithm alg = new Algorithm(network, requests.get(i), parameters);
          results[i] = alg.maxThroughputWithoutDelay();
          if (results[i].isAdmitted()) {
            accepted++;
          }
        }
        expSum += accepted;
      }
      System.out.println("\n" + expSum);
    }
  }

  private static void betaImpactWithDelays() {
    for (int beta = 2; beta <= 6; beta += 2) {
      Parameters parameters = new Parameters.Builder().beta(beta).build();

      int accepted = 0; //number of accepted requests
      int expSum = 0; //sum of all exponential cost accepted requests
      Result[] results = new Result[parameters.numRequest];

      for (TopologyFile topologyFile : topologyFiles) {
        Network network = generateAndInitializeNetwork(topologyFile, parameters);
        ArrayList<Request> requests = generateRequests(parameters, network, parameters.numRequest);
        network.wipeLinks();

        for (int i = 0; i < parameters.numRequest; i++) {
          Algorithm alg = new Algorithm(network, requests.get(i), parameters);
          results[i] = alg.maxThroughputWithDelay();
          if (results[i].isAdmitted()) {
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

  private static void ThresholdEffectWithoutDelays() {
    Parameters parametersWithThreshold = new Parameters.Builder().build();
    Parameters parametersWithOutThreshold = new Parameters.Builder().threshold(Integer.MAX_VALUE)
                                                                    .build();

    int accepted = 0; //number of accepted requests
    int expSum = 0; //sum of all exponential cost accepted requests
    int linSum = 0; //sum of all linear cost accepted requests
    Result[] results = new Result[parametersWithThreshold.numRequest];

    for (TopologyFile topologyFile : topologyFiles) {
      Network network = generateAndInitializeNetwork(topologyFile, parametersWithThreshold);
      ArrayList<Request> requests = generateRequests(parametersWithThreshold, network, parametersWithThreshold.numRequest);

      network.wipeLinks();
      for (int i = 0; i < parametersWithThreshold.numRequest; i++) {
        Algorithm alg = new Algorithm(network, requests.get(i), parametersWithThreshold);
        results[i] = alg.maxThroughputWithoutDelay();
        if (results[i].isAdmitted()) {
          accepted++;
        }
      }
      expSum += accepted;
      accepted = 0;
      network.wipeLinks();
      for (int i = 0; i < parametersWithThreshold.numRequest; i++) {
        Algorithm alg = new Algorithm(network, requests.get(i), parametersWithOutThreshold);
        results[i] = alg.maxThroughputWithoutDelay();
        if (results[i].isAdmitted()) {
          accepted++;
        }
      }
      linSum += accepted;
    }
    System.out.println("\n" + expSum);
    System.out.println(linSum);
  }

  private static void ThresholdEffectWithDelays() {
    Parameters parametersWithThreshold = new Parameters.Builder().build();
    Parameters parametersWithOutThreshold = new Parameters.Builder().threshold(Integer.MAX_VALUE)
                                                                    .build();

    int accepted = 0; //number of accepted requests
    int expSum = 0; //sum of all exponential cost accepted requests
    int linSum = 0; //sum of all linear cost accepted requests
    Result[] results = new Result[parametersWithThreshold.numRequest];

    for (TopologyFile topologyFile : topologyFiles) {
      Network network = generateAndInitializeNetwork(topologyFile, parametersWithThreshold);
      ArrayList<Request> requests = generateRequests(parametersWithThreshold, network, parametersWithThreshold.numRequest);

      network.wipeLinks();
      for (int i = 0; i < parametersWithThreshold.numRequest; i++) {
        Algorithm alg = new Algorithm(network, requests.get(i), parametersWithThreshold);
        results[i] = alg.maxThroughputWithoutDelay();
        if (results[i].isAdmitted()) {
          accepted++;
        }
      }
      expSum += accepted;
      accepted = 0;
      network.wipeLinks();
      for (int i = 0; i < parametersWithThreshold.numRequest; i++) {
        Algorithm alg = new Algorithm(network, requests.get(i), parametersWithOutThreshold);
        results[i] = alg.maxThroughputWithDelay();
        if (results[i].isAdmitted()) {
          accepted++;
        }
      }
      linSum += accepted;
    }
    System.out.println("\n" + expSum);
    System.out.println(linSum);
  }

  private static void LEffectWithoutDelays() {
    for (int L = 2; L <= 6; L += 2) {
      Parameters parameters = new Parameters.Builder().L(L).build();

      int accepted = 0; //number of accepted requests
      int expSum = 0; //sum of all exponential cost accepted requests
      Result[] results = new Result[parameters.numRequest];

      for (TopologyFile topologyFile : topologyFiles) {
        Network network = generateAndInitializeNetwork(topologyFile, parameters);
        ArrayList<Request> requests = generateRequests(parameters, network, parameters.numRequest);
        network.wipeLinks();

        for (int i = 0; i < parameters.numRequest; i++) {
          Algorithm alg = new Algorithm(network, requests.get(i), parameters);
          results[i] = alg.minOpCostWithoutDelay();
          if (results[i].isAdmitted()) {
            accepted++;
          }
        }
        expSum += accepted;
      }
      System.out.println("\n" + expSum);
    }
  }

  private static void LEffectWithDelays() {
    for (int L = 2; L <= 6; L += 2) {
      Parameters parameters = new Parameters.Builder().L(L).build();

      int accepted = 0; //number of accepted requests
      int expSum = 0; //sum of all exponential cost accepted requests
      Result[] results = new Result[parameters.numRequest];

      for (TopologyFile topologyFile : topologyFiles) {
        Network network = generateAndInitializeNetwork(topologyFile, parameters);
        Request[] requests = new Request[parameters.numRequest];
        for (int i = 0; i < parameters.numRequest; i++) {
          requests[i] = new Request(network.getRandomServer(), network.getRandomServer(), parameters);
        }
        network.wipeLinks();
        for (int i = 0; i < parameters.numRequest; i++) {
          Algorithm alg = new Algorithm(network, requests[i], parameters);
          results[i] = alg.minOpCostWithoutDelay();
          if (results[i].isAdmitted()) {
            accepted++;
          }
        }
        expSum += accepted;
      }
      System.out.println("\n" + expSum);
    }
  }

  private static void initializeNetwork(Network network, Parameters parameters) {
    NetworkValueSetter networkValueSetter = new NetworkValueSetter(network, parameters);
    networkValueSetter.setConstantServerCapacity(Integer.MAX_VALUE, parameters.serverRatio);
    networkValueSetter.setRandomLinkCapacity(parameters.linkBWCapMin, parameters.linkBWCapMax);
    networkValueSetter.placeNFVs(parameters.nfvProb);
  }

  private static Network generateAndInitializeNetwork(TopologyFile topologyFile, Parameters parameters) {
    Network network = new NetworkGenerator().generateRealNetworks(topologyFile.getPrefix(), topologyFile.getSuffix());
    initializeNetwork(network, parameters);
    return network;
  }

  private static class TopologyFile {
    private final int prefix;
    private final String suffix;

    TopologyFile(int prefix, String suffix) {
      this.prefix = prefix;
      this.suffix = suffix;
    }

    TopologyFile(String suffix) {
      this.prefix = -1;
      this.suffix = suffix;
    }

    int getPrefix() {
      return prefix;
    }

    String getSuffix() {
      return suffix;
    }

    @Override public String toString() {
      return "TopologyFile{" +
          "prefix=" + prefix +
          ", suffix='" + suffix + '\'' +
          '}';
    }
  }
}
