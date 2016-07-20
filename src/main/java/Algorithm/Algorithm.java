package Algorithm;

import java.util.ArrayList;

import Algorithm.CostFunctions.CostFunction;
import Algorithm.CostFunctions.ExponentialCostFunction;
import Algorithm.CostFunctions.OperationalCostFunction;
import Network.AuxiliaryNetwork;
import Network.Network;
import Network.Request;
import Network.Server;
import NetworkGenerator.AuxiliaryGraphBuilder;
import Simulation.Parameters;
import Simulation.Simulation;

@SuppressWarnings("Duplicates") public class Algorithm {
  private final Network originalNetwork;
  private final Request request;
  private final Parameters parameters;
  private AuxiliaryNetwork auxiliaryNetwork;

  public Algorithm(Network originalNetwork, Request request, Parameters parameters) {
    this.originalNetwork = originalNetwork;
    this.request = request;
    this.parameters = parameters;
  }

  /**
   * Operational cost minimization without delay constraints
   */
  public Result minOpCostWithoutDelay() {
    CostFunction costFunction = new OperationalCostFunction();
    auxiliaryNetwork = AuxiliaryGraphBuilder.buildAuxiliaryGraph(originalNetwork, request, costFunction, parameters, true);
    if (auxiliaryNetwork == null) { //this means that some servers cannot be reached due to insufficient bandwidth
      Simulation.getLogger().trace("No bandwidth");
      return new Result.Builder().build(); //this generates a no-admittance result
    }
    ArrayList<Server> path = auxiliaryNetwork.findShortestPath();
    double finalPathCost = auxiliaryNetwork.calculatePathCost(path, costFunction);
    Result result = new Result.Builder().path(path)
                                        .pathCost(finalPathCost)
                                        .admit(true)
                                        .build();
    Simulation.getLogger().trace(result.toString());
    return result;
  }

  /**
   * Operational cost minimization with delay constraints
   */
  public Result minOpCostWithDelay() {
    CostFunction costFunction = new ExponentialCostFunction();
    auxiliaryNetwork = AuxiliaryGraphBuilder.buildAuxiliaryGraph(originalNetwork, request, costFunction, parameters, true);
    if (auxiliaryNetwork == null) { //this means that some servers cannot be reached due to insufficient bandwidth
      Simulation.getLogger().trace("No bandwidth");
      return new Result.Builder().build(); //this generates a no-admittance result
    }
    ArrayList<Server> path = auxiliaryNetwork.findDelayAwareShortestPath();
    double finalPathCost = auxiliaryNetwork.calculatePathCost(path, costFunction);
    Result result = new Result.Builder().path(path)
                                        .pathCost(finalPathCost)
                                        .admit(true)
                                        .build();
    Simulation.getLogger().trace(result.toString());
    return result;
  }

  /**
   * Throughput maximization without delay constraints
   */
  public Result maxThroughputWithoutDelay() { //s is source, t is sink
    auxiliaryNetwork = AuxiliaryGraphBuilder.buildAuxiliaryGraph(originalNetwork, request, parameters.costFunc, parameters, false);
    if (auxiliaryNetwork == null) { //this means that some servers cannot be reached due to insufficient bandwidth
      Simulation.getLogger().trace("No bandwidth");
      return new Result.Builder().build(); //this generates a no-admittance result
    }
    ArrayList<Server> path = auxiliaryNetwork.findShortestPath();
    double finalPathCost = auxiliaryNetwork.calculatePathCost(path, parameters.costFunc);
    boolean admit = admissionControl(finalPathCost);
    if (admit) {
      auxiliaryNetwork.admitRequestAndReserveResources(path);
    }
    Result result = new Result.Builder().path(path)
                                        .pathCost(finalPathCost)
                                        .admit(admit)
                                        .build();
    Simulation.getLogger().trace(result.toString());
    return result;
  }

  /**
   * Throughput maximization with delay constraints
   */
  public Result maxThroughputWithDelay() { //s is source, t is sink
    auxiliaryNetwork = AuxiliaryGraphBuilder.buildAuxiliaryGraph(originalNetwork, request, parameters.costFunc, parameters, false);
    if (auxiliaryNetwork == null) { //this means that some servers cannot be reached due to insufficient bandwidth
      Simulation.getLogger().trace("No bandwidth");
      return new Result.Builder().build(); //this generates a no-admittance result
    }
    ArrayList<Server> path = auxiliaryNetwork.findDelayAwareShortestPath();
    double finalPathCost = auxiliaryNetwork.calculatePathCost(path, parameters.costFunc);
    boolean admit = admissionControl(finalPathCost);
    if (admit) {
      auxiliaryNetwork.admitRequestAndReserveResources(path);
    }
    Result result = new Result.Builder().path(path)
                                        .pathCost(finalPathCost)
                                        .admit(admit)
                                        .build();
    Simulation.getLogger().trace(result.toString());
    return result;
  }

  private boolean admissionControl(double pathCost) {
    return pathCost < auxiliaryNetwork.size() * parameters.threshold - 1;
  }
}
