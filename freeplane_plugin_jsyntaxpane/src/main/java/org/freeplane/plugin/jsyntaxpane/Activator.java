package org.freeplane.plugin.jsyntaxpane;

import java.awt.GraphicsEnvironment;
import java.util.Hashtable;

import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.syntaxkits.GroovySyntaxKit;

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
	public void start(final BundleContext context) throws Exception {
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		props.put("mode", new String[] { MModeController.MODENAME });
		context.registerService(IModeControllerExtensionProvider.class.getName(),
		    new IModeControllerExtensionProvider() {
			    public void installExtension(ModeController modeController) {
			    	if(! GraphicsEnvironment.isHeadless())
			    		initJSyntaxPane(context);
				    //new ScriptingRegistration(modeController);
			    }
		    }, props);
	}
	
	private void initJSyntaxPane(BundleContext context) {
	    final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
	    try {
            Thread.currentThread().setContextClassLoader(DefaultSyntaxKit.class.getClassLoader());
            DefaultSyntaxKit.initKit();
            final String components = "jsyntaxpane.components.PairsMarker" //
            		+ ", jsyntaxpane.components.LineNumbersRuler" //
            		+ ", jsyntaxpane.components.TokenMarker" //
            		+ ", org.freeplane.plugin.jsyntaxpane.NodeIdHighLighter";
            	new GroovySyntaxKit().setProperty("Components", components);
        }
        finally {
        	Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
	}
}
