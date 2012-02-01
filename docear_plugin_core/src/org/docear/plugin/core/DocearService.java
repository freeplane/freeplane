/**
 * author: Marcel Genzmehr
 * 19.10.2011
 */
package org.docear.plugin.core;

import java.util.Hashtable;

import org.freeplane.features.mode.ModeController;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * 
 */
public abstract class DocearService implements BundleActivator {
	public final static String DEPENDS_ON = "org.docear.plugin.core";
	private DocearBundleInfo info;
	
	public abstract void startService(BundleContext context, ModeController modeController);
	
	public void start(BundleContext context) throws Exception {
		this.info = new DocearBundleInfo(context);
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		props.put("dependsOn", new String[] { DEPENDS_ON }); //$NON-NLS-1$
		context.registerService(DocearService.class.getName(), this, props);
	}
	
	public DocearBundleInfo getBundleInfo() {		
		return info;
	}
}
