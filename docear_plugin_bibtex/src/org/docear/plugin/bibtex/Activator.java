package org.docear.plugin.bibtex;

import java.util.Hashtable;


import org.docear.plugin.core.DocearBundleInfo;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		final DocearBundleInfo info = new DocearBundleInfo(context);
		props.put("mode", new String[] { MModeController.MODENAME }); //$NON-NLS-1$
		context.registerService(IModeControllerExtensionProvider.class.getName(),
		    new IModeControllerExtensionProvider() {
			    public void installExtension(ModeController modeController) {
				    new ReferencesController(modeController);
			    }
		    }, props);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		System.out.println("Goodbye World!!"); //$NON-NLS-1$
	}

}
