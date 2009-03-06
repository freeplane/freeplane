package org.freeplane.features.controller.print;

import org.freeplane.core.controller.Controller;

class PrintDirectAction extends PrintAction {
	static final String NAME = "printDirect";
	private static final long serialVersionUID = 6534539560828315255L;

	PrintDirectAction(final Controller controller, final PrintController printController) {
		super(controller, printController, false);
	}

	@Override
	public String getName() {
		return NAME;
	}
}
