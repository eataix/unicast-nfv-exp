package tests.java;

import java.util.ArrayList;

import main.java.Algorithm.Algorithm;
import main.java.Algorithm.Result;
import main.java.Network.Link;
import main.java.Network.Network;
import main.java.Network.Request;
import main.java.Network.Server;
import main.java.NetworkGenerator.NetworkValueSetter;
import main.java.Simulation.Parameters;
import org.junit.Test;

import static org.junit.Assert.*;

public class AlgorithmTest {

  @Test
  public void testMinOpCostWithoutDelay() {
    //simple diamond shaped graph
    Parameters.importParameters("test1.txt");
    ArrayList<Server> servers = new ArrayList<Server>();
    ArrayList<Link> links = new ArrayList<Link>();

    Server s0 = new Server(0);
    servers.add(s0);
    Server s1 = new Server(1);
    servers.add(s1);
    Server s2 = new Server(2);
    servers.add(s2);
    Server s3 = new Server(3);
    servers.add(s3);

    Link l0_1 = new Link(s0, s1);
    links.add(l0_1);
    Link l0_2 = new Link(s0, s2);
    links.add(l0_2);
    Link l1_3 = new Link(s1, s3);
    links.add(l1_3);
    Link l2_3 = new Link(s2, s3);
    links.add(l2_3);

    l0_1.setOpCost(8);
    l0_2.setOpCost(10);
    l1_3.setOpCost(15);
    l2_3.setOpCost(2);

    Network n = new Network(servers, links);
    NetworkValueSetter nvs = new NetworkValueSetter(n);
    nvs.setConstantLinkCapacity(1000);
    nvs.setConstantServerCapacity(10000, 1);

    s1.addVM(0);
    s3.addVM(1);

    Request r = new Request(0, s0, s3);
    r.setServiceChain(new int[] {0, 1});

    //nfvreq = {2, 3}
    //nfvcost = {2, 3}
    //nfvinit cost = {5, 6}
    Algorithm alg = new Algorithm(n, r);
    Result res = alg.minOpCostWithoutDelay();
    ArrayList<Server> path = res.getPath();
    assertEquals(s0.getId(), path.get(0).getId());
    assertEquals(s2.getId(), path.get(1).getId());
    assertEquals(s3.getId(), path.get(2).getId());
    assertEquals(s3.getId(), path.get(3).getId());
    System.out.println("Path cost = " + res.pathCost); //should be 22.0
  }
}
