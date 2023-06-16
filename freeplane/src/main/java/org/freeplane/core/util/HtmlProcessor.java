package org.freeplane.core.util;

import java.io.StringReader;
import java.io.StringWriter;

import javax.swing.JOptionPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLWriter;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog.MessageType;

import com.lightdev.app.shtm.SHTMLWriter;

public class HtmlProcessor {

    private static final String SAVE_COMPACT_HTML_PROPERTY = "saveCompactHypertext";

    public static boolean preservesUnknownTags() {
        ResourceController resourceController = ResourceController.getResourceController();
        String saveCompactHypertextProperty = resourceController.getProperty(SAVE_COMPACT_HTML_PROPERTY, "false");
        return ! Boolean.valueOf(saveCompactHypertextProperty);
    }

    public static void configureUnknownTags(HTMLDocument doc) {
        doc.setPreservesUnknownTags(preservesUnknownTags());
    }

    private static HTMLDocument createDocument(String input, boolean saveCompactHypertext) {
        final HTMLEditorKit kit = new HTMLEditorKit();
        try {
            HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
            doc.setPreservesUnknownTags(! saveCompactHypertext);
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
	public HtmlProcessor(final String input){
	    doc = createDocument(input);
	}

    private static HTMLDocument createDocument(final String input) {
        ResourceController resourceController = ResourceController.getResourceController();
        String saveCompactHypertextProperty = resourceController.getProperty(SAVE_COMPACT_HTML_PROPERTY, null);
        if(saveCompactHypertextProperty != null)
            return createDocument(input, Boolean.valueOf(saveCompactHypertextProperty));
        HTMLDocument compactDocument = createDocument(input, true);
        if(compactDocument == null)
            return compactDocument;
        HTMLDocument fullDocument = createDocument(input, false);
        if(fullDocument == null)
            return compactDocument;
        String compactText = cleanHtml(compactDocument);
        String fullText = cleanHtml(fullDocument);
        if(compactText.equals(fullText))
            return compactDocument;
        if (JOptionPane.OK_OPTION == OptionalDontShowMeAgainDialog.showWithExplanation("OptionPanel." + SAVE_COMPACT_HTML_PROPERTY + ".question",
                "OptionPanel." + SAVE_COMPACT_HTML_PROPERTY + ".explanation",
                SAVE_COMPACT_HTML_PROPERTY,
                MessageType.BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED))
                return compactDocument;
        else
            return fullDocument;

    }

    public String htmlSubstring(int pos, int length){
		return htmlSubstring(doc, pos, length);
	}

	public String cleanHtml() {
	    return cleanHtml(doc);
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
}
