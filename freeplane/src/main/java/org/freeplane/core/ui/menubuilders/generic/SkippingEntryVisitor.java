package org.freeplane.core.ui.menubuilders.generic;

public class SkippingEntryVisitor implements EntryVisitor {
    @Override
	public void visit(Entry target) {
	}

    @Override
	public boolean shouldSkipChildren(Entry entry) {
		return true;
	}
}