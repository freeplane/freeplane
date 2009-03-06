package org.freeplane.features.controller.help;

import org.freeplane.core.controller.Controller;

class WebDocuAction extends OpenURLAction {
	private static final String NAME = "webDocu";
	private static final long serialVersionUID = 9103877920868471662L;

	WebDocuAction(final Controller controller, final String description, final String url) {
		super(controller, description, url);
	}

	@Override
	public String getName() {
		return NAME;
	}
}
