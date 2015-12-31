package org.freeplane.core.ui.menubuilders.generic;


public interface EntryVisitor {
	public static final EntryVisitor ILLEGAL = new IllegalEntryVisitor();
	public static final EntryVisitor EMTPY = new EmptyEntryVisitor();
	public static final EntryVisitor SKIP = new SkippingEntryVisitor();
//	public static final EntryVisitor CHILD_ENTRY_REMOVER = new ChildEntryRemover();

	public void visit(Entry target);
	public boolean shouldSkipChildren(Entry entry);
}
