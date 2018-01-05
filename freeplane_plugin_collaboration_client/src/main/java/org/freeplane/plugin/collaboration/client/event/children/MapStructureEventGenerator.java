package org.freeplane.plugin.collaboration.client.event.children;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.batch.UpdateBlockGeneratorFactory;
import org.freeplane.plugin.collaboration.client.event.batch.Updates;
import org.freeplane.plugin.collaboration.client.event.content.ContentUpdateGenerators;

public class MapStructureEventGenerator{
	final private UpdateBlockGeneratorFactory updates;
	final private ContentUpdateGenerators contentUpdateGenerators;

	public MapStructureEventGenerator(UpdateBlockGeneratorFactory updateBlockGeneratorFactory, ContentUpdateGenerators contentUpdateGenerators) {
		this.updates = updateBlockGeneratorFactory;
		this.contentUpdateGenerators = contentUpdateGenerators;
	}
	
	public void onNewMap(MapModel map) {
		final Updates updates = this.updates.of(map);
		updates.addUpdateEvents("map", () -> 
			updates.addUpdateEvent(createRootNodeIdUpdatedEvent(map)));
		contentUpdateGenerators.onNewMap(map);
		generateEventsForSubtree(map.getRootNode());
	}

	public void onNodeInserted(NodeModel node) {
		generateNodeInsertedEvent(node);
		generateEventsForSubtree(node);
	}

	public void onNodeMoved(NodeModel node) {
		String nodeId = node.createID();
		final Updates updates = this.updates.of(node);
		updates.addUpdateEvents(() -> {
			updates.addUpdateEvents(NodeMoved.builder()
				.nodeId(nodeId)
				.position(nodePositionOf(node)).build());
		});
	}

	public void onNodeRemoved(NodeModel child) {
		String nodeId = child.createID();
		final Updates updates = this.updates.of(child);
		updates.addUpdateEvents(() -> {
			updates.addUpdateEvents(NodeRemoved.builder()
				.nodeId(nodeId).build());
		});
	}

	public ImmutableNodePosition nodePositionOf(NodeModel node) {
		NodeModel parent = node.getParentNode();
		ImmutableNodePosition.Builder builder = NodePosition.builder()
				.parentId(parent.createID())
				.position(node.getIndex());
		if(parent.isRoot())
			builder.side(Side.of(node));
		ImmutableNodePosition position = builder.build();
		return position;
	}

	
	private RootNodeIdUpdated createRootNodeIdUpdatedEvent(MapModel map) {
		return RootNodeIdUpdated.builder().nodeId(map.getRootNode().getID()).build();
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

	private void generateNodeInsertedEvent(final NodeModel node) {
		String nodeId = node.createID();
		final Updates updates = this.updates.of(node);
		updates.addUpdateEvents(() -> {
			updates.addUpdateEvents(NodeInserted.builder()
				.nodeId(nodeId)
				.position(nodePositionOf(node)).build());
		});
	}

	private void generateContentUpdateEvents(final NodeModel node) {
		contentUpdateGenerators.onNewNode(node);
	}


	private void generateSpecialTypeEvent(final NodeModel node) {
		SpecialNodeTypeSet.SpecialNodeType.of(node).ifPresent((c) -> {
			final Updates updates = this.updates.of(node);
			updates.addUpdateEvent(SpecialNodeTypeSet.builder().nodeId(node.createID()).content(c).build());
		});
	}



}