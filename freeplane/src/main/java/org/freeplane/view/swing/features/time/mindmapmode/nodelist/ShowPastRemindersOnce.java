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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.features.map.NodeModel;


/**
 * @author Dimitry Polivaev
 * Feb 20, 2009
 */
public class ShowPastRemindersOnce {
	private static final long UNDEFINED = - 1;
	protected static final String REMINDER_TEXT_WINDOW_TITLE = "reminder.WindowTitle_pastReminders";
	private boolean listIsShown;
	private long timeLimit = UNDEFINED;
	private final List<NodeModel> nodes = new ArrayList<NodeModel>();

	/**
	 * @param b
	 */
	public ShowPastRemindersOnce() {
		super();
		reset();
	}

	public void addNode(NodeModel node) {
		nodes.add(node);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				if(! listIsShown){
					listIsShown = true;
					final int showResult = OptionalDontShowMeAgainDialog.show("OptionPanel.reminder.showPastRemindersOnStart", "confirmation",
					    "reminder.showPastRemindersOnStart",
					    OptionalDontShowMeAgainDialog.BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED);
					if (showResult != JOptionPane.OK_OPTION) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								reset();
							}
						});
						return;
					}
					NodeListWithReminders timeList = new NodeListWithReminders(REMINDER_TEXT_WINDOW_TITLE,
						true, "allmaps.timelistwindow.configuration") {

							@Override
							protected void disposeDialog() {
								super.disposeDialog();
								reset();
							}

					};
					timeList.startup(nodes);
					nodes.clear();
				}
			}
		});
	}

	public boolean alreadyExecuted(){
		return listIsShown;
	}

	public long timeLimit() {
		if(timeLimit == UNDEFINED)
			timeLimit = System.currentTimeMillis();
		return timeLimit;
	}

	private void reset() {
		listIsShown = false;
		nodes.clear();
		timeLimit = UNDEFINED;
	}
}
