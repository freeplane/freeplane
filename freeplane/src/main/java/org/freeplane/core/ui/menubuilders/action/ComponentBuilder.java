package org.freeplane.core.ui.menubuilders.action;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.menu.ComponentProvider;

public class ComponentBuilder implements EntryVisitor {
	final private ComponentProvider componentProvider;

	public ComponentBuilder(ComponentProvider componentProvider) {
		this.componentProvider = componentProvider;
	}

	@Override
	public void visit(Entry target) {
		new EntryAccessor().setComponent(target, componentProvider.createComponent(target));
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}
}
