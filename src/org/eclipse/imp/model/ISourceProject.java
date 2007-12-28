/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
/*
 * Created on Mar 13, 2007
 */
package org.eclipse.imp.model;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public interface ISourceProject extends ISourceContainer {
    /**
     * @return the build path, a List of IPathEntry's
     */
    List<IPathEntry> getBuildPath();

    /**
     * @return the underlying IProject
     */
    IProject getRawProject();

    /**
     * @return the resolved and possibly filesystem-absolute path corresponding to the given IPath
     */
    IPath resolvePath(IPath path);

    ISourceFolder[] getSourceRoots();

    ISourceEntity[] getChildren();
}
