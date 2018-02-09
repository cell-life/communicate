package org.celllife.jdk.utilities.runtimeinformation;

import java.lang.management.MemoryPoolMXBean;

public class MemoryInformationBean implements MemoryInformation {
	private final MemoryPoolMXBean memoryPool;

	MemoryInformationBean(MemoryPoolMXBean memoryPool) {
		this.memoryPool = memoryPool;
	}

	public String getName() {
		return this.memoryPool.getName();
	}

	public long getTotal() {
		return this.memoryPool.getUsage().getMax();
	}

	public long getUsed() {
		return this.memoryPool.getUsage().getUsed();
	}

	public long getFree() {
		return getTotal() - getUsed();
	}

	public String toString() {
		return this.memoryPool.getName() + ": " + this.memoryPool.getUsage();
	}
}
