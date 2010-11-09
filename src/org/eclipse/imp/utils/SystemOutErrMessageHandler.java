package org.eclipse.imp.utils;

import java.io.PrintStream;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.imp.parser.IMessageHandler;

/**
 * A trivial implementation of IMessageHandler that sends messages to System.out/err.<br>
 * <b>Intended only for debugging; normal Eclipse users may not see such output.</b>
 */
public class SystemOutErrMessageHandler implements IMessageHandler {
    public void clearMessages() { }

    public void endMessages() { }

    public void startMessageGroup(String groupName) { }

    public void endMessageGroup() { }

    public void handleSimpleMessage(String msg, int startOffset, int endOffset, int startCol, int endCol, int startLine, int endLine) {
        System.out.println("[line " + startLine + ", col " + startCol + "]: " + msg);
    }

    public void handleSimpleMessage(String msg, int startOffset, int endOffset, int startCol, int endCol, int startLine, int endLine,
            Map<String, Object> attributes) {
        Object sev= attributes.get(IMessageHandler.SEVERITY_KEY);
        PrintStream ps;
        if (sev != null && sev.equals(IMarker.SEVERITY_ERROR)) {
            ps= System.err;
        } else {
            ps= System.out;
        }
        ps.println("[" + startOffset + ":" + (endOffset - startOffset + 1) + "]: " + msg);
    }
}