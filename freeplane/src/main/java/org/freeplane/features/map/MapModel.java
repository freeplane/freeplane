/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.freeplane.core.extension.ExtensionContainer;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.icon.IconRegistry;
import org.freeplane.features.map.mindmapmode.DocuMapAttribute;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class MapModel {
	private static Random ran = new Random();
	private static final int UNDEFINED_NODE_ID = 2000000000;
	/**
	 * denotes the amount of changes since the last save. The initial value is
	 * zero, such that new models are not to be saved.
	 */
	protected int changesPerformedSinceLastSave = 0;
	private final ExtensionContainer extensionContainer;
	private Filter filter = null;
	private IconRegistry iconRegistry;
	final private List<IMapChangeListener> listeners;
	final private Map<String, NodeModel> nodes;
	private boolean readOnly = false;
	private NodeModel root;
	private URL url;
	private NodeChangeAnnouncer nodeChangeAnnouncer;

	public MapModel(IconRegistry iconRegistry, NodeChangeAnnouncer nodeChangeAnnouncer) {
		extensionContainer = new ExtensionContainer(new HashMap<Class<? extends IExtension>, IExtension>());
		this.root = null;
		listeners = new LinkedList<IMapChangeListener>();
		nodes = new HashMap<String, NodeModel>();
		final FilterController filterController = FilterController.getCurrentFilterController();
		if (filterController != null) {
			filter = filterController.createTransparentFilter();
		}
		this.iconRegistry = iconRegistry;
		this.nodeChangeAnnouncer = nodeChangeAnnouncer;
	}

	public MapModel() {
		this(null, null);
		final ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		iconRegistry = new IconRegistry(mapController, this);
		this.nodeChangeAnnouncer = mapController;
	}

	public void createNewRoot() {
		root = new NodeModel(TextUtils.getText("new_mindmap"), this);
		root.attach();
	}

	public void addExtension(final Class<? extends IExtension> clazz, final IExtension extension) {
		extensionContainer.addExtension(clazz, extension);
	}

	public void addExtension(final IExtension extension) {
		extensionContainer.addExtension(extension);
	}

	public IExtension putExtension(final Class<? extends IExtension> clazz, final IExtension extension) {
		return extensionContainer.putExtension(clazz, extension);
	}

	public IExtension putExtension(final IExtension extension) {
		return extensionContainer.putExtension(extension);
	}

	public boolean containsExtension(Class<? extends IExtension> clazz) {
		return extensionContainer.containsExtension(clazz);
	}

	public void addMapChangeListener(final IMapChangeListener listener) {
		listeners.add(listener);
	}

	public void destroy() {
	}

	public void fireMapChangeEvent(final MapChangeEvent event) {
		for (final IMapChangeListener listener : listeners) {
			listener.mapChanged(event);
		}
	}

	public String generateNodeID(final String proposedID) {
		if (proposedID != null && !"".equals(proposedID) && getNodeForID(proposedID) == null) {
			return proposedID;
		}
		String returnValue;
		do {
			final String prefix = "ID_";
			/*
			 * The prefix is to enable the id to be an ID in the sense of
			 * XML/DTD.
			 */
			returnValue = prefix + Integer.toString(ran.nextInt(UNDEFINED_NODE_ID));
		} while (nodes.containsKey(returnValue));
		return returnValue;
	}

	public <T extends IExtension> T getExtension(final Class<T> clazz) {
		return extensionContainer.getExtension(clazz);
	}

	public Map<Class<? extends IExtension>, IExtension> getExtensions() {
		return extensionContainer.getExtensions();
	}

	/**
	 * Change this to always return null if your model doesn't support files.
	 */
	public File getFile() {
			return url != null && url.getProtocol().equals("file") ? Compat.urlToFile(url) : null;
	}

	public Filter getFilter() {
		return filter;
	}

	public IconRegistry getIconRegistry() {
		return iconRegistry;
	}

	/**
	 * @param nodeID
	 * @return
	 */
	public NodeModel getNodeForID(final String nodeID) {
		final NodeModel node = nodes.get(nodeID);
		return node;
	}

	public int getNumberOfChangesSinceLastSave() {
		return changesPerformedSinceLastSave;
	}

	public NodeModel getRootNode() {
		return root;
	}

	public String getTitle() {
		if (getURL() == null) {
			return null;
		}
		else {
			return getURL().toString();
		}
	}

	/**
	 * Get the value of url.
	 *
	 * @return Value of url.
	 */
	public URL getURL() {
		return url;
	}

	public boolean isReadOnly() {
		return readOnly || isDocumentation();
	}

	public boolean isDocumentation() {
		return containsExtension(DocuMapAttribute.class);
	}

	public boolean isSaved() {
		return changesPerformedSinceLastSave == 0;
	}

	/**
	 * @param value
	 * @param nodeModel
	 */
	void registryID(final String value, final NodeModel nodeModel) {
		final NodeModel old = nodes.put(value, nodeModel);
		if (null != old && nodeModel != old) {
			throw new RuntimeException("id " + value + " already registered");
		}
	}

	/**
	 * @param nodeModel
	 * @return
	 */
	public String registryNode(final NodeModel nodeModel) {
		final String id = generateNodeID(nodeModel.getID());
		registryID(id, nodeModel);
		return id;
	}

	public void registryNodeRecursive(final NodeModel nodeModel) {
		registryNodeRecursive(nodeModel, 0);
	}

	private void registryNodeRecursive(final NodeModel nodeModel, final int depth) {
		if (depth > 400) {
			throw new StackOverflowError();
		}
		final String id = nodeModel.getID();
		if (id != null) {
			registryID(id, nodeModel);
		}
		final Iterator<NodeModel> iterator = nodeModel.getChildren().iterator();
		while (iterator.hasNext()) {
			final NodeModel next = iterator.next();
			registryNodeRecursive(next, depth + 1);
		}
	}

	public IExtension removeExtension(final Class<? extends IExtension> clazz) {
		return extensionContainer.removeExtension(clazz);
	}

	public boolean removeExtension(final IExtension extension) {
		return extensionContainer.removeExtension(extension);
	};

	public void removeMapChangeListener(final IMapChangeListener listener) {
		listeners.remove(listener);
	};

	public void setFilter(final Filter filter) {
		this.filter = filter;
	}

	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
	}

	public void setRoot(final NodeModel root) {
		this.root = root;
		root.attach();
		root.setMap(this);
	}

	/**
	 * Counts the amount of actions performed.
	 *
	 * @param saved
	 *            true if the file was saved recently. False otherwise.
	 */
	public void setSaved(final boolean saved) {
		if (saved) {
			changesPerformedSinceLastSave = 0;
		}
		else {
			++changesPerformedSinceLastSave;
		}
	}

	/**
	 * Set the value of url.
	 *
	 * @param v
	 *            Value to assign to url.
	 */
	public void setURL(final URL v) {
		url = v;
	}

	public void unregistryNodes(final NodeModel node) {
		final List<NodeModel> children = node.getChildren();
		for (final NodeModel child : children) {
			unregistryNodes(child);
		}
		final String id = node.getID();
		if (id != null) {
			nodes.put(id, null);
		}
	}


	public NodeChangeAnnouncer getNodeChangeAnnouncer() {
		return nodeChangeAnnouncer;
	}

	public boolean close() {
		Controller.getCurrentModeController().getMapController().closeWithoutSaving(this);
		return true;
	}
}
