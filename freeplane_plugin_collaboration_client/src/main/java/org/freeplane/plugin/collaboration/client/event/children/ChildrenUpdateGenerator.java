package org.freeplane.plugin.collaboration.client.event.children;

import java.awt.event.ActionEvent;
import java.util.LinkedHashSet;
import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimer;
import org.freeplane.plugin.collaboration.client.event.content.ContentUpdateEventFactory;

public class ChildrenUpdateGenerator implements IExtension{
	final private MapUpdateTimer timer;
	final private StructureUpdateEventFactory structuralEventFactory;
	final private ContentUpdateEventFactory contentUpdateEventFactory;
	final private LinkedHashSet<NodeModel> changedParents;
	final private LinkedHashSet<NodeModel> insertedChildren;

	public ChildrenUpdateGenerator(MapUpdateTimer timer, StructureUpdateEventFactory eventFactory,
	                               ContentUpdateEventFactory contentUpdateEventFactory) {
		this.timer = timer;
		this.structuralEventFactory = eventFactory;
		this.contentUpdateEventFactory = contentUpdateEventFactory;
		changedParents = new LinkedHashSet<>();
		insertedChildren = new LinkedHashSet<>();
	}
	
	public void onNewMap(MapModel map) {
		timer.addActionListener(e -> 
			{
				timer.addUpdateEvent(createRootNodeIdUpdatedEvent(map));
				generateStructureChangedEventForSubtree(map.getRootNode());
			});
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

	
	private void generateStructureChangedEvent(ActionEvent e) {
		for(NodeModel parent : changedParents) {
			final ChildrenUpdated childrenUpdated = structuralEventFactory.createChildrenUpdatedEvent(parent);
			timer.addUpdateEvent(childrenUpdated);
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
			timer.addUpdateEvent(SpecialNodeTypeSet.builder().nodeId(node.createID()).content(c).build());
		});
		timer.addUpdateEvent(contentUpdateEventFactory.createNodeContentUpdatedEvent(node));
		if(node.hasChildren()) {
			final ChildrenUpdated childrenUpdated = structuralEventFactory.createChildrenUpdatedEvent(node);
			timer.addUpdateEvent(childrenUpdated);
			for(NodeModel child : node.getChildren()) {
				generateStructureChangedEventForSubtree(child);
			}
		}
	}
	

}