package org.eclipse.imp.ui.dialogs.providers;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.pde.internal.ui.elements.DefaultContentProvider;

public class ContentProviderForGivenProjects
	extends DefaultContentProvider implements IStructuredContentProvider
{

	private IProject[] fProjects;

	public Object[] getElements(Object parent) {	
		if (fProjects!= null)
			return fProjects;
		return new Object[0];
	}
	
	public void setProjects(IProject[] projects) {
		fProjects = projects;
	}
	
	public IProject[] getProjects() {
		return fProjects;
	}

	
}
