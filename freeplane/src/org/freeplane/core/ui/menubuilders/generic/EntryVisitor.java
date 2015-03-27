package org.freeplane.core.ui.menubuilders.generic;

public interface EntryVisitor {
	public static EntryVisitor ILLEGAL = new EntryVisitor() {
		@Override
		public void visit(Entry target) {
			throw new IllegalStateException("no builder found");
		}

		@Override
		public boolean shouldSkipChildren(Entry entry) {
			return false;
		}

	};

	public static EntryVisitor EMTPY = new EntryVisitor() {
		@Override
		public void visit(Entry target) {
		}

		@Override
		public boolean shouldSkipChildren(Entry entry) {
			return false;
		}
	};

	public static EntryVisitor SKIP = new EntryVisitor() {
		@Override
		public void visit(Entry target) {
		}

		@Override
		public boolean shouldSkipChildren(Entry entry) {
			return true;
		}
	};

	public void visit(Entry target);
	public boolean shouldSkipChildren(Entry entry);
}
