package org.docear.plugin.communications;

import org.docear.plugin.core.DocearPlugin;
import org.freeplane.features.mode.ModeController;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator extends DocearPlugin implements BundleActivator {

//	public void start(BundleContext context) throws Exception {
//		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
//		props.put("mode", new String[] { MModeController.MODENAME });
//		context.registerService(IModeControllerExtensionProvider.class.getName(),
//		    new IModeControllerExtensionProvider() {
//			    public void installExtension(ModeController modeController) {
//			    	new Communication();
//				    
//			    }
//		    }, props);
//	}
	

	public void stop(BundleContext context) throws Exception {
	}

	public void startPlugin(BundleContext context, ModeController modeController) {
		// TODO: DOCEAR - initialize the module		
	}

}
