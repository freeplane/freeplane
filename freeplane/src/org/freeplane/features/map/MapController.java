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
package org.freeplane.features.map;

import java.awt.EventQueue;
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
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.Action;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.UnknownElementWriter;
import org.freeplane.core.io.UnknownElements;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.NodeModel.NodeChangeType;
import org.freeplane.features.mode.AController.IActionOnChange;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.SelectionController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.url.UrlManager;
import org.freeplane.main.addons.AddOnsController;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.n3.nanoxml.XMLParseException;

/**
 * @author Dimitry Polivaev
 */
public class MapController extends SelectionController implements IExtension{
	public enum Direction {
		BACK, BACK_N_FOLD, FORWARD, FORWARD_N_FOLD
	}

	private static class ActionEnablerOnChange implements INodeChangeListener, INodeSelectionListener, IActionOnChange, IMapChangeListener {
		final AFreeplaneAction action;

		public ActionEnablerOnChange(final AFreeplaneAction action) {
			super();
			this.action = action;
		}

		public AFreeplaneAction getAction() {
			return action;
		}

		public void nodeChanged(final NodeChangeEvent event) {
			action.setEnabled();
		}

		public void onDeselect(final NodeModel node) {
		}

		public void onSelect(final NodeModel node) {
			action.setEnabled();
		}

		public void mapChanged(MapChangeEvent event) {
			action.setEnabled();
		}

		public void onNodeDeleted(NodeModel parent, NodeModel child, int index) {
			action.setEnabled();
		}

		public void onNodeInserted(NodeModel parent, NodeModel child,
				int newIndex) {
			action.setEnabled();
		}

		public void onNodeMoved(NodeModel oldParent, int oldIndex,
				NodeModel newParent, NodeModel child, int newIndex) {
			action.setEnabled();
		}

		public void onPreNodeMoved(NodeModel oldParent, int oldIndex,
				NodeModel newParent, NodeModel child, int newIndex) {
		}

		public void onPreNodeDelete(NodeModel oldParent,
				NodeModel selectedNode, int index) {
			setActionEnabled();
		}

		private void setActionEnabled() {
			final IMapSelection selection = Controller.getCurrentController().getSelection();
			if (selection == null || selection.getSelected() == null) {
				return;
			}
			action.setEnabled();
		}
	}

	private static class ActionSelectorOnChange implements INodeChangeListener, INodeSelectionListener,
	        IActionOnChange, IMapChangeListener {
		final AFreeplaneAction action;

		public ActionSelectorOnChange(final AFreeplaneAction action) {
			super();
			this.action = action;
		}

		public AFreeplaneAction getAction() {
			return action;
		}

		public void nodeChanged(final NodeChangeEvent event) {
			if (NodeChangeType.REFRESH.equals(event.getProperty())) {
				return;
			}
			setActionSelected();
		}

		private void setActionSelected() {
			final IMapSelection selection = Controller.getCurrentController().getSelection();
			if (selection == null || selection.getSelected() == null) {
				return;
			}
			action.setSelected();
		}

		public void onDeselect(final NodeModel node) {
		}

		public void onSelect(final NodeModel node) {
			setActionSelected();
		}

		public void mapChanged(final MapChangeEvent event) {
			setActionSelected();
		}

		public void onNodeDeleted(final NodeModel parent, final NodeModel child, final int index) {
			setActionSelected();
		}

		public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
			setActionSelected();
		}

