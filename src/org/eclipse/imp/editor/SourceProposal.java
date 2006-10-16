/**
 * 
 */
package org.eclipse.uide.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class SourceProposal implements ICompletionProposal {
	private final String fName;

	private final String fProposal;

    private final String fPrefix;

    private final int fOffset;

    public SourceProposal(String name, String prefix, int offset) {
        super();
        fProposal= name;
        fName= name;
        fPrefix= prefix;
        fOffset= offset;
    }

    public SourceProposal(String proposal, String name, String prefix, int offset) {
        super();
        fProposal= proposal;
        fName= name;
        fPrefix= prefix;
        fOffset= offset;
    }

    public void apply(IDocument document) {
        try {
            document.replace(fOffset, 0, fName.substring(fPrefix.length()));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public Point getSelection(IDocument document) {
        int newOffset= fOffset + fName.length() - fPrefix.length();
        return new Point(newOffset, 0);
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