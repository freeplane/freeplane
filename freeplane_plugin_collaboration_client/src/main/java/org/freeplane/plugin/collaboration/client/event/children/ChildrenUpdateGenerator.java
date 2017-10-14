package org.freeplane.plugin.collaboration.client.event.children;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimer;
import org.freeplane.plugin.collaboration.client.event.children.SpecialNodeTypeSet.SpecialNodeType;

class ChildrenUpdateGenerator {
	final private MapUpdateTimer timer;
	final private UpdateEventFactory eventFactory;
	final private LinkedHashSet<NodeModel> changedParents;
	final private LinkedHashMap<NodeModel, SpecialNodeType> specialNodes;

	ChildrenUpdateGenerator(MapUpdateTimer timer, UpdateEventFactory eventFactory) {
		this.timer = timer;
		this.eventFactory = eventFactory;
		changedParents = new LinkedHashSet<>();
		specialNodes = new LinkedHashMap<>();
	}

	
	void onNodeInserted(NodeModel parent, NodeModel child) {
		onChangedStructure(parent);
		if(specialNodes.isEmpty())
			timer.addActionListener(e -> generateSpecialNodeTypeSetEvent());
		SpecialNodeTypeSet.SpecialNodeType.of(child).ifPresent(t -> specialNodes.put(child, t));
	}

	void onChangedStructure(NodeModel parent) {
		if(changedParents.isEmpty())
			timer.addActionListener(e -> generateStructureChangedEvent());
		changedParents.add(parent);
		timer.restart();
	}

	
	private void generateSpecialNodeTypeSetEvent() {
		for( Entry<NodeModel, SpecialNodeType> e : specialNodes.entrySet()) {
			timer.addUpdateEvents(SpecialNodeTypeSet.builder()
					.nodeId(e.getKey().createID())
					.content(e.getValue()).build());
		}
		specialNodes.clear();
	}
	
	private void generateStructureChangedEvent() {
		for(NodeModel parent : changedParents) {
			final ChildrenUpdated childrenUpdated = eventFactory.createChildrenUpdatedEvent(parent);
			timer.addUpdateEvents(childrenUpdated);
		}
		changedParents.clear();
	}
	

}