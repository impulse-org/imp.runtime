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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * Adds view menus to switch between flat and hierarchical layout.
 * 
 * @since 2.1
 */
class LayoutActionGroup extends MultiActionGroup {
    LayoutActionGroup(ProjectExplorerPart projectExplorer) {
        super(createActions(projectExplorer), getSelectedState(projectExplorer));
    }

    /*
     * (non-Javadoc)
     * 
     * @see ActionGroup#fillActionBars(IActionBars)
     */
    public void fillActionBars(IActionBars actionBars) {
        super.fillActionBars(actionBars);
        contributeToViewMenu(actionBars.getMenuManager());
    }

    private void contributeToViewMenu(IMenuManager viewMenu) {
        viewMenu.add(new Separator());
        // Create layout sub menu
        IMenuManager layoutSubMenu= new MenuManager(ExplorerMessages.LayoutActionGroup_label);
        final String layoutGroupName= "layout"; //$NON-NLS-1$
        Separator marker= new Separator(layoutGroupName);
        viewMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        viewMenu.add(marker);
        viewMenu.appendToGroup(layoutGroupName, layoutSubMenu);
        viewMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end"));//$NON-NLS-1$                
        addActions(layoutSubMenu);
    }

    static int getSelectedState(ProjectExplorerPart projectExplorer) {
        if (projectExplorer.isFlatLayout())
            return 0;
        else
            return 1;
    }

    static IAction[] createActions(ProjectExplorerPart projectExplorer) {
        IAction flatLayoutAction= new LayoutAction(projectExplorer, true);
        flatLayoutAction.setText(ExplorerMessages.LayoutActionGroup_flatLayoutAction_label);
//      JavaPluginImages.setLocalImageDescriptors(flatLayoutAction, "flatLayout.gif"); //$NON-NLS-1$
        IAction hierarchicalLayout= new LayoutAction(projectExplorer, false);
        hierarchicalLayout.setText(ExplorerMessages.LayoutActionGroup_hierarchicalLayoutAction_label);
//      JavaPluginImages.setLocalImageDescriptors(hierarchicalLayout, "hierarchicalLayout.gif"); //$NON-NLS-1$
        return new IAction[] { flatLayoutAction, hierarchicalLayout };
    }
}

class LayoutAction extends Action implements IAction {
    private boolean fIsFlatLayout;

    private ProjectExplorerPart fProjectExplorer;

    public LayoutAction(ProjectExplorerPart packageExplorer, boolean flat) {
        super("", AS_RADIO_BUTTON); //$NON-NLS-1$
        fIsFlatLayout= flat;
        fProjectExplorer= packageExplorer;
//        if (fIsFlatLayout)
//            PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.LAYOUT_FLAT_ACTION);
//        else
//            PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.LAYOUT_HIERARCHICAL_ACTION);
    }

    /*
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
        if (fProjectExplorer.isFlatLayout() != fIsFlatLayout)
            fProjectExplorer.toggleLayout();
    }
}
