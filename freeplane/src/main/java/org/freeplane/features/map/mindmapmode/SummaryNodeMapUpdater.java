package org.freeplane.features.map.mindmapmode;

import org.freeplane.features.map.FirstGroupNodeFlag;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryLevels;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.features.map.SummaryNodeFlag;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.url.MapVersionInterpreter;

public class SummaryNodeMapUpdater implements IMapLifeCycleListener {
	final private MMapController mapController;
	final private MModeController modeController;
	public SummaryNodeMapUpdater(MModeController modeController,  MMapController mapController){
		this.modeController = modeController;
		this.mapController = mapController;

	}
	@Override
	public void onCreate(MapModel map) {
		final MapVersionInterpreter mapXmlVersionInterpreter = map.getExtension(MapVersionInterpreter.class);
		if(mapXmlVersionInterpreter == null || mapXmlVersionInterpreter.version < 6){
			modeController.deactivateUndo((MMapModel) map);
			updateSummaryNodes(map.getRootNode());
		}
	}
	private void updateSummaryNodes(NodeModel parentNode) {
		final NodeModel[] nodes = parentNode.getChildren().toArray(new NodeModel[]{});
		for(NodeModel node : nodes){
			SummaryLevels summaryLevels = null;
			if(SummaryNode.isFirstGroupNode(node)){
				if(summaryLevels == null)
					summaryLevels = new SummaryLevels(parentNode);
				if (summaryLevels.findSummaryNode(node.getIndex()) == null)
					node.removeExtension(FirstGroupNodeFlag.class);
			}
			if(SummaryNode.isSummaryNode(node)) {
				if(summaryLevels == null)
					summaryLevels = new SummaryLevels(parentNode);
				final NodeModel groupBeginNode = summaryLevels.findGroupBeginNode(parentNode.previousNodeIndex(node.getIndex(), node.isLeft()));
				if(groupBeginNode == null)
					node.removeExtension(SummaryNodeFlag.class);
				else {
					if (! groupBeginNode.containsExtension(FirstGroupNodeFlag.class)) {
						if(SummaryNode.isSummaryNode(groupBeginNode))
							groupBeginNode.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
						else {
							final NodeModel newFirstGroupNode = mapController.addNewNode(groupBeginNode.getParentNode(), groupBeginNode.getIndex(), groupBeginNode.isLeft());
							newFirstGroupNode.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
						}
					}
					if (node.isFolded() || !node.hasChildren() || !node.getText().isEmpty()){
						node.removeExtension(SummaryNodeFlag.class);
						final NodeModel newParent = mapController.addNewNode(node.getParentNode(), node.getIndex(), node.isLeft());
						newParent.addExtension(SummaryNodeFlag.SUMMARY);
						if(SummaryNode.isFirstGroupNode(node)){
							node.removeExtension(FirstGroupNodeFlag.class);
							newParent.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
						}
						mapController.moveNodeAndItsClones(node, newParent, 0, false, false);
					}
				}
			}
			else if(SummaryNode.isFirstGroupNode(node) && (node.hasChildren() || !node.getText().isEmpty())){
				final NodeModel newFirstGroupNode = mapController.addNewNode(node.getParentNode(), node.getIndex(), node.isLeft());
				node.removeExtension(FirstGroupNodeFlag.class);
				newFirstGroupNode.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
			}
			updateSummaryNodes(node);
		}
	}
}