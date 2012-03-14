package org.docear.plugin.backup;

import org.docear.plugin.core.DocearService;
import org.freeplane.features.mode.ModeController;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator extends DocearService implements BundleActivator {

//	public void start(BundleContext context) throws Exception {
//		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
//		props.put("mode", new String[] { MModeController.MODENAME });
//		context.registerService(IModeControllerExtensionProvider.class.getName(),
//		    new IModeControllerExtensionProvider() {
//			    public void installExtension(ModeController modeController) {
//			    	new BackupConfiguration();
//				    new BackupStarter();
//				    new BackupPreferences();
//			    }
//		    }, props);
//	}
	
	
	public void stop(BundleContext context) throws Exception {
		System.out.println("Goodbye World!!");
	}


	public void startService(BundleContext context, ModeController modeController) {
		new BackupConfiguration();
	    new BackupStarter();
	    new BackupPreferences();		
	}

}
