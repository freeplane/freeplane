package org.freeplane.plugin.svg;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.mindmapmode.MModeController;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		final MenuBuilder menuBuilder = Controller.getController().getModeController(
		    MModeController.MODENAME).getUserInputListenerFactory().getMenuBuilder();
		menuBuilder.addAnnotatedAction(new ExportPdf());
		menuBuilder.addAnnotatedAction(new ExportSvg());
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
	}
}
