package com.gm910.temendingir.api.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

/**
 * This map's <code>{@link java.util.Map#get get}</code> method is equivalent to
 * the normal map's <code>{@link Map#computeIfAbsent computeIfAbsent}</code>
 * method using a supplier given in the constructor. It's good for storing lists
 * The
 * <code>{@link Map#getOrDefault getOrDefault} method with a <code>null</code>
 * argument can be used to simulate regular map behavior
 * 
 * @author borah
 *
 * @param <K>
 * @param <V>
 */
public class NonNullTreeMap<K, V> extends TreeMap<K, V> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1093942400819952056L;

	private BiFunction<K, NonNullTreeMap<K, V>, V> supplier;

	public static final float DEFAULT_LOAD_FACTOR = ModReflect.getField(HashMap.class, float.class,
			"DEFAULT_LOAD_FACTOR", null, null);
	public static final int MAXIMUM_CAPACITY = ModReflect.getField(HashMap.class, int.class, "MAXIMUM_CAPACITY", null,
			null);

	public NonNullTreeMap(BiFunction<K, NonNullTreeMap<K, V>, V> supplier, @Nullable Comparator<K> comparator) {
		super(comparator);
		this.supplier = supplier;
	}

	public NonNullTreeMap(Function<K, V> supplier, @Nullable Comparator<K> comparator) {
		super(comparator);
		this.supplier = (a, b) -> {
			return supplier.apply(a);
		};
	}

	public static <K, V> NonNullTreeMap<K, V> create(Function<NonNullTreeMap<K, V>, V> supplier,
			@Nullable Comparator<K> comparator) {
		BiFunction<K, NonNullTreeMap<K, V>, V> supplier2 = (a, b) -> {
			return supplier.apply(b);
		};
		return new NonNullTreeMap<K, V>(supplier2, comparator);
	}

	public NonNullTreeMap(Supplier<V> supplier, @Nullable Comparator<K> comparator) {
		super(comparator);
		this.supplier = (a, b) -> {
			return supplier.get();
		};
	}

	public BiFunction<K, NonNullTreeMap<K, V>, V> getSupplier() {
		return supplier;
	}

	@Override
	public V get(Object key) {
		V val = super.get(key);
		if (val == null && ModReflect.<K>instanceOf(key, Object.class)) {
			val = supplier.apply((K) key, this);
			super.put((K) key, val);
		}
		return val;
	}

	/**
	 * Replaces the entire contents of this map with the contents of the other map
	 * Returns self for easy chaining (since the constructor only accepts suppliers)
	 * 
	 * @param newmap
	 */
	public NonNullTreeMap<K, V> setAs(Map<? extends K, ? extends V> newmap) {
		this.clear();
		this.putAll(newmap);

		return this;
	}

	/**
	 * Constructs map the same way as the given Returns self for easy chaining
	 * (since the constructor only accepts suppliers)
	 * 
	 * @param newmap
	 */
	public NonNullTreeMap<K, V> setAs(int capacity) {

		return setAs(capacity, DEFAULT_LOAD_FACTOR);
	}

	public NonNullTreeMap<K, V> setAs(int initialCapacity, float loadFactor) {

		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
		if (initialCapacity > MAXIMUM_CAPACITY)
			initialCapacity = MAXIMUM_CAPACITY;
		if (loadFactor <= 0 || Float.isNaN(loadFactor))
			throw new IllegalArgumentException("Illegal load factor: " + loadFactor);

		System.out.println("Nonnullmapmaking "
				+ ModReflect.setField(HashMap.class, float.class, "loadFactor", null, this, loadFactor));
		System.out.println("Nonnullmapmaking " + ModReflect.setField(HashMap.class, int.class, "threshold", null, this,
				ModReflect.run(HashMap.class, int.class, "tableSizeFor", null, initialCapacity)));

		return this;
	}

	public NonNullTreeMap<K, V> initialize(Consumer<NonNullTreeMap<K, V>> initializer) {
		initializer.accept(this);
		return this;
	}

}
