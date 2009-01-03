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
package org.freeplane.addins.mindmapmode.time;

import java.awt.event.ActionEvent;

import org.freeplane.addins.NodeHookDescriptor;
import org.freeplane.addins.PersistentNodeHook;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IXMLElement;
import org.freeplane.core.map.ModeController;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.ui.ActionDescriptor;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.core.ui.VisibleAction;

/**
 * @author foltin
 */
@NodeHookDescriptor(hookName = "plugins/TimeManagementReminder.xml", onceForMap = false)
@ActionDescriptor(name = "plugins/RemoveReminder.xml_name", //
tooltip = "accessories/plugins/RevisionPlugin.properties_documentation", //
locations = { "/menu_bar/extras/first/time_management" })
public class ReminderHook extends PersistentNodeHook {
	@VisibleAction(checkOnNodeChange = true)
	private class HideableAction extends HookAction {
		public HideableAction() {
			super();
		}

		@Override
		public void setVisible() {
			setVisible(isActiveForSelection());
		}
	}

	@ActionDescriptor(name = "plugins/NodeList.xml_name", //
	locations = { "/menu_bar/edit/find" }, //
	keyStroke = "keystroke_plugins/TimeList.xml_key", //
	tooltip = "plugins/NodeList.xml_documentation")
	static private class NodeListAction extends FreeplaneAction {
		private final TimeList timeList;

		public NodeListAction() {
			super(NodeListAction.class.getAnnotation(ActionDescriptor.class));
			timeList = new TimeList(getModeController(), true);
		}

		public void actionPerformed(final ActionEvent e) {
			timeList.startup();
		}
	}

	@ActionDescriptor(name = "plugins/TimeList.xml_name", //
	locations = { "/menu_bar/extras/first/time_management" }, //
	tooltip = "plugins/TimeList.xml_documentation")
	static private class TimeListAction extends FreeplaneAction {
		private final TimeList timeList;

		public TimeListAction() {
			super(TimeListAction.class.getAnnotation(ActionDescriptor.class));
			timeList = new TimeList(getModeController(), false);
		}

		public void actionPerformed(final ActionEvent e) {
			timeList.startup();
		}
	}

	@ActionDescriptor(name = "plugins/TimeManagement.xml_name", //
	locations = { "/menu_bar/extras/first/time_management" }, //
	keyStroke = "keystroke_plugins/TimeManagement.xml_key", //
	tooltip = "plugins/TimeManagement.xml_documentation")
	static private class TimeManagementAction extends FreeplaneAction {
		private final TimeManagement timeManagement;

		public TimeManagementAction(final ModeController modeController,
		                            final ReminderHook reminderHook) {
			super(TimeManagementAction.class.getAnnotation(ActionDescriptor.class));
			timeManagement = new TimeManagement(modeController, reminderHook);
		}

		public void actionPerformed(final ActionEvent e) {
			timeManagement.startup();
		}
	}

	/**
	 *
	 */
	public ReminderHook(final ModeController modeController) {
		super(modeController);
		if (modeController instanceof ModeController) {
			final FreeplaneAction timeManagementAction = new TimeManagementAction(modeController,
			    this);
			registerAction(timeManagementAction);
			final FreeplaneAction timeListAction = new TimeListAction();
			registerAction(timeListAction);
			final FreeplaneAction nodeListAction = new NodeListAction();
			registerAction(nodeListAction);
		}
	}

	@Override
	protected void add(final NodeModel node, final IExtension extension) {
		final ReminderExtension reminderExtension = (ReminderExtension) extension;
		reminderExtension.scheduleTimer();
		super.add(node, extension);
	}

	@Override
	protected FreeplaneAction createAction() {
		return getModeController() instanceof ModeController ? new HideableAction() : null;
	}

	@Override
	protected IExtension createExtension(final NodeModel node, final IXMLElement element) {
		final ReminderExtension reminderExtension = new ReminderExtension(node);
		final String attribute = element.getFirstChildNamed("Parameters").getAttribute(
		    ReminderExtension.REMINDUSERAT, "0");
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
		reminderExtension.deactivate();
		super.remove(node, extension);
	}

	@Override
	protected void saveExtension(final IExtension extension, final IXMLElement element) {
		super.saveExtension(extension, element);
		final ReminderExtension reminderExtension = (ReminderExtension) extension;
		final IXMLElement parameters = element.createElement("Parameters");
		parameters.setAttribute(ReminderExtension.REMINDUSERAT, Long.toString(reminderExtension
		    .getRemindUserAt()));
		element.addChild(parameters);
	}
}
