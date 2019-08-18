package org.freeplane.plugin.script;

public class CyclicScriptReferenceException extends RuntimeException {
	public CyclicScriptReferenceException(String message) {
		super(message);
	}
}
