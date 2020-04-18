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

import java.awt.GraphicsEnvironment;
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
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.UnknownElementWriter;
import org.freeplane.core.io.UnknownElements;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.UserRole;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.DelayedRunner;
import org.freeplane.features.clipboard.ClipboardControllers;
import org.freeplane.features.explorer.MapExplorerController;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.NodeModel.NodeChangeType;
import org.freeplane.features.map.clipboard.MapClipboardController;
import org.freeplane.features.mode.AController.IActionOnChange;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.SelectionController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.url.UrlManager;
import org.freeplane.main.addons.AddOnsController;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.n3.nanoxml.XMLParseException;
import org.freeplane.view.swing.map.NodeView;

/**
 * @author Dimitry Polivaev
 */
public class MapController extends SelectionController
implements IExtension, NodeChangeAnnouncer{
	public enum Direction {
		BACK, BACK_N_FOLD, FORWARD, FORWARD_N_FOLD
	}

	private static boolean hasValidSelection() {
		final IMapSelection selection = Controller.getCurrentController().getSelection();
		return selection != null && selection.getSelected() != null;
	}

	private static class ActionEnablerOnChange implements INodeChangeListener, INodeSelectionListener, IMapChangeListener {
		final private Collection<AFreeplaneAction> actions;
		final private DelayedRunner runner;

		public ActionEnablerOnChange(final ModeController modeController) {
			super();
			actions = new HashSet<AFreeplaneAction>();
			runner = new DelayedRunner(new Runnable() {
				@Override
				public void run() {
					if(modeController == Controller.getCurrentModeController())
						setActionsEnabledNow();
				}
			});
		}

		@Override
		public void nodeChanged(final NodeChangeEvent event) {
			setActionEnabled();
		}

		@Override
		public void onDeselect(final NodeModel node) {
		}

		@Override
		public void onSelect(final NodeModel node) {
			runner.runLater();
		}

		private void setActionsEnabledNow() {
			if (hasValidSelection()) {
				MapModel map = Controller.getCurrentController().getMap();
				UserRole userRole = Controller.getCurrentModeController().userRole(map);
				for (AFreeplaneAction action : actions)
					action.setEnabled(userRole);
			}
		}

		@Override
		public void mapChanged(MapChangeEvent event) {
			setActionEnabled();
		}

		@Override
		public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
			setActionEnabled();
		}

		@Override
		public void onNodeInserted(NodeModel parent, NodeModel child,
				int newIndex) {
			setActionEnabled();
		}

		@Override
		public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
			setActionEnabled();
		}

		@Override
		public void onPreNodeMoved(NodeMoveEvent nodeMoveEvent) {
		}

		@Override
		public void onPreNodeDelete(NodeDeletionEvent nodeDeletionEvent) {
			setActionEnabled();
		}

		private void setActionEnabled() {
			if (hasValidSelection())
				runner.runLater();
		}

		public void add(AFreeplaneAction action) {
			actions.add(action);
		}

		public void remove(AFreeplaneAction action) {
			actions.remove(action);
		}
	}

	private static class ActionSelectorOnChange implements INodeChangeListener, INodeSelectionListener, IMapChangeListener {
		final private Collection<AFreeplaneAction> actions;
		final private DelayedRunner runner;

		public ActionSelectorOnChange(final ModeController modeController) {
			super();
			actions = new HashSet<AFreeplaneAction>();
			runner = new DelayedRunner(new Runnable() {
				@Override
				public void run() {
					if(modeController == Controller.getCurrentModeController())
						setActionsSelectedNow();
				}
			});
		}

		@Override
		public void nodeChanged(final NodeChangeEvent event) {
			if (NodeChangeType.REFRESH.equals(event.getProperty())) {
				return;
			}
			setActionsSelected();
		}

		private void setActionsSelected() {
			if (hasValidSelection())
				runner.runLater();
		}

		private void setActionsSelectedNow() {
			if (hasValidSelection())
				for (AFreeplaneAction action : actions)
					action.setSelected();
		}

		@Override
		public void onDeselect(final NodeModel node) {
		}

		@Override
		public void onSelect(final NodeModel node) {
			setActionsSelected();
		}

		@Override
		public void mapChanged(final MapChangeEvent event) {
			setActionsSelected();
		}

		@Override
		public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
			setActionsSelected();
		}

		@Override
		public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
			setActionsSelected();
		}

		@Override
		public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
			setActionsSelected();
		}

		@Override
		public void onPreNodeDelete(NodeDeletionEvent nodeDeletionEvent) {
			setActionsSelected();
		}

		@Override
		public void onPreNodeMoved(NodeMoveEvent nodeMoveEvent) {
			setActionsSelected();
		}

		public void add(AFreeplaneAction action) {
			actions.add(action);
		}

		public void remove(AFreeplaneAction action) {
			actions.remove(action);
		}
	}

	public static void install() {
		final ConditionFactory conditionFactory = FilterController.getCurrentFilterController().getConditionFactory();
		conditionFactory.addConditionController(80, new NodeLevelConditionController());
		conditionFactory.addConditionController(75, new CloneConditionController());
	}


	public void addListenerForAction(final AFreeplaneAction action) {
		if (action.checkEnabledOnChange()) {
			actionEnablerOnChange.add(action);
		}
		if (action.checkSelectionOnChange()) {
			actionSelectorOnChange.add(action);
		}
	}

	public void removeListenerForAction(final AFreeplaneAction action) {
		if (action.checkEnabledOnChange()) {
			actionEnablerOnChange.remove(action);
		}
		if (action.checkSelectionOnChange()) {
			actionSelectorOnChange.remove(action);
		}
	}



	/**
	 * This class sorts nodes by ascending depth of their paths to root. This
	 * is useful to assure that children are cutted <b>before </b> their
	 * fathers!!!. Moreover, it sorts nodes with the same depth according to
	 * their position relative to each other.
	 */
	static private class NodesDepthComparator implements Comparator<NodeModel> {
		public NodesDepthComparator() {
		}

		/* the < relation. */
		@Override
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
			return n1.getParentNode().getIndex(n1) - n2.getParentNode().getIndex(n2);
		}
	}

