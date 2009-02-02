package org.freeplane.plugin.svg;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.main.mindmapmode.MModeControllerFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		final MModeController modeController = MModeControllerFactory.getModeController();
		final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
		final Controller controller = modeController.getController();
		menuBuilder.addAnnotatedAction(new ExportPdf(controller));
		menuBuilder.addAnnotatedAction(new ExportSvg(controller));
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
	}
}
