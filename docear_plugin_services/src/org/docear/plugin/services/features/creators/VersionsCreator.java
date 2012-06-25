package org.docear.plugin.services.features.creators;

import java.sql.Date;
import java.util.HashMap;

import org.docear.plugin.core.Version;
import org.docear.plugin.services.features.elements.Application;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.n3.nanoxml.XMLElement;

public class VersionsCreator implements IElementDOMHandler {

	public Object createElement(Object parent, String tag, XMLElement attributes) {
		if (parent == null) {
			return null;
		}
		
		HashMap<Date, Version> versions = new HashMap<Date, Version>();
		
		if (parent instanceof Application) {
			((Application) parent).setVersions(versions);
		}
	
		return versions;
	}

	public void endElement(Object parent, String tag, Object element, XMLElement dom) {	
	}

}
