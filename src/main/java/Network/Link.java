package Network;

public class Link {
  private final Server s1;
  private final Server s2;
  private int bandwidthCapacity; //bandwidth capacity
  private int allocatedBandwidth; //bandwidth being used
  private int opCost; //operation cost
  private double d; //delay
  private double pathUtCost; //utilization cost of a link representing a shortest path.

  public Link(Server svr1, Server svr2) {
    s1 = svr1;
    s2 = svr2;
    addLinkToServers();
  }

  private void addLinkToServers() {
    s1.addLink(this);
    s2.addLink(this);
  }

  private Link(Server svr1, Server svr2, int BW, int bw, double delay, int opcost) {
    s1 = svr1;
    s2 = svr2;
    bandwidthCapacity = BW;
    allocatedBandwidth = bw;
    d = delay;
    opCost = opcost;
  }

  public Link clone() {
    return new Link(s1, s2, bandwidthCapacity, allocatedBandwidth, d, opCost);
  }

  public Link(Server s) { //use this constructor to create a self link
    bandwidthCapacity = 20; //default value
    s1 = s;
    s2 = s;
  }

  void wipe() {
    allocatedBandwidth = 0;
  }

  @Override
  public String toString() {
    return this.s1.getId() + "<->" + this.s2.getId();
  }

  public int getResidualBandwidth() {
    return bandwidthCapacity - allocatedBandwidth;
  }

  public int getBandwidth() {
    return bandwidthCapacity;
  }

  public int getAllocatedBandwidth() {
    return allocatedBandwidth;
  }

  public void setDelay(double D) {
    d = D;
  }

  public double getDelay() {
    return d;
  }

  public void setBandwidth(int bw) {
    bandwidthCapacity = bw;
  }

  boolean canSupportBandwidth(double d) {
    return allocatedBandwidth + d < bandwidthCapacity;
  }

  private boolean selfLink() {
    return s1 == s2;
  }

  void allocateBandwidth(double d) {
    if (allocatedBandwidth + d < bandwidthCapacity) {
      allocatedBandwidth += d;
    }
  }

  public double getPathCost() { //to be used in the algorithm section
    return pathUtCost;
  }

  void setPathCost(double pathcost) {
    pathUtCost = pathcost;
  }

  public double getOperationalCost() {
    if (s1 == s2) {
      return 0;
    }
    return opCost;
  }

  public void setOpCost(int cost) {
    opCost = cost;
  }

  Server getLinkedServer(Server s) {
    if (s == s1) {
      return s2;
    }
    if (s == s2) {
      return s1;
    } else {
      return null;
    }
  }
}
