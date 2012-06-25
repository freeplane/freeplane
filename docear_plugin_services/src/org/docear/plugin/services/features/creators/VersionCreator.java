package org.docear.plugin.services.features.creators;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.util.HashMap;

import org.docear.plugin.core.Version;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLElement;

public class VersionCreator implements IElementDOMHandler {
	@SuppressWarnings("unchecked")
	public Object createElement(Object parent, String tag, XMLElement attributes) {
		if (attributes == null) {
			return null;
		}
		
		Integer id = attributes.getAttribute("id", -1);		
		String hrefString = attributes.getAttribute("href", "");
		URL href = null;
		
		if (hrefString.length()>0) {
			try {
				href = new URL(hrefString);
			} catch (MalformedURLException e) {				
				LogUtils.warn(e);
			}
		}
		
		Version version = new Version(id, href);
		
		if (parent instanceof HashMap) {
			((HashMap<Date, Version>) parent).put(version.getReleaseDate(), version);
		}
		return version;
	}

	public void endElement(Object parent, String tag, Object element, XMLElement dom) {
	}
	
};