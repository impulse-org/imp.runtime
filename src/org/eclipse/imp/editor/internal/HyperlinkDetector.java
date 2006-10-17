package org.eclipse.uide.internal.editor;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.uide.core.ILanguageService;
import org.eclipse.uide.core.Language;
import org.eclipse.uide.editor.IReferenceResolver;
import org.eclipse.uide.editor.ISourceHyperlinkDetector;
import org.eclipse.uide.editor.TargetLink;
import org.eclipse.uide.editor.UniversalEditor;
import org.eclipse.uide.parser.IASTNodeLocator;
import org.eclipse.uide.parser.IParseController;
import org.eclipse.uide.runtime.RuntimePlugin;
import org.eclipse.uide.utils.ExtensionPointFactory;

/**
 * Provides a method to detect hyperlinks originating from a
 * given region in the parse stream of a given parse controller.
 */
public class HyperlinkDetector implements ISourceHyperlinkDetector, ILanguageService {
    private IReferenceResolver fResolver;
    private final Language fLanguage;

    public HyperlinkDetector(Language lang) {
	fLanguage= lang;
    }

    public IHyperlink[] detectHyperlinks(
    		final IRegion region, UniversalEditor editor, final ITextViewer textViewer, IParseController parseController)
    {
    	// This is the only language-specific bit ...
	if (fResolver == null)
	    fResolver= (IReferenceResolver) ExtensionPointFactory.createExtensionPoint(fLanguage, RuntimePlugin.UIDE_RUNTIME, "referenceResolvers");

	if (fResolver == null)
	    return null;

	// Get stuff for getting link source node
        Object ast= parseController.getCurrentAst();
        if (ast == null) return null;
        int offset= region.getOffset();
        IASTNodeLocator nodeLocator = parseController.getNodeLocator();

        // Get link source node
        Object source = nodeLocator.findNode(ast, offset);
        if (source == null) return null;

        // Got a suitable link source node; get link target node
       	final Object target = fResolver.getLinkTarget(source, parseController);
       	if (target == null) return null;

        // Link target node exists; get info for new hyperlink
       	// Note:  source presumably has a legitimate starting offset
       	// and length (since they have been selected from the source file)
        final int srcStart= nodeLocator.getStartOffset(source);
        final int srcLength= nodeLocator.getEndOffset(source) - srcStart + 1;

        // The target (depending on what--and where--the target is) may not have a
        // legitimate location (or one wihtin the file). In that case, set the target
        // to the beginning of the file and give it a nominal length.

        final int targetStart= (nodeLocator.getStartOffset(target) < 0) ? 0 : nodeLocator.getStartOffset(target);
        //final int targetLength= (targetStart == 0) ? 1 : nodeLocator.getEndOffset(target) - targetStart + 1;
        final int targetLength= nodeLocator.getEndOffset(target) - targetStart + 1;
        final String linkText = fResolver.getLinkText(source);
       	
        // Create and return the new hyperlink
        IHyperlink[] result = new IHyperlink[] {
            new TargetLink(linkText, srcStart, textViewer, editor, target, srcLength, targetStart, targetLength)
        };
        
        return result;
    }
}
