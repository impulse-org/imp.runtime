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

package org.eclipse.imp.ui.explorer;

import org.eclipse.jface.action.Action;

class CollapseAllAction extends Action {
    private ProjectExplorerPart fProjectExplorer;

    CollapseAllAction(ProjectExplorerPart part) {
        super(ExplorerMessages.CollapseAllAction_label);
        setDescription(ExplorerMessages.CollapseAllAction_description);
        setToolTipText(ExplorerMessages.CollapseAllAction_tooltip);
//      JavaPluginImages.setLocalImageDescriptors(this, "collapseall.gif"); //$NON-NLS-1$
        fProjectExplorer= part;
        // PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.COLLAPSE_ALL_ACTION);
    }

    public void run() {
        fProjectExplorer.collapseAll();
    }
}
