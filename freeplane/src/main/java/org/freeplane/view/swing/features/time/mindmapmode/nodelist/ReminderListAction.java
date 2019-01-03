package org.freeplane.view.swing.features.time.mindmapmode.nodelist;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;

public class ReminderListAction extends AFreeplaneAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	private final NodeList timeList;

	public ReminderListAction() {
		super("ReminderListAction");
		timeList = new NodeListWithReminders(NodeList.REMINDER_TEXT_WINDOW_TITLE,
			false, "timelistwindow.configuration");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		timeList.startup((node, reminder) -> reminder != null);
	}
}