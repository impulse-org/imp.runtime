package org.eclipse.imp.utils;

public class ExtensionException extends Exception {
	private static final long serialVersionUID = 6634753150865956173L;

	public ExtensionException() {
		super();
	}
	
	public ExtensionException(String message) {
		super(message);
	}
	
	public ExtensionException(String message, Throwable cause) {
		super(message, cause);
	}
}
