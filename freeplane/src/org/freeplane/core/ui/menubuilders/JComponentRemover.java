package org.freeplane.core.ui.menubuilders;

import java.awt.Component;
import java.awt.Container;

public class JComponentRemover implements EntryVisitor{

	@Override
	public void visit(Entry target) {
		final Component component = (Component) target.getComponent();
		final Container parent = component.getParent();
		parent.remove(component);
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return true;
	}
	
}