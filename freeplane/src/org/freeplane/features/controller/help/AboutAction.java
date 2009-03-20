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
package org.freeplane.features.controller.help;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.FreeplaneVersion;
import org.freeplane.core.resources.FreeplaneResourceBundle;
import org.freeplane.core.ui.IFreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;

class AboutAction extends AbstractAction implements IFreeplaneAction {
	private static final long serialVersionUID = -5711560831676141530L;
	final private Controller controller;

	/**
	 *
	 */
	AboutAction(final Controller controller) {
		this.controller = controller;
		MenuBuilder.setLabelAndMnemonic(this, FreeplaneResourceBundle.getText("about"));
	}

	public void actionPerformed(final ActionEvent e) {
		JOptionPane.showMessageDialog(controller.getViewController().getViewport(), FreeplaneResourceBundle
		    .getText("about_text")
		        + FreeplaneVersion.getVersion(), FreeplaneResourceBundle.getText("about"),
		    JOptionPane.INFORMATION_MESSAGE);
	}

	public String getName() {
		return "about";
	}
}
