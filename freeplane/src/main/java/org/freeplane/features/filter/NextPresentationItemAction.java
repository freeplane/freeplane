package org.freeplane.features.filter;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JComponent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.MapController.Direction;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.view.swing.map.NodeView;

public class NextPresentationItemAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Direction direction;

	static public NextPresentationItemAction createFoldingAction(){
		return new NextPresentationItemAction(Direction.FORWARD_N_FOLD, "NextPresentationItemAction");
	}
	
	static public NextPresentationItemAction createNotFoldingAction(){
		return new NextPresentationItemAction(Direction.FORWARD, "NotFoldingNextPresentationItemAction");
	}
	
	private NextPresentationItemAction(Direction direction, String key) {
		super(key);
		this.direction = direction;
	}

	/**
	 * 
	 */
	public void actionPerformed(final ActionEvent e) {
		final FilterController filterController = FilterController.getCurrentFilterController();
		final Controller controller = Controller.getCurrentController();
		final NodeModel start = controller.getSelection().getSelected();
		final IMapViewManager mapViewManager = controller.getMapViewManager();
		final NodeModel next = filterController.findNext(start, null, direction, null);
		if(next != null){
			final MapController mapController = Controller.getCurrentModeController().getMapController();
			if (!next.hasVisibleContent()) {
            	next.getFilterInfo().reset();
            	mapController.nodeRefresh(next);
            }
            final NodeModel[] path = next.getPathToRoot();
            for (int i = 1; i < path.length; i++) {
            	final NodeModel nodeOnPath = path[i];
            	final NodeModel parentNode = nodeOnPath.getParentNode();
            	if(parentNode.isFolded())
            		mapController.showNextChild(parentNode);
            	else {
            		if(mapViewManager.isChildHidden(nodeOnPath)) {
            			mapController.showNextChild(parentNode);
            	}
            	}
            }
            Controller.getCurrentController().getSelection().selectAsTheOnlyOneSelected(next);
		}
	}
}
