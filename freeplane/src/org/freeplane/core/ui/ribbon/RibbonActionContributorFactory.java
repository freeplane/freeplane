package org.freeplane.core.ui.ribbon;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.AccelerateableAction;
import org.freeplane.core.ui.IAcceleratorChangeListener;
import org.freeplane.core.ui.ribbon.RibbonSeparatorContributorFactory.RibbonSeparator;
import org.freeplane.core.ui.ribbon.StructureTree.StructurePath;
import org.freeplane.core.ui.ribbon.event.AboutToPerformEvent;
import org.freeplane.core.util.ActionUtils;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.common.JCommandToggleButton;
import org.pushingpixels.flamingo.api.common.JCommandToggleMenuButton;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;

public class RibbonActionContributorFactory implements IRibbonContributorFactory {

	public static final String ACTION = "ACTION_KEY";
	public static final String ACTION_ACCELERATOR = "ACTION_ACCELERATOR";
	public static final String ACTION_NAME_PROPERTY = "ACTION_NAME";
	public static final String ACTION_CHANGE_LISTENER = "ACTION_CHANGE_LISTENER";
	public static final String MANDATORY_PROPERTY = "MANDATORY";
	public static ResizableIcon BLANK_ACTION_ICON;
	static {
		URL location = ResourceController.getResourceController().getResource("/images/blank_icon_48x48.png");
		if (location != null) {
			BLANK_ACTION_ICON = ImageWrapperResizableIcon.getIcon(location, new Dimension(48, 48));
		}
	}
	
