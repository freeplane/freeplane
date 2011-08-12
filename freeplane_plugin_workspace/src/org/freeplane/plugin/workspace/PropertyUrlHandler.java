package org.freeplane.plugin.workspace;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.freeplane.core.resources.ResourceController;
import org.osgi.service.url.AbstractURLStreamHandlerService;

public class PropertyUrlHandler extends AbstractURLStreamHandlerService {

	public URLConnection openConnection(URL url) throws IOException {		
		String propertyName = url.getPath();
		if (propertyName.startsWith("/")) {
			propertyName = propertyName.substring(1);
		}
		
		String property = ResourceController.getResourceController().getProperty(propertyName);
		
		URL ret = new URL("file", null, property);		
        System.out.println("PropertyUrlHandler returns: "+ret);
		return ret.openConnection();
	}
}
