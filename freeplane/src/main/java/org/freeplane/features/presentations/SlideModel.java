package org.freeplane.features.presentations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.NodeModel;

public class SlideModel implements NamedElement<SlideModel>{
	private String name;
	private boolean changesZoom;
	private float zoom;
	private boolean showsOnlySpecificNodes;
	private boolean showsAncestors;
	private boolean showsDescendants;
	private ASelectableCondition filterCondition;
	private Set<String> selectedNodeIds;
	private Set<String> visibleNodeIds;
	private boolean highlightsVisibleNodes;
	
	private final ArrayList<SlideChangeListener> slideChangeListeners;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SlideModel saveAs(String name) {
		return new SlideModel(name, new LinkedHashSet<String>(), new HashSet<String>(), changesZoom, zoom, showsOnlySpecificNodes, showsAncestors, showsDescendants, filterCondition);
	}

	public SlideModel(String name){
		this(name, new LinkedHashSet<String>(), new HashSet<String>(), false, 1f, false, false, false, null);
	}
	
	public SlideModel(String name, Set<String> selectedNodeIds, Set<String> visibleNodeIds, boolean changeZoom,
			float zoom, boolean showOnlySpecificNodes, boolean showAncestors, boolean showDescendants,
			ASelectableCondition filterCondition) {
		super();
		this.name = name;
		this.selectedNodeIds = selectedNodeIds;
		this.visibleNodeIds = visibleNodeIds;
		this.changesZoom = changeZoom;
		this.zoom = zoom;
		this.showsOnlySpecificNodes = showOnlySpecificNodes;
		this.showsAncestors = showAncestors;
		this.showsDescendants = showDescendants;
		this.filterCondition = filterCondition;
		slideChangeListeners = new ArrayList<>();
	}

	public boolean isNodeSelected(NodeModel node) {
		return selectedNodeIds.contains(node.getID());
	}

	public void setSelectedNodeIds(Set<String> selectedNodeIds) {
		if(this.selectedNodeIds != selectedNodeIds){
			this.selectedNodeIds = new LinkedHashSet<>(selectedNodeIds);
			fireSlideChangeEvent();
		}
	}

	public Set<String> getVisibleNodeIds() {
		return visibleNodeIds;
	}

	public boolean isNodeVisible(NodeModel node) {
		return visibleNodeIds.contains(node.getID());
	}
	

	public void setVisibleNodeIds(Set<String> visibleNodeIds) {
		if(this.visibleNodeIds != visibleNodeIds){
			this.visibleNodeIds = new HashSet<>(visibleNodeIds);
			fireSlideChangeEvent();
		}
	}

	public void addVisibleNodeIds(Collection<String> addedVisibleNodeIds) {
		if(visibleNodeIds.addAll(addedVisibleNodeIds)){
			fireSlideChangeEvent();
		}
	}

	public void removeVisibleNodeIds(Collection<String> removedVisibleNodeIds) {
		if(visibleNodeIds.removeAll(removedVisibleNodeIds)){
			fireSlideChangeEvent();
		}
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

	public boolean highlightsVisibleNodes() {
		return highlightsVisibleNodes;
	}

	public void setHighlightsVisibleNodes(boolean highlightsVisibleNodes) {
		if(this.highlightsVisibleNodes != highlightsVisibleNodes){
			this.highlightsVisibleNodes = highlightsVisibleNodes;
			fireSlideChangeEvent();
		}
	}

}
