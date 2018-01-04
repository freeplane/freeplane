package org.freeplane.plugin.collaboration.client.event.content.other;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.NodeContentManipulator;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessor;

public class NodeContentUpdateProcessor implements UpdateProcessor<NodeContentUpdated> {

	private final NodeContentManipulator updater;

	public NodeContentUpdateProcessor(NodeContentManipulator updater) {
		super();
		this.updater = updater;
	}

	@Override
	public void onUpdate(MapModel map, NodeContentUpdated event) {
		updater.updateNodeContent(map.getNodeForID(event.nodeId()), event.content(), ContentUpdateGenerator.getNodeContentExclusions());
	}

	@Override
	public Class<NodeContentUpdated> eventClass() {
		return NodeContentUpdated.class;
	}
}
