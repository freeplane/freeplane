package org.freeplane.features.common.styles;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.common.map.MapModel;

public class MaxNodeWidthAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MaxNodeWidthAction() {
		super("MaxNodeWidthAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final MapModel map = Controller.getCurrentController().getMap();
		final int maxNodeWidth = MapStyleModel.getExtension(map).getMaxNodeWidth();
		final JSpinner spinner = new JSpinner(new SpinnerNumberModel(maxNodeWidth, 1, Integer.MAX_VALUE, 1));
		if (JOptionPane.CANCEL_OPTION == JOptionPane.showConfirmDialog(UITools.getFrame(), spinner,
		    (String) getValue(Action.NAME), JOptionPane.OK_CANCEL_OPTION)) {
			return;
		}
		final MapStyle mapStyle = (MapStyle) Controller.getCurrentModeController().getExtension(MapStyle.class);
		final Integer newWidth = (Integer) spinner.getValue();
		mapStyle.setMaxNodeWidth(map, newWidth);
	}
}
