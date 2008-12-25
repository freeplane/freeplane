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

import java.util.HashMap;

import org.freeplane.io.IAttributeHandler;
import org.freeplane.io.INodeCreator;
import org.freeplane.io.ReadManager;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;
import org.freeplane.main.Tools;
import org.freeplane.modes.mindmapmode.EncryptionModel;

public class NodeBuilder implements INodeCreator {
	static class IconProperties {
		String iconName;
	}

	public static class NodeObject {
		public NodeModel node;

		public NodeObject(final NodeModel node) {
			super();
			this.node = node;
		}
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

	public void completeNode(final Object parent, final String tag, final Object userObject) {
		if (tag.equals("node") && parent instanceof MapModel) {
			mapChild = ((NodeObject) userObject).node;
			return;
		}
		if (parent instanceof NodeObject) {
			final NodeModel node = ((NodeObject) parent).node;
			if (tag.equals("node") && userObject instanceof NodeObject) {
				node.insert(((NodeObject) userObject).node, -1);
			}
			return;
		}
	}

	protected void createEncryptedNode(final NodeModel node, final String additionalInfo) {
		final EncryptionModel encryptionModel = new EncryptionModel(node, additionalInfo);
		node.addExtension(encryptionModel);
	}

	public NodeModel createNode() {
		return new NodeModel(getMap());
	}

	public Object createNode(final Object parent, final String tag) {
		if (tag.equals("map") || tag.equals("attribute_registry")) {
			return getMap();
		}
		if (tag.equals(NodeBuilder.XML_NODE)) {
			final NodeModel userObject = createNode();
			if (mapChild == null) {
				mapChild = userObject;
			}
			return new NodeObject(userObject);
		}
		return null;
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
			    public void parseAttribute(final Object userObject, final String value) {
				    final NodeObject nodeObject = (NodeObject) userObject;
				    createEncryptedNode(nodeObject.node, value);
			    }
		    });
		reader.addAttributeHandler(NodeBuilder.XML_NODE, NodeBuilder.XML_NODE_HISTORY_CREATED_AT,
		    new IAttributeHandler() {
			    public void parseAttribute(final Object userObject, final String value) {
				    final NodeModel node = ((NodeObject) userObject).node;
				    if (node.getHistoryInformation() == null) {
					    node.setHistoryInformation(new HistoryInformationModel());
				    }
				    node.getHistoryInformation().setCreatedAt(Tools.xmlToDate(value));
			    }
		    });
		reader.addAttributeHandler(NodeBuilder.XML_NODE,
		    NodeBuilder.XML_NODE_HISTORY_LAST_MODIFIED_AT, new IAttributeHandler() {
			    public void parseAttribute(final Object userObject, final String value) {
				    final NodeModel node = ((NodeObject) userObject).node;
				    if (node.getHistoryInformation() == null) {
					    node.setHistoryInformation(new HistoryInformationModel());
				    }
				    node.getHistoryInformation().setLastModifiedAt(Tools.xmlToDate(value));
			    }
		    });
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "FOLDED", new IAttributeHandler() {
			public void parseAttribute(final Object userObject, final String value) {
				final NodeModel node = ((NodeObject) userObject).node;
				if (value.equals("true")) {
					node.setFolded(true);
				}
			}
		});
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "POSITION", new IAttributeHandler() {
			public void parseAttribute(final Object userObject, final String value) {
				final NodeModel node = ((NodeObject) userObject).node;
				node.setLeft(value.equals("left"));
			}
		});
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "ID", new IAttributeHandler() {
			public void parseAttribute(final Object userObject, final String value) {
				final NodeModel node = ((NodeObject) userObject).node;
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
		reader.addNodeCreator("map", this);
		registerAttributeHandlers(reader);
		reader.addNodeCreator(NodeBuilder.XML_NODE, this);
	}

	public void reset() {
		mapChild = null;
	}

	public void setAttributes(final String tag, final Object node, final IXMLElement attributes) {
	}
}
