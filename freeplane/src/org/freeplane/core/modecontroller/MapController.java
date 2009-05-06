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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.Action;

import org.freeplane.core.Compat;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.IMapViewManager;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.MapReader;
import org.freeplane.core.io.MapWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.UnknownElementWriter;
import org.freeplane.core.io.UnknownElements;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.io.MapWriter.Mode;
import org.freeplane.core.modecontroller.ModeController.IActionOnChange;
import org.freeplane.core.model.EncryptionModel;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.model.NodeModel.NodeChangeType;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.LogTool;
import org.freeplane.n3.nanoxml.XMLParseException;

/**
 * @author Dimitry Polivaev
 */
public class MapController extends SelectionController {
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
			final NodeModel[] path1 = n1.getPathToRoot();
			final NodeModel[] path2 = n2.getPathToRoot();
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
			return n1.getParentNode().getChildPosition(n1) - n2.getParentNode().getChildPosition(n2);
		}
	}

	private static boolean saveOnlyIntrinsicallyNeededIds = false;

	public static boolean saveOnlyIntrinsicallyNeededIds() {
		return saveOnlyIntrinsicallyNeededIds;
	}

	public static void setSaveOnlyIntrinsicallyNeededIds(final boolean saveOnlyIntrinsicallyNeededIds) {
		MapController.saveOnlyIntrinsicallyNeededIds = saveOnlyIntrinsicallyNeededIds;
	}

	final private Controller controller;
	protected final Collection<IMapChangeListener> mapChangeListeners;
	final private Collection<IMapLifeCycleListener> mapLifeCycleListeners;
	final private MapReader mapReader;
	final private MapWriter mapWriter;
	final private ModeController modeController;
	final private LinkedList<INodeChangeListener> nodeChangeListeners;
	final private ReadManager readManager;
	private final WriteManager writeManager;

	public MapController(final ModeController modeController) {
		super();
		this.modeController = modeController;
		controller = modeController.getController();
		mapLifeCycleListeners = new LinkedList<IMapLifeCycleListener>();
		writeManager = new WriteManager();
		mapWriter = new MapWriter(this);
		readManager = new ReadManager();
		mapReader = new MapReader(readManager);
		readManager.addElementHandler("map", mapReader);
		readManager.addAttributeHandler("map", "version", new IAttributeHandler() {
			public void setAttribute(final Object node, final String value) {
			}
		});
		writeManager.addElementWriter("map", mapWriter);
		writeManager.addAttributeWriter("map", mapWriter);
		final UnknownElementWriter unknownElementWriter = new UnknownElementWriter();
		writeManager.addExtensionAttributeWriter(UnknownElements.class, unknownElementWriter);
		writeManager.addExtensionElementWriter(UnknownElements.class, unknownElementWriter);
		createActions(modeController);
		mapChangeListeners = new LinkedList<IMapChangeListener>();
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
		if (isFolded(node) != folded) {
			node.setFolded(folded);
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

	public void centerNode(final NodeModel node) {
		getController().getSelection().centerNode(node);
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
		final MapModel map = getController().getMap();
		map.destroy();
		return true;
	}

	/**
	 *
	 */
	private void createActions(final ModeController modeController) {
		modeController.addAction(new ToggleFoldedAction(controller));
		modeController.addAction(new ToggleChildrenFoldedAction(this));
		modeController.addAction(new CenterSelectedNodeAction(controller));
	}

	public void displayNode(final NodeModel node) {
		displayNode(node, null);
	}

	/**
	 * Display a node in the display (used by find and the goto action by arrow
	 * link actions).
	 */
	private void displayNode(final NodeModel node, final ArrayList nodesUnfoldedByDisplay) {
		if (!node.isVisible()) {
			node.getFilterInfo().reset();
			nodeRefresh(node);
		}
		final NodeModel[] path = node.getPathToRoot();
		for (int i = 0; i < path.length - 1; i++) {
			final NodeModel nodeOnPath = path[i];
			if (isFolded(nodeOnPath)) {
				if (nodesUnfoldedByDisplay != null) {
					nodesUnfoldedByDisplay.add(nodeOnPath);
				}
				setFolded(nodeOnPath, false);
			}
		}
	}

	public void fireMapChanged(final MapChangeEvent event) {
		for (final IMapChangeListener listener : mapChangeListeners) {
			listener.mapChanged(event);
		}
		final MapModel map = event.getMap();
		if (map != null) {
			map.fireMapChangeEvent(event);
			setSaved(map, false);
		}
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

	private void fireNodeChanged(final NodeModel node, final NodeChangeEvent nodeChangeEvent) {
		final Iterator<INodeChangeListener> iterator = nodeChangeListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().nodeChanged(nodeChangeEvent);
		}
		node.fireNodeChanged(nodeChangeEvent);
	}

	protected void fireNodeDeleted(final NodeModel parent, final NodeModel child, final int index) {
		final Iterator<IMapChangeListener> iterator = mapChangeListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().onNodeDeleted(parent, child, index);
		}
	}

	protected void fireNodeInserted(final NodeModel parent, final NodeModel child, final int index) {
		final Iterator<IMapChangeListener> iterator = mapChangeListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().onNodeInserted(parent, child, index);
		}
	}

	protected void fireNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
	                             final NodeModel child, final int newIndex) {
		final Iterator<IMapChangeListener> iterator = mapChangeListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().onNodeMoved(oldParent, oldIndex, newParent, child, newIndex);
		}
	}

	protected void firePreNodeDelete(final NodeModel parent, final NodeModel selectedNode, final int index) {
		final Iterator<IMapChangeListener> iterator = mapChangeListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().onPreNodeDelete(parent, selectedNode, index);
		}
	}

	public Controller getController() {
		return controller;
	}

	public void getFilteredXml(final MapModel map, final Writer fileout, final Mode mode) throws IOException {
		getMapWriter().writeMapAsXml(map, fileout, mode, false);
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
		Boolean state = null;
		boolean allNodeHaveSameFoldedStatus = true;
		for (final ListIterator it = iterator; it.hasNext();) {
			final NodeModel node = (NodeModel) it.next();
			if (node.getChildCount() == 0) {
				continue;
			}
			if (state == null) {
				state = isFolded(node);
			}
			else {
				if (isFolded(node) != state) {
					allNodeHaveSameFoldedStatus = false;
					break;
				}
			}
		}
		/* if the folding state is ambiguous, the nodes are folded. */
		boolean fold = true;
		if (allNodeHaveSameFoldedStatus && state != null) {
			fold = !state;
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
		final MapModel map = getController().getMap();
		final NodeModel node = map.getNodeForID(nodeID);
		if (node == null) {
			throw new IllegalArgumentException("Node belonging to the node id " + nodeID + " not found.");
		}
		return node;
	}

	public String getNodeID(final NodeModel selected) {
		return selected.createID();
	}

	public ReadManager getReadManager() {
		return readManager;
	}

	public NodeModel getRootNode() {
		final MapModel map = getController().getMap();
		return map.getRootNode();
	}

	public NodeModel getSelectedNode() {
		return getController().getSelection().getSelected();
	}

	/**
	 * fc, 24.1.2004: having two methods getSelecteds with different return
	 * values (linkedlists of models resp. views) is asking for trouble. @see
	 * MapView
	 *
	 * @return returns a list of MindMapNode s.
	 */
	public List<NodeModel> getSelectedNodes() {
		final IMapSelection selection = getController().getSelection();
		if (selection == null) {
			final List<NodeModel> list = Collections.emptyList();
			return list;
		}
		return selection.getSelection();
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

	/**
	 * True iff one of node's <i>strict</i> descendants is folded. A node N is
	 * not its strict descendant - the fact that node itself is folded is not
	 * sufficient to return true.
	 */
	public boolean hasFoldedStrictDescendant(final NodeModel node) {
		for (final ListIterator e = childrenUnfolded(node); e.hasNext();) {
			final NodeModel child = (NodeModel) e.next();
			if (isFolded(child) || hasFoldedStrictDescendant(child)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.modes.MindMap#insertNodeInto(javax.swing.tree.MutableTreeNode,
	 * javax.swing.tree.MutableTreeNode)
	 */
	public void insertNodeIntoWithoutUndo(final NodeModel newChild, final NodeModel parent) {
		insertNodeIntoWithoutUndo(newChild, parent, parent.getChildCount());
	}

	public void insertNodeIntoWithoutUndo(final NodeModel newNode, final NodeModel parent, final int index) {
		parent.insert(newNode, index);
		fireNodeInserted(parent, newNode, index);
	}

	public boolean isFolded(final NodeModel node) {
		return node.isFolded();
	}

	public void load(final MapModel map, final URL url) throws IOException, XMLParseException, URISyntaxException {
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
				absolute = Compat.fileToUrl(new File(relative));
			}
			else if (relative.startsWith("#")) {
				final String target = relative.substring(1);
				try {
					select(getNodeFromID(target));
					return;
				}
				catch (final Exception e) {
					LogTool.logException(e);
					getController().getViewController().out(FpStringUtils.formatText("link_not_found", target));
					return;
				}
			}
			else {
				/*
				 * Remark: getMap().getURL() returns URLs like file:/C:/... It
				 * seems, that it does not cause any problems.
				 */
				final MapModel map = getController().getMap();
				absolute = new URL(map.getURL(), relative);
			}
			final URL originalURL = absolute;
			final String ref = absolute.getRef();
			if (ref != null) {
				absolute = UrlManager.getURLWithoutReference(absolute);
			}
			final String extension = UrlManager.getExtension(absolute.toString());
			if ((extension != null)
			        && extension.equals(org.freeplane.core.url.UrlManager.FREEPLANE_FILE_EXTENSION_WITHOUT_DOT)) {
				final IMapViewManager mapViewManager = getController().getMapViewManager();
				/*
				 * this can lead to confusion if the user handles multiple maps
				 * with the same name. Obviously, this is wrong. Get a better
				 * check whether or not the file is already opened.
				 */
				final String mapExtensionKey = mapViewManager.checkIfFileIsAlreadyOpened(absolute);
				if (mapExtensionKey == null) {
					getController().getViewController().setWaitingCursor(true);
					newMap(absolute);
				}
				else {
					mapViewManager.tryToChangeToMapView(mapExtensionKey);
				}
				if (ref != null) {
					try {
						final ModeController newModeController = getController().getModeController();
						final MapController newMapController = newModeController.getMapController();
						newMapController.select(newMapController.getNodeFromID(ref));
					}
					catch (final Exception e) {
						LogTool.logException(e);
						getController().getViewController().out(FpStringUtils.formatText("link_not_found", ref));
						return;
					}
				}
			}
			else {
				getController().getViewController().openDocument(originalURL);
			}
		}
		catch (final MalformedURLException ex) {
			LogTool.logException(ex);
			getController().errorMessage(ResourceBundles.getText("url_error") + "\n" + ex);
			return;
		}
		catch (final Exception e) {
			LogTool.logException(e);
		}
		finally {
			getController().getViewController().setWaitingCursor(false);
		}
	}

	public MapModel newMap(final NodeModel root) {
		final MapModel newModel = newModel(root);
		fireMapCreated(newModel);
		newMapView(newModel);
		return newModel;
	}

	public MapModel newMap(final URL file) throws FileNotFoundException, XMLParseException, IOException,
	        URISyntaxException {
		final MapModel newModel = newModel(null);
		load(newModel, file);
		fireMapCreated(newModel);
		newMapView(newModel);
		setSaved(newModel, true);
		return newModel;
	}

	protected void newMapView(final MapModel mapModel) {
		getController().getMapViewManager().newMapView(mapModel, getModeController());
		setSaved(mapModel, false);
	}

	public MapModel newModel(final NodeModel root) {
		final MapModel mindMapMapModel = new MapModel(getModeController(), root);
		fireMapCreated(mindMapMapModel);
		return mindMapMapModel;
	}

	public NodeModel newNode(final Object userObject, final MapModel map) {
		return new NodeModel(userObject, map);
	}

	@Deprecated
	public void nodeChanged(final NodeModel node) {
		nodeChanged(node, NodeModel.UNKNOWN_PROPERTY, null, null);
	}

	public void nodeChanged(final NodeModel node, final Object property, final Object oldValue, final Object newValue) {
		setSaved(node.getMap(), false);
		nodeRefresh(node, property, oldValue, newValue, true);
	}

	@Deprecated
	public void nodeRefresh(final NodeModel node) {
		nodeRefresh(node, NodeModel.UNKNOWN_PROPERTY, null, null);
	}

	public void nodeRefresh(final NodeModel node, final Object property, final Object oldValue, final Object newValue) {
		nodeRefresh(node, property, oldValue, newValue, false);
	}

	private void nodeRefresh(final NodeModel node, final Object property, final Object oldValue, final Object newValue,
	                         final boolean isUpdate) {
		if (mapReader.isMapLoadingInProcess()) {
			return;
		}
		if (isUpdate) {
			if (node.getHistoryInformation() != null) {
				node.getHistoryInformation().setLastModifiedAt(new Date());
			}
		}
		final NodeChangeEvent nodeChangeEvent = new NodeChangeEvent(node, property, oldValue, newValue);
		fireNodeChanged(node, nodeChangeEvent);
	}

	public void refreshMap() {
		final MapModel map = getController().getMap();
		final NodeModel root = map.getRootNode();
		refreshMapFrom(root);
	}

	private void refreshMap(final NodeModel node, final NodeChangeEvent event) {
		final Iterator iterator = node.getChildren().iterator();
		while (iterator.hasNext()) {
			final NodeModel child = (NodeModel) iterator.next();
			refreshMap(child, event);
		}
		fireNodeChanged(node, event);
	}

	public void refreshMapFrom(final NodeModel node) {
		final NodeChangeEvent event = new NodeChangeEvent(node, NodeChangeType.REFRESH, null, null);
		refreshMap(node, event);
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

	void removeNodeSelectionListener(final Class<? extends IActionOnChange> clazz, final Action action) {
		final Iterator<INodeSelectionListener> iterator = getNodeSelectionListeners().iterator();
		while (iterator.hasNext()) {
			final INodeSelectionListener next = iterator.next();
			if (next instanceof IActionOnChange && ((IActionOnChange) next).getAction() == action) {
				iterator.remove();
				return;
			}
		}
	}

	private ListIterator resetIterator(final ListIterator iterator) {
		while (iterator.hasPrevious()) {
			iterator.previous();
		}
		return iterator;
	}

	public void select(final NodeModel node) {
		displayNode(node);
		getController().getSelection().selectAsTheOnlyOneSelected(node);
	}

	public void selectBranch(final NodeModel selected, final boolean extend) {
		displayNode(selected);
		getController().getSelection().selectBranch(selected, extend);
	}

	public void selectMultipleNodes(final NodeModel focussed, final Collection selecteds) {
		for (final Iterator i = selecteds.iterator(); i.hasNext();) {
			final NodeModel node = (NodeModel) (i.next());
			displayNode(node);
		}
		select(focussed);
		for (final Iterator i = selecteds.iterator(); i.hasNext();) {
			final NodeModel node = (NodeModel) i.next();
			getController().getSelection().makeTheSelected(node);
		}
		getController().getViewController().obtainFocusForSelected();
	}

	public void setFolded(final NodeModel node, final boolean folded) {
		_setFolded(node, folded);
	}

	public void setSaved(final MapModel mapModel, final boolean saved) {
		final boolean setTitle = saved != mapModel.isSaved();
		mapModel.setSaved(saved);
		if (setTitle) {
			getModeController().getController().getViewController().setTitle();
		}
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

	public void toggleFolded() {
		toggleFolded(getSelectedNodes().listIterator());
	}

	public void toggleFolded(final ListIterator listIterator) {
		final boolean fold = getFoldingState(resetIterator(listIterator));
		for (final Iterator i = resetIterator(listIterator); i.hasNext();) {
			final NodeModel node = (NodeModel) i.next();
			setFolded(node, fold);
		}
	}
}
