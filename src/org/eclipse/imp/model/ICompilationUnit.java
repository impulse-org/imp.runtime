/*
 * Created on Mar 13, 2007
 */
package org.eclipse.uide.model;

import lpg.runtime.IMessageHandler;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public interface ICompilationUnit {
    Object getAST(IMessageHandler msgHandler, IProgressMonitor monitor);
    String getName();
    IPath getPath();
    IFile findFile();
    ISourceProject getProject();
}