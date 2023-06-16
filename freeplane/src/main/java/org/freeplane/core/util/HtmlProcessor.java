package org.freeplane.core.util;

import java.io.StringReader;
import java.io.StringWriter;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLWriter;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog.MessageType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Entities.EscapeMode;

import com.lightdev.app.shtm.SHTMLWriter;

public class HtmlProcessor {

    private static final String SAVE_COMPACT_HTML_PROPERTY = "saveCompactHypertext";

    private static Integer lastDecision = null;

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
    private String cleanHtml;
    private Document jsoupDocument;

	public HtmlProcessor(final String input){
	    doc = createDocument(input);
	}

    private static HTMLDocument createDocument(final String input) {
        if(lastDecision != null) {
            return createDocument(input, lastDecision.intValue() == JOptionPane.OK_OPTION);
        }
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
        lastDecision = OptionalDontShowMeAgainDialog.showWithExplanation("OptionPanel." + SAVE_COMPACT_HTML_PROPERTY + ".question",
                "OptionPanel." + SAVE_COMPACT_HTML_PROPERTY + ".tooltip",
                SAVE_COMPACT_HTML_PROPERTY,
                MessageType.BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED);
        SwingUtilities.invokeLater(() -> {lastDecision = null;});
        if (JOptionPane.OK_OPTION == lastDecision)
                return compactDocument;
        else
            return fullDocument;

    }

    public String htmlSubstring(int pos, int length){
		return htmlSubstring(doc, pos, length);
	}

	public String cleanHtml() {
           if(cleanHtml != null)
                return cleanHtml;
            String html = cleanHtml(doc);
            if(! doc.getPreservesUnknownTags()) {
                this.cleanHtml = html;
                return html;
            }
            return this.cleanHtml = cleanWithJSoup(html, Syntax.html);

	}

	private String cleanWithJSoup(String html, Syntax syntax) {
	    if(jsoupDocument == null)
	        jsoupDocument = Jsoup.parse(html);
	    jsoupDocument.outputSettings()
	        .syntax(syntax)
	        .escapeMode(EscapeMode.xhtml);
	    return jsoupDocument.html();
	}

	public String cleanXhtml() {
	    String cleanHtml = cleanHtml();
	    if(cleanHtml.isEmpty())
	        return cleanHtml;
	    else
	        return cleanWithJSoup(cleanHtml, Syntax.xml);

	}
	public int getDocumentLength() {
		return doc.getLength();
	}

    public boolean isOk() {
        return doc != null;
    }
}
