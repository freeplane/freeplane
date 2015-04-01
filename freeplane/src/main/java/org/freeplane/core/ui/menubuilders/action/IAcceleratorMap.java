package org.freeplane.core.ui.menubuilders.action;

import javax.swing.KeyStroke;

import org.freeplane.core.ui.IAcceleratorChangeListener;

public interface IAcceleratorMap {
	void setDefaultAccelerator(final String actionKey, final String accelerator);
	public KeyStroke getAccelerator(String actionKey);
	void addAcceleratorChangeListener(IAcceleratorChangeListener changeListener);
}
