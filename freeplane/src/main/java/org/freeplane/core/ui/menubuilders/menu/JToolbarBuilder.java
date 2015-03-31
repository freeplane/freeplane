package org.freeplane.core.ui.menubuilders.menu;

import javax.swing.SwingConstants;

import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;

public class JToolbarBuilder implements EntryVisitor {

	@Override
	public void visit(Entry target) {
		new EntryAccessor().setComponent(target, new FreeplaneToolBar("main_toolbar", SwingConstants.HORIZONTAL));
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		// TODO Auto-generated method stub
		return false;
	}

}
