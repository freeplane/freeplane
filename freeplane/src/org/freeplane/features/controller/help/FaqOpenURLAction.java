package org.freeplane.features.controller.help;

import org.freeplane.core.controller.Controller;

class FaqOpenURLAction extends OpenURLAction {

	FaqOpenURLAction(Controller controller, String description, String url) {
	    super(controller, description, url);
    }

	private static final String NAME = "faq";

	@Override
    public String getName() {
	    return NAME;
    }

	/**
     * 
     */
    private static final long serialVersionUID = -893393063182710686L;
}
