package org.eclipse.imp.editor.internal;

import java.util.Iterator;

import lpg.runtime.IMessageHandler;
import lpg.runtime.ParseErrorCodes;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.ITextEditor;

public class AnnotationCreator implements IMessageHandler {
    private final ITextEditor fEditor;
    private final String fAnnotationType;

    public AnnotationCreator(ITextEditor textEditor, String annotationType) {
        fEditor= textEditor;
        if (annotationType == null)
        	fAnnotationType = UniversalEditor.PARSE_ANNOTATION_TYPE;
        else 
        	fAnnotationType= annotationType;
    }
    public void handleMessage(int errorCode, int [] msgLocation, int[] errorLocation, String filename, String [] errorInfo) {
        int offset = msgLocation[IMessageHandler.OFFSET_INDEX],
            length = msgLocation[IMessageHandler.LENGTH_INDEX];
        String message = "";
        if (errorCode != ParseErrorCodes.DELETION_CODE &&
            errorCode != ParseErrorCodes.MISPLACED_CODE)
        {        
            for (int i = 0; i < errorInfo.length; i++)
                message += errorInfo[i] + " ";
        }
        message += ParseErrorCodes.errorMsgText[errorCode];

        IAnnotationModel model= fEditor.getDocumentProvider().getAnnotationModel(fEditor.getEditorInput());
        Annotation annotation= new Annotation(fAnnotationType, false, message);
        Position pos= new Position(offset, length);

        model.addAnnotation(annotation, pos);
    }

    public void removeAnnotations() {
	IAnnotationModel model= fEditor.getDocumentProvider().getAnnotationModel(fEditor.getEditorInput());

	if (model == null)
	    return;

	for(Iterator i= model.getAnnotationIterator(); i.hasNext(); ) {
	    Annotation a= (Annotation) i.next();

	    if (a.getType().equals(fAnnotationType))
		model.removeAnnotation(a);
	}
    }
}
