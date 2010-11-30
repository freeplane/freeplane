package org.freeplane.features.mindmapmode.styles;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.styles.IStyle;
import org.freeplane.features.common.styles.LogicalStyleController;
import org.freeplane.features.common.styles.LogicalStyleKeys;
import org.freeplane.features.common.styles.MapStyleModel;

@ActionLocationDescriptor(locations = { "/menu_bar/styles/manage" })
public class RedefineStyleAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	
	public RedefineStyleAction() {
		super("RedefineStyleAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel node = Controller.getCurrentController().getSelection().getSelected();
		final ArrayList<IStyle> styles = new ArrayList<IStyle>(LogicalStyleController.getController().getStyles(node));
		final MapStyleModel styleModel = MapStyleModel.getExtension(node.getMap());
		for(int i = styles.size() - 1; i >= 0; i--){
			IStyle style = styles.get(i);
			if(MapStyleModel.DEFAULT_STYLE.equals(style)){
				continue;
			}
			final NodeModel styleNode = styleModel.getStyleNode(style);
			Controller.getCurrentModeController().undoableCopyExtensions(LogicalStyleKeys.NODE_STYLE, node, styleNode);
			Controller.getCurrentModeController().undoableRemoveExtensions(LogicalStyleKeys.NODE_STYLE, node, node);
		}
		LogicalStyleController.getController().refreshMap(node.getMap());
	}
}
