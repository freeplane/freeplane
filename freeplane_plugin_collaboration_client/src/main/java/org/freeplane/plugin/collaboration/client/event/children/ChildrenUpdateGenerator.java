package org.freeplane.plugin.collaboration.client.event.children;

import java.util.LinkedHashSet;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.batch.Updates;
import org.freeplane.plugin.collaboration.client.event.content.ContentUpdateEventFactory;

public class ChildrenUpdateGenerator implements IExtension{
	final private Updates updates;
	final private StructureUpdateEventFactory structuralEventFactory;
	final private ContentUpdateEventFactory contentUpdateEventFactory;
	final private LinkedHashSet<NodeModel> changedParents;
	final private LinkedHashSet<NodeModel> insertedChildren;

	public ChildrenUpdateGenerator(Updates updates, StructureUpdateEventFactory eventFactory,
	                               ContentUpdateEventFactory contentUpdateEventFactory) {
		this.updates = updates;
		this.structuralEventFactory = eventFactory;
		this.contentUpdateEventFactory = contentUpdateEventFactory;
		changedParents = new LinkedHashSet<>();
		insertedChildren = new LinkedHashSet<>();
	}
	
	public void onNewMap(MapModel map) {
		updates.addUpdateEvents(() -> 
			{
				updates.addUpdateEvents(createRootNodeIdUpdatedEvent(map),
					contentUpdateEventFactory.createMapContentUpdatedEvent(map));
				generateStructureChangedEventForSubtree(map.getRootNode());
			});
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
			updates.addUpdateEvents(this::generateStructureChangedEvent);
		changedParents.add(parent);
	}

	
	private void generateStructureChangedEvent() {
		for(NodeModel parent : changedParents) {
			final ChildrenUpdated childrenUpdated = structuralEventFactory.createChildrenUpdatedEvent(parent);
			updates.addUpdateEvent(childrenUpdated);
			for(NodeModel child : parent.getChildren()) {
				if(insertedChildren.contains(child))
					generateStructureChangedEventForSubtree(child);
			}
		}
		insertedChildren.clear();
		changedParents.clear();
	}


	private void generateStructureChangedEventForSubtree(final NodeModel node) {
		SpecialNodeTypeSet.SpecialNodeType.of(node).ifPresent((c) -> {
			updates.addUpdateEvent(SpecialNodeTypeSet.builder().nodeId(node.createID()).content(c).build());
		});
		updates.addUpdateEvent(contentUpdateEventFactory.createNodeContentUpdatedEvent(node));
		if(node.hasChildren()) {
			final ChildrenUpdated childrenUpdated = structuralEventFactory.createChildrenUpdatedEvent(node);
			updates.addUpdateEvent(childrenUpdated);
			for(NodeModel child : node.getChildren()) {
				generateStructureChangedEventForSubtree(child);
			}
		}
	}
	

}