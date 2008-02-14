package org.eclipse.imp.language;

/**
 * These kinds of errors should really be flushed out in the first tests
 * of a language service implementation. That is why they are RuntimeExceptions,
 * instead of something that needs to be caught and dealt with by the IDE.
 * 
 * @author jurgenv
 *
 */
public class ServiceException extends RuntimeException {
	private static final long serialVersionUID = 6634753150865956173L;

	public ServiceException() {
		super();
	}
	
	public ServiceException(String message) {
		super(message);
	}
	
	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
