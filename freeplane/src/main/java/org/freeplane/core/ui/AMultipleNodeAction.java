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
package org.freeplane.core.ui;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 */
public abstract class AMultipleNodeAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public AMultipleNodeAction(final String key) {
		super(key);
	}

	public AMultipleNodeAction(final String key, final String name, final ImageIcon imageIcon) {
		super(key, name, imageIcon);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final Collection<NodeModel> nodes = getNodes();
        for (final NodeModel selected : nodes.toArray(new NodeModel[]{})) {
			actionPerformed(e, selected);
		}
	}

	protected List<NodeModel> getNodes() {
		return Controller.getCurrentModeController().getMapController().getSelectedNodes();
	}

	abstract protected void actionPerformed(ActionEvent e, NodeModel node);
}
