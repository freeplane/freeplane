package org.freeplane.core.ui.menubuilders.generic;


public abstract class ChildEntryFilter extends ChildEntryRemover {
	abstract public boolean shouldRemove(Entry entry);

	@Override
	public void visit(Entry entry) {
		if (shouldRemove(entry))
			super.visit(entry);
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return shouldRemove(entry);
	}
	
}