package org.freeplane.plugin.codeexplorer;

import java.util.Hashtable;

import org.freeplane.features.mode.Controller;
import org.freeplane.main.application.CommandLineOptions;
import org.freeplane.main.osgi.IControllerExtensionProvider;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		registerMindMapModeExtension(context);
	}

	private void registerMindMapModeExtension(final BundleContext context) {
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		context.registerService(IControllerExtensionProvider.class.getName(),
		    new IControllerExtensionProvider() {
			    @Override
				public void installExtension(Controller controller, CommandLineOptions options) {
			        CodeModeControllerFactory.createModeController();
			    }

		    }, props);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
	}
}
