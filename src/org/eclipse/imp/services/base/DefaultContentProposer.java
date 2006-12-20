package org.eclipse.uide.defaults;
/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */


import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.editor.IContentProposer;
import org.eclipse.uide.parser.IParseController;



/**
 *  @author Chris Laffra
 */
public class DefaultContentProposer implements IContentProposer {
	
    String language; 
    
    public void setLanguage(String language) {
        ErrorHandler.reportError("No Content Proposer defined for \""+language+"\"");
        this.language = language;
    }

    public ICompletionProposal[] getContentProposals(IParseController controller, int offset, ITextViewer viewer) {
		char keywords[][] = controller.getKeywords();
        String msg = "This is the default content proposer.";
        String info = "You can add your own using the UIDE wizard.\n" +
                "See class 'org.eclipse.uide.defaults.DefaultContentProposer'.";
		ICompletionProposal[] result = new ICompletionProposal[keywords.length + 1];
        for (int n = 0; n < 1; n++) {
            result[n] = new CompletionProposal(msg, offset, 0, msg.length(),
                                null, msg, null, info);
        }
        for (int n = 0, k = 1; n < keywords.length; n++, k++) {
			String keyword = new String(keywords[n]);
			result[k] = new CompletionProposal(keyword, offset, 0, keyword.length(), 
                                null, "Insert keyword "+k+" = '"+keyword+"'", null, 
                                "That is all I can say about keyword "+keyword+". Sorry.");
		}
		return result;
	}

}

