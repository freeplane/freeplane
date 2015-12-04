package org.freeplane.core.ui.menubuilders.generic;


public abstract class ChildEntryFilter implements EntryVisitor {
	abstract public boolean shouldRemove(Entry entry);

	@Override
	public void visit(Entry entry) {
		if (shouldRemove(entry))
			entry.getParent().remove(entry);
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return shouldRemove(entry);
	}
	
}