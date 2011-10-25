/**
 * author: Marcel Genzmehr
 * 19.10.2011
 */
package org.docear.plugin.core;

import java.util.Dictionary;
import java.util.Vector;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

/**
 * 
 */
public class DocearBundleInfo {
	private final String bundleName;
	private final Version bundleVersion;
	private final DocearBundleInfo[] requiredBundles;
	private final Bundle bundle;
	
	

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public DocearBundleInfo(BundleContext context) {
 		bundleName = context.getBundle().getSymbolicName();
 		bundleVersion = new Version("1.0.0");//context.getBundle().getVersion();
		requiredBundles = extractRequiredBundles(context.getBundle().getHeaders());
		bundle = context.getBundle();
	}
	
	private DocearBundleInfo(final String bundleName, final Version bundleVersion) {
		this.bundleName = bundleName;
 		this.bundleVersion = bundleVersion;
		requiredBundles = new DocearBundleInfo[]{};
		bundle = null;
	}
	
	

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	private DocearBundleInfo[] extractRequiredBundles(Dictionary<String, String> manifest) {
		Vector<DocearBundleInfo> requires = new Vector<DocearBundleInfo>();
		for(String req : manifest.get("Require-Bundle").split(",")) {
			String[] token = req.split(";");
			String name = token[0].substring(token[0].indexOf("=")+1);
			String version = token[1].substring(token[1].indexOf("=")+2);
			version = version.substring(0, version.length()-1);
			requires.add(new DocearBundleInfo(name, new Version(version)));
		}
 		return requires.toArray(new DocearBundleInfo[]{});
	}



	public String getBundleName() {
		return bundleName;
	}
	

	public DocearBundleInfo[] getRequiredBundles() {
		return requiredBundles;
	}



	public Version getBundleVersion() {
		return bundleVersion;
	}
	
	public Bundle getBundle() {
		return bundle;
	}
	
	public boolean isCompatible(Object o) {
		if(o instanceof DocearBundleInfo) {
			if(((DocearBundleInfo) o).getBundleName().equals(this.getBundleName()) &&
			((DocearBundleInfo) o).getBundleVersion().compareTo(this.getBundleVersion()) >= 0) {
				return true;
			}			
		}
		return false;
	}
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
