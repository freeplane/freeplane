package org.freeplane.features.map.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.List;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

@SuppressWarnings("serial")
@EnabledAction(checkOnNodeChange = true)
public class ConvertCloneToIndependentNodeAction extends AFreeplaneAction {

	public ConvertCloneToIndependentNodeAction() {
		super("ConvertCloneToIndependentNodeAction");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
		final Collection<NodeModel> selectedNodes = Controller.getCurrentController().getSelection().getSelection();
		for(final NodeModel node :selectedNodes)
			mapController.convertClonesToIndependentNodes(node);
	}
	
	@Override
	public void setEnabled() {
		final Collection<NodeModel> selectedNodes = Controller.getCurrentController().getSelection().getSelection();
		for(NodeModel node :selectedNodes)
			if(! node.isCloneTreeRoot()){
				setEnabled(false);
				return;
			}
		setEnabled(true);
	}

}
