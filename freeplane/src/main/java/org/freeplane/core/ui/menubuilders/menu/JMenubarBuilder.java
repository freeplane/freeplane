package org.freeplane.core.ui.menubuilders.menu;

import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;

public class JMenubarBuilder implements EntryVisitor {

	@Override
	public void visit(Entry target) {
		new EntryAccessor().setComponent(target, new FreeplaneMenuBar());
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		// TODO Auto-generated method stub
		return false;
	}
}
