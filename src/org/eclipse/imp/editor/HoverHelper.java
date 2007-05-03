/*
 * Created on Feb 9, 2006
 */
package org.eclipse.uide.editor;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.uide.core.IDocumentationProvider;
import org.eclipse.uide.core.ILanguageService;
import org.eclipse.uide.core.Language;
import org.eclipse.uide.core.LanguageRegistry;
import org.eclipse.uide.editor.IHoverHelper;
import org.eclipse.uide.editor.IReferenceResolver;
import org.eclipse.uide.parser.IASTNodeLocator;
import org.eclipse.uide.parser.IParseController;
import org.eclipse.uide.runtime.RuntimePlugin;
import org.eclipse.uide.utils.ExtensionPointFactory;
import org.eclipse.uide.utils.HTMLPrinter;

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

    	IReferenceResolver refResolver = (IReferenceResolver) ExtensionPointFactory.createExtensionPoint(fLanguage, ILanguageService.REFERENCE_RESOLVER_SERVICE);
        Object root= parseController.getCurrentAst();
        IASTNodeLocator nodeLocator = parseController.getNodeLocator();

        if (root == null) return null;

        Object selNode = nodeLocator.findNode(root, offset);

        if (selNode == null) return null;

       	Object target = (refResolver != null) ? refResolver.getLinkTarget(selNode, parseController) : selNode;

       	if (target == null) return null;

       	IDocumentationProvider docProvider= (IDocumentationProvider) ExtensionPointFactory.createExtensionPoint(fLanguage, ILanguageService.DOCUMENTATION_PROVIDER_SERVICE);
       	String doc= (docProvider != null) ? docProvider.getDocumentation(target, parseController) : null;

       	return doc;
//       	StringBuffer buffer= new StringBuffer();
//
//	HTMLPrinter.addSmallHeader(buffer, target.toString());
//
//	if (doc != null)
//       	    HTMLPrinter.addParagraph(buffer, doc);
//
//       	return buffer.toString();
    }
}
