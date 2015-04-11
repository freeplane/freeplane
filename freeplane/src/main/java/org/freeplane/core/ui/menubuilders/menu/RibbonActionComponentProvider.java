package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.KeyStroke;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.AccelerateableAction;
import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.ui.ribbon.ARibbonContributor.ChildProperties;
import org.freeplane.core.util.ActionUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.common.JCommandToggleButton;
import org.pushingpixels.flamingo.api.common.JCommandToggleMenuButton;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;

public class RibbonActionComponentProvider implements ComponentProvider {
	
	public static final String ACTION = "ACTION_KEY";
	public static final String MANDATORY_PROPERTY = "MANDATORY";
	
	private IAcceleratorMap accelerators;
	private AcceleratebleActionProvider acceleratebleActionProvider;
	private EntryAccessor entryAccessor;
	
	public RibbonActionComponentProvider(IAcceleratorMap accelerators, AcceleratebleActionProvider acceleratebleActionProvider, ResourceAccessor resourceAccessor) {
		this.accelerators = accelerators;
		this.acceleratebleActionProvider = acceleratebleActionProvider;
		this.entryAccessor = new EntryAccessor(resourceAccessor);
	}
	
	@Override
	public Component createComponent(Entry entry) {
		return createButtonComponent(entry);
	}
	
	private Component createButtonComponent(Entry entry) {
		final AFreeplaneAction action = entryAccessor.getAction(entry);
		
		if(action != null) {
			Object attr = entry.getAttribute("mandatory");
			final boolean mandatory = Boolean.parseBoolean(attr == null? "false":String.valueOf(attr).toLowerCase());
			attr = entry.getAttribute("orderPriority");
			ChildProperties childProps = //new ChildProperties(parseOrderSettings(attr == null? "":String.valueOf(attr)));
					new ChildProperties(JRibbonContainer.APPEND);
			attr = entry.getAttribute("priority");
			childProps.set(RibbonElementPriority.class, getPriority(attr == null? "medium" : String.valueOf(attr)));
			
			if(mandatory) {
				action.putValue(MANDATORY_PROPERTY, mandatory);
			}
			
			AbstractCommandButton button;
			if(isSelectionListener(action)) {
				button = createCommandToggleButton(action);
				if (entry.hasChildren()) {
					LogUtils.severe("RibbonActionComponentProvider.createButton(): can't add popup menu to toggle button for action: "+ entry);
				}
			}
			else {
				button = createCommandButton(action);
				if(entry.hasChildren()) {
					//((JCommandButton)button).setPopupCallback(getPopupPanelCallBack(path, context));
					((JCommandButton)button).setCommandButtonKind(CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION);
					KeyStroke ks = accelerators.getAccelerator(action.getKey());
					updateRichTooltip(button, action, ks);
					updateActionState(action, button);
				}
			}
			button.putClientProperty(ACTION, action);
			
//			KeyStroke ks = context.getBuilder().getAcceleratorManager().getAccelerator(actionKey);
//			if(ks != null) {
//				button.putClientProperty(ACTION_ACCELERATOR, ks);
//				updateRichTooltip(button, action, ks);
//			}
//			getAccelChangeListener().addCommandButton(actionKey, button);
//			
//			builder.getMapChangeAdapter().addListener(new ActionChangeListener(action, button));	
//			parent.addChild(button, childProps);
			return button;
		}
		return null;
	}
	
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
		button.addActionListener(new AccelerateableAction(action));
		button.setFocusable(false);
		return button;
	}

	public static JCommandButton createCommandButton(final AFreeplaneAction action) {
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		
		final JCommandButton button = new JCommandButton(title, icon);
		
		updateRichTooltip(button, action, null);
		button.addActionListener(new AccelerateableAction(action));
		button.setFocusable(false);
		return button;
	}
	
	public static JCommandToggleButton createCommandToggleButton(final AFreeplaneAction action) {
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		
		final JCommandToggleButton button = new JCommandToggleButton(title, icon);
		
		updateRichTooltip(button, action, null);
		button.addActionListener(new AccelerateableAction(action));
		button.setFocusable(false);
		return button;
	}
	
	public static JCommandMenuButton createCommandMenuButton(final AFreeplaneAction action) {
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		
		final JCommandMenuButton button = new JCommandMenuButton(title, icon);
		
		updateRichTooltip(button, action, null);
		button.addActionListener(new AccelerateableAction(action));
		button.setFocusable(false);
		return button;
	}
	
	public static JCommandToggleMenuButton createCommandToggleMenuButton(final AFreeplaneAction action) {
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		
		final JCommandToggleMenuButton button = new JCommandToggleMenuButton(title, icon);
		
		updateRichTooltip(button, action, null);
		button.addActionListener(new AccelerateableAction(action));
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
	
	public static void updateActionState(AFreeplaneAction action, AbstractCommandButton button) {		
		final AFreeplaneAction action1 = action;
		if(action1.checkEnabledOnChange()) {
			action.setEnabled();
			button.setEnabled(action.isEnabled());
		}
		if(isSelectionListener(action)) {
			action.setSelected();
			button.getActionModel().setSelected(action.isSelected());
		}
	}
	
	public static boolean isSelectionListener(AFreeplaneAction action) {		
		return action.checkSelectionOnChange() || action.checkSelectionOnPopup() || action.checkSelectionOnPropertyChange();
	}

}
