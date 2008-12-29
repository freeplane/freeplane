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
package plugins.help;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.logging.Logger;

import javax.help.HelpBroker;
import javax.help.HelpSet;

import org.freeplane.controller.ActionDescriptor;
import org.freeplane.controller.FreeplaneAction;

/**
 * @author foltin
 */
@ActionDescriptor(name = "plugins/FreemindHelp.xml_name", //
tooltip = "plugins/FreemindHelp.xml_documentation", //
locations = { "/menu_bar/help/doc" })
public class FreeplaneHelpStarter extends FreeplaneAction {
	/**
	 *
	 */
	public void actionPerformed(final ActionEvent e) {
		final String helpHS = "plugins/help/doc/freemind.hs";
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
			org.freeplane.Tools.logException(ee);
			Logger.global.warning("HelpSet " + ee.getMessage() + ee);
			Logger.global.warning("HelpSet " + helpHS + " not found");
			return;
		}
	}
}
