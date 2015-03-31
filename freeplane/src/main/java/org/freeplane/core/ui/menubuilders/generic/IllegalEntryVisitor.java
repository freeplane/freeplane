package org.freeplane.core.ui.menubuilders.generic;

public class IllegalEntryVisitor implements EntryVisitor {
    @Override
	public void visit(Entry target) {
		throw new IllegalStateException("no builder found");
	}

    @Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}
}