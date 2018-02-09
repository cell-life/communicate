package org.celllife.mobilisr.startup;

import java.util.List;

import org.celllife.jdk.utilities.runtimeinformation.MemoryInformation;

public abstract interface SystemInfoUtils {

	long getTotalMemory();

	long getFreeMemory();

	long getUsedMemory();

	List<MemoryInformation> getMemoryPoolInformation();

	long getTotalPermGenMemory();

	long getFreePermGenMemory();

	long getUsedPermGenMemory();

	long getTotalNonHeapMemory();

	long getFreeNonHeapMemory();

	long getUsedNonHeapMemory();

	String getJvmInputArguments();
}