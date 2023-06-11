package org.freeplane.core.util;

import java.io.StringReader;
import java.io.StringWriter;

import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLWriter;

public class HtmlProcessor {
	private final HTMLDocument doc;
	private final boolean ok;
	public HtmlProcessor(final String input){
		final HTMLEditorKit kit = new HTMLEditorKit();
		doc = (HTMLDocument) kit.createDefaultDocument();
		doc.setPreservesUnknownTags(false);
		boolean statusOk = false;
		try {
			final int defaultDocumentLength = doc.getLength();
			kit.read(new StringReader(input), doc, defaultDocumentLength);
			statusOk = true;
		} catch (Exception e) {
		    LogUtils.severe(e);
		}
		ok = statusOk;
	}

	public String htmlSubstring(int pos, int length){
		final StringWriter writer = new StringWriter();
		try {
			if(pos < doc.getLength() && length > 0){
				final HTMLWriter hw = new HTMLWriter(writer, (HTMLDocument) doc, pos,
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
	    StringWriter writer = new StringWriter();
        final XHTMLWriter xhw = new XHTMLWriter(writer, (HTMLDocument) doc);
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
        return ok;
    }
}
