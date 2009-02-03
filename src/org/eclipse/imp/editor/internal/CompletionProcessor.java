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

package org.eclipse.imp.editor.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.ServiceFactory;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IContentProposer;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class CompletionProcessor implements IContentAssistProcessor, IModelListener {
    private final IContextInformation[] NO_CONTEXTS= new IContextInformation[0];

    private ICompletionProposal[] NO_COMPLETIONS= new ICompletionProposal[0];

    private IParseController parseController;

    private final Language fLanguage;

    private IContentProposer contentProposer;

    // private HippieProposalProcessor hippieProcessor= new HippieProposalProcessor();

    public CompletionProcessor(Language language) {
        contentProposer= ServiceFactory.getInstance().getContentProposer(language);
        fLanguage= language;
    }

    public AnalysisRequired getAnalysisRequired() {
        return AnalysisRequired.LEXICAL_ANALYSIS;
    }

    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        try {
            if (parseController != null && contentProposer != null) {
                return contentProposer.getContentProposals(parseController, offset, viewer);
            }
            // TODO Once we move to 3.2, delegate to the HippieProposalProcessor
            // return hippieProcessor.computeCompletionProposals(viewer, offset);
        } catch (Throwable e) {
            ErrorHandler.reportError("Exception caught from language-specific content proposer implementation", e);
        }
        return NO_COMPLETIONS;
    }

    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        return NO_CONTEXTS;
    }

    public char[] getCompletionProposalAutoActivationCharacters() {
        return null;
    }

    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }

    public String getErrorMessage() {
        return null;
    }

    public void update(IParseController parseController, IProgressMonitor monitor) {
        this.parseController= parseController;
    }

    public String toString() {
        return "Completion processor for " + fLanguage.getName() + " on " + parseController.getPath().toPortableString();
    }
}
