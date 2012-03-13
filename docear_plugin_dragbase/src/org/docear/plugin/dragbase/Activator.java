package org.docear.plugin.dragbase;

import org.docear.plugin.core.DocearService;
import org.freeplane.features.mode.ModeController;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;



/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends DocearService implements BundleActivator {

	
//	public void start(BundleContext context) throws Exception {
//		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
//		props.put("mode", new String[] { MModeController.MODENAME }); //$NON-NLS-1$
//		context.registerService(IModeControllerExtensionProvider.class.getName(),
//		    new IModeControllerExtensionProvider() {
//			    public void installExtension(ModeController modeController) {
//				    DragbaseController.startDragbasePlugin(modeController);
//			    }
//		    }, props);		
//	}
	
	public void stop(BundleContext context) throws Exception {		
	}

	@Override
	public void startService(BundleContext context,	ModeController modeController) {
		DragbaseController.startDragbasePlugin(modeController);		
	}

}
