/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Nnamdi Kohn in 2012.
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

package org.freeplane.features.link.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.link.LinkController;

@EnabledAction(checkOnNodeChange=true)
public class ClearLinkAnchorAction extends AFreeplaneAction {
	public ClearLinkAnchorAction() {
		super("ClearLinkAnchorAction");
	}

	private static final long serialVersionUID = 1L;

	public void actionPerformed(final ActionEvent e) {

		/**
		 * @TODO
		 *
		 * -# clear tickmark in menu
		 * -# clear tooltip for mouse-over ClearLinkAnchorAction in menu
		 * 
		 */
		
		// clear current anchor in LinkModule
		((MLinkController)(LinkController.getController())).setAnchorID( null );
	}
	@Override
	public void setEnabled() {
		final boolean isAnchored = ((MLinkController)(LinkController.getController())).isAnchored();
		setEnabled( isAnchored );
	}
}
