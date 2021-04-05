package org.freeplane.core.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

public class Hyperlink {
	private final URI uri;

	public Hyperlink(URI uri) {
		super();
		this.uri = uri;
	}

	public URI getUri() {
		return uri;
	}

	@Override
	public int hashCode() {
		return Objects.hash(uri);
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
		return Objects.equals(uri, other.uri);
	}

	@Override
	public String toString() {
		return uri.toString();
	}

	public String getScheme() {
		return uri.getScheme();
	}

	public String decodedString() {
		StringBuffer sb = new StringBuffer();
		String scheme = uri.getScheme();
		if (scheme != null) {
			sb.append(scheme);
			sb.append(':');
		}
		if (uri.isOpaque()) {
			sb.append(uri.getSchemeSpecificPart());
		} else {
			String host = uri.getHost();
			if (host != null) {
				sb.append("//");
				if (uri.getUserInfo() != null) {
					sb.append(uri.getUserInfo());
					sb.append('@');
				}
				boolean needBrackets = ((host.indexOf(':') >= 0)
						&& !host.startsWith("[")
						&& !host.endsWith("]"));
				if (needBrackets) sb.append('[');
				sb.append(host);
				if (needBrackets) sb.append(']');
				if (uri.getPort() != -1) {
					sb.append(':');
					sb.append(uri.getPort());
				}
			} else if (uri.getAuthority() != null) {
				sb.append("//");
				sb.append(uri.getAuthority());
			}
			if (uri.getPath() != null)
				sb.append(uri.getPath());
			if (uri.getQuery() != null) {
				sb.append('?');
				sb.append(uri.getQuery());
			}
		}
		if (uri.getFragment() != null) {
			sb.append('#');
			sb.append(uri.getFragment());
		}
		return sb.toString();
	}

	public URL toUrl() throws MalformedURLException {
		final String scheme = uri.getScheme();
		final String host = uri.getHost();
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
