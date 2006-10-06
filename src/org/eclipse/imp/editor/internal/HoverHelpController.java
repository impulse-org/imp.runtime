/**
 * 
 */
package org.eclipse.uide.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.core.Language;
import org.eclipse.uide.parser.IModelListener;
import org.eclipse.uide.parser.IParseController;
import org.eclipse.uide.runtime.RuntimePlugin;
import org.eclipse.uide.utils.AnnotationUtils;
import org.eclipse.uide.utils.ExtensionPointFactory;

class HoverHelpController implements ITextHover, IModelListener {
    private IParseController controller;

    private IHoverHelper hoverHelper;

    public AnalysisRequired getAnalysisRequired() {
	return AnalysisRequired.NAME_ANALYSIS;
    }

    public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
        return new Region(offset, 0);
    }

    public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
        try {
    	final int offset= hoverRegion.getOffset();
    	String help= null;

    	if (controller != null && hoverHelper != null)
    	    help= hoverHelper.getHoverHelpAt(controller, (ISourceViewer) textViewer, offset);
    	if (help == null)
    	    help= AnnotationUtils.formatAnnotationList(AnnotationUtils.getAnnotationsForOffset((ISourceViewer) textViewer, offset));

    	return help;
        } catch (Throwable e) {
    	ErrorHandler.reportError("Universal Editor Error", e);
        }
        return null;
    }

    public void update(IParseController controller, IProgressMonitor monitor) {
        this.controller= controller;
    }

    public void setLanguage(Language language) {
        hoverHelper= (IHoverHelper) ExtensionPointFactory.createExtensionPoint(language, RuntimePlugin.UIDE_RUNTIME,
    	    "hoverHelper");
    }
}
