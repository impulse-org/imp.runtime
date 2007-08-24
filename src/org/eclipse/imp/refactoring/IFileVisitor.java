package org.eclipse.imp.refactoring;

import org.eclipse.core.resources.IFile;

/**
 * 
 */
public interface IFileVisitor {
    public void enterFile(IFile file);
    public void leaveFile(IFile file);
}