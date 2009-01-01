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

import java.util.HashMap;
import java.util.Iterator;

public class ExtensionHashMap implements IModifiableExtensionCollection {
	final private HashMap<Class, IExtension> collection;

	public ExtensionHashMap() {
		collection = new HashMap();
	}

	public ExtensionHashMap(final int initialCapacity) {
		collection = new HashMap(initialCapacity);
	}

	public ExtensionHashMap(final int initialCapacity, final float loadFactor) {
		collection = new HashMap(initialCapacity, loadFactor);
	}

	public boolean addExtension(final Class clazz, final IExtension extension) {
		if (containsExtension(clazz)) {
			throw new IllegalArgumentException("element with class " + clazz.getName()
			        + " already exist");
		}
		setExtension(clazz, extension);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.extensions.Extensions#addExtension(freemind.extensions.Extension
	 * )
	 */
	public boolean addExtension(final IExtension extension) {
		final Class clazz = extension.getClass();
		return addExtension(clazz, extension);
	}

	public boolean containsExtension(final Class clazz) {
		final Iterator iterator = extensionIterator();
		while (iterator.hasNext()) {
			final Object extension = iterator.next();
			if (clazz.isAssignableFrom(extension.getClass())) {
				return true;
			}
		}
		return false;
	}

	public Iterator extensionIterator() {
		return collection.values().iterator();
	}

	public Iterator extensionIterator(final Class clazz) {
		return new SubsetIterator(collection.values(), clazz);
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.extensions.Extensions#getExtension(java.lang.Class)
	 */
	public IExtension getExtension(final Class clazz) {
		return collection.get(clazz);
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.extensions.Extensions#removeExtension(java.lang.Class)
	 */
	public IExtension removeExtension(final Class clazz) {
		return collection.remove(clazz);
	}

	public boolean removeExtension(final IExtension extension) {
		final Class clazz = extension.getClass();
		return removeExtension(clazz) != null;
	}

	public void setExtension(final Class clazz, final IExtension extension) {
		collection.put(clazz, extension);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.extensions.Extensions#setExtension(freemind.extensions.Extension
	 * )
	 */
	public void setExtension(final IExtension extension) {
		final Class clazz = extension.getClass();
		setExtension(clazz, extension);
	}
}
