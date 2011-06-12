package org.freeplane.plugin.script.proxy;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.note.NoteModel;
import org.freeplane.plugin.script.FormulaUtils;
import org.freeplane.plugin.script.ScriptContext;

/** Uses plain note text as a basis for conversions. */
public class ConvertibleNoteText extends Convertible {
	private final NodeModel nodeModel;

	public ConvertibleNoteText(final NodeModel nodeModel, final ScriptContext scriptContext) {
		super(FormulaUtils.evalIfScript(nodeModel, scriptContext, htmlToPlain(nodeModel)));
		this.nodeModel = nodeModel;
	}

	private static String htmlToPlain(final NodeModel nodeModel) {
		final String htmlNote = getHtmlNote(nodeModel);
		return htmlNote == null ? null : HtmlUtils.htmlToPlain(htmlNote);
	}

	private static String getHtmlNote(final NodeModel nodeModel) {
		return NoteModel.getNoteText(nodeModel);
	}

	/** returns the original HTML text. */
	public String getString() {
		return getHtmlNote(nodeModel);
	}

	/** returns the original HTML text. */
	public String getText() {
		return getHtmlNote(nodeModel);
	}

	public String getPlain() {
		// for conversions we use the plain text
		return super.getText();
	}

	/** since equals handles Strings special we have to stick to that here too since
	 * equal objects have to have the same hasCode. */
	@Override
	public int hashCode() {
		return getHtmlNote(nodeModel).hashCode();
	}

	/** note: if obj is a String the result is true if String.equals(text). */
	@Override
	public boolean equals(final Object obj) {
		return getHtmlNote(nodeModel).equals(obj);
	}

	@Override
	public String toString() {
		return getText();
	}
}
