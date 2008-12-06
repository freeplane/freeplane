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
package org.freeplane.extension;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

final class SubsetIterator implements Iterator {
	final private Class clazz;
	Iterator iterator;
	Object next;

	SubsetIterator(final Collection collection, final Class clazz) {
		this(collection.iterator(), clazz);
	}

	SubsetIterator(final Iterator iterator, final Class clazz) {
		this.clazz = clazz;
		this.iterator = iterator;
		hasNext();
	}

	public boolean hasNext() {
		if (next != null) {
			return true;
		}
		while (iterator.hasNext()) {
			next = iterator.next();
			if (clazz.isAssignableFrom(next.getClass())) {
				return true;
			}
		}
		next = null;
		return false;
	}

	public Object next() {
		if (next != null) {
			final Object result = next;
			next = null;
			return result;
		}
		throw new NoSuchElementException();
	}

	public void remove() {
		iterator.remove();
	}
}
