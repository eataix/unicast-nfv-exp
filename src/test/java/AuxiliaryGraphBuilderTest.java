import java.util.ArrayList;

import Algorithm.CostFunctions.OperationalCostFunction;
import Network.AuxiliaryNetwork;
import Network.Link;
import Network.Network;
import Network.Request;
import Network.Server;
import NetworkGenerator.AuxiliaryGraphBuilder;
import Simulation.Parameters;
import Simulation.Simulation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AuxiliaryGraphBuilderTest {
  @Test
  public void testShortestPathsByCost() {
    //simple diamond shaped graph
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
    AuxiliaryNetwork auxnet = AuxiliaryGraphBuilder.buildAuxiliaryGraph(n, new Request(s0, s3, Simulation.defaultParameters), new OperationalCostFunction(),
                                                                        Simulation.defaultParameters);
    ArrayList<Link> path = auxnet.getLinkPath(s0, s3);
    assertEquals(2, path.size());
    assertEquals(l0_2, path.get(0));
    assertEquals(l2_3, path.get(1));

    path = auxnet.getLinkPath(s1, s2);
    assertEquals(2, path.size());
    assertEquals(l1_3, path.get(0));
    assertEquals(l2_3, path.get(1));
  }
}
