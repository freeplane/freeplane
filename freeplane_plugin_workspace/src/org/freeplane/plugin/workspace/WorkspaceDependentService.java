/**
 * author: Marcel Genzmehr
 * 20.10.2011
 */
package org.freeplane.plugin.workspace;

import java.util.Hashtable;

import org.freeplane.features.mode.ModeController;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * 
 */
public abstract class WorkspaceDependentService implements BundleActivator{
	public final static String DEPENDS_ON = "org.freeplane.plugin.workspace";
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public abstract void startPlugin(BundleContext context, ModeController modeController);
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void start(BundleContext context) throws Exception {
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		props.put("dependsOn", new String[] { DEPENDS_ON }); //$NON-NLS-1$
		context.registerService(WorkspaceDependentService.class.getName(), this, props);
	}
	
}
