import java.util.ArrayList;
import java.util.HashSet;

import Algorithm.CostFunctions.OpCostFunction;
import Network.AuxiliaryNetwork;
import Network.Link;
import Network.Network;
import Network.Request;
import Network.Server;
import NetworkGenerator.NetworkPathFinder;
import NetworkGenerator.NetworkValueSetter;
import Simulation.Parameters;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AuxiliaryNetworkTest {

  @Test
  public void testMinOpCostWithoutDelay() {
    //simple diamond shaped graph
    ArrayList<Server> servers = new ArrayList<>();
    ArrayList<Link> links = new ArrayList<>();

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
    l0_2.setOpCost(11);
    l1_3.setOpCost(15);
    l2_3.setOpCost(2);

    Network n = new Network(servers, links);
    NetworkValueSetter nvs = new NetworkValueSetter(n, parameters);
    nvs.setConstantLinkCapacity(1000);
    nvs.setConstantServerCapacity(10000, 1);

    s1.addVM(0);
    s3.addVM(1);

    Request r = new Request(0, s0, s3, new Parameters.Builder().build());
    r.setServiceChain(new int[] {0, 1});

    //nfvreq = {2, 3}
    //nfvcost = {2, 3}
    //nfvinit cost = {5, 6}
    AuxiliaryNetwork auxnet = new NetworkPathFinder().shortestPathsByCost(n, r, new OpCostFunction());
    auxnet.generateNetwork(true);
    Server src = auxnet.getSource();
    Server dest = auxnet.getDestination();
    ArrayList<HashSet<Server>> serviceLayers = auxnet.serviceLayers;
    assertEquals(2, serviceLayers.size());
    HashSet<Server> layer0 = serviceLayers.get(0);
    HashSet<Server> layer1 = serviceLayers.get(1);
    assertEquals(4, layer0.size());
    assertEquals(4, layer1.size());

    //each link represents shortest path between 2 nodes
    Server s3_0 = getServer(layer0, 3);
    assertEquals(13.0, s3_0.getLink(src).getPathCost(), 0.001);
    Server s0_0 = getServer(layer0, 0);
    assertEquals(0, s0_0.getLink(src).getPathCost(), 0.001);
    Server s1_0 = getServer(layer0, 1);
    Server s2_1 = getServer(layer1, 2);
    assertEquals(17, s1_0.getLink(s2_1).getPathCost(), 0.001);
    assertEquals(2, s2_1.getLink(dest).getPathCost(), 0.001);
  }

  private Server getServer(HashSet<Server> servers, int id) {
    for (Server s : servers) {
      if (s.getId() == id) {
        return s;
      }
    }
    return null;
  }
}
