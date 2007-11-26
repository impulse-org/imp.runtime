package org.eclipse.imp.ui.dialogs.validators;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.dialogs.ISelectionValidator;

public class SelectionValidatorForAllProjects implements ISelectionValidator {

	public String isValid(Object selection) {
		if (!(selection instanceof IProject))
			return "SelectionValidatorForAllProjects:  selection is not a project";
		return null;
	}

}
