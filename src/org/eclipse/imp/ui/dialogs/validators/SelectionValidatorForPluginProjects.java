package org.eclipse.imp.ui.dialogs.validators;

import org.eclipse.core.resources.IProject;
import org.eclipse.imp.utils.ValidationUtils;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.dialogs.ISelectionValidator;

public class SelectionValidatorForPluginProjects implements ISelectionValidator {

	public String isValid(Object selection) {
		if (!(selection instanceof IProject))
			return "SelectionValidatorForPluginProjects:  selection is not a project";
		if (!ValidationUtils.isJavaProject((IProject) selection))
			return "SelectionValidatorForPluginProjects:  selection is not a Java project";
		if (!ValidationUtils.isPluginProject((IProject) selection)) {
			return "SelectionValidatorForPluginProjects:  selection is not a plug-in project";
		}
		return null;
	}

}
