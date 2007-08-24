/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
/*
 * Created on Feb 8, 2006
 */
package org.eclipse.imp.services;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public interface ISourceHyperlinkDetector {
    IHyperlink[] detectHyperlinks(IRegion region, UniversalEditor editor, ITextViewer textViewer, IParseController parseController);
}
