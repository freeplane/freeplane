package org.freeplane.plugin.script.proxy;

import java.util.Date;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.script.ScriptExecution;
import org.freeplane.view.swing.features.time.mindmapmode.ReminderExtension;
import org.freeplane.view.swing.features.time.mindmapmode.ReminderHook;

public class ReminderProxy extends AbstractProxy<NodeModel> implements Proxy.Reminder {
    ReminderProxy(final NodeModel delegate, final ScriptExecution scriptExecution) {
        super(delegate, scriptExecution);
    }

    // ReminderRO
    public Date getRemindAt() {
        final ReminderExtension extension = getDelegate().getExtension(ReminderExtension.class);
        if (extension == null)
            return null;
        return new Date(extension.getRemindUserAt());
    }

    // ReminderRO
    public String getPeriodUnit() {
        final ReminderExtension extension = getDelegate().getExtension(ReminderExtension.class);
        if (extension == null || extension.getPeriodUnit() == null)
            return null;
        return extension.getPeriodUnitAsString();
    }

    // ReminderRO
    public Integer getPeriod() {
        final ReminderExtension extension = getDelegate().getExtension(ReminderExtension.class);
        if (extension == null)
            return null;
        return extension.getPeriod();
    }

    // ReminderRO
    public String getScript() {
        final ReminderExtension extension = getDelegate().getExtension(ReminderExtension.class);
        if (extension == null)
            return null;
        return extension.getScript();
    }

//    // Reminder
//    public void createOrReplace(Date remindAt) {
//        final ReminderHook reminderHook = Controller.getCurrentModeController().getExtension(ReminderHook.class);
//        removeOldReminder(reminderHook);
//        final ReminderExtension reminder = newReminder(remindAt);
//        reminderHook.add(getDelegate(), reminder);
//    }

    private void removeOldReminder(final ReminderHook reminderHook) {
        final ReminderExtension oldReminder = getDelegate().getExtension(ReminderExtension.class);
        if (oldReminder != null) {
            reminderHook.remove(getDelegate(), oldReminder);
        }
    }

    // Reminder
    public void createOrReplace(Date remindAt, String periodUnit, Integer period) {
        final ReminderHook reminderHook = Controller.getCurrentModeController().getExtension(ReminderHook.class);
        removeOldReminder(reminderHook);
        final ReminderExtension reminder = newReminder(remindAt);
        reminder.setPeriodUnitAsString(periodUnit);
        reminder.setPeriod(period);
        reminderHook.add(getDelegate(), reminder);
    }

    private ReminderExtension newReminder(Date remindAt) {
        final ReminderExtension reminder = new ReminderExtension(getDelegate());
        reminder.setRemindUserAt(remindAt.getTime());
        return reminder;
    }

    // Reminder
    public void setScript(String scriptSource) {
        final ReminderExtension reminder = getDelegate().getExtension(ReminderExtension.class);
        if (reminder == null) 
            throw new NullPointerException("no reminder defined for node " + this);
        reminder.setScript(scriptSource);
    }

    // Reminder
    public void remove() {
        final ReminderHook reminderHook = Controller.getCurrentModeController().getExtension(ReminderHook.class);
        removeOldReminder(reminderHook);
    }

    /** make <code>if (node.reminder) println "has reminder"</code> work. */
    public boolean asBoolean() {
        return getDelegate().getExtension(ReminderExtension.class) != null;
    }

    @Override
    public String toString() {
        final ReminderExtension reminder = getDelegate().getExtension(ReminderExtension.class);
        if (reminder == null) {
            return "no reminder";
        }
        else {
            return "periodic reminder fires at " + reminder.getRemindUserAt() + " and then every "
                    + reminder.getPeriod() + " " + reminder.getPeriodUnit();
        }
    }
}
