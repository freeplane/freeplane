/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.map.icon.mindmapnode;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.freeplane.controller.Controller;
import org.freeplane.main.Tools;
import org.freeplane.map.icon.IIconInformation;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.MultipleNodeAction;

/**
 * @author foltin
 */
class RemoveAllIconsAction extends MultipleNodeAction implements
        IIconInformation {
	/**
	 */
	public RemoveAllIconsAction() {
		super("remove_all_icons", "images/edittrash.png");
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		removeAllIcons(node);
	}

	public String getDescription() {
		return (String) getValue(Action.SHORT_DESCRIPTION);
	}

	public ImageIcon getIcon() {
		return (ImageIcon) getValue(Action.SMALL_ICON);
	}

	public KeyStroke getKeyStroke() {
		return Tools.getKeyStroke(Controller.getResourceController()
		    .getAdjustableProperty(getKeystrokeResourceName()));
	}

	public String getKeystrokeResourceName() {
		return "keystroke_remove_all_icons";
	}

	public void removeAllIcons(final NodeModel node) {
		final int size = node.getIcons().size();
		final MIconController iconController = (MIconController) getModeController()
		    .getIconController();
		for (int i = 0; i < size; i++) {
			iconController.removeIcon(node, 0);
		}
	}
}
