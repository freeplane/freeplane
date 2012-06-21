package org.docear.plugin.services.recommendations;

import java.net.MalformedURLException;
import java.net.URL;

public class RecommendationEntry {

	private final String title;
	private final URL link;
	private final URL clickUrl;
	
	public RecommendationEntry(String title, String url, String clickUrl) throws MalformedURLException {
		this.title = title;
		this.link = (url==null ? null:new URL(url));
		this.clickUrl = (clickUrl==null ? null:new URL(clickUrl));
	}

	public String getTitle() {
		return title;
	}

	public URL getLink() {
		return link;
	}
	
	public URL getClickUrl() {
		return clickUrl;
	}

}
