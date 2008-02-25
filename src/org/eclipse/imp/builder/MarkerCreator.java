/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.IParseController;

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
	
    private IParseController parseController = null;
    private IFile file = null;
    private String problemType = IMarker.PROBLEM;

    public MarkerCreator(
            IFile file, IParseController parseController)
    {
        this.parseController = parseController;
        this.file = file;
    }

    public MarkerCreator(
            IFile file,
            IParseController parseController,
            String problemType)
    {
        this.file = file;
        this.parseController = parseController;
        this.problemType = problemType;
    }

    public void handleSimpleMessage(String msg, int startOffset, int endOffset,
            int startCol, int endCol,
            int startLine, int endLine) {
        try {
            // Based closely on the Eclipse "FAQ How do I create problem markers for my compiler?"
            IMarker m = file.createMarker(problemType);
            m.setAttribute(IMarker.LINE_NUMBER, startLine);
            // SMS 23 Apr 2007
            // Removed previously added adjustment to start offset that seems
            // to have been rendered unnecessary by a fix somewhere else
            m.setAttribute(IMarker.CHAR_START, startOffset);
            m.setAttribute(IMarker.CHAR_END, endOffset);
            m.setAttribute(IMarker.MESSAGE, msg);
            m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
            m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
        } catch (CoreException e) {
            System.err.println("MarkerCreator.handleMessage:  CoreException trying to create marker");
        }
    }

    public void endMessageGroup() { }

    public void startMessageGroup(String groupName) { }
}
