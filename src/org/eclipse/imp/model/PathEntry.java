/*
 * Created on Mar 13, 2007
 */
package org.eclipse.uide.model;

import org.eclipse.core.runtime.IPath;

public class PathEntry implements IPathEntry {
    private final IPath fPath;
    private final PathEntryType fType;
    public PathEntry(PathEntryType type, IPath path) {
        fType= type;
        fPath= path;
    }
    public PathEntryType getEntryType() {
        return fType;
    }
    public IPath getProjectRelativePath() {
        return fPath;
    }
}