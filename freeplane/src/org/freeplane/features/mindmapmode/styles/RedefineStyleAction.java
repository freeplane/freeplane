package org.freeplane.features.mindmapmode.styles;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.styles.LogicalStyleController;
import org.freeplane.features.common.styles.LogicalStyleKeys;
import org.freeplane.features.common.styles.LogicalStyleModel;
import org.freeplane.features.common.styles.MapStyleModel;

@ActionLocationDescriptor(locations = { "/menu_bar/styles/manage" })
public class RedefineStyleAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	
	public RedefineStyleAction(final Controller controller) {
		super("RedefineStyleAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel node = getController().getSelection().getSelected();
		final Object style = LogicalStyleModel.getStyle(node);
		final MapStyleModel extension = MapStyleModel.getExtension(node.getMap());
		final NodeModel styleNode = extension.getStyleNode(style);
		getModeController().undoableCopyExtensions(LogicalStyleKeys.NODE_STYLE, node, styleNode);
		getModeController().undoableRemoveExtensions(LogicalStyleKeys.NODE_STYLE, node, node);
		LogicalStyleController.getController(getModeController()).refreshMap(node.getMap());
	}
}
