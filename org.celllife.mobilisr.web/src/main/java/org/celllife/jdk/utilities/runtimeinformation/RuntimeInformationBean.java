package org.celllife.jdk.utilities.runtimeinformation;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RuntimeInformationBean implements RuntimeInformation {
	private final MemoryMXBean memoryBean;
	private final RuntimeMXBean runtimeBean;

	public RuntimeInformationBean() {
		this.memoryBean = ManagementFactory.getMemoryMXBean();
		this.runtimeBean = ManagementFactory.getRuntimeMXBean();
	}

	public long getTotalHeapMemory() {
		return this.memoryBean.getHeapMemoryUsage().getMax();
	}

	public long getTotalHeapMemoryUsed() {
		return this.memoryBean.getHeapMemoryUsage().getUsed();
	}

	public List<MemoryInformation> getMemoryPoolInformation() {
		List<MemoryPoolMXBean> mxBeans = ManagementFactory.getMemoryPoolMXBeans();
		List<MemoryInformation> result = new ArrayList<MemoryInformation>(mxBeans.size());
		for (MemoryPoolMXBean mxBean : mxBeans) {
			result.add(new MemoryInformationBean(mxBean));
		}
		return Collections.unmodifiableList(result);
	}

	public long getTotalPermGenMemory() {
		return getPermGen().getTotal();
	}

	public long getTotalPermGenMemoryUsed() {
		return getPermGen().getUsed();
	}

	public long getTotalNonHeapMemory() {
		return this.memoryBean.getNonHeapMemoryUsage().getMax();
	}

	public long getTotalNonHeapMemoryUsed() {
		return this.memoryBean.getNonHeapMemoryUsage().getUsed();
	}

	public String getJvmInputArguments() {
		StringBuilder sb = new StringBuilder();
		for (String argument : this.runtimeBean.getInputArguments()) {
			sb.append(argument).append(" ");
		}
		return sb.toString();
	}

	private MemoryInformation getPermGen() {
		for (MemoryInformation info : getMemoryPoolInformation()) {
			String name = info.getName().toLowerCase();
			if (name.contains("perm gen")) {
				return info;
			}
		}
		return new MemoryInformation() {
			public String getName() {
				return "";
			}

			public long getTotal() {
				return -1L;
			}

			public long getUsed() {
				return -1L;
			}

			public long getFree() {
				return -1L;
			}
		};
	}
}