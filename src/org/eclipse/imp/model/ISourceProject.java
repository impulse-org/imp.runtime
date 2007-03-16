/*
 * Created on Mar 13, 2007
 */
package org.eclipse.uide.model;

import java.util.List;

import org.eclipse.core.resources.IProject;

public interface ISourceProject {
    // TODO Put one of these in the IParseController...
    List<IPathEntry> getBuildPath();
    IProject getRawProject();
}