package org.freeplane.core.ui.menubuilders.menu;

import javax.swing.ButtonGroup;

import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;

public class JMenuRadioGroupBuilder implements EntryVisitor {
	private EntryPopupListener popupListener;
	private IAcceleratorMap accelerators;
	private AcceleratebleActionProvider acceleratebleActionProvider;
	private ResourceAccessor resourceAccessor;

	public JMenuRadioGroupBuilder(EntryPopupListener popupListener, IAcceleratorMap accelerators,
	                        AcceleratebleActionProvider acceleratebleActionProvider, ResourceAccessor resourceAccessor) {
		this.popupListener = popupListener;
		this.accelerators = accelerators;
		this.acceleratebleActionProvider = acceleratebleActionProvider;
		this.resourceAccessor = resourceAccessor;
	}
	@Override
	public void visit(Entry target) {
        if(Boolean.FALSE.equals(target.getAttribute("allowed")))
            return;
		ButtonGroup buttonGroup = new ButtonGroup();
		final MenuRadioActionComponentProvider menuActionComponentProvider = new MenuRadioActionComponentProvider(
		    accelerators, acceleratebleActionProvider, resourceAccessor, buttonGroup);
		JMenuItemBuilder menuItemBuilder = new JMenuItemBuilder(popupListener, menuActionComponentProvider, resourceAccessor);
		menuItemBuilder.visit(target);
		for (Entry childEntry : target.children()) {
			menuItemBuilder.visit(childEntry);
		}
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return true;
	}
}
