/*
 * Created on Nov 1, 2005
 */
package org.eclipse.uide.runtime;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public abstract class SAFARIPluginBase extends AbstractUIPlugin implements IPluginLog {
    private ILog sLog= null;

    protected boolean fEmitInfoMessages= false;

    public abstract String getID();

    public void maybeWriteInfoMsg(String msg) {
        if (!fEmitInfoMessages)
            return;
    
        Status status= new Status(Status.INFO, getID(), 0, msg, null);
    
        if (sLog == null)
            sLog= getLog();
    
        sLog.log(status);
    }

    public void writeErrorMsg(String msg) {
        Status status= new Status(Status.ERROR, getID(), 0, msg, null);
    
        if (sLog == null)
            sLog= getLog();
    
        sLog.log(status);
    }

    public void logException(String msg, Throwable t) {
        Status status= new Status(Status.ERROR, getID(), 0, msg, t);

        if (sLog == null)
            sLog= getLog();

        sLog.log(status);
    }

    public void refreshPrefs() {
	// default: do nothing, no preferences
    }
}
