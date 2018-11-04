package org.freeplane.plugin.script;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class ConcurrentCache <K, V> {

	private static final float DEFAULT_LOAD_FACTOR = 0.75f;
	private static final int DEFAULT_INITIAL_CAPACITY = 100;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwl.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwl.writeLock();

	private final LinkedHashMap<K, V> cache ;
	@SuppressWarnings("serial")
	public ConcurrentCache(IntSupplier maxSize) {
		super();
		cache = new  LinkedHashMap<K, V>(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, true){
			@Override
			protected boolean removeEldestEntry(Entry<K, V> eldest) {
				return size() > maxSize.getAsInt();
			}
		};
	}

	public V computeIfAbsent(K key, Supplier<? extends V> supplier) {
        readLock.lock();
        try {
            V value = cache.get(key);
            if (null != value) {
                return value;
            }
        } finally {
            readLock.unlock();
        }
        V value = supplier.get();
        writeLock.lock();
        try {
            V oldValue = cache.putIfAbsent(key, value);
            return null != oldValue ? oldValue : value;
        } finally {
        	writeLock.unlock();
        }
	}

}
