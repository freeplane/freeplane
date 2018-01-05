package org.freeplane.plugin.collaboration.client.event.children_deprecated;

import org.freeplane.features.map.FirstGroupNodeFlag;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNodeFlag;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessor;
import org.freeplane.plugin.collaboration.client.event.children_deprecated.SpecialNodeTypeSet.SpecialNodeType;

public class SpecialNodeTypeProcessor implements UpdateProcessor<SpecialNodeTypeSet> {

	@Override
	public void onUpdate(MapModel map, SpecialNodeTypeSet event) {
		NodeModel node = map.getNodeForID(event.nodeId());
		SpecialNodeType type = event.content();
		if(type == SpecialNodeType.SUMMARY_BEGIN || type == SpecialNodeType.SUMMARY_BEGIN_END)
			node.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
		if(type == SpecialNodeType.SUMMARY_END || type == SpecialNodeType.SUMMARY_BEGIN_END)
			node.addExtension(SummaryNodeFlag.SUMMARY);
		
	}

	@Override
	public Class<SpecialNodeTypeSet> eventClass() {
		return SpecialNodeTypeSet.class;
	}
}
