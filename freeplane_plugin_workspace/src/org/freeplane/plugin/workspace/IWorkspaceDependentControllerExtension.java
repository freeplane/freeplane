package org.freeplane.plugin.workspace;

import org.freeplane.features.mode.Controller;
import org.osgi.framework.BundleContext;

public interface IWorkspaceDependentControllerExtension {
	public void installExtension(BundleContext context, Controller controller);
}