		public void onNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
		                        final NodeModel child, final int newIndex) {
			setActionSelected();
		}

		public void onPreNodeDelete(final NodeModel oldParent, final NodeModel selectedNode, final int index) {
			setActionSelected();
		}

		public void onPreNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
		                           final NodeModel child, final int newIndex) {
			setActionSelected();
		}
	}

	public static void install() {
		FilterController.getCurrentFilterController().getConditionFactory().addConditionController(8,
		    new NodeLevelConditionController());
	}


	public void addListenerForAction(final AFreeplaneAction action) {
		if (AFreeplaneAction.checkEnabledOnChange(action)) {
			final ActionEnablerOnChange listener = new ActionEnablerOnChange(action);
			addNodeSelectionListener(listener);
			addNodeChangeListener(listener);
			addMapChangeListener(listener);
		}
		if (AFreeplaneAction.checkSelectionOnChange(action)) {
			final ActionSelectorOnChange listener = new ActionSelectorOnChange(action);
			addNodeSelectionListener(listener);
			addNodeChangeListener(listener);
			addMapChangeListener(listener);
		}
	}

	public void removeListenerForAction(final AFreeplaneAction action) {
		if (AFreeplaneAction.checkEnabledOnChange(action)) {
			removeNodeSelectionListener(ActionEnablerOnChange.class, action);
			removeNodeChangeListener(ActionEnablerOnChange.class, action);
			removeMapChangeListener(ActionEnablerOnChange.class, action);
		}
		if (AFreeplaneAction.checkSelectionOnChange(action)) {
			removeNodeSelectionListener(ActionSelectorOnChange.class, action);
			removeNodeChangeListener(ActionSelectorOnChange.class, action);
			removeMapChangeListener(ActionSelectorOnChange.class, action);
		}
	}

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

// 	final private Controller controller;
	protected final Collection<IMapChangeListener> mapChangeListeners;
	final private Collection<IMapLifeCycleListener> mapLifeCycleListeners;
	final private MapReader mapReader;
	final private MapWriter mapWriter;
