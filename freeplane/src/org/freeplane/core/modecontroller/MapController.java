/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.core.modecontroller;

import java.awt.EventQueue;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.MapViewManager;
import org.freeplane.core.io.MapReader;
import org.freeplane.core.io.MapWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.modecontroller.ModeController.IActionOnChange;
import org.freeplane.core.model.EncryptionModel;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.Tools;
import org.freeplane.n3.nanoxml.XMLParseException;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/**
 * @author Dimitry Polivaev
 */
public class MapController {
	/**
	 * This class sortes nodes by ascending depth of their paths to root. This
	 * is useful to assure that children are cutted <b>before </b> their
	 * fathers!!!. Moreover, it sorts nodes with the same depth according to
	 * their position relative to each other.
	 */
	static private class NodesDepthComparator implements Comparator<NodeModel> {
		public NodesDepthComparator() {
		}

		/* the < relation. */
		public int compare(final NodeModel n1, final NodeModel n2) {
			final MapModel map = n1.getMap();
			final Object[] path1 = map.getPathToRoot(n1);
			final Object[] path2 = map.getPathToRoot(n2);
			final int depth = path1.length - path2.length;
			if (depth > 0) {
				return -1;
			}
			if (depth < 0) {
				return 1;
			}
			if (n1.isRoot()) {
				return 0;
			}
			return n1.getParentNode().getChildPosition(n1)
			        - n2.getParentNode().getChildPosition(n2);
		}
	}

	private static boolean saveOnlyIntrinsicallyNeededIds = false;
	static private CommonToggleFoldedAction toggleFolded;

	public static boolean saveOnlyIntrinsicallyNeededIds() {
		return saveOnlyIntrinsicallyNeededIds;
	}

	public static void setSaveOnlyIntrinsicallyNeededIds(
	                                                     final boolean saveOnlyIntrinsicallyNeededIds) {
		MapController.saveOnlyIntrinsicallyNeededIds = saveOnlyIntrinsicallyNeededIds;
	}

	protected final Collection<IMapChangeListener> mapChangeListeners;
	final private Collection<IMapLifeCycleListener> mapLifeCycleListeners;
	final private MapReader mapReader;
	final private MapWriter mapWriter;
	final private ModeController modeController;
	final private LinkedList<INodeChangeListener> nodeChangeListeners;
	final private LinkedList<INodeSelectionListener> nodeSelectionListeners;
	final private ReadManager readManager;
	private final WriteManager writeManager;

	public MapController(final ModeController modeController) {
		super();
		this.modeController = modeController;
		mapLifeCycleListeners = new LinkedList<IMapLifeCycleListener>();
		writeManager = new WriteManager();
		mapWriter = new MapWriter(writeManager);
		readManager = new ReadManager();
		mapReader = new MapReader(readManager);
		writeManager.addElementWriter("map", mapWriter);
		writeManager.addAttributeWriter("map", mapWriter);
		createActions(modeController);
		mapChangeListeners = new LinkedList<IMapChangeListener>();
		nodeSelectionListeners = new LinkedList();
		nodeChangeListeners = new LinkedList();
	}

	/**
	 * Don't call me directly!!! The basic folding method. Without undo.
	 */
	public void _setFolded(final NodeModel node, final boolean folded) {
		if (node == null) {
			throw new IllegalArgumentException("setFolded was called with a null node.");
		}
		if (node.isRoot() && folded) {
			return;
		}
		if (node.getModeController().getMapController().isFolded(node) != folded) {
			node.setFolded(folded);
			nodeStructureChanged(node);
		}
	}

	public void addMapChangeListener(final IMapChangeListener listener) {
		mapChangeListeners.add(listener);
	}

	public void addMapLifeCycleListener(final IMapLifeCycleListener listener) {
		mapLifeCycleListeners.add(listener);
	}

	public void addNodeChangeListener(final INodeChangeListener listener) {
		nodeChangeListeners.add(listener);
	}

	public void addNodeSelectionListener(final INodeSelectionListener listener) {
		nodeSelectionListeners.add(listener);
	}

