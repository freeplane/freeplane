package org.freeplane.plugin.workspace;

import org.freeplane.features.mode.Controller;
import org.osgi.framework.BundleContext;

public interface IWorkspaceDependingControllerExtension {
	public void installExtension(BundleContext context, Controller controller);
}
