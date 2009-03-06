package org.freeplane.features.controller.help;

import org.freeplane.core.controller.Controller;

class FaqOpenURLAction extends OpenURLAction {
	private static final String NAME = "faq";
	/**
	 * 
	 */
	private static final long serialVersionUID = -893393063182710686L;

	FaqOpenURLAction(final Controller controller, final String description, final String url) {
		super(controller, description, url);
	}

	@Override
	public String getName() {
		return NAME;
	}
}
