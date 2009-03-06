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
package org.freeplane.features.mindmapmode.addins.time;

import java.util.TimerTask;

import javax.swing.SwingUtilities;

/**
 * @author Dimitry Polivaev
 * Feb 20, 2009
 */
class TimerBlinkTask extends TimerTask {
	private final ReminderHook reminderController;
	/**
	 * 
	 */
	private final ReminderExtension reminderExtension;
	private boolean stateAdded = false;

	/**
	 */
	public TimerBlinkTask(final ReminderHook reminderController, final ReminderExtension reminderExtension,
	                      final boolean stateAdded) {
		super();
		this.reminderController = reminderController;
		this.reminderExtension = reminderExtension;
		this.stateAdded = stateAdded;
	}

	@Override
	public void run() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				stateAdded = !stateAdded;
				reminderController.blink(reminderExtension, stateAdded);
			}
		});
	}
}
