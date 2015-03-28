package org.freeplane.features.styles.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.LogicalStyleKeys;
import org.freeplane.features.styles.MapStyleModel;

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
		if(styleNode == null)
			return;
		Controller.getCurrentModeController().undoableCopyExtensions(LogicalStyleKeys.NODE_STYLE, node, styleNode);
		Controller.getCurrentModeController().undoableRemoveExtensions(LogicalStyleKeys.NODE_STYLE, node, node);
		LogicalStyleController.getController().refreshMap(node.getMap());
	}
}
