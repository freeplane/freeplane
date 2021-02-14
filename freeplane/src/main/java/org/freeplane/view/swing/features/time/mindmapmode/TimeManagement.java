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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.JComboBoxWithBorder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.components.calendar.JCalendar;
import org.freeplane.core.ui.components.calendar.JDayChooser;
import org.freeplane.core.ui.components.calendar.JTripleCalendar;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.format.FormatController;
import org.freeplane.features.format.FormattedDate;
import org.freeplane.features.format.PatternFormat;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.script.IScriptEditorStarter;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;

/**
 * @author foltin
 */
class TimeManagement implements PropertyChangeListener, IMapSelectionListener {

	class JTimePanel extends JPanel
	{
        private static final long serialVersionUID = 1L;
		private JButton setReminderButton;
		private JButton removeReminderButton;
		private JButton remindLaterButton;
		private PeriodPanel periodPanel;
		private ComboBoxEditor scriptEditor;
		private JCalendar calendarComponent;
		private JComboBox dateFormatChooser;

		public JTimePanel(boolean useTriple, int colCount) {
	        super();
	        init(useTriple, colCount);
	        final NodeModel selected = reminderHook.getModeController().getMapController().getSelectedNode();
	        update(selected);
        }

		public void update(NodeModel node){
			if(node == null)
				return;
			final ReminderExtension reminder = ReminderExtension.getExtension(node);
			final boolean reminderIsSet = reminder != null;
			removeReminderButton.setEnabled(reminderIsSet);
			if(reminderIsSet){
				final long reminderTime = reminder.getRemindUserAt();
				updateCalendar(reminderTime);
				periodPanel.setPeriod(reminder.getPeriod());
				periodPanel.setPeriodUnit(reminder.getPeriodUnit());
				if(scriptEditor != null)
					scriptEditor.setItem(reminder.getScript());
			}
			else{
				if(scriptEditor != null)
					scriptEditor.setItem(null);
			}
		}

		private void updateCalendar(final long reminderTime) {
	        TimeManagement.this.calendar.setTimeInMillis(reminderTime);
	        calendarComponent.setCalendar(TimeManagement.this.calendar);
	        dateFormatChooser.repaint();
        }

