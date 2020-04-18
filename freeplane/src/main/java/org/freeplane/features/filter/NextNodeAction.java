package org.freeplane.features.filter;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapController.Direction;
import org.freeplane.features.mode.Controller;

public class NextNodeAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Direction direction;

	public NextNodeAction( final Direction direction) {
		super("NextNodeAction." + direction.toString());
		this.direction = direction;
	}

	/**
	 * 
	 */
	public void actionPerformed(final ActionEvent e) {
		final FilterController filterController = FilterController.getCurrentFilterController();
		IMapSelection selection = Controller.getCurrentController().getSelection();
        final NodeModel start = selection.getSelected();
		final NodeModel next = filterController.findNext(start, null, direction, null, selection.getFilter());
		if(next != null){
			Controller.getCurrentModeController().getMapController().select(next);
		}
	}
}
