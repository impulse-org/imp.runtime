package org.eclipse.imp.ui.explorer;

import org.eclipse.ui.views.framelist.TreeFrame;
import org.eclipse.ui.views.framelist.TreeViewerFrameSource;

class ProjectsFrameSource extends TreeViewerFrameSource {
        private ProjectExplorerPart fPackagesExplorer;
        
        ProjectsFrameSource(ProjectExplorerPart explorer) {
                super(explorer.getViewer());
                fPackagesExplorer= explorer;
        }

        protected TreeFrame createFrame(Object input) {
                TreeFrame frame = super.createFrame(input);
                frame.setName(fPackagesExplorer.getFrameName(input));
                frame.setToolTipText(fPackagesExplorer.getToolTipText(input));
                return frame;
        }

        /**
         * Also updates the title of the packages explorer
         */
        protected void frameChanged(TreeFrame frame) {
                super.frameChanged(frame);
                fPackagesExplorer.updateTitle();
        }

}
