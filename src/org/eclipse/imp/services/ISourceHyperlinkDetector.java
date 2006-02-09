/*
 * Created on Feb 8, 2006
 */
package org.eclipse.uide.editor;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.uide.parser.IParseController;

public interface ISourceHyperlinkDetector {
    IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, IParseController parseController);
}
