package org.eclipse.imp.utils;

import java.util.Map;

import org.eclipse.imp.parser.IMessageHandler;

/**
 * Do-nothing implementation of IMessageHandler - throws away all messages.<br>
 * Please only use if you are sure it's appropriate to throw away this information!
 * @author rfuhrer@watson.ibm.com
 */
public class NullMessageHandler implements IMessageHandler {
	public void clearMessages() { }

    public void endMessages() { }

    public void startMessageGroup(String groupName) { }

	public void endMessageGroup() { }

	public void handleSimpleMessage(String msg, int startOffset, int endOffset,
			int startCol, int endCol, int startLine, int endLine,
			Map<String, Object> attributeMap) { }

	public void handleSimpleMessage(String msg, int startOffset, int endOffset,
			int startCol, int endCol, int startLine, int endLine) { }
}
