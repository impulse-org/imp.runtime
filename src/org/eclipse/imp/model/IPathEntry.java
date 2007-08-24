/*
 * Created on Mar 13, 2007
 */
package org.eclipse.imp.model;

import org.eclipse.core.runtime.IPath;

public interface IPathEntry {
    public enum PathEntryType {
        /**
         * Path entry corresponding to a source folder in the referencing project. The
         * associated path must be absolute (not project-relative).
         **/
	SOURCE_FOLDER,
        /**
         * Path entry corresponding to an archive (jar, zip) somewhere in the workspace.
         * The associated path must be absolute (not project-relative).
         **/
	ARCHIVE,
        /**
         * Path entry corresponding to a project dependency. The
         * associated path must be absolute (not project-relative).
         **/
	PROJECT,
        /**
         * Path entry corresponding to some form of pseudo-container (not an archive),
         * e.g. the set of plugin dependencies. Not sure what the path means in this
         * case.
         **/
	CONTAINER
    }

    /**
     * @return the type of this path entry
     */
    PathEntryType getEntryType();

    /**
     * @return the path associated with this path entry. Different entry types may have
     * different interpretations for the associated path. @see PathEntryType
     */
    IPath getPath();
}
