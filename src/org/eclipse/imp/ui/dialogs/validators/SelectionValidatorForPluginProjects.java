package org.eclipse.imp.ui.dialogs.validators;

import org.eclipse.core.resources.IProject;
import org.eclipse.imp.utils.ValidationUtils;
import org.eclipse.ui.dialogs.ISelectionValidator;

public class SelectionValidatorForPluginProjects implements ISelectionValidator {

	public String isValid(Object selection) {
		if (!(selection instanceof IProject))
			return "Selection is not a project";
		if (!ValidationUtils.isJavaProject((IProject) selection))
			return "Selection is not a Java project";
		if (!ValidationUtils.isPluginProject((IProject) selection)) {
			return "Selection is not a plug-in project";
		}
		return null;
	}

}
