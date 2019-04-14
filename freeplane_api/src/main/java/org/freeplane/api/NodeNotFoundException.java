package org.freeplane.api;

@SuppressWarnings("serial")
public class NodeNotFoundException extends IllegalArgumentException {

	public NodeNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NodeNotFoundException(String s) {
		super(s);
	}

	public NodeNotFoundException(Throwable cause) {
		super(cause);
	}
}
