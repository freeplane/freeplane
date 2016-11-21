package org.freeplane.features.presentations.mindmapmode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.freeplane.core.undo.IActor;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

class UndoableSlide {
	final private ModeController controller;
	final private MapModel mapModel;
	final private Slide slide;
	
	public static UndoableSlide of(Slide slide){
		return new UndoableSlide(Controller.getCurrentModeController(), Controller.getCurrentController().getMap(), slide);
	}

	public UndoableSlide(ModeController controller, MapModel mapModel, Slide slide) {
		super();
		this.controller = controller;
		this.mapModel = mapModel;
		this.slide = slide;
	}
	public void setName(final String name) {
		final String oldName = slide.getName();
		if(name.equals(oldName))
			return;
		IActor actor = new IActor() {
			
			@Override
			public String getDescription() {
				return "setName";
			}
			
			@Override
			public void act() {
				slide.setName(name);
			}
			
			@Override
			public void undo() {
				slide.setName(oldName);
			}
			
		};
		controller.execute(actor, mapModel);
	}
	public void setSelectedNodeIds(final Set<String> selectedNodeIds) {
		final Set<String> oldSelectedNodeIds = slide.getSelectedNodeIds();
		if(selectedNodeIds.equals(oldSelectedNodeIds))
			return;
		IActor actor = new IActor() {
			
			@Override
			public String getDescription() {
				return "setSelectedNodeIds";
			}
			
			@Override
			public void act() {
				slide.setSelectedNodeIds(selectedNodeIds);
			}
			
			@Override
			public void undo() {
				slide.setSelectedNodeIds(oldSelectedNodeIds);
			}
			
		};
		controller.execute(actor, mapModel);
	}
	
	public void addSelectedNodeIds(final Collection<String> selectedNodeIds) {
		final Set<String> newSelectedNodeIds = new HashSet<>(selectedNodeIds);
		newSelectedNodeIds.removeAll(slide.getSelectedNodeIds());
		
		if(newSelectedNodeIds.isEmpty())
			return;
		IActor actor = new IActor() {
			
			@Override
			public String getDescription() {
				return "addSelectedNodeIds";
			}
			
			@Override
			public void act() {
				slide.addSelectedNodeIds(newSelectedNodeIds);
			}
			
			@Override
			public void undo() {
				slide.removeSelectedNodeIds(newSelectedNodeIds);
			}
			
		};
		controller.execute(actor, mapModel);
	}
	
	public void removeSelectedNodeIds(final Collection<String> selectedNodeIds) {
		final Set<String> removedNodeIds = new HashSet<>(selectedNodeIds);
		removedNodeIds.retainAll(slide.getSelectedNodeIds());
		
		if(removedNodeIds.isEmpty())
			return;
		IActor actor = new IActor() {
			
			@Override
			public String getDescription() {
				return "removeSelectedNodeIds";
			}
			
			@Override
			public void act() {
				slide.removeSelectedNodeIds(removedNodeIds);
			}
			
			@Override
			public void undo() {
				slide.addSelectedNodeIds(removedNodeIds);
			}
			
		};
		controller.execute(actor, mapModel);
	}
	
	
	public void setCentersSelectedNode(final boolean centersSelectedNode) {
		final boolean oldCentersSelectedNode = slide.centersSelectedNode();
		if(centersSelectedNode == oldCentersSelectedNode)
			return;
		IActor actor = new IActor() {
			
			@Override
			public String getDescription() {
				return "setCentersSelectedNode";
			}
			
			@Override
			public void act() {
				slide.setCentersSelectedNode(centersSelectedNode);
			}
			
			@Override
			public void undo() {
				slide.setCentersSelectedNode(oldCentersSelectedNode);
			}
			
		};
		controller.execute(actor, mapModel);
	}
	public void setChangesZoom(final boolean changeZoom) {
		final boolean oldChangesZoom = slide.changesZoom();
		if(changeZoom == oldChangesZoom)
			return;
		IActor actor = new IActor() {
			
			@Override
			public String getDescription() {
				return "setChangesZoom";
			}
			
			@Override
			public void act() {
				slide.setChangesZoom(changeZoom);
			}
			
			@Override
			public void undo() {
				slide.setChangesZoom(oldChangesZoom);
			}
			
		};
		controller.execute(actor, mapModel);
	}
	
