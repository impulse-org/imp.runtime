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

/*
 * Created on Nov 1, 2005
 */
package org.eclipse.imp.runtime;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.imp.preferences.PreferencesService;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public abstract class PluginBase extends AbstractUIPlugin implements IPluginLog {
    private ILog sLog= null;

    protected boolean fEmitInfoMessages= false;

    public abstract String getID();

    public void maybeWriteInfoMsg(String msg) {
        if (!fEmitInfoMessages)
            return;
        if (msg == null) {
        	msg = "No message given";
        }
        writeInfoMsg(msg);
    }

    public void writeInfoMsg(String msg) {
    	if (msg == null)
    		msg = "No message given";
    	Status status= new Status(Status.INFO, getID(), 0, msg, null);

        if (sLog == null)
            sLog= getLog();

        sLog.log(status);
    }

    public void writeErrorMsg(String msg) {
    	if (msg == null)
    		msg = "No message given";
        Status status= new Status(Status.ERROR, getID(), 0, msg, null);

        if (sLog == null)
            sLog= getLog();

        sLog.log(status);
    }

    public void logException(String msg, Throwable t) {
    	if (msg == null) {
    		if (t == null || t.getMessage() == null)
        		msg = "No message given";
    		else
    			msg = t.getMessage();
    	}

        Status status= new Status(Status.ERROR, getID(), 0, msg, t);

        if (sLog == null)
            sLog= getLog();

        sLog.log(status);
    }

    public void refreshPrefs() {
	// default: do nothing, no preferences
    }
    
    
    // SMS 22 Aug 2006
    protected static PreferencesService preferencesService = null;
    public static PreferencesService getPreferencesService() {
    	if (preferencesService == null) {
    		preferencesService = new PreferencesService(ResourcesPlugin.getWorkspace().getRoot().getProject());
    	}
    	return preferencesService;
    }

    
}
