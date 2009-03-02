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

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;

import org.freeplane.core.addins.NodeHookDescriptor;
import org.freeplane.core.addins.PersistentNodeHook;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MindIcon;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.FreeplaneResourceBundle;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionDescriptor;
import org.freeplane.core.ui.VisibleAction;
import org.freeplane.n3.nanoxml.IXMLElement;

/**
 * @author foltin
 */
@NodeHookDescriptor(hookName = "plugins/TimeManagementReminder.xml", onceForMap = false)
@ActionDescriptor(name = "plugins/RemoveReminder.xml_name", //
tooltip = "accessories/plugins/RevisionPlugin.properties_documentation", //
locations = { "/menu_bar/extras/first/time_management" })
public class ReminderHook extends PersistentNodeHook {
	private static ImageIcon bellIcon;
	static final int CLOCK_INVISIBLE = 0;
	static final int CLOCK_VISIBLE = 1;
	private static ImageIcon clockIcon = null;
	private static ImageIcon flagIcon;
	static final String PLUGIN_LABEL = "plugins/TimeManagementReminder.xml";
	static final String REMINDUSERAT = "REMINDUSERAT";
	private static final int REMOVE_CLOCK = -1;
	final private String STATE_TOOLTIP = TimerBlinkTask.class.getName() + "_STATE_";
	
//******************************************
	
	void blink(ReminderExtension model, final boolean stateAdded) {
		model.setRemindUserAt(System.currentTimeMillis() + 3000);
		scheduleTimer(model, new TimerBlinkTask(this, model, stateAdded));
		displayState(model, (stateAdded) ? CLOCK_VISIBLE : CLOCK_INVISIBLE, model.getNode(), true);
	}

	void deactivate(ReminderExtension model) {
		setToolTip(model.getNode(), null);
		if (model.getTimer() != null) {
			model.getTimer().cancel();
		}
		displayState(model, REMOVE_CLOCK, model.getNode(), true);
	}

	void displayState(ReminderExtension model, final int stateAdded, final NodeModel pNode, final boolean recurse) {
		ImageIcon icon = null;
		if (stateAdded == CLOCK_VISIBLE) {
			icon = getClockIcon();
		}
		else if (stateAdded == CLOCK_INVISIBLE) {
			if (pNode == model.getNode()) {
				icon = getBellIcon();
			}
			else {
				icon = getFlagIcon();
			}
		}
		pNode.setStateIcon(getStateKey(), icon);
		getModeController().getMapController().nodeRefresh(pNode);
		if (recurse && !pNode.isRoot()) {
			displayState(model, stateAdded, pNode.getParentNode(), recurse);
		}
	}

	private ImageIcon getBellIcon() {
		if (bellIcon == null) {
			bellIcon = MindIcon.factory("bell").getIcon();
		}
		return bellIcon;
	}

	private ImageIcon getClockIcon() {
		if (clockIcon == null) {
			clockIcon = MindIcon.factory("clock2").getIcon();
		}
		return clockIcon;
	}

	private ImageIcon getFlagIcon() {
		if (flagIcon == null) {
			flagIcon = MindIcon.factory("flag").getIcon();
		}
		return flagIcon;
	}

	String getStateKey() {
		return STATE_TOOLTIP;
	}

	public void scheduleTimer(ReminderExtension model) {
		scheduleTimer(model, new TimerBlinkTask(this, model, false));
	}

	void scheduleTimer(ReminderExtension model, final TimerTask task) {
		model.setTimer(new Timer());
		final Date date = new Date(model.getRemindUserAt());
		model.getTimer().schedule(task, date);
		final Object[] messageArguments = { date };
		final MessageFormat formatter = new MessageFormat(FreeplaneResourceBundle
		    .getText("plugins/TimeManagement.xml_reminderNode_tooltip"));
		final String message = formatter.format(messageArguments);
		setToolTip(model.getNode(), message);
		displayState(model, CLOCK_VISIBLE, model.getNode(), false);
	}

	protected void setToolTip(final NodeModel node, final String value) {
		(getModeController().getMapController()).setToolTip(node, getClass().getName(), value);
	}

//******************************************	
	@VisibleAction(checkOnNodeChange = true)
	private class HideableAction extends HookAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2103302242985377540L;

		public HideableAction() {
			super();
		}

		@Override
		public void setVisible() {
			setVisible(isActiveForSelection());
		}

