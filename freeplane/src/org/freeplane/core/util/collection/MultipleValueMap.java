/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
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
package org.freeplane.core.util.collection;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Dimitry Polivaev
 * 06.01.2009
 */
public class MultipleValueMap<K, V> {
	final private Map<K, List<V>> map;

	public MultipleValueMap() {
		super();
		this.map = new HashMap<K, List<V>>();
	}

	public boolean containsKey(final Object key) {
		return map.containsKey(key);
	}

	@SuppressWarnings("unchecked")
	public List<V> get(final Object key) {
		final List<V> list = map.get(key);
		return list == null ? Collections.EMPTY_LIST : list;
	}

	// FIXME: value is not used - how should it? - VB
	public List<V> put(final K key, final V value) {
		List<V> list = map.get(key);
		if (list == null) {
			list = new LinkedList<V>();
			map.put(key, list);
		}
		return list;
	}
}
