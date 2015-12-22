package org.freeplane.core.ui.menubuilders.generic;


class ChildEntryRemover implements EntryVisitor {
    @Override
    public void visit(Entry target) {
    	target.removeChildren();
    }

    @Override
    public boolean shouldSkipChildren(Entry entry) {
    	return true;
    }
}