package org.freeplane.features.mindmapmode.addins.styles;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.features.common.addins.styles.LogicalStyleController;
import org.freeplane.features.common.addins.styles.LogicalStyleKeys;
import org.freeplane.features.common.addins.styles.LogicalStyleModel;
import org.freeplane.features.common.addins.styles.MapStyleModel;

@ActionLocationDescriptor(locations = { "/menu_bar/styles/manage" })
public class RedefineStyleAction extends AFreeplaneAction{

	public RedefineStyleAction(Controller controller) {
		super("RedefineStyleAction", controller);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void actionPerformed(ActionEvent e) {
		NodeModel node = getController().getSelection().getSelected();
		Object style = LogicalStyleModel.getStyle(node);
		MapStyleModel extension = MapStyleModel.getExtension(node.getMap());
		NodeModel styleNode = extension.getStyleNode(style);
		getModeController().undoableCopyExtensions(LogicalStyleKeys.NODE_STYLE, node, styleNode);
		getModeController().undoableRemoveExtensions(LogicalStyleKeys.NODE_STYLE, node, node);
		LogicalStyleController.getController(getModeController()).refreshMap(node.getMap());
	}

}
