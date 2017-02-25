package org.freeplane.features.presentations.mindmapmode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.DisjunctConditions;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.filter.condition.SelectedViewSnapshotCondition;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;

public class Slide implements NamedElement<Slide>{
	private static final String PRESENTATION_SLOW_MOTION_KEY = "presentation.slowMotion";
	public static final Slide ALL_NODES = new Slide("All nodes");
	private static final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
	private String name;
	private boolean changesZoom;
	private String centeredNodeId;
	private float zoom;
	private boolean showsOnlySpecificNodes;
	private boolean showsAncestors;
	private boolean showsDescendants;
	private ASelectableCondition filterCondition;
	private Set<String> selectedNodeIds;
	private Set<String> foldedNodeIds;
	private final ArrayList<SlideChangeListener> slideChangeListeners;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Slide saveAs(String name) {
		return new Slide(name, new LinkedHashSet<String>(), centeredNodeId != null ? "" : null,
		    changesZoom, zoom, showsOnlySpecificNodes, showsAncestors, showsDescendants, null);
	}

	public Slide(String name){
		this(name, new LinkedHashSet<String>(), null, false, 1f, false, false, false, null);
	}
	
	private Slide(String name, Set<String> selectedNodeIds, String centeredNodeId,
	                  boolean changeZoom,
			float zoom, boolean showOnlySpecificNodes, boolean showAncestors, boolean showDescendants,
			ASelectableCondition filterCondition) {
		super();
		this.name = name;
		this.selectedNodeIds = selectedNodeIds;
		this.centeredNodeId = centeredNodeId;
		this.changesZoom = changeZoom;
		this.zoom = zoom;
		this.showsOnlySpecificNodes = showOnlySpecificNodes;
		this.showsAncestors = showAncestors;
		this.showsDescendants = showDescendants;
		this.filterCondition = filterCondition;
		slideChangeListeners = new ArrayList<>();
		foldedNodeIds = null;
	}

	public Set<String> getSelectedNodeIds() {
		return selectedNodeIds;
	}

	public void setSelectedNodeIds(Set<String> selectedNodeIds) {
		if(this.selectedNodeIds != selectedNodeIds){
			this.selectedNodeIds = new LinkedHashSet<>(selectedNodeIds);
			fireSlideChangeEvent();
		}
	}

	public void addSelectedNodeIds(Collection<String> selectedNodeIds) {
		if (this.selectedNodeIds.addAll(selectedNodeIds)) {
			fireSlideChangeEvent();
		}
	}

	public void removeSelectedNodeIds(Collection<String> selectedNodeIds) {
		if (this.selectedNodeIds.removeAll(selectedNodeIds)) {
			fireSlideChangeEvent();
		}
	}
	
	public Set<String> getFoldedNodeIds() {
		return foldedNodeIds;
	}
	
	public boolean isNodeFolded(NodeModel node) {
		return foldsNodes() &&  foldedNodeIds.contains(node.getID());
	}
	
	public boolean foldsNodes(){
		return foldedNodeIds != null;
	}
	
	public void unsetFoldsNodes(){
		if(foldedNodeIds != null){
			foldedNodeIds = null;
			fireSlideChangeEvent();
		}
	}
	
	public Collection<String> getCurrentFoldedNodeIds(){
		return createNodeIterator().getCurrentFoldedNodeIds();
	}
	
	public void setFoldedNodeIDs(Collection<String> foldedNodeIds) {
		if(this.foldedNodeIds != foldedNodeIds){
			this.foldedNodeIds = new LinkedHashSet<>(foldedNodeIds);
			fireSlideChangeEvent();
		}
	}
	
	private static class SlideFilter extends Filter {
		private boolean selectsVisibleNodes;

		private SlideFilter(ICondition condition, boolean areAncestorsShown, boolean areDescendantsShown,
				boolean applyToVisibleNodesOnly) {
			super(condition, areAncestorsShown, areDescendantsShown, applyToVisibleNodesOnly);
			selectsVisibleNodes = false;
		}

		@Override
		protected void selectVisibleNode() {
			if(selectsVisibleNodes)
				selectVisibleNodesNow();
		}

		protected void selectVisibleNodesNow() {
			super.selectVisibleNode();
			selectsVisibleNodes = true;
		}
	}

	private class NodeIterator {
		
		private Filter filter;
		
		public Set<String> getCurrentFoldedNodeIds(){
			filter = calculateFilterResults();
			Set<String> foldedNodeIds = calculateCurrentFoldedNodeIds();
			filter = null;
			return foldedNodeIds;
		}

		private Set<String> calculateCurrentFoldedNodeIds() {
			MapModel map = getMap();
			HashSet<String> nodeIds = new HashSet<>();
			addCurrentFoldedNodeIds(map.getRootNode(), nodeIds);
			return nodeIds;
		}

