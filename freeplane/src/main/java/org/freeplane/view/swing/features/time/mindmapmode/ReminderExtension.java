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

import java.awt.event.ActionEvent;
import java.time.Duration;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.script.IScriptStarter;
import org.freeplane.view.swing.features.time.mindmapmode.nodelist.ShowPastRemindersOnce;

/**
 * @author Dimitry Polivaev 30.11.2008
 */
public class ReminderExtension implements IExtension, IMapChangeListener {
    private static final ShowPastRemindersOnce pastReminders = new ShowPastRemindersOnce();
    private static final int BLINKING_PERIOD = 1000;
    private static final int MAXIMAL_DELAY = (int) Duration.ofMinutes(5).toMillis();
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
    private final ReminderHook reminderController;
    private boolean stateAdded = false;
    private boolean reminderInThePast = false;
    private boolean alreadyExecuted = false;

    public ReminderExtension(ReminderHook reminderController, final NodeModel node) {
        this.reminderController = reminderController;
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
    
    void scheduleTimer() {
        long timeBeforeReminder = remindUserAt - System.currentTimeMillis();
        reminderInThePast = timeBeforeReminder < - MAXIMAL_DELAY;
        int delay = (int) Math.min(Integer.MAX_VALUE, Math.max(0, timeBeforeReminder));
        if (timer == null) {
            timer = new Timer(delay, this::remind);
            timer.setRepeats(false);
        }
        timer.start();
        final NodeModel node = getNode();
        if(reminderInThePast)
            pastReminders.addNode(node);
        displayState(ClockState.CLOCK_VISIBLE, node, false);
    }


    void deactivateTimer() {
        if (timer == null) {
            return;
        }
        displayState(null, getNode(), true);
        timer.stop();
        timer = null;
    }

    private boolean isAncestorNode(final NodeModel parent) {
        for (NodeModel n = node; n != null; n = n.getParentNode()) {
            if (n.equals(parent)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsScript() {
        return script != null && ! script.isEmpty();
    }

    private void remind(ActionEvent e) {
        if(! alreadyExecuted && remindUserAt > System.currentTimeMillis()) {
            scheduleTimer();
            return;
        }
            
        if(!reminderInThePast && containsScript()){
            reminderInThePast = true;
            runScript();
        }
        if(! alreadyExecuted){
            if(!reminderInThePast && ResourceController.getResourceController().getBooleanProperty("remindersShowNotifications"))
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        reminderController.showNotificationPopup(ReminderExtension.this);
                    }
                });

            alreadyExecuted = true;
        }
        stateAdded = !stateAdded;
        blink(stateAdded);
        timer.setDelay(BLINKING_PERIOD);
        timer.start();
    }

    public void runScript() {
    	if(! containsScript())
    		return;
    	final String script = getScript();
    	ModeController modeController = reminderController.getModeController();
        final IScriptStarter starter = modeController.getExtension(IScriptStarter.class);
    	if(starter == null)
    		return;
    	final NodeModel node = getNode();
    	final MapModel map = node.getMap();
    	final Controller controller = modeController.getController();
    	if(! controller.getMapViewManager().getMaps(modeController.getModeName()).containsValue(map))
    		return;
    	try {
    		starter.executeScript(node, script);
    	}
    	catch (Exception e) {
    		LogUtils.warn(e);
    		UITools.errorMessage(TextUtils.format("reminder_script_error", e.toString(), node.getMap().getTitle(), node.getID()));
    	}
    }

    private void blink(final boolean stateAdded) {
        if (getNode().getMap() != Controller.getCurrentController().getMap()) {
            return;
        }
        displayState((stateAdded) ? ClockState.CLOCK_INVISIBLE : ClockState.CLOCK_VISIBLE, getNode(), true);
        if(! ResourceController.getResourceController().getBooleanProperty(ReminderHook.REMINDERS_BLINK))
            deactivateTimer();
    }

    private void displayStateIcon(final NodeModel parent, final ClockState state) {
        if (alreadyExecuted || !isAncestorNode(parent)) {
            return;
        }
        displayState(state, parent, true);
    }

    private void displayState(final ClockState stateAdded, final NodeModel pNode,
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
}