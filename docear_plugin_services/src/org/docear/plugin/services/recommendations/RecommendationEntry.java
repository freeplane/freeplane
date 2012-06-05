package org.docear.plugin.services.recommendations;

import java.net.URI;

public class RecommendationEntry {

	private final String title;
	private final URI link;
	
	public RecommendationEntry(String title, String url) {
		this.title = title;
		this.link = URI.create(url);
	}

	public String getTitle() {
		return title;
	}

	public URI getLink() {
		return link;
	}

	public URI getLinkUrl() {
		return null;
	}

}
