package org.eclipse.uide.defaults;

import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.editor.IHoverHelper;
import org.eclipse.uide.parser.Ast;
import org.eclipse.uide.parser.IASTNodeLocator;
import org.eclipse.uide.parser.IParseController;

import com.ibm.lpg.IToken;
import com.ibm.lpg.PrsStream;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */

/**
 *  @author Chris Laffra
 */
public class DefaultHoverHelper implements IHoverHelper {
    public void setLanguage(String language) {
	ErrorHandler.reportError("No Hoverhelper defined for \"" + language + "\"");
    }

    public String getHoverHelpAt(IParseController controller, int offset) {
	if (!(controller.getCurrentAst() instanceof Ast))
	    return "";
	try {
	    Ast ast= (Ast) controller.getCurrentAst();
	    IToken token= controller.getTokenAtCharacter(offset);
	    PrsStream parser= controller.getParser().getParseStream();
	    IASTNodeLocator nodeLocator= controller.getNodeLocator();
	    Ast node= nodeLocator.findNode(ast, offset);
	    if (token == null)
		return null;
	    //			Ast node = token.getAst();
	    String answer= "This is the default hover helper. Add your own using the UIDE wizard"
		    + "\nSee class 'org.eclipse.uide.defaults.DefaultContentProposer'." + "\nNow, what can I say about: "
		    + token.getValue(controller.getLexer().getLexStream().getInputChars()) + "?" + "\nIt is a token of kind "
		    + parser.orderedTerminalSymbols()[token.getKind()] + "\nAST tree: ";
	    while (node != null) {
		answer+= "> " + /* replace: controller.getString(node); by */node.getRuleName();
		node= node.parent;
	    }
	    answer+= "\nDuring parsing, " + controller.getParser().getParseStream().getSize() + " tokens were created.";
	    IToken lastErrorToken= /* controller.getLastErrorToken(); */null; /* temp patch */
	    if (lastErrorToken != null) {
		int startOffset= lastErrorToken.getStartOffset();
		int endOffset= lastErrorToken.getEndOffset();
		//				String tokenKindName = lastErrorToken.getTokenKindName();
		String value= String.copyValueOf(controller.getLexer().getLexStream().getInputChars(), startOffset, endOffset
			- startOffset);
		answer+= "\n\nSyntax error at \"" + value + "\" at offset " + startOffset;
	    }
	    return answer;
	} catch (Throwable e) {
	    ErrorHandler.reportError("Cannot get hover help...", e);
	    return "Oops: " + e;
	}
    }

}
