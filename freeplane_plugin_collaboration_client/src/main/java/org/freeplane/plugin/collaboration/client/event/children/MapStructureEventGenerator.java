package org.freeplane.plugin.collaboration.client.event.children;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.batch.UpdateBlockGeneratorFactory;
import org.freeplane.plugin.collaboration.client.event.batch.Updates;
import org.freeplane.plugin.collaboration.client.event.content.ContentUpdateGenerators;

public class MapStructureEventGenerator{
	final private UpdateBlockGeneratorFactory updates;
	final private ContentUpdateGenerators contentUpdateGenerators;
	final private MapStructureEventFactory mapStructureEventFactory;
	public MapStructureEventGenerator(UpdateBlockGeneratorFactory updateBlockGeneratorFactory, 
	                                  ContentUpdateGenerators contentUpdateGenerators) {
		this(updateBlockGeneratorFactory, contentUpdateGenerators, new MapStructureEventFactory());	
	}

	MapStructureEventGenerator(UpdateBlockGeneratorFactory updateBlockGeneratorFactory, 
		ContentUpdateGenerators contentUpdateGenerators,
		final MapStructureEventFactory mapStructureEventFactory) {
		this.updates = updateBlockGeneratorFactory;
		this.contentUpdateGenerators = contentUpdateGenerators;
		this.mapStructureEventFactory = mapStructureEventFactory;
	}

	public void onNewMap(MapModel map) {
		final Updates updates = this.updates.of(map);
		updates.addUpdateEvent("map", 
			() -> mapStructureEventFactory.createRootNodeIdUpdatedEvent(map));
		contentUpdateGenerators.onNewMap(map);
		generateEventsForSubtree(map.getRootNode());
	}

	public void onNodeInserted(NodeModel node) {
		generateNodeInsertedEvent(node);
		generateEventsForSubtree(node);
	}

	public void onNodeMoved(NodeModel node) {
		final Updates updates = this.updates.of(node);
		updates.addUpdateEvent(() -> mapStructureEventFactory.createNodeMovedEvent(node));
	}

	public void onNodeRemoved(NodeModel child) {
		final Updates updates = this.updates.of(child);
		updates.addUpdateEvent(() -> 
		mapStructureEventFactory.createNodeRemovedEvent(child)
				);
	}

	private void generateNodeInsertedEvent(final NodeModel node) {
		final Updates updates = this.updates.of(node);
		updates.addUpdateEvent(() -> mapStructureEventFactory.createNodeInsertedEvent(node));
	}


	private void generateEventsForSubtree(final NodeModel node) {
		generateSpecialTypeEvent(node);
		generateContentUpdateEvents(node);
		if(node.hasChildren()) {
			for(NodeModel child : node.getChildren()) {
				generateNodeInsertedEvent(child);
				generateEventsForSubtree(child);
			}
		}
	}

	private void generateContentUpdateEvents(final NodeModel node) {
		contentUpdateGenerators.onNewNode(node);
	}


	private void generateSpecialTypeEvent(final NodeModel node) {
		SpecialNodeTypeSet.SpecialNodeType.of(node).ifPresent(c -> {
			final Updates updates = this.updates.of(node);
			updates.addUpdateEvent(() -> mapStructureEventFactory.createSpecialNodeTypeSetEvent(node, c));
		});
	}

}