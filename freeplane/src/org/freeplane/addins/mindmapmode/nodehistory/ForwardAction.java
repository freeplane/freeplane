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
package org.freeplane.addins.mindmapmode.nodehistory;

import java.awt.event.ActionEvent;

import org.freeplane.controller.ActionDescriptor;
import org.freeplane.controller.FreeplaneAction;
import org.freeplane.ui.EnabledAction;

/**
 * @author Dimitry Polivaev
 * 13.12.2008
 */
@EnabledAction(checkOnNodeChange = true)
@ActionDescriptor(name = "accessories/plugins/NodeHistoryForward.properties_name", //
tooltip = "accessories/plugins/NodeHistoryForward.properties_documentation", //
keyStroke = "keystroke_accessories/plugins/NodeHistoryForward.keystroke.alt_FORWARD", //
iconPath = "accessories/plugins/icons/forward.png", //
locations = { "/menu_bar/navigate/folding", "/main_toolbar/folding" })
class ForwardAction extends FreeplaneAction {
	final private NodeHistory nodeHistory;

	public ForwardAction(final NodeHistory nodeHistory) {
		super();
		this.nodeHistory = nodeHistory;
	}

	public void actionPerformed(final ActionEvent e) {
		nodeHistory.goForward();
	}

	@Override
	public void setEnabled() {
		setEnabled(nodeHistory.canGoForward());
	}
}
