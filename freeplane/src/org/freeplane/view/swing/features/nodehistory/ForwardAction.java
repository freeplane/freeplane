/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.view.swing.features.nodehistory;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 * 13.12.2008
 */
@EnabledAction(checkOnNodeChange = true)
class ForwardAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private NodeHistory nodeHistory;

	public ForwardAction(final Controller controller, final NodeHistory nodeHistory) {
		super("ForwardAction");
		this.nodeHistory = nodeHistory;
		setEnabled(false);
	}

	public void actionPerformed(final ActionEvent e) {
		nodeHistory.goForward(0 != (e.getModifiers() & ActionEvent.CTRL_MASK));
	}

	@Override
	public void setEnabled() {
		setEnabled(nodeHistory.canGoForward());
	}
}
