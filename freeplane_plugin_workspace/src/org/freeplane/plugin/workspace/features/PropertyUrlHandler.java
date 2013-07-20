package org.freeplane.plugin.workspace.features;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.osgi.service.url.AbstractURLStreamHandlerService;

public class PropertyUrlHandler extends AbstractURLStreamHandlerService {

	public URLConnection openConnection(URL url) throws IOException {		
		String propertyName = url.getPath();
		if (propertyName.startsWith("/")) {
			propertyName = propertyName.substring(1);
		}
		
		String property = ResourceController.getResourceController().getProperty(propertyName);	
		
		if (property!=null && property.length()>0) {
			File file = new File(property);
			URL ret = file.toURI().toURL();	        
			return ret.openConnection();
		}
		LogUtils.warn("Property Path :"+url+" is unknown");
		return null;
	}
}
