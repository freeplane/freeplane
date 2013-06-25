package org.freeplane.plugin.workspace;

import java.util.Hashtable;

import org.freeplane.core.ui.FreeplaneActionCascade;
import org.freeplane.core.user.UserAccountController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.main.osgi.IControllerExtensionProvider;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.freeplane.plugin.workspace.actions.WorkspaceQuitAction;
import org.freeplane.plugin.workspace.features.ProjectURLHandler;
import org.freeplane.plugin.workspace.features.PropertyUrlHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

public class Activator implements BundleActivator {
	public void start(final BundleContext context) throws Exception {	
		registerClasspathUrlHandler(context);
			
		context.registerService(IControllerExtensionProvider.class.getName(), new IControllerExtensionProvider() {
			public void installExtension(Controller controller) {
				WorkspaceController.install(controller);
				UserAccountController.install(controller);
				LogUtils.info("Workspace controller installed.");
				startControllerExtensions(context, controller);
			}
		}, null);
		
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		//WORKSPACE - todo(low): list all modes from freeplane controller		
		props.put("mode", new String[] { MModeController.MODENAME });
		
		context.registerService(IModeControllerExtensionProvider.class.getName(),
		    new IModeControllerExtensionProvider() {
				public void installExtension(ModeController modeController) {
					addToQuitChain();
			    	WorkspaceController.getController().installMode(modeController);
				    startPluginServices(context, modeController);
				    WorkspaceController.getController().startModeExtension(modeController);
			    }
		    }, props);
	}
	
	protected final void addToQuitChain() {
		FreeplaneActionCascade.addAction(new WorkspaceQuitAction());
	}

	private void registerClasspathUrlHandler(final BundleContext context) {
		Hashtable<String, String[]> properties = new Hashtable<String, String[]>();
//        properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { WorkspaceController.WORKSPACE_RESOURCE_URL_PROTOCOL });
//        context.registerService(URLStreamHandlerService.class.getName(), new WorkspaceUrlHandler(), properties);
        
        properties = new Hashtable<String, String[]>();
        properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { WorkspaceController.PROJECT_RESOURCE_URL_PROTOCOL });
        context.registerService(URLStreamHandlerService.class.getName(), new ProjectURLHandler(), properties);
        
        properties = new Hashtable<String, String[]>();
        properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { WorkspaceController.PROPERTY_RESOURCE_URL_PROTOCOL });
        context.registerService(URLStreamHandlerService.class.getName(), new PropertyUrlHandler(), properties);
    }
	
	public void stop(BundleContext context) throws Exception {
		LogUtils.info("Workspace: shuting down ...");
//		WorkspaceUtils.saveCurrentConfiguration();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void startPluginServices(BundleContext context, ModeController modeController) {
		try {
			final ServiceReference[] dependends = context.getServiceReferences(WorkspaceDependingService.class.getName(),
					"(dependsOn="+ WorkspaceDependingService.DEPENDS_ON +")");
			if (dependends != null) {
				for (int i = 0; i < dependends.length; i++) {
					final ServiceReference serviceReference = dependends[i];
					final WorkspaceDependingService service = (WorkspaceDependingService) context.getService(serviceReference);
					service.startPlugin(context, modeController);
					context.ungetService(serviceReference);
				}
			}
		}
		catch (final InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void startControllerExtensions(BundleContext context, Controller controller) {
		try {
			final ServiceReference[] extensions = context.getServiceReferences(IWorkspaceDependingControllerExtension.class.getName(), "(dependsOn="+ WorkspaceDependingService.DEPENDS_ON +")");
			if (extensions != null) {
				for (ServiceReference serviceReference : extensions) {
					final IWorkspaceDependingControllerExtension extension = (IWorkspaceDependingControllerExtension) context.getService(serviceReference);
					extension.installExtension(context, controller);
					context.ungetService(serviceReference);
				}
			}
		}
		catch (final InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}
}
