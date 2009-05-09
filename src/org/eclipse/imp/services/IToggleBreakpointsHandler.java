package org.eclipse.imp.services;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.imp.language.ILanguageService;

public interface IToggleBreakpointsHandler extends ILanguageService {
	void clearLineBreakpoint(IFile file, int lineNumber, IMarker marker);
	void setLineBreakpoint(IFile file, int lineNumber, IMarker marker);
	void disableLineBreakpoint(IFile file, int lineNumber, IMarker marker);
	void enableLineBreakpoint(IFile file, int lineNumber, IMarker marker);
	// TODO
	//void toggleEntryBreakpoint(IFile file, int lineNumber);
}
