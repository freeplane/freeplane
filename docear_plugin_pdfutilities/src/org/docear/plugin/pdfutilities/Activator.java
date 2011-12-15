package org.docear.plugin.pdfutilities;

import org.docear.plugin.core.DocearPlugin;
import org.freeplane.features.mode.ModeController;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator extends DocearPlugin implements BundleActivator {

	
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

	public void startPlugin(BundleContext context, ModeController modeController) {
		new PdfUtilitiesController(modeController);
	}

}
