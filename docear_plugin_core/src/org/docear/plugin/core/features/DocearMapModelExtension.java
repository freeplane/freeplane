package org.docear.plugin.core.features;

import java.net.URI;

import org.freeplane.core.extension.IExtension;

public class DocearMapModelExtension implements IExtension {
	
	public final static String MAP_ID_ATTRIBUTE = "dcr_id";
	public final static String MAP_URI_ATTRIBUTE = "dcr_uri";
	
	private MapModificationSession session;
	
	private String version = "1.0";
	private DocearMapType type;
	private String mapId;
	private URI uri;
	private boolean incompatible;	

	public DocearMapModelExtension() {
		setVersion("1.0");
	}
	
	public DocearMapModelExtension(String version) {
		setVersion(version);
	}
	
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
		else if(type.equalsIgnoreCase("trash")){
			this.type = DocearMapType.trash;
		}
		else if(type.equalsIgnoreCase("temp")){
			this.type = DocearMapType.temp;
		}
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String mapExtensionVersion) {
		if(mapExtensionVersion == null) {
			throw new IllegalArgumentException("mapExtensionVersion cannot be NULL");
		}
		this.version = mapExtensionVersion;
	}
	
	public enum DocearMapType{
		incoming, my_publications, literature_annotations, trash, temp
	}

	public String getMapId() {
		return mapId;
	}

	public void setMapId(String mapId) {
		this.mapId = mapId;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public void resetModificationSession() {
		this.session = null;		
	}

	public void setMapModificationSession(MapModificationSession session) {
		this.session = session;
	}
	
	public MapModificationSession getMapModificationSession() {
		return session;
	}
	
	public boolean isIncompatible() {
		return incompatible;
	}

	public void setIncompatible(boolean incompatible) {
		this.incompatible = incompatible;
	}
	
	
}
