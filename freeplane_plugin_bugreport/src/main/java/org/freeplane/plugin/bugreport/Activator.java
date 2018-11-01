package org.freeplane.plugin.bugreport;

import org.freeplane.core.util.logging.LogHandlers;
import org.freeplane.features.mode.Controller;
import org.freeplane.main.osgi.IControllerExtensionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	private ReportGenerator handler;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		handler = new ReportGenerator();
		LogHandlers.addHandler(handler);
		context.registerService(IControllerExtensionProvider.class.getName(), new IControllerExtensionProvider() {
			@Override
			public void installExtension(Controller controller) {
				handler.setBugReportListener(new ManualBugReporter());
			}
		}, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		LogHandlers.removeHandler(handler);
	}
}