		private Filter calculateFilterResults() {
			MapModel map = getMap();
			final ICondition condition = getEffectiveFilterCondition();
			Filter filter = Filter.createOneTimeFilter(condition, true, showsDescendants, false);
			filter.calculateFilterResults(map);
			return filter;
		}
		
		private void addCurrentFoldedNodeIds(NodeModel node, HashSet<String> nodeIds) {
			if(isFoldedOnCurrentView(node)){
				if(filter.isVisible(node))
					nodeIds.add(node.getID());
				return;
			}
			else if(filter.isVisible(node))
				for(NodeModel child : node.getChildren())
					addCurrentFoldedNodeIds(child, nodeIds);
		}

		public void foldNodes() {
			if(foldsNodes()) {
				filter = new Filter(getEffectiveFilterCondition(), true, showsDescendants, false);
				foldNodes(getMap().getRootNode());
				filter = null;
			}
		}

		private void foldNodes(NodeModel node) {
			if(filter.isVisible(node)) {
				if(foldedNodeIds.contains(node.getID())) {
					setFoldedOnCurrentView(node, true);
					return;
				}
				mapViewManager.setFoldedOnCurrentView(node, false);
				for(NodeModel child : node.getChildren())
					foldNodes(child);
			}
		}

	}
	
	public boolean isNodeVisible(NodeModel node) {
		return selectedNodeIds.contains(node.getID());
	}

	public String getCenteredNodeId() {
		return centeredNodeId;
	}

	public void setCenteredNodeId(String centeredNodeId) {
		this.centeredNodeId = centeredNodeId;
	}

	public boolean changesZoom() {
		return changesZoom;
	}

	public void setChangesZoom(boolean changeZoom) {
		if(this.changesZoom != changeZoom) {
			this.changesZoom = changeZoom;
			fireSlideChangeEvent();
		}
	}

	public float getZoom() {
		return zoom;
	}

	public void setZoom(float zoom) {
		if(this.zoom != zoom) {
			this.zoom = zoom;
			fireSlideChangeEvent();
		}
	}

	public boolean showsOnlySpecificNodes() {
		return showsOnlySpecificNodes;
	}

	public void setShowsOnlySpecificNodes(boolean showOnlySpecificNodes) {
		if(this.showsOnlySpecificNodes != showOnlySpecificNodes) {
			this.showsOnlySpecificNodes = showOnlySpecificNodes;
			fireSlideChangeEvent();
		}
	}

	public boolean showsAncestors() {
		return showsAncestors;
	}

	public void setShowsAncestors(boolean showAncestors) {
		if(this.showsAncestors != showAncestors) {
			this.showsAncestors = showAncestors;
			fireSlideChangeEvent();
		}
	}

	public boolean showsDescendants() {
		return showsDescendants;
	}

	public void setShowsDescendants(boolean showDescendants) {
		if(this.showsDescendants != showDescendants) {
			this.showsDescendants = showDescendants;
			fireSlideChangeEvent();
		}
	}

	public ASelectableCondition getFilterCondition() {
		return filterCondition;
	}

	public void setFilterCondition(ASelectableCondition filterCondition) {
		if(this.filterCondition != filterCondition) {
			this.filterCondition = filterCondition;
			fireSlideChangeEvent();
		}
	}

	public void addSlideChangeListener(SlideChangeListener slideChangeListener) {
		this.slideChangeListeners.add(slideChangeListener);
	}

	public void removeSlideChangeListener(SlideChangeListener slideChangeListener) {
		this.slideChangeListeners.remove(slideChangeListener);
	}
	
	private void fireSlideChangeEvent() {
		for (SlideChangeListener slideChangeListener : slideChangeListeners)
			slideChangeListener.onSlideModelChange(SlideChangeEvent.of(this));
	}

	void replaceCurrentSelection() {
		ArrayList<NodeModel> selectedNodes = getSelectedNodes(true);
		if (!selectedNodes.isEmpty()) {
			NodeModel[] nodes = selectedNodes.toArray(new NodeModel[] {});
			for (NodeModel node : nodes)
				displayOnCurrentView(node);
			Controller.getCurrentController().getSelection().replaceSelection(nodes);
		}
	}

	private ArrayList<NodeModel> getSelectedNodes(boolean onlyVisible) {
		MapModel map = getMap();
		ArrayList<NodeModel> selectedNodes = new ArrayList<>(selectedNodeIds.size());
		for (String id : selectedNodeIds) {
			NodeModel node = map.getNodeForID(id);
			if (node != null && (!onlyVisible || node.isVisible()))
				selectedNodes.add(node);
		}
		return selectedNodes;
	}

	void apply(float zoomFactor) {
		final SlideFilter slideFilter = applyFilter();
		applySelection();
		foldNodes();
		applyZoom(zoomFactor);
		centerSelectedNode();
		slideFilter.selectVisibleNodesNow();
		scrollMapToSelectedNode();
	}

	private void scrollMapToSelectedNode() {
		if(centeredNodeId == null){
			final Controller controller = Controller.getCurrentController();
			final NodeModel selected = controller.getSelection().getSelected();
			controller.getMapViewManager().scrollNodeToVisible(selected);
		}
	}


