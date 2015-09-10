package org.freeplane.core.ui.menubuilders.ribbon;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;

public class JRibbonComponentRemover implements EntryVisitor {

	@Override
	public void visit(Entry target) {
		new EntryAccessor().removeComponent(target);
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}

}
