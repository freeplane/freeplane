/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.core.extension;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SmallExtensionMap implements Map<Class<? extends IExtension>, IExtension> {
	private static final int INITIAL_CAPACITY = 5;
	private List<IExtension> collection;

	public void clear() {
		collection = null;
	}

	public boolean containsKey(final Object key) {
		if (collection == null) {
			return false;
		}
		if (!(key instanceof Class<?>)) {
			return false;
		}
		for (int i = 0; i < collection.size(); i++) {
			final Object extension = collection.get(i);
			if (key.equals(extension.getClass())) {
				return true;
			}
		}
		return false;
	}

	public boolean containsValue(final Object value) {
		if (collection == null) {
			return false;
		}
		if (!(value instanceof IExtension)) {
			return false;
		}
		for (int i = 0; i < collection.size(); i++) {
			if ( value.equals(collection.get(i))) {
				return true;
			}
		}
		return false;
	}

	private List<IExtension> createCollection() {
		if (collection == null) {
			collection = new ArrayList<IExtension>(INITIAL_CAPACITY);
		}
		return collection;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set<java.util.Map.Entry<Class<? extends IExtension>, IExtension>> entrySet() {
		if (collection == null) {
			return Collections.emptySet();
		}
		return collection.stream()
					.map(e -> new AbstractMap.SimpleEntry(e.getClass(), e))
					.collect(Collectors.toSet());

	}

	private int find(final Class<? extends IExtension> clazz) {
		if (collection == null) {
			return -1;
		}
		for (int i = 0; i < collection.size(); i++) {
			if (clazz.equals(collection.get(i).getClass())) {
				return i;
			}
		}
		return -1;
	}

	@SuppressWarnings("unchecked")
    public IExtension get(final Object key) {
		if (!(key instanceof Class<?>)) {
			return null;
		}
		final int index = find((Class<? extends IExtension>) key);
		if (index >= 0) {
			return collection.get(index);
		}
		return null;
	}

	public boolean isEmpty() {
		return collection == null;
	}

	public Set<Class<? extends IExtension>> keySet() {
		if (collection == null) {
			return Collections.emptySet();
		}

		return collection.stream().map(IExtension::getClass).collect(Collectors.toSet());
	}

	public IExtension put(final Class<? extends IExtension> key, final IExtension value) {
		final int index = find(key);
		if (index >= 0) {
			final IExtension oldValue = collection.get(index);
			collection.set(index, value);
			return oldValue;
		}
		else {
			if (!key.equals(value.getClass())) {
				throw new ClassCastException();
			}
			createCollection().add(value);
			return null;
		}
	}

	public void putAll(final Map<? extends Class<? extends IExtension>, ? extends IExtension> source) {
		for (final Entry<? extends Class<? extends IExtension>, ? extends IExtension> entry : source.entrySet()) {
			final Class<? extends IExtension> key = entry.getKey();
			final IExtension value = entry.getValue();
			put(key, value);
		}
	}

	@SuppressWarnings("unchecked")
    public IExtension remove(final Object key) {
		if (collection == null || !(key instanceof Class<?>)) {
			return null;
		}
		final int index = find((Class<? extends IExtension>) key);
		if (index == -1) {
			return null;
		}
		final IExtension remove = collection.remove(index);
		removeEmptyCollection();
		return remove;
	}

	private void removeEmptyCollection() {
		if (collection.size() == 0) {
			collection = null;
		}
	}

	public int size() {
		return collection == null ? 0 : collection.size();
	}

	public Collection<IExtension> values() {
		final Collection<IExtension> emptyList = Collections.emptyList();
		return collection == null ? emptyList : collection;
	}
}
