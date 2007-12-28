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
