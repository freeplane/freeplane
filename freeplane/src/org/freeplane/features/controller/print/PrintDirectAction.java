package org.freeplane.features.controller.print;

import org.freeplane.core.controller.Controller;

class PrintDirectAction extends PrintAction {
	private static final long serialVersionUID = 6534539560828315255L;

	static final String NAME = "printDirect";

	PrintDirectAction(Controller controller, PrintController printController) {
		super(controller, printController, false);
	}

	@Override
	public String getName() {
		return NAME;
	}


}
