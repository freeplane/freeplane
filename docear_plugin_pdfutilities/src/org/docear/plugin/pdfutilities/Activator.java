package org.docear.plugin.pdfutilities;

import java.util.Collection;

import org.docear.plugin.core.DocearService;
import org.freeplane.features.mode.ModeController;
import org.freeplane.main.osgi.IControllerExtensionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator extends DocearService implements BundleActivator {

	
//	public void start(BundleContext context) throws Exception {
//		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
//		props.put("mode", new String[] { MModeController.MODENAME }); //$NON-NLS-1$
//		context.registerService(IModeControllerExtensionProvider.class.getName(),
//		    new IModeControllerExtensionProvider() {
//			    public void installExtension(ModeController modeController) {
//			    	new PdfUtilitiesController(modeController);
//			    }
//		    }, props);
//	}
	
	
	public void stop(BundleContext context) throws Exception {
	}

	public void startService(BundleContext context, ModeController modeController) {
		new PdfUtilitiesController(modeController);
	}

	protected Collection<IControllerExtensionProvider> getControllerExtensions() {
		return null;
	}

}