	public NodeIterator createNodeIterator() {
		return new NodeIterator();
	} 
	
	private void foldNodes() {
		createNodeIterator().foldNodes();
	}

	private void applyZoom(float zoomFactor) {
		if (changesZoom)
			Controller.getCurrentController().getMapViewManager().setZoom(zoom * zoomFactor);
	}
	
	private boolean displaysAllSlideNodes() {
		return ResourceController.getResourceController().getBooleanProperty("presentation.slideDisplaysAllNodes");
	}


	private void applySelection() {
		if (selectedNodeIds.isEmpty())
			return;
		ArrayList<NodeModel> selectedNodes = getSelectedNodes(true);
		final boolean displaysAllSlideNodes = displaysAllSlideNodes();
		final boolean selectsAllVisibleNodes = displaysAllSlideNodes && showsOnlySpecificNodes && mapViewManager.isPresentationModeEnabled();
		final boolean replacesSelectionBySelectedNodes = ! (selectsAllVisibleNodes || showsOnlySpecificNodes || selectedNodes.isEmpty());
		if(! replacesSelectionBySelectedNodes && ! foldsNodes() && displaysAllSlideNodes){
			for (NodeModel node : selectedNodes) {
				displayOnCurrentView(node);
				if(showsDescendants)
					displayDescendantsOnCurrentView(node);
			}
		}

		final IMapSelection selection = Controller.getCurrentController().getSelection();
		if (replacesSelectionBySelectedNodes) {
			NodeModel[] nodes = selectedNodes.toArray(new NodeModel[] {});
			selection.replaceSelection(nodes);
		}
		if (showsOnlySpecificNodes) {
			final NodeModel firstNode = selectedNodes.get(0);
			selection.selectAsTheOnlyOneSelected(firstNode);
		} 
		if(selectsAllVisibleNodes){
			if(showsAncestors) {
				final NodeModel rootNode = selection.getSelected().getMap().getRootNode();
				selection.selectBranch(rootNode, true);
			} else for(NodeModel node :selectedNodes)
				selection.selectBranch(node, true);
		}
	}

	private void displayDescendantsOnCurrentView(NodeModel node) {
		mapViewManager.setFoldedOnCurrentView(node, false);
		for(NodeModel child : node.getChildren()) {
			displayDescendantsOnCurrentView(child);
		}
	}

	private void centerSelectedNode() {
		MapModel map = getMap();
		if (centeredNodeId != null) {
			NodeModel centeredNode = map.getNodeForID(centeredNodeId);
			final IMapSelection selection = Controller.getCurrentController().getSelection();
			if(centeredNode != null && centeredNode.hasVisibleContent()) {
				displayOnCurrentView(centeredNode);
			} else {
				centeredNode = selection.getSelected();
			}
			final boolean slowMotion = ResourceController.getResourceController().getBooleanProperty(PRESENTATION_SLOW_MOTION_KEY, false);
			if(slowMotion)
				selection.centerNodeSlowly(centeredNode);
			else
				selection.centerNode(centeredNode);
		}
	}

	private MapModel getMap() {
		return Controller.getCurrentController().getMap();
	}

	private SlideFilter applyFilter() {
		MapModel map = getMap();
		final ICondition condition = getEffectiveFilterCondition();
		final SlideFilter filter = new SlideFilter(condition, showsAncestors, showsDescendants, false);
		filter.applyFilter(this, map, false);
		return filter;
	}

	public ICondition getEffectiveFilterCondition() {
		final ICondition  condition;
		if(showsOnlySpecificNodes && filterCondition != null){
			SelectedViewSnapshotCondition selectedViewSnapshotCondition = getFilterConditionForSelectedNodes();
			condition = new DisjunctConditions(selectedViewSnapshotCondition, filterCondition);
		}
		else if (showsOnlySpecificNodes && filterCondition == null) {
			condition = getFilterConditionForSelectedNodes();
		}
		else if (!showsOnlySpecificNodes && filterCondition != null) {
			condition = filterCondition;
		}
		else{
			condition = null;
		}
		return condition;
	}

	private SelectedViewSnapshotCondition getFilterConditionForSelectedNodes() {
		ArrayList<NodeModel> selectedNodes = getSelectedNodes(false);
		SelectedViewSnapshotCondition selectedViewSnapshotCondition = new SelectedViewSnapshotCondition(selectedNodes);
		return selectedViewSnapshotCondition;
	}

	private boolean isFoldedOnCurrentView(NodeModel node) {
		return mapViewManager.isFoldedOnCurrentView(node);
	}

	private void displayOnCurrentView(NodeModel node) {
		mapViewManager.displayOnCurrentView(node);
	}

	private void setFoldedOnCurrentView(NodeModel node, boolean folded) {
		mapViewManager.setFoldedOnCurrentView(node, folded);
	}
}
