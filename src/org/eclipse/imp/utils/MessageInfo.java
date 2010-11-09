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

/**
 * A simple class to save message information sent to an IMessageHandler.
 * @author rfuhrer
 */
public class MessageInfo {
    public final int fSeverity;
    public final String fMessage;
    public final int fStartOffset, fEndOffset, fStartCol, fEndCol, fStartLine, fEndLine;

    public MessageInfo(int severity, String msg, int startOffset, int endOffset, int startCol, int endCol, int startLine, int endLine) {
        fSeverity= severity;
        fMessage= msg;
        fStartLine= startLine;
        fEndLine= endLine;
        fStartCol= startCol;
        fEndCol= endCol;
        fStartOffset= startOffset;
        fEndOffset= endOffset;
    }
}
