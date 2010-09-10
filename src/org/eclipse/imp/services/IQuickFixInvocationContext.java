package org.eclipse.imp.services;

import org.eclipse.imp.model.ICompilationUnit;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;

public interface IQuickFixInvocationContext extends IQuickAssistInvocationContext{
	public ICompilationUnit getModel();
}
