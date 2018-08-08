package org.freeplane.api;

import java.util.Date;

/** Reminder: <code>node.reminder</code> - read-only.
 * <pre>
 *  def rem = node.reminder
 *  if (!rem.remindAt)
 *      c.statusInfo = "this node has no reminder"
 *  else
 *      c.statusInfo = "reminder fires at ${rem.remindAt} and then every ${rem.period} ${rem.periodUnit}"
 * </pre> */
public interface ReminderRO {
    /** The timestamp when the reminder fires first. */
    Date getRemindAt();
    /** One of ["MINUTE", "HOUR", "DAY", "WEEK", "MONTH", "YEAR"]. */
    String getPeriodUnit();
    /** Count in units of "PeriodUnit". (period=2, periodUnit="WEEK") reminds every two weeks. */
    Integer getPeriod();
    /** optional: a Groovy script to execute when the reminder fires. */
    String getScript();
}