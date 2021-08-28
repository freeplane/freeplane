package org.freeplane.core.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

public class Hyperlink {
	private URI uri;
	private String uriString;

    public Hyperlink(URI uri) {
        super();
        this.uri = uri;
    }

    public Hyperlink(String uriString, URI uri) {
        super();
        this.uriString = uriString;
        this.uri = uri;
    }

	public URI getUri() {
		return uri;
	}

	@Override
	public int hashCode() {
		return Objects.hash(toString());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Hyperlink other = (Hyperlink) obj;
		return Objects.equals(toString(), other.toString());
	}

	@Override
	public String toString() {
		if(uriString == null)
			uriString = uri.toString();
		return uriString;
	}

	public String getScheme() {
		return uri.getScheme();
	}

	public URL toUrl() throws MalformedURLException {
		final String scheme = uri.getScheme();
		final String host = uri.getHost() == null ? "" : uri.getHost();
		final String path = uri.isOpaque() ? uri.getSchemeSpecificPart() : uri.getPath();
		final int port = uri.getPort();
		final String query = uri.getQuery();
		final String fragment = uri.getFragment();
		final StringBuilder file = new StringBuilder(path);
		if(query != null){
			file.append('?');
			file.append(query);
		}
		if(fragment != null){
			file.append('#');
			file.append(fragment);
		}
		final URL url = new URL(scheme, host, port, file.toString());
		return url;
	}
	
	

}
