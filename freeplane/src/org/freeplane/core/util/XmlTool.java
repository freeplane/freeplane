package org.freeplane.core.util;

public class XmlTool {
	/** \0 is not allowed: */
	public static String makeValidXml(final String pXmlNoteText) {
		return pXmlNoteText.replace('\0', ' ');
	}
}
