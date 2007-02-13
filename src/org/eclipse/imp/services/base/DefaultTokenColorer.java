package org.eclipse.uide.defaults;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.editor.ITokenColorer;
import org.eclipse.uide.parser.IParseController;

import lpg.javaruntime.IToken;

/**
 * @author CLaffra
 */
public class DefaultTokenColorer implements ITokenColorer {

    TextAttribute boldYellowRed;
        
	public TextAttribute getColoring(IParseController controller, IToken token) {
        if (controller.isKeyword(token.getKind()))
            return boldYellowRed;
        return null;
    }

    public void setLanguage(String language) {
        ErrorHandler.reportError("No Token colorer defined for \""+language+"\"");
        Display display = Display.getDefault();
        Color redColor = display.getSystemColor(SWT.COLOR_RED);
        Color yellowColor = display.getSystemColor(SWT.COLOR_YELLOW);
        boldYellowRed = new TextAttribute(redColor, yellowColor, SWT.BOLD); 
    }
 
}
					
