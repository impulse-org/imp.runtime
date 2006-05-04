package org.eclipse.uide.internal.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
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
import org.eclipse.uide.core.Language;
import org.eclipse.uide.editor.ITokenColorer;
import org.eclipse.uide.parser.IModelListener;
import org.eclipse.uide.parser.IParseController;
import org.eclipse.uide.parser.ParseError;
import org.eclipse.uide.runtime.RuntimePlugin;
import org.eclipse.uide.utils.ExtensionPointFactory;
import lpg.lpgjavaruntime.IToken;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

/**
 * @author Claffra
 *
 * TODO add documentation
 */
public class PresentationController implements IModelListener {
    //  private ArrayList squigglies;
    private ISourceViewer sourceViewer;

    //  private IParser parser;
    private ITokenColorer colorer;

    private List annotations;

    public PresentationController(ISourceViewer sourceViewer) {
	this.sourceViewer= sourceViewer;
	annotations= new ArrayList();
    }

    public void setLanguage(Language language) {
	colorer= (ITokenColorer) ExtensionPointFactory.createExtensionPoint(language, RuntimePlugin.UIDE_RUNTIME,
		"tokenColorer");
	//		parser = (IParser) ExtensionPointFactory.createExtensionPoint(language, "org.eclipse.uide", "parser");
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
	for(int n= startIndex; !monitor.isCanceled() && n <= endIndex; n++) {
	    IToken token= controller.getParser().getParseStream().getTokenAt(n);
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
		    sourceViewer.changeTextPresentation(presentation, true);
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
