package org.freeplane.core.ui;

public class KeyAlreadyUsedException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public KeyAlreadyUsedException(final String message) {
		super(message);
	}
}
