package org.docear.plugin.core.features;

import org.freeplane.core.extension.IExtension;

public class DocearMapModelExtension implements IExtension{
	
	String version;
	DocearMapType type;

	public DocearMapType getType() {
		return type;
	}

	public void setType(DocearMapType type) {
		this.type = type;
	}
	
	public void setType(String type) {
		if(type.equalsIgnoreCase("incoming")){
			this.type = DocearMapType.incoming;
		}
		else if(type.equalsIgnoreCase("my_publications")){
			this.type = DocearMapType.my_publications;
		}
		else if(type.equalsIgnoreCase("literature_annotations")){
			this.type = DocearMapType.literature_annotations;
		}		
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String mapExtensionVersion) {
		this.version = mapExtensionVersion;
	}
	
	public enum DocearMapType{
		incoming, my_publications, literature_annotations
	}

}
