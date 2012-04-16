package org.docear.plugin.search;

import org.docear.plugin.core.ALanguageController;

public class DocearSearchController extends ALanguageController {
	
	private final static DocearSearchController thisController = new DocearSearchController();
	
	
	public DocearSearchController() {
		super();
		// initialize whatever is necessary
	}
	
	
	public static DocearSearchController getController() {
		return thisController;
	}
}
