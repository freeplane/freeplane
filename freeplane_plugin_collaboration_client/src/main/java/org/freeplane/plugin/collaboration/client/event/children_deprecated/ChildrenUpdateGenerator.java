package org.freeplane.plugin.collaboration.client.event.children_deprecated;

import java.util.LinkedHashSet;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.batch.Updates;
import org.freeplane.plugin.collaboration.client.event.content.ContentUpdateGenerators;

public class ChildrenUpdateGenerator implements IExtension{
	final private Updates updates;
	final private StructureUpdateEventFactory structuralEventFactory;
	final private ContentUpdateGenerators contentUpdateGenerators;
	final private LinkedHashSet<NodeModel> changedParents;
	final private LinkedHashSet<NodeModel> insertedChildren;

	public ChildrenUpdateGenerator(Updates updates, StructureUpdateEventFactory eventFactory,
	                               ContentUpdateGenerators contentUpdateGenerators) {
		this.updates = updates;
		this.structuralEventFactory = eventFactory;
		this.contentUpdateGenerators = contentUpdateGenerators;
		changedParents = new LinkedHashSet<>();
		insertedChildren = new LinkedHashSet<>();
	}
	
	public void onNewMap(MapModel map) {
		updates.addUpdateEvents("map", () -> 
			{
				updates.addUpdateEvent(createRootNodeIdUpdatedEvent(map));
				contentUpdateGenerators.onNewMap(map);
				generateEventsForSubtree(map.getRootNode());
			});
	}

	public void onNodeInserted(NodeModel parent, NodeModel child) {
		onChangedStructure(parent);
		insertedChildren.add(child);
	}

	public void onChangedStructure(NodeModel parent) {
		if(changedParents.isEmpty())
			updates.addUpdateEvents(parent.createID(), this::generateStructureChangedEvents);
		changedParents.add(parent);
	}

	
	private RootNodeIdUpdated createRootNodeIdUpdatedEvent(MapModel map) {
		return RootNodeIdUpdated.builder().nodeId(map.getRootNode().getID()).build();
	}


	private void generateStructureChangedEvents() {
		for(NodeModel parent : changedParents) {
			final ChildrenUpdated childrenUpdated = structuralEventFactory.createChildrenUpdatedEvent(parent);
			updates.addUpdateEvent(childrenUpdated);
			for(NodeModel child : parent.getChildren()) {
				if(insertedChildren.contains(child))
					generateEventsForSubtree(child);
			}
		}
		insertedChildren.clear();
		changedParents.clear();
	}


	private void generateEventsForSubtree(NodeModel node) {
		generateStructureChangedEventsForSubtree(node);
	}

	private void generateStructureChangedEventsForSubtree(final NodeModel node) {
		generateSpecialTypeEvent(node);
		generateContentUpdateEvents(node);
		generateChildrenUpdateEvent(node);
		if(node.hasChildren()) {
			for(NodeModel child : node.getChildren()) {
				generateStructureChangedEventsForSubtree(child);
			}
		}
	}

	private void generateContentUpdateEvents(final NodeModel node) {
		contentUpdateGenerators.onNewNode(node);
	}

	private void generateChildrenUpdateEvent(final NodeModel node) {
		if(node.hasChildren()) {
			final ChildrenUpdated childrenUpdated = structuralEventFactory.createChildrenUpdatedEvent(node);
			updates.addUpdateEvent(childrenUpdated);
		}
	}

	private void generateSpecialTypeEvent(final NodeModel node) {
		SpecialNodeTypeSet.SpecialNodeType.of(node).ifPresent((c) -> {
			updates.addUpdateEvent(SpecialNodeTypeSet.builder().nodeId(node.createID()).content(c).build());
		});
	}



}