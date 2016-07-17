package Algorithm;

import java.util.ArrayList;

import Network.Server;

public class Result {
  private final ArrayList<Server> path;
  private final double pathCost;
  private final boolean admit;

  private Result(ArrayList<Server> p, double pc, boolean a) {
    path = p;
    pathCost = pc;
    admit = a;
  }

  public ArrayList<Server> getPath() {
    return path;
  }

  public boolean isAdmit() {
    return admit;
  }

  public double getPathCost() {
    return pathCost;
  }

  static class Builder {
    private ArrayList<Server> path = null;
    private double pathCost = Double.MAX_VALUE;
    private boolean admit = false;

    public Builder path(ArrayList<Server> path) {
      this.path = path;
      return this;
    }

    public Builder pathCost(double pathCost) {
      this.pathCost = pathCost;
      return this;
    }

    public Builder admit(boolean admit) {
      this.admit = admit;
      return this;
    }

    public Result build() {
      return new Result(path, pathCost, admit);
    }
  }
}
