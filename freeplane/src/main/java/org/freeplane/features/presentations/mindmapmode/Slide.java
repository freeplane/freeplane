package org.freeplane.features.presentations.mindmapmode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConjunctConditions;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.filter.condition.SelectedViewSnapshotCondition;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class Slide implements NamedElement<Slide>{
	private String name;
	private boolean changesZoom;
	private boolean centersSelectedNode;
	private float zoom;
	private boolean showsOnlySpecificNodes;
	private boolean showsAncestors;
	private boolean showsDescendants;
	private ASelectableCondition filterCondition;
	private Set<String> selectedNodeIds;
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
		    changesZoom, zoom, showsOnlySpecificNodes, showsAncestors, showsDescendants, filterCondition);
	}

	public Slide(String name){
		this(name, new LinkedHashSet<String>(), false, false, 1f, false, false, false, null);
	}
	
	public Slide(String name, Set<String> selectedNodeIds, boolean centerSelectedNode,
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

	public void addSelectionChangeListener(SlideChangeListener slideChangeListener) {
		this.slideChangeListeners.add(slideChangeListener);
	}

	public void removeSelectionChangeListener(SlideChangeListener slideChangeListener) {
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
		applyZoom();
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
		final ICondition  condition;
		if(showsOnlySpecificNodes && filterCondition != null){
			SelectedViewSnapshotCondition selectedViewSnapshotCondition = getFilterConditionForSelectedNodes();
			condition = new ConjunctConditions(selectedViewSnapshotCondition, filterCondition);
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
		MapModel map = getMap();
		new Filter(condition, showsAncestors, showsDescendants, false).applyFilter(this, map, false);
	}

	private SelectedViewSnapshotCondition getFilterConditionForSelectedNodes() {
		ArrayList<NodeModel> selectedNodes = getSelectedNodes(false);
		SelectedViewSnapshotCondition selectedViewSnapshotCondition = new SelectedViewSnapshotCondition(selectedNodes);
		return selectedViewSnapshotCondition;
	}

}
