
/**
 * Provides a simple label provider for projects for use in
 * project selection dialogs.
 * 
 * Copied from ConvertedProjectsPage so as to have one handy
 * within IMP.
 * 
 * @author sutton (Stan Sutton, suttons@us.ibm.com)
 * @since 2007 11 15
 * @see org.eclipse.pde.internal.ui.wizards.tools.ConvertedProjectsPage.LabelProviderForProjects
 * 
 */


package org.eclipse.imp.ui.dialogs.providers;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class LabelProviderForProjects
	extends LabelProvider
	implements ITableLabelProvider
{
	public String getColumnText(Object obj, int index) {
		if (index == 0) 
			return ((IProject) obj).getName();
		return ""; //$NON-NLS-1$
	}
	
	public Image getColumnImage(Object obj, int index) {
		return PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT);
	}
	
}
