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

package org.eclipse.imp.ui.dialogs.validators;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.dialogs.ISelectionValidator;

public class SelectionValidatorForAllProjects implements ISelectionValidator {
	private static final String ERROR_MSG_NOT_A_PROJECT= "Selection is not a project";

    public String isValid(Object selection) {
		if (!(selection instanceof IProject))
			return ERROR_MSG_NOT_A_PROJECT;
		return null;
	}
}
