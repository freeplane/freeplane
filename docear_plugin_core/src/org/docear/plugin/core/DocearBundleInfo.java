/**
 * author: Marcel Genzmehr
 * 19.10.2011
 */
package org.docear.plugin.core;

import java.util.Dictionary;
import java.util.Vector;

import org.osgi.framework.BundleContext;

/**
 * 
 */
public class DocearBundleInfo {
	private final String bundleName;
	private final String bundleVersion;
	private final DocearBundleInfo[] requiredBundles;
	
	

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public DocearBundleInfo(BundleContext context) {
		Dictionary<String, String> manifest = context.getBundle().getHeaders();
 		bundleName = extractBundleName(manifest);
 		bundleVersion = extractBundleVersion(manifest);
		requiredBundles = extractRequiredBundles(manifest);
	}
	
	private DocearBundleInfo(final String bundleName, final String bundleVersion) {
		this.bundleName = bundleName;
 		this.bundleVersion = bundleVersion;
		requiredBundles = new DocearBundleInfo[]{};
	}
	
	

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	private String extractBundleName(Dictionary<String, String> manifest) {
		return manifest.get("Bundle-Name");
	}
	
	private String extractBundleVersion(Dictionary<String, String> manifest) {
		return manifest.get("Bundle-Version");
	}
	
	private DocearBundleInfo[] extractRequiredBundles(Dictionary<String, String> manifest) {
		Vector<DocearBundleInfo> requires = new Vector<DocearBundleInfo>();
		for(String req : manifest.get("Require-Bundle").split(",")) {
			String[] token = req.split(";");
			String name = token[0].substring(token[0].indexOf("=")+1);
			String version = token[1].substring(token[1].indexOf("=")+2);
			version = version.substring(0, version.length()-1);
			requires.add(new DocearBundleInfo(name, version));
		}
 		return requires.toArray(new DocearBundleInfo[]{});
	}



	public String getBundleName() {
		return bundleName;
	}
	

	public DocearBundleInfo[] getRequiredBundles() {
		return requiredBundles;
	}



	public String getBundleVersion() {
		return bundleVersion;
	}

	public boolean equals(Object o) {
		if(o instanceof DocearBundleInfo) {
			if(((DocearBundleInfo) o).getBundleName().equals(this.getBundleName()) &&
			((DocearBundleInfo) o).getBundleVersion().equals(this.getBundleVersion())) {
				return true;
			}			
		}
		return false;
	}
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