// 	final private Controller controller;
	final private Collection<IMapChangeListener> mapChangeListeners;
	final private Collection<IMapLifeCycleListener> mapLifeCycleListeners;
	final private MapReader mapReader;
	final private MapWriter mapWriter;
 	final private ModeController modeController;
	final LinkedList<INodeChangeListener> nodeChangeListeners;
	final private ReadManager readManager;
	private final WriteManager writeManager;

	public MapController(ModeController modeController) {
		super();
		modeController.setMapController(this);
		refresher = new Refresher();
		this.modeController = modeController;
		mapLifeCycleListeners = new LinkedList<IMapLifeCycleListener>();
		addMapLifeCycleListener(modeController.getController());
		writeManager = new WriteManager();
		mapWriter = new MapWriter(this);
		readManager = new ReadManager();
		mapReader = new MapReader(readManager);
		readManager.addElementHandler("map", mapReader);
		readManager.addAttributeHandler("map", "version", new IAttributeHandler() {
			@Override
			public void setAttribute(final Object node, final String value) {
			}
		});
		readManager.addAttributeHandler("map", "dialect", new IAttributeHandler() {
			@Override
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
		actionEnablerOnChange = new ActionEnablerOnChange(modeController);
		actionSelectorOnChange = new ActionSelectorOnChange(modeController);
		addNodeSelectionListener(actionEnablerOnChange);
		addUINodeChangeListener(actionEnablerOnChange);
		addUIMapChangeListener(actionEnablerOnChange);
		addNodeSelectionListener(actionSelectorOnChange);
		addUINodeChangeListener(actionSelectorOnChange);
		addUIMapChangeListener(actionSelectorOnChange);
		final MapClipboardController mapClipboardController = createMapClipboardController();
		modeController.addExtension(MapClipboardController.class, mapClipboardController);
		createActions(modeController);
	}


	protected MapClipboardController createMapClipboardController() {
		final MapClipboardController mapClipboardController = new MapClipboardController();
		modeController.getExtension(ClipboardControllers.class).add(mapClipboardController);
		return mapClipboardController;
	}

	public void unfoldAndScroll(final NodeModel node, Filter filter) {
		final boolean wasFoldedOnCurrentView = canBeUnfoldedOnCurrentView(node, filter);
		unfold(node, filter);
		if (wasFoldedOnCurrentView && ResourceController.getResourceController().getBooleanProperty("scrollOnUnfold")) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					Controller.getCurrentController().getSelection().scrollNodeTreeToVisible(node);
				}
			});

		}
	}

	public void setFolded(final NodeModel node, final boolean fold, Filter filter) {
		if(!fold || node.isRoot())
			unfold(node, filter);
		else
			fold(node);
	}

	public void toggleFolded(final NodeModel node) {
        Filter filter = Controller.getCurrentController().getSelection().getFilter();
		if (canBeUnfoldedOnCurrentView(node, filter)) {
			unfold(node, filter);
		}
		else{
			fold(node);
		}
	}

	public void toggleFoldedAndScroll(final NodeModel node, Filter filter){
		if(canBeUnfoldedOnCurrentView(node, filter))
			unfoldAndScroll(node, filter);
		else
			fold(node);
	}

	public void unfold(final NodeModel node, Filter filter) {
		if (node.getChildCount() == 0)
			return;
		final boolean hiddenChildShown = unfoldHiddenChildren(node);
		boolean mapChanged = false;
	    if (canBeUnfoldedOnCurrentView(node, filter)) {
	    	unfoldUpToVisibleChild(node, filter);
			mapChanged = true;
		} else if (node.isFolded()) {
			mapChanged = true;
			setFoldingState(node, false);
		}
		if(mapChanged){
			fireFoldingChanged(node);
		}
		if(hiddenChildShown)
	        fireNodeUnfold(node);
	}

	public void fold(final NodeModel node) {
		if (node.getChildCount() == 0|| node.isRoot())
			return;
		final boolean hiddenChildShown = unfoldHiddenChildren(node);
		boolean mapChanged = false;
	    if (!node.isFolded()) {
			mapChanged = true;
		}
	    setFoldingState(node, true);
		if(mapChanged){
			fireFoldingChanged(node);
		}
		if(hiddenChildShown)
	        fireNodeUnfold(node);
	}


	protected void setFoldingState(final NodeModel node, final boolean folded) {
		node.setFolded(folded);
	}

	public boolean showNextChild(final NodeModel node) {
		if (node.getChildCount() == 0)
			return false;
		final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		final boolean unfold = mapViewManager.isFoldedOnCurrentView(node);
		if (unfold){
			mapViewManager.hideChildren(node);
			setFoldingState(node, false);
		}
		boolean childShown = false;
        Filter filter = Controller.getCurrentController().getSelection().getFilter();
		for(NodeModel child:node.getChildren()){
			if (mapViewManager.showHiddenNode(child)) {
				if (child.hasVisibleContent(filter)) {
					childShown = true;
					break;
				} else if (canBeUnfoldedOnCurrentView(child, filter)) {
					unfoldUpToVisibleChild(child, filter);
					childShown = true;
					break;
				}
			}
		}
		if(childShown){
			fireNodeUnfold(node);
		}
		return childShown;
	}


	private void fireNodeUnfold(final NodeModel node) {
		node.fireNodeChanged(new NodeChangeEvent(node, NodeView.Properties.HIDDEN_CHILDREN, null,
				null, false, false));
    }

	private void fireFoldingChanged(final NodeModel node) {
	    if (isFoldingPersistentAlways()) {
	    	final MapModel map = node.getMap();
	    	setSaved(map, false);
	    }
    }

	private boolean isFoldingPersistentAlways() {
	    final ResourceController resourceController = ResourceController.getResourceController();
		return resourceController.getProperty(NodeBuilder.RESOURCES_SAVE_FOLDING).equals(
	    	NodeBuilder.RESOURCES_ALWAYS_SAVE_FOLDING);
	}


	protected boolean unfoldHiddenChildren(NodeModel node) {
		final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		return ! mapViewManager.isFoldedOnCurrentView(node)
				&& mapViewManager.unfoldHiddenChildren(node);
	}


	public boolean canBeUnfoldedOnCurrentView(final NodeModel node, Filter filter) {
		final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		final boolean isFolded = mapViewManager.isFoldedOnCurrentView(node) ||  mapViewManager.hasHiddenChildren(node);
		for(int i = 0; i < node.getChildCount(); i++){
			final NodeModel child = node.getChildAt(i);
			if(child.hasVisibleContent(filter)){
				if (isFolded)
					return true;
			} else if (filter.getFilterInfo(node).isAncestor() && canBeUnfoldedOnCurrentView(child, filter)) {
				return true;
			}
		}
		return false;
	}

	private void unfoldUpToVisibleChild(final NodeModel node, Filter filter) {
		for(int i = 0; i < node.getChildCount(); i++){
			final NodeModel child = node.getChildAt(i);
			if (!child.hasVisibleContent(filter) && canBeUnfoldedOnCurrentView(child, filter)) {
				unfoldUpToVisibleChild(child, filter);
			}
		}
		setFoldingState(node, false);

	}

	public void addUIMapChangeListener(final IMapChangeListener listener) {
		if(!GraphicsEnvironment.isHeadless())
			mapChangeListeners.add(listener);
	}

	public void addMapChangeListener(final IMapChangeListener listener) {
		mapChangeListeners.add(listener);
	}

	public void addUINodeChangeListener(final INodeChangeListener listener) {
		if(!GraphicsEnvironment.isHeadless())
			nodeChangeListeners.add(listener);
	}

	public void addNodeChangeListener(final INodeChangeListener listener) {
		nodeChangeListeners.add(listener);
	}

	public void addMapLifeCycleListener(final IMapLifeCycleListener listener) {
		mapLifeCycleListeners.add(listener);
	}


	public void centerNode(final NodeModel node) {
		Controller.getCurrentController().getSelection().centerNode(node);
	}

	public List<NodeModel> childrenFolded(final NodeModel node) {
		if (node.isFolded()) {
			final List<NodeModel> empty = Collections.emptyList();
			return empty;
		}
		return node.getChildren();
	}

	public void closeWithoutSaving(final MapModel map) {
		fireMapRemoved(map);
		map.releaseResources();
	}

	/**
	 * @param modeController
	 * @param modeController
	 *
	 */
	private void createActions(ModeController modeController) {
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
	    IMapSelection selection = Controller.getCurrentController().getSelection();
	    if(node.getMap() != selection.getSelected().getMap())
	        return;
	    Filter filter = selection.getFilter();
		if (!node.hasVisibleContent(filter)) {
		    filter.getFilterInfo(node).reset();
			nodeRefresh(node);
		}
		final NodeModel[] path = node.getPathToRoot();
		for (int i = 0; i < path.length - 1; i++) {
			final NodeModel nodeOnPath = path[i];
			if (nodesUnfoldedByDisplay != null && isFolded(nodeOnPath)) {
            	nodesUnfoldedByDisplay.add(nodeOnPath);
            }
		}
		modeController.getController().getMapViewManager().displayOnCurrentView(node);
	}

	public void fireMapChanged(final MapChangeEvent event) {
		final MapModel map = event.getMap();
		if (map != null && event.setsDirtyFlag()) {
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
		final INodeChangeListener[] nodeChangeListeners = this.nodeChangeListeners.toArray(new INodeChangeListener[]{});
	    node.fireNodeChanged(nodeChangeListeners, nodeChangeEvent);
	}

	protected void fireNodeDeleted(final NodeDeletionEvent nodeDeletionEvent) {
		final IMapChangeListener[] list = mapChangeListeners.toArray(new IMapChangeListener[]{});
		for (final IMapChangeListener next : list) {
			next.onNodeDeleted(nodeDeletionEvent);
		}
		NodeModel node = nodeDeletionEvent.node;
		node.getMap().unregistryNodes(node);
	}

	protected void fireNodeInserted(final NodeModel parent, final NodeModel child, final int index) {
		parent.getMap().registryNodeRecursive(child);
		final IMapChangeListener[] list = mapChangeListeners.toArray(new IMapChangeListener[]{});
		for (final IMapChangeListener next : list) {
			next.onNodeInserted(parent, child, index);
		}
	}

	protected void fireNodeMoved(final NodeMoveEvent nodeMoveEvent) {
		final IMapChangeListener[] list = mapChangeListeners.toArray(new IMapChangeListener[]{});
		for (final IMapChangeListener next : list) {
			next.onNodeMoved(nodeMoveEvent);
		}
	}

	protected void firePreNodeMoved(final NodeMoveEvent nodeMoveEvent) {
		final IMapChangeListener[] list = mapChangeListeners.toArray(new IMapChangeListener[]{});
		for (final IMapChangeListener next : list) {
			next.onPreNodeMoved(nodeMoveEvent);
		}
	}

	protected void firePreNodeDelete(final NodeDeletionEvent nodeDeletionEvent) {
		final IMapChangeListener[] list = mapChangeListeners.toArray(new IMapChangeListener[]{});
		for (final IMapChangeListener next : list) {
			next.onPreNodeDelete(nodeDeletionEvent);
		}
	}

	public void getFilteredXml(final MapModel map, final Writer fileout, final Mode mode, final boolean forceFormat)
			throws IOException {
		getMapWriter().writeMapAsXml(map, fileout, mode, false, forceFormat);
	}

	public void getFilteredXml(Collection<NodeModel> nodes, final Writer fileout, final Mode mode, final boolean forceFormat)
			throws IOException {
		for(NodeModel node :nodes)
		getMapWriter().writeNodeAsXml(fileout, node, mode, false, true, forceFormat);
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
	public NodeModel getNodeFromID_(final String nodeID) {
		final MapModel map = Controller.getCurrentController().getMap();
		if(map == null)
			return null;
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
	public List<NodeModel> getSelectedNodes() {
		final IMapSelection selection = Controller.getCurrentController().getSelection();
		if (selection == null) {
			return Collections.emptyList();
		}
		return selection.getOrderedSelection();
	}

	public WriteManager getWriteManager() {
		return writeManager;
	}

	/**
	 * True iff one of node's <i>strict</i> descendants is folded. A node N is
	 * not its strict descendant - the fact that node itself is folded is not
	 * sufficient to return true.
	 */
	public boolean hasFoldedStrictDescendant(final NodeModel node) {
		for (final NodeModel child : node.getChildren()) {
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
		if(parent.getParentNode() != null){
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
	public void openMap(final URL url) throws FileNotFoundException, XMLParseException,IOException, URISyntaxException, XMLException{
		if (AddOnsController.getController().installIfAppropriate(url))
			return;
		final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		if (mapViewManager.tryToChangeToMapView(url))
			return;
		try {
			Controller.getCurrentController().getViewController().setWaitingCursor(true);
			final MapModel newModel = new MapModel();
			UrlManager.getController().loadCatchExceptions(url, newModel);
			newModel.setReadOnly(true);
			newModel.setSaved(true);
			fireMapCreated(newModel);
			createMapView(newModel);
		}
		finally {
			Controller.getCurrentController().getViewController().setWaitingCursor(false);
		}
	}


	public void openMapSelectReferencedNode(final URL url) throws FileNotFoundException,
	        XMLParseException, IOException, URISyntaxException, XMLException, MalformedURLException {
	    String nodeReference = url.getRef();
	    if(nodeReference != null){
	    	openMap(new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath()));
	    	final NodeModel node = getNodeAt(nodeReference);
	    	if(node != null)
	    		select(node);
	    }
	    else{
	    	openMap(url);
	    }
	}


	private NodeModel getNodeAt(String nodeReference) {
		return modeController.getExtension(MapExplorerController.class).getNodeAt(getRootNode(), nodeReference);
	}

	public void createMapView(final MapModel mapModel) {
		mapModel.beforeViewCreated();
		Controller.getCurrentController().getMapViewManager().newMapView(mapModel, Controller.getCurrentModeController());
	}

	public MapModel newMap() {
		final MapModel mindMapMapModel = new MapModel();
		mindMapMapModel.createNewRoot();
		fireMapCreated(mindMapMapModel);
		createMapView(mindMapMapModel);
		return mindMapMapModel;
	}

	public NodeModel newNode(final Object userObject, final MapModel map) {
		return new NodeModel(userObject, map);
	}

	@Override
	public void nodeChanged(final NodeModel node) {
		nodeChanged(node, NodeModel.UNKNOWN_PROPERTY, null, null);
	}

	@Override
	public void nodeChanged(final NodeModel node, final Object property, final Object oldValue, final Object newValue) {
		final NodeChangeEvent nodeChangeEvent = new NodeChangeEvent(node, property, oldValue, newValue, true, true);
		nodeRefresh(nodeChangeEvent);
	}

	@Override
	public void nodeRefresh(final NodeModel node) {
		nodeRefresh(node, NodeModel.UNKNOWN_PROPERTY, null, null);
	}

	@Override
	public void nodeRefresh(final NodeModel node, final Object property, final Object oldValue, final Object newValue) {
		final NodeChangeEvent nodeChangeEvent = new NodeChangeEvent(node, property, oldValue, newValue, false, false);
		nodeRefresh(nodeChangeEvent);
	}

	@Override
	public void nodeRefresh(final NodeChangeEvent nodeChangeEvent) {
		if (mapReader.isMapLoadingInProcess()) {
			return;
		}
		final NodeModel node = nodeChangeEvent.getNode();
		final MapModel map = node.getMap();
		if(nodeChangeEvent.setsDirtyFlag())
			setSaved(map, false);
		if (nodeChangeEvent.updatesModificationTime() && !map.isUndoActionRunning()) {
			final HistoryInformationModel historyInformation = node.getHistoryInformation();
			if (historyInformation != null) {
				final IActor historyActor = new IActor() {
					private final Date lastModifiedAt = historyInformation.getLastModifiedAt();
					private final Date now = new Date();

					@Override
					public void undo() {
						setDate(historyInformation, lastModifiedAt);
					}

					private void setDate(final HistoryInformationModel historyInformation, final Date lastModifiedAt) {
						final Date oldLastModifiedAt = historyInformation.getLastModifiedAt();
						historyInformation.setLastModifiedAt(lastModifiedAt);
						final NodeChangeEvent nodeChangeEvent = new NodeChangeEvent(node,
						    HistoryInformationModel.class, oldLastModifiedAt, lastModifiedAt, false, false);
						fireNodeChanged(node, nodeChangeEvent);
					}

					@Override
					public String getDescription() {
						return null;
					}

					@Override
					public void act() {
						setDate(historyInformation, now);
					}
				};
				Controller.getCurrentModeController().execute(historyActor, map);
			}
		}
		fireNodeChanged(node, nodeChangeEvent);
	}


	// nodes may only be refreshed by their own ModeController, so we have to store that too
	private final ActionEnablerOnChange actionEnablerOnChange;
	private final ActionSelectorOnChange actionSelectorOnChange;
	private final Refresher refresher;

	static class NodeRefreshKey{
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

	static class NodeRefreshValue{
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

	static class Refresher {
		private final ConcurrentHashMap<NodeRefreshKey, NodeRefreshValue> nodesToRefresh = new ConcurrentHashMap<NodeRefreshKey, NodeRefreshValue>();

		/** optimization of nodeRefresh() as it handles multiple nodes in one Runnable, even nodes that weren't on the
		 * list when the thread was started.*/
		void delayedNodeRefresh(final NodeModel node, final Object property, final Object oldValue,
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
				final Runnable refresher = new Runnable() {
					@Override
					public void run() {
						final ModeController currentModeController = Controller.getCurrentModeController();
						@SuppressWarnings("unchecked")
						final Entry<NodeRefreshKey, NodeRefreshValue>[] entries = nodesToRefresh.entrySet().toArray(new Entry[]{} );
						nodesToRefresh.clear();
						for (Entry<NodeRefreshKey, NodeRefreshValue> entry : entries) {
							final NodeRefreshValue info = entry.getValue();
							if (info.controller == currentModeController){
								final NodeRefreshKey key = entry.getKey();
								currentModeController.getMapController().nodeRefresh(key.node, key.property, info.oldValue, info.newValue);
							}
						}
					}
				};
				Controller.getCurrentController().getViewController().invokeLater(refresher);
			}
		}
	}
	public void delayedNodeRefresh(final NodeModel node, final Object property, final Object oldValue,
			final Object newValue){
		if(Controller.getCurrentController().getViewController().isDispatchThread())
			refresher.delayedNodeRefresh(node, property, oldValue, newValue);
		else
			nodeRefresh(node, property, oldValue, newValue);
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
		final MapModel map = node.getMap();
		final Controller controller = Controller.getCurrentController();
		if (! map.equals(controller.getMap())){
			controller.getMapViewManager().changeToMap(map);
		}
		displayNode(node);
		IMapSelection selection = controller.getSelection();
        selection.selectAsTheOnlyOneSelected(node);
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

	public void toggleFolded(Filter filter, final Collection<NodeModel> collection) {
		Boolean shouldBeFolded = ! canBeUnfoldedOnCurrentView(filter, collection);
		final NodeModel nodes[] = collection.toArray(new NodeModel[]{});
		for (final NodeModel node:nodes) {
			setFolded(node, shouldBeFolded, filter);
		}
	}

	private boolean canBeUnfoldedOnCurrentView(Filter filter, Collection<NodeModel> collection) {
		for(NodeModel node : collection){
			if(node.isRoot())
				return false;
		}
		for(NodeModel node : collection){
			if(canBeUnfoldedOnCurrentView(node, filter))
				return true;
		}
		return false;
	}


	public ModeController getModeController() {
		return modeController;
	}


	public void select(String nodeReference) {
		select(getNodeFromID_(nodeReference));
	}

}
