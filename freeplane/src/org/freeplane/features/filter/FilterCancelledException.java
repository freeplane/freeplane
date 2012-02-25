package org.freeplane.features.filter;

public class FilterCancelledException extends RuntimeException {

	public FilterCancelledException() {
	}

	public FilterCancelledException(String message) {
		super(message);
	}

	public FilterCancelledException(Throwable cause) {
		super(cause);
	}

	public FilterCancelledException(String message, Throwable cause) {
		super(message, cause);
	}

}
