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
import org.eclipse.imp.parser.IMessageHandler;

/**
 * A trivial implementation of the IMessageHandler interface that only records
 * whether any errors were reported, but not what they were.
 * @author rfuhrer
 */
public class ErrorIndicatorMessageHandler implements IMessageHandler {
    private boolean fHadErrors;

    public void clearMessages() {
        fHadErrors= false;
    }

    public void endMessages() { }

    public void startMessageGroup(String groupName) { }

    public void endMessageGroup() { }

    public void handleSimpleMessage(String msg, int startOffset, int endOffset, int startCol, int endCol, int startLine, int endLine) {
        fHadErrors= true;
    }

    public void handleSimpleMessage(String msg, int startOffset, int endOffset, int startCol, int endCol, int startLine, int endLine,
            Map<String, Object> attributes) {
        Object sev= attributes.get(IMessageHandler.SEVERITY_KEY);
        if (sev != null && sev.equals(IMarker.SEVERITY_ERROR)) {
            fHadErrors= true;
        }
    }

    public boolean hadErrors() {
        return fHadErrors;
    }
}