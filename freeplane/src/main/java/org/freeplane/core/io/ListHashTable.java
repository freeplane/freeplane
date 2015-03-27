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
package org.freeplane.core.io;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class ListHashTable<K, V> {
	/**
	 * @author Dimitry Polivaev
	 */
	private static class EmptyIterator<V> implements Iterator<V> {
		public boolean hasNext() {
			return false;
		}

		public V next() {
			throw new NoSuchElementException();
		}

		public void remove() {
			throw new IllegalStateException();
		}
	}

	final private Map<K, List<V>> table = new Hashtable<K, List<V>>();

	public void add(final K tag, final V element) {
		List<V> elementsForTag = table.get(tag);
		if (elementsForTag == null) {
			elementsForTag = new LinkedList<V>();
			table.put(tag, elementsForTag);
		}
		elementsForTag.add(element);
	}

	public boolean isEmpty(final K tag) {
		final List<V> elementsForTag = list(tag);
		if (elementsForTag == null) {
			return true;
		}
		return elementsForTag.isEmpty();
	}

	public Iterator<V> iterator(final K tag) {
		final List<V> elementsForTag = list(tag);
		if (elementsForTag == null) {
			return new EmptyIterator<V>();
		}
		return elementsForTag.listIterator();
	}

	public List<V> list(final K tag) {
		return table.get(tag);
	}

	public boolean remove(final K tag, final V element) {
		final List<V> elementsForTag = list(tag);
		if (elementsForTag == null) {
			return false;
		}
		return elementsForTag.remove(element);
	}
}
