/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.main.mindmapmode.stylemode;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.common.addins.mapstyle.MapStyle;
import org.freeplane.features.common.addins.mapstyle.MapStyleModel;
import org.freeplane.features.mindmapmode.MModeController;

/**
 * @author Dimitry Polivaev
 * 13.09.2009
 */
public class EditStylesAction extends AFreeplaneAction {
	public EditStylesAction(Controller controller) {
	    super("EditStylesAction", controller);
    }

	/**
     * 
     */
    private static final long serialVersionUID = 1L;

	public void actionPerformed(ActionEvent e) {
		final JDialog dialog = new JDialog(getController().getViewController().getJFrame());
		dialog.setSize(400, 300);
		dialog.setLocationRelativeTo(getController().getViewController().getJFrame());
		final MModeController modeController = SModeControllerFactory.getInstance().createModeController(dialog);
		final MapModel map = getController().getMap();
		final MapStyleModel mapStyleModel = MapStyleModel.getExtension(map);
		final MapModel styleMap = mapStyleModel.getStyleMap();
		mapStyleModel.getBackgroundColor();
		modeController.getMapController().newMapView(styleMap);
		dialog.addComponentListener(new ComponentAdapter() {

			@Override
            public void componentHidden(ComponentEvent e) {
	            modeController.getController().getMapViewManager().close(true);
	            dialog.removeComponentListener(this);
	            super.componentHidden(e);
            }
			
		});
		final ViewController viewController = modeController.getController().getViewController();
		viewController.init();
		viewController.setLeftToolbarVisible(false);
		dialog.setVisible(true);
	}
}
