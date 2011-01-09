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
package org.freeplane.features.mindmapmode.time;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.IMapSelectionListener;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.common.time.swing.JCalendar;
import org.freeplane.features.common.time.swing.JDayChooser;
import org.freeplane.features.mindmapmode.text.MTextController;

/**
 * @author foltin
 */
class TimeManagement implements PropertyChangeListener, ActionListener, IMapSelectionListener {
	private class RemoveReminders implements ActionListener {
		/**
		 *
		 */
		private final TimeManagement timeManagement;

		/**
		 * @param timeManagement
		 */
		RemoveReminders(final TimeManagement timeManagement) {
			this.timeManagement = timeManagement;
		}

		public void actionPerformed(final ActionEvent e) {
			for (final NodeModel node : timeManagement.getMindMapController().getMapController().getSelectedNodes()) {
				final ReminderExtension alreadyPresentHook = ReminderExtension.getExtension(node);
				if (alreadyPresentHook != null) {
					reminderHook.undoableToggleHook(node);
				}
			}
		}
	}

	private static Date lastDate = null;
	public final static String REMINDER_HOOK_NAME = "plugins/TimeManagementReminder.xml";
	private static TimeManagement sCurrentlyOpenTimeManagement = null;
	private JCalendar calendar;
// // 	final private Controller controller;
	private JDialog dialog;
// 	final private ModeController modeController;
	private final ReminderHook reminderHook;

	public TimeManagement( final ReminderHook reminderHook) {
//		this.modeController = modeController;
//		controller = modeController.getController();
		this.reminderHook = reminderHook;
		Controller.getCurrentController().getMapViewManager().addMapSelectionListener(this);
	}

	public void actionPerformed(final ActionEvent arg0) {
		final Date date = getCalendarDate();
		Controller controller = Controller.getCurrentController();
		for (final NodeModel node : controller.getModeController().getMapController().getSelectedNodes()) {
			final ReminderExtension alreadyPresentHook = ReminderExtension.getExtension(node);
			if (alreadyPresentHook != null) {
				final Object[] messageArguments = { new Date(alreadyPresentHook.getRemindUserAt()), date };
				final MessageFormat formatter = new MessageFormat(
				    getResourceString("plugins/TimeManagement.xml_reminderNode_onlyOneDate"));
				final String message = formatter.format(messageArguments);
				final int result = JOptionPane.showConfirmDialog(controller.getViewController().getFrame(), message,
				    "Freeplane", JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.NO_OPTION) {
					return;
				}
				reminderHook.undoableToggleHook(node);
			}
			final ReminderExtension reminderExtension = new ReminderExtension(reminderHook, node);
			reminderExtension.setRemindUserAt(date.getTime());
			reminderHook.undoableActivateHook(node, reminderExtension);
		}
	}

	public void afterMapChange(final MapModel oldMap, final MapModel newMap) {
	}

	public void afterMapClose(final MapModel oldMap) {
	}

	public void beforeMapChange(final MapModel oldMap, final MapModel newMap) {
		disposeDialog();
	}

	/**
	 *
	 */
	private void disposeDialog() {
		if (dialog == null) {
			return;
		}
		dialog.setVisible(false);
		dialog.dispose();
		dialog = null;
		TimeManagement.lastDate = getCalendarDate();
		TimeManagement.sCurrentlyOpenTimeManagement = null;
	}

	/**
	 */
	private Date getCalendarDate() {
		final Calendar cal = calendar.getCalendar();
		return cal.getTime();
	}

	private ModeController getMindMapController() {
		return Controller.getCurrentModeController();
	}

	private String getResourceString(final String string) {
		return TextUtils.getText(string);
	}


	public void propertyChange(final PropertyChangeEvent event) {
		if (event.getPropertyName().equals(JDayChooser.DAY_PROPERTY)) {
		}
	}

