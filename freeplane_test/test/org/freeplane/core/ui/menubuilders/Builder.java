package org.freeplane.core.ui.menubuilders;

public interface Builder {
	public static Builder EMTPY_BUILDER = new Builder() {
		@Override
		public void build(Entry target) {
		}
	};

	public void build(Entry target);
}
