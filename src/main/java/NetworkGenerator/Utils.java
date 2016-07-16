package NetworkGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import Network.Link;
import Network.Network;
import Network.Server;

public class Utils {

  public interface LinkCostFunction {
    double getCost(Link l);
  }

  /**
   * @param s source
   * @param t destination
   * @param delay maximum delay
   * @return A path from @s to @t
   */
  static ArrayList<Link> LARAC(Network network, Server s, Server t, LinkCostFunction c, LinkCostFunction d, double delay) {
    ArrayList<Link> path = new ArrayList<>();

    ReturnVal rvc = Dijkstra(network, s, t, c);
    if (rvc.delay < delay) {
      return path;
    }

    ReturnVal rvd = Dijkstra(network, s, t, d);
    if (rvd.delay > delay) {
      return null;
    }
    while (true) {
      double lambda = (rvc.cost - rvd.cost) / (rvd.delay - rvc.delay);
      ReturnVal rvr = Dijkstra(network, s, t, c);
    }
  }

  static ReturnVal Dijkstra(Network network, Server src, Server dest, LinkCostFunction costFunction) {
    final HashMap<Server, Double> dist = new HashMap<>();
    HashMap<Server, Server> prev = new HashMap<>();
    dist.put(src, 0.0);

    PriorityQueue<Server> Q = new PriorityQueue<>(network.getServers().size(), new Comparator<Server>() {
      @Override public int compare(Server s1, Server s2) {
        return dist.get(s1).compareTo(dist.get(s2));
      }
    });

    for (Server s : network.getServers()) {
      if (s == src) {
        continue;
      }

      dist.put(s, Double.MAX_VALUE);
      prev.put(s, null);

      Q.add(s);
    }

    while (!Q.isEmpty()) {
      Server u = Q.poll();

      for (Server neighbour : u.getAllNeighbours()) {
        Link l = u.getLink(neighbour);
        double alt = dist.get(u) + costFunction.getCost(l);
        if (alt < dist.get(neighbour)) {
          dist.put(neighbour, alt);
          prev.put(neighbour, u);
          Q.remove(neighbour);
          Q.add(neighbour);
        }
      }
    }

    if (dist.get(dest) == Double.MAX_VALUE) {
      return null;
    }

    double delay = 0;
    Server curr = dest;
    ArrayList<Link> shortestPath = new ArrayList<>();
    while (prev.get(curr) != null) {
      Link l = curr.getLink(prev.get(curr));
      shortestPath.add(l);
      delay += l.getDelay();
      curr = prev.get(curr);
    }
    Collections.reverse(shortestPath);
    return new ReturnVal(shortestPath, delay, dist.get(dest));
  }

  static class ReturnVal {
    ArrayList<Link> shortestPath;
    double delay;
    double cost;

    ReturnVal(ArrayList<Link> shortestPath, double delay, double cost) {
      this.shortestPath = shortestPath;
      this.delay = delay;
      this.cost = cost;
    }
  }
}
