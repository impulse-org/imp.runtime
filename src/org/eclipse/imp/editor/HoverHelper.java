/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
/*
 * Created on Feb 9, 2006
 */
package org.eclipse.imp.editor;

import java.util.List;

import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.ServiceFactory;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.services.IDocumentationProvider;
import org.eclipse.imp.services.IHoverHelper;
import org.eclipse.imp.services.IReferenceResolver;
import org.eclipse.imp.utils.HTMLPrinter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Helper class for implementing "hover help" which encapsulates the process of locating
 * the AST node under the cursor, and asking the documentation provider for the relevant
 * information to show in the hover.<br>
 * No longer an extension point itself, this class is instantiated directly by the
 * HoverHelpController and initialized with a Language so that it can instantiate the
 * correct documentation provider.
 * @author rfuhrer
 * TODO Fold this functionality into HoverHelpController?
 */
public class HoverHelper implements IHoverHelper {
    private final Language fLanguage;

    public HoverHelper(Language lang) {
	fLanguage= lang;
    }

    public String getHoverHelpAt(IParseController parseController, ISourceViewer srcViewer, int offset) {
		try {
		    List/*<Annotation>*/ annotations= AnnotationHoverBase.getSourceAnnotationsForLine(srcViewer, srcViewer.getDocument().getLineOfOffset(offset));
	
		    if (annotations != null && annotations.size() > 0)
			return AnnotationHoverBase.formatAnnotationList(annotations);
		} catch (BadLocationException e) {
		    return "???";
		}

    	IReferenceResolver refResolver = ServiceFactory.getInstance().getReferenceResolver(fLanguage);
        Object root= parseController.getCurrentAst();
        ISourcePositionLocator nodeLocator = parseController.getNodeLocator();

        if (root == null) return null;

        Object selNode = nodeLocator.findNode(root, offset);

        if (selNode == null) return null;

       	Object target = (refResolver != null) ? refResolver.getLinkTarget(selNode, parseController) : selNode;

       	if (target == null) return null;

       	IDocumentationProvider docProvider= ServiceFactory.getInstance().getDocumentationProvider(fLanguage);
       	String doc= (docProvider != null) ? docProvider.getDocumentation(target, parseController) : null;			

       	if (doc != null)
       		return doc;

       	StringBuffer buffer= new StringBuffer();

       	HTMLPrinter.addSmallHeader(buffer, target.toString());
       	HTMLPrinter.addParagraph(buffer, doc);
       	return buffer.toString();
    }
}
