package org.freeplane.core.ui.menubuilders;

import org.freeplane.core.ui.components.FreeplaneMenuBar;

public class JMenubarBuilder implements EntryVisitor {

	@Override
	public void visit(Entry target) {
		target.setComponent(new FreeplaneMenuBar());
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		// TODO Auto-generated method stub
		return false;
	}
}
