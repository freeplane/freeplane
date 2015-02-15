package org.freeplane.core.ui.menubuilders;

public interface Builder {
	public static Builder ILLEGAL_BUILDER = new Builder() {
		@Override
		public void build(Entry target) {
			throw new IllegalStateException("no builder found");
		}

		@Override
		public void destroy(Entry target) {
			throw new IllegalStateException("no builder found");
			
		}
	};

	public static Builder EMTPY_BUILDER = new Builder() {
		@Override
		public void build(Entry target) {
		}

		@Override
		public void destroy(Entry target) {
		}
	};

	public void build(Entry target);
	public void destroy(Entry target);
}
