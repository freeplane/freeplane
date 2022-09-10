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
package org.freeplane.features.styles.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.Set;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.LogicalStyleKeys;

/**
 * @author Dimitry Polivaev
 * 02.10.2009
 */
public class NewUserStyleFromSelectionAction extends AFreeplaneAction {
	public NewUserStyleFromSelectionAction() {
		super("NewUserStyleFromSelectionAction");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void actionPerformed(final ActionEvent e) {
		final MLogicalStyleController styleController = (MLogicalStyleController) LogicalStyleController.getController();
		IStyle newStyle = styleController.addNewUserStyle(true);
		if(newStyle != null) {
			Set<NodeModel> nodes = Controller.getCurrentController().getSelection().getSelection();
			for (NodeModel node : nodes) { 
				ModeController modeController = Controller.getCurrentModeController();
				modeController.undoableRemoveExtensions(LogicalStyleKeys.NODE_STYLE, node, node);
				styleController.setStyle(node, newStyle);
			}
		}
	}

}
