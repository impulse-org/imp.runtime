/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.editor.internal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.editor.TargetLink;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.ServiceFactory;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.services.IReferenceResolver;
import org.eclipse.imp.services.ISourceHyperlinkDetector;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IFileEditorInput;

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
	if (fResolver == null) {
		fResolver = ServiceFactory.getInstance().getReferenceResolver(fLanguage);
	}

	// SMS 17 Aug 2007
	if (fResolver == null)
	    return null;

	if (parseController == null)
		return null;
	
	// Get stuff for getting link source node
        Object ast= parseController.getCurrentAst();
        if (ast == null) return null;
        int offset= region.getOffset();
        ISourcePositionLocator nodeLocator = parseController.getNodeLocator();

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
        final int targetLength= nodeLocator.getEndOffset(target) - targetStart + 1;

        // Use the file path info to determine whether the target editor is the same as
        // the source editor, and initialize the TargetLink accordingly.
        final IPath targetPath= nodeLocator.getPath(target);
        // SMS 10 Sep 2007
        if (targetPath == null) {
        	//System.out.println("HyperlinkDetector.detectHyperlinks(..):  targetPath == null, returning null");
        	return null;
        }
        final String linkText = fResolver.getLinkText(source);

        IPath srcPath= ((IFileEditorInput) editor.getEditorInput()).getFile().getLocation();
        // SMS 11 Jun 2007:  default implementation of getPath in NodeLocator template returns
        // an empty path, so test for that here and assume it means that the link target is in 
        // the same unit as the link source
        UniversalEditor targetEditor= ((targetPath.segmentCount() == 0 || targetPath.equals(srcPath)) ? editor : null);
        Object targetArg= targetEditor == null ? targetPath : target;

        // If the target is exactly the same entity, don't bother with the hyperlink.
        if (srcStart == targetStart && srcLength == targetLength && targetPath.equals(srcPath))
            return null;

        IHyperlink[] result = new IHyperlink[] {
            new TargetLink(linkText, srcStart, srcLength, targetArg, targetStart, targetLength, targetEditor)
        };

        return result;
    }
}
