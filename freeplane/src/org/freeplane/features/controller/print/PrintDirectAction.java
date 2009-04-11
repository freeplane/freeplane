package org.freeplane.features.controller.print;

import org.freeplane.core.controller.Controller;

class PrintDirectAction extends PrintAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	PrintDirectAction(final Controller controller, final PrintController printController) {
		super("PrintDirectAction", controller, printController, false);
	}
}
