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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.ui.dialogs.providers.ContentProviderForIDEProjects;

public class ValidationUtils {

	public static boolean isJavaProject(IProject project) {
	    if (project == null) return false;
	    try {
	    	return (project.exists() && project.isOpen() && project.hasNature("org.eclipse.jdt.core.javanature"));	
	    } catch (CoreException e) {
	    	ErrorHandler.reportError(
	    		"ValidatorUtils.isPluginProject:  Core exception validating project = " + project.getName()
	    		+ "; returning false", e);
	    	return false;
	    }
	}
	
	
	public static boolean isPluginProject(IProject project) {
	    if (project == null) return false;
	    try {
	    	return (project.exists() && project.hasNature("org.eclipse.pde.PluginNature"));	
	    } catch (CoreException e) {
	    	ErrorHandler.reportError(
	    		"ValidatorUtils.isPluginProject:  Core exception validating project = " + project.getName()
	    		+ "; returning false", e);
	    	return false;
	    }
	}
	

	public static boolean isIDEProject(IProject project) {
		ContentProviderForIDEProjects provider = new ContentProviderForIDEProjects();
		IProject[] ideProjects = provider.getProjects();
		for (int i = 0; i < ideProjects.length; i++) {
			if (ideProjects[i].equals(project))
				return true;
		}
		return false;
	}
	
}