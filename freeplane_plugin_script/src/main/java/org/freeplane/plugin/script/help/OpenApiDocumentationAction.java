package org.freeplane.plugin.script.help;

import java.io.File;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.help.OpenURLAction;

@SuppressWarnings("serial")
public class OpenApiDocumentationAction extends OpenURLAction {

	public OpenApiDocumentationAction() {
		super("OpenApiDocumentationAction", uri());
	}

	private static String uri() {
		return new File(ResourceController.getResourceController().getInstallationBaseDir()).toURI().toString() + "/doc/api/index.html";
	}
}
