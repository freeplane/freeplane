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
package org.freeplane.map.tree;

import java.awt.EventQueue;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
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

import org.freeplane.controller.Controller;
import org.freeplane.controller.views.MapViewManager;
import org.freeplane.io.ReadManager;
import org.freeplane.io.WriteManager;
import org.freeplane.io.xml.TreeXmlWriter;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;
import org.freeplane.io.xml.n3.nanoxml.XMLElement;
import org.freeplane.io.xml.n3.nanoxml.XMLParseException;
import org.freeplane.main.Tools;
import org.freeplane.map.tree.mindmapmode.IMapChangeListener;
import org.freeplane.map.tree.view.MainView;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.map.tree.view.NodeView;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.mindmapmode.EncryptionModel;

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
	static private class NodesDepthComparator implements Comparator {
		public NodesDepthComparator() {
		}

		/* the < relation. */
		public int compare(final Object p1, final Object p2) {
			final NodeModel n1 = ((NodeModel) p1);
			final MapModel map = n1.getMap();
			final NodeModel n2 = ((NodeModel) p2);
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

	private static boolean sSaveOnlyIntrinsicallyNeededIds = false;
	static private CommonToggleFoldedAction toggleFolded;

	public static boolean isSSaveOnlyIntrinsicallyNeededIds() {
		return sSaveOnlyIntrinsicallyNeededIds;
	}

	public static void setSaveOnlyIntrinsicallyNeededIds(
	                                                     final boolean sSaveOnlyIntrinsicallyNeededIds) {
		MapController.sSaveOnlyIntrinsicallyNeededIds = sSaveOnlyIntrinsicallyNeededIds;
	}

	protected final Collection<IMapChangeListener> mapChangeListeners;
	final private Collection<IMapLifeCycleListener> mapLifeCycleListeners;
	final private MapReader mapReader;
	final private MapWriter mapWriter;
	final private ModeController modeController;
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
		writeManager.addNodeWriter("map", mapWriter);
		createActions(modeController);
		mapChangeListeners = new LinkedList<IMapChangeListener>();
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

	public ListIterator<NodeModel> childrenFolded(final NodeModel nodeAdapter) {
		if (nodeAdapter.isFolded()) {
			return Collections.EMPTY_LIST.listIterator();
		}
		return childrenUnfolded(nodeAdapter);
	}

	public ListIterator<NodeModel> childrenUnfolded(final NodeModel node) {
		final EncryptionModel encryptionModel = node.getEncryptionModel();
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

	public NodeModel createNodeTreeFromXml(final MapModel map, final Reader reader)
	        throws XMLParseException, IOException {
		return mapReader.createNodeTreeFromXml(map, reader);
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
		if (extend || range || branch || !getMapView().isSelected(newlySelectedNodeView)) {
			if (!range) {
				if (extend) {
					getMapView().toggleSelected(newlySelectedNodeView);
				}
				else {
					getModeController().select(newlySelectedNodeView);
				}
				retValue = true;
			}
			else {
				retValue = getMapView().selectContinuous(newlySelectedNodeView);
			}
			if (branch) {
				getMapView().selectBranch(newlySelectedNodeView, extend);
				retValue = true;
			}
		}
		if (retValue) {
			e.consume();
			String link = newlySelectedNodeView.getModel().getLink();
			link = (link != null ? link : " ");
			Controller.getController().getViewController().out(link);
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

	public MapView getMapView() {
		return getModeController().getMapView();
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

	public ReadManager getReadManager() {
		return readManager;
	}

	public NodeModel getRootNode() {
		final MapModel map = Controller.getController().getMap();
		return (NodeModel) map.getRoot();
	}

	public WriteManager getWriteManager() {
		return writeManager;
	}

	public boolean hasChildren(final NodeModel node) {
		final EncryptionModel encryptionModel = node.getEncryptionModel();
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
		final NodeModel root = getModeController().getUrlManager().load(url, map);
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
			if (Tools.isAbsolutePath(relative)) {
				absolute = Tools.fileToUrl(new File(relative));
			}
			else if (relative.startsWith("#")) {
				final String target = relative.substring(1);
				try {
					getModeController().centerNode(getNodeFromID(target));
					return;
				}
				catch (final Exception e) {
					org.freeplane.main.Tools.logException(e);
					Controller.getController().getViewController().out(
					    Tools.expandPlaceholders(getModeController().getText("link_not_found"),
					        target));
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
				absolute = Tools.getURLWithoutReference(absolute);
			}
			final String extension = Tools.getExtension(absolute.toString());
			if ((extension != null)
			        && extension
			            .equals(org.freeplane.map.url.mindmapmode.FileManager.FREEMIND_FILE_EXTENSION_WITHOUT_DOT)) {
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
						Controller.getController();
						final ModeController newModeController = Controller.getModeController();
						newModeController.centerNode(newModeController.getMapController()
						    .getNodeFromID(ref));
					}
					catch (final Exception e) {
						org.freeplane.main.Tools.logException(e);
						Controller.getController().getViewController().out(
						    Tools.expandPlaceholders(getModeController().getText("link_not_found"),
						        ref));
						return;
					}
				}
			}
			else {
				Controller.getController().getViewController().openDocument(originalURL);
			}
		}
		catch (final MalformedURLException ex) {
			org.freeplane.main.Tools.logException(ex);
			Controller.getController().errorMessage(
			    getModeController().getText("url_error") + "\n" + ex);
			return;
		}
		catch (final Exception e) {
			org.freeplane.main.Tools.logException(e);
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
			getModeController().onUpdate(node, property, oldValue, newValue);
		}
		(node.getMap()).nodeChangedInternal(node);
	}

	/**
	 */
	public void nodeStructureChanged(final NodeModel node) {
		node.getMap().nodeStructureChanged(node);
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

	public void setFolded(final NodeModel node, final boolean folded) {
		_setFolded(node, folded);
	}

	public void sortNodesByDepth(final List inPlaceList) {
		Collections.sort(inPlaceList, new NodesDepthComparator());
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

	/**
	 * writes the content of the map to a writer.
	 *
	 * @throws IOException
	 */
	public void writeMapAsXml(final MapModel map, final Writer fileout, final boolean saveInvisible)
	        throws IOException {
		final TreeXmlWriter xmlWriter = new TreeXmlWriter(writeManager, fileout);
		final IXMLElement xmlMap = new XMLElement("map");
		xmlMap.setAttribute("version", Controller.XML_VERSION);
		mapWriter.setSaveInvisible(saveInvisible);
		xmlWriter.addNode(map, xmlMap);
		fileout.close();
	}

	public void writeNodeAsXml(final StringWriter stringWriter, final NodeModel r,
	                           final boolean saveInvisible, final boolean b) throws IOException {
		mapWriter.writeNodeAsXml(stringWriter, r, saveInvisible, b);
	}
}
