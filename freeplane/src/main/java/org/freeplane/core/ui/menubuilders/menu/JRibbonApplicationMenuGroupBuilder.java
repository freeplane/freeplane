package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntrySecondary;

public class JRibbonApplicationMenuGroupBuilder implements EntryVisitor {
	private EntryAccessor entryAccessor;
	private IAcceleratorMap accelerators;
	
	public JRibbonApplicationMenuGroupBuilder(ResourceAccessor resourceAccessor, IAcceleratorMap accelerators) {
		super();
		this.entryAccessor = new EntryAccessor(resourceAccessor);
		this.accelerators = accelerators;
	}
	
	@Override
	public void visit(Entry entry) {
		Object parent = entryAccessor.getAncestorComponent(entry);
		if(parent instanceof RibbonApplicationMenuContainer) {
			entryAccessor.setComponent(entry, new RibbonApplicationMenuEntryGroupDelegator(entryAccessor.getText(entry), (RibbonApplicationMenuContainer) parent, accelerators));
		}
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}
}

class RibbonApplicationMenuEntryGroupDelegator extends JRibbonContainer {

	private final String title;
	private final RibbonApplicationMenuContainer parent;
	private AcceleratebleActionProvider acceleratebleActionProvider;
	private IAcceleratorMap accelerators;

	public RibbonApplicationMenuEntryGroupDelegator(String title, RibbonApplicationMenuContainer parent, IAcceleratorMap accelerators) {
		this.title = title;
		this.parent = parent;
		this.acceleratebleActionProvider = new AcceleratebleActionProvider();
		this.accelerators = accelerators;
	}
	
	@Override
	public void add(Component component, Object constraints, int index) {
		if(component instanceof AbstractCommandButton) {
			AbstractCommandButton button = (AbstractCommandButton) component;
			AFreeplaneAction action = (AFreeplaneAction) button.getClientProperty(RibbonActionComponentProvider.ACTION);
			if(action != null) {
				CommandButtonKind kind = CommandButtonKind.ACTION_ONLY;
				PopupPanelCallback callback = null;
				if(button instanceof JCommandButton) {
					if(((JCommandButton) button).getPopupCallback() != null) {
						kind = (((JCommandButton) button).getCommandButtonKind());
						callback = ((JCommandButton) button).getPopupCallback();
					}
				}
				RibbonApplicationMenuEntrySecondary entry = new RibbonApplicationMenuEntrySecondary(button.getIcon(), button.getText(), acceleratebleActionProvider.acceleratableAction(action), kind);
				if(callback != null) {
					entry.setPopupCallback(callback);
				}
			
			
				RichTooltip tip = RibbonActionContributorFactory.getRichTooltip(action, accelerators.getAccelerator(action));
				if(tip != null) {
					entry.setActionRichTooltip(tip);
				}
			
				parent.add(new SecondaryGroupEntry(title, entry));
			}
		}
		else {
			throw new RuntimeException("component type "+ component.getClass() +" is not supported!");
		}
		
	}

	@Override
	public Object getParent() {
		return parent;
	}
	
}
