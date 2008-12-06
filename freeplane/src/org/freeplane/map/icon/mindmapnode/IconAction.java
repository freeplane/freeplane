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

import org.freeplane.controller.Freeplane;
import org.freeplane.main.Tools;
import org.freeplane.map.icon.IIconInformation;
import org.freeplane.map.icon.MindIcon;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.mindmapmode.MModeController;

import deprecated.freemind.modes.mindmapmode.actions.undo.MultipleNodeAction;

class IconAction extends MultipleNodeAction implements IIconInformation {
	final private MindIcon icon;

	public IconAction(final MModeController controller, final MindIcon _icon) {
		super(controller, "icon_" + _icon.getName(), _icon.getIcon());
		putValue(Action.SHORT_DESCRIPTION, _icon.getDescription());
		icon = _icon;
	}

	@Override
	public void actionPerformed(final ActionEvent e, final NodeModel node) {
		if (e.getID() == ActionEvent.ACTION_FIRST
		        && (e.getModifiers() & ActionEvent.SHIFT_MASK
		                & ~ActionEvent.CTRL_MASK & ~ActionEvent.ALT_MASK) != 0) {
			((MIconController) getModeController().getIconController())
			    .removeAllIcons(node);
			((MIconController) getModeController().getIconController())
			    .addIcon(node, icon, 0);
			return;
		}
		if (e == null
		        || (e.getModifiers() & (ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK)) == 0) {
			((MIconController) getModeController().getIconController())
			    .addIcon(node, icon, MindIcon.LAST);
			return;
		}
	}

	public String getDescription() {
		return icon.getDescription();
	}

	public ImageIcon getIcon() {
		return icon.getIcon();
	}

	public KeyStroke getKeyStroke() {
		final String keystrokeResourceName = icon.getKeystrokeResourceName();
		final String keyStrokeDescription = Freeplane.getController()
		    .getResourceController().getAdjustableProperty(
		        keystrokeResourceName);
		return Tools.getKeyStroke(keyStrokeDescription);
	}

	public String getKeystrokeResourceName() {
		return icon.getKeystrokeResourceName();
	}

	public MindIcon getMindIcon() {
		return icon;
	}
}
