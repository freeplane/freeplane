package org.docear.plugin.services.features.creators;

import java.sql.Date;

import org.docear.plugin.core.Version;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.n3.nanoxml.XMLElement;

public class ReleaseDateCreator implements IElementDOMHandler {
	
	public Object createElement(Object parent, String tag, XMLElement attributes) {
		return new Date(System.currentTimeMillis());
	}

	public void endElement(Object parent, String tag, Object element, XMLElement dom) {
		if (parent instanceof Version) {
			String releaseDate = dom.getContent();
			if (releaseDate != null && releaseDate.trim().length()>0) {
				((Version) parent).setReleaseDate(Date.valueOf(releaseDate));
			}
		}
	}
	
};