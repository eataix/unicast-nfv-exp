package Algorithm;

import java.util.ArrayList;

import Network.Server;

public class Result {
  enum Reason {
    OK,
    FAILED_TO_CONSTRUCT_AUX_GRAPH,
    NO_PATH_AUX_GRAPH,
    FAILED_ADMISSION_CONTROL
  }

  private final ArrayList<Server> path;
  private final double pathCost;
  private final boolean admit;
  private final Result.Reason rejectionReason;

  private Result(ArrayList<Server> p, double pc, boolean a, Result.Reason reason) {
    path = p;
    pathCost = pc;
    admit = a;
    this.rejectionReason = reason;
  }

  public ArrayList<Server> getPath() {
    return path;
  }

  public boolean isAdmitted() {
    return admit;
  }

  public double getPathCost() {
    return pathCost;
  }

  static class Builder {
    private ArrayList<Server> path = null;
    private double pathCost = Double.MAX_VALUE;
    private boolean admit = false;
    private Result.Reason rejectionReason = Reason.OK;

    Builder path(ArrayList<Server> path) {
      this.path = path;
      return this;
    }

    Builder pathCost(double pathCost) {
      this.pathCost = pathCost;
      return this;
    }

    Builder admit(boolean admit) {
      this.admit = admit;
      return this;
    }

    Builder rejectionReason(Result.Reason rejectionReason) {
      this.rejectionReason = rejectionReason;
      return this;
    }

    Result build() {
      return new Result(path, pathCost, admit, rejectionReason);
    }
  }

  @Override public String toString() {
    return "Result{" +
        "path=" + path +
        ", pathCost=" + pathCost +
        ", admit=" + admit +
        ", rejectionReason=" + rejectionReason +
        '}';
  }
}
