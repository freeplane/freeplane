package org.docear.plugin.services.features.creators;

import org.docear.plugin.core.Version;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLElement;

public class ReleaseNotesCreator implements IElementDOMHandler {	
	public Object createElement(Object parent, String tag, XMLElement attributes) {
		return "";
	}

	public void endElement(Object parent, String tag, Object element, XMLElement dom) {
		try {
			((Version) parent).setReleaseNotes(dom.getContent());
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
	}
	
};