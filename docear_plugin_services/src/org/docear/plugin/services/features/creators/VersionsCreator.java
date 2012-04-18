package org.docear.plugin.services.features.creators;

import java.util.ArrayList;

import org.docear.plugin.services.features.elements.Application;
import org.docear.plugin.services.features.elements.Version;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.n3.nanoxml.XMLElement;

public class VersionsCreator implements IElementDOMHandler {

	public Object createElement(Object parent, String tag, XMLElement attributes) {
		if (parent == null) {
			return null;
		}
		
		ArrayList<Version> versions = new ArrayList<Version>();
		
		if (parent instanceof Application) {
			((Application) parent).setVersions(versions);
		}
	
		return versions;
	}

	public void endElement(Object parent, String tag, Object element, XMLElement dom) {	
	}

}
