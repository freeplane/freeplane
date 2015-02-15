package org.freeplane.core.ui.menubuilders;

import org.freeplane.core.ui.components.FreeplaneMenuBar;

public class JMenubarBuilder implements Builder {

	@Override
	public void build(Entry target) {
		target.setComponent(new FreeplaneMenuBar());
	}

	@Override
	public void destroy(Entry target) {
		throw new UnsupportedOperationException();
	}

}
