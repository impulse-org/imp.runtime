package org.eclipse.imp.ui.dialogs.filters;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class ViewerFilterForPluginProjects extends ViewerFilter {
	
    /**
     * Returns whether the given element makes it through this filter.
     *
     * @param viewer the viewer
     * @param parentElement the parent element
     * @param element the element
     * @return <code>true</code> if element is included in the
     *   filtered set, and <code>false</code> if excluded
     */
    public boolean select(Viewer viewer, Object parentElement,
            Object element)
    {
    	if (element instanceof IProject) {
			IProject project = (IProject) element;
			try {
				return (project.exists() && project.hasNature("org.eclipse.pde.PluginNature"));	
		    } catch (CoreException e) {
		    	// This gets thrown when a project is not open; finding such
		    	// a project is not an error, so don't report it as such
//		    	ErrorHandler.reportError(
//		    		"ViewerFilterforPluginProjects.select(..):  Core exception evaluating project = " + project.getName()
//		    		+ "; returning false", e);
		    	return false;
		    }
    	}
    	return false;
    }
}
