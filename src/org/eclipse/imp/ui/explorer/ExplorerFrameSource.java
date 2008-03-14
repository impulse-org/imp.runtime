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

import org.eclipse.ui.views.framelist.TreeFrame;
import org.eclipse.ui.views.framelist.TreeViewerFrameSource;

class ExplorerFrameSource extends TreeViewerFrameSource {
    private ProjectExplorerPart fProjectExplorer;

    ExplorerFrameSource(ProjectExplorerPart explorer) {
        super(explorer.getViewer());
        fProjectExplorer= explorer;
    }

    protected TreeFrame createFrame(Object input) {
        TreeFrame frame= super.createFrame(input);
        frame.setName(fProjectExplorer.getFrameName(input));
        frame.setToolTipText(fProjectExplorer.getToolTipText(input));
        return frame;
    }

    /**
     * Also updates the title of the packages explorer
     */
    protected void frameChanged(TreeFrame frame) {
        super.frameChanged(frame);
        fProjectExplorer.updateTitle();
    }
}
