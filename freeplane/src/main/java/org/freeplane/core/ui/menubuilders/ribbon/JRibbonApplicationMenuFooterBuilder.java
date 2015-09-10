package org.freeplane.core.ui.menubuilders.ribbon;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.util.TextUtils;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryFooter;

public class JRibbonApplicationMenuFooterBuilder implements EntryVisitor {

	private EntryAccessor entryAccessor;
	private AcceleratebleActionProvider acceleratableActionProvider;
	private RibbonComponentDecorator decorator;

	public JRibbonApplicationMenuFooterBuilder(ResourceAccessor resourceAccessor, IAcceleratorMap accelerators) {
		this.entryAccessor = new EntryAccessor(resourceAccessor);
		this.acceleratableActionProvider = new AcceleratebleActionProvider();
		this.decorator = new RibbonComponentDecorator(resourceAccessor, accelerators);
	}

	@Override
	public void visit(Entry target) {
		AFreeplaneAction action = entryAccessor.getAction(target);
		if(action == null) 
			throw new RuntimeException("there is no action with the name: "+ target.getName());
		String title = TextUtils.removeMnemonic(entryAccessor.getText(target));
		
		ResizableIcon icon = decorator.getIcon(target);
		
		RibbonApplicationMenuEntryFooter footerEntry = new RibbonApplicationMenuEntryFooter(icon, title, acceleratableActionProvider.acceleratableAction(action));
		
		decorator.decorate(footerEntry, target);		
		entryAccessor.setComponent(target, footerEntry);
		Object parent = entryAccessor.getAncestorComponent(target);
		if(parent instanceof RibbonApplicationMenuContainer) {
			((RibbonApplicationMenuContainer) parent).add(footerEntry);
		}
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return true;
	}

}
