package org.docear.plugin.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceDependentService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class Activator extends WorkspaceDependentService {
	
	public void startPlugin(BundleContext context, ModeController modeController) {
		loadAndStoreVersion();	
		new CoreConfiguration(modeController);		
		startPluginServices(context, modeController);
	}
		
	@SuppressWarnings("rawtypes")
	protected void startPluginServices(BundleContext context, ModeController modeController) {		
		try {
			final ServiceReference[] dependends = context.getServiceReferences(DocearService.class.getName(),
					"(dependsOn="+DocearService.DEPENDS_ON+")");
			if (dependends != null) {
				List<DocearService> services = sortOnDependencies(dependends, context);
				for(DocearService service : services) {
					service.startService(context, modeController);
				}
				
			}
		}
		catch (final InvalidSyntaxException e) {
			e.printStackTrace();
		}
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
	
	private void loadAndStoreVersion() {
		//FIXME: has to be called before the splash is showing
		final Properties versionProperties = new Properties();
		InputStream in = null;
		try {
			in = Activator.this.getClass().getResource("/version.properties").openStream();
			versionProperties.load(in);
		}
		catch (final IOException e) {
			
		}
		
		final Properties buildProperties = new Properties();
		in = null;
		try {
			in = Activator.this.getClass().getResource("/build.number").openStream();
			buildProperties.load(in);
		}
		catch (final IOException e) {
			
		}
		final String versionNumber = versionProperties.getProperty("docear_version");
		final String versionStatus = versionProperties.getProperty("docear_version_status");
		final String versionStatusNumber = versionProperties.getProperty("docear_version_status_number");
		final int versionBuild = Integer.parseInt(buildProperties.getProperty("build.number")) -1;
		ResourceController.getResourceController().setProperty("docear_version", versionNumber);
		ResourceController.getResourceController().setProperty("docear_status", versionStatus+" "+versionStatusNumber+" build "+versionBuild);
		
	}

	

}
