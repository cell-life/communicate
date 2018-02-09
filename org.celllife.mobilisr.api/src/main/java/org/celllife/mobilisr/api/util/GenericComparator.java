package org.celllife.mobilisr.api.util;

import java.util.Comparator;

public class GenericComparator {

	public static <T> Comparator<T> createComparator(final Comparator<T>... c) {
		return new Comparator<T>() {
			public int compare(T r1, T r2) {
				int result = 0;

				for (Comparator<T> comp : c) {
					result = comp.compare(r1, r2);
					if (result != 0) {
						break;
					}
				}

				return result;
			}
		};
	}
}