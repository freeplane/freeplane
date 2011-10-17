package org.freeplane.features.styles;

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
		final MapStyleModel mapStyleModel = MapStyleModel.getExtension(Controller.getCurrentController().getMap());
		if (newWidth < mapStyleModel.getMinNodeWidth()){
			final String minWidthError = TextUtils.getRawText("MaxNodeWidthInvalid.text");
			final String message =minWidthError.replaceFirst("\\{0\\}", Integer.toString( mapStyleModel.getMinNodeWidth()));
			JOptionPane.showMessageDialog(null, message, TextUtils.getRawText("error"), JOptionPane.WARNING_MESSAGE);
		}
		else {
			mapStyle.setMaxNodeWidth(map, newWidth);
		}
	}
}
