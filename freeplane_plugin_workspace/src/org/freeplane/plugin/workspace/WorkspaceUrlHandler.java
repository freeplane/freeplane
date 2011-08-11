package org.freeplane.plugin.workspace;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.osgi.service.url.AbstractURLStreamHandlerService;

public class WorkspaceUrlHandler extends AbstractURLStreamHandlerService {

	public URLConnection openConnection(URL url) throws IOException {
//		try {
//			throw new Exception("DOCEAR URLHANDLER: "+WorkspaceController.getCurrentWorkspaceController().getWorkspaceLocation() + url.getPath());
//		}
//		catch (Exception e) {			
//			e.printStackTrace();
//		}
//		try {
//			System.out.println("WorkspaceUrlHandler: "+WorkspaceController.getCurrentWorkspaceController().getWorkspaceLocation() + url.getPath());
			URL ret = new URL("file", null, WorkspaceController.getCurrentWorkspaceController().getWorkspaceLocation() + url.getPath());
			System.out.println("WorkspaceUrlHandler2: "+ret.getPath());
//			File f = new File (ret.getPath());
//			System.out.println("WorkspaceUrlHandler3: "+f+" : "+f.exists());
			return ret.openConnection();
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;

	}
}
