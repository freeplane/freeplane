/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
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
package org.freeplane.view.swing.features.time.mindmapmode.nodelist;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;


/**
 * @author Dimitry Polivaev
 * Feb 20, 2009
 */
public class ShowPastRemindersOnce implements Runnable {
	private boolean alreadyExecuted;

	/**
	 * @param b
	 */
	public ShowPastRemindersOnce() {
		super();
		alreadyExecuted = false;
	}

	@Override
	public void run() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				if(! alreadyExecuted){
					alreadyExecuted = true;
					final int showResult = OptionalDontShowMeAgainDialog.show("OptionPanel.plugins/TimeManagement.xml_showPastRemindersOnStart", "confirmation",
					    "plugins/TimeManagement.xml_showPastRemindersOnStart",
					    OptionalDontShowMeAgainDialog.BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED);
					if (showResult != JOptionPane.OK_OPTION) {
						return;
					}
					final long currentTimeMillis = System.currentTimeMillis();
					NodeListWithReminders timeList = new NodeListWithReminders(NodeList.PLUGINS_TIME_MANAGEMENT_XML_WINDOW_TITLE,
						(node, reminder) -> reminder != null && reminder.getRemindUserAt() < currentTimeMillis,
						true, "timelistwindow.configuration");
					timeList.startup();
				}
			}
		});
	}

	public boolean alreadyExecuted(){
		return alreadyExecuted;
	}
}
