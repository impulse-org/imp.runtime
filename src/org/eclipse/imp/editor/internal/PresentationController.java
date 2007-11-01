/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.editor.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import lpg.runtime.IPrsStream;
import lpg.runtime.IToken;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ParseError;
import org.eclipse.imp.preferences.PreferenceCache;
import org.eclipse.imp.services.ITokenColorer;
import org.eclipse.imp.utils.ExtensionPointFactory;
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
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;


/**
 * @author Claffra
 * @author rfuhrer@watson.ibm.com
 */
public class PresentationController implements IModelListener {
    private ISourceViewer sourceViewer;

    private ITokenColorer colorer;

    private List<Annotation> annotations;

    public PresentationController(ISourceViewer sourceViewer) {
	this.sourceViewer= sourceViewer;
	annotations= new ArrayList<Annotation>();
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

    private static final String CONSOLE_NAME= "Source Statistics";

    private MessageConsole findConsole() {
        MessageConsole myConsole= null;
        final IConsoleManager consoleManager= ConsolePlugin.getDefault().getConsoleManager();
        IConsole[] consoles= consoleManager.getConsoles();
        for(int i= 0; i < consoles.length; i++) {
            IConsole console= consoles[i];
            if (console.getName().equals(CONSOLE_NAME))
                myConsole= (MessageConsole) console;
        }
        if (myConsole == null) {
            myConsole= new MessageConsole(CONSOLE_NAME, null);
            consoleManager.addConsoles(new IConsole[] { myConsole });
        }
        consoleManager.showConsoleView(myConsole);
        return myConsole;
    }

    private void dumpToken(IPrsStream prs, int i, MessageConsoleStream mcs) {
	mcs.print( " (" + prs.getKind(i) + ")");
	mcs.print(" \t" + prs.getStartOffset(i));
	mcs.print(" \t" + prs.getTokenLength(i));
	mcs.print(" \t" + prs.getLineNumberOfTokenAt(i));
	mcs.print(" \t" + prs.getColumnOfTokenAt(i));
	mcs.print(" \t" + prs.getTokenText(i));
	mcs.println();
    }

    private void dumpTokens(IPrsStream prs, MessageConsoleStream mcs) {
	if (prs.getSize() > 2) {
	    mcs.println(" Kind \tOffset \tLen \tLine \tCol \tText");
	    for(int i = 1; i < prs.getSize() - 1; i++)
		dumpToken(prs, i, mcs);
	}
    }

    public void changeTextPresentation(IParseController controller, IProgressMonitor monitor, Region damage) {
	if (controller == null) {
	    return;
	}
	if (PreferenceCache.dumpTokens) {
	    MessageConsole myConsole= findConsole();
	    MessageConsoleStream consStream= myConsole.newMessageStream();

	    dumpTokens(controller.getParser().getParseStream(), consStream);
	}

	int startIndex= controller.getTokenIndexAtCharacter(damage.getOffset());
	int endIndex= controller.getTokenIndexAtCharacter(damage.getOffset() + damage.getLength());
	final TextPresentation presentation= new TextPresentation();
//	controller.getParser().getParseStream().dumpTokens();
	
	for(int n= startIndex; !monitor.isCanceled() && n <= endIndex; n++) {
	    IToken token= controller.getParser().getParseStream().getTokenAt(n);

	    //if (token.getEndOffset() < token.getStartOffset()) continue;
	    
	    // SMS 18 Jul 2007  Added condition below to process tokens only if they have
	    // a positive range.  Tokens with a negative length can lead to overlapping
	    // offsets.  Tokens with 0 length can lead to overlapping offsets, as well.
	    // In such cases two tokens can start at the same offset, which may make some
	    // sense if you think of the first token as having length 0.  But below we
	    // give the 0-length tokens a length of 1, without bumping up the start of
	    // the following token.  So we create an overlap in that way.  Overlapping
	    // text ranges lead to IllegalArgumentExceptions when processing StyleRanges
	    // or related types (e.g., StyledText).  Some of the cases in which the
	    // exception gets thrown may be new in Eclipse 3.2, but the modifications
	    // made to address them are probably appropriate independent of Eclipse version.
	    //
	    // PC 18 Sep 2007. Changed condition described above to also process tokens of
	    // length 1 (startOffset == endOffset). Otherwise, such tokens are not colored
	    // properly.
	    if (token.getKind() != controller.getParser().getEOFTokenKind()) {
	    	if (token.getEndOffset() >= token.getStartOffset()) {
	    		changeTokenPresentation(controller, presentation, token);
	    	}

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

    // TODO This method is unnecessary - shouldn't even get this far if there's no colorer implementation!
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
