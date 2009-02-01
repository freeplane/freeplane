/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.features.mindmapmode.addins.styles;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.ui.ActionDescriptor;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.features.mindmapnode.pattern.MPatternController;
import org.freeplane.features.mindmapnode.pattern.StylePatternFactory;
import org.freeplane.view.swing.ui.UserInputListenerFactory;

@ActionDescriptor(tooltip = "accessories/plugins/ManagePatterns.properties_documentation", //
name = "accessories/plugins/ManagePatterns.properties_name", //
keyStroke = "keystroke_accessories/plugins/ManagePatterns_manage_patterns_dialog", //
locations = { "/menu_bar/format/patterns/manage", "/node_popup/patterns/manage" })
public class ManagePatterns extends FreeplaneAction {
	/**
	 *
	 */
	public ManagePatterns(final Controller controller) {
		super(controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final ModeController mindMapController = getModeController();
		final ManagePatternsPopupDialog formatDialog = new ManagePatternsPopupDialog(mindMapController);
		formatDialog.setModal(true);
		formatDialog.setVisible(true);
		if (formatDialog.getResult() == ChooseFormatPopupDialog.OK) {
			try {
				final MPatternController patternController = MPatternController.getController(mindMapController);
				final File patternFile = patternController.getPatternsFile();
				StylePatternFactory.savePatterns(new FileWriter(patternFile), formatDialog.getPatternList());
				patternController.loadPatterns(patternController.getPatternReader());
				patternController.createPatternSubMenu(
				    mindMapController.getUserInputListenerFactory().getMenuBuilder(),
				    UserInputListenerFactory.NODE_POPUP);
			}
			catch (final Exception ex) {
				JOptionPane.showMessageDialog(null, ex.getLocalizedMessage());
			}
		}
	}
}
