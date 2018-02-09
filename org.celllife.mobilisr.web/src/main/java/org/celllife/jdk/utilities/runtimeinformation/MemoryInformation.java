package org.celllife.jdk.utilities.runtimeinformation;

public abstract interface MemoryInformation
{
  public abstract String getName();

  public abstract long getTotal();

  public abstract long getUsed();

  public abstract long getFree();
}