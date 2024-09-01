package org.freeplane.plugin.bugreport;

import java.util.logging.Logger;

import org.freeplane.features.mode.Controller;
import org.freeplane.main.application.CommandLineOptions;
import org.freeplane.main.mindmapmode.stylemode.ExtensionInstaller;
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
		getRootLogger().addHandler(handler);
		context.registerService(IControllerExtensionProvider.class.getName(), new IControllerExtensionProvider() {
			@Override
			public void installExtension(Controller controller, CommandLineOptions options, ExtensionInstaller.Context context) {
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
	    getRootLogger().removeHandler(handler);
	}

	private Logger getRootLogger() {
	    return Logger.getAnonymousLogger().getParent();
	}

}
