package org.eclipse.uide.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.uide.parser.IParseController;

import lpg.runtime.IMessageHandler;
import lpg.runtime.IPrsStream;
import lpg.runtime.IToken;


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
	
	
	public void handleMessage(int errorCode, int [] msgLocation, int[] errorLocation, String filename, String [] errorInfo) {
            int offset = msgLocation[IMessageHandler.OFFSET_INDEX],
                length = msgLocation[IMessageHandler.LENGTH_INDEX];
            String message = "";
            for (int i = 0; i < errorInfo.length; i++)
                message += (errorInfo[i] + (i < errorInfo.length - 1 ? " " : ""));
            IPrsStream  ps = parseController.getParser().getParseStream();
            IToken token = ps.getTokenAtCharacter(offset);
            int line = token.getLine();

		try {
			// Based closely on "FAQ How do I create problem markers for my compiler?"
			IMarker m = file.createMarker(problemType);
			m.setAttribute(IMarker.LINE_NUMBER, line);
			// SMS 17 May 2006
			// Note: positions of text ranges seem to be handled differently
			// for Annotations and Markers.  Annotations are associated with a
			// Position, which takes an offset and length.  The offset and length
			// provided to handleMessage(..), when used in a Position, enable the
			// annotation to be plotted in the correct place.  Markers, in contrast,
			// get a start and end character.  When the offset and length provided
			// to handleMessage(..) are used in these roles, the Marker is not
			// plotted in the correct place.  In the cases I have seen, the Marker
			// is plotted somewhat in advance of its intended location.  Adding
			// the line number (less one) to the offset yields the correct
			// starting character for the marker.  So that is done here.
			int start = offset + line - 1;
			m.setAttribute(IMarker.CHAR_START, start);
			m.setAttribute(IMarker.CHAR_END, start + length);
			m.setAttribute(IMarker.MESSAGE, message);
			m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
			m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		} catch (CoreException e) {
			System.err.println("MarkerCreator.handleMessage:  CoreException trying to create marker");
		}
	}
}
