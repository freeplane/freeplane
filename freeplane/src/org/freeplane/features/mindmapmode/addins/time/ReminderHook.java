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
import java.util.TimerTask;

import org.freeplane.core.addins.NodeHookDescriptor;
import org.freeplane.core.addins.PersistentNodeHook;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.icon.IconStore;
import org.freeplane.core.icon.UIIcon;
import org.freeplane.core.icon.factory.IconStoreFactory;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.ITooltipProvider;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author foltin
 */
@NodeHookDescriptor(hookName = "plugins/TimeManagementReminder.xml", onceForMap = false)
@ActionLocationDescriptor(locations = { "/menu_bar/extras/first/time_management" })
public class ReminderHook extends PersistentNodeHook {
	
	private static final IconStore STORE = IconStoreFactory.create();

	//******************************************	
	@EnabledAction(checkOnNodeChange = true)
	private class ReminderHookAction extends HookAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * 
		 */
		public ReminderHookAction() {
			super("ReminderHookAction");
		}

		@Override
		public void setEnabled() {
			setEnabled(isActiveForSelection());
		}
	}

	@ActionLocationDescriptor(locations = { "/menu_bar/extras/first/time_management" })
	static private class TimeListAction extends AFreeplaneAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		/**
		 * 
		 */
		private final NodeList timeList;

		public TimeListAction(final ModeController modeController) {
			super("TimeListAction", modeController.getController());
			timeList = new NodeList(modeController, false, false);
		}

		public void actionPerformed(final ActionEvent e) {
			timeList.startup();
		}
	}

	@ActionLocationDescriptor(locations = { "/menu_bar/extras/first/time_management" }, //
	accelerator = "control T")
	static private class TimeManagementAction extends AFreeplaneAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		/**
		 * 
		 */
		private final TimeManagement timeManagement;

		public TimeManagementAction(final ModeController modeController, final ReminderHook reminderHook) {
			super("TimeManagementAction", modeController.getController());
			timeManagement = new TimeManagement(modeController, reminderHook);
		}

		public void actionPerformed(final ActionEvent e) {
			timeManagement.startup();
		}
	}

	private static UIIcon bellIcon;
	private static UIIcon clockIcon;
	private static UIIcon flagIcon;
	//******************************************
	static final String PLUGIN_LABEL = "plugins/TimeManagementReminder.xml";
	static final String REMINDUSERAT = "REMINDUSERAT";
	final private String STATE_TOOLTIP = TimerBlinkTask.class.getName() + "_STATE_";

	/**
	 *
	 */
	public ReminderHook(final ModeController modeController) {
		super(modeController);
		registerAction(new TimeManagementAction(modeController, this));
		registerAction(new TimeListAction(modeController));
		registerAction(new NodeListAction(modeController));
		registerAction(new AllMapsNodeListAction(modeController));
	}

	@Override
	protected void add(final NodeModel node, final IExtension extension) {
		final ReminderExtension reminderExtension = (ReminderExtension) extension;
		scheduleTimer(reminderExtension);
		getModeController().getMapController().addMapChangeListener(reminderExtension);
		super.add(node, extension);
	}

	void blink(final ReminderExtension model, final boolean stateAdded) {
		model.setRemindUserAt(System.currentTimeMillis() + 3000);
		scheduleTimer(model, new TimerBlinkTask(this, model, stateAdded));
		if (model.getNode().getMap() != getController().getMap()) {
			return;
		}
		displayState(model, (stateAdded) ? ClockState.CLOCK_VISIBLE : ClockState.CLOCK_INVISIBLE, model.getNode(), true);
	}

	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		final ReminderExtension reminderExtension = new ReminderExtension(this, node);
		final String attribute = element.getFirstChildNamed("Parameters").getAttribute(REMINDUSERAT, "0");
		reminderExtension.setRemindUserAt(Long.parseLong(attribute));
		return reminderExtension;
	}

	@Override
	protected HookAction createHookAction() {
		return new ReminderHookAction();
	}

	void displayState(final ReminderExtension model, final ClockState stateAdded, final NodeModel pNode, final boolean recurse) {
		UIIcon icon = null;
		if (stateAdded == ClockState.CLOCK_VISIBLE) {
			icon = getClockIcon();
		}
		else if (stateAdded == ClockState.CLOCK_INVISIBLE) {
			if (pNode == model.getNode()) {
				icon = getBellIcon();
			}
			else {
				icon = getFlagIcon();
			}
		}
		if(stateAdded != ClockState.REMOVE_CLOCK || pNode == model.getNode() || ReminderExtension.getExtension(pNode) == null){
			pNode.setStateIcon(STATE_TOOLTIP, icon, true);
		}
		getModeController().getMapController().nodeRefresh(pNode);
		if (! recurse) {
			return;
		}
		final NodeModel parentNode = pNode.getParentNode();
		if (parentNode == null) {
			return;
		}
		displayState(model, stateAdded, parentNode, recurse);
	}

	private UIIcon getBellIcon() {
		if (bellIcon == null) {
			bellIcon = STORE.getUIIcon("bell.png");
		}
		return bellIcon;
	}

	private UIIcon getClockIcon() {
		if (clockIcon == null) {
			clockIcon = STORE.getUIIcon("clock.png");
		}
		return clockIcon;
	}

	@Override
	protected Class<?> getExtensionClass() {
		return ReminderExtension.class;
	}

	private UIIcon getFlagIcon() {
		if (flagIcon == null) {
			flagIcon = STORE.getUIIcon("flag.png");
		}
		return flagIcon;
	}

	@Override
	protected void remove(final NodeModel node, final IExtension extension) {
		final ReminderExtension reminderExtension = (ReminderExtension) extension;
		setToolTip(reminderExtension.getNode(), null);
        reminderExtension.deactivateTimer();
        displayState(reminderExtension, ClockState.REMOVE_CLOCK, reminderExtension.getNode(), true);
        getModeController().getMapController().removeMapChangeListener(reminderExtension);
		super.remove(node, extension);
	}

	@Override
	protected void saveExtension(final IExtension extension, final XMLElement element) {
		super.saveExtension(extension, element);
		final ReminderExtension reminderExtension = (ReminderExtension) extension;
		final XMLElement parameters = element.createElement("Parameters");
		parameters.setAttribute(REMINDUSERAT, Long.toString(reminderExtension.getRemindUserAt()));
		element.addChild(parameters);
	}

	private void scheduleTimer(final ReminderExtension model) {
		scheduleTimer(model, new TimerBlinkTask(this, model, false));
		final Date date = new Date(model.getRemindUserAt());
		final Object[] messageArguments = { date };
		final MessageFormat formatter = new MessageFormat(ResourceBundles
		    .getText("plugins/TimeManagement.xml_reminderNode_tooltip"));
		final String message = formatter.format(messageArguments);
		setToolTip(model.getNode(), message);
		displayState(model, ClockState.CLOCK_VISIBLE, model.getNode(), false);
	}

	private void scheduleTimer(final ReminderExtension model, final TimerTask task) {
		final Date date = new Date(model.getRemindUserAt());
		model.scheduleTimer(task, date);
	}

	private void setToolTip(final NodeModel node, final String value) {
		(getModeController().getMapController()).setToolTip(node, getClass().getName(), new ITooltipProvider() {
			public String getTooltip() {
				return value;
			}
		});
	}
}
