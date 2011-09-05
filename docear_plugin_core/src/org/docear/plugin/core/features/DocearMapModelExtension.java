package org.docear.plugin.core.features;

import org.freeplane.core.extension.IExtension;

public class DocearMapModelExtension implements IExtension{
	
	String version;

	public String getVersion() {
		return version;
	}

	public void setVersion(String mapExtensionVersion) {
		this.version = mapExtensionVersion;
	}
	
	

}
