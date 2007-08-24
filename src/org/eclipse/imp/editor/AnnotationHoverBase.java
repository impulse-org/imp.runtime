/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
/*
 * Created on Oct 27, 2006
 */
package org.eclipse.imp.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.utils.HTMLPrinter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.projection.AnnotationBag;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;

public class AnnotationHoverBase implements IAnnotationHover, ILanguageService {

    public AnnotationHoverBase() {
    }

    protected static boolean isRulerLine(Position position, IDocument document, int line) {
        if (position.getOffset() > -1 && position.getLength() > -1) {
            try {
        	// RMF 11/10/2006 - This used to add 1 to the line computed by the document,
        	// which appears to be bogus. First, it didn't work right (annotation hovers
        	// never appeared); second, the line passed in comes from the Eclipse
        	// framework, so it should be consistent (wrt the index base) with what the
        	// IDocument API provides.
        	return line == document.getLineOfOffset(position.getOffset())/* + 1*/;
            } catch (BadLocationException x) {
            }
        }
        return false;
    }

    protected static IAnnotationModel getAnnotationModel(ISourceViewer viewer) {
        if (viewer instanceof ISourceViewerExtension2) {
            ISourceViewerExtension2 extension= (ISourceViewerExtension2) viewer;
            return extension.getVisualAnnotationModel();
        }
        return viewer.getAnnotationModel();
    }

    public static List getSourceAnnotationsForLine(ISourceViewer viewer, int line) {
        IAnnotationModel model= getAnnotationModel(viewer);
        if (model == null)
            return null;

        IDocument document= viewer.getDocument();
        List/*<Annotation>*/javaAnnotations= new ArrayList();
        HashMap messagesAtPosition= new HashMap();
        Iterator iterator= model.getAnnotationIterator();
    
        while (iterator.hasNext()) {
            Annotation annotation= (Annotation) iterator.next();
            Position position= model.getPosition(annotation);
    
            if (position == null)
        	continue;
    
            if (!isRulerLine(position, document, line))
        	continue;

            if (annotation instanceof AnnotationBag) {
        	AnnotationBag bag= (AnnotationBag) annotation;
        	Iterator e= bag.iterator();
        	while (e.hasNext()) {
        	    annotation= (Annotation) e.next();
        	    position= model.getPosition(annotation);
        	    if (position != null && includeAnnotation(annotation, position, messagesAtPosition))
        		javaAnnotations.add(annotation);
        	}
        	continue;
            }
    
            if (includeAnnotation(annotation, position, messagesAtPosition))
        	javaAnnotations.add(annotation);
        }
    
        return javaAnnotations;
    }

    /**
     * Check preferences, etc., to determine whether this annotation is actually showing.
     * (Don't want to show a hover for a non-visible annotation.)
     * @param annotation
     * @param position
     * @param messagesAtPosition
     * @return
     */
    private static boolean includeAnnotation(Annotation annotation, Position position, HashMap messagesAtPosition) {
        return !(annotation instanceof ProjectionAnnotation) && !annotation.getType().equals("org.eclipse.ui.workbench.texteditor.quickdiffUnchanged");
    }

    public static String formatAnnotationList(List javaAnnotations) {
        if (javaAnnotations != null) {
            if (javaAnnotations.size() == 1) {
        	// optimization
        	Annotation annotation= (Annotation) javaAnnotations.get(0);
        	String message= annotation.getText();
    
        	if (message != null && message.trim().length() > 0)
        	    return formatSingleMessage(message);
            } else {
        	List messages= new ArrayList();
        	Iterator e= javaAnnotations.iterator();
    
        	while (e.hasNext()) {
        	    Annotation annotation= (Annotation) e.next();
        	    String message= annotation.getText();
        	    if (message != null && message.trim().length() > 0)
        		messages.add(message.trim());
        	}
    
        	if (messages.size() == 1)
        	    return formatSingleMessage((String) messages.get(0));
    
        	if (messages.size() > 1)
        	    return formatMultipleMessages(messages);
            }
        }
        return null;
    }

    /**
     * Formats a message as HTML text.
     */
    public static String formatSingleMessage(String message) {
        if (true) // until we hook in the HTML-enabled hover viewer
            return message;
        StringBuffer buffer= new StringBuffer();
        HTMLPrinter.addPageProlog(buffer);
        HTMLPrinter.addParagraph(buffer, HTMLPrinter.convertToHTMLContent(message));
        HTMLPrinter.addPageEpilog(buffer);
        return buffer.toString();
    }

    /**
     * Formats several messages as HTML text.
     */
    public static String formatMultipleMessages(List messages) {
        if (true) { // until we hook in the HTML-enabled hover viewer
            StringBuffer buff= new StringBuffer();
    
            buff.append("Multiple messages:\n");
            for(Iterator iter= messages.iterator(); iter.hasNext();) {
        	String msg= (String) iter.next();
        	buff.append("  ");
        	buff.append(msg);
        	if (iter.hasNext())
        	    buff.append('\n');
            }
            return buff.toString();
        }
        StringBuffer buffer= new StringBuffer();
        HTMLPrinter.addPageProlog(buffer);
        HTMLPrinter.addParagraph(buffer, HTMLPrinter.convertToHTMLContent("Multiple messages at this line"));
    
        HTMLPrinter.startBulletList(buffer);
        Iterator e= messages.iterator();
        while (e.hasNext())
            HTMLPrinter.addBullet(buffer, HTMLPrinter.convertToHTMLContent((String) e.next()));
        HTMLPrinter.endBulletList(buffer);
    
        HTMLPrinter.addPageEpilog(buffer);
        return buffer.toString();
    }

    /**
     * @see IVerticalRulerHover#getHoverInfo(ISourceViewer, int)
     */
    public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
	List javaAnnotations= getSourceAnnotationsForLine(sourceViewer, lineNumber);

	return formatAnnotationList(javaAnnotations);
    }
}
