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

package org.eclipse.imp.parser;

public interface IMessageHandler {
    /**
     * @param msg
     * @param startOffset 0-based, inclusive
     * @param endOffset 0-based, inclusive
     * @param startCol ?-based, inclusive
     * @param endCol ?-based, inclusive
     * @param startLine ?-based, inclusive
     * @param endLine ?-based, inclusive
     */
    void handleSimpleMessage(String msg, int startOffset, int endOffset,
            int startCol, int endCol,
            int startLine, int endLine);

    void startMessageGroup(String groupName);
    void endMessageGroup();
}
