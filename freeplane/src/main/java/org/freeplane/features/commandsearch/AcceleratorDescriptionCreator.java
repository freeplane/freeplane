package org.freeplane.features.commandsearch;

import javax.swing.KeyStroke;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.KeystrokeDescriber;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;

class AcceleratorDescriptionCreator {
	final private IAcceleratorMap acceleratorMap;

	final static AcceleratorDescriptionCreator INSTANCE = new AcceleratorDescriptionCreator();

	private AcceleratorDescriptionCreator() {
		this. acceleratorMap = ResourceController.getResourceController().getAcceleratorManager();
	}

	String createAcceleratorDescription(AFreeplaneAction action) {
		KeyStroke accelerator = acceleratorMap.getAccelerator(action);
		return KeystrokeDescriber.createKeystrokeDescription(accelerator);
	}
}