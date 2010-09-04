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

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.styles.LogicalStyleKeys;
import org.freeplane.features.common.styles.MapStyleModel;
import org.freeplane.features.mindmapmode.icon.MIconController.Keys;
import org.freeplane.features.mindmapmode.map.MMapController;

/**
 * @author Dimitry Polivaev
 * 02.10.2009
 */
public class DuplicateUserStyleAction extends AFreeplaneAction {
	public DuplicateUserStyleAction() {
		super("DuplicateUserStyleAction");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void actionPerformed(final ActionEvent e) {
		final String styleName = JOptionPane.showInputDialog(TextUtils.getText("enter_new_style_name"));
		if (styleName == null) {
			return;
		}
		final Controller controller = Controller.getCurrentController();
		final NodeModel selectedNode = controller.getSelection().getSelected();
		final MapModel map = controller.getMap();
		final MapStyleModel styleModel = MapStyleModel.getExtension(map);
		if (null != styleModel.getStyleNode(styleName)) {
			UITools.errorMessage(TextUtils.getText("style_already_exists"));
			return;
		}
		final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
		final NodeModel newNode = new NodeModel(map);
		newNode.setUserObject(styleName);
		Controller.getCurrentModeController().copyExtensions(LogicalStyleKeys.NODE_STYLE, selectedNode, newNode);
		Controller.getCurrentModeController().copyExtensions(Keys.ICONS, selectedNode, newNode);
		mapController.insertNode(newNode, getUserStyleParentNode(map), false, false, true);
		mapController.select(newNode);
		final IActor actor = new IActor() {
			public void undo() {
				styleModel.removeStyleNode(newNode);
			}

			public String getDescription() {
				return "NewStyle";
			}

			public void act() {
				styleModel.addStyleNode(newNode);
			}
		};
		Controller.getCurrentModeController().execute(actor, map);
	}

	private NodeModel getUserStyleParentNode(final MapModel map) {
		return (NodeModel) map.getRootNode().getChildAt(2);
	}
}
