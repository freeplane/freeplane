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
package org.freeplane.features.map;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.IElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.link.LinkBuilder;
import org.freeplane.features.map.MapWriter.Hint;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.MapWriter.WriterHint;
import org.freeplane.n3.nanoxml.XMLElement;

public class NodeWriter implements IElementWriter, IAttributeWriter {
	private boolean mayWriteChildren;
	final private MapController mapController;
	final private boolean shouldWriteChildren;
	private final boolean writeFolded;
	final private boolean writeInvisible;
	private XMLElement xmlNode;
	final private String nodeTag;

	public static boolean shouldWriteSharedContent(ITreeWriter writer){
		if (! Boolean.TRUE.equals(writer.getHint(WriterHint.ALREADY_WRITTEN)))
			return true;
		final Object mode = writer.getHint(Hint.MODE);
		return Mode.EXPORT.equals(mode);
	}

	private final Map<SharedNodeData, NodeModel> alreadyWrittenSharedContent;
	private final LinkBuilder linkBuilder;

	public NodeWriter(final MapController mapController, LinkBuilder linkBuilder, final String nodeTag, final boolean writeChildren,
	                  final boolean writeInvisible) {
		this.linkBuilder = linkBuilder;
		alreadyWrittenSharedContent = new HashMap<SharedNodeData, NodeModel>();
		this.mapController = mapController;
		this.shouldWriteChildren = writeChildren;
		this.mayWriteChildren = true;
		this.writeInvisible = writeInvisible;
		this.nodeTag = nodeTag;
		final String saveFolding = ResourceController.getResourceController().getProperty(
		    NodeBuilder.RESOURCES_SAVE_FOLDING);
		writeFolded = saveFolding.equals(NodeBuilder.RESOURCES_ALWAYS_SAVE_FOLDING)
		        || saveFolding.equals(NodeBuilder.RESOURCES_SAVE_FOLDING_IF_MAP_IS_CHANGED);
	}

