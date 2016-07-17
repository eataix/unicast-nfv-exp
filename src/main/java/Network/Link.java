package Network;

public class Link {
  private final Server s1;
  private final Server s2;
  private int bandwidthCapacity; //bandwidth capacity
  private int allocatedBandwidth; //bandwidth being used
  private int operationalCost; //operation cost
  private double delay; //delay
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
    this.delay = delay;
    operationalCost = opcost;
  }

  public Link(Link oLink) {
    this(oLink.getS1(), oLink.getS2(), oLink.getBandwidth(), oLink.getAllocatedBandwidth(), oLink.getDelay(), oLink.getOperationalCost());
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
    delay = D;
  }

  public double getDelay() {
    return delay;
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

  void setPathCost(double pathCost) {
    this.pathUtCost = pathCost;
  }

  public int getOperationalCost() {
    if (s1 == s2) {
      return 0;
    }
    return operationalCost;
  }

  public void setOperationalCost(int cost) {
    operationalCost = cost;
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

  private Server getS1() {
    return s1;
  }

  private Server getS2() {
    return s2;
  }

  public int getBandwidthCapacity() {
    return bandwidthCapacity;
  }

  public double getPathUtCost() {
    return pathUtCost;
  }
}
