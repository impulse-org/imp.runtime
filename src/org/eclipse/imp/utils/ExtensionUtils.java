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

package org.eclipse.imp.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;

public class ExtensionUtils {

	// Copied and adapted from ExtensionPointEnabler
    public static IPluginModel getPluginModel(final IProject project) {
    	try {
    	    if (project == null)
    	    	return null;
    	    	
        	IFile pluginXML= project.getFile("plugin.xml"); 
        	if (!pluginXML.exists())
        		return null;

        	return getPluginModelForProject(project);
    	} catch (Exception e) {
    	    //ErrorHandler.reportError("Could not find plugin for project " + project.getName(), true, e);
    	    return null;
    	}
    }

    // Copied from ExtensionPointEnabler
    public static IPluginModel getPluginModelForProject(final IProject project) {
//      WorkspaceModelManager wmm = PDECore.getDefault().getWorkspaceModelManager();
//      IPluginModelBase[] wsPlugins= wmm.getFeatureModel(project).getWorkspaceModels();
		PluginModelManager pmm = PDECore.getDefault().getModelManager();
		IPluginModelBase[] wsPlugins= pmm.getWorkspaceModels();
	
		if (wsPlugins.length == 0) {
		    //ErrorHandler.reportError("Project " + project.getName() + " is not a plugin project (no plugin projects)?", true);
		    return null;
		}
		for(int i= 0; i < wsPlugins.length; i++) {
		    IPluginModelBase wsPlugin= wsPlugins[i];
	//	    if (wsPlugin.getBundleDescription().getName().equals(project.getName())) {
		    
		    // SMS 19 Jul 2006
		    // May get both workspace and project plugin models
		    // (although only the latter are of interest)
		    IPluginBase pmBase = wsPlugin.getPluginBase();
		    if (pmBase == null) continue;
		    String id = pmBase.getId();
		    if (id == null) continue;
		    String projName = project.getName();
		    if (projName == null) continue;
		       
		    // SMS 22 Mar 2007:  This depends on the plugin id being equal to the project name
//		    if (wsPlugin.getPluginBase().getId().equals(project.getName())) {
//		        return (IPluginModel) wsPlugin;
//		    }
		    // SMS 22 Mar 2007 Use this, instead:
		    String resourceLocation = pmBase.getModel().getUnderlyingResource().getLocation().toString();
		    if (resourceLocation.endsWith(projName + "/META-INF/MANIFEST.MF")) {
		    	return (IPluginModel) wsPlugin;
		    }
		    
		}
		//ErrorHandler.reportError("Could not find plugin for project " + project.getName(), true);
		return null;
    }
	
}
