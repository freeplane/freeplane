package org.freeplane.features.presentations.mindmapmode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class SlideModel implements NamedElement<SlideModel>{
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SlideModel saveAs(String name) {
		return new SlideModel(name, new LinkedHashSet<String>(), centersSelectedNode,
		    changesZoom, zoom, showsOnlySpecificNodes, showsAncestors, showsDescendants, filterCondition);
	}

	public SlideModel(String name){
		this(name, new LinkedHashSet<String>(), false, false, 1f, false, false, false, null);
	}
	
	public SlideModel(String name, Set<String> selectedNodeIds, boolean centerSelectedNode,
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
		Set<String> selectedNodeIds = getSelectedNodeIds();
		MapModel map = Controller.getCurrentController().getMap();
		ArrayList<NodeModel> selectedNodes = new ArrayList<>(selectedNodeIds.size());
		for (String id : selectedNodeIds) {
			NodeModel node = map.getNodeForID(id);
			if (node != null && node.isVisible())
				selectedNodes.add(node);
		}
		if (!selectedNodes.isEmpty()) {
			NodeModel[] nodes = selectedNodes.toArray(new NodeModel[] {});
			for (NodeModel node : nodes)
				Controller.getCurrentModeController().getMapController().displayNode(node);
			Controller.getCurrentController().getSelection().replaceSelection(nodes);
		}
	}

}
