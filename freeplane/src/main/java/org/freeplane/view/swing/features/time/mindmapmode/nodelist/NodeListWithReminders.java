package org.freeplane.view.swing.features.time.mindmapmode.nodelist;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.features.time.mindmapmode.ReminderExtension;
import org.freeplane.view.swing.features.time.mindmapmode.ReminderHook;

public class NodeListWithReminders extends NodeList{
	public NodeListWithReminders(String windowTitle, boolean searchInAllMaps,
	                             String windowPreferenceStorageProperty) {
		super(windowTitle, searchInAllMaps, windowPreferenceStorageProperty);
	}

	@Override
	protected void createSpecificButtons(final Container container) {
		final AbstractAction runAllAction = new AbstractAction(TextUtils
			.getText("reminder.Run_All")) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent arg0) {
				runScripts(false);
			}
		};
		final JButton runAllButton = new JButton(runAllAction);
		final AbstractAction runSelectedAction = new AbstractAction(TextUtils
			.getText("reminder.Run_Selected")) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent arg0) {
				runScripts(true);
			}
		};
		final JButton runSelectedButton = new JButton(runSelectedAction);
		runSelectedAction.setEnabled(false);
		final AbstractAction removeAllAction = new AbstractAction(TextUtils
			.getText("reminder.Remove_All")) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent arg0) {
				removeReminders(false);
				disposeDialog();
			}
		};
		final JButton removeAllButton = new JButton(removeAllAction);
		final AbstractAction removeSelectedAction = new AbstractAction(TextUtils
			.getText("reminder.Remove_Selected")) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent arg0) {
				removeReminders(true);
			}
		};
		final JButton removeSelectedButton = new JButton(removeSelectedAction);
		removeSelectedAction.setEnabled(false);
		final ListSelectionModel rowSM1 = tableView.getSelectionModel();
		rowSM1.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				final ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				final boolean enable = !(lsm.isSelectionEmpty());
				runSelectedAction.setEnabled(enable);
				removeSelectedAction.setEnabled(enable);
			}
		});
		Component[] components = new Component[] {runAllButton, runSelectedButton, removeAllButton, removeSelectedButton};
		for(Component c : components)
			container.add(c);
	}


	private interface ReminderAction{
		void apply(ReminderHook controller, NodeModel node, ReminderExtension reminder, int row);
	}
	private void runScripts(boolean selectedOnly) {
		apply(selectedOnly,	this::runScript);
	}

	private void runScript(ReminderHook c, NodeModel n, ReminderExtension r, int row) {
		c.runScript(r);
	}
	private void removeReminders(boolean selectedOnly) {
		apply(selectedOnly,	this::removeReminder);
	}

	private void removeReminder(ReminderHook reminderHook, NodeModel node, ReminderExtension reminder, int row) {
		reminderHook.undoableDeactivateHook(node);
		tableView.setValueAt(null, row, nodeReminderColumn);
	}

	private void apply(boolean selectedOnly, ReminderAction action) {
		final ReminderHook reminderController = Controller.getCurrentModeController().getExtension(ReminderHook.class);
		for (int row = 0; row < tableView.getRowCount(); row++) {
			if(! selectedOnly || tableView.isRowSelected(row)) {
				final NodeModel node = getMindMapNode(row);
				final ReminderExtension reminder = node.getExtension(ReminderExtension.class);
				if(reminder != null) {
					action.apply(reminderController, node, reminder, row);
				}
			}
		}
	}

}
