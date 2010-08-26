package org.freeplane.plugin.script.proxy;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.note.NoteModel;

/**
 * Notes are special since they have HTML text. For conversions to other type than String we use the plain note text.
 * for {@link #getString()}, {@link #getText()} and {@link #toString()} we use the HTML text.
 */
public class ConvertibleNoteText extends Convertible {
	private final NodeModel nodeModel;

	public ConvertibleNoteText(NodeModel nodeModel) {
		super(htmlToPlain(nodeModel));
		this.nodeModel = nodeModel;
	}

	private static String htmlToPlain(NodeModel nodeModel) {
		final String htmlNote = getHtmlNote(nodeModel);
		return htmlNote == null ? null : HtmlUtils.htmlToPlain(htmlNote);
	}

	private static String getHtmlNote(NodeModel nodeModel) {
		return NoteModel.getNoteText(nodeModel);
	}

	public Convertible getValue() {
		return new Convertible(FormulaUtils.evalNoteText(nodeModel, getText()));
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
	public boolean equals(Object obj) {
		return getHtmlNote(nodeModel).equals(obj);
	}

	@Override
	public String toString() {
		return getText();
	}
}
