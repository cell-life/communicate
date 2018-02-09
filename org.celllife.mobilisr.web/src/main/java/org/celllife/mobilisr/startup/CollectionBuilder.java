package org.celllife.mobilisr.startup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public final class CollectionBuilder<T> {
	private final List<T> elements = new LinkedList<T>();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> CollectionBuilder<T> newBuilder() {
		return new CollectionBuilder(Collections.emptyList());
	}

	CollectionBuilder(Collection<? extends T> initialElements) {
		this.elements.addAll(initialElements);
	}

	public CollectionBuilder<T> add(T element) {
		this.elements.add(element);
		return this;
	}

	public <E extends T> CollectionBuilder<T> addAll(E[] elements) {
		this.elements.addAll(Arrays.asList((T[]) Assertions.notNull("elements",
				elements)));
		return this;
	}

	public CollectionBuilder<T> addAll(Collection<? extends T> elements) {
		this.elements.addAll(Assertions.notNull("elements",	elements));
		return this;
	}

	public Collection<T> asCollection() {
		return asList();
	}

	public Collection<T> asMutableCollection() {
		return asMutableList();
	}

	public List<T> asArrayList() {
		return new ArrayList<T>(this.elements);
	}

	public List<T> asLinkedList() {
		return new LinkedList<T>(this.elements);
	}

	public List<T> asList() {
		return Collections.unmodifiableList(new ArrayList<T>(this.elements));
	}

	public List<T> asMutableList() {
		return asArrayList();
	}

	public Set<T> asHashSet() {
		return new HashSet<T>(this.elements);
	}

	public Set<T> asListOrderedSet() {
		return new LinkedHashSet<T>(this.elements);
	}

	public Set<T> asImmutableListOrderedSet() {
		return Collections.unmodifiableSet(new LinkedHashSet<T>(this.elements));
	}

	public Set<T> asSet() {
		return Collections.unmodifiableSet(new HashSet<T>(this.elements));
	}

	public Set<T> asMutableSet() {
		return asHashSet();
	}

	public SortedSet<T> asTreeSet() {
		return new TreeSet<T>(this.elements);
	}

	public SortedSet<T> asSortedSet() {
		return Collections.unmodifiableSortedSet(new TreeSet<T>(this.elements));
	}

	public SortedSet<T> asSortedSet(Comparator<? super T> comparator) {
		SortedSet<T> result = new TreeSet<T>(comparator);
		result.addAll(this.elements);
		return Collections.unmodifiableSortedSet(result);
	}

	public SortedSet<T> asMutableSortedSet() {
		return asTreeSet();
	}
}