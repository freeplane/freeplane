package org.freeplane.core.ui.menubuilders;

public interface EntryVisitor {
	public static EntryVisitor ILLEGAL_VISITOR = new EntryVisitor() {
		@Override
		public void visit(Entry target) {
			throw new IllegalStateException("no builder found");
		}

		@Override
		public boolean shouldSkipChildren(Entry entry) {
			// TODO Auto-generated method stub
			return false;
		}

	};

	public static EntryVisitor EMTPY_VISITOR = new EntryVisitor() {
		@Override
		public void visit(Entry target) {
		}

		@Override
		public boolean shouldSkipChildren(Entry entry) {
			// TODO Auto-generated method stub
			return false;
		}
	};

	public void visit(Entry target);
	public boolean shouldSkipChildren(Entry entry);
}
