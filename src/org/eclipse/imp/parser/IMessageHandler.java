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
