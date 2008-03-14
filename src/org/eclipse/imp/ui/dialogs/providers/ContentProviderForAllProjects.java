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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.pde.internal.ui.elements.DefaultContentProvider;


/**
 * A content provider that provides all projects, all the time.
 * These are recomputed on each call to retrieve the contents.
 * 
 * @author sutton
 * @since 20071116
 *
 */
public class ContentProviderForAllProjects
	extends DefaultContentProvider implements IStructuredContentProvider
{
	public Object[] getElements(Object parent) {	
		return ResourcesPlugin.getWorkspace().getRoot().getProjects();
	}
	
	public IProject[] getProjects() {
		return ResourcesPlugin.getWorkspace().getRoot().getProjects();
	}
}
