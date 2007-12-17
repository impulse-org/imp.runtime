/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
/**
 * 
 */
package org.eclipse.imp.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class SourceProposal implements ICompletionProposal {
    /**
     * The text shown to the user in the popup view
     */
    private final String fProposal;

    /**
     * The new text being added/substituted if the user accepts this proposal
     */
    private final String fNewText;

    /**
     * The prefix being completed.
     */
    private final String fPrefix;

    /**
     * The offset into the text of the text being replaced.
     */
    private final int fOffset;

    /**
     * The offset at which the insertion point should be placed after completing
     * using this proposal
     */
    private final int fCursorLoc;

    /**
     * Create a new completion proposal.
     * @param newText the actual replacement text for this proposal
     * @param prefix the prefix being completed
     * @param offset the starting character offset of the text to be replaced
     */
    public SourceProposal(String newText, String prefix, int offset) {
        this(newText, newText, prefix, offset);
    }

    /**
     * Create a new completion proposal.
     * @param proposal the text to be shown in the popup view listing the proposals
     * @param newText the actual replacement text for this proposal
     * @param prefix the prefix being completed
     * @param offset the starting character offset of the text to be replaced
     */
    public SourceProposal(String proposal, String newText, String prefix, int offset) {
        this(proposal, newText, prefix, offset, offset + newText.length() - prefix.length());
    }

    /**
     * Create a new completion proposal.
     * @param proposal the text to be shown in the popup view listing the proposals
     * @param newText the actual replacement text for this proposal
     * @param prefix the prefix being completed
     * @param offset the starting character offset of the text to be replaced
     * @param cursorLoc the point at which to place the cursor after the replacement
     */
    public SourceProposal(String proposal, String newText, String prefix, int offset, int cursorLoc) {
        fProposal= proposal;
        fNewText= newText;
        fPrefix= prefix;
        fOffset= offset;
        fCursorLoc= cursorLoc;
    }

    public void apply(IDocument document) {
        try {
            document.replace(fOffset, 0, fNewText.substring(fPrefix.length()));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public Point getSelection(IDocument document) {
        return new Point(fCursorLoc, 0);
    }

    public String getAdditionalProposalInfo() {
        return null;
    }

    public String getDisplayString() {
        return fProposal;
    }

    public Image getImage() {
        return null;
    }

    public IContextInformation getContextInformation() {
        return null;
    }
}
