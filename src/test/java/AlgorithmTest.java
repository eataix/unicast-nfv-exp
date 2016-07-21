import java.util.ArrayList;

import Algorithm.Algorithm;
import Algorithm.Result;
import Network.Link;
import Network.Network;
import Network.Request;
import Network.Server;
import NetworkGenerator.NetworkValueSetter;
import Simulation.Parameters;
import Simulation.Simulation;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class AlgorithmTest {

  @Test
  public void testMinOpCostWithoutDelay() {
    //simple diamond shaped graph
    //Parameters.importParameters("test1.txt");
    ArrayList<Server> servers = new ArrayList<>();
    ArrayList<Link> links = new ArrayList<>();

    Parameters parameters = new Parameters.Builder().L(2).build();

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

    l0_1.setOperationalCost(8);
    l0_2.setOperationalCost(10);
    l1_3.setOperationalCost(15);
    l2_3.setOperationalCost(2);

    Network n = new Network(servers, links);
    NetworkValueSetter nvs = new NetworkValueSetter(n, Simulation.baseParameters);
    nvs.setConstantLinkCapacity(1000);
    nvs.setConstantServerCapacity(10000, 1d);

    s1.addVM(0);
    s0.addVM(1);

    Request r = new Request(s0, s3, Simulation.baseParameters);
    r.setServiceChain(new int[] {0, 1});

    //nfvreq = {2, 3}
    //nfvcost = {2, 3}
    //nfvinit cost = {5, 6}
    Algorithm alg = new Algorithm(n, r, Simulation.baseParameters);
    Result res = alg.minOpCostWithoutDelay();
    ArrayList<Server> path = res.getPath();
    assertEquals(0, path.get(0).getId());
    assertEquals(0, path.get(1).getId());
    assertEquals(0, path.get(2).getId());
    assertEquals(3, path.get(3).getId());
    assertEquals(22, res.getPathCost(), 0.01);
  }
}
