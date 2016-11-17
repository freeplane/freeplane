package org.freeplane.features.presentations.mindmapmode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.DisjunctConditions;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.filter.condition.SelectedViewSnapshotCondition;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;

public class Slide implements NamedElement<Slide>{
	public static final Slide ALL_NODES = new Slide("All nodes");
	private String name;
	private boolean changesZoom;
	private boolean centersSelectedNode;
	private float zoom;
	private boolean showsOnlySpecificNodes;
	private boolean showsAncestors;
	private boolean showsDescendants;
	private ASelectableCondition filterCondition;
	private Set<String> selectedNodeIds;
	private boolean foldsNodes;
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
		return new Slide(name, new LinkedHashSet<String>(), centersSelectedNode,
		    changesZoom, zoom, showsOnlySpecificNodes, showsAncestors, showsDescendants, null);
	}

	public Slide(String name){
		this(name, new LinkedHashSet<String>(), false, false, 1f, false, false, false, null);
	}
	
	private Slide(String name, Set<String> selectedNodeIds, boolean centerSelectedNode,
	                  boolean changeZoom,
			float zoom, boolean showOnlySpecificNodes, boolean showAncestors, boolean showDescendants,
			ASelectableCondition filterCondition) {
		super();
		this.name = name;
		this.selectedNodeIds = selectedNodeIds;
		this.centersSelectedNode = centerSelectedNode;
		this.changesZoom = changeZoom;
		this.zoom = zoom;
		this.showsOnlySpecificNodes = showOnlySpecificNodes;
		this.showsAncestors = showAncestors;
		this.showsDescendants = showDescendants;
		this.filterCondition = filterCondition;
		slideChangeListeners = new ArrayList<>();
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
	
	public boolean foldsNodes(){
		return foldsNodes;
	}
	
	public void unsetFoldsNodes(){
		if(this.foldsNodes){
			this.foldsNodes = false;
			foldedNodeIds.clear();
			fireSlideChangeEvent();
		}
	}
	
	public Collection<String> getCurrentFoldedNodeIds(){
		return createNodeIterator().getCurrentFoldedNodeIds();
	}
	
	public void setFoldedNodeIDs(Collection<String> foldedNodeIds) {
		if(this.foldedNodeIds != foldedNodeIds){
			this.foldedNodeIds = new LinkedHashSet<>(foldedNodeIds);
			foldsNodes = true;
			fireSlideChangeEvent();
		}
	}
	
	private class NodeIterator {
		
		private final IMapViewManager mapViewManager;
		private Filter filter;
		
		public NodeIterator(IMapViewManager mapViewManager) {
			super();
			this.mapViewManager = mapViewManager;
		}
		
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
			if(mapViewManager.isFoldedOnCurrentView(node)){
				if(filter.isVisible(node))
					nodeIds.add(node.getID());
				return;
			}
			else if(filter.isVisible(node))
				for(NodeModel child : node.getChildren())
					addCurrentFoldedNodeIds(child, nodeIds);
		}

		public void foldNodes() {
			if(foldsNodes) {
				filter = new Filter(getEffectiveFilterCondition(), true, showsDescendants, false);
				foldNodes(getMap().getRootNode());
				filter = null;
			}
		}

		private void foldNodes(NodeModel node) {
			if(filter.isVisible(node)) {
				if(foldedNodeIds.contains(node.getID())) {
					mapViewManager.setFoldedOnCurrentView(node, true);
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

	public boolean centersSelectedNode() {
		return centersSelectedNode;
	}

	public void setCentersSelectedNode(boolean centersSelectedNode) {
		this.centersSelectedNode = centersSelectedNode;
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
				Controller.getCurrentModeController().getMapController().displayNode(node);
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

	void apply() {
		applyFilter();
		applySelection();
		foldNodes();
		applyZoom();
	}


	public NodeIterator createNodeIterator() {
		return new NodeIterator(Controller.getCurrentController().getMapViewManager());
	} 
	
	private void foldNodes() {
		createNodeIterator().foldNodes();
	}

	private void applyZoom() {
		if (changesZoom)
			Controller.getCurrentController().getMapViewManager().setZoom(zoom);
	}

	private void applySelection() {
		if (selectedNodeIds.isEmpty())
			return;
		MapModel map = getMap();
		NodeModel node = map.getNodeForID(selectedNodeIds.iterator().next());
		if (!showsOnlySpecificNodes)
			replaceCurrentSelection();
		else {
			if (node != null)
				Controller.getCurrentController().getSelection().selectAsTheOnlyOneSelected(node);
		}
		if (node != null && centersSelectedNode)
			Controller.getCurrentController().getSelection().centerNode(node);
	}

	private MapModel getMap() {
		return Controller.getCurrentController().getMap();
	}

	private void applyFilter() {
		MapModel map = getMap();
		final ICondition condition = getEffectiveFilterCondition();
		new Filter(condition, showsAncestors, showsDescendants, false).applyFilter(this, map, false);
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

}
