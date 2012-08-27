package org.freeplane.plugin.workspace;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.osgi.service.url.AbstractURLStreamHandlerService;

public class WorkspaceUrlHandler extends AbstractURLStreamHandlerService {

	public URLConnection openConnection(URL url) throws IOException {
		String path = WorkspaceUtils.getWorkspaceBaseURI().toURL().getPath();
		URL ret = new URL("file", null,  path + url.getPath());
		try {
			URI uri = ret.toURI();
			if(uri.getPath().startsWith("//")) {
				uri = uri.normalize();
				uri = new URI(uri.getScheme(), null, "///"+uri.getPath(), null);
			}
			else {
				uri = uri.normalize();
			}
			ret = uri.toURL();
		}
		catch (URISyntaxException e) {
			throw new IOException(e.getMessage());
		}
		return ret.openConnection();
	}
}
