package org.freeplane.features.common.filter;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.text.TextController.Direction;

public class NextNodeAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Direction direction;

	public NextNodeAction(final Controller controller, final Direction direction) {
		super("NextNodeAction." + direction.toString(), controller);
		this.direction = direction;
	}

	/**
	 * 
	 */
	public void actionPerformed(final ActionEvent e) {
		final FilterController filterController = FilterController.getController(getController());
		final NodeModel start = getController().getSelection().getSelected();
		final NodeModel next = filterController.findNext(start, null, direction, null);
		if(next != null){
			getModeController().getMapController().select(next);
		}
	}
}
