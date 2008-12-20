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
package org.freeplane.io;

import org.freeplane.extension.IExtension;

public class WriteManager {
	final private ListHashTable<String, IAttributeWriter<String>> attributeWriters = new ListHashTable();
	final private ListHashTable<Class<? extends IExtension>, IAttributeWriter<IExtension>> extensionAttributeWriters = new ListHashTable();
	final private ListHashTable<Class<? extends IExtension>, INodeWriter<IExtension>> extensionNodeWriters = new ListHashTable();;
	final private ListHashTable<String, INodeWriter<String>> nodeWriters = new ListHashTable();
	final private ListHashTable<String, IXMLElementWriter> xmlWriters = new ListHashTable();

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

	public void addExtensionAttributeWriter(final Class<? extends IExtension> clazz,
	                                        final IAttributeWriter<IExtension> aw) {
		extensionAttributeWriters.add(clazz, aw);
	}

	public void addExtensionNodeWriter(final Class<? extends IExtension> clazz,
	                                   final INodeWriter<IExtension> nw) {
		extensionNodeWriters.add(clazz, nw);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Writer#addNodeSaver(java.lang.String,
	 * freeplane.persistence.NodeSaver)
	 */
	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.ISaverManager#addNodeSaver(java.lang.String,
	 * freeplane.persistence.NodeSaver)
	 */
	public void addNodeWriter(final String parentTag, final INodeWriter nw) {
		nodeWriters.add(parentTag, nw);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.persistence.ISaverManager#addXMLElementSaver(java.lang.String,
	 * freeplane.persistence.XMLElementSaver)
	 */
	public void addXMLElementWriter(final String parentTag, final IXMLElementWriter xw) {
		xmlWriters.add(parentTag, xw);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.ISaverManager#getAttributeSavers()
	 */
	public ListHashTable<String, IAttributeWriter<String>> getAttributeWriters() {
		return attributeWriters;
	}

	public ListHashTable<Class<? extends IExtension>, IAttributeWriter<IExtension>> getExtensionAttributeWriters() {
		return extensionAttributeWriters;
	}

	public ListHashTable<Class<? extends IExtension>, INodeWriter<IExtension>> getExtensionNodeWriters() {
		return extensionNodeWriters;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.ISaverManager#getNodeSavers()
	 */
	public ListHashTable<String, INodeWriter<String>> getNodeWriters() {
		return nodeWriters;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.ISaverManager#getXmlSavers()
	 */
	public ListHashTable<String, IXMLElementWriter> getXmlWriters() {
		return xmlWriters;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Writer#removeAttributeSaver(java.lang.String,
	 * freeplane.persistence.AttributeSaver)
	 */
	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.persistence.ISaverManager#removeAttributeSaver(java.lang.String
	 * , freeplane.persistence.AttributeSaver)
	 */
	public void removeAttributeWriter(final String parentTag, final IAttributeWriter aw) {
		attributeWriters.remove(parentTag, aw);
	}

	public void removeExtensionAttributeWriter(final Class<? extends IExtension> clazz,
	                                           final IAttributeWriter<IExtension> aw) {
		extensionAttributeWriters.remove(clazz, aw);
	}

	public void removeExtensionNodeWriter(final Class<? extends IExtension> clazz,
	                                      final INodeWriter<IExtension> nw) {
		extensionNodeWriters.remove(clazz, nw);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Writer#removeNodeSaver(java.lang.String,
	 * freeplane.persistence.NodeSaver)
	 */
	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.persistence.ISaverManager#removeNodeSaver(java.lang.String,
	 * freeplane.persistence.NodeSaver)
	 */
	public void removeNodeWriter(final String parentTag, final INodeWriter nw) {
		nodeWriters.remove(parentTag, nw);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.persistence.ISaverManager#removeXMLElementSaver(java.lang.String
	 * , freeplane.persistence.XMLElementSaver)
	 */
	public void removeXMLElementWriter(final String parentTag, final IXMLElementWriter xw) {
		xmlWriters.remove(parentTag, xw);
	}
}
