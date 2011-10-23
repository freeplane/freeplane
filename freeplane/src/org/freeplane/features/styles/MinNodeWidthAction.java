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

public class MinNodeWidthAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MinNodeWidthAction() {
		super("MinNodeWidthAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final MapModel map = Controller.getCurrentController().getMap();
		final int minNodeWidth = MapStyleModel.getExtension(map).getMinNodeWidth();
		final JSpinner spinner = new JSpinner(new SpinnerNumberModel(minNodeWidth, 1, Integer.MAX_VALUE, 1));
		if (JOptionPane.CANCEL_OPTION == JOptionPane.showConfirmDialog(UITools.getFrame(), spinner,
		    (String) getValue(Action.NAME), JOptionPane.OK_CANCEL_OPTION)) {
			return;
		}
		final MapStyle mapStyle = (MapStyle) Controller.getCurrentModeController().getExtension(MapStyle.class);
		final Integer newWidth = (Integer) spinner.getValue();
		final MapStyleModel mapStyleModel = MapStyleModel.getExtension(Controller.getCurrentController().getMap());
		if (newWidth > mapStyleModel.getMaxNodeWidth()){
			final String minWidthError = TextUtils.getRawText("MinNodeWidthInvalid.text");
			final String message =minWidthError.replaceFirst("\\{0\\}", Integer.toString( mapStyleModel.getMaxNodeWidth()));
			JOptionPane.showMessageDialog(null, message, TextUtils.getRawText("error"), JOptionPane.WARNING_MESSAGE);
		}
		else {
			mapStyle.setMinNodeWidth(map, newWidth);
		}
	}
}
