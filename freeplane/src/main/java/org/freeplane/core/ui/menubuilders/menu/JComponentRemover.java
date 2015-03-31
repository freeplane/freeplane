package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;

import org.freeplane.core.ui.MenuSplitter;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;

public class JComponentRemover implements EntryVisitor{
	final private static MenuSplitter menuSplitter = new MenuSplitter(0);

	@Override
	public void visit(Entry target) {
		final Component component = (Component) new EntryAccessor().removeComponent(target);
		if (component != null) {
			removeMenuComponent(component);
		}
	}

	private void removeMenuComponent(final Component component) {
		menuSplitter.removeMenuComponent(component);
    }

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}
	
}