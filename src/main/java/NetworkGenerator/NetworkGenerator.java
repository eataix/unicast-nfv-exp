package NetworkGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import Network.Link;
import Network.Network;
import Network.Server;

public class NetworkGenerator {

  // use "networkIndexPostFix" to either denote the number of the generated network, or the name of a real topology, such as "GEANT", "AS1755"
  public Network generateRealNetworks(int n, String networkIndexPostFix) {
    ArrayList<Server> servers = new ArrayList<>();
    ArrayList<Link> links = new ArrayList<>();

    String fileName = null;

    if (networkIndexPostFix.equals("GEANT") || networkIndexPostFix.equals("AS1755") || networkIndexPostFix.equals("AS4755")) {
      fileName = ".//data//" + networkIndexPostFix + ".txt";
    } else {
    	if (!networkIndexPostFix.equals("0"))
    		fileName = ".//data//" + n + "-25-25-" + networkIndexPostFix + ".txt";
    	else 
    		fileName = ".//data//" + n + "-25-25" + ".txt";
    }

    try {
      File file = new File(fileName);
      BufferedReader reader = new BufferedReader(new FileReader(file));

      String lineString = null;
      int readStatus = -1; // 0: reading vertices data; 1: reading edges data
      int numOfNodeRead = 0;
      while ((lineString = reader.readLine()) != null) {
        if (lineString.startsWith("#")) {
          continue;
        }

        if (lineString.contains("VERTICES")) {//start to parse vertices data
          readStatus = 0;
          continue;
        } else if (lineString.contains("EDGES")) {
          readStatus = 1;
          continue;
        }
        if (0 == readStatus) {
          lineString = lineString.trim();
          String[] attrs = lineString.split(" ");

          int id = Integer.parseInt(attrs[0]);
          numOfNodeRead++;

          servers.add(new Server(id));
        }

        if (1 == readStatus) {
          lineString = lineString.trim();
          String[] attrs = lineString.split(" ");

          int fromNodeId = Integer.parseInt(attrs[0]);
          int toNodeId = Integer.parseInt(attrs[1]);
          Server s1 = null;
          Server s2 = null;

          for (Server server : servers) {
            if (server.getId() == fromNodeId) {
              s1 = server;
            } else if (server.getId() == toNodeId) {
              s2 = server;
            }
          }

          Link l = new Link(s1, s2);
          links.add(l);
        }
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return new Network(servers, links);
  }

  public Network barabasiAlbertNetwork(int n, int l) { //Barabasi-Albert Model - n is number of nodes. Servers are added one at a time
    ArrayList<Server> servers = new ArrayList<>();
    ArrayList<Link> links = new ArrayList<>();
    //create seed network
    Server s1 = new Server(0);
    servers.add(s1);
    Server s2 = new Server(1);
    servers.add(s2);
    Link li = new Link(s1, s2);
    links.add(li);
    int id = 2;
    //add server, and link server with existing server based on node degree distribution.
    while (id < n) {
      Server next = new Server(id);
      servers.add(next);
      for (int i = 0; i < l; i++) { //each new server links to L other servers.
        int degrees = (links.size() - next.getDegree()) * 2; // total number of node degrees
        int index = (int) Math.floor(Math.random() * degrees);
        boolean addedLink = false;
        for (Server s : servers) {
          if (s == next) {
            continue;
          }
          index -= s.getDegree();
          if (index <= 0) { //connect to this server and break
            Link link = new Link(next, s);
            links.add(link);
            addedLink = true;
            break;
          }
        }
        if (!addedLink) { //add a link to a random server
          Server s = servers.get((int) Math.floor(Math.random() * servers.size()));
          Link link = new Link(next, s);
          links.add(link);
        }
        id++;
      }
    }
    return new Network(servers, links);
  }

  public Network randomGraphNetwork(int n, double k) { //Random Graph Model - n is number of nodes, k is average degree of each node
    ArrayList<Server> servers = new ArrayList<>();
    ArrayList<Link> links = new ArrayList<>();

    //create servers
    for (int i = 0; i < n; i++) {
      servers.add(new Server(i));
    }
    ArrayList<Server> traversed = new ArrayList<>();

    //create links
    for (Server s1 : servers) {
      for (Server s2 : servers) {
        if (s1 == s2 || traversed.contains(s1)) {
          continue;
        }
        if (Math.random() < k / (n - 1)) {//we create a link between these two servers
          Link l = new Link(s1, s2);
          links.add(l);
        }
      }
      traversed.add(s1);
    }

    //connect disjoint servers
    ArrayList<Server> disconnected = new ArrayList<>(servers);
    while (disconnected.size() > 0) {
      ArrayList<Server> queue = new ArrayList<>();
      ArrayList<Server> connected = new ArrayList<>();
      queue.add(servers.get(0));
      while (!queue.isEmpty()) {
        Server curr = queue.remove(0);
        disconnected.remove(curr);
        connected.add(curr);
        queue.addAll(curr.getAllNeighbours());
      }
      //connect a server from disconnected to a random connected server
      Server s1 = disconnected.remove(0);
      int index = (int) Math.floor(Math.random() * connected.size());
      Server s2 = connected.get(index);
      Link l = new Link(s1, s2);
      links.add(l);
    }
    return new Network(servers, links);
  }

  public Network testNetwork() {
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

    l0_1.setOperationalCost(8);
    l0_2.setOperationalCost(10);
    l1_3.setOperationalCost(15);
    l2_3.setOperationalCost(2);

    return new Network(servers, links);
  }

  // unit tests
  public static void main(String[] s) {
    NetworkGenerator netGen = new NetworkGenerator();
    Network net = netGen.generateRealNetworks(-1, "GEANT");
    System.out.println(net.toString());
  }
}
