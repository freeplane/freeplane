package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;

import javax.swing.KeyStroke;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.util.ActionUtils;
import org.freeplane.core.util.LogUtils;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.common.JCommandToggleButton;
import org.pushingpixels.flamingo.api.common.JCommandToggleMenuButton;
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
			RibbonAcceleratorChangeListener.updateRichTooltip(button, action, ks);
			updateActionState(action, button);
			
			return button;
		}
		return null;
	}
	
	private RibbonElementPriority getPriority(String attr) {
		RibbonElementPriority prio = RibbonElementPriority.MEDIUM;
		if("top".equals(attr.trim().toLowerCase())) {
			prio = RibbonElementPriority.TOP;
		}
		else if("low".equals(attr.trim().toLowerCase())) {
			prio = RibbonElementPriority.LOW;
		}
		return prio;
	}

	private JCommandButton createCommandButton(final AFreeplaneAction action) {
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		
		final JCommandButton button = new JCommandButton(title, icon);
		
		RibbonAcceleratorChangeListener.updateRichTooltip(button, action, null);		
		button.addActionListener(acceleratebleActionProvider.acceleratableAction(action));
		button.setFocusable(false);
		return button;
	}
	
	private JCommandToggleButton createCommandToggleButton(final AFreeplaneAction action) {
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		
		final JCommandToggleButton button = new JCommandToggleButton(title, icon);
		
		RibbonAcceleratorChangeListener.updateRichTooltip(button, action, null);
		button.addActionListener(acceleratebleActionProvider.acceleratableAction(action));
		button.setFocusable(false);
		return button;
	}
	
	private JCommandMenuButton createCommandMenuButton(final AFreeplaneAction action) {
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		
		final JCommandMenuButton button = new JCommandMenuButton(title, icon);
		
		RibbonAcceleratorChangeListener.updateRichTooltip(button, action, null);
		button.addActionListener(acceleratebleActionProvider.acceleratableAction(action));
		button.setFocusable(false);
		return button;
	}
	
	private JCommandToggleMenuButton createCommandToggleMenuButton(final AFreeplaneAction action) {
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);
		
		final JCommandToggleMenuButton button = new JCommandToggleMenuButton(title, icon);
		
		RibbonAcceleratorChangeListener.updateRichTooltip(button, action, null);
		button.addActionListener(acceleratebleActionProvider.acceleratableAction(action));
		button.setFocusable(false);
		return button;
	}
	
	private void updateActionState(AFreeplaneAction action, AbstractCommandButton button) {		
		final AFreeplaneAction action1 = action;
		if(action1.checkEnabledOnChange()) {
			//action.setEnabled();
			button.setEnabled(action.isEnabled());
		}
		if(isSelectionListener(action)) {
			//action.setSelected();
			button.getActionModel().setSelected(action.isSelected());
		}
	}
	
	private boolean isSelectionListener(AFreeplaneAction action) {		
		return action.checkSelectionOnChange() || action.checkSelectionOnPopup() || action.checkSelectionOnPropertyChange();
	}
}
