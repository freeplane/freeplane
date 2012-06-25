package org.docear.plugin.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.main.osgi.IControllerExtensionProvider;
import org.freeplane.plugin.workspace.WorkspaceDependentService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class Activator extends WorkspaceDependentService {
	CoreConfiguration config;
	
	public final void startPlugin(BundleContext context, ModeController modeController) {	
		getConfig().initMode(modeController);		
		startPluginServices(context, modeController);
	}
	
	protected Collection<IControllerExtensionProvider> getControllerExtensions() {
		List<IControllerExtensionProvider> controllerExtensions = new ArrayList<IControllerExtensionProvider>();
		controllerExtensions.add(new IControllerExtensionProvider() {
			public void installExtension(Controller controller) {			
				getConfig().initController(controller);
			}
		});
		return controllerExtensions;
	}
	
	private CoreConfiguration getConfig() {
		if(config == null) {
			config = new CoreConfiguration();
		}
		return config;
	}
		
	@SuppressWarnings("rawtypes")
	protected final void startPluginServices(BundleContext context, ModeController modeController) {		
		try {
			final ServiceReference[] dependends = context.getServiceReferences(DocearService.class.getName(),
					"(dependsOn="+DocearService.DEPENDS_ON+")");
			if (dependends != null) {
				List<DocearService> services = sortOnDependencies(dependends, context);
				for(DocearService service : services) {
					if(isValid(service)) {
						service.startService(context, modeController);
					}
				}
				
			}
		}
		catch (final InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}

	private boolean isValid(DocearService service) {
		if(isBlacklisted(service)) {
			return false;
		}
		return true;
	}

	private boolean isBlacklisted(DocearService service) {
		if("org.docear.plugin.backup".equals(service.getBundleInfo().getBundleName())) {
			return true;
		}
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<DocearService> sortOnDependencies(ServiceReference[] dependends, BundleContext context) {
		ArrayList<DocearService> list = new ArrayList<DocearService>();
		HashMap<String, DocearService> requiredFor = new HashMap<String, DocearService>();
		
		for(ServiceReference serviceReference : dependends) {
			final DocearService service = (DocearService) context.getService(serviceReference); 
			for(DocearBundleInfo info : service.getBundleInfo().getRequiredBundles()) {
				if(info.getBundleName().startsWith("org.docear") && !info.getBundleName().equals("org.docear.plugin.core") && !inList(info, list)) {
					requiredFor.put(info.getBundleName(), service);
				}
			}			
			if(!requiredFor.containsValue(service)) {
				list.add(service);
				continue;
			}
			
			resolveDependencies(list, requiredFor);			
		}
		while( requiredFor.size() > 0) {
			resolveDependencies(list, requiredFor);
		}
		
		return list;
	}

	/**
	 * @param list
	 * @param requiredFor
	 */
	private void resolveDependencies(List<DocearService> list, Map<String, DocearService> map) {
		ArrayList<DocearService> buffer = new ArrayList<DocearService>();
		for(DocearService plugin : list) {
			if(map.containsKey(plugin.getBundleInfo().getBundleName())) {
				DocearService inDept = map.get(plugin.getBundleInfo().getBundleName());
				map.remove(plugin.getBundleInfo().getBundleName());
				if(!map.containsValue(inDept)) {
					buffer.add(inDept);
				}				
			}
		}
		for(DocearService plugin : buffer) {
			list.add(plugin);
		}
	}

	/**
	 * @param info
	 * @param list
	 * @return <code>true</code> if ..., else <code>false</code>
	 */
	private boolean inList(DocearBundleInfo info, List<DocearService> list) {
		for(DocearService plugin : list) {
			if(plugin.getBundleInfo().getBundleName().equals(info.getBundleName())) {
				return true;
			}
		}
		return false;
	}

	public void stop(BundleContext context) throws Exception {
	}
}
