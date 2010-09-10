package org.eclipse.imp.editor.quickfix;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.imp.editor.UniversalEditor;



public interface IAnnotation {

	static final int WARNING = IStatus.WARNING;
	static final int ERROR = IStatus.ERROR;
	static final int INFO = IStatus.INFO;
	
	/**
	 * Returns the problem id or <code>-1</code> if no problem id can be evaluated.
	 *
	 * @return returns the problem id or <code>-1</code>
	 */
	int getId();
	
	/**
	 * Returns the attribute or null if the attribute doesn't exist.
	 *
	 * @return Returns the attribute or <code>null</code>
	 */
	Object getAttribute(String key);
	
	/**
	 * Returns the severity code of the annotation
	 * 
	 * @return Returns the severity code of the annotation
	 */
	int getSeverity();
	
	/**
	 * Returns the UniversalEditor corresponding to the document on which the annotation is set
	 * or <code>null</code> if no corresponding editor exists.
	 */
	public UniversalEditor getEditor();
}
