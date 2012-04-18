package org.docear.plugin.services.features.creators;

import java.net.MalformedURLException;
import java.net.URL;

import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.elements.Application;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLElement;

public class ApplicationCreator implements IElementDOMHandler {
	public Object createElement(Object parent, String tag, XMLElement attributes) {
		if (attributes == null) {
			return null;
		}	
		
		String id = attributes.getAttribute("id", "");
		String name = attributes.getAttribute("name", "");
		String hrefString = attributes.getAttribute("href", "");
		URL href = null;
		
		if (hrefString.length()>0) {
			try {
				href = new URL(hrefString);
			} catch (MalformedURLException e) {				
				LogUtils.warn(e);
			}
		}
		
		Application application = new Application(id, name, href);
		ServiceController.getController().setApplication(application);
		
		return application;
	}

	public void endElement(Object parent, String tag, Object element, XMLElement dom) {
	}
	
};