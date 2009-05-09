package org.eclipse.imp.services;

import org.eclipse.core.resources.IFile;
import org.eclipse.imp.language.ILanguageService;

public interface IToggleBreakpointsHandler extends ILanguageService {
	void clearLineBreakpoint(IFile file, int lineNumber);
	void setLineBreakpoint(IFile file, int lineNumber);
	void disableLineBreakpoint(IFile file, int lineNumber);
	void enableLineBreakpoint(IFile file, int lineNumber);
	// TODO
	//void toggleEntryBreakpoint(IFile file, int lineNumber);
}
