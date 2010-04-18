package org.freeplane.features.common.filter;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.text.TextController;
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
	public void actionPerformed(ActionEvent e) {
		FilterController filterController = FilterController.getController(getController());
		NodeModel start = getController().getSelection().getSelected();
		NodeModel next = filterController.findNext(start, null, direction, null);
		getModeController().getMapController().select(next);
	}
}
