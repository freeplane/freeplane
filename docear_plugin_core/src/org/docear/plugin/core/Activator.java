package org.docear.plugin.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		HashMap<String, Set<DocearService>> requiredFor = new HashMap<String, Set<DocearService>>();
		
		for(ServiceReference serviceReference : dependends) {
			final DocearService service = (DocearService) context.getService(serviceReference); 
			for(DocearBundleInfo info : service.getBundleInfo().getRequiredBundles()) {
				if(info.getBundleName().startsWith("org.docear") && !info.getBundleName().equals("org.docear.plugin.core") && !inList(info, list)) {
					Set<DocearService> services = requiredFor.get(info.getBundleName());
					if (services == null) {
						services = new HashSet<DocearService>();
						requiredFor.put(info.getBundleName(), services);
					}					
					services.add(service);
					
				}
			}			
			if(!requiredFor.containsValue(service)) {
				if(!list.contains(service)) {
					list.add(service);
				}
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
	private void resolveDependencies(List<DocearService> list, Map<String, Set<DocearService>> map) {
		ArrayList<DocearService> buffer = new ArrayList<DocearService>();
		for(DocearService plugin : list) {
			if(map.containsKey(plugin.getBundleInfo().getBundleName())) {
				Set<DocearService> services = map.get(plugin.getBundleInfo().getBundleName());
				if (services != null) {
					Iterator<DocearService> iter = services.iterator();
					map.remove(plugin.getBundleInfo().getBundleName());
    				while (iter.hasNext()) {
    					DocearService inDept = iter.next();
    					iter.remove();
    					if(!hasMoreDepencies(map, inDept)) {
        					buffer.add(inDept);
        				}    					
    				}
				}
			}
		}
		for(DocearService plugin : buffer) {
			if(!list.contains(plugin)) {
				list.add(plugin);
			}
		}
	}

	private boolean hasMoreDepencies(Map<String, Set<DocearService>> map, DocearService inDept) {
		for(Set<DocearService> services : map.values()) {
			if(services.contains(inDept)) {
				return true;
			}
		}
		return false;
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
