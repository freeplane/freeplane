package org.freeplane.plugin.collaboration.client.event.children;

import java.awt.event.ActionEvent;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimer;
import org.freeplane.plugin.collaboration.client.event.children.ImmutableRootNodeIdUpdated.Builder;
import org.freeplane.plugin.collaboration.client.event.children.SpecialNodeTypeSet.SpecialNodeType;

public class ChildrenUpdateGenerator implements IExtension{
	final private MapUpdateTimer timer;
	final private UpdateEventFactory eventFactory;
	final private LinkedHashSet<NodeModel> changedParents;
	final private LinkedHashSet<NodeModel> insertedChildren;
	final private LinkedHashMap<NodeModel, SpecialNodeType> specialNodes;

	public ChildrenUpdateGenerator(MapUpdateTimer timer, UpdateEventFactory eventFactory) {
		this.timer = timer;
		this.eventFactory = eventFactory;
		changedParents = new LinkedHashSet<>();
		insertedChildren = new LinkedHashSet<>();
		specialNodes = new LinkedHashMap<>();
	}
	
	public void onNewMap(MapModel map) {
		timer.addActionListener(e -> 
			timer.addUpdateEvents(createRootNodeIdUpdatedEvent(map)));
		timer.restart();
	}

	private RootNodeIdUpdated createRootNodeIdUpdatedEvent(MapModel map) {
		return RootNodeIdUpdated.builder().nodeId(map.getRootNode().getID()).build();
	}


	public void onNodeInserted(NodeModel parent, NodeModel child) {
		onChangedStructure(parent);
		insertedChildren.add(child);
	}

	public void onChangedStructure(NodeModel parent) {
		if(changedParents.isEmpty())
			timer.addActionListener(this::generateStructureChangedEvent);
		changedParents.add(parent);
		timer.restart();
	}

	
	private void generateSpecialNodeTypeSetEvent(ActionEvent e) {
		for( Entry<NodeModel, SpecialNodeType> nodeTypePair : specialNodes.entrySet()) {
			timer.addUpdateEvents(SpecialNodeTypeSet.builder()
					.nodeId(nodeTypePair.getKey().createID())
					.content(nodeTypePair.getValue()).build());
		}
		specialNodes.clear();
	}
	
	private void generateStructureChangedEvent(ActionEvent e) {
		for(NodeModel parent : changedParents) {
			final ChildrenUpdated childrenUpdated = eventFactory.createChildrenUpdatedEvent(parent);
			timer.addUpdateEvents(childrenUpdated);
			for(NodeModel child : parent.getChildren()) {
				if(insertedChildren.contains(child))
					generateStructureChangedEventForSubtree(child);
			}
		}
		insertedChildren.clear();
		changedParents.clear();
	}


	private void generateStructureChangedEventForSubtree(NodeModel node) {
		SpecialNodeTypeSet.SpecialNodeType.of(node).ifPresent(
			t -> {
				if(specialNodes.isEmpty())
					timer.addActionListener(this::generateSpecialNodeTypeSetEvent);
				specialNodes.put(node, t);
			}
		);

		if(node.getParentNode() != null && node.hasChildren()) {
			final ChildrenUpdated childrenUpdated = eventFactory.createChildrenUpdatedEvent(node);
			timer.addUpdateEvents(childrenUpdated);
			for(NodeModel parent1 : node.getChildren()) {
				generateStructureChangedEventForSubtree(parent1);
			}
		}
	}
	

}