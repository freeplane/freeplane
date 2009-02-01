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
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.IIconInformation;
import org.freeplane.core.model.MindIcon;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.MultipleNodeAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IUndoableActor;

/**
 * @author foltin
 */
class RemoveIconAction extends MultipleNodeAction implements IIconInformation {
	/**
	 */
	public RemoveIconAction(final Controller controller) {
		super(controller, "remove_last_icon", "/images/remove.png");
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		if ((e.getModifiers() & ~ActionEvent.SHIFT_MASK & ~ActionEvent.CTRL_MASK & ActionEvent.ALT_MASK) != 0) {
			removeIcon(node, MindIcon.LAST);
			return;
		}
		if ((e.getModifiers() & ~ActionEvent.SHIFT_MASK & ~ActionEvent.CTRL_MASK & ~ActionEvent.ALT_MASK) != 0) {
			removeIcon(node, 0);
			return;
		}
	}

	public String getDescription() {
		return (String) getValue(Action.SHORT_DESCRIPTION);
	}

	public ImageIcon getIcon() {
		return (ImageIcon) getValue(Action.SMALL_ICON);
	}

	public KeyStroke getKeyStroke() {
		return UITools.getKeyStroke(Controller.getResourceController()
		    .getAdjustableProperty(getKeystrokeResourceName()));
	}

	public String getKeystrokeResourceName() {
		return "keystroke_remove_last_icon";
	}

	public int removeIcon(final NodeModel node, final int position) {
		final IUndoableActor actor = new IUndoableActor() {
			private final MindIcon icon = node.getIcon(position);

			public void act() {
				node.removeIcon(position);
				getModeController().getMapController().nodeChanged(node, "icon", icon, null);
			}

			public String getDescription() {
				return "removeIcon";
			}

			public void undo() {
				node.addIcon(icon, position);
				getModeController().getMapController().nodeChanged(node, "icon", null, icon);
			}
		};
		getModeController().execute(actor);
		return node.getIcons().size();
	}
}
