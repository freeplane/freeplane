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
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.icon.IStateIconProvider;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.IconStore;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.ITooltipProvider;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.features.script.IScriptStarter;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.view.swing.features.time.mindmapmode.TimeManagement.JTimePanel;
import org.freeplane.view.swing.features.time.mindmapmode.nodelist.AllMapsNodeListAction;
import org.freeplane.view.swing.features.time.mindmapmode.nodelist.NodeList;
import org.freeplane.view.swing.features.time.mindmapmode.nodelist.NodeListAction;
import org.freeplane.view.swing.map.attribute.AttributePanelManager;

/**
 * @author foltin
 */
@NodeHookDescriptor(hookName = "plugins/TimeManagementReminder.xml", onceForMap = false)
public class ReminderHook extends PersistentNodeHook implements IExtension {

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

	static private class TimeListAction extends AFreeplaneAction {
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
			timeList = new NodeList(false, false);
		}

		public void actionPerformed(final ActionEvent e) {
			timeList.startup();
		}
	}

	static private class TimeManagementAction extends AFreeplaneAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		/**
		 * 
		 */
		private final TimeManagement timeManagement;

		public TimeManagementAction( final ReminderHook reminderHook) {
			super("TimeManagementAction");
			timeManagement = new TimeManagement(reminderHook);
		}

		public void actionPerformed(final ActionEvent e) {
			timeManagement.showDialog();
		}
	}

	//******************************************
	static final String PLUGIN_LABEL = "plugins/TimeManagementReminder.xml";
	static final String REMINDUSERAT = "REMINDUSERAT";
	static final String PERIOD = "PERIOD";
	static final String UNIT = "UNIT";
	static final String SCRIPT = "SCRIPT";
	private static final Integer REMINDER_TOOLTIP = 12;
	private ModeController modeController;

	/**
	 *
	 */
	public ReminderHook(ModeController modeController){
		super();
		this.modeController = modeController;
		modeController.addMenuContributor(new IMenuContributor() {
			public void updateMenus(ModeController modeController, MenuBuilder builder) {
				createTimePanel();
			}
		});
		registerAction(new TimeManagementAction(this));
		registerAction(new TimeListAction());
		registerAction(new NodeListAction());
		registerAction(new AllMapsNodeListAction());
		registerTooltipProvider();
		registerStateIconProvider();

		FilterController.getCurrentFilterController().getConditionFactory().addConditionController(9,
			new ReminderConditionController());
	}
	private static final IconStore STORE = IconStoreFactory.create();
	private static UIIcon bellIcon;
	private static UIIcon clockIcon;
	private static UIIcon flagIcon;
	void registerStateIconProvider(){
		IconController.getController(modeController).addStateIconProvider(new IStateIconProvider() {
			public UIIcon getStateIcon(NodeModel node) {
				UIIcon icon = null;
				ClockState stateAdded = node.getExtension(ClockState.class);
				ReminderExtension reminder = node.getExtension(ReminderExtension.class);
				if (stateAdded == ClockState.CLOCK_VISIBLE) {
					icon = getClockIcon();
				}
				else if (stateAdded == ClockState.CLOCK_INVISIBLE) {
					if (reminder != null && node == reminder.getNode()) {
						icon = getBellIcon();
					}
					else {
						icon = getFlagIcon();
					}
				}
				if (stateAdded != null || reminder != null &&  node == reminder.getNode()
				        || ReminderExtension.getExtension(node) == null) {
					return icon;
				}
				return null;
			}
		});
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

	private UIIcon getFlagIcon() {
		if (flagIcon == null) {
			flagIcon = STORE.getUIIcon("flag.png");
		}
		return flagIcon;
	}
	private void registerTooltipProvider() {
		modeController.addToolTipProvider(REMINDER_TOOLTIP, new ITooltipProvider() {
			public String getTooltip(ModeController modeController, NodeModel node, Component view) {
				final ReminderExtension model = ReminderExtension.getExtension(node);
				if(model == null)
					return null;
				final Date date = new Date(model.getRemindUserAt());
				final Object[] messageArguments = { date };
				final MessageFormat formatter = new MessageFormat(TextUtils
					.getText("plugins/TimeManagement.xml_reminderNode_tooltip"));
				final String message = formatter.format(messageArguments);
				return message;
			}
		});

	}

	private void createTimePanel() {
		final TimeManagement timeManagement = new TimeManagement(this);
		final int axis = BoxLayout.Y_AXIS;
		final JTimePanel timePanel = timeManagement.createTimePanel(null, false, 1);
		modeController.getMapController().addNodeSelectionListener(new INodeSelectionListener() {
			public void onSelect(NodeModel node) {
				timePanel.update(node);
			}
			
			public void onDeselect(NodeModel node) {
			}
		});
		modeController.getMapController().addNodeChangeListener(new INodeChangeListener() {
			public void nodeChanged(NodeChangeEvent event) {
				final NodeModel node = event.getNode();
				if(event.getProperty().equals(getExtensionClass()) && node.equals(modeController.getMapController().getSelectedNode()))
						timePanel.update(node);
			}
		});
		timePanel.setBorder(BorderFactory.createTitledBorder(TextUtils.getText("calendar_panel")));
		final JPanel tablePanel = new AttributePanelManager(modeController).getTablePanel();
		tablePanel.setBorder(BorderFactory.createTitledBorder(TextUtils.getText("attributes_attribute")));
		final Box panel = new Box(axis);
		panel.add(timePanel);
		panel.add(tablePanel);
		final JTabbedPane tabs = (JTabbedPane) modeController.getUserInputListenerFactory().getToolBar("/format").getComponent(1);
		final JScrollPane timeScrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		UITools.setScrollbarIncrement(timeScrollPane);
		tabs.add(TextUtils.getText("calendar_attributes_panel"), timeScrollPane);
    }

	@Override
	public void add(final NodeModel node, final IExtension extension) {
		final ReminderExtension reminderExtension = (ReminderExtension) extension;
		scheduleTimer(reminderExtension);
		modeController.getMapController().addMapChangeListener(reminderExtension);
		super.add(node, extension);
	}

	void blink(final ReminderExtension model, final boolean stateAdded) {
		if (model.getNode().getMap() != Controller.getCurrentController().getMap()) {
			return;
		}
		model.displayState((stateAdded) ? ClockState.CLOCK_VISIBLE : ClockState.CLOCK_INVISIBLE, model.getNode(), true);
	}

	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		final ReminderExtension reminderExtension = new ReminderExtension(node);
		final XMLElement parameters = element.getFirstChildNamed("Parameters");
		final String time = parameters.getAttribute(REMINDUSERAT, "0");
		final String unit = parameters.getAttribute(UNIT, "DAY");
		final String period = parameters.getAttribute(PERIOD, "1");
		reminderExtension.setRemindUserAt(Long.parseLong(time));
		reminderExtension.setPeriodUnit(PeriodUnit.valueOf(unit));
		reminderExtension.setPeriod(Integer.parseInt(period));
		final String script = parameters.getAttribute(SCRIPT, null);
		reminderExtension.setScript(script);
		return reminderExtension;
	}

	@Override
	protected HookAction createHookAction() {
		return new ReminderHookAction();
	}

	@Override
	protected Class<? extends IExtension> getExtensionClass() {
		return ReminderExtension.class;
	}

	@Override
	public void remove(final NodeModel node, final IExtension extension) {
		final ReminderExtension reminderExtension = (ReminderExtension) extension;
		reminderExtension.deactivateTimer();
		reminderExtension.displayState(null, reminderExtension.getNode(), true);
		modeController.getMapController().removeMapChangeListener(reminderExtension);
		super.remove(node, extension);
	}

	@Override
	protected void saveExtension(final IExtension extension, final XMLElement element) {
		super.saveExtension(extension, element);
		final ReminderExtension reminderExtension = (ReminderExtension) extension;
		final XMLElement parameters = element.createElement("Parameters");
		parameters.setAttribute(REMINDUSERAT, Long.toString(reminderExtension.getRemindUserAt()));
		parameters.setAttribute(PERIOD, Integer.toString(reminderExtension.getPeriod()));
		parameters.setAttribute(UNIT, reminderExtension.getPeriodUnit().toString());
		final String script = reminderExtension.getScript();
		if(script != null){
			parameters.setAttribute(SCRIPT, script);
		}
		
		element.addChild(parameters);
	}

	private void scheduleTimer(final ReminderExtension model) {
		final Date date = new Date(model.getRemindUserAt());
		scheduleTimer(model, new TimerBlinkTask(this, model, false, System.currentTimeMillis() < date.getTime() + ReminderExtension.BLINKING_PERIOD));
		model.displayState(ClockState.CLOCK_VISIBLE, model.getNode(), false);
	}

	private void scheduleTimer(final ReminderExtension model, final TimerBlinkTask task) {
		final Date date = new Date(model.getRemindUserAt());
		model.scheduleTimer(task, date);
	}

	ModeController getModeController() {
    	return modeController;
    }
	public void runScript(ReminderExtension reminderExtension) {
		final String script = reminderExtension.getScript();
		if(script == null || script.equals(""))
			return;
		final IScriptStarter starter = (IScriptStarter) modeController.getExtension(IScriptStarter.class);
		if(starter == null)
			return;
		final NodeModel node = reminderExtension.getNode();
		final MapModel map = node.getMap();
		final Controller controller = modeController.getController();
		if(! controller.getMapViewManager().getMaps(modeController.getModeName()).containsValue(map))
			return;
		starter.executeScript(node, script);
    }
}
