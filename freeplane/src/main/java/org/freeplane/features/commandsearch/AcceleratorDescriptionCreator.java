package org.freeplane.features.commandsearch;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;

class AcceleratorDescriptionCreator {
	final private IAcceleratorMap acceleratorMap;

	final static AcceleratorDescriptionCreator INSTANCE = new AcceleratorDescriptionCreator();

	private AcceleratorDescriptionCreator() {
		this. acceleratorMap = ResourceController.getResourceController().getAcceleratorManager();
	}

	String createAcceleratorDescription(AFreeplaneAction action) {
		KeyStroke accelerator = acceleratorMap.getAccelerator(action);
		String acceleratorText =  null;
		if (accelerator !=  null)
		{
			acceleratorText = "";
			int modifiers = accelerator.getModifiers();
			if (modifiers > 0)
			{
				acceleratorText += InputEvent.getModifiersExText(modifiers);
				acceleratorText += "+";
			}
			acceleratorText += KeyEvent.getKeyText(accelerator.getKeyCode());
		}
		return acceleratorText;
	}
}