package org.freeplane.features.filter;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.HideChildSubtree;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.MapController.Direction;
import org.freeplane.features.mode.Controller;

public class NextPresentationItemAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NextPresentationItemAction() {
		super("NextPresentationItemAction");
	}

	/**
	 * 
	 */
	public void actionPerformed(final ActionEvent e) {
		final FilterController filterController = FilterController.getCurrentFilterController();
		final NodeModel start = Controller.getCurrentController().getSelection().getSelected();
		final NodeModel next = filterController.findNext(start, null, Direction.FORWARD_N_FOLD, null);
		if(next != null){
			final MapController mapController = Controller.getCurrentModeController().getMapController();
			if (!next.isVisible()) {
            	next.getFilterInfo().reset();
            	mapController.nodeRefresh(next);
            }
            final NodeModel[] path = next.getPathToRoot();
            for (int i = 1; i < path.length; i++) {
            	final NodeModel nodeOnPath = path[i];
            	final NodeModel parentNode = nodeOnPath.getParentNode();
            	while(parentNode.isFolded() || nodeOnPath.containsExtension(HideChildSubtree.class))
            		mapController.showNextChild(parentNode);
            }
            Controller.getCurrentController().getSelection().selectAsTheOnlyOneSelected(next);
		}
	}
}
