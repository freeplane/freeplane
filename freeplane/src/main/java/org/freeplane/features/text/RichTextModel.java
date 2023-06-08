package org.freeplane.features.text;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.XmlUtils;


public class RichTextModel {
    private String contentType = null;
	private String text = null;
	private String xml = null;



	public RichTextModel() {
        super();
    }



    public RichTextModel(String contentType, String text, String xml) {
        super();
        this.contentType = contentType;
        this.text = text;
        this.xml = xml != null ? xml.trim() : null;
    }

    public String getText() {
		return text;
	}

	public String getXml() {
		return xml;
	}

	public final void setText(final String newContent) {
		if (newContent == null) {
		    xml = null;
		    text = null;
		    return;
		}
		if(newContent.startsWith("<html>")) {
		    try {
		        text = XmlUtils.replaceAscii0BySpace(newContent);
		        String xhtml = HtmlUtils.toXhtml(text);
		        xml = xhtml != null ? xhtml.trim() : null;
		        if (xml != null && !xml.startsWith("<")) {
		            text = xml;
		        }
		    } catch (Exception e) {
		        text = xml = HtmlUtils.unescapeHTMLUnicodeEntity(newContent);
		    }
		}
		else {
		    text = newContent;
		    xml = "<text>" + HtmlUtils.toXMLEscapedText(newContent) + "</text>";
		}
	}

	public final void setXml(final String newContent) {
		if (newContent == null) {
			xml = null;
			text = null;
			return;
		}
		String trimmed = newContent.trim();
        if(HtmlUtils.isHtml(newContent)) {
		    xml = trimmed;
		    text = HtmlUtils.toHtml(trimmed);
        }
        else if (trimmed.startsWith("<text>") && trimmed.endsWith("</text>")){
            xml = trimmed;
            text = HtmlUtils.toXMLUnescapedText(trimmed.substring("<text>".length(), trimmed.length() - "</text>".length()));
        }  else if (trimmed.equals("<text/>")){
            xml = trimmed;
            text = "";
        }
        else
            setText(newContent);

	}

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String format) {
        this.contentType = format;
    }



    public boolean isEmpty() {
        return contentType == null && text == null && xml == null;
    }

	public String getTextOr(String fallback) {
		return text == null ? fallback : text;
	}
}
