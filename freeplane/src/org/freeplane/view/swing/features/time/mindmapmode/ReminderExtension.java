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
package org.freeplane.view.swing.features.time.mindmapmode;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.SysUtils;
import org.freeplane.features.icon.IconStore;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev 30.11.2008
 */
class ReminderExtension implements IExtension, IMapChangeListener {
	static final int BLINKING_PERIOD = 1000;
	private static final IconStore STORE = IconStoreFactory.create();
	private static UIIcon bellIcon;
	private static UIIcon clockIcon;
	private static UIIcon flagIcon;
	final private String STATE_TOOLTIP = ReminderExtension.class.getName() + "_STATE_";
	/**
	 */
	public static ReminderExtension getExtension(final NodeModel node) {
		return (ReminderExtension) node.getExtension(ReminderExtension.class);
	}

	private final NodeModel node;
	private long remindUserAt = 0;
	private Timer timer;
	private String script;

	public ReminderExtension(final NodeModel node) {
		this.node = node;
	}

	public NodeModel getNode() {
		return node;
	}

	public long getRemindUserAt() {
		return remindUserAt;
	}

	public void setRemindUserAt(final long remindUserAt) {
		this.remindUserAt = remindUserAt;
	}
	

	String getScript() {
    	return script;
    }

	void setScript(String script) {
    	this.script = script;
    }

	public void scheduleTimer(final TimerTask task, final Date date) {
		if (timer == null) {
			timer = SysUtils.createTimer(getClass().getSimpleName());
		}
		timer.schedule(task, date, BLINKING_PERIOD);
	}

	public void deactivateTimer() {
		if (timer == null) {
			return;
		}
		timer.cancel();
		timer = null;
	}

	private void displayStateIcon(final NodeModel parent, final ClockState state) {
		if (!isAncestorNode(parent)) {
			return;
		}
		displayState(state, parent, true);
	}

	private boolean isAncestorNode(final NodeModel parent) {
		for (NodeModel n = node; n != null; n = n.getParentNode()) {
			if (n.equals(parent)) {
				return true;
			}
		}
		return false;
	}

	public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
		displayStateIcon(parent, ClockState.CLOCK_VISIBLE);
	}

	public void onNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
	                        final NodeModel child, final int newIndex) {
		displayStateIcon(newParent, ClockState.CLOCK_VISIBLE);
	}

	public void onPreNodeDelete(final NodeModel oldParent, final NodeModel selectedNode, final int index) {
		displayStateIcon(oldParent, ClockState.REMOVE_CLOCK);
	}

	public void onPreNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
	                           final NodeModel child, final int newIndex) {
		displayStateIcon(oldParent, ClockState.REMOVE_CLOCK);
	}

	public void mapChanged(final MapChangeEvent event) {
	}

	public void onNodeDeleted(final NodeModel parent, final NodeModel child, final int index) {
	}
	
	public void displayState(final ClockState stateAdded, final NodeModel pNode,
	                  final boolean recurse) {
		UIIcon icon = null;
		if (stateAdded == ClockState.CLOCK_VISIBLE) {
			icon = getClockIcon();
		}
		else if (stateAdded == ClockState.CLOCK_INVISIBLE) {
			if (pNode == getNode()) {
				icon = getBellIcon();
			}
			else {
				icon = getFlagIcon();
			}
		}
		if (stateAdded != ClockState.REMOVE_CLOCK || pNode == getNode()
		        || ReminderExtension.getExtension(pNode) == null) {
			pNode.setStateIcon(STATE_TOOLTIP, icon, true);
		}
		Controller.getCurrentModeController().getMapController().nodeRefresh(pNode);
		if (!recurse) {
			return;
		}
		final NodeModel parentNode = pNode.getParentNode();
		if (parentNode == null) {
			return;
		}
		displayState(stateAdded, parentNode, recurse);
	}
	private UIIcon getBellIcon() {
		if (bellIcon == null) {
			bellIcon = STORE.getUIIcon("bell.png");
		}
		return bellIcon;
	}

	private UIIcon getClockIcon() {
		if (clockIcon == null) {
			clockIcon = STORE.getUIIcon("clock.png");
		}
		return clockIcon;
	}

	private UIIcon getFlagIcon() {
		if (flagIcon == null) {
			flagIcon = STORE.getUIIcon("flag.png");
		}
		return flagIcon;
	}
}
