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
package org.freeplane.core.map;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.ExtensionHashMap;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.extension.IExtensionCollection;
import org.freeplane.core.filter.DefaultFilter;
import org.freeplane.core.filter.IFilter;
import org.freeplane.core.filter.condition.NoFilteringCondition;

public class MapModel extends DefaultTreeModel {
	private static Random ran = new Random();
	private static final int UNDEFINED_NODE_ID = 2000000000;
	/**
	 * denotes the amount of changes since the last save. The initial value is
	 * zero, such that new models are not to be saved.
	 */
	protected int changesPerformedSinceLastSave = 0;
	final private ExtensionHashMap extensions;
	private IFilter filter = null;
	protected final ModeController mModeController;
	final private HashMap<String, NodeModel> nodes;
	private boolean readOnly = true;
	final private MapRegistry registry;
	private URL url;

	public MapModel(final ModeController modeController, NodeModel root) {
		super(root);
		extensions = new ExtensionHashMap();
		mModeController = modeController;
		nodes = new HashMap<String, NodeModel>();
		registry = new MapRegistry(this, modeController);
		filter = new DefaultFilter(NoFilteringCondition.createCondition(), true, false);
		if (root == null) {
			root = new NodeModel(Controller.getText("new_mindmap"), this);
			setRoot(root);
		}
		else {
			root.setMap(this);
		}
	}

	public boolean addExtension(final Class clazz, final IExtension extension) {
		return extensions.addExtension(clazz, extension);
	}

	public boolean addExtension(final IExtension extension) {
		return extensions.addExtension(extension);
	}

	public boolean containsExtension(final Class clazz) {
		return extensions.containsExtension(clazz);
	}

	protected void destroy() {
	}

	public Iterator extensionIterator() {
		return extensions.extensionIterator();
	}

	public Iterator extensionIterator(final Class clazz) {
		return extensions.extensionIterator(clazz);
	}

	@Override
	protected void fireTreeNodesChanged(final Object source, final Object[] path,
	                                    final int[] childIndices, final Object[] children) {
		final Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		e = fireTreeNodesChanged(source, path, childIndices, children, listeners, e);
		final NodeModel node = (NodeModel) path[path.length - 1];
		fireTreeNodesChanged(source, path, childIndices, children, node.getListeners()
		    .getListenerList(), e);
	}

	protected TreeModelEvent fireTreeNodesChanged(final Object source, final Object[] path,
	                                              final int[] childIndices,
	                                              final Object[] children,
	                                              final Object[] listeners, TreeModelEvent e) {
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				if (e == null) {
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
			}
		}
		return e;
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 *
	 * @param source
	 *            the node being changed
	 * @param path
	 *            the path to the root node
	 * @param childIndices
	 *            the indices of the changed elements
	 * @param children
	 *            the changed elements
	 * @see EventListenerList
	 */
	@Override
	protected void fireTreeNodesInserted(final Object source, final Object[] path,
	                                     final int[] childIndices, final Object[] children) {
		for (int i = 0; i < children.length; i++) {
			registryNodeRecursive((NodeModel) children[i]);
		}
		final Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		e = fireTreeNodesInserted(source, path, childIndices, children, listeners, e);
		final NodeModel node = (NodeModel) path[path.length - 1];
		fireTreeNodesInserted(source, path, childIndices, children, node.getListeners()
		    .getListenerList(), e);
	}

