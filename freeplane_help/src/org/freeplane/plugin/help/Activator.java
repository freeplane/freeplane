package org.freeplane.plugin.help;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.main.mindmapmode.MModeControllerFactory;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
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
		context.registerService(IModeControllerExtensionProvider.class.getName(), new IModeControllerExtensionProvider() {
			public void installExtension(ModeController modeController) {
				final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
				menuBuilder.addAnnotatedAction(new FreeplaneHelpStarter(modeController.getController()));
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
