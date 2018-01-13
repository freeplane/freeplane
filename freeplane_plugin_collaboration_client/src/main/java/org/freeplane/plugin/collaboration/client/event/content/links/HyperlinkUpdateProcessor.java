package org.freeplane.plugin.collaboration.client.event.content.links;

import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessor;

public class HyperlinkUpdateProcessor implements UpdateProcessor<HyperlinkUpdated> {
	private final MLinkController linkController;

	public HyperlinkUpdateProcessor(MLinkController linkController) {
		this.linkController = linkController;
	}

	@Override
	public void onUpdate(MapModel map, HyperlinkUpdated event) {
		NodeModel node = map.getNodeForID(event.nodeId());
		linkController.setLink(node, event.uri().orElse(null), LinkController.LINK_ABSOLUTE);
	}

	@Override
	public Class<HyperlinkUpdated> eventClass() {
		return HyperlinkUpdated.class;
	}
}
