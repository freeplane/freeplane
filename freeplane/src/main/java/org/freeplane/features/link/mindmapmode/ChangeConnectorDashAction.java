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
package org.freeplane.features.link.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.Arrays;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.DashVariant;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.LinkController;

class ChangeConnectorDashAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ConnectorModel connector;
	private final DashVariant dash;

	public ChangeConnectorDashAction(final MLinkController linkController,
	                                   final ConnectorModel connector, final DashVariant dash) {
		super("ChangeConnectorDashAction", "", dash.icon);
		this.connector = connector;
		this.dash = dash;
		final int[] dash2 = connector.getDash();
		final int[] variant = dash.variant;
		final boolean selected = dash2 == variant || variant != null && Arrays.equals(variant, dash2);
		setSelected(selected);
	}

	public void actionPerformed(final ActionEvent e) {
		final MLinkController linkController = (MLinkController) LinkController.getController();
		linkController.setConnectorDashArray(connector, dash.variant);
	}
}
