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
package org.freeplane.addins.mindmapmode.styles;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import org.freeplane.core.controller.ActionDescriptor;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.FreeplaneAction;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.mode.ModeController;
import org.freeplane.map.pattern.mindmapnode.Pattern;
import org.freeplane.map.pattern.mindmapnode.StylePatternFactory;
import org.freeplane.modes.mindmapmode.MModeController;

@ActionDescriptor(name = "accessories/plugins/ApplyFormatPlugin.properties_name", //
locations = { "/menu_bar/format/change" }, //
tooltip = "accessories/plugins/ApplyFormatPlugin.properties_documentation" //
)
public class ApplyFormatPlugin extends FreeplaneAction {
	/**
	 */
	public ApplyFormatPlugin() {
		super();
	}

	public void actionPerformed(final ActionEvent e) {
		final ModeController modeController = getModeController();
		final NodeModel focussed = modeController.getSelectedNode();
		final List selected = modeController.getSelectedNodes();
		final Pattern nodePattern = StylePatternFactory.createPatternFromSelected(focussed,
		    selected);
		final ChooseFormatPopupDialog formatDialog = new ChooseFormatPopupDialog(Controller
		    .getController().getViewController().getJFrame(), (MModeController) modeController,
		    "accessories/plugins/ApplyFormatPlugin.dialog.title", nodePattern);
		formatDialog.setModal(true);
		formatDialog.setVisible(true);
		if (formatDialog.getResult() == ChooseFormatPopupDialog.OK) {
			final Pattern pattern = formatDialog.getPattern();
			for (final Iterator iter = selected.iterator(); iter.hasNext();) {
				final NodeModel node = (NodeModel) iter.next();
				getMModeController().getPatternController().applyPattern(node, pattern);
			}
		}
	}
}
