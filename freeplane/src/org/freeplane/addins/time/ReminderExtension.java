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
package org.freeplane.addins.time;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.freeplane.controller.Freeplane;
import org.freeplane.extension.IExtension;
import org.freeplane.map.icon.MindIcon;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.mindmapmode.MMapController;

/**
 * @author Dimitry Polivaev 30.11.2008
 */
class ReminderExtension implements IExtension {
	class TimerBlinkTask extends TimerTask {
		private boolean stateAdded = false;

		/**
		 */
		public TimerBlinkTask(final boolean stateAdded) {
			super();
			this.stateAdded = stateAdded;
		}

		@Override
		public void run() {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					stateAdded = !stateAdded;
					blink(stateAdded);
				}
			});
		}
	}

	private static ImageIcon bellIcon;
	static final int CLOCK_INVISIBLE = 0;
	static final int CLOCK_VISIBLE = 1;
	private static ImageIcon clockIcon = null;
	private static ImageIcon flagIcon;
	static final String PLUGIN_LABEL = "plugins/TimeManagementReminder.xml";
	static final String REMINDUSERAT = "REMINDUSERAT";
	private static final int REMOVE_CLOCK = -1;

	/**
	 */
	public static ReminderExtension getExtension(final NodeModel node) {
		return (ReminderExtension) node.getExtension(ReminderExtension.class);
	}

	private String mStateTooltipName = null;
	private final NodeModel node;
	private long remindUserAt = 0;
	final private String STATE_TOOLTIP = TimerBlinkTask.class.getName()
	        + "_STATE_";
	private Timer timer;

	public ReminderExtension(final NodeModel node) {
		this.node = node;
	}

	void blink(final boolean stateAdded) {
		setRemindUserAt(System.currentTimeMillis() + 3000);
		scheduleTimer(new TimerBlinkTask(stateAdded));
		displayState((stateAdded) ? CLOCK_VISIBLE : CLOCK_INVISIBLE, node, true);
	}

	void deactivate() {
		setToolTip(node, null);
		if (timer != null) {
			timer.cancel();
		}
		displayState(ReminderExtension.REMOVE_CLOCK, node, true);
	}

	void displayState(final int stateAdded, final NodeModel pNode,
	                  final boolean recurse) {
		ImageIcon icon = null;
		if (stateAdded == CLOCK_VISIBLE) {
			icon = getClockIcon();
		}
		else if (stateAdded == CLOCK_INVISIBLE) {
			if (pNode == node) {
				icon = getBellIcon();
			}
			else {
				icon = getFlagIcon();
			}
		}
		pNode.setStateIcon(getStateKey(), icon);
		pNode.getModeController().getMapController().nodeRefresh(pNode);
		if (recurse && !pNode.isRoot()) {
			displayState(stateAdded, pNode.getParentNode(), recurse);
		}
	}

	private ImageIcon getBellIcon() {
		if (bellIcon == null) {
			bellIcon = MindIcon.factory("bell").getIcon();
		}
		return bellIcon;
	}

	private ImageIcon getClockIcon() {
		if (clockIcon == null) {
			clockIcon = MindIcon.factory("clock2").getIcon();
		}
		return clockIcon;
	}

	private ImageIcon getFlagIcon() {
		if (flagIcon == null) {
			flagIcon = MindIcon.factory("flag").getIcon();
		}
		return flagIcon;
	}

	long getRemindUserAt() {
		return remindUserAt;
	}

	String getStateKey() {
		if (mStateTooltipName == null) {
			mStateTooltipName = STATE_TOOLTIP;
		}
		return mStateTooltipName;
	}

	public void scheduleTimer() {
		scheduleTimer(new TimerBlinkTask(false));
	}

	void scheduleTimer(final TimerTask task) {
		timer = new Timer();
		final Date date = new Date(getRemindUserAt());
		timer.schedule(task, date);
		final Object[] messageArguments = { date };
		final MessageFormat formatter = new MessageFormat(Freeplane
		    .getText("plugins/TimeManagement.xml_reminderNode_tooltip"));
		final String message = formatter.format(messageArguments);
		setToolTip(node, message);
		displayState(CLOCK_VISIBLE, node, false);
	}

	void setRemindUserAt(final long remindUserAt) {
		this.remindUserAt = remindUserAt;
	}

	protected void setToolTip(final NodeModel node, final String value) {
		((MMapController) node.getModeController().getMapController())
		    .setToolTip(node, getClass().getName(), value);
	}
}
