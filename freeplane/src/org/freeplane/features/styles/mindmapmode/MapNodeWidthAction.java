package org.freeplane.features.styles.mindmapmode;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapStyleModel;

class MapNodeWidthAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MapNodeWidthAction() {
		super("MapNodeWidthAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final MapModel map = Controller.getCurrentController().getMap();
		final int minNodeWidth = MapStyleModel.getExtension(map).getMinNodeWidth();
		final int maxNodeWidth = MapStyleModel.getExtension(map).getMaxNodeWidth();
		final MapStyle mapStyle = (MapStyle) Controller.getCurrentModeController().getExtension(MapStyle.class);
		final NodeSizeDialog nodeSizeDialog = new NodeSizeDialog();
		nodeSizeDialog.setTitle(TextUtils.getText("MapNodeWidthAction.text"));
		if(nodeSizeDialog.showDialog(minNodeWidth, maxNodeWidth)){
			mapStyle.setMinNodeWidth(map, nodeSizeDialog.getMinWidth());
			mapStyle.setMaxNodeWidth(map, nodeSizeDialog.getMaxTextWidth());
		}
	}
}
