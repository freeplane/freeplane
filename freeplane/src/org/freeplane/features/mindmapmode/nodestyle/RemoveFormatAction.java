package org.freeplane.features.mindmapmode.nodestyle;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.styles.LogicalStyleKeys;

class RemoveFormatAction extends AMultipleNodeAction {
	public RemoveFormatAction(final Controller controller) {
		super("RemoveFormatAction");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		getModeController().undoableRemoveExtensions(LogicalStyleKeys.NODE_STYLE, node, node);
	}
}