	public void centerNode(final NodeModel node) {
		NodeView view = null;
		if (node != null) {
			view = Controller.getController().getMapView().getNodeView(node);
		}
		if (view == null) {
			displayNode(node);
			view = Controller.getController().getMapView().getNodeView(node);
		}
		centerNode(view);
	}

	private void centerNode(final NodeView node) {
		Controller.getController().getMapView().centerNode(node);
		Controller.getController().getMapView().selectAsTheOnlyOneSelected(node);
		Controller.getController().getViewController().obtainFocusForSelected();
	}

	public ListIterator<NodeModel> childrenFolded(final NodeModel node) {
		if (node.isFolded()) {
			return Collections.EMPTY_LIST.listIterator();
		}
		return childrenUnfolded(node);
	}

	public ListIterator<NodeModel> childrenUnfolded(final NodeModel node) {
		final EncryptionModel encryptionModel = EncryptionModel.getModel(node);
		if (encryptionModel != null && !encryptionModel.isAccessible()) {
			return Collections.EMPTY_LIST.listIterator();
		}
		return node.getChildren().listIterator();
	}

	/**
	 * Return false if user has canceled.
	 */
	public boolean close(final boolean force) {
		final MapModel map = Controller.getController().getMap();
		map.destroy();
		return true;
	}

	/**
	 *
	 */
	private void createActions(final ModeController modeController) {
		modeController.addAction("newMap", new NewMapAction());
		toggleFolded = new CommonToggleFoldedAction();
		modeController.addAction("toggleFolded", toggleFolded);
		modeController
		    .addAction("toggleChildrenFolded", new CommonToggleChildrenFoldedAction(this));
	}

	public void displayNode(final NodeModel node) {
		displayNode(node, null);
	}

	/**
	 * Display a node in the display (used by find and the goto action by arrow
	 * link actions).
	 */
	public void displayNode(final NodeModel node, final ArrayList nodesUnfoldedByDisplay) {
		final Object[] path = node.getMap().getPathToRoot(node);
		for (int i = 0; i < path.length - 1; i++) {
			final NodeModel nodeOnPath = (NodeModel) path[i];
			if (nodeOnPath.getModeController().getMapController().isFolded(nodeOnPath)) {
				if (nodesUnfoldedByDisplay != null) {
					nodesUnfoldedByDisplay.add(nodeOnPath);
				}
				setFolded(nodeOnPath, false);
			}
		}
	}

	public boolean extendSelection(final MouseEvent e) {
		final NodeView newlySelectedNodeView = ((MainView) e.getComponent()).getNodeView();
		final boolean extend = e.isControlDown();
		final boolean range = e.isShiftDown();
		final boolean branch = e.isAltGraphDown() || e.isAltDown();
		/* windows alt, linux altgraph .... */
		boolean retValue = false;
		if (extend || range || branch
		        || !Controller.getController().getMapView().isSelected(newlySelectedNodeView)) {
			if (!range) {
				if (extend) {
					Controller.getController().getMapView().toggleSelected(newlySelectedNodeView);
				}
				else {
					select(newlySelectedNodeView);
				}
				retValue = true;
			}
			else {
				retValue = Controller.getController().getMapView().selectContinuous(
				    newlySelectedNodeView);
			}
			if (branch) {
				Controller.getController().getMapView().selectBranch(newlySelectedNodeView, extend);
				retValue = true;
			}
		}
		if (retValue) {
			e.consume();
		}
		return retValue;
	}

