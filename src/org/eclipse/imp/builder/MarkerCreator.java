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

package org.eclipse.imp.builder;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.runtime.RuntimePlugin;

/**
 * This class provides a message handler that creates markers in
 * response to received messages.
 * 
 * MarkerCreators are instantiated with a file (IFile) and a parse
 * controller (IParseController).  The parse controller should
 * be parsing the file and generating the messages that are
 * received by the MarkerCreator.  The MarkerCreator, in turn,
 * creates a problem marker for each error message received,
 * uses the parse controller to compute a line number for the
 * token provided with each message, and attaches the marker to
 * the given file at the computed line.
 */
public class MarkerCreator implements IMessageHandler {
    protected IFile file;
    protected String problemType;

    public MarkerCreator(IFile file) {
        this(file, IMarker.PROBLEM);
    }

    public MarkerCreator(IFile file, String problemType) {
        this.file = file;
        this.problemType = problemType;
    }

    public void clearMessages() {
        // TODO Clear markers on this file?
    }

    void createMarker(String msg, int startOffset, int endOffset,
                      int startCol, int endCol,
                      int startLine, int endLine, Map<String, Object> attributes)
    {
        String[] attributeNames= new String[] {
                IMarker.LINE_NUMBER, IMarker.CHAR_START, IMarker.CHAR_END, IMarker.MESSAGE, IMarker.PRIORITY, IMarker.SEVERITY
        };
        Object[] values= new Object[] {
                startLine, startOffset, endOffset, msg, IMarker.PRIORITY_HIGH, IMarker.SEVERITY_ERROR
        };
        try {
            IMarker m= file.createMarker(problemType);
        
            m.setAttributes(attributeNames, values);
        
            if (attributes != null) {
                for(String key: attributes.keySet()) {
                    m.setAttribute(key, attributes.get(key));
                }
            }
        } catch (CoreException e) {
            RuntimePlugin.getInstance().logException("MarkerCreator.handleMessage(): CoreException caught while trying to create marker", e);
        } catch (Exception e) {
            RuntimePlugin.getInstance().logException("MarkerCreator.handleMessage(): Exception caught while trying to create marker", e);
        }
    }

    public void handleSimpleMessage(String msg, int startOffset, int endOffset,
			int startCol, int endCol, int startLine, int endLine,
			Map<String, Object> attributes) {
		
    	createMarker(msg, startOffset, endOffset, startCol, endCol, startLine, endLine, attributes);
	}

	public void handleSimpleMessage(String msg, int startOffset, int endOffset,
            int startCol, int endCol,
            int startLine, int endLine)
    {	
		createMarker(msg, startOffset, endOffset, startCol, endCol, startLine, endLine, null);
    }

    public void endMessageGroup() { }

    public void startMessageGroup(String groupName) { }

    public void endMessages() { }
}
