package org.freeplane.features.common.addins.mapstyle;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;

public class MaxNodeWidthAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MaxNodeWidthAction(Controller controller) {
		super("MaxNodeWidthAction", controller);
	}

	public void actionPerformed(ActionEvent e) {
		MapModel map = getController().getMap();
		int maxNodeWidth = MapStyleModel.getExtension(map).getMaxNodeWidth();
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(maxNodeWidth, 1,
				Integer.MAX_VALUE, 1));
		if (JOptionPane.CANCEL_OPTION == JOptionPane.showConfirmDialog(UITools
				.getFrame(), spinner, (String) getValue(Action.NAME),
				JOptionPane.OK_CANCEL_OPTION)) {
			return;
		}
		MapStyle mapStyle = (MapStyle) getModeController().getExtension(
				MapStyle.class);
		Integer newWidth = (Integer) spinner.getValue();
		mapStyle.setMaxNodeWidth(map, newWidth);
	}

}
