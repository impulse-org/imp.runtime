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

package org.eclipse.imp.ui.dialogs.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.imp.language.ServiceFactory;
import org.eclipse.imp.utils.ExtensionUtils;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModel;

/**
 * A content provider that provides all projects, all the time.
 * These are recomputed on each call to retrieve the contents.
 * 
 * @author sutton
 * @since 20071116
 */
public class ContentProviderForIDEProjects implements IStructuredContentProvider {
	public Object[] getElements(Object parent) {	
		return getIDEProjects();
	}
	
	public IProject[] getProjects() {
		return getIDEProjects();
	}
	
	
	private static IProject[] getIDEProjects() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		List<IProject> ideProjects = new ArrayList<IProject>();

		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
            IPluginModel pluginModel= ExtensionUtils.getPluginModel(project);
            if (pluginModel == null)
            	continue;
            IExtensions iExtensions = pluginModel.getExtensions();
            if (iExtensions == null)
            	continue;
            IPluginExtension[] extensions = iExtensions.getExtensions();
            if (extensions == null)
            	continue;

            for (int j = 0; j < extensions.length; j++) {
            	String pointID= extensions[j].getPoint();
                if (pointID.equals(ServiceFactory.LANGUAGE_DESCRIPTION_QUALIFIED_POINT_ID) ||
            	    ServiceFactory.ALL_SERVICES.contains(pointID)) {
            		ideProjects.add(project);
            		break;
            	}
            }
		}

 		return ideProjects.toArray(new IProject[ideProjects.size()]);
	}

    public void dispose() { }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
}
