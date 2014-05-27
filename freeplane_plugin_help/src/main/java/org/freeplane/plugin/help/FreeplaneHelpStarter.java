/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file author is Christian Foltin
 *  It is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.plugin.help;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.help.HelpBroker;
import javax.help.HelpSet;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogUtils;

/**
 * @author foltin
 */
class FreeplaneHelpStarter extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FreeplaneHelpStarter() {
		super("FreeplaneHelpStarter");
	}

	/**
	 *
	 */
	public void actionPerformed(final ActionEvent e) {
		final String helpHS = "org/freeplane/plugin/help/doc/freeplane.hs";
		try {
			final ClassLoader classLoader = this.getClass().getClassLoader();
			final URL hsURL = HelpSet.findHelpSet(classLoader, helpHS);
			final HelpSet hs = new HelpSet(classLoader, hsURL);
			final HelpBroker hb = hs.createHelpBroker();
			hb.initPresentation();
			hb.setDisplayed(true);
			hb.setViewDisplayed(true);
		}
		catch (final Exception ee) {
			LogUtils.severe("HelpSet " + helpHS + " not found", ee);
			return;
		}
	}
}
