package org.freeplane.features.icon.factory;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class WeakValueCache<K, V> {
	final private Map<K, WeakReference<V>> map = new HashMap<>();

	public boolean containsKey(String key) {
		final WeakReference<V> weakReference = map.get(key);
		return weakReference != null && weakReference.get() != null;
	}

	public V get(K key) {
		final WeakReference<V> weakReference = map.get(key);
		return weakReference != null ? weakReference.get() : null;
	}

	public void put(K key, V value) {
		if(value == null)
			throw new IllegalArgumentException("null values are not allowed");
		final WeakReference<V> reference = new WeakReference<>(value);
		map.put(key, reference);
	}
}
