package org.freeplane.api;

import java.util.Date;

/** Reminder: <code>node.reminder</code> - read-write. For creating and modifying reminders:
     * <pre>
     *  def reminder = node.reminder
     *  if (!reminder)
     *      c.statusInfo = "node has no reminder"
     *  else
     *      c.statusInfo = "node has a reminder: $reminder"
     *
     *  def inAMinute = new Date(System.currentTimeMillis() + 60*1000)
     *  node.reminder.createOrReplace(inAMinute, "WEEK", 2)
     *  if (node.map.file) {
     *      node.reminder.setScript("loadUri(new URI('${node.map.file.toURI()}#${node.id}'))")
     *  }
     *  // a click on the node opens time management dialog
     *  node.link.text = 'menuitem:_$ReminderListAction$0'
     * </pre> */
    public interface Reminder extends ReminderRO {
//        /** Creates a new reminder. Removes existing reminders for the same node if they exist.
//         * @param remindAt The timestamp when the reminder should fire. */
//        void createOrReplace(Date remindAt);
        /** Creates a periodic reminder. To make the reminder fire every second week:
         * <pre>
         *   node.reminder.createOrReplace(new Date() + 1, "WEEK", 2)
         * </pre>
         * @param remindAt The timestamp when the reminder fires first.
         * @param periodUnit one of ["MINUTE", "HOUR", "DAY", "WEEK", "MONTH", "YEAR"].
         * @param period counts the periodUnits. */
        void createOrReplace(Date remindAt, String periodUnit, Integer period);

        /** optional: a Groovy script to execute when the reminder fires.
         * @param scriptSource the script itself, not a path to a file.
         * @throws NullPointerException if there is no reminder yet. */
        void setScript(String scriptSource);

        /** removes a reminder from a node. It's not an error if there is no reminder to remove. */
        void remove();
    }