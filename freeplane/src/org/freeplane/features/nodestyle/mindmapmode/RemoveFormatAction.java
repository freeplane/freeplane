package org.freeplane.features.nodestyle.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.LogicalStyleKeys;

class RemoveFormatAction extends AMultipleNodeAction {
	public RemoveFormatAction() {
		super("RemoveFormatAction");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		Controller.getCurrentModeController().undoableRemoveExtensions(LogicalStyleKeys.NODE_STYLE, node, node);
	}
}
