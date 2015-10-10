package org.freeplane.core.ui.menubuilders.ribbon;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryFooter;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntrySecondary;

public class RibbonComponentDecorator {
	
	private IAcceleratorMap accelerators;
	private EntryAccessor entryAccessor;
	private ResourceAccessor resourceAccessor;
	private RibbonComponentDecorationProvider decorationProvider;
		
	public RibbonComponentDecorator(ResourceAccessor resourceAccessor, IAcceleratorMap acceleratorMap) {
		this.accelerators = acceleratorMap;
		this.entryAccessor = new EntryAccessor(resourceAccessor);
		this.resourceAccessor = resourceAccessor;
		this.decorationProvider = new RibbonComponentDecorationProvider(resourceAccessor);
	}
	
	public RibbonComponentDecorationProvider getDecorationProvider() {
		return this.decorationProvider;
	}

	public void decorate(AbstractCommandButton button, Entry entry) {
		updateRichToolTip(button, entry);
		updateIcon(button, entry);
	}

	public void decorate(RibbonApplicationMenuEntryPrimary menuEntry, Entry entry) {
		updateRichToolTip(menuEntry, entry);
	}

	public void decorate(RibbonApplicationMenuEntrySecondary menuEntry, Entry entry) {
		updateRichToolTip(menuEntry, entry);
	}

	public void decorate(RibbonApplicationMenuEntryFooter menuEntry, Entry entry) {
		updateRichToolTip(menuEntry, entry);
	}

	public void updateRichTooltip(Object component, AFreeplaneAction action, KeyStroke ks) {
		final String title = resourceAccessor.getText(action.getTextKey(), "");
		final String tooltip = getActionTooltip(action);
		RichTooltip tip = decorationProvider.createRichTooltip(title, tooltip, ks);
		setRichToolTip(component, tip);
	}
	
	public void updateRichTooltip(Object component, AFreeplaneAction action) {
		final KeyStroke ks = accelerators.getAccelerator(action);
		updateRichTooltip(component, action, ks);
	}
	
	public void updateRichToolTip(final Object component, Entry entry) {
		AFreeplaneAction action = entryAccessor.getAction(entry);	
		updateRichTooltip(component, action);
	}
	
	private void setRichToolTip(final Object component, RichTooltip tip) {
		if(component instanceof AbstractCommandButton) {
			((AbstractCommandButton) component).setActionRichTooltip(tip);
		} 
		else if(component instanceof RibbonApplicationMenuEntryPrimary) {
			((RibbonApplicationMenuEntryPrimary) component).setActionRichTooltip(tip);
		}
		else if(component instanceof RibbonApplicationMenuEntryFooter) {
			((RibbonApplicationMenuEntryFooter) component).setActionRichTooltip(tip);
		}
		else if(component instanceof RibbonApplicationMenuEntrySecondary) {
			((RibbonApplicationMenuEntrySecondary) component).setActionRichTooltip(tip);
		}
		else {
			throw new RuntimeException("unsupported component type: " + component.getClass());
		}
	}
	
	private String getActionTooltip(final AFreeplaneAction action) {
		if (action != null) {
			final String text = resourceAccessor.getText(action.getTooltipKey(), null);
			if (text != null)
				return text;					
		}
		return "";
	}
	
	public void updateIcon(Object component, Entry entry) {
		ResizableIcon icon = getIcon(entry);
		setIcon(component, icon);
	}

	public ResizableIcon getIcon(Entry entry) {
		ResizableIcon icon = decorationProvider.createIcon((ImageIcon)entryAccessor.getIcon(entry));
		if(icon == null) {
			icon = decorationProvider.getActionIcon(entryAccessor.getAction(entry));
		}
		return icon;
	}
	
	private void setIcon(final Object component, ResizableIcon icon) {
		if(component instanceof AbstractCommandButton) {
			((AbstractCommandButton) component).setIcon(icon);
		}
		else {
			throw new RuntimeException("unsupported component type: " + component.getClass());
		}
	}
}
