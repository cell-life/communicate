package org.celllife.jdk.utilities.runtimeinformation;

import java.util.Collections;
import java.util.List;

class RuntimeInformationStub
  implements RuntimeInformation
{
  public String getJvmInputArguments()
  {
    return "Unknown";
  }

  public long getTotalHeapMemory()
  {
    return 0L;
  }

  public long getTotalHeapMemoryUsed()
  {
    return 0L;
  }

  public List<MemoryInformation> getMemoryPoolInformation()
  {
    return Collections.emptyList();
  }

  public long getTotalPermGenMemory()
  {
    return 0L;
  }

  public long getTotalPermGenMemoryUsed()
  {
    return 0L;
  }

  public long getTotalNonHeapMemory()
  {
    return 0L;
  }

  public long getTotalNonHeapMemoryUsed()
  {
    return 0L;
  }
}
