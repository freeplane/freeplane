package org.freeplane.features.mindmapmode.styles;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.styles.IStyle;
import org.freeplane.features.common.styles.LogicalStyleController;
import org.freeplane.features.common.styles.LogicalStyleKeys;
import org.freeplane.features.common.styles.MapStyleModel;

public class RedefineStyleAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	
	public RedefineStyleAction() {
		super("RedefineStyleAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel node = Controller.getCurrentController().getSelection().getSelected();
		final IStyle style = LogicalStyleController.getController().getFirstStyle(node);
		final MapStyleModel extension = MapStyleModel.getExtension(node.getMap());
		final NodeModel styleNode = extension.getStyleNode(style);
		Controller.getCurrentModeController().undoableCopyExtensions(LogicalStyleKeys.NODE_STYLE, node, styleNode);
		Controller.getCurrentModeController().undoableRemoveExtensions(LogicalStyleKeys.NODE_STYLE, node, node);
		LogicalStyleController.getController().refreshMap(node.getMap());
	}
}
