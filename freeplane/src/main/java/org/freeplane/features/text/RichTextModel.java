package org.freeplane.features.text;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.XmlUtils;


public class RichTextModel {
	private String html = null;
	private String xml = null;

	public String getHtml() {
		return html;
	}

	public String getXml() {
		return xml;
	}

	public final void setHtml(final String pNoteText) {
		if (pNoteText == null) {
			xml = null;
			html = null;
			return;
		}
		try {
			html = XmlUtils.makeValidXml(pNoteText);
			xml = HtmlUtils.toXhtml(html);
			if (xml != null && !xml.startsWith("<")) {
				html = xml;
			}
		} catch (Exception e) {
			html = xml = HtmlUtils.unescapeHTMLUnicodeEntity(pNoteText);
		}
	}

	public final void setXml(final String pXmlNoteText) {
		if (pXmlNoteText == null) {
			xml = null;
			html = null;
			return;
		}
		xml = XmlUtils.makeValidXml(pXmlNoteText);
		html = HtmlUtils.toHtml(xml);
	}
}
