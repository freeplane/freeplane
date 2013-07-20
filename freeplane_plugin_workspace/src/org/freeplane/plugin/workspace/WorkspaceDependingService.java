/**
 * author: Marcel Genzmehr
 * 20.10.2011
 */
package org.freeplane.plugin.workspace;

import java.util.Collection;
import java.util.Hashtable;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.ModeController;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * 
 */
public abstract class WorkspaceDependingService implements BundleActivator{
	public final static String DEPENDS_ON = "org.freeplane.plugin.workspace";
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public abstract void startPlugin(BundleContext context, ModeController modeController);
	
	protected abstract Collection<IWorkspaceDependingControllerExtension> getControllerExtensions();
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public final void start(BundleContext context) throws Exception {
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		props.put("dependsOn", new String[] { DEPENDS_ON }); //$NON-NLS-1$

		Collection<IWorkspaceDependingControllerExtension> extensions = getControllerExtensions();
		if(extensions != null) {
			for(IWorkspaceDependingControllerExtension provider : extensions) {
				try {
					context.registerService(IWorkspaceDependingControllerExtension.class.getName(), provider, props);
				}
				catch (Exception e) {
					LogUtils.warn(provider.getClass() +" has not been registered", e);
				}
			}
		}
		context.registerService(WorkspaceDependingService.class.getName(), this, props);
	}
	
}
