/*
 * Created on Mar 30, 2007
 */
package org.eclipse.imp.model;

import org.eclipse.core.runtime.IProgressMonitor;

public interface ISourceEntity {
    /**
     * Commit any pending changes to the given entity to disk.
     * @param monitor
     */
    void commit(IProgressMonitor monitor);
}
