package org.freeplane.features.text;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.XmlUtils;


public class RichTextModel {
	private String text = null;
	private String xml = null;

	public String getText() {
		return text;
	}

	public String getXml() {
		return xml;
	}

	public final void setText(final String newText) {
		if (newText == null) {
			xml = null;
			text = null;
			return;
		}
	    try {
	        text = XmlUtils.replaceAscii0BySpace(newText);
	        xml = HtmlUtils.toXhtml(text);
	        if (xml != null && !xml.startsWith("<")) {
	            text = xml;
	        }
	    } catch (Exception e) {
	        text = xml = HtmlUtils.unescapeHTMLUnicodeEntity(newText);
	    }
	}

	public final void setXml(final String pXmlNoteText) {
		if (pXmlNoteText == null) {
			xml = null;
			text = null;
			return;
		}
		xml = XmlUtils.replaceAscii0BySpace(pXmlNoteText);
		text = HtmlUtils.toHtml(xml);
	}
}
