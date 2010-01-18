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
package org.freeplane.core.model;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.ExtensionContainer;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.filter.Filter;
import org.freeplane.core.filter.FilterController;
import org.freeplane.core.icon.IconRegistry;
import org.freeplane.core.modecontroller.IMapChangeListener;
import org.freeplane.core.modecontroller.MapChangeEvent;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.resources.ResourceBundles;

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
	final private IconRegistry iconRegistry;
	final private List<IMapChangeListener> listeners;
	final private Map<String, NodeModel> nodes;
	private boolean readOnly = true;
	private NodeModel root;
	private URL url;

	public MapModel(final ModeController modeController, NodeModel root) {
		extensionContainer = new ExtensionContainer(new HashMap<Class<? extends IExtension>, IExtension>());
		this.root = root;
		listeners = new LinkedList<IMapChangeListener>();
		final Controller controller = modeController.getController();
		nodes = new HashMap<String, NodeModel>();
		filter = FilterController.getController(controller).createTransparentFilter();
		if (root == null) {
			root = new NodeModel(ResourceBundles.getText("new_mindmap"), this);
			setRoot(root);
		}
		else {
			root.setMap(this);
		}
		iconRegistry = new IconRegistry(modeController.getMapController(), this);
	}

	public void addExtension(final Class<? extends IExtension> clazz, final IExtension extension) {
		extensionContainer.addExtension(clazz, extension);
	}

	public void addExtension(final IExtension extension) {
		extensionContainer.addExtension(extension);
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
		if (proposedID != null 
				&& ! "".equals(proposedID)
				&& getNodeForID(proposedID) == null) {
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

	public IExtension getExtension(final Class<? extends IExtension> clazz) {
		return extensionContainer.getExtension(clazz);
	}

	public Map<Class<? extends IExtension>, IExtension> getExtensions() {
		return extensionContainer.getExtensions();
	}

	/**
	 * Change this to always return null if your model doesn't support files.
	 */
	public File getFile() {
		return url != null ? new File(url.getFile()) : null;
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
		return readOnly;
	}

	public boolean isSaved() {
		return (changesPerformedSinceLastSave == 0);
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
		final String id = nodeModel.getID();
		if (id != null) {
			registryID(id, nodeModel);
		}
		final Iterator<NodeModel> iterator = nodeModel.getChildren().iterator();
		while (iterator.hasNext()) {
			final NodeModel next = iterator.next();
			registryNodeRecursive(next);
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

	public void unregistryNodes(NodeModel node) {
		final List<NodeModel> children = node.getChildren();
		for(NodeModel child:children){
			unregistryNodes(child);
		}
		final String id = node.getID();
		if(id != null){
			nodes.put(id, null);
		}
    }
}
