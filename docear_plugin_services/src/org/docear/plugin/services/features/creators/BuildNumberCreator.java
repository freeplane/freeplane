package org.docear.plugin.services.features.creators;


import org.docear.plugin.services.features.elements.Version;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLElement;

public class BuildNumberCreator implements IElementDOMHandler {	
	public Object createElement(Object parent, String tag, XMLElement attributes) {
		return -1;
	}

	public void endElement(Object parent, String tag, Object element, XMLElement dom) {
		try {
			((Version) parent).setBuildNumber(Integer.parseInt(dom.getContent()));
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
	}
	
};