package org.celllife.mobilisr.startup;

import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.lang.StringUtils;

public final class Assertions {
	public static <T> T notNull(String name, T notNull)
			throws IllegalArgumentException {
		if (notNull == null) {
			throw new NullArgumentException(name);
		}
		return notNull;
	}

	public static <T extends Collection<?>> T notEmpty(String name, T notEmpty)
			throws IllegalArgumentException {
		if (notEmpty == null) {
			throw new NullArgumentException(name);
		}
		if (notEmpty.isEmpty()) {
			throw new EmptyArgumentException(name);
		}
		return notEmpty;
	}

	public static <C extends Iterable<?>> C containsNoNulls(String name,
			C containsNoNulls) throws IllegalArgumentException {
		notNull(name, containsNoNulls);
		int i = 0;
		for (Iterator<?> i$ = containsNoNulls.iterator(); i$.hasNext();) {
			Object item = i$.next();

			if (item == null) {
				throw new NullArgumentException(name + "[" + i + "]");
			}
			i++;
		}
		return containsNoNulls;
	}

	public static <C> C[] containsNoNulls(String name, C[] containsNoNulls)
			throws IllegalArgumentException {
		notNull(name, containsNoNulls);
		int i = 0;
		for (Object item : containsNoNulls) {
			if (item == null) {
				throw new NullArgumentException(name + "[" + i + "]");
			}
			i++;
		}
		return containsNoNulls;
	}

	public static String notBlank(String name, String string)
			throws IllegalArgumentException {
		notNull(name, string);
		if (string.trim().length() == 0) {
			throw new BlankStringArgumentException(name);
		}
		return string;
	}

	public static <C extends Iterable<String>> C containsNoBlanks(String name,
			C stringsNotBlank) {
		notNull(name, stringsNotBlank);
		int i = 0;
		for (String item : stringsNotBlank) {
			if (StringUtils.isBlank(item)) {
				throw new BlankStringArgumentException(name + "[" + i + "]");
			}
			i++;
		}
		return stringsNotBlank;
	}

	public static <T> T equals(String name, T expected, T got)
			throws IllegalArgumentException {
		if (!expected.equals(got)) {
			throw new IllegalArgumentException(name + ". Expected:" + expected
					+ " but got: " + got);
		}
		return got;
	}

	static class EmptyArgumentException extends IllegalArgumentException {
		private static final long serialVersionUID = 5915436895286120015L;

		EmptyArgumentException(String name) {
			super();
		}
	}

	static class NullArgumentException extends IllegalArgumentException {
		private static final long serialVersionUID = -7024015563450697425L;

		NullArgumentException(String name) {
			super();
		}
	}

	static class BlankStringArgumentException extends IllegalArgumentException {
		private static final long serialVersionUID = 8514979305397364052L;

		BlankStringArgumentException(String name) {
			super();
		}
	}
}