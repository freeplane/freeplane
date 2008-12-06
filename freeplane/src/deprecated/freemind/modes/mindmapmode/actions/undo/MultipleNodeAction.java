/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is to be deleted.
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
package deprecated.freemind.modes.mindmapmode.actions.undo;

import java.awt.event.ActionEvent;
import java.util.ListIterator;

import javax.swing.ImageIcon;

import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.ModeControllerAction;
import org.freeplane.modes.mindmapmode.MModeController;

/**
 * @author Dimitry Polivaev
 */
public abstract class MultipleNodeAction extends ModeControllerAction {
	public MultipleNodeAction(final MModeController modeController,
	                          final String name) {
		super(modeController, name);
	}

	/**
	 *
	 */
	public MultipleNodeAction(final MModeController modeController,
	                          final String name, final ImageIcon imageIcon) {
		super(modeController, name, imageIcon);
	}

	public MultipleNodeAction(final MModeController modeController,
	                          final String name, final String imageIcon) {
		super(modeController, name, imageIcon);
	}

	public void actionPerformed(final ActionEvent e) {
		for (final ListIterator it = getMModeController().getSelectedNodes()
		    .listIterator(); it.hasNext();) {
			final NodeModel selected = (NodeModel) it.next();
			actionPerformed(e, selected);
		}
	}

	abstract protected void actionPerformed(ActionEvent e, NodeModel node);
}
