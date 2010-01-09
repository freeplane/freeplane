package org.freeplane.features.mindmapmode.nodestyle;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.features.common.addins.styles.LogicalStyleKeys;

class RemoveFormatAction extends AMultipleNodeAction {

	public RemoveFormatAction(Controller controller) {
		super("RemoveFormatAction", controller);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void actionPerformed(ActionEvent e, NodeModel node) {
		getModeController().undoableRemoveExtensions(LogicalStyleKeys.NODE_STYLE, node, node);
	}

}
