/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
/*
 * Created on Mar 13, 2007
 */
package org.eclipse.imp.model.internal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.model.IPathEntry;

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

    public IPath getPath() {
        return fPath;
    }

    public String toString() {
	return "<" + fType + ": " + fPath.toPortableString() + ">";
    }
}
