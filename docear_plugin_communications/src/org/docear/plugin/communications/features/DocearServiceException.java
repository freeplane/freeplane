package org.docear.plugin.communications.features;

public class DocearServiceException extends Exception {

	private static final long serialVersionUID = 1L;

	public enum DocearServiceExceptionType {
		NO_CONNECTION,
		UNAUTHORIZED,
		SIGNUP_FAILED,
		LOGIN_FAILED,
		UPLOAD_FAILED
	}
	
	private final DocearServiceExceptionType type;
	
	public DocearServiceException(String message) {
		this(message, null);
	}
	
	public DocearServiceException(String message, DocearServiceExceptionType type) {
		super(message);
		this.type = type;
	}
	

	public DocearServiceExceptionType getType() {
		return type;
	}
	
}
