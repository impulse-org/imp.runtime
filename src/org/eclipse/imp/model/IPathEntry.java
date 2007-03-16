/*
 * Created on Mar 13, 2007
 */
package org.eclipse.uide.model;

import org.eclipse.core.runtime.IPath;

public interface IPathEntry {
    public enum PathEntryType {
        SOURCE_FOLDER, ARCHIVE
    }
    IPathEntry.PathEntryType getEntryType();
    IPath getProjectRelativePath();
}