package org.freeplane.core.ui.menubuilders;

import javax.swing.SwingConstants;

import org.freeplane.core.ui.components.FreeplaneToolBar;

public class JToolbarBuilder implements EntryVisitor {

	@Override
	public void visit(Entry target) {
		target.setComponent(new FreeplaneToolBar("main_toolbar", SwingConstants.HORIZONTAL));
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		// TODO Auto-generated method stub
		return false;
	}

}
