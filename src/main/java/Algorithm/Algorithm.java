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
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("Duplicates") public class Algorithm {
  @NotNull private final Network originalNetwork;
  @NotNull private final Request request;
  @NotNull private final Parameters parameters;

  public Algorithm(@NotNull Network originalNetwork, @NotNull Request request, @NotNull Parameters parameters) {
    this.originalNetwork = originalNetwork;
    this.request = request;
    this.parameters = parameters;
  }

  /**
   * Operational cost minimization without delay constraints
   */
  public Result minOpCostWithoutDelay() {
    Result.Builder builder = new Result.Builder();
    //CostFunction costFunction = new OperationalCostFunction();
    AuxiliaryNetwork auxiliaryNetwork = AuxiliaryGraphBuilder.buildAuxiliaryGraphOffline(originalNetwork, request, parameters.costFunc, parameters);
    if (auxiliaryNetwork == null) { //this means that some servers cannot be reached due to insufficient bandwidth
      builder.rejectionReason(Result.Reason.FAILED_TO_CONSTRUCT_AUX_GRAPH); //this generates a no-admittance result
    } else {
      ArrayList<Server> path = auxiliaryNetwork.findShortestPath();
      if (path.size() != request.getSC().length + 2) {
        builder.rejectionReason(Result.Reason.NO_PATH_AUX_GRAPH);
      }
      double finalPathCost = auxiliaryNetwork.calculatePathCost(path, parameters.costFunc);
      builder.path(path)
             .pathCost(finalPathCost)
             .admit(true);
    }
    Result result = builder.build();
    Simulation.getLogger().trace(result.toString());
    return result;
  }

  /**
   * Operational cost minimization with delay constraints
   */
  public Result minOpCostWithDelay() {
    Result.Builder builder = new Result.Builder();
    AuxiliaryNetwork auxiliaryNetwork = AuxiliaryGraphBuilder.buildAuxiliaryGraphOffline(originalNetwork, request, parameters.costFunc, parameters);
    if (auxiliaryNetwork == null) { //this means that some servers cannot be reached due to insufficient bandwidth
      builder.rejectionReason(Result.Reason.FAILED_TO_CONSTRUCT_AUX_GRAPH); //this generates a no-admittance result
    } else {
      ArrayList<Server> path = auxiliaryNetwork.findDelayAwareShortestPath();
      if (path == null) {
        builder.path(null).pathCost(Double.MAX_VALUE).rejectionReason(Result.Reason.NO_PATH_AUX_GRAPH);
      } else {
        double finalPathCost = auxiliaryNetwork.calculatePathCost(path, parameters.costFunc);
        if (Double.MAX_VALUE == finalPathCost) {
          builder.path(null).pathCost(Double.MAX_VALUE).rejectionReason(Result.Reason.NO_PATH_AUX_GRAPH);
        } else {
          builder.path(path).pathCost(finalPathCost).admit(true);
        }
      }
    }
    Result result = builder.build();
    Simulation.getLogger().trace(result.toString());
    return result;
  }

  /**
   * Throughput maximization without delay constraints
   */
  public Result maxThroughputWithoutDelay() { //s is source, t is sink
    Result.Builder builder = new Result.Builder();
    AuxiliaryNetwork auxiliaryNetwork = AuxiliaryGraphBuilder.buildAuxiliaryGraph(originalNetwork, request, parameters.costFunc, parameters);
    if (auxiliaryNetwork == null) { //this means that some servers cannot be reached due to insufficient bandwidth
      builder.rejectionReason(Result.Reason.FAILED_TO_CONSTRUCT_AUX_GRAPH); //this generates a no-admittance result
    } else {
      ArrayList<Server> path = auxiliaryNetwork.findShortestPath();
      if (path.size() != request.getSC().length + 2) {
        builder.rejectionReason(Result.Reason.NO_PATH_AUX_GRAPH);
      } else {
        double finalPathCost = auxiliaryNetwork.calculatePathCost(path, parameters.costFunc);
        boolean passAdmissionControl = admissionControlTest(finalPathCost);
        if (passAdmissionControl) {
          auxiliaryNetwork.admitRequestAndReserveResources(path);
        } else {
          builder.rejectionReason(Result.Reason.FAILED_ADMISSION_CONTROL);
        }
        builder.path(path)
               .pathCost(finalPathCost)
               .admit(passAdmissionControl);
      }
    }
    Result result = builder.build();
    Simulation.getLogger().trace(result.toString());
    return result;
  }

  /**
   * Throughput maximization with delay constraints
   */
  public Result maxThroughputWithDelay() { //s is source, t is sink
    Result.Builder builder = new Result.Builder();
    AuxiliaryNetwork auxiliaryNetwork = AuxiliaryGraphBuilder.buildAuxiliaryGraph(originalNetwork, request, parameters.costFunc, parameters);
    if (auxiliaryNetwork == null) { //this means that some servers cannot be reached due to insufficient bandwidth
      builder.rejectionReason(Result.Reason.FAILED_TO_CONSTRUCT_AUX_GRAPH); //this generates a no-admittance result
    } else {
      ArrayList<Server> path = auxiliaryNetwork.findDelayAwareShortestPath();
      if (path == null) {
        builder.rejectionReason(Result.Reason.NO_PATH_AUX_GRAPH);
      } else {
        double finalPathCost = auxiliaryNetwork.calculatePathCost(path, parameters.costFunc);
        boolean admit = admissionControlTest(finalPathCost);
        if (admit) {
          auxiliaryNetwork.admitRequestAndReserveResources(path);
        } else {
          builder.rejectionReason(Result.Reason.FAILED_ADMISSION_CONTROL);
        }
        builder.path(path)
               .pathCost(finalPathCost)
               .admit(admit);
      }
    }
    Result result = builder.build();
    Simulation.getLogger().trace(result.toString());
    return result;
  }

  private boolean admissionControlTest(double pathCost) {
    if (pathCost == Double.MAX_VALUE || pathCost == Double.POSITIVE_INFINITY) {
      return false;
    }
    return pathCost < (double) originalNetwork.size() * parameters.threshold - 1d;
  }
}
