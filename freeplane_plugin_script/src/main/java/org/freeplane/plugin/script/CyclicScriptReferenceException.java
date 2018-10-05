package org.freeplane.plugin.script;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;

public class CyclicScriptReferenceException extends StackOverflowError {
	public final NodeScript nodeScript;

	public CyclicScriptReferenceException(NodeScript nodeScript) {
		super(TextUtils.format("formula.error.circularReference",
				HtmlUtils.htmlToPlain(nodeScript.script)));
		this.nodeScript = nodeScript;
	}
}
