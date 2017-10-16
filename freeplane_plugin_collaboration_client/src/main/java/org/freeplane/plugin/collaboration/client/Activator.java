package org.freeplane.plugin.collaboration.client;

import java.awt.Component;
import java.awt.Window;

import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.main.osgi.IControllerExtensionProvider;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.freeplane.plugin.collaboration.client.ui.EventStreamDialog;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		
		context.registerService(IControllerExtensionProvider.class.getName(), new IControllerExtensionProvider() {
			@Override
			public void installExtension(Controller controller) {
				Component menuComponent = controller.getViewController().getMenuComponent();
				new EventStreamDialog((Window)menuComponent).show();
			}
		}, null);

		context.registerService(IModeControllerExtensionProvider.class.getName(), new IModeControllerExtensionProvider() {
			@Override
			public void installExtension(ModeController modeController) {
				// TODO
			}
		}, null);
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		// intentionally left blank
	}
}
