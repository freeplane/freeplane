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
package org.freeplane.core.resources.components;

import javax.swing.JDialog;

import org.freeplane.core.resources.WindowConfigurationStorage;
import org.freeplane.n3.nanoxml.XMLElement;

class OptionPanelWindowConfigurationStorage extends WindowConfigurationStorage {
	public OptionPanelWindowConfigurationStorage() {
	    super("option_panel_window_configuration_storage");
    }

	public static OptionPanelWindowConfigurationStorage decorateDialog(final String marshalled, final JDialog dialog) {
		final OptionPanelWindowConfigurationStorage storage = new OptionPanelWindowConfigurationStorage();
		final XMLElement xml = storage.unmarschall(marshalled, dialog);
		if (xml != null) {
			storage.panel = xml.getAttribute("panel", null);
			return storage;
		}
		return null;
	}

	protected String panel;

	public String getPanel() {
		return panel;
	}

	@Override
	protected void marshallSpecificElements(final XMLElement xml) {
		xml.setAttribute("panel", panel);
	}

	public void setPanel(final String panel) {
		this.panel = panel;
	}
}
