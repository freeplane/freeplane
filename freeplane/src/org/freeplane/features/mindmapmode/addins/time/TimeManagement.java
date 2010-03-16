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
package org.freeplane.features.mindmapmode.addins.time;

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
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.IMapSelectionListener;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlTools;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.text.TextController;
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
			for (final Iterator i = timeManagement.getMindMapController().getMapController().getSelectedNodes()
			    .iterator(); i.hasNext();) {
				final NodeModel node = (NodeModel) i.next();
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
	private JTripleCalendar calendar;
	final private Controller controller;
	private JDialog dialog;
	private JTextField hourField;
	private ModeController mController;
	private JTextField minuteField;
	final private ModeController modeController;
	private final ReminderHook reminderHook;
	private JPanel timePanel;

	public TimeManagement(final ModeController modeController, final ReminderHook reminderHook) {
		this.modeController = modeController;
		controller = modeController.getController();
		this.reminderHook = reminderHook;
		controller.getMapViewManager().addMapSelectionListener(this);
	}

	public void actionPerformed(final ActionEvent arg0) {
		final Date date = getCalendarDate();
		for (final Iterator i = mController.getMapController().getSelectedNodes().iterator(); i.hasNext();) {
			final NodeModel node = (NodeModel) i.next();
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
		try {
			int value = 0;
			value = Integer.parseInt(hourField.getText());
			cal.set(Calendar.HOUR_OF_DAY, value);
			value = Integer.parseInt(minuteField.getText());
			cal.set(Calendar.MINUTE, value);
			cal.set(Calendar.SECOND, 0);
		}
		catch (final Exception e) {
		    LogTool.warn(e);
		}
		return cal.getTime();
	}

	private ModeController getMindMapController() {
		return modeController;
	}

	private String getResourceString(final String string) {
		return ResourceBundles.getText(string);
	}

	/**
	 */
	private JPanel getTimePanel() {
		if (timePanel == null) {
			timePanel = new JPanel();
			timePanel.setLayout(new GridBagLayout());
			{
				final GridBagConstraints gb2 = new GridBagConstraints();
				gb2.gridx = 0;
				gb2.gridy = 0;
				gb2.fill = GridBagConstraints.HORIZONTAL;
				timePanel.add(new JLabel(getResourceString("plugins/TimeManagement.xml_hour")), gb2);
			}
			{
				final GridBagConstraints gb2 = new GridBagConstraints();
				gb2.gridx = 1;
				gb2.gridy = 0;
				gb2.fill = GridBagConstraints.HORIZONTAL;
				hourField = new JTextField(2);
				hourField.setText(new Integer(calendar.getCalendar().get(Calendar.HOUR_OF_DAY)).toString());
				timePanel.add(hourField, gb2);
			}
			{
				final GridBagConstraints gb2 = new GridBagConstraints();
				gb2.gridx = 2;
				gb2.gridy = 0;
				gb2.fill = GridBagConstraints.HORIZONTAL;
				timePanel.add(new JLabel(getResourceString("plugins/TimeManagement.xml_minute")), gb2);
			}
			{
				final GridBagConstraints gb2 = new GridBagConstraints();
				gb2.gridx = 3;
				gb2.gridy = 0;
				gb2.fill = GridBagConstraints.HORIZONTAL;
				minuteField = new JTextField(2);
				String minuteString = new Integer(calendar.getCalendar().get(Calendar.MINUTE)).toString();
				if (minuteString.length() < 2) {
					minuteString = "0" + minuteString;
				}
				minuteField.setText(minuteString);
				timePanel.add(minuteField, gb2);
			}
		}
		return timePanel;
	}

	public void propertyChange(final PropertyChangeEvent event) {
		if (event.getPropertyName().equals(JDayChooser.DAY_PROPERTY)) {
		}
	}

	public void startup() {
		if (TimeManagement.sCurrentlyOpenTimeManagement != null) {
			TimeManagement.sCurrentlyOpenTimeManagement.dialog.getContentPane().setVisible(true);
			return;
		}
		TimeManagement.sCurrentlyOpenTimeManagement = this;
		mController = getMindMapController();
		dialog = new JDialog(controller.getViewController().getFrame(), false /*not modal*/);
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
		calendar = new JTripleCalendar();
		final Container contentPane = dialog.getContentPane();
		contentPane.setLayout(new GridBagLayout());
		final GridBagConstraints gb1 = new GridBagConstraints();
		gb1.gridx = 0;
		gb1.gridwidth = 4;
		gb1.fill = GridBagConstraints.BOTH;
		gb1.weighty = 1;
		gb1.gridy = 0;
		calendar.getDayChooser().addPropertyChangeListener(this);
		contentPane.add(calendar, gb1);
		{
			final GridBagConstraints gb2 = new GridBagConstraints();
			gb2.gridx = 0;
			gb2.gridy = 1;
			gb2.gridwidth = 4;
			gb2.fill = GridBagConstraints.HORIZONTAL;
			contentPane.add(getTimePanel(), gb2);
		}
		{
			final GridBagConstraints gb2 = new GridBagConstraints();
			gb2.gridx = 0;
			gb2.gridy = 2;
			gb2.fill = GridBagConstraints.HORIZONTAL;
			final JButton appendButton = new JButton(getResourceString("plugins/TimeManagement.xml_appendButton"));
			appendButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent arg0) {
					final DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
					final String dateAsString = df.format(getCalendarDate());
					final Window parentWindow = (Window) dialog.getParent();
					final Component mostRecentFocusOwner = parentWindow.getMostRecentFocusOwner();
					if(mostRecentFocusOwner instanceof JTextComponent){
						JTextComponent text = (JTextComponent) mostRecentFocusOwner;
						text.replaceSelection(dateAsString);
						return;
					}
					for (final Iterator i = mController.getMapController().getSelectedNodes().iterator(); i.hasNext();) {
						final NodeModel element = (NodeModel) i.next();
						final String text = element.getText();
						final StringBuilder newText = new StringBuilder();
						if (HtmlTools.isHtmlNode(text)){
							int bodyEndPos = HtmlTools.endOfText(text);
							newText.append(text.substring(0, bodyEndPos));
							newText.append("<p>");
							newText.append(dateAsString);
							newText.append("</p>");
							newText.append(text.substring(bodyEndPos));
						}
						else{
							newText.append(text);
							newText.append(" ");
							newText.append(dateAsString);
						}
						((MTextController) TextController.getController(mController)).setNodeText(element, newText.toString());
					}
				}
			});
			contentPane.add(appendButton, gb2);
		}
		{
			final GridBagConstraints gb2 = new GridBagConstraints();
			gb2.gridx = 1;
			gb2.gridy = 2;
			gb2.fill = GridBagConstraints.HORIZONTAL;
			final JButton reminderButton = new JButton(getResourceString("plugins/TimeManagement.xml_reminderButton"));
			reminderButton.setToolTipText(getResourceString("plugins/TimeManagement.xml_reminderButton_tooltip"));
			reminderButton.addActionListener(this);
			contentPane.add(reminderButton, gb2);
		}
		{
			final GridBagConstraints gb2 = new GridBagConstraints();
			gb2.gridx = 2;
			gb2.gridy = 2;
			gb2.fill = GridBagConstraints.HORIZONTAL;
			final JButton reminderButton = new JButton(
			    getResourceString("plugins/TimeManagement.xml_removeReminderButton"));
			reminderButton.setToolTipText(getResourceString("plugins/TimeManagement.xml_removeReminderButton_tooltip"));
			reminderButton.addActionListener(new RemoveReminders(this));
			contentPane.add(reminderButton, gb2);
		}
		{
			final GridBagConstraints gb2 = new GridBagConstraints();
			gb2.gridx = 3;
			gb2.gridy = 2;
			gb2.fill = GridBagConstraints.HORIZONTAL;
			final JButton todayButton = new JButton(getResourceString("plugins/TimeManagement.xml_todayButton"));
			todayButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent arg0) {
					calendar.setCalendar(Calendar.getInstance());
				}
			});
			contentPane.add(todayButton, gb2);
		}
		{
			final GridBagConstraints gb2 = new GridBagConstraints();
			gb2.gridx = 4;
			gb2.gridy = 2;
			gb2.fill = GridBagConstraints.HORIZONTAL;
			final JButton cancelButton = new JButton(getResourceString("plugins/TimeManagement.xml_closeButton"));
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent arg0) {
					disposeDialog();
				}
			});
			contentPane.add(cancelButton, gb2);
		}
		if (TimeManagement.lastDate != null) {
			calendar.setDate(TimeManagement.lastDate);
		}
		dialog.pack();
		UITools.setBounds(dialog, -1, -1, dialog.getWidth(), dialog.getHeight());
		calendar.getDayChooser().setFocus();
		dialog.setVisible(true);
	}
}
