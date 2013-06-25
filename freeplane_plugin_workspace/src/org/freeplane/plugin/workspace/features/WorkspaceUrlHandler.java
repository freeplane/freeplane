package org.freeplane.plugin.workspace.features;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.osgi.service.url.AbstractURLStreamHandlerService;

/**
 * @deprecated - use {@link ProjectURLHandler} instead
 */
@Deprecated
public class WorkspaceUrlHandler extends AbstractURLStreamHandlerService {

	public URLConnection openConnection(URL url) throws IOException {
		String path = "";
//		List<AWorkspaceProject> projects = WorkspaceController.getCurrentModel().getProjects();
//		synchronized (projects) {
//			for (AWorkspaceProject project : projects) {
//				File file = new File(project.getProjectHome().getPath() + url.getPath());
//				if(file.exists()) {
//					path = project.getProjectHome().getPath();
//				}
//			}
//		}
//		if(path == null) {
//			return null;
//		}
		path = WorkspaceController.getCurrentProject().getProjectHome().getPath();
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
