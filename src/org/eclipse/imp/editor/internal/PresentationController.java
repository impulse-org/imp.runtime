package org.eclipse.uide.internal.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import lpg.runtime.ErrorToken;
import lpg.runtime.IToken;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.core.ILanguageService;
import org.eclipse.uide.core.Language;
import org.eclipse.uide.editor.ITokenColorer;
import org.eclipse.uide.parser.IModelListener;
import org.eclipse.uide.parser.IParseController;
import org.eclipse.uide.parser.ParseError;
import org.eclipse.uide.utils.ExtensionPointFactory;

/**
 * @author Claffra
 * @author rfuhrer@watson.ibm.com
 */
public class PresentationController implements IModelListener {
    private ISourceViewer sourceViewer;

    private ITokenColorer colorer;

    private List annotations;

    public PresentationController(ISourceViewer sourceViewer) {
	this.sourceViewer= sourceViewer;
	annotations= new ArrayList();
    }

    public AnalysisRequired getAnalysisRequired() {
	return AnalysisRequired.LEXICAL_ANALYSIS;
    }

    public void setLanguage(Language language) {
	colorer= (ITokenColorer) ExtensionPointFactory.createExtensionPoint(language, ILanguageService.TOKEN_COLORER_SERVICE);
    }

    protected void generateErrorAnnotations(IParseController controller) {
	List errors= controller.getErrors();
	if (errors == null) // no errors?
	    return;
	IAnnotationModel annotationModel= sourceViewer.getAnnotationModel();
	for(int n= 0; n < annotations.size(); n++) {
	    Annotation annotation= (Annotation) annotations.get(n);
	    annotationModel.removeAnnotation(annotation);
	}
	annotations.clear();
	int max= sourceViewer.getDocument().getLength() - 1;
	for(int n= 0; n < errors.size(); n++) {
	    ParseError error= (ParseError) errors.get(n);
	    if (error.token == null)
		continue;
	    try {
		int offset= error.token.getStartOffset();
		if (offset == -1)
		    offset= max;
		int length= Math.max(1, error.token.getEndOffset() - offset + 1);
		Annotation annotation= new Annotation("org.eclipse.ui.workbench.texteditor.error", false, error.description);
		annotationModel.addAnnotation(annotation, new Position(offset, length));
		annotations.add(annotation);
	    } catch (Exception e) {
		ErrorHandler.reportError("Unexpected error while drawing squigglies... " + error.token.getStartOffset(), e);
	    }
	}
    }

    public void changeTextPresentation(IParseController controller, IProgressMonitor monitor, Region damage) {
	if (controller == null) {
	    return;
	}
	int startIndex= controller.getTokenIndexAtCharacter(damage.getOffset());
	int endIndex= controller.getTokenIndexAtCharacter(damage.getOffset() + damage.getLength());
	final TextPresentation presentation= new TextPresentation();
//	controller.getParser().getParseStream().dumpTokens();
	
	for(int n= startIndex; !monitor.isCanceled() && n <= endIndex; n++) {
	    IToken token= controller.getParser().getParseStream().getTokenAt(n);
	    
	    // SMS 09 Jul 2007  Adding in an attempt to preclude exceptions
	    // arising from negative style ranges (as per discussion in
	    // group meeting of 06 Jul 2007)
	    // SMS 16 Jul 2007  Added "=" to condition; it turns out that there may
	    // be many kinds of tokens with equal start and end offsets, and these
	    // will be given a length of 1 in the style range, which will create an
	    // overlap with the following range, which will trigger an
	    // IllegalArgumentException in org.eclipse.swt.custom.StyledText.
	    // This exception many only show up in Eclipse 3.2.2 (i.e., not in
	    // Eclipse 3.1), but the change is probably appropriate independent
	    // of Eclipse version.
	    if (token.getEndOffset() <= token.getStartOffset()) {
	    	// SMS 16 Jul 2007  Commenting out this reporting because there may be
	    	// many token kinds with equal start and end offsets, such as control
	    	// characters and blanks.
//	    	if (!(token instanceof ErrorToken))
//	    		ErrorHandler.logMessage(
//	    			"PresentationController.changeTextPresentation:  Encountered token other than ErrorToken with endOffset <= startOffset;\n" +
//	    			"\ttoken number = " + n + "; start = " + token.getStartOffset() + "; end = " + token.getEndOffset() + "; tokenKind = " + token.getKind(), null);	
	    	continue;
	    }

	    

	    if (token.getKind() != controller.getParser().getEOFTokenKind()) {
			changeTokenPresentation(controller, presentation, token);
			IToken adjuncts[]= controller.getParser().getParseStream().getFollowingAdjuncts(n);
			for(int i= 0; i < adjuncts.length; i++)
			    changeTokenPresentation(controller, presentation, adjuncts[i]);
	    }
	}
	if (!monitor.isCanceled()) {
	    Display.getDefault().asyncExec(new Runnable() {
		public void run() {
			// SMS 21 Jun 2007 added try-catch block
			// Note:  It doesn't work to just eat the exception here; if there is a problematic token
			// then an exception is likely to arise downstream in the computation anyway
			try {
				sourceViewer.changeTextPresentation(presentation, true);	
			} catch (IllegalArgumentException e) {
				// One possible cause is a negative length in a styleRange in the presentation
				ErrorHandler.logError("PresentationController.changeTextPresentation:  Caught IllegalArgumentException; rethrowing", e);
				throw e;
			}
		}
	    });
	}
    }

    private TextAttribute getColoring(IParseController controller, IToken token) {
	if (colorer != null)
	    return colorer.getColoring(controller, token);
	return null;
    }

    private void changeTokenPresentation(IParseController controller, TextPresentation presentation, IToken token) {
	TextAttribute attribute= getColoring(controller, token);
	StyleRange styleRange= new StyleRange(token.getStartOffset(), token.getEndOffset() - token.getStartOffset() + 1,
		attribute == null ? null : attribute.getForeground(), attribute == null ? null : attribute.getBackground(),
		attribute == null ? SWT.NORMAL : attribute.getStyle());
	
	// SMS 21 Jun 2007:  negative (possibly 0) length style ranges seem to cause problems;
	// but if you have one it should lead to an IllegalArgumentException in changeTextPresentation(..)
	if (styleRange.length <= 0) {
		System.err.println("PresentationController.changeTokenPresentation():  adding style range, start =  " + styleRange.start + ", length = " + styleRange.length);
	}
	
	presentation.addStyleRange(styleRange);
    }

    public void update(IParseController controller, IProgressMonitor monitor) {
	if (!monitor.isCanceled()) {
	    synchronized (workItems) {
		for(int n= workItems.size() - 1; !monitor.isCanceled() && n >= 0; n--) {
		    Region damage= (Region) workItems.get(n);
		    changeTextPresentation(controller, monitor, damage);
		}
		if (!monitor.isCanceled())
		    workItems.removeAllElements();
	    }
	    // TODO This should be separated out to another service, since it requires deeper analysis than lexical analysis...
	    generateErrorAnnotations(controller);
	}
    }

    Stack workItems= new Stack();

    /**
     * Repair the damaged area
     * @param offset the start of the damaged area
     * @param length the length of the damaged area
     */
    public void damage(int offset, int length) {
	workItems.push(new Region(offset, length));
    }
}
