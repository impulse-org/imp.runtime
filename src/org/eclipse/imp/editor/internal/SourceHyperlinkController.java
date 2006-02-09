/*
 * Created on Feb 8, 2006
 */
package org.eclipse.uide.internal.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.uide.editor.ISourceHyperlinkDetector;
import org.eclipse.uide.editor.UniversalEditor;
import org.eclipse.uide.parser.IModelListener;
import org.eclipse.uide.parser.IParseController;

public class SourceHyperlinkController implements IHyperlinkDetector, IModelListener {
    private final ISourceHyperlinkDetector fSourceHyperlinkDetector;
    private IParseController fParseController;

    public SourceHyperlinkController(ISourceHyperlinkDetector sourceHyperlinkDetector) {
	fSourceHyperlinkDetector= sourceHyperlinkDetector;
    }

    public IHyperlink[] detectHyperlinks(final ITextViewer textViewer, final IRegion region, boolean canShowMultipleHyperlinks) {
	return fSourceHyperlinkDetector.detectHyperlinks(textViewer, region, fParseController);
    }

    public void update(IParseController parseController, IProgressMonitor monitor) {
	fParseController= parseController;
    }
}
