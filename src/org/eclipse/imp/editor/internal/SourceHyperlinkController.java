/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
/*
 * Created on Feb 8, 2006
 */
package org.eclipse.imp.editor.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.ISourceHyperlinkDetector;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

public class SourceHyperlinkController implements IHyperlinkDetector, IModelListener {
    private final ISourceHyperlinkDetector fSourceHyperlinkDetector;
    private IParseController fParseController;
    private final UniversalEditor fEditor;

    public SourceHyperlinkController(ISourceHyperlinkDetector sourceHyperlinkDetector, UniversalEditor editor) {
	fSourceHyperlinkDetector= sourceHyperlinkDetector;
	fEditor= editor;
    }

    public AnalysisRequired getAnalysisRequired() {
        return AnalysisRequired.NAME_ANALYSIS;
    }

    public IHyperlink[] detectHyperlinks(final ITextViewer textViewer, final IRegion region, boolean canShowMultipleHyperlinks) {
	return fSourceHyperlinkDetector.detectHyperlinks(region, fEditor, textViewer, fParseController);
    }

    public void update(IParseController parseController, IProgressMonitor monitor) {
	fParseController= parseController;
    }
}
