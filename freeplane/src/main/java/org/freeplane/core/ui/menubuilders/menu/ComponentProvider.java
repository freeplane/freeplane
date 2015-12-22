package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;

import org.freeplane.core.ui.menubuilders.generic.Entry;

public interface ComponentProvider {
	public Component createComponent(Entry entry);
}
