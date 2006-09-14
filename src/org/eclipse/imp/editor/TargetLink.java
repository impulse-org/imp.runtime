package org.eclipse.uide.editor;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

/**
 * Common class to represent a hyperlink to a given target location.
 * Currently limited to intra-file references.<br>
 * @author rfuhrer
 */
// TODO Enhance this to handle cross-file references.
public final class TargetLink implements IHyperlink {
    private final String fText;

    private final Object fTarget;

    private final int fStart;

    private final int fLength;

    private final int fTargetStart;

    private final int fTargetLength;

    private final ITextViewer fViewer;

    private final UniversalEditor fEditor;

    public TargetLink(String text, int srcStart, ITextViewer viewer, UniversalEditor editor, Object target, int srcLength, int targetStart, int targetLength) {
        super();
        fText= text;
        fStart= srcStart;
        fViewer= viewer;
	fEditor= editor;
        fTarget= target;
        fLength= srcLength;
        fTargetStart= targetStart;
        fTargetLength= targetLength;
    }

    public IRegion getHyperlinkRegion() {
        return new Region(fStart, fLength);
    }

    public String getTypeLabel() {
        return fTarget.getClass().getName();
    }

    public String getHyperlinkText() {
        return new String(fText);
    }

    public void open() {
	fEditor.selectAndReveal(fTargetStart, fTargetLength);
    }
}
