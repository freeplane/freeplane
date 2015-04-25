package org.freeplane.core.ui.menubuilders.menu;

import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;

public class JRibbonBuilder implements EntryVisitor {
	private JRibbon ribbon = null;

	public JRibbonBuilder(IUserInputListenerFactory userInputListenerFactory) {
		super();
		this.ribbon = userInputListenerFactory.getRibbon();
	}

	@Override
	public void visit(Entry target) {
		new EntryAccessor().setComponent(target, ribbon);
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}
}
