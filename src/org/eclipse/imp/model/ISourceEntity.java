/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
/*
 * Created on Mar 30, 2007
 */
package org.eclipse.imp.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

public interface ISourceEntity {
    String getName();

    ISourceEntity getParent();

    ISourceEntity getAncestor(Class ofType);

    IResource getResource();

    /**
     * Commit any pending changes to the given entity to disk.
     * @param monitor
     */
    void commit(IProgressMonitor monitor);
}
