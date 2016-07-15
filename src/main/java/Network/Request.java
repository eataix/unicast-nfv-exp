package Network;

import java.util.HashSet;

import Simulation.Parameters;

public class Request {
  public int delay; //delay bound requirement
  public int[] SC; //each number points to index of nfv in Parameters
  public final int bandwidth;
  final Server src;
  final Server dst;

  public Request(int b, Server s, Server t) {
    bandwidth = b;
    src = s;
    dst = t;
    generateServiceChain();
  }

  public boolean setServiceChain(int[] sc) {
    //make sure all elements are valid nfv ids, and there are no repeats.
    HashSet<Integer> nfvs = new HashSet<>();
    for (int aSc : sc) {
      if (aSc >= Parameters.L || nfvs.contains(aSc)) {
        return false;
      } else {
        nfvs.add(aSc);
      }
    }
    SC = sc;
    return true;
  }

  public Server getSource() {
    return src;
  }

  public Server getDest() {
    return dst;
  }

  private void generateServiceChain() {
    //create randomly ordered list of NFVs
    int[] nfvlist = new int[Parameters.L];
    for (int i = 0; i < Parameters.L; i++)
      nfvlist[i] = i;
    //fisher-yates shuffle
    for (int i = 0; i < Parameters.L; i++) {
      int temp = nfvlist[i];
      int index = (int) Math.floor(Math.random() * (Parameters.L - i) + i);
      nfvlist[i] = nfvlist[index];
      nfvlist[index] = temp;
    }

    int l = (int) Math.floor(Math.random() * (Parameters.L - 1) + 1); //ensure there is at least one service in the service chain

    SC = new int[l];
    System.arraycopy(nfvlist, 0, SC, 0, l);
  }
}
