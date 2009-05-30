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
import org.eclipse.imp.utils.ValidationUtils;

public class SelectionValidatorForPluginProjects extends SelectionValidatorForJavaProjects {

	public String isValid(Object selection) {
	    String result= super.isValid(selection);
	    if (result == null) {
	        if (!ValidationUtils.isPluginProject((IProject) selection)) {
	            return "Selection is not a plug-in project";
	        }
		}
		return null;
	}
}
