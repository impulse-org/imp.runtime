/*
 * Created on Feb 8, 2006
 */
package org.eclipse.uide.internal.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.uide.editor.ISourceHyperlinkDetector;
import org.eclipse.uide.editor.UniversalEditor;
import org.eclipse.uide.parser.IModelListener;
import org.eclipse.uide.parser.IParseController;

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
