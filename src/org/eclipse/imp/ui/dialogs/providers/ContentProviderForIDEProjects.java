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
import org.eclipse.imp.utils.ExtensionUtils;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.internal.ui.elements.DefaultContentProvider;


/**
 * A content provider that provides all projects, all the time.
 * These are recomputed on each call to retrieve the contents.
 * 
 * @author sutton
 * @since 20071116
 *
 */
public class ContentProviderForIDEProjects
	extends DefaultContentProvider implements IStructuredContentProvider
{
	public Object[] getElements(Object parent) {	
		return getIDEProjects();
	}
	
	public IProject[] getProjects() {
		return getIDEProjects();
	}
	
	
	private static IProject[] getIDEProjects() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		List ideProjects = new ArrayList();
		
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
            	if (extensions[j].getPoint().equals("org.eclipse.imp.runtime.languageDescription")) {
            		ideProjects.add(project);
            		break;
            	}
            }
		}

		IProject[] result = new IProject[ideProjects.size()];
		for (int i = 0; i < ideProjects.size(); i++) {
			result[i] = (IProject) ideProjects.get(i);
		}
 		return result;
	}
	
}
