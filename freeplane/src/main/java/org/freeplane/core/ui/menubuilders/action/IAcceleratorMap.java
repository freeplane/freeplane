package org.freeplane.core.ui.menubuilders.action;

import javax.swing.KeyStroke;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IAcceleratorChangeListener;
import org.freeplane.features.mode.FreeplaneActions;

public interface IAcceleratorMap {
	void setUserDefinedAccelerator(AFreeplaneAction action);
	void setDefaultAccelerator(final AFreeplaneAction action, final String accelerator);
	public KeyStroke getAccelerator(AFreeplaneAction action);

	void addAcceleratorChangeListener(FreeplaneActions freeplaneActions, IAcceleratorChangeListener changeListener);
	void removeActionAccelerator(FreeplaneActions freeplaneActions, AFreeplaneAction action);
}
