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
import org.freeplane.plugin.workspace.WorkspaceDependentPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class Activator extends WorkspaceDependentPlugin {
	
	public void startPlugin(BundleContext context, ModeController modeController) {
		new CoreConfiguration(modeController);
		loadAndStoreVersion();
		startPluginServices(context, modeController);
	}
	
	@SuppressWarnings("rawtypes")
	protected void startPluginServices(BundleContext context, ModeController modeController) {		
		try {
			final ServiceReference[] dependends = context.getServiceReferences(DocearPlugin.class.getName(),
					"(dependsOn="+DocearPlugin.DEPENDS_ON+")");
			if (dependends != null) {
				List<DocearPlugin> services = sortOnDependencies(dependends, context);
				for(DocearPlugin service : services) {
					service.startPlugin(context, modeController);
				}
				
			}
		}
		catch (final InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<DocearPlugin> sortOnDependencies(ServiceReference[] dependends, BundleContext context) {
		ArrayList<DocearPlugin> list = new ArrayList<DocearPlugin>();
		HashMap<String, DocearPlugin> requiredFor = new HashMap<String, DocearPlugin>();
		
		for(ServiceReference serviceReference : dependends) {
			final DocearPlugin service = (DocearPlugin) context.getService(serviceReference); 
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
	private void resolveDependencies(List<DocearPlugin> list, Map<String, DocearPlugin> map) {
		ArrayList<DocearPlugin> buffer = new ArrayList<DocearPlugin>();
		for(DocearPlugin plugin : list) {
			if(map.containsKey(plugin.getBundleInfo().getBundleName())) {
				DocearPlugin inDept = map.get(plugin.getBundleInfo().getBundleName());
				map.remove(plugin.getBundleInfo().getBundleName());
				if(!map.containsValue(inDept)) {
					buffer.add(inDept);
				}				
			}
		}
		for(DocearPlugin plugin : buffer) {
			list.add(plugin);
		}
	}

	/**
	 * @param info
	 * @param list
	 * @return <code>true</code> if ..., else <code>false</code>
	 */
	private boolean inList(DocearBundleInfo info, List<DocearPlugin> list) {
		for(DocearPlugin plugin : list) {
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
			in = CoreConfiguration.class.getResource("/version.properties").openStream();
			versionProperties.load(in);
		}
		catch (final IOException e) {
			
		}
		final String versionNumber = versionProperties.getProperty("docear_version");
		final String versionStatus = versionProperties.getProperty("docear_version_status");
		final String versionStatusNumber = versionProperties.getProperty("docear_version_status_number");
		final String versionBuild = versionProperties.getProperty("docear_version_build");
		ResourceController.getResourceController().setProperty("docear_version", versionNumber);
		ResourceController.getResourceController().setProperty("docear_status", versionStatus+" "+versionStatusNumber+" build"+versionBuild);
		
	}

	

}
