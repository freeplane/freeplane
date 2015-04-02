package org.freeplane.core.ui.menubuilders.menu;

import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;

public class JMenubarBuilder implements EntryVisitor {
	private final IUserInputListenerFactory userInputListenerFactory;

	public JMenubarBuilder(IUserInputListenerFactory userInputListenerFactory) {
		super();
		this.userInputListenerFactory = userInputListenerFactory;
	}

	@Override
	public void visit(Entry target) {
		new EntryAccessor().setComponent(target, userInputListenerFactory.getMenuBar());
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		// TODO Auto-generated method stub
		return false;
	}
}
