package org.celllife.mobilisr.startup;

import org.celllife.jdk.utilities.runtimeinformation.MemoryInformation;
import org.celllife.jdk.utilities.runtimeinformation.RuntimeInformation;
import org.celllife.jdk.utilities.runtimeinformation.RuntimeInformationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class SystemInfoUtilsImpl implements SystemInfoUtils {
	private static final Logger log = LoggerFactory
			.getLogger(SystemInfoUtilsImpl.class);
	private final Runtime rt;
	private final RuntimeInformation runtimeInformation;

	public SystemInfoUtilsImpl() {
		this.rt = Runtime.getRuntime();

		this.runtimeInformation = RuntimeInformationFactory
				.getRuntimeInformation();
	}

	@Override
	public long getTotalMemory() {
		long totalMemory = this.rt.maxMemory();
		return totalMemory / 1048576L;
	}

	@Override
	public long getFreeMemory() {
		long freeMemory = this.rt.maxMemory() - this.rt.totalMemory()
				+ this.rt.freeMemory();
		return freeMemory / 1048576L;
	}

	@Override
	public long getUsedMemory() {
		return getTotalMemory() - getFreeMemory();
	}

	@Override
	public List<MemoryInformation> getMemoryPoolInformation() {
		List<MemoryInformation> list = this.runtimeInformation.getMemoryPoolInformation();
		List<MemoryInformation> validList = new ArrayList<MemoryInformation>();
		for (MemoryInformation memoryInfo : list) {
			try {
				log.debug("Checking memory pool info is ok for: "
						+ memoryInfo.getName());

				memoryInfo.toString();

				validList.add(memoryInfo);
			} catch (RuntimeException e) {
				log.warn("Memory pool info returned by the java runtime is invalid for pool - "
						+ memoryInfo.getName());
				log.debug(e.getMessage(), e);
			}
		}
		return validList;
	}

	@Override
	public long getTotalPermGenMemory() {
		return this.runtimeInformation.getTotalPermGenMemory() / 1048576L;
	}

	@Override
	public long getFreePermGenMemory() {
		long freeMemory = this.runtimeInformation.getTotalPermGenMemory()
				- this.runtimeInformation.getTotalPermGenMemoryUsed();
		return freeMemory / 1048576L;
	}

	@Override
	public long getUsedPermGenMemory() {
		return this.runtimeInformation.getTotalPermGenMemoryUsed() / 1048576L;
	}

	@Override
	public long getTotalNonHeapMemory() {
		return this.runtimeInformation.getTotalNonHeapMemory() / 1048576L;
	}

	@Override
	public long getFreeNonHeapMemory() {
		long freeMemory = this.runtimeInformation.getTotalNonHeapMemory()
				- this.runtimeInformation.getTotalNonHeapMemoryUsed();
		return freeMemory / 1048576L;
	}

	@Override
	public long getUsedNonHeapMemory() {
		return this.runtimeInformation.getTotalNonHeapMemoryUsed() / 1048576L;
	}

	@Override
	public String getJvmInputArguments() {
		return this.runtimeInformation.getJvmInputArguments();
	}
}