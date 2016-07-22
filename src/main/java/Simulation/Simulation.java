package Simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import Algorithm.Algorithm;
import Algorithm.Benchmark;
import Algorithm.CostFunctions.ExponentialCostFunction;
import Algorithm.CostFunctions.LinCostFunction;
import Algorithm.Result;
import Network.Network;
import Network.Request;
import Network.Server;
import NetworkGenerator.NetworkGenerator;
import NetworkGenerator.NetworkValueSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@SuppressWarnings({"Duplicates", "unused"}) public class Simulation {
  public static final Random random = new Random();
  public static Parameters baseParameters = new Parameters.Builder().build();
  private static final ExecutorService threadPool = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());
  private static final ArrayList<TopologyFile> topologyFiles = new ArrayList<>();
  private static final Logger logger = LoggerFactory.getLogger(Simulation.class);

  public static void main(String[] args) {
    ArrayList<Runnable> listOfTasks = new ArrayList<>();
    for (String arg : args) {
      switch (arg) {
        case "d":
          logger.info("Enabled debugging profile");
          baseParameters = new Parameters.Builder().networkSizes(new int[] {50, 100})
                                                   .numRequests(100)
                                                   .numTrials(3)
                                                   .build();
          break;
        case "0":
          listOfTasks.add(new Thread(() -> LEffectWithoutDelays()));
          break;
        case "1":
          listOfTasks.add(new Thread(() -> LEffectWithDelays()));
          break;
        case "2":
          listOfTasks.add(new Thread(() -> betaImpactWithoutDelays()));
          break;
        case "3":
          listOfTasks.add(new Thread(() -> betaImpactWithDelays()));
          break;
        case "4":
          listOfTasks.add(new Thread(() -> ThresholdEffectWithoutDelays()));
          break;
        case "5":
          listOfTasks.add(new Thread(() -> ThresholdEffectWithDelays()));
          break;
        case "6":
          listOfTasks.add(new Thread(() -> CompareCostFnsWithoutDelays()));
          break;
        case "7":
          listOfTasks.add(new Thread(() -> CompareCostFnsWithDelays()));
          break;
        default:
          System.out.println("Unknown argument: " + arg);
          System.exit(1);
      }
    }

    listOfTasks.forEach(threadPool::execute);

    threadPool.shutdown();
    try {
      threadPool.awaitTermination(1L, TimeUnit.DAYS);
    } catch (InterruptedException ie) {
      ie.printStackTrace();
    }
  }

  public static void prepareTopologies() {
    for (int networkSize : baseParameters.networkSizes) {
      for (int trial = 0; trial < baseParameters.numTrials; ++trial) {
        topologyFiles.add(new TopologyFile(networkSize, String.valueOf(trial)));
      }
    }
    topologyFiles.add(new TopologyFile("GEANT"));
    topologyFiles.add(new TopologyFile("AS1755"));
    topologyFiles.add(new TopologyFile("AS4755"));
    topologyFiles.forEach(System.out::println);
  }

  /**
   * We compare the performance of the proposed offline algorithms while using different cost functions, i.e., a linear cost function and an exponential cost
   * function
   */
  private static void CompareCostFnsWithoutDelays() {
    prepareLogging();
    Parameters parametersWithExpCostFn = new Parameters.Builder().costFunc(new ExponentialCostFunction())
                                                                 .offline(false)
                                                                 .build();
    Parameters parametersWithLinearCostFn = new Parameters.Builder().costFunc(new LinCostFunction())
                                                                    .offline(false)
                                                                    .build();

    Result[][] expResults = new Result[baseParameters.numTrials][baseParameters.numRequests];
    Result[][] linearResults = new Result[baseParameters.numTrials][baseParameters.numRequests];

    for (int nSizeIndex = 0; nSizeIndex < baseParameters.networkSizes.length; nSizeIndex++) {
      int networkSize = baseParameters.networkSizes[nSizeIndex];

      for (int trial = 0; trial < baseParameters.numTrials; ++trial) {
        Network network = generateAndInitializeNetwork(networkSize, trial, parametersWithExpCostFn);
        ArrayList<Request> requests = generateRequests(parametersWithExpCostFn, network, parametersWithExpCostFn.numRequests);

        HashMap<Server, Server> serverMap = new HashMap<>();
        Network networkAlt = network.newNetwork(serverMap);
        ArrayList<Request> requestsAlt = mapRequestsToNewNetwork(requests, serverMap);

        network.wipeLinks();
        logger.debug(String.format("Network size: %d\texp cost\ttrial: %d started", networkSize, trial));
        for (int i = 0; i < baseParameters.numRequests; ++i) {
          Algorithm alg = new Algorithm(network, requests.get(i), parametersWithExpCostFn);
          expResults[trial][i] = alg.maxThroughputWithoutDelay();
        }
        logger.debug(String.format("Network size: %d\texp cost\ttrial: %d finished", networkSize, trial));

        network.wipeLinks();
        logger.debug(String.format("Network size: %d\tlinear cost\ttrial: %d started", networkSize, trial));
        for (int i = 0; i < baseParameters.numRequests; ++i) {
          Algorithm alg = new Algorithm(networkAlt, requestsAlt.get(i), parametersWithLinearCostFn);
          linearResults[trial][i] = alg.maxThroughputWithoutDelay();
        }
        logger.debug(String.format("Network size: %d\tlinear cost\ttrial: %d finished", networkSize, trial));
      }

      double expAdmittedCount = 0d;
      double expPathCost = 0d;
      double linearAdmittedCount = 0d;
      double linearPathCost = 0d;
      for (int trial = 0; trial < baseParameters.numTrials; ++trial) {
        for (int i = 0; i < baseParameters.numRequests; ++i) {
          if (expResults[trial][i].isAdmitted()) {
            ++expAdmittedCount;
            expPathCost += expResults[trial][i].getPathCost();
          }
          if (linearResults[trial][i].isAdmitted()) {
            ++linearAdmittedCount;
            linearPathCost += linearResults[trial][i].getPathCost();
          }
        }
      }
      expAdmittedCount /= (double) baseParameters.numTrials;
      expPathCost /= (double) baseParameters.numTrials;
      linearAdmittedCount /= (double) baseParameters.numTrials;
      linearPathCost /= (double) baseParameters.numTrials;
      logger.info(String.format("%d %f %f", networkSize, expAdmittedCount, linearAdmittedCount));
      logger.info(String.format("%d %f %f", networkSize, expPathCost, linearPathCost));
    }
  }

  private static void CompareCostFnsWithDelays() {
    prepareLogging();
    Parameters parametersWithExpCostFn = new Parameters.Builder().costFunc(new ExponentialCostFunction()).build();
    Parameters parametersWithLinearCostFn = new Parameters.Builder().costFunc(new LinCostFunction()).build();

    Result[][] expResults = new Result[baseParameters.numTrials][baseParameters.numRequests];
    Result[][] linearResults = new Result[baseParameters.numTrials][baseParameters.numRequests];
    for (int networkSize : baseParameters.networkSizes) {
      for (int trial = 0; trial < baseParameters.numTrials; ++trial) {
        Network network = generateAndInitializeNetwork(networkSize, trial, parametersWithExpCostFn);
        ArrayList<Request> requests = generateRequests(parametersWithExpCostFn, network, parametersWithExpCostFn.numRequests);

        HashMap<Server, Server> serverMap = new HashMap<>();
        Network networkAlt = network.newNetwork(serverMap);
        ArrayList<Request> requestsAlt = mapRequestsToNewNetwork(requests, serverMap);

        network.wipeLinks();
        logger.debug(String.format("Network size: %d\texp cost\ttrial: %d started", networkSize, trial));
        for (int i = 0; i < baseParameters.numRequests; ++i) {
          Algorithm alg = new Algorithm(network, requests.get(i), parametersWithExpCostFn);
          expResults[trial][i] = alg.maxThroughputWithDelay();
        }
        logger.debug(String.format("Network size: %d\texp cost\ttrial: %d finished", networkSize, trial));

        network.wipeLinks();
        logger.debug("Network size: " + networkSize + "\tlinear cost" + "\ttrial: " + trial + " started");
        for (int i = 0; i < baseParameters.numRequests; ++i) {
          Algorithm alg = new Algorithm(networkAlt, requestsAlt.get(i), parametersWithLinearCostFn);
          linearResults[trial][i] = alg.maxThroughputWithDelay();
        }
        logger.debug(String.format("Network size: %d\tlinear cost\ttrial: %d finished", networkSize, trial));
      }

      double expAdmittedCount = 0d;
      double expPathCost = 0d;
      double linearAdmittedCount = 0d;
      double linearPathCost = 0d;
      for (int trial = 0; trial < baseParameters.numTrials; ++trial) {
        for (int i = 0; i < baseParameters.numRequests; ++i) {
          if (expResults[trial][i].isAdmitted()) {
            ++expAdmittedCount;
            expPathCost += expResults[trial][i].getPathCost();
          }
          if (linearResults[trial][i].isAdmitted()) {
            ++linearAdmittedCount;
            linearPathCost += linearResults[trial][i].getPathCost();
          }
        }
      }
      expAdmittedCount /= (double) baseParameters.numTrials;
      expPathCost /= (double) baseParameters.numTrials;
      linearAdmittedCount /= (double) baseParameters.numTrials;
      linearPathCost /= (double) baseParameters.numTrials;
      logger.info(String.format("%d %f %f", networkSize, expAdmittedCount, linearAdmittedCount));
      logger.info(String.format("%d %f %f", networkSize, expPathCost, linearPathCost));
    }
  }

  private static void betaImpactWithoutDelays() {
    prepareLogging();
    double[] betas = new double[] {2d, 4d, 6d, 8d};
    for (int networkSize : baseParameters.networkSizes) {
      Result[][][] results = new Result[betas.length][baseParameters.numTrials][baseParameters.numRequests];

      for (int betaIdx = 0; betaIdx < betas.length; ++betaIdx) {
        double beta = betas[betaIdx];
        Parameters parameters = new Parameters.Builder().networkSize(networkSize)
                                                        .beta(beta)
                                                        .offline(false)
                                                        .build();

        for (int trial = 0; trial < baseParameters.numTrials; ++trial) {
          Network network = generateAndInitializeNetwork(networkSize, trial, parameters);
          ArrayList<Request> requests = generateRequests(parameters, network, parameters.numRequests);

          network.wipeLinks();
          logger.debug(String.format("Network size: %d\tbeta: %f\ttrial: %d started", networkSize, beta, trial));
          for (int i = 0; i < baseParameters.numRequests; i++) {
            Algorithm alg = new Algorithm(network, requests.get(i), parameters);
            results[betaIdx][trial][i] = alg.maxThroughputWithoutDelay();
          }
          logger.debug(String.format("Network size: %d\tbeta: %f\ttrial: %d finished", networkSize, beta, trial));
        }
      }

      StringBuilder count = new StringBuilder();
      StringBuilder cost = new StringBuilder();
      count.append(networkSize).append(" ");
      cost.append(networkSize).append(" ");
      for (int betaIdx = 0; betaIdx < betas.length; ++betaIdx) {
        double beta = betas[betaIdx];
        double expAdmittedCount = 0d;
        double expPathCost = 0d;
        for (int i = 0; i < baseParameters.numRequests; ++i) {
          for (int trial = 0; trial < baseParameters.numTrials; ++trial) {
            if (results[betaIdx][trial][i].isAdmitted()) {
              ++expAdmittedCount;
              expPathCost += results[betaIdx][trial][i].getPathCost();
            }
          }
        }
        expAdmittedCount /= (double) baseParameters.numTrials;
        count.append(expAdmittedCount).append(" ");
        expPathCost /= (double) baseParameters.numTrials;
        cost.append(expPathCost).append(" ");
      }
      logger.info(count.toString());
      logger.info(cost.toString());
    }
  }

  private static void betaImpactWithDelays() {
    prepareLogging();
    double[] betas = new double[] {2d, 4d, 6d, 8d};
    for (int networkSize : baseParameters.networkSizes) {
      Result[][][] results = new Result[betas.length][baseParameters.numTrials][baseParameters.numRequests];

      for (int betaIdx = 0; betaIdx < betas.length; ++betaIdx) {
        double beta = betas[betaIdx];
        Parameters parameters = new Parameters.Builder().networkSize(networkSize)
                                                        .beta(beta)
                                                        .offline(false)
                                                        .build();

        for (int trial = 0; trial < baseParameters.numTrials; ++trial) {
          Network network = generateAndInitializeNetwork(networkSize, trial, parameters);
          ArrayList<Request> requests = generateRequests(parameters, network, parameters.numRequests);
          network.wipeLinks();

          logger.debug(String.format("Network size: %d\tbeta: %f\ttrial: %d started", networkSize, beta, trial));
          for (int i = 0; i < baseParameters.numRequests; i++) {
            Algorithm alg = new Algorithm(network, requests.get(i), parameters);
            results[betaIdx][trial][i] = alg.maxThroughputWithDelay();
          }
          logger.debug(String.format("Network size: %d\tbeta: %f\ttrial: %d finished", networkSize, beta, trial));
        }
      }

      StringBuilder count = new StringBuilder();
      StringBuilder cost = new StringBuilder();
      count.append(networkSize).append(" ");
      cost.append(networkSize).append(" ");
      for (int betaIdx = 0; betaIdx < betas.length; ++betaIdx) {
        double beta = betas[betaIdx];

        double expAdmittedCount = 0d;
        double expPathCost = 0d;
        for (int i = 0; i < baseParameters.numRequests; ++i) {

          for (int trial = 0; trial < baseParameters.numTrials; ++trial) {
            if (results[betaIdx][trial][i].isAdmitted()) {
              ++expAdmittedCount;
              expPathCost += results[betaIdx][trial][i].getPathCost();
            }
          }
        }
        expAdmittedCount /= (double) baseParameters.numTrials;
        count.append(expAdmittedCount).append(" ");
        expPathCost /= (double) baseParameters.numTrials;
        cost.append(expPathCost).append(" ");
      }
      logger.info(count.toString());
      logger.info(cost.toString());
    }
  }

  /**
   * We test the impact of threshold on the performance of the proposed offline algorithms by running the algorithms with and without the threshold
   */
  private static void ThresholdEffectWithoutDelays() {
    prepareLogging();
    Result[][] withThresholdResults = new Result[baseParameters.numTrials][baseParameters.numRequests];
    Result[][] withoutThresholdResults = new Result[baseParameters.numTrials][baseParameters.numRequests];

    for (int networkSize : baseParameters.networkSizes) {
      Parameters parametersWithThreshold = new Parameters.Builder().networkSize(networkSize)
                                                                   .offline(false)
                                                                   .build();
      Parameters parametersWithOutThreshold = new Parameters.Builder().threshold(Double.MAX_VALUE)
                                                                      .networkSize(networkSize)
                                                                      .offline(false)
                                                                      .build();

      for (int trial = 0; trial < baseParameters.numTrials; ++trial) {
        Network network = generateAndInitializeNetwork(networkSize, trial, parametersWithThreshold);
        ArrayList<Request> requests = generateRequests(parametersWithThreshold, network, parametersWithThreshold.numRequests);

        HashMap<Server, Server> serverMap = new HashMap<>();
        Network networkAlt = network.newNetwork(serverMap);
        ArrayList<Request> requestsAlt = mapRequestsToNewNetwork(requests, serverMap);

        network.wipeLinks();
        logger.debug(String.format("Network size: %d\tw/ threshold\ttrial: %d started", networkSize, trial));
        for (int i = 0; i < baseParameters.numRequests; ++i) {
          Algorithm alg = new Algorithm(network, requests.get(i), parametersWithThreshold);
          withThresholdResults[trial][i] = alg.maxThroughputWithoutDelay();
        }
        logger.debug(String.format("Network size: %d\tw/ threshold\ttrial: %d finished", networkSize, trial));

        network.wipeLinks();
        logger.debug(String.format("Network size: %d\tw/o threshold\ttrial: %d started", networkSize, trial));
        for (int i = 0; i < baseParameters.numRequests; ++i) {
          Algorithm alg = new Algorithm(networkAlt, requestsAlt.get(i), parametersWithOutThreshold);
          withoutThresholdResults[trial][i] = alg.maxThroughputWithoutDelay();
        }
        logger.debug(String.format("Network size: %d\tw/o threshold\ttrial: %d finished", networkSize, trial));
      }

      double withThresholdAdmissionCount = 0d;
      double withThresholdPathCost = 0d;
      double withoutThresholdAdmissionCount = 0d;
      double withoutThresholdPathCost = 0d;
      for (int trial = 0; trial < baseParameters.numTrials; ++trial) {
        for (int i = 0; i < baseParameters.numRequests; ++i) {
          if (withThresholdResults[trial][i].isAdmitted()) {
            ++withThresholdAdmissionCount;
            withThresholdPathCost += withThresholdResults[trial][i].getPathCost();
          }
          if (withoutThresholdResults[trial][i].isAdmitted()) {
            ++withoutThresholdAdmissionCount;
            withoutThresholdPathCost += withoutThresholdResults[trial][i].getPathCost();
          }
        }
      }
      withThresholdAdmissionCount /= (double) baseParameters.numTrials;
      withThresholdPathCost /= (double) baseParameters.numTrials;
      withoutThresholdAdmissionCount /= (double) baseParameters.numTrials;
      withoutThresholdPathCost /= (double) baseParameters.numTrials;
      logger.info(String.format("%d %f %f", networkSize, withThresholdAdmissionCount, withoutThresholdAdmissionCount));
      logger.info(String.format("%d %f %f", networkSize, withThresholdPathCost, withoutThresholdPathCost));
    }
  }

  private static void ThresholdEffectWithDelays() {
    prepareLogging();

    Result[][] withThresholdResults = new Result[baseParameters.numTrials][baseParameters.numRequests];
    Result[][] withoutThresholdResults = new Result[baseParameters.numTrials][baseParameters.numRequests];

    for (int networkSize : baseParameters.networkSizes) {
      Parameters parametersWithThreshold = new Parameters.Builder().networkSize(networkSize)
                                                                   .offline(false)
                                                                   .build();
      Parameters parametersWithOutThreshold = new Parameters.Builder().threshold(Double.MAX_VALUE)
                                                                      .networkSize(networkSize)
                                                                      .offline(false)
                                                                      .build();

      for (int trial = 0; trial < baseParameters.numTrials; ++trial) {
        Network network = generateAndInitializeNetwork(networkSize, trial, parametersWithThreshold);
        ArrayList<Request> requests = generateRequests(parametersWithThreshold, network, parametersWithThreshold.numRequests);

        HashMap<Server, Server> serverMap = new HashMap<>();
        Network networkAlt = network.newNetwork(serverMap);
        ArrayList<Request> requestsAlt = mapRequestsToNewNetwork(requests, serverMap);

        network.wipeLinks();
        logger.debug(String.format("Network size: %d\tw/ threshold\ttrial: %d started", networkSize, trial));
        for (int i = 0; i < baseParameters.numRequests; ++i) {
          Algorithm alg = new Algorithm(network, requests.get(i), parametersWithThreshold);
          withThresholdResults[trial][i] = alg.maxThroughputWithDelay();
        }
        logger.debug(String.format("Network size: %d\tw/ threshold\ttrial: %d started", networkSize, trial));

        network.wipeLinks();
        logger.debug(String.format("Network size: %d\tw/o threshold\ttrial: %d started", networkSize, trial));
        for (int i = 0; i < baseParameters.numRequests; ++i) {
          Algorithm alg = new Algorithm(networkAlt, requestsAlt.get(i), parametersWithOutThreshold);
          withoutThresholdResults[trial][i] = alg.maxThroughputWithDelay();
        }
        logger.debug(String.format("Network size: %d\tw/o threshold\ttrial: %d started", networkSize, trial));
      }

      double withThresholdAdmissionCount = 0d;
      double withThresholdPathCost = 0d;
      double withoutThresholdAdmissionCount = 0d;
      double withoutThresholdPathCost = 0d;
      for (int trial = 0; trial < baseParameters.numTrials; ++trial) {
        for (int i = 0; i < baseParameters.numRequests; ++i) {
          if (withThresholdResults[trial][i].isAdmitted()) {
            ++withThresholdAdmissionCount;
            withThresholdPathCost += withThresholdResults[trial][i].getPathCost();
          }
          if (withoutThresholdResults[trial][i].isAdmitted()) {
            ++withoutThresholdAdmissionCount;
            withoutThresholdPathCost += withoutThresholdResults[trial][i].getPathCost();
          }
        }
      }
      withThresholdAdmissionCount /= (double) baseParameters.numTrials;
      withThresholdPathCost /= (double) baseParameters.numTrials;
      withoutThresholdAdmissionCount /= (double) baseParameters.numTrials;
      withoutThresholdPathCost /= (double) baseParameters.numTrials;
      logger.info(String.format("%d %f %f", networkSize, withThresholdAdmissionCount, withoutThresholdAdmissionCount));
      logger.info(String.format("%d %f %f", networkSize, withThresholdPathCost, withoutThresholdPathCost));
    }
  }

  private static void LEffectWithoutDelays() {
    prepareLogging();
    for (int L = 2; L <= 6; L += 2) {
      //Parameters parameters = new Parameters.Builder().L(L).build();

      int expSum = 0; //sum of all exponential cost accepted requests
      Result[] results = new Result[baseParameters.numRequests];
      Result[] resultsBenchmark = new Result[baseParameters.numRequests];

      for (int networkSize : baseParameters.networkSizes) {
        Parameters parameters = new Parameters.Builder().networkSize(networkSize)
                                                        .L(L)
                                                        .costFunc(new LinCostFunction())
                                                        .offline(true)
                                                        .build();
        int accepted = 0; //number of accepted requests
        int acceptedBenchmark = 0;

        double averageCostNet = 0d;
        double averageCostNetBenchmark = 0d;
        for (int trial = 0; trial < baseParameters.numTrials; ++trial) {
          Network network = generateAndInitializeNetwork(networkSize, trial, parameters);
          ArrayList<Request> requests = generateRequests(parameters, network, parameters.numRequests);
          network.wipeLinks();

          HashMap<Server, Server> serverMap = new HashMap<Server, Server>();
          Network networkAlt = network.newNetwork(serverMap);
          ArrayList<Request> requestsAlt = mapRequestsToNewNetwork(requests, serverMap);

          logger.debug(String.format("Network size: %d\tL: %d\ttrial: %d Algorithm started", networkSize, L, trial));
          double averageCostReq = 0d;
          for (int i = 0; i < parameters.numRequests; i++) {
            Algorithm alg = new Algorithm(network, requests.get(i), parameters);
            results[i] = alg.minOpCostWithoutDelay();
            if (results[i].isAdmitted()) {
              accepted++;
              averageCostReq += results[i].getPathCost();
            }
          }
          logger.debug(String.format("Network size: %d\tL: %d\ttrial: %d Algorithm finished", networkSize, L, trial));

          averageCostReq = averageCostReq / accepted;
          averageCostNet += (averageCostReq / baseParameters.numTrials);
          expSum += accepted;

          network.wipeLinks();

          logger.debug(String.format("Network size: %d\tL: %d\ttrial: %d Benchmark started", networkSize, L, trial));
          double averageCostReqBenchmark = 0d;
          for (int i = 0; i < parameters.numRequests; i++) {
            Benchmark benchmark = new Benchmark(networkAlt, requestsAlt.get(i), parameters);
            resultsBenchmark[i] = benchmark.benchmarkNFVUnicast();
            if (resultsBenchmark[i].isAdmitted()) {
              acceptedBenchmark++;
              averageCostReqBenchmark += resultsBenchmark[i].getPathCost();
            }
          }
          logger.debug(String.format("Network size: %d\tL: %d\ttrial: %d Benchmark finished", networkSize, L, trial));

          averageCostReqBenchmark = averageCostReqBenchmark / acceptedBenchmark;
          averageCostNetBenchmark += (averageCostReqBenchmark / baseParameters.numTrials);
        }
        logger.info(String.format("%d %s %s", networkSize, averageCostNet, averageCostNetBenchmark));
      }
    }
  }

  private static void LEffectWithDelays() {
    prepareLogging();
    for (int L = 2; L <= 6; L += 2) {
      int expSum = 0; // sum of all exponential cost accepted requests
      Result[] results = new Result[baseParameters.numRequests];
      Result[] resultsBenchmark = new Result[baseParameters.numRequests];

      for (int networkSize : baseParameters.networkSizes) {
        Parameters parameters = new Parameters.Builder().networkSize(networkSize)
                                                        .L(L)
                                                        .offline(true)
                                                        .build();

        int accepted = 0; // number of accepted requests
        int acceptedBenchmark = 0;

        double averageCostNet = 0d;
        double averageCostNetBenchmark = 0d;
        for (int trial = 0; trial < baseParameters.numTrials; ++trial) {
          Network network = generateAndInitializeNetwork(networkSize, trial, parameters);
          ArrayList<Request> requests = generateRequests(parameters, network, parameters.numRequests);
          network.wipeLinks();

          HashMap<Server, Server> serverMap = new HashMap<Server, Server>();
          Network networkAlt = network.newNetwork(serverMap);
          ArrayList<Request> requestsAlt = mapRequestsToNewNetwork(requests, serverMap);

          logger.debug(String.format("Network size: %d\tL: %d\ttrial: %d Algorithm started", networkSize, L, trial));
          double averageCostReq = 0d;
          for (int i = 0; i < parameters.numRequests; i++) {
            Algorithm alg = new Algorithm(network, requests.get(i), parameters);
            results[i] = alg.minOpCostWithDelay();
            if (results[i].isAdmitted()) {
              accepted++;
              averageCostReq += results[i].getPathCost();
            }
          }
          logger.debug(String.format("Network size: %d\tL: %d\ttrial: %d Algorithm finished", networkSize, L, trial));

          averageCostReq = averageCostReq / accepted;
          averageCostNet += (averageCostReq / baseParameters.numTrials);
          expSum += accepted;

          network.wipeLinks();

          logger.debug(String.format("Network size: %d\tL: %d\ttrial: %d Benchmark started", networkSize, L, trial));
          double averageCostReqBenchmark = 0d;
          for (int i = 0; i < parameters.numRequests; i++) {
            Benchmark benchmark = new Benchmark(networkAlt, requestsAlt.get(i), parameters);
            resultsBenchmark[i] = benchmark.benchmarkNFVUnicastDelay();
            if (resultsBenchmark[i].isAdmitted()) {
              acceptedBenchmark++;
              averageCostReqBenchmark += resultsBenchmark[i].getPathCost();
            }
          }
          logger.debug(String.format("Network size: %d\tL: %d\ttrial: %d Benchmark finished", networkSize, L, trial));

          averageCostReqBenchmark = averageCostReqBenchmark / acceptedBenchmark;
          averageCostNetBenchmark += (averageCostReqBenchmark / baseParameters.numTrials);
        }
        logger.info(String.format("%d %s %s", networkSize, averageCostNet, averageCostNetBenchmark));
      }
    }
  }

  private static ArrayList<Request> generateRequests(Parameters parameters, Network network, int numRequests) {
    ArrayList<Request> requests = new ArrayList<>();
    for (int i = 0; i < numRequests; ++i) {
      // source and destination should be different.
      Server source = network.getRandomServer();
      Server destination = network.getRandomServer();
      while (source.equals(destination)) {
        destination = network.getRandomServer();
      }
      requests.add(new Request(source, destination, parameters));
    }
    return requests;
  }

  private static ArrayList<Request> mapRequestsToNewNetwork(ArrayList<Request> originalRequests, HashMap<Server, Server> serverMap) {
    return originalRequests.stream().map(original -> original.newRequest(serverMap)).collect(Collectors.toCollection(ArrayList::new));
  }

  private static void prepareLogging() {
    String functionName = Thread.currentThread().getStackTrace()[2].getMethodName();
    MDC.put("exp", functionName);
    Thread.currentThread().setName(functionName);
    logger.debug(String.format("%s started", functionName));
  }

  private static void initializeNetwork(Network network, Parameters parameters) {
    NetworkValueSetter networkValueSetter = new NetworkValueSetter(network, parameters);
    networkValueSetter.setConstantServerCapacity(Double.MAX_VALUE, parameters.serverRatio);
    networkValueSetter.setRandomLinkCapacity(parameters.linkBWCapMin, parameters.linkBWCapMax);
    networkValueSetter.setRandomLinkDelay(parameters.linkDelayMin, parameters.linkDelayMax);
    networkValueSetter.placeNFVs(parameters.nfvProb);
  }

  private static Network generateAndInitializeNetwork(int networkSize, int trial, Parameters parameters) {
    Network network = NetworkGenerator.generateRealNetworks(networkSize, String.valueOf(trial));
    initializeNetwork(network, parameters);
    return network;
  }

  private static Network generateAndInitializeNetwork(TopologyFile topologyFile, Parameters parameters) {
    Network network = NetworkGenerator.generateRealNetworks(topologyFile.getPrefix(), topologyFile.getSuffix());
    initializeNetwork(network, parameters);
    return network;
  }

  public static Logger getLogger() {
    return logger;
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
