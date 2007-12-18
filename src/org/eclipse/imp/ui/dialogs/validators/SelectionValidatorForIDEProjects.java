package org.eclipse.imp.ui.dialogs.validators;

import org.eclipse.core.resources.IProject;
import org.eclipse.imp.utils.ValidationUtils;
import org.eclipse.ui.dialogs.ISelectionValidator;

public class SelectionValidatorForIDEProjects implements ISelectionValidator {

	public String isValid(Object selection)
	{
		if (!(selection instanceof IProject))
			return "SelectionValidatorForIDEProjects:  selection is not a project";
		if (!ValidationUtils.isJavaProject((IProject) selection))
			return "SelectionValidatorForIDEProjects:  selection is not a Java project";	
		if (!ValidationUtils.isPluginProject((IProject) selection))
			return "SelectionValidatorForIDEProjects:  selection is not a plug-in project";
		if (!ValidationUtils.isIDEProject((IProject) selection))
			return "SelectionValidatorForIDEProjects:  selection is not an IDE project";

		return null;
	}

}
