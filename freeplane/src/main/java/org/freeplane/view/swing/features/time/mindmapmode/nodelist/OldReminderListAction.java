package org.freeplane.view.swing.features.time.mindmapmode.nodelist;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;

public class OldReminderListAction extends AFreeplaneAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	private final NodeList timeList;

	public OldReminderListAction() {
		super("OldReminderListAction");
		timeList = new NodeListWithReminders(NodeList.PAST_REMINDERS_TEXT_WINDOW_TITLE,
			true, "allmaps.timelistwindow.configuration");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		long currentTimeMillis = System.currentTimeMillis();
		timeList.startup((node, reminder) -> reminder != null && reminder.getRemindUserAt() <= currentTimeMillis);
	}
}