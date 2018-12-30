package org.freeplane.view.swing.features.time.mindmapmode.nodelist;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;

public class TimeListAction extends AFreeplaneAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	private final NodeList timeList;

	public TimeListAction() {
		super("TimeListAction");
		timeList = new NodeListWithReminders(NodeList.PLUGINS_TIME_MANAGEMENT_XML_WINDOW_TITLE,
			(node, reminder) -> reminder != null,
			false, "timelistwindow.configuration");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		timeList.startup();
	}
}