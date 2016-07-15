package tests.java;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import main.java.Algorithm.CostFunctions.OpCostFunction;
import main.java.Network.AuxiliaryNetwork;
import main.java.Network.Link;
import main.java.Network.Network;
import main.java.Network.Request;
import main.java.Network.Server;
import main.java.NetworkGenerator.NetworkPathFinder;

public class NetworkPathFinderTest {
	@Test
	public void testShortestPathsByCost() {
		//simple diamond shaped graph
		ArrayList<Server> servers = new ArrayList<Server>();
		ArrayList<Link> links = new ArrayList<Link>();

		Server s0 = new Server(0); servers.add(s0);
		Server s1 = new Server(1); servers.add(s1);
		Server s2 = new Server(2); servers.add(s2);
		Server s3 = new Server(3); servers.add(s3);

		Link l0_1 = new Link(s0, s1); links.add(l0_1);
		Link l0_2 = new Link(s0, s2); links.add(l0_2);
		Link l1_3 = new Link(s1, s3); links.add(l1_3);
		Link l2_3 = new Link(s2, s3); links.add(l2_3);

		l0_1.setOpCost(8);
		l0_2.setOpCost(10);
		l1_3.setOpCost(15);
		l2_3.setOpCost(2);

		Network n = new Network(servers, links);
		AuxiliaryNetwork auxnet = new NetworkPathFinder().shortestPathsByCost(n, new Request(2, s0, s3),  new OpCostFunction());
		ArrayList<Link>path = auxnet.getLinkPath(s0, s3);
		assertEquals(2, path.size());
		assertEquals(l0_2, path.get(0));
		assertEquals(l2_3, path.get(1));

		path = auxnet.getLinkPath(s1, s2);
		assertEquals(2, path.size());
		assertEquals(l1_3, path.get(0));
		assertEquals(l2_3, path.get(1));
	}
}
