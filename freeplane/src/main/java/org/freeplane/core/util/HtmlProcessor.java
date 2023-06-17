package org.freeplane.core.util;

import java.io.StringReader;
import java.io.StringWriter;

import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLWriter;

import com.lightdev.app.shtm.SHTMLWriter;

public class HtmlProcessor {
    public static void configureUnknownTags(HTMLDocument doc) {
        doc.setPreservesUnknownTags(false);
    }

    private static HTMLDocument createDocument(String input) {
        final HTMLEditorKit kit = new HTMLEditorKit();
        try {
            HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
            doc.setPreservesUnknownTags(false);
            kit.read(new StringReader(input), doc, 0);
            return doc;
        } catch (Exception e) {
            LogUtils.severe(e);
            return null;
        }
    }

    private static String cleanHtml(HTMLDocument document) {
        return htmlSubstring(document, 0, document.getLength());
    }

    private static String htmlSubstring(HTMLDocument document, int pos, int length) {
        if(document == null)
            return "";
        final StringWriter writer = new StringWriter();
        try {
            if(pos < document.getLength() && length > 0){
                final HTMLWriter hw = new SHTMLWriter(writer, document, pos,
                        Math.min(length, document.getLength() - pos));
                hw.write();
            }
        } catch (Exception e) {
            LogUtils.severe(e);
        }
        return writer.toString();
    }


    private final HTMLDocument doc;
    private String cleanHtml;

	public HtmlProcessor(final String input){
	    doc = createDocument(input);
	}

	public String htmlSubstring(int pos, int length){
	    return htmlSubstring(doc, pos, length);
	}

	public String cleanHtml() {
	    if(cleanHtml != null)
	        return cleanHtml;
	    String html = cleanHtml(doc);
	    this.cleanHtml = html;
	    return html;
	}

	public String cleanXhtml() {
	    String cleanHtml = cleanHtml();
	    if(cleanHtml.isEmpty())
	        return cleanHtml;
	    else
	        return HtmlUtils.toXhtml(cleanHtml);
	}

	public int getDocumentLength() {
		return doc.getLength();
	}

    public boolean isOk() {
        return doc != null;
    }
}
