package org.freeplane.plugin.script.proxy;

public class ConversionException extends org.freeplane.api.ConversionException {
	private static final long serialVersionUID = 1L;

	public ConversionException(String message, Throwable cause) {
	    super(message, cause);
    }

	public ConversionException(String message) {
	    super(message);
    }

	public ConversionException(Throwable cause) {
		super(cause);
	}
}
