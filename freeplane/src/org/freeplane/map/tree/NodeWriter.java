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
package org.freeplane.map.tree;

import java.io.IOException;
import java.util.ListIterator;

import org.freeplane.io.IAttributeWriter;
import org.freeplane.io.IElementWriter;
import org.freeplane.io.ITreeWriter;
import org.freeplane.io.xml.TreeXmlWriter;
import org.freeplane.io.xml.n3.nanoxml.XMLElement;
import org.freeplane.map.icon.MindIcon;
import org.freeplane.map.text.HtmlTools;
import org.freeplane.map.text.NodeTextBuilder;
import org.freeplane.modes.mindmapmode.EncryptionModel;

class NodeWriter implements IElementWriter, IAttributeWriter {
	private EncryptionModel encryptionModel;
	private boolean isTextNode;
	final private boolean saveOnlyIntrinsicallyNeededIds;
	final private boolean writeChildren;
	final private boolean writeInvisible;
	private XMLElement xmlNode;

	public NodeWriter(final MapController mapController, final boolean writeChildren,
	                  final boolean writeInvisible, final boolean saveOnlyIntrinsicallyNeededIds) {
		this.writeChildren = writeChildren;
		this.writeInvisible = writeInvisible;
		this.saveOnlyIntrinsicallyNeededIds = saveOnlyIntrinsicallyNeededIds;
	}

	private void saveChildren(final ITreeWriter writer, final NodeModel node) throws IOException {
		for (final ListIterator e = node.getModeController().getMapController().childrenUnfolded(
		    node); e.hasNext();) {
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
			final NodeModel nodeAdapter = (NodeModel) content;
			writeAttributesGenerateContent(writer, nodeAdapter);
			return;
		}
	}

	private void writeAttributesGenerateContent(final ITreeWriter writer, final NodeModel node) {
		/** fc, 12.6.2005: XML must not contain any zero characters. */
		final String text = node.toString().replace('\0', ' ');
		isTextNode = !HtmlTools.isHtmlNode(text);
		if (isTextNode) {
			writer.addAttribute(NodeTextBuilder.XML_NODE_TEXT, text);
		}
		xmlNode = new XMLElement();
		encryptionModel = node.getEncryptionModel();
		if (encryptionModel != null) {
			final String additionalInfo = encryptionModel.getEncryptedContent();
			writer.addAttribute(NodeBuilder.XML_NODE_ENCRYPTED_CONTENT, additionalInfo);
		}
		if (node.getModeController().getMapController().isFolded(node)) {
			writer.addAttribute("FOLDED", "true");
		}
		if (!(node.isRoot()) && (node.getParentNode().isRoot())) {
			writer.addAttribute("POSITION", node.isLeft() ? "left" : "right");
		}
		final String id = node.createID();
		final boolean saveID = saveOnlyIntrinsicallyNeededIds
		        || !node.getModeController().getLinkController().getLinksTo(node).isEmpty();
		if (saveID) {
			if (id != null) {
				writer.addAttribute("ID", id);
			}
		}
		if (node.getHistoryInformation() != null) {
			writer.addAttribute(NodeBuilder.XML_NODE_HISTORY_CREATED_AT, TreeXmlWriter
			    .dateToString(node.getHistoryInformation().getCreatedAt()));
			writer.addAttribute(NodeBuilder.XML_NODE_HISTORY_LAST_MODIFIED_AT, TreeXmlWriter
			    .dateToString(node.getHistoryInformation().getLastModifiedAt()));
		}
		for (int i = 0; i < node.getIcons().size(); ++i) {
			final XMLElement iconElement = new XMLElement();
			iconElement.setName("icon");
			iconElement.setAttribute("BUILTIN", ((MindIcon) node.getIcons().get(i)).getName());
			xmlNode.addChild(iconElement);
		}
		writer.addExtensionAttributes(node, node.getExtensions());
	}

	private void writeContent(final ITreeWriter writer, final NodeModel node) throws IOException {
		writer.addExtensionNodes(node, node.getExtensions());
		if (!isTextNode) {
			final XMLElement htmlElement = new XMLElement();
			htmlElement.setName(NodeTextBuilder.XML_NODE_XHTML_CONTENT_TAG);
			htmlElement.setAttribute(NodeTextBuilder.XML_NODE_XHTML_TYPE_TAG,
			    NodeTextBuilder.XML_NODE_XHTML_TYPE_NODE);
			final String content = node.getXmlText().replace('\0', ' ');
			writer.addElement(content, htmlElement);
		}
		for (int i = 0; i < xmlNode.getChildrenCount(); i++) {
			writer.addElement(null, xmlNode.getChildAtIndex(i));
		}
		if (encryptionModel == null && writeChildren
		        && node.getModeController().getMapController().childrenUnfolded(node).hasNext()) {
			saveChildren(writer, node);
		}
	}

	public void writeContent(final org.freeplane.io.ITreeWriter writer, final Object content,
	                         final String tag) throws IOException {
		if (tag.equals(NodeTextBuilder.XML_NODE_XHTML_CONTENT_TAG)) {
			writer.addElementContent((String) content);
			return;
		}
		if (tag.equals(NodeBuilder.XML_NODE)) {
			final NodeModel nodeAdapter = (NodeModel) content;
			writeContent(writer, nodeAdapter);
			return;
		}
	}
}
