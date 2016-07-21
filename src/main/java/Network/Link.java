package Network;

public class Link {
  private final Server s1;
  private final Server s2;
  private double bandwidthCapacity; //bandwidth capacity
  private double allocatedBandwidth; //bandwidth being used
  private int operationalCost; //operation cost
  private double delay; //delay
  private double weight;

  public Link(Server s1, Server s2) {
    this.s1 = s1;
    this.s2 = s2;
    addLinkToServers();
  }

  private void addLinkToServers() {
    s1.addLink(this);
    s2.addLink(this);
  }

  Link(Server s1, Server s2, double bandwidthCapacity, double allocatedBandwidth, double delay, int operationalCost) {
    this.s1 = s1;
    this.s2 = s2;
    this.bandwidthCapacity = bandwidthCapacity;
    this.allocatedBandwidth = allocatedBandwidth;
    this.delay = delay;
    this.operationalCost = operationalCost;
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

  public double getResidualBandwidth() {
    return bandwidthCapacity - allocatedBandwidth;
  }

  public double getBandwidth() {
    return bandwidthCapacity;
  }

  public double getAllocatedBandwidth() {
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

  public Server getS1() {
    return s1;
  }

  public Server getS2() {
    return s2;
  }

  public double getBandwidthCapacity() {
    return bandwidthCapacity;
  }

  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }
}
