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

import java.util.Iterator;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
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

    public void startMessageGroup(String groupName) { }
    public void endMessageGroup() { }

    public void handleSimpleMessage(String message, int startOffset, int endOffset,
            int startCol, int endCol,
            int startLine, int endLine) {
        
//    public void handleMessage(int errorCode, int [] msgLocation, int[] errorLocation, String filename, String [] errorInfo) {
//        int offset = msgLocation[IMessageHandler.OFFSET_INDEX],
//            length = msgLocation[IMessageHandler.LENGTH_INDEX];
//        String message = "";
//        if (errorCode != ParseErrorCodes.DELETION_CODE &&
//            errorCode != ParseErrorCodes.MISPLACED_CODE)
//        {        
//            for (int i = 0; i < errorInfo.length; i++)
//                message += errorInfo[i] + " ";
//        }
//        message += ParseErrorCodes.errorMsgText[errorCode];

        IAnnotationModel model= fEditor.getDocumentProvider().getAnnotationModel(fEditor.getEditorInput());
        Annotation annotation= new Annotation(fAnnotationType, false, message);
        
        Position pos= new Position(startOffset, endOffset - startOffset + 1);

        model.addAnnotation(annotation, pos);
    }

    public void removeAnnotations() {
        final IDocumentProvider docProvider= fEditor.getDocumentProvider();

        if (docProvider == null) {
            return;
        }

        IAnnotationModel model= docProvider.getAnnotationModel(fEditor.getEditorInput());

        if (model == null)
            return;

        for(Iterator i= model.getAnnotationIterator(); i.hasNext();) {
            Annotation a= (Annotation) i.next();

            if (a.getType().equals(fAnnotationType))
                model.removeAnnotation(a);
        }
    }

    public void clearMessages() {
        removeAnnotations();
    }
}
