package org.freeplane.plugin.workspace;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.freeplane.core.util.FileUtils;
import org.freeplane.features.url.UrlManager;
import org.osgi.service.url.AbstractURLStreamHandlerService;

public class WorkspaceUrlHandler extends AbstractURLStreamHandlerService {

	public URLConnection openConnection(URL url) throws IOException {
		//DOCEAR: special handling for linked mindmap files
		String ext = FileUtils.getExtension(url.getPath());
		if((ext != null) && ext.equals(UrlManager.FREEPLANE_FILE_EXTENSION_WITHOUT_DOT)) {
			URI path;
			try {
				path = new URI(WorkspaceUtils.getWorkspaceBaseURI().toString()+url.getPath()).normalize();
				File file = new File(path);
				return file.toURL().openConnection();
			}
			catch (URISyntaxException e) {
				throw new IOException(e.getMessage());
			}
		} 
		//DOCEAR: usual handling
		URL ret = new URL("file", null, WorkspaceUtils.getWorkspaceBaseURI().toURL().getPath() + url.getPath());
		try {
			ret = ret.toURI().normalize().toURL();
		}
		catch (URISyntaxException e) {
			throw new IOException(e.getMessage());
		}
		return ret.openConnection();
	}
}