		public String getName() {
			return getClass().getSimpleName();
		}
	}

	@ActionDescriptor(name = "plugins/NodeList.xml_name", //
	locations = { "/menu_bar/edit/find" }, //
	keyStroke = "keystroke_plugins/TimeList.xml_key", //
	tooltip = "plugins/NodeList.xml_documentation")
	static private class NodeListAction extends AFreeplaneAction {
		private static final long serialVersionUID = -4589651186325844658L;
		private final TimeList timeList;

		public NodeListAction(final ModeController modeController) {
			super(modeController.getController(), NodeListAction.class.getAnnotation(ActionDescriptor.class));
			timeList = new TimeList(modeController, true);
		}

		public void actionPerformed(final ActionEvent e) {
			timeList.startup();
		}
		// TODO rladstaetter 15.02.2009 remove name attribute from ActionDescriptor
		public String getName() {
			return "plugins/NodeList.xml_name";
		}
	}

	@ActionDescriptor(name = "plugins/TimeList.xml_name", //
	locations = { "/menu_bar/extras/first/time_management" }, //
	tooltip = "plugins/TimeList.xml_documentation")
	static private class TimeListAction extends AFreeplaneAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7761576994743050649L;
		private final TimeList timeList;

		public TimeListAction(final ModeController modeController) {
			super(modeController.getController(), TimeListAction.class.getAnnotation(ActionDescriptor.class));
			timeList = new TimeList(modeController, false);
		}

		public void actionPerformed(final ActionEvent e) {
			timeList.startup();
		}

		public String getName() {
			return "plugins/TimeList.xml_name";
		}
	}

	@ActionDescriptor(name = "plugins/TimeManagement.xml_name", //
	locations = { "/menu_bar/extras/first/time_management" }, //
	keyStroke = "keystroke_plugins/TimeManagement.xml_key", //
	tooltip = "plugins/TimeManagement.xml_documentation")
	static private class TimeManagementAction extends AFreeplaneAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8894665436621773509L;
		private final TimeManagement timeManagement;

		public TimeManagementAction(final ModeController modeController, final ReminderHook reminderHook) {
			super(modeController.getController(), TimeManagementAction.class.getAnnotation(ActionDescriptor.class));
			timeManagement = new TimeManagement(modeController, reminderHook);
		}

		public void actionPerformed(final ActionEvent e) {
			timeManagement.startup();
		}

		public String getName() {
			return "plugins/TimeManagement.xml_name";
		}
	}

	/**
	 *
	 */
	public ReminderHook(final ModeController modeController) {
		super(modeController);
		final AFreeplaneAction timeManagementAction = new TimeManagementAction(modeController, this);
		registerAction(timeManagementAction);
		final AFreeplaneAction timeListAction = new TimeListAction(modeController);
		registerAction(timeListAction);
		final AFreeplaneAction nodeListAction = new NodeListAction(modeController);
		registerAction(nodeListAction);
	}

	@Override
	protected void add(final NodeModel node, final IExtension extension) {
		final ReminderExtension reminderExtension = (ReminderExtension) extension;
		scheduleTimer(reminderExtension);
		super.add(node, extension);
	}

	@Override
	protected HookAction createHookAction() {
		return getModeController() instanceof ModeController ? new HideableAction() : null;
	}

	@Override
	protected IExtension createExtension(final NodeModel node, final IXMLElement element) {
		final ReminderExtension reminderExtension = new ReminderExtension(node);
		final String attribute = element.getFirstChildNamed("Parameters").getAttribute(REMINDUSERAT,
		    "0");
		reminderExtension.setRemindUserAt(Long.parseLong(attribute));
		return reminderExtension;
	}

	@Override
	protected Class getExtensionClass() {
		return ReminderExtension.class;
	}

	@Override
	protected void remove(final NodeModel node, final IExtension extension) {
		final ReminderExtension reminderExtension = (ReminderExtension) extension;
		deactivate(reminderExtension);
		super.remove(node, extension);
	}

	@Override
	protected void saveExtension(final IExtension extension, final IXMLElement element) {
		super.saveExtension(extension, element);
		final ReminderExtension reminderExtension = (ReminderExtension) extension;
		final IXMLElement parameters = element.createElement("Parameters");
		parameters.setAttribute(REMINDUSERAT, Long.toString(reminderExtension.getRemindUserAt()));
		element.addChild(parameters);
	}
}