	void showDialog() {
		if (TimeManagement.sCurrentlyOpenTimeManagement != null) {
			TimeManagement.sCurrentlyOpenTimeManagement.dialog.getContentPane().setVisible(true);
			return;
		}
		TimeManagement.sCurrentlyOpenTimeManagement = this;
		dialog = new JDialog(Controller.getCurrentController().getViewController().getFrame(), false /*not modal*/);
		dialog.setTitle(getResourceString("plugins/TimeManagement.xml_WindowTitle"));
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent event) {
				disposeDialog();
			}
		});
		final Action action = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent arg0) {
				disposeDialog();
			}
		};
		UITools.addEscapeActionToDialog(dialog, action);
		final Container contentPane = dialog.getContentPane();
		init(contentPane, true, BoxLayout.X_AXIS);
		dialog.pack();
		UITools.setBounds(dialog, -1, -1, dialog.getWidth(), dialog.getHeight());
		calendar.getDayChooser().setFocus();
		dialog.setVisible(true);
	}

	public void init(final Container contentPane, boolean useTripple, int axis) {
		final JComponent calendarComponent;
		if(useTripple){
			final JTripleCalendar trippleCalendar = new JTripleCalendar();
			calendar = trippleCalendar.getCalendar();
			calendarComponent = trippleCalendar;
		}
		else{
			calendar = new JCalendar();
			calendarComponent = calendar;
		}
		setCurrentTime();
	    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		calendar.getDayChooser().addPropertyChangeListener(this);
		calendarComponent.setAlignmentX(0.5f);
		contentPane.add(calendarComponent);
		
		Box buttons = new Box(axis);
		buttons.setAlignmentX(0.5f);
		contentPane.add(buttons);
		{
			final JButton appendButton = new JButton(getResourceString("plugins/TimeManagement.xml_appendButton"));
			appendButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent arg0) {
					final DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
					final String dateAsString = df.format(getCalendarDate());
					if(dialog != null){
						final Window parentWindow = (Window) dialog.getParent();
						final Component mostRecentFocusOwner = parentWindow.getMostRecentFocusOwner();
						if (mostRecentFocusOwner instanceof JTextComponent) {
							final JTextComponent text = (JTextComponent) mostRecentFocusOwner;
							text.replaceSelection(dateAsString);
							return;
						}
					}
					ModeController mController  = Controller.getCurrentModeController();
					for (final NodeModel element : mController .getMapController().getSelectedNodes()) {
						final String text = element.getText();
						final StringBuilder newText = new StringBuilder();
						if (HtmlUtils.isHtmlNode(text)) {
							final int bodyEndPos = HtmlUtils.endOfText(text);
							newText.append(text.substring(0, bodyEndPos));
							newText.append("<p>");
							newText.append(dateAsString);
							newText.append("</p>");
							newText.append(text.substring(bodyEndPos));
						}
						else {
							newText.append(text);
							newText.append(" ");
							newText.append(dateAsString);
						}
						((MTextController) TextController.getController()).setNodeText(element, newText
						    .toString());
					}
				}
			});
			appendButton.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
			buttons.add(appendButton);
		}
		{
			final JButton reminderButton = new JButton(getResourceString("plugins/TimeManagement.xml_reminderButton"));
			reminderButton.setToolTipText(getResourceString("plugins/TimeManagement.xml_reminderButton_tooltip"));
			reminderButton.addActionListener(this);
			reminderButton.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
			buttons.add(reminderButton);
		}
		{
			final JButton reminderButton = new JButton(
			    getResourceString("plugins/TimeManagement.xml_removeReminderButton"));
			reminderButton.setToolTipText(getResourceString("plugins/TimeManagement.xml_removeReminderButton_tooltip"));
			reminderButton.addActionListener(new RemoveReminders(this));
			reminderButton.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
			buttons.add(reminderButton);
		}
		{
			final JButton todayButton = new JButton(getResourceString("plugins/TimeManagement.xml_todayButton"));
			todayButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent arg0) {
					setCurrentTime();
				}
			});
			todayButton.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
			buttons.add(todayButton);
		}
		{
			final JButton cancelButton = new JButton(getResourceString("plugins/TimeManagement.xml_closeButton"));
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent arg0) {
					disposeDialog();
				}
			});
			cancelButton.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
			buttons.add(cancelButton);
		}
		if (TimeManagement.lastDate != null) {
			calendar.setDate(TimeManagement.lastDate);
		}
    }

 void setCurrentTime() {
	    final Calendar calender = Calendar.getInstance();
	    calender.set(Calendar.SECOND, 0);
	    calendar.setCalendar(calender);
    }
}
