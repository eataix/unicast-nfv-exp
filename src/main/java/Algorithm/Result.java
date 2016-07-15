package Algorithm;

import java.util.ArrayList;

import Network.Server;

public class Result {
  private ArrayList<Server> path;
  public final double pathCost;
  public boolean admit;

  Result(ArrayList<Server> p, double pc, boolean a) {
    path = p;
    pathCost = pc;
    admit = a;
  }

  Result(ArrayList<Server> p, double pc) {
    path = p;
    pathCost = pc;
  }

  Result() {
    admit = false;
    pathCost = Double.MAX_VALUE;
  }

  public ArrayList<Server> getPath() {
    return path;
  }
}
