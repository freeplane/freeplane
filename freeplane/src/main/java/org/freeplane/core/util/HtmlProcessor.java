package org.freeplane.core.util;

import java.io.StringReader;
import java.io.StringWriter;

import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLWriter;

public class HtmlProcessor {
	private final Document doc;
	public HtmlProcessor(final String input){
		final HTMLEditorKit kit = new HTMLEditorKit();
		doc = kit.createDefaultDocument();
		try {
			final int defaultDocumentLength = doc.getLength();
			kit.read(new StringReader(input), doc, defaultDocumentLength);
		} catch (Exception e) {
		}
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
		}
		return writer.toString();
	}
	public int getDocumentLength() {
		return doc.getLength();
	}



}
