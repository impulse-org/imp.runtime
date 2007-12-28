package org.eclipse.imp.ui.explorer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.Assert;

public class ViewAction extends Action {
    private final ViewActionGroup fActionGroup;

    private final int fMode;

    public ViewAction(ViewActionGroup group, int mode) {
        super("", AS_RADIO_BUTTON); //$NON-NLS-1$
        Assert.isNotNull(group);
        fActionGroup= group;
        fMode= mode;
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        if (isChecked())
            fActionGroup.setMode(fMode);
    }
}
