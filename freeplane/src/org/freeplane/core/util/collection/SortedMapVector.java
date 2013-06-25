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

import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * @author Dimitry Polivaev
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SortedMapVector {
	private static class MapElement {
        final private Comparable key;
		private final Object value;

		public MapElement(final Comparable key, final Object value) {
			super();
			this.key = key;
			this.value = value;
		}

		Comparable getKey() {
			return key;
		}

		Object getValue() {
			return value;
		}
	}

	private static final int CAPACITY_INCREMENT = 10;
	private static final int ELEMENT_NOT_FOUND_FLAG = 1 << 31;
	final private Vector<MapElement> elements;

	public SortedMapVector() {
		elements = new Vector<MapElement>(0, SortedMapVector.CAPACITY_INCREMENT);
	}

	public int add(final Comparable key, final Object value) {
		int index = findElement(key);
		if ((index & SortedMapVector.ELEMENT_NOT_FOUND_FLAG) != 0) {
			index &= ~SortedMapVector.ELEMENT_NOT_FOUND_FLAG;
			elements.add(index, new MapElement(key, value));
		}
		return index;
	}

	public int capacity() {
		return elements.capacity();
	}

	public void clear() {
		elements.clear();
	}

	public boolean containsKey(final Comparable key) {
		final int index = findElement(key);
		return (index & SortedMapVector.ELEMENT_NOT_FOUND_FLAG) == 0;
	}

	private int findElement(final Comparable key) {
		return findElement(key, 0, size());
	}

	private int findElement(final Comparable key, final int first, final int size) {
		if (size == 0) {
			return first | SortedMapVector.ELEMENT_NOT_FOUND_FLAG;
		}
		final int halfSize = size / 2;
		final int middle = first + halfSize;
		final MapElement middleElement = elements.get(middle);
		int comparationResult = key.compareTo(middleElement.getKey());
		final int last = first + size - 1;
		if (comparationResult < 0) {
			if (halfSize <= 1) {
				if (middle != first) {
					comparationResult = key.compareTo(elements.get(first).getKey());
				}
				if (comparationResult < 0) {
					return first | SortedMapVector.ELEMENT_NOT_FOUND_FLAG;
				}
				if (comparationResult == 0) {
					return first;
				}
				return middle | SortedMapVector.ELEMENT_NOT_FOUND_FLAG;
			}
			return findElement(key, first, halfSize);
		}
		else if (comparationResult == 0) {
			return middle;
		}
		else {
			if (halfSize <= 1) {
				if (middle != last) {
					comparationResult = key.compareTo(elements.get(last).getKey());
				}
				if (comparationResult < 0) {
					return last | SortedMapVector.ELEMENT_NOT_FOUND_FLAG;
				}
				if (comparationResult == 0) {
					return last;
				}
				return last + 1 | SortedMapVector.ELEMENT_NOT_FOUND_FLAG;
			}
			return findElement(key, middle, size - halfSize);
		}
	}

	public Comparable getKey(final int index) {
		return elements.get(index).getKey();
	}

	public Object getValue(final Comparable key) {
		final int index = findElement(key);
		if ((index & SortedMapVector.ELEMENT_NOT_FOUND_FLAG) == 0) {
			return elements.get(index).getValue();
		}
		throw new NoSuchElementException();
	}

	public Object getValue(final int index) {
		return elements.get(index).getValue();
	}

	public int indexOf(final Comparable key) {
		final int index = findElement(key);
		if ((index & SortedMapVector.ELEMENT_NOT_FOUND_FLAG) == 0) {
			return index;
		}
		return -1;
	}

	public boolean remove(final Comparable key) {
		final int index = findElement(key);
		if ((index & SortedMapVector.ELEMENT_NOT_FOUND_FLAG) == 0) {
			elements.remove(index);
			return true;
		}
		return false;
	}

	public void remove(final int index) {
		elements.removeElementAt(index);
	}

	public int size() {
		return elements.size();
	}
}
