package org.freeplane.core.ui.menubuilders;

public interface Builder {
	public static Builder ILLEGAL_BUILDER = new Builder() {
		@Override
		public void build(Entry target) {
			throw new IllegalStateException("no builder found");
		}
	};

	public void build(Entry target);
}
