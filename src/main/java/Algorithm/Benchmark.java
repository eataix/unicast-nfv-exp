package Algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Algorithm.CostFunctions.CostFunction;
import Algorithm.CostFunctions.OperationalCostFunction;
import Network.Link;
import Network.Network;
import Network.Request;
import Network.Server;
import Simulation.Parameters;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

public class Benchmark {

  private final Network originalNetwork;
  private final Request request;
  private final Parameters parameters;
  private SimpleWeightedGraph<Server, DefaultWeightedEdge> weightedGraph;
  private HashMap<DefaultWeightedEdge, Link> linkEdgeMap = new HashMap<DefaultWeightedEdge, Link>();

  public Benchmark(Network originalNetwork, Request request, Parameters parameters) {
    this.originalNetwork = originalNetwork;
    this.request = request;
    this.parameters = parameters;
  }

  public Result benchmarkNFVUnicast() {

    Server source = this.request.getSource();
    Server destination = this.request.getDestination();

    double minCost = Double.MAX_VALUE;
    for (Server potentialServer : this.originalNetwork.getServers()) {
      if (0 == potentialServer.getCapacity()) {
        continue;
      }

      ArrayList<Link> linksSToS = this.shortestPath(source, potentialServer);
      ArrayList<Link> linksSToD = this.shortestPath(potentialServer, destination);
      double path1Cost = 0d;
      for (Link link : linksSToS) {
        path1Cost += parameters.costFunc.getCost(link, this.request.getBandwidth(), parameters);
      }

      double path2Cost = 0d;
      for (Link link : linksSToD) {
        path2Cost += parameters.costFunc.getCost(link, this.request.getBandwidth(), parameters);
      }

      double serverCost = 0d;
      for (int nfv : this.request.getSC()) {
        serverCost += potentialServer.getOperationalCost(nfv);
      }

      if ((path1Cost + path2Cost + serverCost) < minCost) {
        minCost = path1Cost + path2Cost + serverCost;
      }
    }

    return new Result.Builder()
        .pathCost(minCost)
        .admit(true)
        .build();
  }

  private ArrayList<Link> shortestPath(Server source, Server destination) {

    if (source.equals(destination)) {
      return new ArrayList<Link>();
    }

    if (null == this.weightedGraph) {
      // build a weighted graph according to network.
      this.weightedGraph = new SimpleWeightedGraph<Server, DefaultWeightedEdge>(DefaultWeightedEdge.class);

      for (Server server : this.getOriginalNetwork().getServers()) {
        this.weightedGraph.addVertex(server);
      }

      for (Link link : this.getOriginalNetwork().getLinks()) {
        Server s1 = link.getS1();
        Server s2 = link.getS2();
        DefaultWeightedEdge edge = this.weightedGraph.addEdge(s1, s2);
        this.weightedGraph.setEdgeWeight(edge, parameters.costFunc.getCost(link, this.request.getBandwidth(), parameters));
        linkEdgeMap.put(edge, link);
      }
    }

    DijkstraShortestPath<Server, DefaultWeightedEdge> shortestPath =
        new DijkstraShortestPath<Server, DefaultWeightedEdge>(this.weightedGraph, source, destination);
    List<DefaultWeightedEdge> sLinks = shortestPath.getPathEdgeList();
    if (sLinks.isEmpty()) {
      System.out.println("ERROR 2 : shortest path should exist!");
    }

    ArrayList<Link> linksInSPath = new ArrayList<Link>();
    //double pathCost = 0d;
    for (DefaultWeightedEdge edge : sLinks) {
      linksInSPath.add(this.linkEdgeMap.get(edge));
    }

    return linksInSPath;
  }

  public Result benchmarkNFVUnicastDelay() {

    Server source = this.request.getSource();
    Server destination = this.request.getDestination();

    double minCost = Double.MAX_VALUE;
    Server minCostServer = null;

    for (Server potentialServer : this.originalNetwork.getServers()) {
      if (0 == potentialServer.getCapacity()) {
        continue;
      }

      ArrayList<Link> linksSToS = this.shortestPath(source, potentialServer);
      ArrayList<Link> linksSToD = this.shortestPath(potentialServer, destination);
      double path1Cost = 0d;
      double path1Delay = 0d;
      for (Link link : linksSToS) {
        path1Cost += parameters.costFunc.getCost(link, this.request.getBandwidth(), parameters);
        path1Delay += link.getDelay();
      }

      double path2Cost = 0d;
      double path2Delay = 0d;
      for (Link link : linksSToD) {
        path2Cost += parameters.costFunc.getCost(link, this.request.getBandwidth(), parameters);
        path2Delay += link.getDelay();
      }

      double serverCost = 0d;
      double processingDelay = 0d;
      for (int nfv : this.request.getSC()) {
        serverCost += potentialServer.getOperationalCost(nfv);
        
        processingDelay += parameters.nfvProcessingDelays[nfv];
        if (!potentialServer.canReuseVM(nfv)) {
        	processingDelay += parameters.nfvInitDelays[nfv];
        }
      }

      if ((path1Delay + path2Delay + processingDelay) <= this.request.getDelayReq()) {
        if ((path1Cost + path2Cost + serverCost) < minCost) {
          minCost = path1Cost + path2Cost + serverCost;
          minCostServer = potentialServer;
        }
      }
    }

    if (null == minCostServer)// reject the request
    {
      return new Result.Builder()
          .pathCost(minCost)
          .admit(false)
          .build();
    } else {
      return new Result.Builder()
          .pathCost(minCost)
          .admit(true)
          .build();
    }
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