	public void setZoom(final float zoom) {
		final float oldZoom = slide.getZoom();
		if(zoom == oldZoom)
			return;
		IActor actor = new IActor() {
			
			@Override
			public String getDescription() {
				return "setChangesZoom";
			}
			
			@Override
			public void act() {
				slide.setZoom(zoom);
			}
			
			@Override
			public void undo() {
				slide.setZoom(oldZoom);
			}
			
		};
		controller.execute(actor, mapModel);
	}
	
	public void setShowsOnlySpecificNodes(final boolean showOnlySpecificNodes) {
		final boolean oldShowsOnlySpecificNodes = slide.showsOnlySpecificNodes();
		if(showOnlySpecificNodes == oldShowsOnlySpecificNodes)
			return;
		IActor actor = new IActor() {
			
			@Override
			public String getDescription() {
				return "setShowsOnlySpecificNodes";
			}
			
			@Override
			public void act() {
				slide.setShowsOnlySpecificNodes(showOnlySpecificNodes);
			}
			
			@Override
			public void undo() {
				slide.setShowsOnlySpecificNodes(oldShowsOnlySpecificNodes);
			}
			
		};
		controller.execute(actor, mapModel);
	}
	
	public void setShowsAncestors(final boolean showAncestors) {
		final boolean oldShowsAncestors = slide.showsAncestors();
		if(showAncestors == oldShowsAncestors)
			return;
		IActor actor = new IActor() {
			
			@Override
			public String getDescription() {
				return "setShowsAncestors";
			}
			
			@Override
			public void act() {
				slide.setShowsAncestors(showAncestors);
			}
			
			@Override
			public void undo() {
				slide.setShowsAncestors(oldShowsAncestors);
			}
			
		};
		controller.execute(actor, mapModel);
	}
	
	public void setShowsDescendants(final boolean showDescendants) {
		final boolean oldShowsDescendants = slide.showsDescendants();
		if(showDescendants == oldShowsDescendants)
			return;
		IActor actor = new IActor() {
			
			@Override
			public String getDescription() {
				return "setShowsDescendants";
			}
			
			@Override
			public void act() {
				slide.setShowsDescendants(showDescendants);
			}
			
			@Override
			public void undo() {
				slide.setShowsDescendants(oldShowsDescendants);
			}
			
		};
		controller.execute(actor, mapModel);
	}
	
	public void setFilterCondition(final ASelectableCondition filterCondition) {
		final ASelectableCondition oldFilterCondition = slide.getFilterCondition();
		if(filterCondition == oldFilterCondition)
			return;
		IActor actor = new IActor() {
			
			@Override
			public String getDescription() {
				return "setFilterCondition";
			}
			
			@Override
			public void act() {
				slide.setFilterCondition(filterCondition);
			}
			
			@Override
			public void undo() {
				slide.setFilterCondition(oldFilterCondition);
			}
			
		};
		controller.execute(actor, mapModel);
	}

	public void setCurrentFoldedNodeIDs() {
		final Collection<String> currentFoldedNodeIds = slide.getCurrentFoldedNodeIds();
		final Set<String> oldFoldedNodeIds = slide.getFoldedNodeIds();
		final boolean foldedNodes = slide.foldsNodes();
		if(currentFoldedNodeIds == oldFoldedNodeIds && foldedNodes)
			return;
		IActor actor = new IActor() {
			
			@Override
			public String getDescription() {
				return "setCurrentFoldedNodeIDs";
			}
			
			@Override
			public void act() {
				slide.setFoldedNodeIDs(currentFoldedNodeIds);
			}
			
			@Override
			public void undo() {
				if(foldedNodes)
					slide.setFoldedNodeIDs(oldFoldedNodeIds);
				else
					slide.unsetFoldsNodes();
			}
			
		};
		controller.execute(actor, mapModel);
	}

	public void unsetFoldsNodes() {
		final boolean foldedNodes = slide.foldsNodes();
		if(! foldedNodes)
			return;
		final Set<String> oldFoldedNodeIds = slide.getFoldedNodeIds();
		IActor actor = new IActor() {
			
			@Override
			public String getDescription() {
				return "unsetFoldsNodes";
			}
			
			@Override
			public void act() {
				slide.unsetFoldsNodes();
			}
			
			@Override
			public void undo() {
				slide.setFoldedNodeIDs(oldFoldedNodeIds);
			}
			
		};
		controller.execute(actor, mapModel);
	}

}