	private final RibbonBuilder builder;
	private AcceleratorChangeListenerForCommandButtons changeListener;

	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public RibbonActionContributorFactory(RibbonBuilder builder) {
		this.builder = builder;
		builder.getAcceleratorManager().addAcceleratorChangeListener(getAccelChangeListener());
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public static RibbonElementPriority getPriority(String attr) {
		RibbonElementPriority prio = RibbonElementPriority.MEDIUM;
		if("top".equals(attr.trim().toLowerCase())) {
			prio = RibbonElementPriority.TOP;
		}
		else if("low".equals(attr.trim().toLowerCase())) {
			prio = RibbonElementPriority.LOW;
		}
		return prio;
	}
	
	public static JButton createButton(final AFreeplaneAction action) {
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		
		final JButton button = new JButton(title, icon);
		
//		updateRichTooltip(button, action, null);
		button.addActionListener(new RibbonActionListener(action));
		button.setFocusable(false);
		return button;
	}

	public static JCommandButton createCommandButton(final AFreeplaneAction action) {
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		
		final JCommandButton button = new JCommandButton(title, icon);
		
		updateRichTooltip(button, action, null);
		button.addActionListener(new RibbonActionListener(action));
		button.setFocusable(false);
		return button;
	}
	
	public static JCommandToggleButton createCommandToggleButton(final AFreeplaneAction action) {
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		
		final JCommandToggleButton button = new JCommandToggleButton(title, icon);
		
		updateRichTooltip(button, action, null);
		button.addActionListener(new RibbonActionListener(action));
		button.setFocusable(false);
		return button;
	}
	
	public static JCommandMenuButton createCommandMenuButton(final AFreeplaneAction action) {
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		
		final JCommandMenuButton button = new JCommandMenuButton(title, icon);
		
		updateRichTooltip(button, action, null);
		button.addActionListener(new RibbonActionListener(action));
		button.setFocusable(false);
		return button;
	}
	
	public static JCommandToggleMenuButton createCommandToggleMenuButton(final AFreeplaneAction action) {
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		
		final JCommandToggleMenuButton button = new JCommandToggleMenuButton(title, icon);
		
		updateRichTooltip(button, action, null);
		button.addActionListener(new RibbonActionListener(action));
		button.setFocusable(false);
		return button;
	}
	
	public static void updateRichTooltip(final AbstractCommandButton button, AFreeplaneAction action, KeyStroke ks) {
		RichTooltip tip = getRichTooltip(action, ks);
		if(tip != null) {
			button.setActionRichTooltip(tip);
		}
		else {
			button.setActionRichTooltip(null);
		}
	}
	
	public static RichTooltip getRichTooltip(AFreeplaneAction action, KeyStroke ks) {
		RichTooltip tip = null;
		final String tooltip = TextUtils.getRawText(action.getTooltipKey(), null);
		if (tooltip != null && !"".equals(tooltip)) {
			tip = new RichTooltip(ActionUtils.getActionTitle(action), TextUtils.removeTranslateComment(tooltip));
		}
		if(ks != null) {
			if(tip == null) {
				tip = new RichTooltip(ActionUtils.getActionTitle(action), "  ");
			}
			tip.addFooterSection(formatShortcut(ks));
		}
		return tip;
	}

	public static String formatShortcut(KeyStroke ks) {
		StringBuilder sb = new StringBuilder();
		if(ks != null) {
			String[] st = ks.toString().split("[\\s]+");
			for (String s : st) {
				if("pressed".equals(s.trim())) {
					continue;
				}
				if(sb.length() > 0) {
					sb.append(" + ");
				}
				sb.append(s.substring(0, 1).toUpperCase(Locale.ENGLISH));
				sb.append(s.substring(1));
			}
		}
		return sb.toString();
	}

	protected AcceleratorChangeListenerForCommandButtons getAccelChangeListener() {
		if(changeListener == null) {
			changeListener = new AcceleratorChangeListenerForCommandButtons();
		}
		return changeListener;
	}
	
	public static void updateActionState(AFreeplaneAction action, AbstractCommandButton button) {		
		if(AFreeplaneAction.checkEnabledOnChange(action)) {
			action.setEnabled();
			button.setEnabled(action.isEnabled());
		}
		if(isSelectionListener(action)) {
			action.setSelected();
			button.getActionModel().setSelected(action.isSelected());
		}
	}


	public static boolean isSelectionListener(AFreeplaneAction action) {
		return AFreeplaneAction.checkSelectionOnChange(action) || AFreeplaneAction.checkSelectionOnPopup(action) || AFreeplaneAction.checkSelectionOnPropertyChange(action);
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public ARibbonContributor getContributor(final Properties attributes) {
		final String actionKey = attributes.getProperty("action");
			if(actionKey != null) {
			String accel = attributes.getProperty("accelerator", null);
			if (accel != null) {
				if (Compat.isMacOsX()) {
					accel = accel.replaceFirst("CONTROL", "META").replaceFirst("control", "meta");
				}
				builder.getAcceleratorManager().setDefaultAccelerator(actionKey, accel);
			}
		}
		return new ARibbonContributor() {
			
			private List<Component> childButtons = new ArrayList<Component>();

			public String getKey() {
				String key = attributes.getProperty("action");
				if(key == null) {
					key = attributes.getProperty("name");
				}
				return key;
			}
			
			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {			
				final String actionKey = attributes.getProperty("action");
				final boolean mandatory = Boolean.parseBoolean(attributes.getProperty("mandatory", "false").toLowerCase());
				ChildProperties childProps = new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", "")));
				childProps.set(RibbonElementPriority.class, getPriority(attributes.getProperty("priority", "medium")));
				
				if(actionKey != null) {
					AFreeplaneAction action = context.getBuilder().getMode().getAction(actionKey);
					
					if(action != null) {
						if(mandatory) {
							action.putValue(MANDATORY_PROPERTY, mandatory);
						}
						AbstractCommandButton button;
						if(isSelectionListener(action)) {
							button = createCommandToggleButton(action);
							if (context.hasChildren(context.getCurrentPath())) {
								LogUtils.severe("RibbonActionContributorFactory.getContributor(): can't add popup menu to toggle button for action: "+context.getCurrentPath().toString());
							}
						}
						else {
							button = createCommandButton(action);
							if(context.hasChildren(context.getCurrentPath())) {
								StructurePath path = context.getCurrentPath();
								((JCommandButton)button).setPopupCallback(getPopupPanelCallBack(path, context));
								((JCommandButton)button).setCommandButtonKind(CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION);
								KeyStroke ks = context.getBuilder().getAcceleratorManager().getAccelerator(actionKey);
								updateRichTooltip(button, action, ks);
								updateActionState(action, button);
							}
						}
						button.putClientProperty(ACTION, action);
						
						KeyStroke ks = context.getBuilder().getAcceleratorManager().getAccelerator(actionKey);
						if(ks != null) {
							button.putClientProperty(ACTION_ACCELERATOR, ks);
							updateRichTooltip(button, action, ks);
						}
						getAccelChangeListener().addCommandButton(actionKey, button);
						
						builder.getMapChangeAdapter().addListener(new ActionChangeListener(action, button));	
						parent.addChild(button, childProps);
					}
				}
				else {
					final String name = attributes.getProperty("name");
					if(name != null) {
						AFreeplaneAction action = ActionUtils.getDummyAction(name);
						final JCommandButton button = new JCommandButton(ActionUtils.getActionTitle(action), ActionUtils.getActionIcon(action));
						button.putClientProperty(ACTION_NAME_PROPERTY, action);
						updateRichTooltip(button, action, null);
						if(context.hasChildren(context.getCurrentPath())) {
							StructurePath path = context.getCurrentPath();
							button.setPopupCallback(getPopupPanelCallBack(path, context));
							button.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
						}
						button.setFocusable(false);
						parent.addChild(button, childProps);
					}
				}
			}
			
			private PopupPanelCallback getPopupPanelCallBack(StructurePath path, final RibbonBuildContext context) {
				childButtons.clear();
				context.processChildren(path, this);
				for (Component child : childButtons){
					if(child instanceof AbstractCommandButton) {
						AFreeplaneAction action = (AFreeplaneAction) ((AbstractCommandButton) child).getClientProperty(ACTION);
						if(action != null) {
							try {
								builder.getMapChangeAdapter().removeListener((IChangeObserver) (action).getValue(ACTION_CHANGE_LISTENER));
								getAccelChangeListener().removeCommandButton(((AFreeplaneAction) action).getKey());
							}
							catch(Exception e) {
								LogUtils.info("RibbonActionContributorFactory.getContributor(...).new ARibbonContributor() {...}.addChild(): "+e.getMessage());
							}
						}
					}
				}

				
				return new PopupPanelCallback() {
					
					public JPopupPanel getPopupPanel(JCommandButton commandButton) {
						JCommandPopupMenu popupmenu = new JCommandPopupMenu();
						for (Component comp : childButtons) {
							if(comp instanceof JSeparator) {
								popupmenu.addMenuSeparator();
							}
							else if(comp instanceof AbstractCommandButton) {
								replaceCommandButtonByCommandMenuButton(context, popupmenu, (AbstractCommandButton)comp);
								
							}
						}
						return popupmenu;
					}

					private void replaceCommandButtonByCommandMenuButton(
							final RibbonBuildContext context,
							JCommandPopupMenu popupmenu, AbstractCommandButton button) {
						
						AbstractCommandButton menuButton = null;								
						AFreeplaneAction action = (AFreeplaneAction)button.getClientProperty(ACTION);
						if(action != null) {
							if(isSelectionListener(action)) {
								menuButton = createCommandToggleMenuButton(action);
								popupmenu.addMenuButton((JCommandToggleMenuButton) menuButton);
							}
							else {
								menuButton = createCommandMenuButton(action);
								popupmenu.addMenuButton((JCommandMenuButton) menuButton);
							}
							menuButton.setEnabled(button.isEnabled());
							menuButton.putClientProperty(ACTION, action);
							KeyStroke ks = context.getBuilder().getAcceleratorManager().getAccelerator(action.getKey());
							updateRichTooltip(menuButton, action, ks);
							updateActionState(action, menuButton);
						}
						else {
							action = (AFreeplaneAction)button.getClientProperty(ACTION_NAME_PROPERTY);
							menuButton = createCommandMenuButton(action);
							if(action != null) {
								menuButton.putClientProperty(ACTION_NAME_PROPERTY, action);
								updateRichTooltip(menuButton, action, null);
							}
						}
						
						if(button instanceof JCommandButton) {
							if(((JCommandButton) button).getPopupCallback() != null) {
								((JCommandMenuButton)menuButton).setPopupCallback(((JCommandButton) button).getPopupCallback());
								((JCommandMenuButton)menuButton).setCommandButtonKind(((JCommandButton) button).getCommandButtonKind());										
							}
						}
						//clear all RibbonActionListeners from the menuButton
						for (ActionListener listener : menuButton.getListeners(ActionListener.class)) {
							if(listener instanceof RibbonActionListener) {
								menuButton.removeActionListener(listener);
							}
						}
						//add 
						for (ActionListener listener : button.getListeners(ActionListener.class)) {
							if(listener instanceof RibbonActionListener) {
								menuButton.addActionListener(listener);
							}
						}
					}
				};
			}

			public void addChild(Object child, ChildProperties properties) {
				if(child instanceof AbstractCommandButton) {
					childButtons.add((AbstractCommandButton) child);
				}
				if(child instanceof RibbonSeparator) {
					childButtons.add(new JSeparator(JSeparator.HORIZONTAL));
				}
				
			}		
		};
	}

	/***********************************************************************************
	 * NESTED TYPE DECLARATIONS
	 **********************************************************************************/
	
	public static class RibbonActionListener implements ActionListener {
		private final String key;
		private final RibbonBuilder builder;

		protected RibbonActionListener(AFreeplaneAction action) {
			this.key = action.getKey();
			this.builder = Controller.getCurrentModeController().getUserInputListenerFactory().getMenuBuilder(RibbonBuilder.class);
		}

		public void actionPerformed(ActionEvent e) {
			AFreeplaneAction action = Controller.getCurrentModeController().getAction(key);
			
			if(action == null || linkAccelerator(action, e)) {
				return;
			}
			
			if ((0 != (e.getModifiers() & ActionEvent.CTRL_MASK))) {
				builder.getAcceleratorManager().newAccelerator(action, null);
				return;
			}
			builder.getRibbonActionEventHandler().fireAboutToPerformEvent(new AboutToPerformEvent(action));
			action.actionPerformed(e);
		}

		private boolean linkAccelerator(AFreeplaneAction action, ActionEvent e) {
			final boolean newAcceleratorOnNextClickEnabled = AccelerateableAction.isNewAcceleratorOnNextClickEnabled();
			if (newAcceleratorOnNextClickEnabled) {
				AccelerateableAction.getAcceleratorOnNextClickActionDialog().setVisible(false);
			}
			final Object source = e.getSource();
			if ((newAcceleratorOnNextClickEnabled || 0 != (e.getModifiers() & ActionEvent.CTRL_MASK)) && source instanceof AbstractCommandButton) {
				builder.getAcceleratorManager().newAccelerator(action, AccelerateableAction.getAcceleratorForNextClick());
				return true;
			}
			return false;
		}
		
		
	}
	
	public static class AcceleratorChangeListenerForCommandButtons implements IAcceleratorChangeListener {
		private final Map<String, AbstractCommandButton> commandButtonsForActionKeys = new HashMap<String, AbstractCommandButton>();
		
		/***********************************************************************************
		 * CONSTRUCTORS
		 **********************************************************************************/

		/***********************************************************************************
		 * METHODS
		 **********************************************************************************/
		
		public void addCommandButton(String actionKey, AbstractCommandButton button) {
			commandButtonsForActionKeys.put(actionKey, button);
		}
		
		public void removeCommandButton(String actionKey) {
			commandButtonsForActionKeys.remove(actionKey);
		}
		
		public void clear() {
			commandButtonsForActionKeys.clear();
		}
		/***********************************************************************************
		 * REQUIRED METHODS FOR INTERFACES
		 **********************************************************************************/
		
		public void acceleratorChanged(JMenuItem action, KeyStroke oldStroke, KeyStroke newStroke) {
			
		}
		
		public void acceleratorChanged(AFreeplaneAction action, KeyStroke oldStroke, KeyStroke newStroke) {
			AbstractCommandButton button = commandButtonsForActionKeys.get(action.getKey()); 
			if(button != null) {
				updateRichTooltip(button, action, newStroke);
			}

		}
	}
	
	public static class ActionChangeListener implements IChangeObserver {		
		
		private final AFreeplaneAction action;
		private final AbstractCommandButton button;
		
		/***********************************************************************************
		 * CONSTRUCTORS
		 **********************************************************************************/
		public ActionChangeListener(AFreeplaneAction action, AbstractCommandButton button) {
			if(button == null || action == null) {
				throw new IllegalArgumentException("NULL");
			}
			this.action = action;
			this.button = button;
			action.putValue(ACTION_CHANGE_LISTENER, this);
		}
		/***********************************************************************************
		 * METHODS
		 **********************************************************************************/
		
		public void updateState(CurrentState state) {
			if(state.isNodeChangeEvent()) {
				updateActionState(action, button);
			}
			else if(state.allMapsClosed()) {
				if (action.getValue(MANDATORY_PROPERTY) == null) {
					action.setEnabled(false);
					button.setEnabled(false);
				}
			}
			else if (state.get(TreeSelectionEvent.class) == null) {
				action.setEnabled(true);
				button.setEnabled(true);
			}
		}
	};
}
