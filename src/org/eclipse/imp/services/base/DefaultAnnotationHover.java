/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.services.base;

import java.util.List;

import org.eclipse.imp.utils.AnnotationUtils;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;

public class DefaultAnnotationHover implements IAnnotationHover {
    /**
     * @see IVerticalRulerHover#getHoverInfo(ISourceViewer, int)
     */
    public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
	List annotations= AnnotationUtils.getAnnotationsForLine(sourceViewer, lineNumber);

	return AnnotationUtils.formatAnnotationList(annotations);
    }
}
