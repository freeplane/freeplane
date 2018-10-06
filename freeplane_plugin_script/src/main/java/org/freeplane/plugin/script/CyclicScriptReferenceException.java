package org.freeplane.plugin.script;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;

public class CyclicScriptReferenceException extends StackOverflowError {
	public CyclicScriptReferenceException(String message) {
		super(message);
	}
}
