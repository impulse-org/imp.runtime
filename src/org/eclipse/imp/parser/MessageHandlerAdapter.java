/**
 * 
 */
package org.eclipse.imp.parser;


public class MessageHandlerAdapter implements lpg.runtime.IMessageHandler {
    private final IMessageHandler fIMPHandler;

    public MessageHandlerAdapter(IMessageHandler impHandler) {
        fIMPHandler= impHandler;
    }

    public void handleMessage(int errorCode, int[] msgLocation, int[] errorLocation, String filename, String[] errorInfo) {
        int startOffset= msgLocation[lpg.runtime.IMessageHandler.OFFSET_INDEX];
        int length= msgLocation[lpg.runtime.IMessageHandler.LENGTH_INDEX];
        int startLine= msgLocation[lpg.runtime.IMessageHandler.START_LINE_INDEX];
        int endLine= msgLocation[lpg.runtime.IMessageHandler.END_LINE_INDEX];
        int startCol= msgLocation[lpg.runtime.IMessageHandler.START_COLUMN_INDEX];
        int endCol= msgLocation[lpg.runtime.IMessageHandler.END_COLUMN_INDEX];
        String message = "";
        for (int i = 0; i < errorInfo.length; i++)
            message += (errorInfo[i] + (i < errorInfo.length - 1 ? " " : ""));

        fIMPHandler.handleSimpleMessage(message, startOffset, startOffset + length - 1,
                startCol, endCol, startLine, endLine);
    }
}