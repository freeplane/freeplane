package org.freeplane.plugin.bugreport;

import java.util.logging.Logger;

import org.freeplane.features.mode.Controller;
import org.freeplane.main.osgi.IControllerExtensionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	private ReportGenerator handler;
	private Logger parentLogger;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		parentLogger = Logger.getAnonymousLogger().getParent();
		handler = new ReportGenerator();
		parentLogger.addHandler(handler);
		context.registerService(IControllerExtensionProvider.class.getName(), new IControllerExtensionProvider() {
			public void installExtension(Controller controller) {
				handler.setBugReportListener(new ManualBugReporter());
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
