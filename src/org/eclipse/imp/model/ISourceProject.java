/*******************************************************************************
* Copyright (c) 2007 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation

*******************************************************************************/

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
