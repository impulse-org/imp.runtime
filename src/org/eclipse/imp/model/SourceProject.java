/*
 * Created on Mar 13, 2007
 */
package org.eclipse.uide.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;

public class SourceProject implements ISourceProject {
    private final IProject fProject;
    final List<IPathEntry> fBuildPath= new ArrayList<IPathEntry>();
    public SourceProject(IProject project) {
        fProject= project;
    }
    public List<IPathEntry> getBuildPath() {
        return fBuildPath;
    }
    public IProject getRawProject() {
        return fProject.getProject();
    }
}