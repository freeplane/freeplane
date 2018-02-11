package org.freeplane.plugin.collaboration.client.event.children;

import java.util.Optional;

import org.freeplane.collaboration.event.children.SpecialNodeTypeSet.SpecialNodeType;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.plugin.collaboration.client.event.batch.UpdatesAccessor;
import org.freeplane.plugin.collaboration.client.event.batch.Updates;
import org.freeplane.plugin.collaboration.client.event.content.ContentUpdateGenerators;

public class MapStructureEventGenerator {
	final private UpdatesAccessor updates;
	final private ContentUpdateGenerators contentUpdateGenerators;
	final private MapStructureEventFactory mapStructureEventFactory;

	public MapStructureEventGenerator(UpdatesAccessor updates,
	                                  ContentUpdateGenerators contentUpdateGenerators) {
		this(updates, contentUpdateGenerators, new MapStructureEventFactory());
	}

	MapStructureEventGenerator(UpdatesAccessor updates,
	                           ContentUpdateGenerators contentUpdateGenerators,
	                           final MapStructureEventFactory mapStructureEventFactory) {
		this.updates = updates;
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
		updates.addUpdateEvent(() -> mapStructureEventFactory.createNodeRemovedEvent(child));
	}

	private void generateNodeInsertedEvent(final NodeModel node) {
		final Updates updates = this.updates.of(node);
		updates.addUpdateEvent(() -> mapStructureEventFactory.createNodeInsertedEvent(node));
	}

	private void generateEventsForSubtree(final NodeModel node) {
		generateSpecialTypeEvent(node);
		generateContentUpdateEvents(node);
		if (node.hasChildren()) {
			for (NodeModel child : node.getChildren()) {
				generateNodeInsertedEvent(child);
				generateEventsForSubtree(child);
			}
		}
	}

	private void generateContentUpdateEvents(final NodeModel node) {
		contentUpdateGenerators.onNewNode(node);
	}

	private void generateSpecialTypeEvent(final NodeModel node) {
		specialNodeTypeOf(node).ifPresent(c -> {
			final Updates updates = this.updates.of(node);
			updates.addUpdateEvent(() -> mapStructureEventFactory.createSpecialNodeTypeSetEvent(node, c));
		});
	}

	private Optional<SpecialNodeType> specialNodeTypeOf(NodeModel node) {
		final Optional<SpecialNodeType> content;
		final boolean isFirstGroupNode = SummaryNode.isFirstGroupNode(node);
		final boolean isSummaryNode = SummaryNode.isSummaryNode(node);
		if (isSummaryNode && isFirstGroupNode)
			content = Optional.of(SpecialNodeType.SUMMARY_BEGIN_END);
		else if (isFirstGroupNode)
			content = Optional.of(SpecialNodeType.SUMMARY_BEGIN);
		else if (isSummaryNode)
			content = Optional.of(SpecialNodeType.SUMMARY_END);
		else
			content = Optional.empty();
		return content;
	}
}
