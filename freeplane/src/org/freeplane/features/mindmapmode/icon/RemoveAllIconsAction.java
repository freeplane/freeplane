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
package org.freeplane.features.mindmapmode.icon;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.icon.IIconInformation;
import org.freeplane.core.icon.IconController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.components.UITools;

/**
 * @author foltin
 */
class RemoveAllIconsAction extends AMultipleNodeAction implements IIconInformation {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 */
	public RemoveAllIconsAction(final Controller controller) {
		super("RemoveAllIconsAction", controller);
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		MIconController iconController = (MIconController) IconController.getController(getModeController());
		iconController.removeAllIcons(node);
	}

	public String getDescription() {
		return (String) getValue(Action.SHORT_DESCRIPTION);
	}

	public Icon getIcon() {
		return (ImageIcon) getValue(Action.SMALL_ICON);
	}

	public KeyStroke getKeyStroke() {
		return UITools.getKeyStroke(ResourceController.getResourceController().getAdjustableProperty(getShortcutKey()));
	}

	public String getShortcutKey() {
		return getKey() + ".shortcut";
	}
}
