package org.freeplane.plugin.workspace.features;

import org.freeplane.features.mode.ModeController;

public class ModeControlAlreadyRegisteredException extends Exception {

	private static final long serialVersionUID = 398606359024873584L;

	public ModeControlAlreadyRegisteredException(Class<? extends ModeController> modeController) {
		super("a workspace control is already registered for this mode: "+modeController);
	}

}
