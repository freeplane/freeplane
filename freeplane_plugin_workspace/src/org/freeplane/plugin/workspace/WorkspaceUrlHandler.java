package org.freeplane.plugin.workspace;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.osgi.service.url.AbstractURLStreamHandlerService;

public class WorkspaceUrlHandler extends AbstractURLStreamHandlerService {

	public URLConnection openConnection(URL url) throws IOException {
		URL ret = new URL("file", null, WorkspaceController.getCurrentWorkspaceController().getWorkspaceLocation()
				+ url.getPath());
		return ret.openConnection();
	}
}
