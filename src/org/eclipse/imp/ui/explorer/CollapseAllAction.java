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
