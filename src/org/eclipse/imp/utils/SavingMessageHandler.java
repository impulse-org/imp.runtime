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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.imp.parser.IMessageHandler;

/**
 * An implementation of {@link IMessageHandler} that merely stores away the messages
 * received in a list of {@link MessageInfo} objects, for later retrieval by clients.
 * @author rfuhrer
 */
public final class SavingMessageHandler implements IMessageHandler {
    private final List<MessageInfo> fMessages= new ArrayList<MessageInfo>();

    public void clearMessages() {
        fMessages.clear();
    }

    public void endMessageGroup() { }

    public void startMessageGroup(String groupName) { }

    public void endMessages() { }

    public void handleSimpleMessage(String msg, int startOffset, int endOffset, int startCol, int endCol, int startLine, int endLine) {
        fMessages.add(new MessageInfo(IMarker.SEVERITY_INFO, msg, startOffset, endOffset, startCol, endCol, startLine, endLine));
    }

    public void handleSimpleMessage(String msg, int startOffset, int endOffset, int startCol, int endCol, int startLine, int endLine, Map<String, Object> attributes) {
        Object severityValue= attributes.get(IMessageHandler.SEVERITY_KEY);
        int severity= (severityValue instanceof Integer) ? ((Integer) severityValue).intValue() : IMarker.SEVERITY_INFO;
        fMessages.add(new MessageInfo(severity, msg, startOffset, endOffset, startCol, endCol, startLine, endLine));
    }

    public List<MessageInfo> getMessages() {
        return Collections.unmodifiableList(fMessages);
    }

    public String getConcatenatedMessages() {
        StringBuilder sb= new StringBuilder();

        for(MessageInfo info: getMessages()) {
            sb.append("Line " + info.fStartLine + ", column " + info.fStartCol + ": " + info.fMessage + "\n");
        }
        return sb.toString();
    }
}
