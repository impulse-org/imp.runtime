package org.eclipse.imp.editor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;

public class GotoMatchingFenceAction extends Action {
    private final UniversalEditor fEditor;

    public GotoMatchingFenceAction(UniversalEditor editor) {
            super("Go to Matching Fence");
            Assert.isNotNull(editor);
            fEditor= editor;
            setEnabled(true);
//          PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.GOTO_MATCHING_BRACKET_ACTION);
    }

    public void run() {
            fEditor.gotoMatchingFence();
    }
}
