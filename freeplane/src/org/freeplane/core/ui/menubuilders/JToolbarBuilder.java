package org.freeplane.core.ui.menubuilders;

import javax.swing.SwingConstants;

import org.freeplane.core.ui.components.FreeplaneToolBar;

public class JToolbarBuilder implements Builder {

	@Override
	public void build(Entry target) {
		target.setComponent(new FreeplaneToolBar("main_toolbar", SwingConstants.HORIZONTAL));
	}

	@Override
	public void destroy(Entry target) {
		throw new UnsupportedOperationException();
	}

}
