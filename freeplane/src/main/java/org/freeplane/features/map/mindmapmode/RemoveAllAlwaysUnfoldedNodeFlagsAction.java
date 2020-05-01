package org.freeplane.features.map.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.AlwaysUnfoldedNode;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

@SuppressWarnings("serial")
public class RemoveAllAlwaysUnfoldedNodeFlagsAction extends AFreeplaneAction {

	private AlwaysUnfoldedNode flagController;

	public RemoveAllAlwaysUnfoldedNodeFlagsAction(AlwaysUnfoldedNode alwaysUnfoldedNode) {
		super("RemoveAllAlwaysUnfoldedNodeFlagsAction");
		this.flagController = alwaysUnfoldedNode;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final NodeModel rootNode = Controller.getCurrentController().getMap().getRootNode();
		final NodeModel node = rootNode;
		removeAlwaysUnfoldedNodeFlags(node);
	}

	private void removeAlwaysUnfoldedNodeFlags(final NodeModel node) {
		if(! node.isFolded() && ! node.isRoot() && node.hasVisibleContent(Controller.getCurrentController().getSelection().getFilter())){
			flagController.undoableDeactivateHook(node);
		}
		for(NodeModel child : node.getChildren())
			removeAlwaysUnfoldedNodeFlags(child);
	}

}
