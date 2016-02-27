package org.freeplane.core.ui;

import javax.swing.ButtonModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.freeplane.core.resources.ResourceController;

public class ButtonModelStateChangeListenerForProperty implements ChangeListener {
	final private String propertyName;

	public ButtonModelStateChangeListenerForProperty(String propertyName) {
		super();
		this.propertyName = propertyName;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		ButtonModel buttonModel = (ButtonModel)e.getSource();
		ResourceController.getResourceController().setProperty(propertyName, buttonModel.isSelected());
	}
}