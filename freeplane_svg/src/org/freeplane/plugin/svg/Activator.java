package org.freeplane.plugin.svg;

import java.util.Hashtable;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.main.osgi.ModeControllerExtensionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		props.put("mode", new String[] { MModeController.MODENAME });
		context.registerService(ModeControllerExtensionProvider.class.getName(), new ModeControllerExtensionProvider() {
			public void installExtension(ModeController modeController) {
				final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
				final Controller controller = modeController.getController();
				menuBuilder.addAnnotatedAction(new ExportPdf(controller));
				menuBuilder.addAnnotatedAction(new ExportSvg(controller));
			}
		}, props);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
	}
}
