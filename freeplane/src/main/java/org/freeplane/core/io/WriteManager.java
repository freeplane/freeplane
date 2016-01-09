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

import org.freeplane.core.extension.IExtension;

public class WriteManager {
	final private ListHashTable<String, IAttributeWriter> attributeWriters = new ListHashTable<String, IAttributeWriter>();
	final private ListHashTable<String, IElementWriter> elementWriters = new ListHashTable<String, IElementWriter>();
	final private ListHashTable<Class<? extends IExtension>, IExtensionAttributeWriter> extensionAttributeWriters = new ListHashTable<Class<? extends IExtension>, IExtensionAttributeWriter>();;
	final private ListHashTable<Class<? extends IExtension>, IExtensionElementWriter> extensionElementWriters = new ListHashTable<Class<? extends IExtension>, IExtensionElementWriter>();

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Writer#addAttributeSaver(java.lang.String,
	 * freeplane.persistence.AttributeSaver)
	 */
	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.persistence.ISaverManager#addAttributeSaver(java.lang.String,
	 * freeplane.persistence.AttributeSaver)
	 */
	public void addAttributeWriter(final String parentTag, final IAttributeWriter aw) {
		attributeWriters.add(parentTag, aw);
	}

	public void addElementWriter(final String parentTag, final IElementWriter nw) {
		elementWriters.add(parentTag, nw);
	}

	public void addExtensionAttributeWriter(final Class<? extends IExtension> clazz, final IExtensionAttributeWriter aw) {
		extensionAttributeWriters.add(clazz, aw);
	}

	public void addExtensionElementWriter(final Class<? extends IExtension> clazz, final IExtensionElementWriter nw) {
		extensionElementWriters.add(clazz, nw);
	}

	public ListHashTable<String, IAttributeWriter> getAttributeWriters() {
		return attributeWriters;
	}

	public ListHashTable<String, IElementWriter> getElementWriters() {
		return elementWriters;
	}

	public ListHashTable<Class<? extends IExtension>, IExtensionAttributeWriter> getExtensionAttributeWriters() {
		return extensionAttributeWriters;
	}

	public ListHashTable<Class<? extends IExtension>, IExtensionElementWriter> getExtensionElementWriters() {
		return extensionElementWriters;
	}

	public void removeAttributeWriter(final String parentTag, final IAttributeWriter aw) {
		final boolean removed = attributeWriters.remove(parentTag, aw);
		assert removed;
	}

	public void removeElementWriter(final String parentTag, final IElementWriter nw) {
		final boolean removed = elementWriters.remove(parentTag, nw);
		assert removed;
	}

	public void removeExtensionAttributeWriter(final Class<? extends IExtension> clazz,
	                                           final IExtensionAttributeWriter aw) {
		final boolean removed = extensionAttributeWriters.remove(clazz, aw);
		assert removed;
	}

	public void removeExtensionElementWriter(final Class<? extends IExtension> clazz, final IExtensionElementWriter nw) {
		final boolean removed = extensionElementWriters.remove(clazz, nw);
		assert removed;
	}
}
