package org.docear.plugin.services.features.elements;

import java.net.URL;
import java.util.ArrayList;

public class Application {
	private final String id;
	private final String name;
	private final URL href;
	
	private ArrayList<Version> versions = new ArrayList<Version>();
	
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

	public ArrayList<Version> getVersions() {
		return versions;
	}

	public void setVersions(ArrayList<Version> versions) {
		this.versions = versions;
	}
	
	public void addVersion(Version version) {
		this.versions.add(version);
	}

	

}
