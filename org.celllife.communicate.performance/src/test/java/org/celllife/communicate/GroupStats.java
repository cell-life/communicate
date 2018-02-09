package org.celllife.communicate;

public class GroupStats {
	
	private Number count;
	
	private String value;

	public Number getCount() {
		return count;
	}

	public void setCount(Number count) {
		this.count = count;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[count=").append(count)
				.append(", value=").append(value).append("]");
		return builder.toString();
	}
	
}
