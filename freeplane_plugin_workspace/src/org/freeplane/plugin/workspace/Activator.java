package org.freeplane.plugin.workspace;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;

import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

public class Activator implements BundleActivator {

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		props.put("mode", new String[] { MModeController.MODENAME });
		registerClasspathUrlHandler(context);
		context.registerService(IModeControllerExtensionProvider.class.getName(),
		    new IModeControllerExtensionProvider() {
			    public void installExtension(ModeController modeController) {
				    new WorkspaceController();
			    }
		    }, props);
	}
	
	private void registerClasspathUrlHandler(final BundleContext context) {
		Hashtable<String, String[]> properties = new Hashtable<String, String[]>();
        properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { WorkspaceController.WORKSPACE_RESOURCE_URL_PROTOCOL });
		
        context.registerService(URLStreamHandlerService.class.getName(), new WorkspaceUrlHandler(), properties);
    }
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {		
	}

}