// 	final private ModeController modeController;
	final private LinkedList<INodeChangeListener> nodeChangeListeners;
	final private ReadManager readManager;
	private final WriteManager writeManager;

	public MapController(ModeController modeController) {
		super();
		modeController.setMapController(this);
//		this.modeController = modeController;
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
		readManager.addAttributeHandler("map", "dialect", new IAttributeHandler() {
			public void setAttribute(final Object node, final String value) {
			}
		});
		writeManager.addElementWriter("map", mapWriter);
		writeManager.addAttributeWriter("map", mapWriter);
		final UnknownElementWriter unknownElementWriter = new UnknownElementWriter();
		writeManager.addExtensionAttributeWriter(UnknownElements.class, unknownElementWriter);
		writeManager.addExtensionElementWriter(UnknownElements.class, unknownElementWriter);
		mapChangeListeners = new LinkedList<IMapChangeListener>();
		nodeChangeListeners = new LinkedList<INodeChangeListener>();
		createActions();
	}

	public void setFolded(final NodeModel node, final boolean folded) {
		if (node == null) {
			throw new IllegalArgumentException("setFolded was called with a null node.");
		}
		if (node.getChildCount() == 0)
			return;
		final boolean unfold = ! folded;
		final boolean childShown = unfoldHiddenChildren(node);
		boolean mapChanged = false;
	    if (unfold && unfoldInvisibleChildren(node, true))
	        mapChanged = true;
		if (node.isFolded() != folded && !(node.isRoot() && folded)){
			node.setFolded(folded);
			mapChanged = true;
		}
		if(mapChanged){
			fireFoldingChanged(node);
		}
		else if(childShown)
	        fireNodeUnfold(node);
	}

	public boolean showNextChild(final NodeModel node) {
		if (node.getChildCount() == 0)
			return false;
		final boolean unfold = node.isFolded();
		if (unfold){
			for(NodeModel child:childrenUnfolded(node)){
				child.addExtension(HideChildSubtree.instance);
			}
			node.setFolded(false);
		}
		boolean childMadeVisible = false;
		for(NodeModel child:childrenUnfolded(node)){
			if (child.removeExtension(HideChildSubtree.instance) &&
					(child.isVisible() || unfoldInvisibleChildren(child, true))){
				childMadeVisible = true;
				break;
			}
		}
		if(childMadeVisible){
			fireNodeUnfold(node);
		}
		return childMadeVisible;
	}


	private void fireNodeUnfold(final NodeModel node) {
	    node.fireNodeChanged(new NodeChangeEvent(node, NodeChangeType.FOLDING, Boolean.TRUE,
	    	Boolean.FALSE));
    }

	public boolean hasHiddenChildren(final NodeModel node){
		if(! node.hasChildren())
			return false;
		if(node.isFolded())
			return true;
		for(NodeModel child:childrenUnfolded(node)){
			if (child.containsExtension(HideChildSubtree.class)){
				return true;
			}
		}
		return false;
	}

	private void fireFoldingChanged(final NodeModel node) {
	    final ResourceController resourceController = ResourceController.getResourceController();
	    if (resourceController.getProperty(NodeBuilder.RESOURCES_SAVE_FOLDING).equals(
	    	NodeBuilder.RESOURCES_ALWAYS_SAVE_FOLDING)) {
	    	final MapModel map = node.getMap();
	    	setSaved(map, false);
	    }
    }


	private boolean unfoldHiddenChildren(NodeModel node) {
		final List<NodeModel> children = childrenFolded(node);
		boolean changed = false;
		for (NodeModel child : children){
			if(child.removeExtension(HideChildSubtree.class) != null)
				changed = true;
		}
		return changed;
    }


	private boolean unfoldInvisibleChildren(final NodeModel node, final boolean reportUnfolded) {
		boolean visibleFound = false;
		boolean unfolded = false;
		for(int i = 0; i < node.getChildCount(); i++){
			final NodeModel child = (NodeModel) node.getChildAt(i);
			if(child.isVisible())
				visibleFound = true;
			else if(unfoldInvisibleChildren(child, false) && child.isFolded()){
				visibleFound = unfolded = true;
				child.setFolded(false);
			}
		}
		if(reportUnfolded)
			return unfolded;
		else
			return visibleFound;
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
		Controller.getCurrentController().getSelection().centerNode(node);
	}

	public List<NodeModel> childrenFolded(final NodeModel node) {
		if (node.isFolded()) {
			final List<NodeModel> empty = Collections.emptyList();
			return empty;
		}
		return childrenUnfolded(node);
	}

	public List<NodeModel> childrenUnfolded(final NodeModel node) {
		final EncryptionModel encryptionModel = EncryptionModel.getModel(node);
		if (encryptionModel != null && !encryptionModel.isAccessible()) {
			final List<NodeModel> empty = Collections.emptyList();
			return empty;
		}
		return node.getChildren();
	}

	/**
	 * Return false if user has canceled.
	 */
	public boolean close(final MapModel map, final boolean force) {
		fireMapRemoved(map);
		map.destroy();
		return true;
	}

	/**
	 * @param modeController
	 *
	 */
	private void createActions() {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addAction(new ToggleFoldedAction());
		modeController.addAction(new ToggleChildrenFoldedAction());
		modeController.addAction(new ShowNextChildAction());
		modeController.addAction(new GotoNodeAction());
	}

	public void displayNode(final NodeModel node) {
		displayNode(node, null);
	}

	/**
	 * Display a node in the display (used by find and the goto action by arrow
	 * link actions).
	 */
	public void displayNode(final NodeModel node, final ArrayList<NodeModel> nodesUnfoldedByDisplay) {
		if (!node.isVisible()) {
			node.getFilterInfo().reset();
			nodeRefresh(node);
		}
		final NodeModel[] path = node.getPathToRoot();
		for (int i = 0; i < path.length - 1; i++) {
			final NodeModel nodeOnPath = path[i];
			if (nodesUnfoldedByDisplay != null && isFolded(nodeOnPath)) {
            	nodesUnfoldedByDisplay.add(nodeOnPath);
            }
			setFolded(nodeOnPath, false);
		}
	}

	public void fireMapChanged(final MapChangeEvent event) {
		final MapModel map = event.getMap();
		if (map != null) {
			setSaved(map, false);
		}
		final IMapChangeListener[] list = mapChangeListeners.toArray(new IMapChangeListener[]{});
		for (final IMapChangeListener next : list) {
			next.mapChanged(event);
		}
		if (map != null) {
			map.fireMapChangeEvent(event);
		}
	}

	public void fireMapCreated(final MapModel map) {
		final IMapLifeCycleListener[] list = mapLifeCycleListeners.toArray(new IMapLifeCycleListener[]{});
		for (final IMapLifeCycleListener next : list) {
			next.onCreate(map);
		}
	}

	protected void fireMapRemoved(final MapModel map) {
		final IMapLifeCycleListener[] list = mapLifeCycleListeners.toArray(new IMapLifeCycleListener[]{});
		for (final IMapLifeCycleListener next : list) {
			next.onRemove(map);
		}
	}

	private void fireNodeChanged(final NodeModel node, final NodeChangeEvent nodeChangeEvent) {
		final INodeChangeListener[] list = nodeChangeListeners.toArray(new INodeChangeListener[]{});
		for (final INodeChangeListener next : list) {
			next.nodeChanged(nodeChangeEvent);
		}
		node.fireNodeChanged(nodeChangeEvent);
	}

	protected void fireNodeDeleted(final NodeModel parent, final NodeModel child, final int index) {
		final IMapChangeListener[] list = mapChangeListeners.toArray(new IMapChangeListener[]{});
		for (final IMapChangeListener next : list) {
			next.onNodeDeleted(parent, child, index);
		}
		child.getMap().unregistryNodes(child);
	}

	protected void fireNodeInserted(final NodeModel parent, final NodeModel child, final int index) {
		parent.getMap().registryNodeRecursive(child);
		final IMapChangeListener[] list = mapChangeListeners.toArray(new IMapChangeListener[]{});
		for (final IMapChangeListener next : list) {
			next.onNodeInserted(parent, child, index);
		}
	}

	protected void fireNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
	                             final NodeModel child, final int newIndex) {
		final IMapChangeListener[] list = mapChangeListeners.toArray(new IMapChangeListener[]{});
		for (final IMapChangeListener next : list) {
			next.onNodeMoved(oldParent, oldIndex, newParent, child, newIndex);
		}
	}

	protected void firePreNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
	                                final NodeModel child, final int newIndex) {
		final IMapChangeListener[] list = mapChangeListeners.toArray(new IMapChangeListener[]{});
		for (final IMapChangeListener next : list) {
			next.onPreNodeMoved(oldParent, oldIndex, newParent, child, newIndex);
		}
	}

	protected void firePreNodeDelete(final NodeModel parent, final NodeModel selectedNode, final int index) {
		final IMapChangeListener[] list = mapChangeListeners.toArray(new IMapChangeListener[]{});
		for (final IMapChangeListener next : list) {
			next.onPreNodeDelete(parent, selectedNode, index);
		}
	}

	public void getFilteredXml(final MapModel map, final Writer fileout, final Mode mode, final boolean forceFormat)
	        throws IOException {
		getMapWriter().writeMapAsXml(map, fileout, mode, false, forceFormat);
	}

	private Boolean getCommonFoldingState(final Collection<NodeModel> list) {
		Boolean state = null;
		for(final NodeModel node : list){
			if (node.getChildCount() == 0) {
				continue;
			}
			if (state == null) {
				state = hasHiddenChildren(node);
			}
			else {
				if (hasHiddenChildren(node) != state) {
					// no common state
					return null;
				}
			}
		}
		return state;
	}

	public MapReader getMapReader() {
		return mapReader;
	}

	public MapWriter getMapWriter() {
		return mapWriter;
	}

	/*
	 * Helper methods
	 */
	public NodeModel getNodeFromID(final String nodeID) {
		final MapModel map = Controller.getCurrentController().getMap();
		final NodeModel node = map.getNodeForID(nodeID);
		return node;
	}

	public String getNodeID(final NodeModel selected) {
		return selected.createID();
	}

	public ReadManager getReadManager() {
		return readManager;
	}

	public NodeModel getRootNode() {
		final MapModel map = Controller.getCurrentController().getMap();
		return map.getRootNode();
	}

	public NodeModel getSelectedNode() {
		final IMapSelection selection = Controller.getCurrentController().getSelection();
		if(selection != null)
			return selection.getSelected();
		return null;
	}

	/**
	 * fc, 24.1.2004: having two methods getSelecteds with different return
	 * values (linkedlists of models resp. views) is asking for trouble. @see
	 * MapView
	 *
	 * @return returns a list of MindMapNode s.
	 */
	public Collection<NodeModel> getSelectedNodes() {
		final IMapSelection selection = Controller.getCurrentController().getSelection();
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
		for (final NodeModel child : childrenUnfolded(node)) {
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
		if(parent.getParent() != null){
			newNode.setLeft(parent.isLeft());
		}
		parent.insert(newNode, index);
		fireNodeInserted(parent, newNode, index);
	}

	public boolean isFolded(final NodeModel node) {
		return node.isFolded();
	}

	/**@throws XMLException
	 * @deprecated -- use MapIO*/
	@Deprecated
	public boolean newMap(final URL url) throws FileNotFoundException, XMLParseException,IOException, URISyntaxException, XMLException{
        	final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
        	if (mapViewManager.tryToChangeToMapView(url))
        		return false;
        	try {
        	if (AddOnsController.getController().installIfAppropriate(url))
        		return false;
        	Controller.getCurrentController().getViewController().setWaitingCursor(true);
        	final MapModel newModel = new MapModel();
        	UrlManager.getController().loadCatchExceptions(url, newModel);
        	newModel.setReadOnly(true);
        	newModel.setSaved(true);
        	fireMapCreated(newModel);
        	newMapView(newModel);
        	return true;
        }
        finally {
        	Controller.getCurrentController().getViewController().setWaitingCursor(false);
        }
	}


	public void openMapSelectReferencedNode(final URL url) throws FileNotFoundException,
	        XMLParseException, IOException, URISyntaxException, XMLException, MalformedURLException {
	    String nodeReference = url.getRef();
	    if(nodeReference != null){
	    	newMap(new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath()));
	    	select(getNodeFromID(nodeReference));
	    }
	    else{
	    	newMap(url);
	    }
	}

	public void newMapView(final MapModel mapModel) {
		Controller.getCurrentController().getMapViewManager().newMapView(mapModel, Controller.getCurrentModeController());
	}

	public MapModel newMap() {
		final MapModel newModel = newModel();
		fireMapCreated(newModel);
		newMapView(newModel);
		return newModel;
	}

	public MapModel newModel() {
		final MapModel mindMapMapModel = new MapModel();
		mindMapMapModel.createNewRoot();
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
		if (isUpdate && !Controller.getCurrentModeController().isUndoAction()) {
			final HistoryInformationModel historyInformation = node.getHistoryInformation();
			if (historyInformation != null) {
				final IActor historyActor = new IActor() {
					private final Date lastModifiedAt = historyInformation.getLastModifiedAt();
					private final Date now = new Date();

					public void undo() {
						setDate(historyInformation, lastModifiedAt);
					}

					private void setDate(final HistoryInformationModel historyInformation, final Date lastModifiedAt) {
						final Date oldLastModifiedAt = historyInformation.getLastModifiedAt();
						historyInformation.setLastModifiedAt(lastModifiedAt);
						final NodeChangeEvent nodeChangeEvent = new NodeChangeEvent(node,
						    HistoryInformationModel.class, oldLastModifiedAt, lastModifiedAt);
						fireNodeChanged(node, nodeChangeEvent);
					}

					public String getDescription() {
						return null;
					}

					public void act() {
						setDate(historyInformation, now);
					}
				};
				Controller.getCurrentModeController().execute(historyActor, node.getMap());
			}
		}
		final NodeChangeEvent nodeChangeEvent = new NodeChangeEvent(node, property, oldValue, newValue);
		fireNodeChanged(node, nodeChangeEvent);
	}

	// nodes may only be refreshed by their own ModeController, so we have to store that too
	private final ConcurrentHashMap<NodeRefreshKey, NodeRefreshValue> nodesToRefresh = new ConcurrentHashMap<NodeRefreshKey, NodeRefreshValue>();

	private static class NodeRefreshKey{
		final NodeModel node;
		final Object property;
		public NodeRefreshKey(NodeModel node, Object property) {
			super();
			this.node = node;
			this.property = property;
		}
		@Override
		public int hashCode() {
			return node.hashCode() + propertyHash();
		}
		protected int propertyHash() {
			return property != null ? 37 * property.hashCode() : 0;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null || ! obj.getClass().equals(getClass()))
				return false;
			NodeRefreshKey key2 = (NodeRefreshKey)obj;
			return node.equals(key2.node) && (property == key2.property || property != null && property.equals(key2.property));
		}
	}

	private static class NodeRefreshValue{
		final ModeController controller;
		Object oldValue;
		Object newValue;
		public NodeRefreshValue(ModeController controller,
				Object oldValue, Object newValue) {
			super();
			this.controller = controller;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

	}

	/** optimization of nodeRefresh() as it handles multiple nodes in one Runnable, even nodes that weren't on the
	 * list when the thread was started.*/
	public void delayedNodeRefresh(final NodeModel node, final Object property, final Object oldValue,
	                               final Object newValue) {
	    final boolean startThread = nodesToRefresh.isEmpty();
	    final NodeRefreshValue value = new NodeRefreshValue(Controller.getCurrentModeController(), oldValue, newValue);
		final NodeRefreshKey key = new NodeRefreshKey(node, property);
		final NodeRefreshValue old = nodesToRefresh.put(key, value);
		if(old != null && old.newValue != value.newValue){
			old.newValue = value.newValue;
			nodesToRefresh.put(key, old);
		}
        if (startThread) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					final ModeController currentModeController = Controller.getCurrentModeController();
					final Iterator<Entry<NodeRefreshKey, NodeRefreshValue>> it = nodesToRefresh.entrySet().iterator();
					while (it.hasNext()) {
					    final Entry<NodeRefreshKey, NodeRefreshValue> entry = it.next();
					    final NodeRefreshValue info = entry.getValue();
					    if (info.controller == currentModeController){
					        final NodeRefreshKey key = entry.getKey();
							currentModeController.getMapController().nodeRefresh(key.node, key.property, info.oldValue, info.newValue);
					    }
					    it.remove();
					}
				}
			});
		}
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

	void removeMapChangeListener(final Class<? extends IActionOnChange> clazz, final Action action) {
		final Iterator<IMapChangeListener> iterator = mapChangeListeners.iterator();
		while (iterator.hasNext()) {
			final IMapChangeListener next = iterator.next();
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

	public Collection<IMapChangeListener> getMapChangeListeners() {
        return Collections.unmodifiableCollection(mapChangeListeners);
    }

	public Collection<IMapLifeCycleListener> getMapLifeCycleListeners() {
        return Collections.unmodifiableCollection(mapLifeCycleListeners);
    }

	public Collection<INodeChangeListener> getNodeChangeListeners() {
        return Collections.unmodifiableCollection(nodeChangeListeners);
    }

	public void select(final NodeModel node) {
		displayNode(node);
		Controller.getCurrentController().getSelection().selectAsTheOnlyOneSelected(node);
	}

	public void selectMultipleNodes(final NodeModel focussed, final Collection<NodeModel> selecteds) {
		for (final NodeModel node : selecteds) {
			displayNode(node);
		}
		select(focussed);
		for (final NodeModel node : selecteds) {
			Controller.getCurrentController().getSelection().makeTheSelected(node);
		}
	}

	public void setSaved(final MapModel mapModel, final boolean saved) {
		mapModel.setSaved(saved);
	}


	public void sortNodesByDepth(final List<NodeModel> collection) {
		Collections.sort(collection, new NodesDepthComparator());
	}

	public void toggleFolded() {
		final Collection<NodeModel> selectedNodes = getSelectedNodes();
		toggleFolded(selectedNodes);
	}

	public void toggleFolded(final Collection<NodeModel> collection) {
		Boolean isFolded = getCommonFoldingState(collection);
		final boolean shouldBeFolded = isFolded != null ?  ! isFolded : true;
		final NodeModel nodes[] = collection.toArray(new NodeModel[]{});
		for (final NodeModel node:nodes) {
			setFolded(node, shouldBeFolded);
		}
	}


	public void toggleFolded(final NodeModel node) {
		setFolded(node, ! node.isFolded());
	}

}
