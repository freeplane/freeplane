package org.freeplane.core.util;

public class XmlUtils {
	/** \0 is not allowed: */
	public static String replaceAscii0BySpace(final String pXmlNoteText) {
		return pXmlNoteText.replace('\0', ' ');
	}
}
