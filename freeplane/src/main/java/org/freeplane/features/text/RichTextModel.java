package org.freeplane.features.text;

import java.io.StringReader;

import javax.xml.parsers.SAXParserFactory;

import org.freeplane.core.util.HtmlProcessor;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.XmlUtils;
import org.xml.sax.InputSource;


public class RichTextModel {
    private String contentType = null;
	private String text = null;
	private String xml = null;

	private static void validateXml(String xml) throws Exception{
            SAXParserFactory.newInstance().newSAXParser().getXMLReader()
                .parse(new InputSource(new StringReader(xml)));
	}


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
        if(HtmlUtils.isHtml(newContent)) {
            String html = XmlUtils.replaceAscii0BySpace(newContent);
            HtmlProcessor htmlProcessor = new HtmlProcessor(html);
            try {
                if(htmlProcessor.isOk()) {
                    String xhtml = htmlProcessor.cleanXhtml().trim();
                    validateXml(xhtml);
                    xml = xhtml;
                    text = htmlProcessor.cleanHtml();
                    return;
                }
            } catch (Exception e) {
                LogUtils.severe("Can not create xhtml", e);
            }
            setText(" " + newContent);
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
