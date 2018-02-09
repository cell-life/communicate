package org.celllife.jdk.utilities.runtimeinformation;

import java.util.List;

public abstract interface RuntimeInformation {
	public abstract long getTotalHeapMemory();

	public abstract long getTotalHeapMemoryUsed();

	public abstract String getJvmInputArguments();

	public abstract List<MemoryInformation> getMemoryPoolInformation();

	public abstract long getTotalPermGenMemory();

	public abstract long getTotalPermGenMemoryUsed();

	public abstract long getTotalNonHeapMemory();

	public abstract long getTotalNonHeapMemoryUsed();
}