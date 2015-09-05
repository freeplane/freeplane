package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.KeyStroke;

import org.freeplane.core.ui.AFreeplaneAction;
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
	public static final String PRIORITY_PROPERTY = "PRIORITY_KEY";
	public static final String ORDER_PROPERTY = "ORDER_KEY";
	
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
			if(mandatory) {
				action.putValue(MANDATORY_PROPERTY, mandatory);
			}
			final boolean isPopupItem = (entryAccessor.getAncestorComponent(entry) instanceof RibbonPopupWrapper);
			
			AbstractCommandButton button;
			if(isSelectionListener(action)) {
				button = isPopupItem ? createCommandToggleMenuButton(action) : createCommandToggleButton(action);
				if (entry.hasChildren()) {
					LogUtils.severe("RibbonActionComponentProvider.createButton(): can't add popup menu to toggle button for action: "+ entry);
				}
			}
			else {
				button = isPopupItem ? createCommandMenuButton(action) : createCommandButton(action);
				if(entry.hasChildren()) {
					((JCommandButton)button).setCommandButtonKind(CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION);
				}
			}
			button.putClientProperty(ACTION, action);
			
			attr = entry.getAttribute("orderPriority");
			button.putClientProperty(ORDER_PROPERTY, JRibbonContainer.APPEND); //parseOrderSettings(attr == null? "":String.valueOf(attr))

			attr = entry.getAttribute("priority");
			button.putClientProperty(PRIORITY_PROPERTY, getPriority(attr == null? "medium" : String.valueOf(attr)));
			
			KeyStroke ks = accelerators.getAccelerator(action);
			updateRichTooltip(button, action, ks);
			updateActionState(action, button);
			
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
	
	private JButton createButton(final AFreeplaneAction action) {
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		
		final JButton button = new JButton(title, icon);
		
//		updateRichTooltip(button, action, null);
		button.addActionListener(acceleratebleActionProvider.acceleratableAction(action));
		button.setFocusable(false);
		return button;
	}

	private JCommandButton createCommandButton(final AFreeplaneAction action) {
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		
		final JCommandButton button = new JCommandButton(title, icon);
		
		updateRichTooltip(button, action, null);		
		button.addActionListener(acceleratebleActionProvider.acceleratableAction(action));
		button.setFocusable(false);
		return button;
	}
	
	private JCommandToggleButton createCommandToggleButton(final AFreeplaneAction action) {
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		
		final JCommandToggleButton button = new JCommandToggleButton(title, icon);
		
		updateRichTooltip(button, action, null);
		button.addActionListener(acceleratebleActionProvider.acceleratableAction(action));
		button.setFocusable(false);
		return button;
	}
	
	private JCommandMenuButton createCommandMenuButton(final AFreeplaneAction action) {
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		
		final JCommandMenuButton button = new JCommandMenuButton(title, icon);
		
		updateRichTooltip(button, action, null);
		button.addActionListener(acceleratebleActionProvider.acceleratableAction(action));
		button.setFocusable(false);
		return button;
	}
	
	private JCommandToggleMenuButton createCommandToggleMenuButton(final AFreeplaneAction action) {
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		
		final JCommandToggleMenuButton button = new JCommandToggleMenuButton(title, icon);
		
		updateRichTooltip(button, action, null);
		button.addActionListener(acceleratebleActionProvider.acceleratableAction(action));
		button.setFocusable(false);
		return button;
	}
	
	private void updateRichTooltip(final AbstractCommandButton button, AFreeplaneAction action, KeyStroke ks) {
		RichTooltip tip = getRichTooltip(action, ks);
		if(tip != null) {
			button.setActionRichTooltip(tip);
		}
		else {
			button.setActionRichTooltip(null);
		}
	}
	
	private RichTooltip getRichTooltip(AFreeplaneAction action, KeyStroke ks) {
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

	private String formatShortcut(KeyStroke ks) {
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
	
	private void updateActionState(AFreeplaneAction action, AbstractCommandButton button) {		
		final AFreeplaneAction action1 = action;
		if(action1.checkEnabledOnChange()) {
			//action.setEnabled();
			button.setEnabled(action.isEnabled());
		}
		if(isSelectionListener(action)) {
//			try {
//				action.setSelected();
//			} catch(Exception cause) {
//				cause.printStackTrace();
//			}
			button.getActionModel().setSelected(action.isSelected());
		}
	}
	
	private boolean isSelectionListener(AFreeplaneAction action) {		
		return action.checkSelectionOnChange() || action.checkSelectionOnPopup() || action.checkSelectionOnPropertyChange();
	}
}
