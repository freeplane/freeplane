package org.docear.plugin.communications;

import org.docear.plugin.core.DocearService;
import org.freeplane.features.mode.ModeController;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sun.jersey.api.client.Client;

public class Activator extends DocearService implements BundleActivator {

	// public void start(BundleContext context) throws Exception {
	// final Hashtable<String, String[]> props = new Hashtable<String,
	// String[]>();
	// props.put("mode", new String[] { MModeController.MODENAME });
	// context.registerService(IModeControllerExtensionProvider.class.getName(),
	// new IModeControllerExtensionProvider() {
	// public void installExtension(ModeController modeController) {
	// new Communication();
	//
	// }
	// }, props);
	// }

	public void stop(BundleContext context) throws Exception {
	}

	public void startService(BundleContext context, ModeController modeController) {

		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		Client client = Client.create();
		client.setReadTimeout(CommunicationsConfiguration.READ_TIMEOUT);
		client.setConnectTimeout(CommunicationsConfiguration.CONNECTION_TIMEOUT);
		Thread.currentThread().setContextClassLoader(contextClassLoader);

		// TODO: DOCEAR - initialize the module
	}

}
