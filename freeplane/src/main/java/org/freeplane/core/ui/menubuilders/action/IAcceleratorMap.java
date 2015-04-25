package org.freeplane.core.ui.menubuilders.action;

import javax.swing.KeyStroke;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IAcceleratorChangeListener;

public interface IAcceleratorMap {
	void setDefaultAccelerator(AFreeplaneAction action);
	void setDefaultAccelerator(final AFreeplaneAction action, final String accelerator);
	public KeyStroke getAccelerator(AFreeplaneAction action);
	void addAcceleratorChangeListener(IAcceleratorChangeListener changeListener);
	void removeAction(AFreeplaneAction action);
}
