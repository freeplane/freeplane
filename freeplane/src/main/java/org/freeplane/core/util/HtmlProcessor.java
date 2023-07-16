package org.freeplane.core.util;

import java.io.StringReader;
import java.io.StringWriter;

import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLWriter;

import org.freeplane.core.resources.ResourceController;

import com.lightdev.app.shtm.SHTMLDocument;
import com.lightdev.app.shtm.SHTMLEditorKit;
import com.lightdev.app.shtm.SHTMLWriter;

public class HtmlProcessor {
    private static final String SAVE_COMPACT_HTML_PROPERTY = "saveCompactHypertext";
    public static void configureUnknownTags(HTMLDocument doc) {
        doc.setPreservesUnknownTags(! ResourceController.getResourceController().getBooleanProperty(SAVE_COMPACT_HTML_PROPERTY));
    }

    private static HTMLDocument createDocument(String input) {
        final HTMLEditorKit kit = new SHTMLEditorKit();
        try {
            HTMLDocument doc = new SHTMLDocument();
            configureUnknownTags(doc);
            kit.read(new StringReader(input), doc, 0);
            return doc;
        } catch (Exception e) {
            LogUtils.severe(e);
            return null;
        }
    }

     private final HTMLDocument doc;

	public HtmlProcessor(final String input){
	    doc = createDocument(input);
	}

	public String htmlSubstring(int pos, int length){
	    if(doc == null)
            return "";
        final StringWriter writer = new StringWriter();
        try {
            if(pos < doc.getLength() && length > 0){
                final HTMLWriter hw = new SHTMLWriter(writer, doc, pos,
                        Math.min(length, doc.getLength() - pos));
                hw.write();
            }
        } catch (Exception e) {
            LogUtils.severe(e);
        }
        return writer.toString();
	}

	public String cleanHtml() {
	    return htmlSubstring(0, doc.getLength());
	}

	public String cleanXhtml() {
        if(doc == null)
            return "";
	    StringWriter writer = new StringWriter();
        final XHTMLWriter xhw = new XHTMLWriter(writer, doc);
	    try {
            xhw.write();
        } catch (Exception e) {
            LogUtils.severe(e);
        }
	    return writer.toString();
	}

	public int getDocumentLength() {
		return doc.getLength();
	}

    public boolean isOk() {
        return doc != null;
    }

    public String getText() {
        try {
            return doc == null ? "" : doc.getText(0, getDocumentLength());
        } catch (BadLocationException e) {
            LogUtils.severe(e);
            return "";
        }
    }
}
