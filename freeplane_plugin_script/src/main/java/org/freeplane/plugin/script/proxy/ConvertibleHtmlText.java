package org.freeplane.plugin.script.proxy;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.FormulaUtils;
import org.freeplane.plugin.script.ScriptExecution;

public class ConvertibleHtmlText extends Convertible {
    private String htmlText;

    public ConvertibleHtmlText(final NodeModel nodeModel, final ScriptExecution scriptExecution, final String htmlText) {
        super(FormulaUtils.safeEvalIfScript(nodeModel, htmlToPlain(htmlText)));
        this.htmlText = htmlText;
    }

    private static String htmlToPlain(final String htmlText) {
        return htmlText == null ? null : HtmlUtils.htmlToPlain(htmlText);
    }

    /** returns the original HTML text. */
    @Override
    public String getString() {
    	return htmlText;
    }

    /** returns the original HTML text. */
    public String getHtml() {
        return htmlText;
    }

    /** returns the possibly transformed plain text. */
    @Override
    public String getPlain() {
    	return super.getText();
    }

    /** since equals handles Strings special we have to stick to that here too since
     * equal objects have to have the same hasCode. */
    @Override
    public int hashCode() {
    	return htmlText.hashCode();
    }

    /** note: if obj is a String the result is true if String.equals(text). */
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ConvertibleHtmlText && htmlText.equals(obj);
    }

    @Override
    public String toString() {
    	return getText();
    }
}
