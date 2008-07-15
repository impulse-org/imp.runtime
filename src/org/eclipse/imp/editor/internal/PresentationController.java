/*******************************************************************************
* Copyright (c) 2007 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation

*******************************************************************************/

package org.eclipse.imp.editor.internal;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Stack;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.ServiceFactory;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.preferences.PreferenceCache;
import org.eclipse.imp.services.ITokenColorer;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
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

//  private List<Annotation> annotations;

    Stack<IRegion> workItems= new Stack<IRegion>();

    public PresentationController(ISourceViewer sourceViewer, Language language) {
	this.sourceViewer= sourceViewer;
//	annotations= new ArrayList<Annotation>();
        colorer= ServiceFactory.getInstance().getTokenColorer(language);
    }

    public AnalysisRequired getAnalysisRequired() {
	return AnalysisRequired.LEXICAL_ANALYSIS;
    }

//    protected void generateErrorAnnotations(IParseController controller) {
//	List errors= controller.getErrors();
//	if (errors == null) // no errors?
//	    return;
//	IAnnotationModel annotationModel= sourceViewer.getAnnotationModel();
//	for(int n= 0; n < annotations.size(); n++) {
//	    Annotation annotation= (Annotation) annotations.get(n);
//	    annotationModel.removeAnnotation(annotation);
//	}
//	annotations.clear();
//	int max= sourceViewer.getDocument().getLength() - 1;
//	for(int n= 0; n < errors.size(); n++) {
//	    ParseError error= (ParseError) errors.get(n);
//	    if (error.token == null)
//		continue;
//	    try {
//		int offset= error.token.getStartOffset();
//		if (offset == -1)
//		    offset= max;
//		int length= Math.max(1, error.token.getEndOffset() - offset + 1);
//		Annotation annotation= new Annotation("org.eclipse.ui.workbench.texteditor.error", false, error.description);
//		annotationModel.addAnnotation(annotation, new Position(offset, length));
//		annotations.add(annotation);
//	    } catch (Exception e) {
//		ErrorHandler.reportError("Unexpected error while drawing squigglies... " + error.token.getStartOffset(), e);
//	    }
//	}
//    }

    private static final String CONSOLE_NAME= "Source Tokens";

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

    private void dumpToken(Object token, PrintStream ps) {
        // TODO Add API to ISourcePositionLocator to give info on tokens
//	mcs.print( " (" + prs.getKind(i) + ")");
//	mcs.print(" \t" + prs.getStartOffset(i));
//	mcs.print(" \t" + prs.getTokenLength(i));
//	mcs.print(" \t" + prs.getLineNumberOfTokenAt(i));
//	mcs.print(" \t" + prs.getColumnOfTokenAt(i));
	ps.print(" \t" + token);
	ps.println();
    }

    private void dumpTokens(Iterator tokenIter, PrintStream ps) {
        ps.println(" Kind \tOffset \tLen \tLine \tCol \tText");
        for(; tokenIter.hasNext(); ) {
            dumpToken(tokenIter.next(), ps);
        }
    }

    private void changeTokenPresentation(IParseController controller, TextPresentation presentation, Object token, ISourcePositionLocator locator) {
        TextAttribute attribute= colorer.getColoring(controller, token);
        
        StyleRange styleRange= new StyleRange(locator.getStartOffset(token), locator.getEndOffset(token) - locator.getStartOffset(token) + 1,
                attribute == null ? null : attribute.getForeground(),
                attribute == null ? null : attribute.getBackground(),
                attribute == null ? SWT.NORMAL : attribute.getStyle());
        
        // SMS 21 Jun 2007:  negative (possibly 0) length style ranges seem to cause problems;
        // but if you have one it should lead to an IllegalArgumentException in changeTextPresentation(..)
        if (styleRange.length <= 0 || styleRange.start + styleRange.length >= this.sourceViewer.getDocument().getLength()) {
//          System.err.println("PresentationController.changeTokenPresentation(): attempting to add style range, start =  " + styleRange.start + ", length = " + styleRange.length);
        } else {
            presentation.addStyleRange(styleRange);
        }
    }

    public void changeTextPresentation(IParseController controller, IProgressMonitor monitor, IRegion damage) {
		if (controller == null) {
		    return;
		}
		if (PreferenceCache.dumpTokens) { // RuntimePlugin.getPreferencesService().getBooleanPreference(PreferenceConstants.P_DUMP_TOKENS)
		    MessageConsole myConsole= findConsole();
		    MessageConsoleStream consStream= myConsole.newMessageStream();
	            PrintStream ps= new PrintStream(consStream);
	
		    dumpTokens(controller.getTokenIterator(damage), ps);
		}

        final TextPresentation presentation= new TextPresentation();
        ISourcePositionLocator locator= controller.getNodeLocator();

        int prevOffset= -1;
        int prevEnd= -1;
        for(Iterator iter= controller.getTokenIterator(damage); iter.hasNext() && !monitor.isCanceled(); ) {
            Object token= iter.next();
            int offset= locator.getStartOffset(token);
            int end= locator.getEndOffset(token);

            if (offset <= prevEnd && end >= prevOffset) {
            	continue;
            }
            changeTokenPresentation(controller, presentation, token, locator);
            prevOffset= offset;
            prevEnd= end;
        }
		if (!monitor.isCanceled() && !presentation.isEmpty()) {
		    Display.getDefault().asyncExec(new Runnable() {
			public void run() {
			    // SMS 21 Jun 2007 added try-catch block
			    // Note:  It doesn't work to just eat the exception here; if there is a problematic token
			    // then an exception is likely to arise downstream in the computation anyway
			    try {
			    	// SMS 04 Dec 2007:
			    	if (sourceViewer != null)
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
//	    // TODO This should be separated out to another service, since it requires deeper analysis than lexical analysis...
//	    generateErrorAnnotations(controller);
	}
    }

    /**
     * Repair the damaged area
     * @param offset the start of the damaged area
     * @param length the length of the damaged area
     */
    public void damage(IRegion region) {
        if (colorer == null)
            return;
        IRegion bigRegion= colorer.calculateDamageExtent(region);
	workItems.push(bigRegion);
    }
}