		private void init(boolean useTriple, int colCount) {
			final JComponent calendarContainer;
			if (useTriple) {
				final JTripleCalendar trippleCalendar = new JTripleCalendar();
				calendarComponent = trippleCalendar.getCalendar();
				calendarContainer = trippleCalendar;
			}
			else {
				calendarComponent = new JCalendar();
				calendarContainer = calendarComponent;
			}
			calendarComponent.setCalendar(TimeManagement.this.calendar);
			if (dialog != null) {
				dialog.addWindowFocusListener(new WindowAdapter() {
					@Override
					public void windowGainedFocus(WindowEvent e) {
						calendarComponent.getDayChooser().setFocus();
					}
				});
			}
			calendarComponent.setMaximumSize(calendarComponent.getPreferredSize());
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(Box.createHorizontalGlue());
			calendarComponent.getDayChooser().addPropertyChangeListener(TimeManagement.this);
			calendarContainer.setAlignmentX(0.5f);
			add(calendarContainer);

			DefaultFormBuilder btnBuilder = new DefaultFormBuilder(new FormLayout(FormSpecs.GROWING_BUTTON_COLSPEC.toString(), ""));
			 btnBuilder.getLayout().addGroupedColumn(btnBuilder.getColumnCount());
			 for(int i = 1; i< colCount; i++){
				 btnBuilder.appendRelatedComponentsGapColumn();
				 btnBuilder.appendColumn(FormSpecs.GROWING_BUTTON_COLSPEC);
				 btnBuilder.getLayout().addGroupedColumn(btnBuilder.getColumnCount());
			}

			{
				final JButton todayButton = new JButton(getResourceString("reminder.todayButton"));
				todayButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent arg0) {
						final Calendar currentTime = Calendar.getInstance();
						currentTime.set(Calendar.SECOND, 0);
						TimeManagement.this.calendar.setTimeInMillis(currentTime.getTimeInMillis());
						calendarComponent.setCalendar(TimeManagement.this.calendar);
					}
				});
				btnBuilder.append(todayButton);
			}
			{
				dateFormatChooser = createDateFormatChooser();
				btnBuilder.append(dateFormatChooser);
			}
			{
				final JButton appendButton = new JButton(getResourceString("reminder.appendButton"));
				if (dialog == null) {
					appendButton.setFocusable(false);
				}
				appendButton.addMouseListener(new MouseAdapter() {
					@Override
	                public void mouseClicked(MouseEvent e) {
						insertTime(dialog, appendButton);
					}
				});
				btnBuilder.append(appendButton);
			}
			{
				scriptEditor = null;
				IScriptEditorStarter editor = reminderHook.getModeController().getExtension(IScriptEditorStarter.class);
				if(editor != null){
					scriptEditor = editor.createComboBoxEditor(new Dimension(600, 400));
					Component scriptButton = scriptEditor.getEditorComponent();
					btnBuilder.append(scriptButton);
				}
			}
			{
				setReminderButton = new JButton(getResourceString("reminder.reminderButton"));
				setReminderButton.setToolTipText(getResourceString("reminder.reminderButton.tooltip"));
				setReminderButton.addMouseListener(new MouseAdapter() {
					@Override
	                public void mouseClicked(MouseEvent e) {
						addReminder();
					}
				});
				btnBuilder.append(setReminderButton);
			}
			{
				remindLaterButton = new JButton(
				    getResourceString("reminder.remindLaterButton"));
				remindLaterButton.setToolTipText(getResourceString("reminder.remindLaterButton.tooltip"));
				remindLaterButton.addMouseListener(new MouseAdapter() {

					@Override
	                public void mouseClicked(MouseEvent e) {
						remindLaterReminder();
	               }

				});
				btnBuilder.append(remindLaterButton);
			}
			{
				periodPanel = new PeriodPanel();
				btnBuilder.append(periodPanel);
			}
			{
				removeReminderButton = new JButton(
				    getResourceString("reminder.removeReminderButton"));
				removeReminderButton.setToolTipText(getResourceString("reminder.removeReminderButton.tooltip"));
				removeReminderButton.addMouseListener(new MouseAdapter() {

					@Override
	                public void mouseClicked(MouseEvent e) {
						removeReminder();
	               }

				});
				btnBuilder.append(removeReminderButton);
			}
			if (dialog != null) {
				final JButton cancelButton = new JButton(getResourceString("reminder.closeButton"));
				cancelButton.addMouseListener(new MouseAdapter() {

					@Override
	                public void mouseClicked(MouseEvent e) {
						disposeDialog();
					}
				});
				btnBuilder.append(cancelButton);
			}
			final JPanel btnPanel = btnBuilder.getPanel();
			btnPanel.setAlignmentX(CENTER_ALIGNMENT);
			add(btnPanel);
        }

		private void addReminder() {
			final Date date = getCalendarDate();
			String script = null;
			if(scriptEditor != null){
				script = (String) scriptEditor.getItem();
				if(script != null && "".equals(script.trim()))
					script = null;
			}
			Controller controller = Controller.getCurrentController();
			for (final NodeModel node : controller.getModeController().getMapController().getSelectedNodes()) {
				final ReminderExtension alreadyPresentHook = ReminderExtension.getExtension(node);
				if (alreadyPresentHook != null) {
					final long oldReminderTime = alreadyPresentHook.getRemindUserAt();
					if(oldReminderTime > System.currentTimeMillis()){
						final Object[] messageArguments = { new Date(oldReminderTime), date };
						final MessageFormat formatter = new MessageFormat(
							getResourceString("reminder.reminderNode_onlyOneDate"));
						final String message = formatter.format(messageArguments);
						final int result = JOptionPane.showConfirmDialog(controller.getViewController().getCurrentRootComponent(), message,
							"Freeplane", JOptionPane.YES_NO_OPTION);
						if (result == JOptionPane.NO_OPTION) {
							return;
						}
					}
					if(scriptEditor == null)
						script = alreadyPresentHook.getScript();
					reminderHook.undoableToggleHook(node);
				}
				final ReminderExtension reminderExtension = new ReminderExtension(reminderHook, node);
				reminderExtension.setRemindUserAt(date.getTime());
				reminderExtension.setPeriodUnit(periodPanel.getPeriodUnit());
				reminderExtension.setPeriod(periodPanel.getPeriod());
				reminderExtension.setScript(script);
				reminderHook.undoableActivateHook(node, reminderExtension);
			}
		}

		private void removeReminder() {
	        for (final NodeModel node : getMindMapController().getMapController().getSelectedNodes()) {
				final ReminderExtension alreadyPresentHook = ReminderExtension.getExtension(node);
				if (alreadyPresentHook != null) {
					reminderHook.undoableToggleHook(node);
				}
			}
	    }
		private void remindLaterReminder(){
			Date nextTime = periodPanel.calculateNextTime(calendar.getTime());
			updateCalendar(nextTime.getTime());
			addReminder();
		}
	}
	private Calendar calendar;
	public final static String REMINDER_HOOK_NAME = "plugins/TimeManagementReminder.xml";
	private static TimeManagement sCurrentlyOpenTimeManagement = null;
	private JDialog dialog;
	private final ReminderHook reminderHook;
	private PatternFormat dateFormat;
	private INodeChangeListener nodeChangeListener;
	private INodeSelectionListener nodeSelectionListener;

	public TimeManagement( final ReminderHook reminderHook) {
		this.reminderHook = reminderHook;
		Controller.getCurrentController().getMapViewManager().addMapSelectionListener(this);
	}


	@Override
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
		getMindMapController().getMapController().removeNodeSelectionListener(nodeSelectionListener);
		nodeSelectionListener = null;
		getMindMapController().getMapController().removeNodeChangeListener(nodeChangeListener);
		nodeChangeListener = null;
		dialog.setVisible(false);
		dialog.dispose();
		dialog = null;
		TimeManagement.sCurrentlyOpenTimeManagement = null;
	}

	private FormattedDate getCalendarDate() {
		return new FormattedDate(calendar.getTime(), dateFormat.getPattern());
	}

	private ModeController getMindMapController() {
		return Controller.getCurrentModeController();
	}

	private String getResourceString(final String string) {
		return TextUtils.getText(string);
	}


	@Override
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
		dialog = new JDialog(UITools.getCurrentFrame(), false /*not modal*/);
		final JTimePanel timePanel =createTimePanel(dialog, true, 4);
		nodeSelectionListener = new INodeSelectionListener() {
			@Override
			public void onSelect(NodeModel node) {
				timePanel.update(node);
			}

			@Override
			public void onDeselect(NodeModel node) {
			}
		};
		getMindMapController().getMapController().addNodeSelectionListener(nodeSelectionListener);
		nodeChangeListener = new INodeChangeListener() {
			@Override
			public void nodeChanged(NodeChangeEvent event) {
				final NodeModel node = event.getNode();
				if(event.getProperty().equals(ReminderExtension.class) && node.equals(getMindMapController().getMapController().getSelectedNode()))
						timePanel.update(node);
			}
		};
		getMindMapController().getMapController().addUINodeChangeListener(nodeChangeListener);

		dialog.setTitle(getResourceString("reminder.WindowTitle"));
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

			@Override
			public void actionPerformed(final ActionEvent arg0) {
				disposeDialog();
			}
		};
		UITools.addEscapeActionToDialog(dialog, action);
		dialog.setContentPane(timePanel);
		dialog.pack();
		UITools.setBounds(dialog, -1, -1, dialog.getWidth(), dialog.getHeight());
		dialog.setVisible(true);
	}

	public JTimePanel createTimePanel(final Dialog dialog, boolean useTriple, int colCount) {
		if (this.calendar == null) {
			this.calendar = Calendar.getInstance();
			this.calendar.set(Calendar.SECOND, 0);
			this.calendar.set(Calendar.MILLISECOND, 0);
		}
		JTimePanel contentPane = new JTimePanel(useTriple, colCount);
		return contentPane;
	}

	private JComboBox createDateFormatChooser() {
		class DateFormatComboBoxElement {
			private final PatternFormat dateFormat;

			DateFormatComboBoxElement(PatternFormat dateFormat) {
				this.dateFormat = dateFormat;
			}

			PatternFormat getDateFormat() {
				return dateFormat;
			}

			@Override
			public String toString() {
				return dateFormat.formatObject(getCalendarDate()).toString();
			}
		}
		final String dateFormatPattern = ResourceController.getResourceController().getProperty(
		    "date_format");
		final Vector<DateFormatComboBoxElement> values = new Vector<DateFormatComboBoxElement>();
		final List<PatternFormat> datePatterns = FormatController.getController().getDateFormats();
		int selectedIndex = 0;
		for (int i = 0; i < datePatterns.size(); ++i) {
			final PatternFormat patternFormat = datePatterns.get(i);
			values.add(new DateFormatComboBoxElement(patternFormat));
			if (patternFormat.getPattern().equals(dateFormatPattern)) {
				selectedIndex = i;
			}
		}
		if (!datePatterns.isEmpty()){
			dateFormat = datePatterns.get(selectedIndex);
		}
		final JComboBox dateFormatChooser = new JComboBoxWithBorder(values);
		dateFormatChooser.setFocusable(false);
		if (!datePatterns.isEmpty()){
			dateFormatChooser.setSelectedIndex(selectedIndex);
		}
		dateFormatChooser.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				dateFormat = ((DateFormatComboBoxElement) e.getItem()).getDateFormat();
				final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
				if(focusOwner instanceof JTable){
					JTable table = (JTable) focusOwner;
					final int[] selectedRows = table.getSelectedRows();
					final int[] selectedColumns = table.getSelectedColumns();
					for(int r : selectedRows)
						for(int c : selectedColumns){
							Object date = table.getValueAt(r, c);
							if(date instanceof FormattedDate){
								final FormattedDate fd = (FormattedDate) date;
								if(! fd.getDateFormat().equals(dateFormat)){
									table.setValueAt(new FormattedDate(fd, dateFormat.getPattern()), r, c);
								}
							}
						}
				}
				else{
					ModeController mController = Controller.getCurrentModeController();
					for (final NodeModel node : mController.getMapController().getSelectedNodes()) {
						final MTextController textController = (MTextController) TextController.getController();
						Object date = node.getUserObject();
						if(date instanceof FormattedDate){
							final FormattedDate fd = (FormattedDate) date;
							if(! fd.getDateFormat().equals(dateFormat)){
								textController.setNodeObject(node, new FormattedDate(fd, dateFormat.getPattern()));
							}
						}
					}
				}

			}
		});
		dateFormatChooser.setAlignmentX(Component.LEFT_ALIGNMENT);
		return dateFormatChooser;
	}

	void insertTime(final Dialog dialog, final JButton appendButton) {
	    FormattedDate date = getCalendarDate();
	    final String dateAsString = dateFormat.formatObject(date).toString();
	    final Window parentWindow;
	    if (dialog != null) {
	    	parentWindow = (Window) dialog.getParent();
	    }
	    else {
	    	parentWindow = SwingUtilities.getWindowAncestor(appendButton);
	    }
	    final Component mostRecentFocusOwner = parentWindow.getMostRecentFocusOwner();
		if (mostRecentFocusOwner instanceof JTextComponent
		        && !(mostRecentFocusOwner.getClass().getName().contains("JSpinField"))) {
	    	final JTextComponent textComponent = (JTextComponent) mostRecentFocusOwner;
	    	textComponent.replaceSelection(dateAsString);
	    	return;
	    }
	    if(mostRecentFocusOwner instanceof JTable){
	    	JTable table = (JTable) mostRecentFocusOwner;
	    	final int[] selectedRows = table.getSelectedRows();
	    	final int[] selectedColumns = table.getSelectedColumns();
	    	for(int r : selectedRows)
	    		for(int c : selectedColumns)
	    			table.setValueAt(date, r, c);
	    }
	    else{
	    	ModeController mController = Controller.getCurrentModeController();
	    	for (final NodeModel node : mController.getMapController().getSelectedNodes()) {
	    		final MTextController textController = (MTextController) TextController.getController();
	    		textController.setNodeObject(node, date);
	    	}
	    }
    }
}
