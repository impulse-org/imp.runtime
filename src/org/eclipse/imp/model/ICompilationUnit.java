/*
 * Created on Mar 13, 2007
 */
package org.eclipse.uide.model;

import lpg.runtime.IMessageHandler;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public interface ICompilationUnit extends ISourceEntity {
    String getName();

    ISourceProject getProject();

    IPath getPath();

    /**
     * @return the file corresponding to the receiver, if possible, or null if not.
     */
    IFile getFile();

    String getSource();

    Object getAST(IMessageHandler msgHandler, IProgressMonitor monitor);
}
