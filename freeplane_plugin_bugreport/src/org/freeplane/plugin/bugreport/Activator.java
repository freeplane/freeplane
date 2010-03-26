package org.freeplane.plugin.bugreport;

import java.util.logging.Logger;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.main.osgi.IControllerExtensionProvider;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	private XmlRpcHandler handler;
	private Logger parentLogger;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		parentLogger = Logger.getAnonymousLogger().getParent();
		handler = new XmlRpcHandler();
		parentLogger.addHandler(handler);
		context.registerService(IControllerExtensionProvider.class.getName(),
			    new IControllerExtensionProvider() {
				    public void installExtension(final Controller Controller) {
				    	handler.setBugReportListener(new ManualBugReporter(Controller));
				    }
			    }, null);
	}


	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
		parentLogger.removeHandler(handler);
	}
}
