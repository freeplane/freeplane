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
package accessories.plugins;

import java.io.File;
import java.io.FileWriter;

import javax.swing.JOptionPane;

import org.freeplane.map.pattern.mindmapnode.MPatternController;
import org.freeplane.map.pattern.mindmapnode.StylePatternFactory;
import org.freeplane.modes.UserInputListenerFactory;
import org.freeplane.modes.mindmapmode.MModeController;

import accessories.plugins.dialogs.ChooseFormatPopupDialog;
import accessories.plugins.dialogs.ManagePatternsPopupDialog;
import deprecated.freemind.modes.mindmapmode.hooks.MindMapHookAdapter;

/** */
public class ManagePatterns extends MindMapHookAdapter {
	/**
	 *
	 */
	public ManagePatterns() {
		super();
	}

	@Override
	public void startup() {
		super.startup();
		final MModeController mindMapController = getMindMapController();
		final ManagePatternsPopupDialog formatDialog = new ManagePatternsPopupDialog(
		    mindMapController);
		formatDialog.setModal(true);
		formatDialog.setVisible(true);
		if (formatDialog.getResult() == ChooseFormatPopupDialog.OK) {
			try {
				final MPatternController patternController = mindMapController
				    .getPatternController();
				final File patternFile = patternController.getPatternsFile();
				StylePatternFactory.savePatterns(new FileWriter(patternFile),
				    formatDialog.getPatternList());
				patternController.loadPatterns(patternController
				    .getPatternReader());
				patternController.createPatternSubMenu(mindMapController
				    .getUserInputListenerFactory().getMenuBuilder(),
				    UserInputListenerFactory.NODE_POPUP);
			}
			catch (final Exception e) {
				JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
			}
		}
	}
}
