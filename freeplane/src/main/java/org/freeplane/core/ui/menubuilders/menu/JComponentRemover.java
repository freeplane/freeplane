package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;
import java.awt.Container;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;

public class JComponentRemover implements EntryVisitor{

	@Override
	public void visit(Entry target) {
		final Component component = (Component) target.removeComponent();
		if (component != null) {
			final Container parent = component.getParent();
			parent.remove(component);
		}
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}
	
}