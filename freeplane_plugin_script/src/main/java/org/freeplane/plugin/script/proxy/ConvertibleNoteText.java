package org.freeplane.plugin.script.proxy;

import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptExecution;

/** Uses plain note text as a basis for conversions.
 * This class is kept for compatibility to Freeplane 1.2. */
public class ConvertibleNoteText extends ConvertibleHtmlText {
	public ConvertibleNoteText(final NodeModel nodeModel, final ScriptExecution scriptExecution, final String htmlText) {
		super(nodeModel, scriptExecution, htmlText);
	}
}
