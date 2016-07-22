package Network;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public class Link {
  @NotNull private final Server s1;
  @NotNull private final Server s2;
  private double bandwidthCapacity; //bandwidth capacity
  private double allocatedBandwidth; //bandwidth being used
  private double operationalCost; //operation cost
  private double delay; //delay
  private double weight;

  public Link(@NotNull Server s1, @NotNull Server s2) {
    this.s1 = s1;
    this.s2 = s2;
    addLinkToServers();
  }

  private void addLinkToServers() {
    s1.addLink(this);
    s2.addLink(this);
  }

  Link(@NotNull Server s1, @NotNull Server s2, double bandwidthCapacity, double allocatedBandwidth, double delay, double operationalCost) {
    checkArgument(bandwidthCapacity >= 0d && allocatedBandwidth >= 0d && delay >= 0d && operationalCost >= 0d);

    this.s1 = s1;
    this.s2 = s2;
    this.bandwidthCapacity = bandwidthCapacity;
    this.allocatedBandwidth = allocatedBandwidth;
    this.delay = delay;
    this.operationalCost = operationalCost;
    addLinkToServers();
  }

  void wipe() {
    allocatedBandwidth = 0d;
  }

  @Override
  public String toString() {
    return this.s1.getId() + "<->" + this.s2.getId();
  }

  public double getResidualBandwidth() {
    return bandwidthCapacity - allocatedBandwidth;
  }

  public double getAllocatedBandwidth() {
    return allocatedBandwidth;
  }

  public void setDelay(double delay) {
    checkArgument(delay >= 0d);
    this.delay = delay;
  }

  public double getDelay() {
    return delay;
  }

  public void setBandwidthCapacity(double bandwidthCapacity) {
    checkArgument(bandwidthCapacity >= 0);
    this.bandwidthCapacity = bandwidthCapacity;
  }

  boolean canSupportBandwidth(double demand) {
    return selfLink() || allocatedBandwidth + demand < bandwidthCapacity;
  }

  public boolean selfLink() {
    return s1.getId() == s2.getId();
  }

  void allocateBandwidth(double demand) {
    if (!selfLink()) {
      checkState(getAllocatedBandwidth() + demand < getBandwidthCapacity());
      if (allocatedBandwidth + demand < bandwidthCapacity) {
        allocatedBandwidth += demand;
      }
    }
  }

  public double getOperationalCost() {
    if (selfLink()) {
      return 0d;
    }
    return operationalCost;
  }

  public void setOperationalCost(double operationalCost) {
    checkArgument(operationalCost >= 0);
    this.operationalCost = operationalCost;
  }

  @Nullable Server getLinkedServer(@NotNull Server s) {
    if (s == s1) {
      return s2;
    }
    if (s == s2) {
      return s1;
    } else {
      return null;
    }
  }

  @NotNull public Server getS1() {
    return s1;
  }

  @NotNull public Server getS2() {
    return s2;
  }

  public double getBandwidthCapacity() {
    return bandwidthCapacity;
  }

  public double getWeight() {
    return weight;
  }

  void setWeight(double weight) {
    if  (weight <0 ) {
      System.out.println("hwelrk");
    }
    checkArgument(weight >= 0d);
    this.weight = weight;
  }
}
