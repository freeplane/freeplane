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

import java.util.HashMap;

import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.model.EncryptionModel;
import org.freeplane.core.model.HistoryInformationModel;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.n3.nanoxml.IXMLElement;

public class NodeBuilder implements IElementDOMHandler {
	static class IconProperties {
		String iconName;
	}

	public static final String XML_NODE = "node";
	public static final String XML_NODE_ADDITIONAL_INFO = "ADDITIONAL_INFO";
	public static final String XML_NODE_CLASS = "AA_NODE_CLASS";
	public static final String XML_NODE_ENCRYPTED_CONTENT = "ENCRYPTED_CONTENT";
	public static final String XML_NODE_HISTORY_CREATED_AT = "CREATED";
	public static final String XML_NODE_HISTORY_LAST_MODIFIED_AT = "MODIFIED";
	private NodeModel mapChild = null;
	private final MapReader mapReader;
	private final HashMap<String, String> newIds;

	public NodeBuilder(final MapReader mapController) {
		mapReader = mapController;
		newIds = new HashMap<String, String>();
	}

	public Object createElement(final Object parent, final String tag, final IXMLElement attributes) {
		if (tag.equals(NodeBuilder.XML_NODE)) {
			final NodeModel userObject = createNode();
			if (mapChild == null) {
				mapChild = userObject;
			}
			return userObject;
		}
		return null;
	}

	protected void createEncryptedNode(final NodeModel node, final String additionalInfo) {
		final EncryptionModel encryptionModel = new EncryptionModel(node, additionalInfo);
		node.addExtension(encryptionModel);
	}

	public NodeModel createNode() {
		return new NodeModel(getMap());
	}

	public void endElement(final Object parentObject, final String tag, final Object userObject,
	                       final IXMLElement dom) {
		final NodeModel node = (NodeModel) userObject;
		if (dom.getAttributeCount() != 0 || dom.hasChildren()) {
			node.addExtension(new UnknownElements(dom));
		}
		if (tag.equals("node") && parentObject instanceof MapModel) {
			mapChild = node;
			return;
		}
		if (parentObject instanceof NodeModel) {
			final NodeModel parentNode = (NodeModel) parentObject;
			if (tag.equals("node") && userObject instanceof NodeModel) {
				parentNode.insert(node, -1);
			}
			return;
		}
	}

	private MapModel getMap() {
		return mapReader.getCreatedMap();
	}

	public NodeModel getMapChild() {
		return mapChild;
	}

	public HashMap<String, String> getNewIds() {
		final HashMap<String, String> ids = newIds;
		return ids;
	}

	private void registerAttributeHandlers(final ReadManager reader) {
		reader.addAttributeHandler(NodeBuilder.XML_NODE, NodeBuilder.XML_NODE_ENCRYPTED_CONTENT,
		    new IAttributeHandler() {
			    public void setAttribute(final Object userObject, final String value) {
				    createEncryptedNode((NodeModel) userObject, value);
			    }
		    });
		reader.addAttributeHandler(NodeBuilder.XML_NODE, NodeBuilder.XML_NODE_HISTORY_CREATED_AT,
		    new IAttributeHandler() {
			    public void setAttribute(final Object userObject, final String value) {
				    final NodeModel node = (NodeModel) userObject;
				    if (node.getHistoryInformation() == null) {
					    node.setHistoryInformation(new HistoryInformationModel());
				    }
				    node.getHistoryInformation().setCreatedAt(TreeXmlReader.xmlToDate(value));
			    }
		    });
		reader.addAttributeHandler(NodeBuilder.XML_NODE,
		    NodeBuilder.XML_NODE_HISTORY_LAST_MODIFIED_AT, new IAttributeHandler() {
			    public void setAttribute(final Object userObject, final String value) {
				    final NodeModel node = (NodeModel) userObject;
				    if (node.getHistoryInformation() == null) {
					    node.setHistoryInformation(new HistoryInformationModel());
				    }
				    node.getHistoryInformation().setLastModifiedAt(TreeXmlReader.xmlToDate(value));
			    }
		    });
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "FOLDED", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				if (value.equals("true")) {
					node.setFolded(true);
				}
			}
		});
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "POSITION", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				node.setLeft(value.equals("left"));
			}
		});
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "ID", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				final String realId = getMap().generateNodeID(value);
				node.setID(realId);
				if (!realId.equals(value)) {
					newIds.put(value, realId);
				}
			}
		});
	}

	/**
	 */
	public void registerBy(final ReadManager reader) {
		registerAttributeHandlers(reader);
		reader.addElementHandler(NodeBuilder.XML_NODE, this);
	}

	public void reset() {
		mapChild = null;
	}

	public void setAttributes(final String tag, final Object node, final IXMLElement attributes) {
	}
}