	private void saveChildren(final ITreeWriter writer, final NodeModel node) throws IOException {
		for (final NodeModel child: mapController.childrenUnfolded(node)) {
		if (writeInvisible || child.isVisible()) {
				writer.setHint(WriterHint.ALREADY_WRITTEN, isAlreadyWritten(child));
				writer.addElement(child, nodeTag);
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
		if (tag.equals(nodeTag)) {
			final NodeModel node = (NodeModel) content;
			writeAttributesGenerateContent(writer, node);
			return;
		}
	}

	private void writeAttributesGenerateContent(final ITreeWriter writer, final NodeModel node) {
		/** fc, 12.6.2005: XML must not contain any zero characters. */
		xmlNode = new XMLElement();
		EncryptionModel encryptionModel = EncryptionModel.getModel(node);
		mayWriteChildren = true;
		final Object mode = mode(writer);
		final boolean isNodeAlreadyWritten = isAlreadyWritten(node);
		if (encryptionModel != null && !(encryptionModel.isAccessible() && Mode.EXPORT.equals(mode)) && ! isNodeAlreadyWritten) {
        	final String enctyptedContent = encryptionModel.calculateEncryptedContent(mapController);
        	if(enctyptedContent != null){
        		writer.addAttribute(NodeBuilder.XML_NODE_ENCRYPTED_CONTENT, enctyptedContent);
        		mayWriteChildren = false;
        	}
        }
		if (mayWriteChildren && !mode.equals(Mode.ADDITIONAL_CONTENT) && (writeFolded || !mode.equals(Mode.FILE))) {
			if(mapController.isFolded(node) && ! isNodeAlreadyWritten){
				writer.addAttribute("FOLDED", "true");
			}
			else if(node.isRoot() && ! Mode.STYLE.equals(mode)){
				writer.addAttribute("FOLDED", "false");
			}
		}
		final NodeModel parentNode = node.getParentNode();
		if (parentNode != null && parentNode.isRoot()) {
			writer.addAttribute("POSITION", node.isLeft() ? "left" : "right");
		}
		final boolean saveID = !mode.equals(Mode.STYLE) && !mode.equals(Mode.ADDITIONAL_CONTENT);
		if (saveID) {
			final String id = node.createID();
			writer.addAttribute("ID", id);
			writeReferenceNodeId(writer, node);
		}
		if(! isNodeAlreadyWritten){
			if (!mode.equals(Mode.STYLE)
					&& node.getHistoryInformation() != null
					&& ResourceController.getResourceController().getBooleanProperty(
						NodeBuilder.RESOURCES_SAVE_MODIFICATION_TIMES)) {
				writer.addAttribute(NodeBuilder.XML_NODE_HISTORY_CREATED_AT, TreeXmlWriter.dateToString(node
					.getHistoryInformation().getCreatedAt()));
				writer.addAttribute(NodeBuilder.XML_NODE_HISTORY_LAST_MODIFIED_AT, TreeXmlWriter.dateToString(node
					.getHistoryInformation().getLastModifiedAt()));
			}
		}
		if(! isNodeAlreadyWritten || Mode.EXPORT.equals(mode)) {
			writeIconSize(writer, node);
			linkBuilder.writeAttributes(writer, node);
			writer.addExtensionAttributes(node, node.getSharedExtensions().values());
		}
		writer.addExtensionAttributes(node, node.getIndividualExtensionValues());
	}

	private void writeIconSize(final ITreeWriter writer, final NodeModel node) {
		final boolean forceFormatting = Boolean.TRUE.equals(writer.getHint(MapWriter.WriterHint.FORCE_FORMATTING));
		Quantity<LengthUnits> iconSize = null;
		if (forceFormatting) {
			final IconController iconController = IconController.getController();
			iconSize = iconController.getIconSize(node);
		} else
			iconSize = node.getSharedData().getIcons().getIconSize();
		if (iconSize != null) {
			writer.addAttribute("ICON_SIZE", iconSize.toString());
		}
	}

	private void writeReferenceNodeId(ITreeWriter writer, NodeModel node) {
	    final NodeModel referenceNode = alreadyWrittenSharedContent.get(node.getSharedData());
	    if(referenceNode != null){
	    	if(referenceNode.isSubtreeCloneOf(node))
	    		writer.addAttribute("TREE_ID", referenceNode.createID());
	    	else
	    		writer.addAttribute("CONTENT_ID", referenceNode.createID());
	    }

    }

	private boolean isAlreadyWritten(final NodeModel node) {
	    return alreadyWrittenSharedContent.containsKey(node.getSharedData());
    }

	private void registerWrittenNode(final NodeModel node) {
	    alreadyWrittenSharedContent.put(node.getSharedData(), node);
    }

	public void writeContent(final ITreeWriter writer, final Object content, final String tag) throws IOException {
		final NodeModel node = (NodeModel) content;
		writer.addExtensionNodes(node, node.getIndividualExtensionValues());
		final boolean isNodeContentWrittenFirstTime = ! isAlreadyWritten(node);
		if(isNodeContentWrittenFirstTime)
			registerWrittenNode(node);
		linkBuilder.writeContent(writer, node);
		if(isNodeContentWrittenFirstTime || Mode.EXPORT.equals(mode(writer))){
			writer.addExtensionNodes(node, node.getSharedExtensions().values());
			for (int i = 0; i < xmlNode.getChildrenCount(); i++) {
				writer.addElement(null, xmlNode.getChildAtIndex(i));
			}
		}
		if (mayWriteChildren && shouldWriteChildren && mapController.childrenUnfolded(node).size()>0) {
			saveChildren(writer, node);
		}
	}

	private Object mode(final ITreeWriter writer) {
	    return writer.getHint(Hint.MODE);
    }

	String getNodeTag() {
		return nodeTag;
	}

	void registerBy(WriteManager writeManager) {
		writeManager.addElementWriter(getNodeTag(), this);
		writeManager.addAttributeWriter(getNodeTag(), this);
	}

	void unregisterFrom(WriteManager writeManager) {
		writeManager.removeElementWriter(getNodeTag(), this);
		writeManager.removeAttributeWriter(getNodeTag(), this);
	}
}
