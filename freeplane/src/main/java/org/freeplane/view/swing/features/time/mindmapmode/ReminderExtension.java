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

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.SysUtils;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev 30.11.2008
 */
public class ReminderExtension implements IExtension, IMapChangeListener {
	static final int BLINKING_PERIOD = 1000;
	/**
	 */
	public static ReminderExtension getExtension(final NodeModel node) {
		return node.getExtension(ReminderExtension.class);
	}

	private final NodeModel node;
	private long remindUserAt = 0;
	private PeriodUnit periodUnit;
	private int period;
	private Timer timer;
	private String script;
	private TimerBlinkTask task;

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


	public PeriodUnit getPeriodUnit() {
    	return periodUnit;
    }

	public void setPeriodUnit(PeriodUnit periodUnit) {
    	this.periodUnit = periodUnit;
    }

    public String getPeriodUnitAsString() {
        return periodUnit == null ? null : periodUnit.name();
    }

    public void setPeriodUnitAsString(String periodUnit) {
        this.periodUnit = PeriodUnit.valueOf(periodUnit);
    }

	public int getPeriod() {
    	return period;
    }

	public void setPeriod(int period) {
    	this.period = period;
    }

	public String getScript() {
    	return script;
    }

	public void setScript(String script) {
    	this.script = script;
    }

	public void scheduleTimer(final TimerBlinkTask task, final Date date) {
		if (timer == null) {
			timer = SysUtils.createTimer(getClass().getSimpleName());
		}
		timer.schedule(task, date, BLINKING_PERIOD);
		this.task = task;
	}

	public void deactivateTimer() {
		if (timer == null) {
			return;
		}
		timer.cancel();
		timer = null;
		task = null;
	}

	private void displayStateIcon(final NodeModel parent, final ClockState state) {
		if (task != null && ! task.alreadyExecuted() || !isAncestorNode(parent)) {
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

	@Override
	public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
		displayStateIcon(parent, ClockState.CLOCK_VISIBLE);
	}

	@Override
	public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
		displayStateIcon(nodeMoveEvent.newParent, ClockState.CLOCK_VISIBLE);
	}

	@Override
	public void onPreNodeDelete(NodeDeletionEvent nodeDeletionEvent) {
		displayStateIcon(nodeDeletionEvent.parent, null);
	}

	@Override
	public void onPreNodeMoved(NodeMoveEvent nodeMoveEvent) {
		displayStateIcon(nodeMoveEvent.oldParent, null);
	}

	@Override
	public void mapChanged(final MapChangeEvent event) {
	}

	@Override
	public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
	}

	public void displayState(final ClockState stateAdded, final NodeModel pNode,
	                  final boolean recurse) {
		if(stateAdded != null)
			pNode.putExtension(stateAdded);
		else
			pNode.removeExtension(ClockState.class);
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

	boolean containsScript() {
		return script != null && ! script.isEmpty();
	}
}