	protected TreeModelEvent fireTreeNodesInserted(final Object source, final Object[] path,
	                                               final int[] childIndices,
	                                               final Object[] children,
	                                               final Object[] listeners, TreeModelEvent e) {
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				if (e == null) {
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
			}
		}
		return e;
	}

	@Override
	protected void fireTreeNodesRemoved(final Object source, final Object[] path,
	                                    final int[] childIndices, final Object[] children) {
		for (int i = 0; i < children.length; i++) {
			unregistryNode((NodeModel) children[i]);
		}
		final Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		e = fireTreeNodesRemoved(source, path, childIndices, children, listeners, e);
		final NodeModel node = (NodeModel) path[path.length - 1];
		fireTreeNodesRemoved(source, path, childIndices, children, node.getListeners()
		    .getListenerList(), e);
	}

	protected TreeModelEvent fireTreeNodesRemoved(final Object source, final Object[] path,
	                                              final int[] childIndices,
	                                              final Object[] children,
	                                              final Object[] listeners, TreeModelEvent e) {
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				if (e == null) {
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
			}
		}
		return e;
	}

	@Override
	protected void fireTreeStructureChanged(final Object source, final Object[] path,
	                                        final int[] childIndices, final Object[] children) {
		final Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		e = fireTreeStructureChanged(source, path, childIndices, children, listeners, e);
		final NodeModel node = (NodeModel) path[path.length - 1];
		fireTreeStructureChanged(source, path, childIndices, children, node.getListeners()
		    .getListenerList(), e);
	}

	protected TreeModelEvent fireTreeStructureChanged(final Object source, final Object[] path,
	                                                  final int[] childIndices,
	                                                  final Object[] children,
	                                                  final Object[] listeners, TreeModelEvent e) {
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				if (e == null) {
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
			}
		}
		return e;
	}

	String generateNodeID(final String proposedID) {
		String myProposedID = new String((proposedID != null) ? proposedID : "");
		String returnValue;
		do {
			if (!myProposedID.equals("")) {
				returnValue = myProposedID;
				myProposedID = "";
			}
			else {
				final String prefix = "ID_";
				/*
				 * The prefix is to enable the id to be an ID in the sense of
				 * XML/DTD.
				 */
				returnValue = prefix + Integer.toString(ran.nextInt(UNDEFINED_NODE_ID));
			}
		} while (nodes.get(returnValue) != null);
		return returnValue;
	}

	public IExtension getExtension(final Class clazz) {
		return extensions.getExtension(clazz);
	}

	IExtensionCollection getExtensions() {
		return extensions;
	}

	/**
	 * Change this to always return null if your model doesn't support files.
	 */
	public File getFile() {
		return url != null ? new File(url.getFile()) : null;
	}

	public IFilter getFilter() {
		return filter;
	}

	public ModeController getModeController() {
		return mModeController;
	}

	/**
	 * @param nodeID
	 * @return
	 */
	public NodeModel getNodeForID(final String nodeID) {
		return nodes.get(nodeID);
	}

	public int getNumberOfChangesSinceLastSave() {
		return changesPerformedSinceLastSave;
	}

	public MapRegistry getRegistry() {
		return registry;
	}

	public NodeModel getRootNode() {
		return (NodeModel) getRoot();
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
	 * This method should not be called directly!
	 */
	@Override
	public void nodeChanged(final TreeNode node) {
		getModeController().getMapController().nodeChanged(((NodeModel) node));
	}

	/**
	 * Invoke this method if you've totally changed the children of node and its
	 * childrens children... This will post a treeStructureChanged event.
	 */
	void nodeChangedInternal(final TreeNode node) {
		if (node != null) {
			fireTreeNodesChanged(this, getPathToRoot(node), null, null);
		}
	}

	public void nodeRefresh(final TreeNode node) {
		getModeController().getMapController().nodeRefresh((NodeModel) node);
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

	void registryNodeRecursive(final NodeModel nodeModel) {
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

	public IExtension removeExtension(final Class clazz) {
		return extensions.removeExtension(clazz);
	}

	public boolean removeExtension(final IExtension extension) {
		return extensions.removeExtension(extension);
	}

	public void setExtension(final Class clazz, final IExtension extension) {
		extensions.setExtension(clazz, extension);
	}

	public void setExtension(final IExtension extension) {
		extensions.setExtension(extension);
	}

	public void setFile(final File file) {
		try {
			url = file.toURL();
		}
		catch (final MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void setFilter(final IFilter filter) {
		this.filter = filter;
	}

	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
	}

	public void setRoot(final NodeModel root) {
		super.setRoot(root);
	}

	/**
	 * Counts the amount of actions performed.
	 *
	 * @param saved
	 *            true if the file was saved recently. False otherwise.
	 */
	public void setSaved(final boolean saved) {
		boolean setTitle = false;
		if (saved) {
			changesPerformedSinceLastSave = 0;
			setTitle = true;
		}
		else {
			if (changesPerformedSinceLastSave == 0) {
				setTitle = true;
			}
			++changesPerformedSinceLastSave;
		}
		if (setTitle) {
			Controller.getController().getViewController().setTitle();
		}
	};

	/**
	 * Set the value of url.
	 *
	 * @param v
	 *            Value to assign to url.
	 */
	public void setURL(final URL v) {
		url = v;
	};

	void unregistryNode(final NodeModel nodeModel) {
		final String id = nodeModel.getID();
		if (id != null) {
			nodes.put(id, null);
		}
		final Iterator<NodeModel> iterator = nodeModel.getChildren().iterator();
		while (iterator.hasNext()) {
			unregistryNode(iterator.next());
		}
	}
}
