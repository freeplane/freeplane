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

import java.io.IOException;
import java.util.ListIterator;

import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.model.EncryptionModel;
import org.freeplane.core.model.MindIcon;
import org.freeplane.core.model.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

class NodeWriter implements IElementWriter, IAttributeWriter {
	private EncryptionModel encryptionModel;
	final private boolean writeChildren;
	final private boolean writeInvisible;
	private XMLElement xmlNode;
	final private MapController mapController;

	public NodeWriter(final MapController mapController, final boolean writeChildren, final boolean writeInvisible) {
		this.mapController = mapController;
		this.writeChildren = writeChildren;
		this.writeInvisible = writeInvisible;
	}

	private void saveChildren(final ITreeWriter writer, final NodeModel node) throws IOException {
		for (final ListIterator e = mapController.childrenUnfolded(node); e.hasNext();) {
			final NodeModel child = (NodeModel) e.next();
			if (writeInvisible || child.isVisible()) {
				writer.addElement(child, NodeBuilder.XML_NODE);
			}
			else {
				saveChildren(writer, child);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.io.IAttributeWriter#saveAttributes(freeplane.io.ITreeWriter,
	 * java.lang.Object, java.lang.String)
	 */
	public void writeAttributes(final ITreeWriter writer, final Object content, final String tag) {
		if (tag.equals(NodeBuilder.XML_NODE)) {
			final NodeModel node = (NodeModel) content;
			writeAttributesGenerateContent(writer, node);
			return;
		}
	}

	private void writeAttributesGenerateContent(final ITreeWriter writer, final NodeModel node) {
		/** fc, 12.6.2005: XML must not contain any zero characters. */
		xmlNode = new XMLElement();
		encryptionModel = EncryptionModel.getModel(node);
		if (encryptionModel != null) {
			final String additionalInfo = encryptionModel.getEncryptedContent(mapController);
			writer.addAttribute(NodeBuilder.XML_NODE_ENCRYPTED_CONTENT, additionalInfo);
		}
		if (mapController.isFolded(node)) {
			writer.addAttribute("FOLDED", "true");
		}
		if (!(node.isRoot()) && (node.getParentNode().isRoot())) {
			writer.addAttribute("POSITION", node.isLeft() ? "left" : "right");
		}
		final boolean saveID = !MapController.saveOnlyIntrinsicallyNeededIds();
		if (saveID) {
			final String id = node.createID();
			writer.addAttribute("ID", id);
		}
		if (node.getHistoryInformation() != null) {
			writer.addAttribute(NodeBuilder.XML_NODE_HISTORY_CREATED_AT, TreeXmlWriter.dateToString(node
			    .getHistoryInformation().getCreatedAt()));
			writer.addAttribute(NodeBuilder.XML_NODE_HISTORY_LAST_MODIFIED_AT, TreeXmlWriter.dateToString(node
			    .getHistoryInformation().getLastModifiedAt()));
		}
		for (int i = 0; i < node.getIcons().size(); ++i) {
			final XMLElement iconElement = new XMLElement();
			iconElement.setName("icon");
			iconElement.setAttribute("BUILTIN", ((MindIcon) node.getIcons().get(i)).getName());
			xmlNode.addChild(iconElement);
		}
		writer.addExtensionAttributes(node, node.getExtensions().values());
	}

	public void writeContent(final ITreeWriter writer, final Object content, final String tag) throws IOException {
		final NodeModel node = (NodeModel) content;
		writer.addExtensionNodes(node, node.getExtensions().values());
		for (int i = 0; i < xmlNode.getChildrenCount(); i++) {
			writer.addElement(null, xmlNode.getChildAtIndex(i));
		}
		if (encryptionModel == null && writeChildren
		        && mapController.childrenUnfolded(node).hasNext()) {
			saveChildren(writer, node);
		}
		return;
	}
}
