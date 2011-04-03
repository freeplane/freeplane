package org.freeplane.plugin.script;

public class ExecuteScriptException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ExecuteScriptException(String message, Throwable e) {
		super(message, e);
	}

	public ExecuteScriptException(String message) {
		super(message);
	}

	public ExecuteScriptException(Throwable cause) {
	    super(cause);
    }
}
