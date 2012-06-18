package org.docear.plugin.services.recommendations;

import java.net.URI;

public class RecommendationEntry {

	private final String title;
	private final URI link;
	private final URI clickUri;
	
	public RecommendationEntry(String title, String url, String clickUrl) {
		this.title = title;
		this.link = (url==null ? null:URI.create(url));
		this.clickUri = (clickUrl==null ? null:URI.create(clickUrl));
	}

	public String getTitle() {
		return title;
	}

	public URI getLink() {
		return link;
	}
	
	public URI getClickUri() {
		return clickUri;
	}

}
