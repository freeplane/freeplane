package org.docear.plugin.search;

public class DocearSearchController {
	
	private final static DocearSearchController thisController = new DocearSearchController();
	
	
	public DocearSearchController() {
		// initialize whatever is necessary
	}
	
	
	public static DocearSearchController getController() {
		return thisController;
	}
}
