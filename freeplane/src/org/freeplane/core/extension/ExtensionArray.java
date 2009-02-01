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
package org.freeplane.core.extension;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ExtensionArray implements IModifiableExtensionCollection {
	final private ArrayList<IExtension> collection;

	public ExtensionArray() {
		this(5);
	}

	public ExtensionArray(final int capacity) {
		collection = new ArrayList(capacity);
	}

	public boolean addExtension(final Class clazz, final IExtension extension) {
		final int index = find(clazz);
		if (index == -1) {
			collection.add(extension);
			return true;
		}
		throw new IllegalArgumentException("element with class " + clazz.getName() + " already exist");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.extensions.Extensions#addExtension(freeplane.extensions.Extension
	 * )
	 */
	public boolean addExtension(final IExtension extension) {
		final Class clazz = extension.getClass();
		return addExtension(clazz, extension);
	}

	public boolean containsExtension(final Class clazz) {
		for (int i = 0; i < collection.size(); i++) {
			final Object extension = collection.get(i);
			if (clazz.equals(extension.getClass())) {
				return true;
			}
		}
		return false;
	}

	public boolean containsExtension(final IExtension extension) {
		for (int i = 0; i < collection.size(); i++) {
			if (extension.equals(collection.get(i))) {
				return true;
			}
		}
		return false;
	}

	public Iterator extensionIterator() {
		return collection.iterator();
	}

	public Iterator extensionIterator(final Class clazz) {
		return new SubsetIterator(collection, clazz);
	}

	private int find(final Class clazz) {
		for (int i = 0; i < collection.size(); i++) {
			if (clazz.isAssignableFrom(collection.get(i).getClass())) {
				return i;
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.extensions.Extensions#getExtension(java.lang.Class)
	 */
	public IExtension getExtension(final Class clazz) {
		final int index = find(clazz);
		if (index >= 0) {
			return getExtension(index);
		}
		return null;
	}

	private IExtension getExtension(final int i) {
		final IExtension o1 = collection.get(i);
		if (i > 0) {
			final IExtension o2 = collection.get(i - 1);
			collection.set(i, o2);
			collection.set(i - 1, o1);
		}
		return o1;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.extensions.Extensions#removeExtension(java.lang.Class)
	 */
	public IExtension removeExtension(final Class clazz) {
		for (int i = 0; i < collection.size(); i++) {
			if (clazz.equals(collection.get(i).getClass())) {
				return collection.remove(i);
			}
		}
		throw new NoSuchElementException("element with class " + clazz.getName() + " not found");
	}

	public boolean removeExtension(final IExtension extension) {
		return collection.remove(extension);
	}

	public void setExtension(final Class clazz, final IExtension extension) {
		final int index = find(clazz);
		if (index >= 0) {
			collection.set(index, extension);
		}
		else {
			collection.add(extension);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.extensions.Extensions#setExtension(freeplane.extensions.Extension
	 * )
	 */
	public void setExtension(final IExtension extension) {
		final Class clazz = extension.getClass();
		setExtension(clazz, extension);
	}
}
