package org.celllife.mobilisr.startup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

public class FormattedLogMsg {
	private static final String NEW_LINE = System.getProperty("line.separator");
	private static final String INDENT_STR = "     ";
	private static final int MAX_DESC_WIDTH = 45;
	private static final int MAX_VALUE_WIDTH = 55;
	private static final int MAX_STAR_WIDTH = 200;
	private final Logger log;
	private final List<String> msgList = new ArrayList<String>();

	public FormattedLogMsg() {
		this(null);
	}

	public FormattedLogMsg(Logger log) {
		this.log = log;
	}

	public void add(Object message) {
		add(message, 0);
	}

	public void add(Object message, int indentLevel) {
		StringBuffer sb = new StringBuffer();

		if (indentLevel > 0) {
			String indentStr = StringUtils.repeat(INDENT_STR, indentLevel);
			sb.append(indentStr);
		}
		sb.append(String.valueOf(message));

		this.msgList.add(sb.toString());
	}

	public void addAll(Collection<String> collection) {
		this.msgList.addAll(collection);
	}

	public void outputProperty(String propertyDesc) {
		outputPropertyImpl(propertyDesc, null, null, 1);
	}

	public void outputProperty(String propertyDesc, String propertyValue) {
		outputPropertyImpl(propertyDesc, propertyValue, null, 1);
	}

	public void outputProperty(String propertyDesc, String propertyValue,
			String splitStr) {
		outputPropertyImpl(propertyDesc, propertyValue, splitStr, 1);
	}

	public void outputProperty(String propertyDesc, String propertyValue,
			int indentLevel) {
		outputPropertyImpl(propertyDesc, propertyValue, null, indentLevel);
	}

	public void outputProperty(String propertyDesc, String propertyValue,
			String splitStr, int indentLevel) {
		outputPropertyImpl(propertyDesc, propertyValue, splitStr, indentLevel);
	}

	private void outputPropertyImpl(String propertyDesc, String propertyValue,
			String splitStr, int indentLevel) {
		propertyDesc = propertyDesc == null ? "" : propertyDesc.trim();
		propertyValue = propertyValue == null ? "" : propertyValue.trim();

		String indentStr = StringUtils.repeat(INDENT_STR, indentLevel);

		StringBuffer sb = new StringBuffer();
		sb.append(indentStr);
		sb.append(propertyDesc);

		int spacesLen = Math.max(MAX_DESC_WIDTH - propertyDesc.length(), 0);
		sb.append(StringUtils.repeat(" ", spacesLen));
		sb.append(" : ");

		int splitIndex = splitStr == null ? -1 : propertyValue
				.indexOf(splitStr);
		if ((propertyValue.length() > MAX_VALUE_WIDTH) && (splitIndex != -1)) {
			String splitValue = indentValue(propertyValue, splitStr, true,
					indentLevel);
			sb.append(splitValue);
		} else {
			propertyValue = indentValue(propertyValue, NEW_LINE, false,
					indentLevel);
			sb.append(propertyValue);
		}
		this.msgList.add(sb.toString());
	}

	public void outputHeader(String header) {
		StringBuffer sb = new StringBuffer();

		boolean addNewLine = !this.msgList.isEmpty();
		if (addNewLine) {
			String prevMsg = String.valueOf(this.msgList.get(this.msgList
					.size() - 1));
			if (prevMsg.equals(NEW_LINE)) {
				addNewLine = false;
			}
		}
		if (addNewLine) {
			sb.append(NEW_LINE);
		}
		sb.append("___ ");
		sb.append(header);
		sb.append(" _");
		int spacesLen = Math.max(MAX_DESC_WIDTH - sb.length(), 0);
		sb.append(StringUtils.repeat("_", spacesLen));
		sb.append(NEW_LINE);
		this.msgList.add(sb.toString());
	}

	private String indentValue(String propertyValue, String splitStr,
			boolean reappendSplitStr, int indentlevel) {
		int splitIndex = propertyValue.indexOf(splitStr);
		if (splitIndex == -1) {
			return propertyValue;
		}
		int lastIndex = 0;
		String indentStr = StringUtils.repeat(INDENT_STR, indentlevel);
		StringBuffer sb = new StringBuffer();
		while (splitIndex != -1) {
			int splitStrLen = splitStr.length();
			String splitValue = propertyValue.substring(lastIndex, splitIndex);
			if (lastIndex > 0) {
				sb.append(NEW_LINE);
				sb.append(indentStr);
				sb.append(StringUtils.repeat(" ", MAX_DESC_WIDTH));
				sb.append("   ");
			}
			sb.append(splitValue);
			if (reappendSplitStr) {
				sb.append(splitStr);
			}
			lastIndex = splitIndex + splitStrLen;
			splitIndex = propertyValue.indexOf(splitStr, lastIndex);
		}
		if (lastIndex < propertyValue.length()) {
			String splitValue = propertyValue.substring(lastIndex);
			sb.append(NEW_LINE);
			sb.append(indentStr);
			sb.append(StringUtils.repeat(" ", 45));
			sb.append("   ");
			sb.append(splitValue);
		}
		return sb.toString();
	}

	public void printMessage(Level logLevel) {
		printMessageImpl(this.msgList, logLevel, true);
	}

	public void printMessage(Level logLevel, boolean useStars) {
		printMessageImpl(this.msgList, logLevel, useStars);
	}

	private void printMessageImpl(Collection<String> messages, Level logLevel,
			boolean useStars) {
		String line = toStringImpl(messages, useStars);
		if (this.log != null) {
			switch (logLevel){
			case TRACE:
				this.log.trace(line);
				break;
			case DEGUG:
				this.log.debug(line);
				break;
			case INFO:
				this.log.info(line);
				break;
			case WARN:
				this.log.warn(line);
				break;
			case ERROR:
				this.log.error(line);
				break;
			}
		}
		resetState();
	}

	public String toString() {
		return toStringImpl(this.msgList, false);
	}

	private String toStringImpl(Collection<String> messages, boolean useStars) {
		if ((messages == null) || (messages.isEmpty())) {
			return "";
		}

		int maxLength = 0;
		for (String message : messages) {
			maxLength = Math.max(message.length(), maxLength);

			maxLength = Math.min(MAX_STAR_WIDTH, maxLength);
		}

		StringBuffer line = new StringBuffer().append(NEW_LINE)
				.append(NEW_LINE);
		if (useStars) {
			line.append(StringUtils.repeat("*", maxLength)).append(NEW_LINE);
		}
		for (String message : messages) {
			line.append(message).append(NEW_LINE);
		}
		if (useStars) {
			line.append(StringUtils.repeat("*", maxLength)).append(NEW_LINE);
		}
		return line.toString();
	}

	private void resetState() {
		this.msgList.clear();
	}
}