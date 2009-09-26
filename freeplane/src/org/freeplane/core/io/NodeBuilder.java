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

import org.freeplane.core.io.MapWriter.Hint;
import org.freeplane.core.io.MapWriter.Mode;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.model.EncryptionModel;
import org.freeplane.core.model.HistoryInformationModel;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.n3.nanoxml.XMLElement;

public class NodeBuilder implements IElementDOMHandler {
	static class IconProperties {
		String iconName;
	}

	protected static final String FOLDING_LOADED = "folding_loaded";
	public static final String RESOURCES_ALWAYS_FOLD_ALL_AFTER_LOAD = "always_fold_all_after_load";
	public static final String RESOURCES_ALWAYS_SAVE_FOLDING = "always_save_folding";
	public static final String RESOURCES_ALWAYS_UNFOLD_ALL_AFTER_LOAD = "always_unfold_all_after_load";
	public static final String RESOURCES_LOAD_FOLDING = "load_folding";
	public static final String RESOURCES_LOAD_FOLDING_FROM_MAP_DEFAULT_FOLD_ALL = "load_folding_from_map_default_fold_all";
	public static final String RESOURCES_LOAD_FOLDING_FROM_MAP_DEFAULT_UNFOLD_ALL = "load_folding_from_map_default_unfold_all";
	public static final String RESOURCES_NEVER_SAVE_FOLDING = "never_save_folding";
	public static final String RESOURCES_SAVE_FOLDING = "save_folding";
	public static final String RESOURCES_SAVE_FOLDING_IF_MAP_IS_CHANGED = "save_folding_if_map_is_changed";
	public static final String RESOURCES_SAVE_MODIFICATION_TIMES = "save_modification_times";
	public static final String XML_NODE = "node";
	public static final String XML_STYLENODE = "stylenode";
	public static final String XML_NODE_ADDITIONAL_INFO = "ADDITIONAL_INFO";
	public static final String XML_NODE_CLASS = "AA_NODE_CLASS";
	public static final String XML_NODE_ENCRYPTED_CONTENT = "ENCRYPTED_CONTENT";
	public static final String XML_NODE_HISTORY_CREATED_AT = "CREATED";
	public static final String XML_NODE_HISTORY_LAST_MODIFIED_AT = "MODIFIED";
	private final MapReader mapReader;

	NodeBuilder(final MapReader mapReader) {
		this.mapReader = mapReader;
	}

	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		final NodeModel userObject = createNode();
		if (getMapChild() == null) {
			setMapChild(userObject);
		}
		return userObject;
	}

	public NodeModel createNode() {
		return new NodeModel(getMap());
	}

	public void endElement(final Object parentObject, final String tag, final Object userObject, final XMLElement dom) {
		final NodeModel node = (NodeModel) userObject;
		if (dom.getAttributeCount() != 0 || dom.hasChildren()) {
			node.addExtension(new UnknownElements(dom));
		}
		if (parentObject instanceof MapModel) {
			setMapChild(node);
			return;
		}
		if (parentObject instanceof NodeModel) {
			final NodeModel parentNode = (NodeModel) parentObject;
			if (userObject instanceof NodeModel) {
				parentNode.insert(node, -1);
			}
			return;
		}
	}

	private MapModel getMap() {
		return mapReader.getCurrentNodeTreeCreator().getCreatedMap();
	}

	public NodeModel getMapChild() {
		return mapReader.getCurrentNodeTreeCreator().getMapChild();
	}

	private void registerAttributeHandlers(final ReadManager reader) {
		reader.addAttributeHandler(NodeBuilder.XML_NODE, NodeBuilder.XML_NODE_ENCRYPTED_CONTENT,
		    new IAttributeHandler() {
			private void createEncryptedNode(final NodeModel node, final String additionalInfo) {
				final EncryptionModel encryptionModel = new EncryptionModel(node, additionalInfo);
				node.addExtension(encryptionModel);
			}

			    public void setAttribute(final Object userObject, final String value) {
				    final NodeModel node = (NodeModel) userObject;
					createEncryptedNode(node, value);
					node.setFolded(true);
				    
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
		reader.addAttributeHandler(NodeBuilder.XML_NODE, NodeBuilder.XML_NODE_HISTORY_LAST_MODIFIED_AT,
		    new IAttributeHandler() {
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
				final Object mode = mapReader.getCurrentNodeTreeCreator().getHint(Hint.MODE);
				if (mode.equals(Mode.FILE)) {
					final String loadFolding = ResourceController.getResourceController().getProperty(
					    NodeBuilder.RESOURCES_LOAD_FOLDING);
					if (loadFolding.equals(NodeBuilder.RESOURCES_ALWAYS_FOLD_ALL_AFTER_LOAD)
					        || loadFolding.equals(NodeBuilder.RESOURCES_ALWAYS_UNFOLD_ALL_AFTER_LOAD)) {
						return;
					}
					mapReader.getCurrentNodeTreeCreator().setHint(FOLDING_LOADED, Boolean.TRUE);
				}
				if (value.equals("true")) {
					node.setFolded(true);
				}
			}
		});
		reader.addReadCompletionListener(new IReadCompletionListener() {
			private void foldAll(final NodeModel node) {
				if (node.getChildCount() == 0) {
					return;
				}
				if (!node.getText().equals("")) {
					node.setFolded(true);
				}
				for (final NodeModel child : node.getChildren()) {
					foldAll(child);
				}
			}

			public void readingCompleted(final NodeModel topNode, final HashMap<String, String> newIds) {
				if (!Mode.FILE.equals(mapReader.getCurrentNodeTreeCreator().getHint(Hint.MODE))) {
					return;
				}
				if (Boolean.TRUE.equals(mapReader.getCurrentNodeTreeCreator().getHint(NodeBuilder.FOLDING_LOADED))) {
					return;
				}
				final String loadFolding = ResourceController.getResourceController().getProperty(
				    NodeBuilder.RESOURCES_LOAD_FOLDING);
				if (loadFolding.equals(NodeBuilder.RESOURCES_ALWAYS_FOLD_ALL_AFTER_LOAD)
				        || loadFolding.equals(NodeBuilder.RESOURCES_LOAD_FOLDING_FROM_MAP_DEFAULT_FOLD_ALL)) {
					for (final NodeModel child : topNode.getChildren()) {
						foldAll(child);
					}
				}
			}
		});
		final IAttributeHandler positionHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				node.setLeft(value.equals("left"));
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "POSITION", positionHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "POSITION", positionHandler);
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "ID", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				final String realId = getMap().generateNodeID(value);
				node.setID(realId);
				if (!realId.equals(value)) {
					mapReader.getCurrentNodeTreeCreator().substituteNodeID(value, realId);
				}
			}
		});
	}

	/**
	 */
	public void registerBy(final ReadManager reader) {
		registerAttributeHandlers(reader);
		reader.addElementHandler(NodeBuilder.XML_NODE, this);
		reader.addElementHandler(NodeBuilder.XML_STYLENODE, this);
	}

	public void reset() {
		setMapChild(null);
	}

	public void setAttributes(final String tag, final Object node, final XMLElement attributes) {
	}

	private void setMapChild(NodeModel mapChild) {
	    mapReader.getCurrentNodeTreeCreator().setMapChild(mapChild);
    }
}
