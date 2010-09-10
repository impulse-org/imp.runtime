/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 *******************************************************************************/

package org.eclipse.imp.utils;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.imp.parser.IMessageHandler;

public final class LogMessageHandler implements IMessageHandler {
    private ILog fLog;

    public LogMessageHandler(ILog log) {
        fLog= log;
    }
    public void clearMessages() { }

    public void endMessages() { }

    public void startMessageGroup(String groupName) { }

    public void endMessageGroup() { }

    public void handleSimpleMessage(String msg, int startOffset, int endOffset, int startCol, int endCol, int startLine, int endLine) {
        String logMsg= msg + "@" + startOffset + "-" + endOffset;
        Status status= new Status(Status.INFO, fLog.getBundle().getSymbolicName(), 0, logMsg, null);

        fLog.log(status);
    }

    public void handleSimpleMessage(String msg, int startOffset, int endOffset, int startCol, int endCol, int startLine, int endLine,
            Map<String, Object> attributes) {
        Object sev= attributes.get(IMessageHandler.SEVERITY_KEY);
        String logMsg;

        if (sev != null && sev.equals(IMarker.SEVERITY_ERROR)) {
            logMsg= "ERROR: " + msg + "@" + startOffset + "-" + endOffset;
        } else {
            logMsg= msg + "@" + startOffset + "-" + endOffset;
        }
        Status status= new Status(Status.INFO, fLog.getBundle().getSymbolicName(), 0, logMsg, null);

        fLog.log(status);
    }
}