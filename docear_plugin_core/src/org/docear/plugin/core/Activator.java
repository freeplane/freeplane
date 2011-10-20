package org.docear.plugin.core;

import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceDependentPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class Activator extends WorkspaceDependentPlugin {

//	public void start(final BundleContext context) throws Exception {
//		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
//		props.put("mode", new String[] { MModeController.MODENAME });
//		context.registerService(IModeControllerExtensionProvider.class.getName(), new IModeControllerExtensionProvider() {
//			public void installExtension(ModeController modeController) {
//				new CoreConfiguration(modeController);
//				//startPluginServices(context, modeController);
//			}
//		}, props);
//	}
	
	public void startPlugin(BundleContext context, ModeController modeController) {
		new CoreConfiguration(modeController);
		startPluginServices(context, modeController);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void startPluginServices(BundleContext context, ModeController modeController) {
		try {
			final ServiceReference[] dependends = context.getServiceReferences(DocearPlugin.class.getName(),
					"(dependsOn="+DocearPlugin.DEPENDS_ON+")");
			if (dependends != null) {
				for (int i = 0; i < dependends.length; i++) {
					final ServiceReference serviceReference = dependends[i];
					final DocearPlugin service = (DocearPlugin) context.getService(serviceReference);
					service.startPlugin(context, modeController);
					context.ungetService(serviceReference);
				}
			}
		}
		catch (final InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}

	public void stop(BundleContext context) throws Exception {
	}

	

}