	protected void fireMapCreated(final MapModel map) {
		final Iterator<IMapLifeCycleListener> iterator = mapLifeCycleListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().onCreate(map);
		}
	}

	protected void fireMapRemoved(final MapModel map) {
		final Iterator<IMapLifeCycleListener> iterator = mapLifeCycleListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().onRemove(map);
		}
	}

	protected void fireNodeDeleted(final NodeModel parent, final NodeModel child) {
		final Iterator<IMapChangeListener> iterator = mapChangeListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().onNodeDeleted(parent, child);
		}
	}

	protected void fireNodeInserted(final NodeModel parent, final NodeModel child, final int index) {
		final Iterator<IMapChangeListener> iterator = mapChangeListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().onNodeInserted(parent, child, index);
		}
	}

	protected void fireNodeMoved(final NodeModel oldParent, final NodeModel newParent,
	                             final NodeModel child, final int newIndex) {
		final Iterator<IMapChangeListener> iterator = mapChangeListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().onNodeMoved(oldParent, newParent, child, newIndex);
		}
	}

	protected void firePreNodeDelete(final NodeModel node) {
		final Iterator<IMapChangeListener> iterator = mapChangeListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().onPreNodeDelete(node);
		}
	}

	public void getFilteredXml(final MapModel map, final Writer fileout) throws IOException {
		getMapWriter().writeMapAsXml(map, fileout, false);
	}

	/**
	 * Determines whether the nodes should be folded or unfolded depending on
	 * their states. If not all nodes have the same folding status, the result
	 * means folding
	 *
	 * @param iterator
	 *            an iterator of MindMapNodes.
	 * @return true, if the nodes should be folded.
	 */
	public boolean getFoldingState(final ListIterator iterator) {
		/*
		 * Retrieve the information whether or not all nodes have the same
		 * folding state.
		 */
		Tools.BooleanHolder state = null;
		boolean allNodeHaveSameFoldedStatus = true;
		for (final ListIterator it = iterator; it.hasNext();) {
			final NodeModel node = (NodeModel) it.next();
			if (node.getChildCount() == 0) {
				continue;
			}
			if (state == null) {
				state = new Tools.BooleanHolder();
				state.setValue(node.getModeController().getMapController().isFolded(node));
			}
			else {
				if (node.getModeController().getMapController().isFolded(node) != state.getValue()) {
					allNodeHaveSameFoldedStatus = false;
					break;
				}
			}
		}
		/* if the folding state is ambiguous, the nodes are folded. */
		boolean fold = true;
		if (allNodeHaveSameFoldedStatus && state != null) {
			fold = !state.getValue();
		}
		return fold;
	}

	public MapReader getMapReader() {
		return mapReader;
	}

	public MapWriter getMapWriter() {
		return mapWriter;
	}

	public ModeController getModeController() {
		return modeController;
	}

	/*
	 * Helper methods
	 */
	public NodeModel getNodeFromID(final String nodeID) {
		final MapModel map = Controller.getController().getMap();
		final NodeModel node = map.getNodeForID(nodeID);
		if (node == null) {
			throw new IllegalArgumentException("Node belonging to the node id " + nodeID
			        + " not found.");
		}
		return node;
	}

	public String getNodeID(final NodeModel selected) {
		return selected.createID();
	}

	public NodeView getNodeView(final NodeModel node) {
		return Controller.getController().getMapView().getNodeView(node);
	}

	private NodeView getNodeView(final Object object) {
		if (object instanceof NodeView) {
			return (NodeView) object;
		}
		if (object instanceof NodeModel) {
			return Controller.getController().getMapView().getNodeView((NodeModel) object);
		}
		throw new ClassCastException();
	}

	public ReadManager getReadManager() {
		return readManager;
	}

	public NodeModel getRootNode() {
		final MapModel map = Controller.getController().getMap();
		return (NodeModel) map.getRoot();
	}

	public NodeModel getSelectedNode() {
		final NodeView selectedView = getSelectedView();
		if (selectedView != null) {
			return selectedView.getModel();
		}
		return null;
	}

	/**
	 * fc, 24.1.2004: having two methods getSelecteds with different return
	 * values (linkedlists of models resp. views) is asking for trouble. @see
	 * MapView
	 *
	 * @return returns a list of MindMapNode s.
	 */
	public List getSelectedNodes() {
		final MapView view = Controller.getController().getMapView();
		if (view == null) {
			return Collections.EMPTY_LIST;
		}
		final LinkedList selecteds = new LinkedList();
		final Iterator it = view.getSelection().iterator();
		if (it != null) {
			while (it.hasNext()) {
				final NodeView selected = (NodeView) it.next();
				selecteds.add(selected.getModel());
			}
		}
		return selecteds;
	}

	public NodeView getSelectedView() {
		if (Controller.getController().getMapView() != null) {
			return Controller.getController().getMapView().getSelected();
		}
		return null;
	}

	public WriteManager getWriteManager() {
		return writeManager;
	}

	public boolean hasChildren(final NodeModel node) {
		final EncryptionModel encryptionModel = EncryptionModel.getModel(node);
		if (encryptionModel != null && !encryptionModel.isAccessible()) {
			return false;
		}
		return node.hasChildren();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.modes.MindMap#insertNodeInto(javax.swing.tree.MutableTreeNode,
	 * javax.swing.tree.MutableTreeNode)
	 */
	public void insertNodeIntoWithoutUndo(final NodeModel newChild, final NodeModel parent) {
		insertNodeIntoWithoutUndo(newChild, parent, parent.getChildCount());
	}

	public void insertNodeIntoWithoutUndo(final NodeModel newNode, final NodeModel parent,
	                                      final int index) {
		parent.getMap().insertNodeInto(newNode, parent, index);
		fireNodeInserted(parent, newNode, index);
	}

	public boolean isFolded(final NodeModel node) {
		return node.isFolded();
	}

	public void load(final MapModel map, final URL url) throws IOException, XMLParseException,
	        URISyntaxException {
		map.setURL(url);
		final NodeModel root = UrlManager.getController(getModeController()).load(url, map);
		if (root != null) {
			map.setRoot(root);
		}
		else {
			throw new IOException();
		}
	}

	public void loadURL(final String relative) {
		try {
			URL absolute = null;
			if (UrlManager.isAbsolutePath(relative)) {
				absolute = UrlManager.fileToUrl(new File(relative));
			}
			else if (relative.startsWith("#")) {
				final String target = relative.substring(1);
				try {
					centerNode(getNodeFromID(target));
					return;
				}
				catch (final Exception e) {
					org.freeplane.core.util.Tools.logException(e);
					Controller.getController().getViewController().out(
					    UrlManager.expandPlaceholders(
					        getModeController().getText("link_not_found"), target));
					return;
				}
			}
			else {
				/*
				 * Remark: getMap().getURL() returns URLs like file:/C:/... It
				 * seems, that it does not cause any problems.
				 */
				final MapModel map = Controller.getController().getMap();
				absolute = new URL(map.getURL(), relative);
			}
			final URL originalURL = absolute;
			final String ref = absolute.getRef();
			if (ref != null) {
				absolute = UrlManager.getURLWithoutReference(absolute);
			}
			final String extension = UrlManager.getExtension(absolute.toString());
			if ((extension != null)
			        && extension
			            .equals(org.freeplane.features.mindmapmode.file.MFileManager.FREEMIND_FILE_EXTENSION_WITHOUT_DOT)) {
				final MapViewManager mapViewManager = Controller.getController()
				    .getMapViewManager();
				/*
				 * this can lead to confusion if the user handles multiple maps
				 * with the same name. Obviously, this is wrong. Get a better
				 * check whether or not the file is already opened.
				 */
				final String mapExtensionKey = mapViewManager.checkIfFileIsAlreadyOpened(absolute);
				if (mapExtensionKey == null) {
					Controller.getController().getViewController().setWaitingCursor(true);
					newMap(absolute);
				}
				else {
					mapViewManager.tryToChangeToMapView(mapExtensionKey);
				}
				if (ref != null) {
					try {
						final ModeController newModeController = Controller.getModeController();
						final MapController newMapController = newModeController.getMapController();
						newMapController.centerNode(newMapController.getNodeFromID(ref));
					}
					catch (final Exception e) {
						org.freeplane.core.util.Tools.logException(e);
						Controller.getController().getViewController().out(
						    UrlManager.expandPlaceholders(getModeController().getText(
						        "link_not_found"), ref));
						return;
					}
				}
			}
			else {
				Controller.getController().getViewController().openDocument(originalURL);
			}
		}
		catch (final MalformedURLException ex) {
			org.freeplane.core.util.Tools.logException(ex);
			Controller.getController().errorMessage(
			    getModeController().getText("url_error") + "\n" + ex);
			return;
		}
		catch (final Exception e) {
			org.freeplane.core.util.Tools.logException(e);
		}
		finally {
			Controller.getController().getViewController().setWaitingCursor(false);
		}
	}

	public MapModel newMap(final NodeModel root) {
		final MapModel newModel = newModel(root);
		newMapView(newModel);
		fireMapCreated(newModel);
		return newModel;
	}

	public MapModel newMap(final URL file) throws FileNotFoundException, XMLParseException,
	        IOException, URISyntaxException {
		final MapModel newModel = newModel(null);
		load(newModel, file);
		newMapView(newModel);
		fireMapCreated(newModel);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				newModel.setSaved(true);
			}
		});
		return newModel;
	}

	protected void newMapView(final MapModel mapModel) {
		Controller.getController().getMapViewManager().newMapView(mapModel,
		    mapModel.getModeController());
		mapModel.setSaved(false);
	}

	/**
	 * You _must_ implement this if you use one of the following actions:
	 * OpenAction, NewMapAction.
	 *
	 * @param root
	 * @param modeController
	 */
	public MapModel newModel(final NodeModel root) {
		throw new java.lang.UnsupportedOperationException();
	}

	public NodeModel newNode(final Object userObject, final MapModel map) {
		return new NodeModel(userObject, map);
	}

	@Deprecated
	public void nodeChanged(final NodeModel node) {
		nodeChanged(node, NodeModel.UNKNOWN_PROPERTY, null, null);
	}

	public void nodeChanged(final NodeModel node, final Object property, final Object oldValue,
	                        final Object newValue) {
		node.getMap().setSaved(false);
		nodeRefresh(node, property, oldValue, newValue, true);
	}

	@Deprecated
	public void nodeRefresh(final NodeModel node) {
		nodeRefresh(node, NodeModel.UNKNOWN_PROPERTY, null, null);
	}

	public void nodeRefresh(final NodeModel node, final Object property, final Object oldValue,
	                        final Object newValue) {
		nodeRefresh(node, property, oldValue, newValue, false);
	}

	private void nodeRefresh(final NodeModel node, final Object property, final Object oldValue,
	                         final Object newValue, final boolean isUpdate) {
		if (mapReader.isMapLoadingInProcess()) {
			return;
		}
		if (isUpdate) {
			if (node.getHistoryInformation() != null) {
				node.getHistoryInformation().setLastModifiedAt(new Date());
			}
			onUpdate(node, property, oldValue, newValue);
		}
		(node.getMap()).nodeChangedInternal(node);
	}

	/**
	 */
	public void nodeStructureChanged(final NodeModel node) {
		node.getMap().nodeStructureChanged(node);
	}

	public void onDeselect(final NodeView node) {
		try {
			final HashSet copy = new HashSet(nodeSelectionListeners);
			for (final Iterator iter = copy.iterator(); iter.hasNext();) {
				final INodeSelectionListener listener = (INodeSelectionListener) iter.next();
				listener.onDeselect(node);
			}
		}
		catch (final RuntimeException e) {
			Logger.global.log(Level.SEVERE, "Error in node selection listeners", e);
		}
	}

	public void onSelect(final NodeView node) {
		for (final Iterator iter = nodeSelectionListeners.iterator(); iter.hasNext();) {
			final INodeSelectionListener listener = (INodeSelectionListener) iter.next();
			listener.onSelect(node);
		}
	}

	/**
	 * @param isUpdate
	 * @param node
	 */
	public void onUpdate(final NodeModel node, final Object property, final Object oldValue,
	                     final Object newValue) {
		final Iterator<INodeChangeListener> iterator = nodeChangeListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().nodeChanged(new NodeChangeEvent(node, property, oldValue, newValue));
		}
	}

	public void refreshMap() {
		final MapModel map = Controller.getController().getMap();
		final NodeModel root = map.getRootNode();
		refreshMapFrom(root);
	}

	public void refreshMapFrom(final NodeModel node) {
		final Iterator iterator = node.getChildren().iterator();
		while (iterator.hasNext()) {
			final NodeModel child = (NodeModel) iterator.next();
			refreshMapFrom(child);
		}
		(node.getMap()).nodeChangedInternal(node);
	}

	public void removeMapChangeListener(final IMapChangeListener listener) {
		mapChangeListeners.remove(listener);
	}

	public void removeMapLifeCycleListener(final IMapLifeCycleListener listener) {
		mapLifeCycleListeners.remove(listener);
	}

	void removeNodeChangeListener(final Class<? extends IActionOnChange> clazz, final Action action) {
		final Iterator<INodeChangeListener> iterator = nodeChangeListeners.iterator();
		while (iterator.hasNext()) {
			final INodeChangeListener next = iterator.next();
			if (next instanceof IActionOnChange && ((IActionOnChange) next).getAction() == action) {
				iterator.remove();
				return;
			}
		}
	}

	public void removeNodeChangeListener(final INodeChangeListener listener) {
		nodeChangeListeners.remove(listener);
	}

	void removeNodeSelectionListener(final Class<? extends IActionOnChange> clazz,
	                                 final Action action) {
		final Iterator<INodeSelectionListener> iterator = nodeSelectionListeners.iterator();
		while (iterator.hasNext()) {
			final INodeSelectionListener next = iterator.next();
			if (next instanceof IActionOnChange && ((IActionOnChange) next).getAction() == action) {
				iterator.remove();
				return;
			}
		}
	}

	public void removeNodeSelectionListener(final INodeSelectionListener listener) {
		nodeSelectionListeners.remove(listener);
	}

	public void select(final NodeView node) {
		Controller.getController().getMapView().scrollNodeToVisible(node);
		Controller.getController().getMapView().selectAsTheOnlyOneSelected(node);
		Controller.getController().getMapView().setSiblingMaxLevel(node.getModel().getNodeLevel());
	}

	public void selectBranch(final NodeView selected, final boolean extend) {
		displayNode(selected.getModel());
		Controller.getController().getMapView().selectBranch(selected, extend);
	}

	public void selectMultipleNodes(final NodeModel focussed, final Collection selecteds) {
		selectMultipleNodesImpl(focussed, selecteds);
	}

	public void selectMultipleNodes(final NodeView focussed, final Collection selecteds) {
		selectMultipleNodesImpl(focussed, selecteds);
	}

	private void selectMultipleNodesImpl(final Object focussed, final Collection selecteds) {
		for (final Iterator i = selecteds.iterator(); i.hasNext();) {
			final NodeModel node = (NodeModel) (i.next());
			displayNode(node);
		}
		select(getNodeView(focussed));
		for (final Iterator i = selecteds.iterator(); i.hasNext();) {
			final NodeView node = getNodeView(i.next());
			Controller.getController().getMapView().makeTheSelected(node);
		}
		Controller.getController().getViewController().obtainFocusForSelected();
	}

	public void setFolded(final NodeModel node, final boolean folded) {
		_setFolded(node, folded);
	}

	/**
	*
	*/
	public void setToolTip(final NodeModel node, final String key, final String value) {
		node.setToolTip(key, value);
		nodeRefresh(node);
	}

	public void sortNodesByDepth(final List<NodeModel> collection) {
		Collections.sort(collection, new NodesDepthComparator());
	}

	/**
	 *
	 */
	public void toggleFolded() {
		toggleFolded.toggleFolded();
	}

	/**
	 * @param listIterator
	 */
	public void toggleFolded(final ListIterator listIterator) {
		toggleFolded.toggleFolded(listIterator);
	}
}
