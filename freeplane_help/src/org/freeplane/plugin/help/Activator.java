package org.freeplane.plugin.help;

import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.main.mindmapmode.MModeControllerFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		final ModeController modeController = MModeControllerFactory.getModeController();
		final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
		menuBuilder.addAnnotatedAction(new FreeplaneHelpStarter(modeController.getController()));
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
	}
}
