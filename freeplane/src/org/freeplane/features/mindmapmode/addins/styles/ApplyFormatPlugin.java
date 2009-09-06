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
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.features.mindmapnode.pattern.MPatternController;
import org.freeplane.features.mindmapnode.pattern.Pattern;
import org.freeplane.features.mindmapnode.pattern.StylePatternFactory;

@ActionLocationDescriptor(locations = { "/menu_bar/format/change" })
public class ApplyFormatPlugin extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 */
	public ApplyFormatPlugin(final Controller controller) {
		super("ApplyFormatPlugin", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final ModeController modeController = getModeController();
		final NodeModel focussed = modeController.getMapController().getSelectedNode();
		final List<NodeModel> selected = modeController.getMapController().getSelectedNodes();
		final Pattern nodePattern = StylePatternFactory.createPatternFromSelected(focussed, selected);
		final ChooseFormatPopupDialog formatDialog = new ChooseFormatPopupDialog(getController().getViewController()
		    .getFrame(), modeController, "accessories/plugins/ApplyFormatPlugin.dialog.title", nodePattern);
		formatDialog.setModal(true);
		formatDialog.setVisible(true);
		if (formatDialog.getResult() == ChooseFormatPopupDialog.OK) {
			final Pattern pattern = formatDialog.getPattern();
			for (final NodeModel node : selected) {
				MPatternController.getController(getModeController()).applyPattern(node, pattern);
			}
		}
	}
}
