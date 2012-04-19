package org.docear.plugin.services.features.elements;

import java.net.URL;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

public class Application {
	private final String id;
	private final String name;
	private final URL href;
	
	private Map<Date, Version> versions = new HashMap<Date, Version>();
	
	public Application(String id, String name, URL href) {
		this.id = id;
		this.name = name;
		this.href = href;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public URL getHref() {
		return href;
	}

	public Map<Date, Version> getVersions() {
		return versions;
	}

	public void setVersions(HashMap<Date, Version> versions) {
		this.versions = versions;
	}
	
	public void addVersion(Version version) {
		this.versions.put(version.getReleaseDate(), version);
	}

	

}
