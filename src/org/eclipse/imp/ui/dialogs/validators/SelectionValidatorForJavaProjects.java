package org.eclipse.imp.ui.dialogs.validators;

import org.eclipse.core.resources.IProject;
import org.eclipse.imp.utils.ValidationUtils;
import org.eclipse.ui.dialogs.ISelectionValidator;

public class SelectionValidatorForJavaProjects implements ISelectionValidator {

	public String isValid(Object selection) {
		if (!(selection instanceof IProject))
			return "SelectionValidatorForJavaProjects:  selection is not a project";
		if (!ValidationUtils.isJavaProject((IProject) selection))
			return "SelectionValidatorForJavaProjects:  selection is not a Java project";
		return null;
	}

}
