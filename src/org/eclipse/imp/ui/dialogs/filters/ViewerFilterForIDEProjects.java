/*******************************************************************************
* Copyright (c) 2007 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation

*******************************************************************************/

package org.eclipse.imp.ui.dialogs.filters;

import org.eclipse.core.resources.IProject;
import org.eclipse.imp.utils.ExtensionUtils;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModel;

public class ViewerFilterForIDEProjects extends ViewerFilter {
	
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
            IPluginModel pluginModel= ExtensionUtils.getPluginModel(project);
            if (pluginModel == null)
            	return false;
            IExtensions iExtensions = pluginModel.getExtensions();
            if (iExtensions == null)
            	return false;
            IPluginExtension[] extensions = iExtensions.getExtensions();
            if (extensions == null)
            	return false;
            
            for (int j = 0; j < extensions.length; j++) {
            	if (extensions[j].getPoint().equals("org.eclipse.imp.runtime.languageDescription"))
            		return true;
            }
    	}
    	return false;
    }

}
