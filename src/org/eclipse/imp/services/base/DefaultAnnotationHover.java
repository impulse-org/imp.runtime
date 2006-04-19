package org.eclipse.uide.defaults;

import java.util.List;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.uide.utils.AnnotationUtils;

public class DefaultAnnotationHover implements IAnnotationHover {
    /**
     * @see IVerticalRulerHover#getHoverInfo(ISourceViewer, int)
     */
    public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
	List annotations= AnnotationUtils.getAnnotationsForLine(sourceViewer, lineNumber);

	return AnnotationUtils.formatAnnotationList(annotations);
    }
}
