package org.eclipse.imp.ui.dialogs.filters;

import org.eclipse.core.resources.IProject;
import org.eclipse.imp.utils.ValidationUtils;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class ViewerFilterForJavaProjects extends ViewerFilter {
	
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
    		if (ValidationUtils.isJavaProject((IProject) element)) {
    			return true;
    		}
    	}
    	return false;
    }

}
